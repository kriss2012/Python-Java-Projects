"""
Logic Legend - PY-03 Face Recognition Attendance System
Database Management Layer (SQLite)
"""

import sqlite3
import hashlib
import os
from datetime import datetime, timedelta

class AttendanceDatabase:
    def __init__(self, db_path="data/attendance.db"):
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

    @staticmethod
    def hash_password(password):
        return hashlib.sha256(password.encode("utf-8")).hexdigest()

    def init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()

            # Students Table with mandatory consent tracking
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    roll_number TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    department TEXT NOT NULL,
                    semester INTEGER DEFAULT 1,
                    consent_given INTEGER NOT NULL CHECK(consent_given IN (0, 1)),
                    photo_path TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Attendance Table with duplicate prevention
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    date TEXT NOT NULL, -- YYYY-MM-DD
                    time TEXT NOT NULL, -- HH:MM:SS
                    session_name TEXT NOT NULL,
                    confidence REAL DEFAULT 1.0,
                    mode TEXT NOT NULL CHECK(mode IN ('FACE_RECOGNITION', 'MANUAL')),
                    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    UNIQUE(student_id, date, session_name)
                )
            """)

            # Admin Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS admin_users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT DEFAULT 'admin'
                )
            """)

            cursor.execute("SELECT COUNT(*) as count FROM admin_users")
            if cursor.fetchone()["count"] == 0:
                h = self.hash_password("admin123")
                cursor.execute(
                    "INSERT INTO admin_users (username, password_hash, role) VALUES (?, ?, ?)",
                    ("admin", h, "superadmin")
                )

            conn.commit()

    def seed_demo_data(self):
        """Seeds realistic student roster and attendance history for demonstrations."""
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) as count FROM students")
            if cursor.fetchone()["count"] == 0:
                students_data = [
                    ("CSE-2026-001", "Aarav Sharma", "Computer Science", 6, 1, ""),
                    ("CSE-2026-002", "Diya Patel", "Computer Science", 6, 1, ""),
                    ("CSE-2026-003", "Rohan Mehta", "Information Technology", 6, 1, ""),
                    ("CSE-2026-004", "Ananya Iyer", "Electronics & Comm", 4, 1, ""),
                    ("CSE-2026-005", "Vikram Singh", "Mechanical Eng", 4, 1, "")
                ]
                cursor.executemany("""
                    INSERT INTO students (roll_number, name, department, semester, consent_given, photo_path)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, students_data)

                # Seed historical attendance records for the past 5 days
                today = datetime.now()
                cursor.execute("SELECT id, roll_number FROM students")
                students = cursor.fetchall()

                attendance_records = []
                for days_ago in range(5, 0, -1):
                    rec_date = (today - timedelta(days=days_ago)).strftime("%Y-%m-%d")
                    # Student 1 has 100% attendance, Student 5 has low attendance
                    attendance_records.append((students[0]["id"], rec_date, "09:05:12", "Morning Lecture", 0.94, "FACE_RECOGNITION"))
                    attendance_records.append((students[1]["id"], rec_date, "09:08:44", "Morning Lecture", 0.88, "FACE_RECOGNITION"))
                    attendance_records.append((students[2]["id"], rec_date, "09:12:01", "Morning Lecture", 0.91, "FACE_RECOGNITION"))
                    if days_ago in (1, 2): # Student 3 missed earlier days
                        attendance_records.append((students[3]["id"], rec_date, "09:15:30", "Morning Lecture", 0.85, "MANUAL"))
                    if days_ago == 1: # Student 5 only attended 1 day -> will trigger low attendance!
                        attendance_records.append((students[4]["id"], rec_date, "09:18:20", "Morning Lecture", 0.79, "FACE_RECOGNITION"))

                for rec in attendance_records:
                    cursor.execute("""
                        INSERT OR IGNORE INTO attendance (student_id, date, time, session_name, confidence, mode)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """, rec)

                conn.commit()

