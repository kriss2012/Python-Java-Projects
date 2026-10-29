"""
Logic Legend - PY-03 Face Recognition Attendance System
Attendance Service & Business Logic Layer
"""

import os
import csv
import cv2
from datetime import datetime

class AttendanceService:
    def __init__(self, db_manager, face_engine, samples_dir="samples"):
        self.db = db_manager
        self.face_engine = face_engine
        self.samples_dir = samples_dir
        if not os.path.exists(self.samples_dir):
            os.makedirs(self.samples_dir, exist_ok=True)
        self.enrolled_signatures = {}
        self.load_enrolled_signatures()

    def load_enrolled_signatures(self):
        """Loads and precomputes face signatures for enrolled students with photos."""
        self.enrolled_signatures = {}
        students = self.get_all_students()
        for s in students:
            p_path = s.get("photo_path")
            if p_path and os.path.exists(p_path):
                img = cv2.imread(p_path)
                if img is not None:
                    sig = self.face_engine.extract_face_signature(img)
                    if sig:
                        self.enrolled_signatures[s["id"]] = sig

    def get_all_students(self):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM students ORDER BY roll_number ASC")
            return [dict(r) for r in cursor.fetchall()]

    def get_student_by_id(self, student_id):
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM students WHERE id = ?", (student_id,))
            row = cursor.fetchone()
            return dict(row) if row else None

    def enroll_student(self, roll_number, name, department, semester, consent_given, face_bgr=None):
        roll = roll_number.strip().upper()
        name = name.strip()
        dept = department.strip()

        if not roll or not name or not dept:
            return False, "Roll number, student name, and department are mandatory."

        if not consent_given:
            return False, "Informed biometric consent is strictly mandatory before enrollment."

        photo_path = ""
        if face_bgr is not None:
            photo_filename = f"{roll.replace('/', '_')}_face.png"
            photo_path = os.path.join(self.samples_dir, photo_filename)
            cv2.imwrite(photo_path, face_bgr)

        try:
            with self.db.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("""
                    INSERT INTO students (roll_number, name, department, semester, consent_given, photo_path)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, (roll, name, dept, int(semester), 1 if consent_given else 0, photo_path))
                conn.commit()
                student_id = cursor.lastrowid

            # Update in-memory signature cache
            if face_bgr is not None:
                sig = self.face_engine.extract_face_signature(face_bgr)
                if sig:
                    self.enrolled_signatures[student_id] = sig

            return True, f"Student {name} ({roll}) enrolled successfully."
        except Exception as e:
            return False, f"Enrollment error: {str(e)}"

    def mark_attendance(self, student_id, session_name="Morning Lecture", mode="FACE_RECOGNITION", confidence=1.0):
        student = self.get_student_by_id(student_id)
        if not student:
            return False, "Invalid student identifier."

        today_date = datetime.now().strftime("%Y-%m-%d")
        current_time = datetime.now().strftime("%H:%M:%S")

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            # Check duplicate attendance for today's session
            cursor.execute("""
                SELECT * FROM attendance
                WHERE student_id = ? AND date = ? AND session_name = ?
            """, (student_id, today_date, session_name))
            existing = cursor.fetchone()

            if existing:
                return False, f"Duplicate: Attendance already recorded today at {existing['time']} ({existing['mode']})."

            cursor.execute("""
                INSERT INTO attendance (student_id, date, time, session_name, confidence, mode)
                VALUES (?, ?, ?, ?, ?, ?)
            """, (student_id, today_date, current_time, session_name, confidence, mode))
            conn.commit()

        return True, f"Attendance marked for {student['name']} ({student['roll_number']}) at {current_time}."

    def process_frame_for_attendance(self, frame_bgr, session_name="Morning Lecture"):
        """
        Analyzes a camera frame, detects face, compares with enrolled students,
        and logs attendance if matched.
        """
        if frame_bgr is None or frame_bgr.size == 0:
            return {"status": "NO_FRAME", "message": "Camera frame unavailable."}

        faces = self.face_engine.detect_faces(frame_bgr)
        if len(faces) == 0:
            return {"status": "NO_FACE", "message": "No face detected in frame."}

        # Take primary face
        x, y, w, h = faces[0]
        face_crop = frame_bgr[y:y+h, x:x+w]

        matched_id, score = self.face_engine.match_against_enrolled(face_crop, self.enrolled_signatures)
        if matched_id is None:
            return {
                "status": "UNKNOWN_FACE",
                "message": f"Face not recognized (Best confidence: {score:.2f}). Please register or mark manually.",
                "confidence": score
            }

        student = self.get_student_by_id(matched_id)
        success, msg = self.mark_attendance(matched_id, session_name, "FACE_RECOGNITION", score)
        return {
            "status": "SUCCESS" if success else "DUPLICATE",
            "message": msg,
            "student": student,
            "confidence": score,
            "bbox": (int(x), int(y), int(w), int(h))
        }

    def get_attendance_records(self, date_str=None, session_name=None, search_keyword=None):
        query = """
            SELECT a.id, a.date, a.time, a.session_name, a.confidence, a.mode,
                   s.roll_number, s.name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE 1=1
        """
        params = []
        if date_str:
            query += " AND a.date = ?"
            params.append(date_str)
        if session_name and session_name != "All":
            query += " AND a.session_name = ?"
            params.append(session_name)
        if search_keyword:
            query += " AND (s.name LIKE ? OR s.roll_number LIKE ?)"
            params.extend([f"%{search_keyword}%", f"%{search_keyword}%"])

        query += " ORDER BY a.date DESC, a.time DESC"

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(r) for r in cursor.fetchall()]

    def calculate_attendance_percentages(self, warning_threshold=75.0):
        """
        Calculates cumulative attendance percentage for all enrolled students
        and flags those falling below warning threshold.
        """
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            # Count total distinct sessions recorded
            cursor.execute("SELECT COUNT(DISTINCT date || session_name) as total_sessions FROM attendance")
            total_sessions = cursor.fetchone()["total_sessions"] or 1

            cursor.execute("""
                SELECT s.id, s.roll_number, s.name, s.department,
                       COUNT(a.id) as sessions_attended
                FROM students s
                LEFT JOIN attendance a ON s.id = a.student_id
                GROUP BY s.id
                ORDER BY s.roll_number ASC
            """)
            students_stats = []
            for row in cursor.fetchall():
                attended = row["sessions_attended"]
                pct = round((attended / total_sessions) * 100, 1)
                is_low = pct < warning_threshold
                students_stats.append({
                    "id": row["id"],
                    "roll_number": row["roll_number"],
                    "name": row["name"],
                    "department": row["department"],
                    "attended": attended,
                    "total_sessions": total_sessions,
                    "percentage": pct,
                    "low_attendance": is_low
                })

            return students_stats

    def export_csv(self, filepath="exports/attendance_export.csv", date_str=None):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        records = self.get_attendance_records(date_str=date_str)
        if not records:
            return False, "No attendance records available for export."

        with open(filepath, mode="w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(
                f,
                fieldnames=["id", "date", "time", "session_name", "roll_number", "name", "department", "confidence", "mode"]
            )
            writer.writeheader()
            for r in records:
                writer.writerow(r)

        return True, filepath

