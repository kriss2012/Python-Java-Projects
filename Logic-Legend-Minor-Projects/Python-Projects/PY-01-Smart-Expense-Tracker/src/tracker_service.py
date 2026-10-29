"""
Logic Legend - PY-01 Smart Expense Tracker with Analytics
Core Business Logic and Service Layer
"""

import csv
import re
import os
from datetime import datetime

class TrackerService:
    def __init__(self, db_manager):
        self.db = db_manager

    # ----------------- Validation -----------------
    @staticmethod
    def validate_amount(amount):
        try:
            val = float(amount)
            if val <= 0:
                return False, "Amount must be strictly greater than 0."
            return True, round(val, 2)
        except (ValueError, TypeError):
            return False, "Invalid numeric value for amount."

    @staticmethod
    def validate_date(date_str):
        try:
            datetime.strptime(date_str, "%Y-%m-%d")
            return True, date_str
        except (ValueError, TypeError):
            return False, "Invalid date format. Expected YYYY-MM-DD."

    # ----------------- Category Operations -----------------
    def get_categories(self, category_type=None):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            if category_type:
                cursor.execute("SELECT name, type FROM categories WHERE type = ? ORDER BY name ASC", (category_type,))
            else:
                cursor.execute("SELECT name, type FROM categories ORDER BY type DESC, name ASC")
            return [dict(row) for row in cursor.fetchall()]

    def add_category(self, name, category_type):
        name = name.strip()
        if not name:
            return False, "Category name cannot be empty."
        if category_type not in ('income', 'expense'):
            return False, "Category type must be 'income' or 'expense'."
        
        try:
            with self.db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("INSERT INTO categories (name, type) VALUES (?, ?)", (name, category_type))
                conn.commit()
                return True, f"Category '{name}' created successfully."
        except Exception as e:
            return False, f"Could not add category: {str(e)}"

    # ----------------- Transaction Operations -----------------
    def add_transaction(self, t_type, amount, category, description, date_str, payment_method="Cash"):
        is_valid_amt, amt_val = self.validate_amount(amount)
        if not is_valid_amt:
            return False, amt_val

        is_valid_date, d_val = self.validate_date(date_str)
        if not is_valid_date:
            return False, d_val

        if t_type not in ('income', 'expense'):
            return False, "Type must be either 'income' or 'expense'."

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO transactions (type, amount, category, description, date, payment_method)
                VALUES (?, ?, ?, ?, ?, ?)
            """, (t_type, amt_val, category, description.strip(), d_val, payment_method))
            conn.commit()
            return True, "Transaction added successfully."

    def update_transaction(self, t_id, t_type, amount, category, description, date_str, payment_method):
        is_valid_amt, amt_val = self.validate_amount(amount)
        if not is_valid_amt:
            return False, amt_val

        is_valid_date, d_val = self.validate_date(date_str)
        if not is_valid_date:
            return False, d_val

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                UPDATE transactions
                SET type = ?, amount = ?, category = ?, description = ?, date = ?, payment_method = ?
                WHERE id = ?
            """, (t_type, amt_val, category, description.strip(), d_val, payment_method, t_id))
            conn.commit()
            if cursor.rowcount > 0:
                return True, "Transaction updated successfully."
            return False, "Transaction ID not found."

    def delete_transaction(self, t_id):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM transactions WHERE id = ?", (t_id,))
            conn.commit()
            return cursor.rowcount > 0

    def get_transactions(self, t_type=None, category=None, start_date=None, end_date=None, search_keyword=None):
        query = "SELECT * FROM transactions WHERE 1=1"
        params = []

        if t_type and t_type != "All":
            query += " AND type = ?"
            params.append(t_type.lower())
        if category and category != "All":
            query += " AND category = ?"
            params.append(category)
        if start_date:
            query += " AND date >= ?"
            params.append(start_date)
        if end_date:
            query += " AND date <= ?"
            params.append(end_date)
        if search_keyword:
            query += " AND (description LIKE ? OR category LIKE ?)"
            params.extend([f"%{search_keyword}%", f"%{search_keyword}%"])

        query += " ORDER BY date DESC, id DESC"

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(row) for row in cursor.fetchall()]

    # ----------------- Financial Analytics -----------------
    def get_financial_summary(self, month_year=None):
        """Calculates total income, total expense, balance, and savings rate."""
        query = "SELECT type, SUM(amount) as total FROM transactions WHERE 1=1"
        params = []
        if month_year:
            query += " AND date LIKE ?"
            params.append(f"{month_year}%")
        query += " GROUP BY type"

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            rows = cursor.fetchall()
            
        summary = {"income": 0.0, "expense": 0.0, "balance": 0.0, "savings_rate": 0.0}
        for row in rows:
            if row["type"] == "income":
                summary["income"] = round(row["total"], 2)
            elif row["type"] == "expense":
                summary["expense"] = round(row["total"], 2)

        summary["balance"] = round(summary["income"] - summary["expense"], 2)
        if summary["income"] > 0:
            summary["savings_rate"] = round((summary["balance"] / summary["income"]) * 100, 1)

        return summary

    def get_category_breakdown(self, t_type="expense", month_year=None):
        query = """
            SELECT category, SUM(amount) as total
            FROM transactions
            WHERE type = ?
        """
        params = [t_type]
        if month_year:
            query += " AND date LIKE ?"
            params.append(f"{month_year}%")
        query += " GROUP BY category ORDER BY total DESC"

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(row) for row in cursor.fetchall()]

    def get_monthly_trends(self, limit=6):
        query = """
            SELECT substr(date, 1, 7) as month, type, SUM(amount) as total
            FROM transactions
            GROUP BY month, type
            ORDER BY month ASC
        """
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query)
            rows = cursor.fetchall()

        months_dict = {}
        for row in rows:
            m = row["month"]
            if m not in months_dict:
                months_dict[m] = {"month": m, "income": 0.0, "expense": 0.0}
            months_dict[m][row["type"]] = round(row["total"], 2)

        trend_list = list(months_dict.values())
        return trend_list[-limit:]

    # ----------------- Budget Management & Threshold Alerts -----------------
    def set_budget(self, category, month_year, limit_amount, threshold_percent=80.0):
        is_valid_amt, amt_val = self.validate_amount(limit_amount)
        if not is_valid_amt:
            return False, amt_val
        try:
            thresh = float(threshold_percent)
            if thresh <= 0 or thresh > 100:
                thresh = 80.0
        except ValueError:
            thresh = 80.0

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT OR REPLACE INTO budgets (category, month_year, limit_amount, threshold_percent)
                VALUES (?, ?, ?, ?)
            """, (category, month_year, amt_val, thresh))
            conn.commit()
            return True, f"Budget for {category} ({month_year}) set to {amt_val}."

    def check_budgets(self, month_year=None):
        """Compares spending to configured budgets and flags threshold alerts."""
        if not month_year:
            month_year = datetime.now().strftime("%Y-%m")

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM budgets WHERE month_year = ?", (month_year,))
            budgets = [dict(r) for r in cursor.fetchall()]

            # Get current spending per category for this month
            cursor.execute("""
                SELECT category, SUM(amount) as spent
                FROM transactions
                WHERE type = 'expense' AND date LIKE ?
                GROUP BY category
            """, (f"{month_year}%",))
            spent_map = {r["category"]: r["spent"] for r in cursor.fetchall()}

        alerts = []
        for b in budgets:
            cat = b["category"]
            limit = b["limit_amount"]
            threshold = b["threshold_percent"]
            spent = spent_map.get(cat, 0.0)
            pct = round((spent / limit) * 100, 1) if limit > 0 else 0.0

            status = "OK"
            if spent >= limit:
                status = "EXCEEDED"
            elif pct >= threshold:
                status = "WARNING"

            alerts.append({
                "category": cat,
                "month_year": month_year,
                "limit": limit,
                "spent": round(spent, 2),
                "remaining": round(max(0.0, limit - spent), 2),
                "percent_used": pct,
                "threshold_percent": threshold,
                "status": status
            })

        return alerts

    # ----------------- Export Functions -----------------
    def export_csv(self, filepath="exports/transactions_export.csv"):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        records = self.get_transactions()
        if not records:
            return False, "No records to export."

        with open(filepath, mode="w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["id", "date", "type", "category", "amount", "payment_method", "description", "created_at"])
            writer.writeheader()
            for r in records:
                writer.writerow(r)

        return True, filepath

    def export_summary_report(self, filepath="exports/financial_report.txt", month_year=None):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        summary = self.get_financial_summary(month_year)
        breakdown = self.get_category_breakdown("expense", month_year)
        budget_alerts = self.check_budgets(month_year)
        period = month_year if month_year else "All-Time"

        lines = [
            "============================================================",
            f"       LOGIC LEGEND SMART EXPENSE TRACKER REPORT",
            f"       Reporting Period: {period}",
            f"       Generated At: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "============================================================",
            "",
            "1. FINANCIAL OVERVIEW",
            f"   Total Income       : ₹ {summary['income']:,.2f}",
            f"   Total Expenses     : ₹ {summary['expense']:,.2f}",
            f"   Net Balance        : ₹ {summary['balance']:,.2f}",
            f"   Savings Rate       : {summary['savings_rate']} %",
            "",
            "2. CATEGORY-WISE EXPENSE BREAKDOWN",
            "   --------------------------------------------------------"
        ]
        for item in breakdown:
            pct = round((item['total'] / summary['expense'] * 100), 1) if summary['expense'] > 0 else 0
            lines.append(f"   - {item['category']:<25}: ₹ {item['total']:>10,.2f} ({pct}%)")

        lines.extend([
            "",
            "3. MONTHLY BUDGET MONITORING",
            "   --------------------------------------------------------"
        ])
        if budget_alerts:
            for b in budget_alerts:
                lines.append(f"   [{b['status']:^8}] {b['category']}: Spent ₹{b['spent']} of ₹{b['limit']} ({b['percent_used']}%)")
        else:
            lines.append("   No active budgets configured for this period.")

        lines.extend([
            "",
            "============================================================",
            "End of Financial Statement - Generated by PY-01 Smart Expense Tracker"
        ])

        with open(filepath, "w", encoding="utf-8") as f:
            f.write("\n".join(lines))

        return True, filepath

    # ----------------- OCR Receipt Scanner (Extension) -----------------
    @staticmethod
    def parse_receipt_text(text):
        """
        Extracts transaction candidates (date, total amount, merchant)
        from raw receipt OCR text using robust regular expressions.
        """
        extracted = {
            "date": datetime.now().strftime("%Y-%m-%d"),
            "amount": 0.0,
            "merchant": "Unknown Store",
            "category": "Shopping & Groceries"
        }

        # Date regex: YYYY-MM-DD or DD/MM/YYYY or DD-MM-YYYY
        date_match = re.search(r'(\d{4}[-/]\d{2}[-/]\d{2})', text)
        if date_match:
            try:
                extracted["date"] = datetime.strptime(date_match.group(1).replace('/', '-'), "%Y-%m-%d").strftime("%Y-%m-%d")
            except ValueError:
                pass
        else:
            date_match2 = re.search(r'(\d{2}[-/]\d{2}[-/]\d{4})', text)
            if date_match2:
                try:
                    d_str = date_match2.group(1).replace('/', '-')
                    extracted["date"] = datetime.strptime(d_str, "%d-%m-%Y").strftime("%Y-%m-%d")
                except ValueError:
                    pass

        # Amount regex: looking for TOTAL, AMOUNT, NET, RS, INR followed by numbers
        amount_patterns = [
            r'(?:total|amount|net|grand total|rs\.?|inr)\s*[:=]?\s*([₹$€]?\s*\d+(?:\.\d{1,2})?)',
            r'([₹$€]?\s*\d+\.\d{2})'
        ]
        amounts_found = []
        for pat in amount_patterns:
            matches = re.findall(pat, text, re.IGNORECASE)
            for m in matches:
                clean_num = re.sub(r'[^\d.]', '', m)
                try:
                    val = float(clean_num)
                    if 0.5 < val < 1000000:
                        amounts_found.append(val)
                except ValueError:
                    continue

        if amounts_found:
            # Usually the total is the largest monetary value on the receipt
            extracted["amount"] = max(amounts_found)

        # Merchant extraction (first non-empty capitalized line)
        for line in text.splitlines():
            line_str = line.strip()
            if len(line_str) > 3 and not re.search(r'(receipt|invoice|bill|tax|date|total)', line_str, re.I):
                extracted["merchant"] = line_str[:30]
                break

        return extracted
