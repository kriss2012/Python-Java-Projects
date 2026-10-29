"""
Logic Legend - PY-02 AI Chatbot for College Queries
Database Layer (SQLite) for Logging and Admin Escalations
"""

import sqlite3
import hashlib
import os
from datetime import datetime

class ChatbotDatabase:
    def __init__(self, db_path="data/chatbot.db"):
        self.db_path = db_path
        db_dir = os.path.dirname(self.db_path)
        if db_dir and not os.path.exists(db_dir):
            os.makedirs(db_dir, exist_ok=True)
        self.init_db()

    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    @staticmethod
    def hash_password(password):
        return hashlib.sha256(password.encode("utf-8")).hexdigest()

    def init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()

            # Chat Conversation Logs Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS chat_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    user_message TEXT NOT NULL,
                    bot_response TEXT NOT NULL,
                    detected_intent TEXT,
                    confidence REAL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Escalations / Talk to Admin Requests
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS escalations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL,
                    student_email TEXT NOT NULL,
                    student_phone TEXT,
                    query_text TEXT NOT NULL,
                    status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'RESOLVED')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    resolved_at TIMESTAMP
                )
            """)

            # Admin Accounts Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS admin_users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT DEFAULT 'staff'
                )
            """)

            # Seed default admin user if absent
            cursor.execute("SELECT COUNT(*) as count FROM admin_users")
            if cursor.fetchone()["count"] == 0:
                default_hash = self.hash_password("admin123")
                cursor.execute(
                    "INSERT INTO admin_users (username, password_hash, role) VALUES (?, ?, ?)",
                    ("admin", default_hash, "superadmin")
                )

            conn.commit()

    def log_interaction(self, session_id, user_message, bot_response, detected_intent, confidence):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO chat_logs (session_id, user_message, bot_response, detected_intent, confidence)
                VALUES (?, ?, ?, ?, ?)
            """, (session_id, user_message, bot_response, detected_intent, confidence))
            conn.commit()

    def create_escalation(self, name, email, phone, query):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO escalations (student_name, student_email, student_phone, query_text)
                VALUES (?, ?, ?, ?)
            """, (name.strip(), email.strip(), phone.strip(), query.strip()))
            conn.commit()
            return cursor.lastrowid

    def get_escalations(self, status=None):
        query = "SELECT * FROM escalations"
        params = []
        if status:
            query += " WHERE status = ?"
            params.append(status)
        query += " ORDER BY created_at DESC"

        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(r) for r in cursor.fetchall()]

    def resolve_escalation(self, esc_id):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            cursor.execute("UPDATE escalations SET status = 'RESOLVED', resolved_at = ? WHERE id = ?", (now, esc_id))
            conn.commit()
            return cursor.rowcount > 0

    def verify_admin(self, username, password):
        h = self.hash_password(password)
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM admin_users WHERE username = ? AND password_hash = ?", (username, h))
            user = cursor.fetchone()
            return dict(user) if user else None

    def get_chat_history(self, session_id=None, limit=50):
        query = "SELECT * FROM chat_logs"
        params = []
        if session_id:
            query += " WHERE session_id = ?"
            params.append(session_id)
        query += " ORDER BY timestamp DESC LIMIT ?"
        params.append(limit)

        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(r) for r in cursor.fetchall()]

