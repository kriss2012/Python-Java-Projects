"""
Logic Legend - PY-09 Secure Password Manager
Database Management Layer (SQLite)
"""

import sqlite3
import os

class VaultDB:
    def __init__(self, db_path="data/password_manager.db"):
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

            # Master Authentication Table (single row allowed)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS master_auth (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    salt_hex TEXT NOT NULL,
                    key_check_token TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Encrypted Vault Credentials Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS vault_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    username TEXT NOT NULL,
                    encrypted_password TEXT NOT NULL,
                    url TEXT,
                    category TEXT DEFAULT 'General',
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Security Audit Logs Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS security_audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_type TEXT NOT NULL,
                    details TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)
            conn.commit()

    def is_master_initialized(self) -> bool:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT id FROM master_auth WHERE id = 1")
            return cursor.fetchone() is not None

    def store_master_auth(self, salt_hex: str, key_check_token: str):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT OR REPLACE INTO master_auth (id, salt_hex, key_check_token)
                VALUES (1, ?, ?)
            """, (salt_hex, key_check_token))
            conn.commit()

    def get_master_auth(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT salt_hex, key_check_token FROM master_auth WHERE id = 1")
            row = cursor.fetchone()
            if row:
                return dict(row)
            return None

    def log_event(self, event_type: str, details: str):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO security_audit_logs (event_type, details)
                VALUES (?, ?)
            """, (event_type, details))
            conn.commit()

