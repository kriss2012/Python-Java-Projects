"""
Logic Legend - PY-01 Smart Expense Tracker with Analytics
Database Management Layer (SQLite)
"""

import sqlite3
import os
from datetime import datetime

DEFAULT_CATEGORIES = [
    ("Salary", "income"),
    ("Freelance & Side Hustles", "income"),
    ("Investment Returns", "income"),
    ("Gifts & Grants", "income"),
    ("Food & Dining", "expense"),
    ("Rent & Utilities", "expense"),
    ("Transportation", "expense"),
    ("Shopping & Groceries", "expense"),
    ("Entertainment & Subscriptions", "expense"),
    ("Healthcare & Medical", "expense"),
    ("Education & Books", "expense"),
    ("Miscellaneous", "expense")
]

class DatabaseManager:
    def __init__(self, db_path="data/expenses.db"):
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
            
            # Categories Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    type TEXT NOT NULL CHECK(type IN ('income', 'expense'))
                )
            """)

            # Transactions Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL CHECK(type IN ('income', 'expense')),
                    amount REAL NOT NULL CHECK(amount > 0),
                    category TEXT NOT NULL,
                    description TEXT,
                    date TEXT NOT NULL,
                    payment_method TEXT DEFAULT 'Cash',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Budgets Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    month_year TEXT NOT NULL, -- Format: YYYY-MM
                    limit_amount REAL NOT NULL CHECK(limit_amount > 0),
                    threshold_percent REAL DEFAULT 80.0,
                    UNIQUE(category, month_year)
                )
            """)

            # Seed default categories if empty
            cursor.execute("SELECT COUNT(*) as count FROM categories")
            if cursor.fetchone()["count"] == 0:
                cursor.executemany(
                    "INSERT OR IGNORE INTO categories (name, type) VALUES (?, ?)",
                    DEFAULT_CATEGORIES
                )

            conn.commit()

    def seed_demo_data(self):
        """Seeds realistic initial records for testing and demonstration."""
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) as count FROM transactions")
            if cursor.fetchone()["count"] == 0:
                current_month = datetime.now().strftime("%Y-%m")
                sample_transactions = [
                    ('income', 45000.0, 'Salary', 'Monthly Tech Stipend & Salary', f'{current_month}-01', 'Bank Transfer'),
                    ('income', 8500.0, 'Freelance & Side Hustles', 'College Web Design Project', f'{current_month}-05', 'UPI'),
                    ('expense', 12000.0, 'Rent & Utilities', 'Hostel / Apartment Rent & Wifi', f'{current_month}-02', 'Bank Transfer'),
                    ('expense', 4200.0, 'Food & Dining', 'Mess Dues & Campus Cafeteria', f'{current_month}-07', 'UPI'),
                    ('expense', 1500.0, 'Transportation', 'Metro Card Recharge & Auto', f'{current_month}-10', 'Card'),
                    ('expense', 2300.0, 'Education & Books', 'Data Structures & ML Reference Books', f'{current_month}-12', 'UPI'),
                    ('expense', 1800.0, 'Entertainment & Subscriptions', 'Streaming & Hackathon Pass', f'{current_month}-15', 'Card')
                ]
                cursor.executemany("""
                    INSERT INTO transactions (type, amount, category, description, date, payment_method)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, sample_transactions)

                # Seed sample budget
                sample_budgets = [
                    ('Food & Dining', current_month, 5000.0, 80.0),
                    ('Entertainment & Subscriptions', current_month, 2000.0, 75.0),
                    ('Shopping & Groceries', current_month, 3000.0, 80.0)
                ]
                cursor.executemany("""
                    INSERT OR REPLACE INTO budgets (category, month_year, limit_amount, threshold_percent)
                    VALUES (?, ?, ?, ?)
                """, sample_budgets)

                conn.commit()

