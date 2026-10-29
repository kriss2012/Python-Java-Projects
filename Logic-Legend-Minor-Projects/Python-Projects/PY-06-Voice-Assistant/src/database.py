"""
Logic Legend - PY-06 Personal Voice Assistant
Database Layer (SQLite) for Tasks, Reminders & Audit Logs
"""

import sqlite3
import os
from datetime import datetime

class AssistantDatabase:
    def __init__(self, db_path="data/assistant.db"):
        self.db_path = db_path
        db_dir = os.path.dirname(self.db_path)
        if db_dir and not os.path.exists(db_dir):
            os.makedirs(db_dir, exist_ok=True)
        self.init_db()

    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()

            # Tasks and Reminders Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS todos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    due_date TEXT,
                    status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'COMPLETED')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Command History Audit Log
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS command_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    command_text TEXT NOT NULL,
                    response_text TEXT NOT NULL,
                    status TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Seed default tasks if empty
            cursor.execute("SELECT COUNT(*) as count FROM todos")
            if cursor.fetchone()["count"] == 0:
                sample_tasks = [
                    ("Prepare Data Structures minor project report", "2026-11-05", "PENDING"),
                    ("Submit Machine Learning assignment on GitHub", "2026-11-08", "PENDING"),
                    ("Review Computer Networks viva question deck", "2026-11-12", "PENDING")
                ]
                cursor.executemany("INSERT INTO todos (title, due_date, status) VALUES (?, ?, ?)", sample_tasks)

            conn.commit()

    def add_todo(self, title, due_date=None):
        title = title.strip()
        if not title:
            return False, "Task title cannot be empty."
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO todos (title, due_date) VALUES (?, ?)", (title, due_date))
            conn.commit()
            return True, cursor.lastrowid

    def get_todos(self, status=None):
        query = "SELECT * FROM todos"
        params = []
        if status:
            query += " WHERE status = ?"
            params.append(status)
        query += " ORDER BY id DESC"
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(r) for r in cursor.fetchall()]

    def complete_todo(self, todo_id):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("UPDATE todos SET status = 'COMPLETED' WHERE id = ?", (todo_id,))
            conn.commit()
            return cursor.rowcount > 0

    def delete_todo(self, todo_id):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM todos WHERE id = ?", (todo_id,))
            conn.commit()
            return cursor.rowcount > 0

    def clear_all_todos(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM todos")
            conn.commit()
            return True

    def log_command(self, cmd_text, resp_text, status="SUCCESS"):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO command_history (command_text, response_text, status)
                VALUES (?, ?, ?)
            """, (cmd_text, resp_text, status))
            conn.commit()

    def get_command_history(self, limit=30):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM command_history ORDER BY id DESC LIMIT ?", (limit,))
            return [dict(r) for r in cursor.fetchall()]
