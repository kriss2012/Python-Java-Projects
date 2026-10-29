"""
Logic Legend - PY-08 Price Tracker with Web Scraping
Database Management Layer (SQLite)
"""

import sqlite3
import os
from datetime import datetime, timedelta

class PriceTrackerDB:
    def __init__(self, db_path="data/price_tracker.db"):
        self.db_path = db_path
        db_dir = os.path.dirname(self.db_path)
        if db_dir and not os.path.exists(db_dir):
            os.makedirs(db_dir, exist_ok=True)
        self.init_db()

    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON")
        return conn

    def init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()

            # Tracked Products Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    url TEXT UNIQUE NOT NULL,
                    title TEXT NOT NULL,
                    target_price REAL NOT NULL CHECK(target_price > 0),
                    current_price REAL DEFAULT 0.0,
                    currency TEXT DEFAULT 'INR',
                    rating REAL DEFAULT 4.5,
                    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'PAUSED')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Price History Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS price_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    price REAL NOT NULL CHECK(price >= 0),
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                )
            """)

            # Alert Logs Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS alert_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    trigger_price REAL NOT NULL,
                    target_price REAL NOT NULL,
                    recipient_email TEXT NOT NULL,
                    status TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                )
            """)

            # Error Logs Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS error_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER,
                    url TEXT,
                    error_message TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            conn.commit()

    def seed_demo_data(self):
        """Seeds realistic sample tracked products and 7-day price histories."""
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) as count FROM products")
            if cursor.fetchone()["count"] == 0:
                sample_products = [
                    ("https://example.com/p/sony-wh-1000xm5", "Sony WH-1000XM5 Wireless Headphones", 24999.0, 26990.0, "INR", 4.7, "ACTIVE"),
                    ("https://example.com/p/logitech-mx-master-3s", "Logitech MX Master 3S Wireless Mouse", 8500.0, 7999.0, "INR", 4.8, "ACTIVE"),
                    ("https://example.com/p/dell-4k-monitor-27", "Dell UltraSharp 27-inch 4K Monitor", 38000.0, 42500.0, "INR", 4.6, "ACTIVE"),
                    ("https://example.com/p/python-fluent-book", "Fluent Python 2nd Edition (Paperback)", 2200.0, 2450.0, "INR", 4.9, "PAUSED")
                ]
                cursor.executemany("""
                    INSERT INTO products (url, title, target_price, current_price, currency, rating, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """, sample_products)

                # Seed historical price drops/fluctuations for the past 7 days
                cursor.execute("SELECT id, current_price FROM products")
                prods = cursor.fetchall()
                now = datetime.now()

                for p in prods:
                    pid = p["id"]
                    base_price = p["current_price"]
                    # Generate 7 historical points
                    for days_ago in range(6, -1, -1):
                        dt = (now - timedelta(days=days_ago)).strftime("%Y-%m-%d %H:%M:%S")
                        # Add variance
                        mult = 1.0 + (days_ago * 0.015)
                        if pid == 2 and days_ago == 0:
                            # Product 2 dropped below target price!
                            hist_price = 7999.0
                        else:
                            hist_price = round(base_price * mult, 2)

                        cursor.execute("""
                            INSERT INTO price_history (product_id, price, timestamp)
                            VALUES (?, ?, ?)
                        """, (pid, hist_price, dt))

                conn.commit()

