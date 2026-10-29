"""
Logic Legend - PY-08 Price Tracker with Web Scraping
Tracker Service & Price Trend Business Logic
"""

import os
import csv
from datetime import datetime

class TrackerService:
    def __init__(self, db, scraper, notifier):
        self.db = db
        self.scraper = scraper
        self.notifier = notifier

    def add_product(self, url, title, target_price):
        url = url.strip()
        title = title.strip()
        try:
            target = float(target_price)
            if target <= 0:
                return False, "Target price must be greater than zero."
        except (ValueError, TypeError):
            return False, "Target price must be a valid number."

        if not url or not title:
            return False, "URL and product title are required."

        # Fetch initial price
        scraped = self.scraper.fetch_product_details(url)
        initial_price = scraped.get("price", target * 1.1)
        rating = scraped.get("rating", 4.5)

        try:
            with self.db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("""
                    INSERT INTO products (url, title, target_price, current_price, rating, status)
                    VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                """, (url, title, target, initial_price, rating))
                pid = cursor.lastrowid

                # Record first history entry
                cursor.execute("""
                    INSERT INTO price_history (product_id, price)
                    VALUES (?, ?)
                """, (pid, initial_price))
                conn.commit()

            return True, f"Product '{title}' added to tracker. Initial price: ₹{initial_price:,.2f}"
        except Exception as e:
            return False, f"Could not add product: {str(e)}"

    def get_all_products(self):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM products ORDER BY id DESC")
            return [dict(r) for r in cursor.fetchall()]

    def toggle_product_status(self, product_id):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT status FROM products WHERE id = ?", (product_id,))
            row = cursor.fetchone()
            if not row:
                return False, "Product not found."
            new_status = "PAUSED" if row["status"] == "ACTIVE" else "ACTIVE"
            cursor.execute("UPDATE products SET status = ? WHERE id = ?", (new_status, product_id))
            conn.commit()
            return True, new_status

    def delete_product(self, product_id):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM products WHERE id = ?", (product_id,))
            conn.commit()
            return cursor.rowcount > 0

    def check_product_price(self, product_id):
        """Scrapes current price, updates history, and triggers email alert if threshold met."""
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM products WHERE id = ?", (product_id,))
            product = cursor.fetchone()

        if not product:
            return {"error": "Product ID not found."}

        res = self.scraper.fetch_product_details(product["url"])
        if "error" in res:
            # Log error
            with self.db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("""
                    INSERT INTO error_logs (product_id, url, error_message)
                    VALUES (?, ?, ?)
                """, (product_id, product["url"], res["error"]))
                conn.commit()
            return {"error": res["error"]}

        new_price = res["price"]
        target = product["target_price"]

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("UPDATE products SET current_price = ? WHERE id = ?", (new_price, product_id))
            cursor.execute("INSERT INTO price_history (product_id, price) VALUES (?, ?)", (product_id, new_price))
            conn.commit()

        alert_dispatched = False
        alert_info = None

        # Check threshold trigger
        if new_price <= target:
            alert_res = self.notifier.send_price_drop_alert(
                product_id=product_id,
                title=product["title"],
                current_price=new_price,
                target_price=target,
                url=product["url"]
            )
            alert_dispatched = True
            alert_info = alert_res

        return {
            "product_id": product_id,
            "title": product["title"],
            "old_price": product["current_price"],
            "new_price": new_price,
            "target_price": target,
            "alert_triggered": alert_dispatched,
            "alert_details": alert_info
        }

    def check_all_active(self):
        products = self.get_all_products()
        results = []
        for p in products:
            if p["status"] == "ACTIVE":
                res = self.check_product_price(p["id"])
                results.append(res)
        return results

    def get_price_history(self, product_id):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT price, timestamp FROM price_history
                WHERE product_id = ?
                ORDER BY timestamp ASC
            """, (product_id,))
            return [dict(r) for r in cursor.fetchall()]

    def analyze_best_time_to_buy(self, product_id):
        """Analyzes historical price trends to advise whether to buy or wait."""
        history = self.get_price_history(product_id)
        if not history or len(history) < 2:
            return {"advice": "Insufficient historical data points.", "verdict": "WAIT"}

        prices = [h["price"] for h in history]
        min_p = min(prices)
        max_p = max(prices)
        avg_p = sum(prices) / len(prices)
        current = prices[-1]

        if current <= min_p * 1.02:
            verdict = "BUY NOW"
            advice = f"Price is at or near its all-time low (₹{current:,.2f}). Excellent time to purchase."
        elif current >= avg_p:
            verdict = "WAIT FOR DROP"
            advice = f"Price (₹{current:,.2f}) is above the 30-day average of ₹{avg_p:,.2f}. Wait for sales."
        else:
            verdict = "FAIR VALUE"
            advice = f"Price (₹{current:,.2f}) is within normal fluctuations below average."

        return {
            "verdict": verdict,
            "advice": advice,
            "min_price": min_p,
            "max_price": max_p,
            "avg_price": round(avg_p, 2),
            "current_price": current
        }

    def export_csv(self, filepath="exports/price_history_export.csv"):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT p.title, p.url, p.target_price, h.price, h.timestamp
                FROM price_history h
                JOIN products p ON h.product_id = p.id
                ORDER BY p.title, h.timestamp ASC
            """)
            rows = [dict(r) for r in cursor.fetchall()]

        if not rows:
            return False, "No price history available to export."

        with open(filepath, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["title", "url", "target_price", "price", "timestamp"])
            writer.writeheader()
            for r in rows:
                writer.writerow(r)

        return True, filepath

