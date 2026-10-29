"""
Logic Legend - PY-03 Face Recognition Attendance System
Automated Unit and Integration Test Suite
"""

import os
import sys
import tempfile
import unittest
import shutil
import gc

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from database import AttendanceDatabase
from face_engine import FaceEngine
from attendance_service import AttendanceService

class TestFaceAttendance(unittest.TestCase):
    def setUp(self):
        self.temp_db_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        os.close(self.temp_db_fd)
        self.db = AttendanceDatabase(db_path=self.temp_db_path)

        self.temp_samples_dir = tempfile.mkdtemp()
        self.engine = FaceEngine(confidence_threshold=0.60)
        self.service = AttendanceService(self.db, self.engine, samples_dir=self.temp_samples_dir)

    def tearDown(self):
        del self.service
        del self.engine
        del self.db
        gc.collect()
        try:
            if os.path.exists(self.temp_db_path):
                os.remove(self.temp_db_path)
            if os.path.exists(self.temp_samples_dir):
                shutil.rmtree(self.temp_samples_dir)
        except OSError:
            pass

    def test_01_enrollment_consent_requirement(self):
        """Test privacy requirement: Enrollment is rejected without explicit consent."""
        # Attempt enrollment without consent
        ok_no_consent, msg = self.service.enroll_student(
            roll_number="CSE-001",
            name="Rahul Sharma",
            department="Computer Science",
            semester=6,
            consent_given=False
        )
        self.assertFalse(ok_no_consent)
        self.assertIn("consent", msg.lower())

        # Attempt enrollment with consent
        ok_consent, _ = self.service.enroll_student(
            roll_number="CSE-001",
            name="Rahul Sharma",
            department="Computer Science",
            semester=6,
            consent_given=True
        )
        self.assertTrue(ok_consent)

    def test_02_duplicate_attendance_prevention(self):
        """Test preventing duplicate attendance scans for same student in same session."""
        self.service.enroll_student("CSE-002", "Priya Sen", "IT", 6, True)
        students = self.service.get_all_students()
        s_id = students[0]["id"]

        # First mark: should succeed
        ok1, msg1 = self.service.mark_attendance(s_id, session_name="Morning Lecture", mode="MANUAL")
        self.assertTrue(ok1)
        self.assertIn("marked", msg1.lower())

        # Second mark: should be rejected as duplicate
        ok2, msg2 = self.service.mark_attendance(s_id, session_name="Morning Lecture", mode="MANUAL")
        self.assertFalse(ok2)
        self.assertIn("duplicate", msg2.lower())

    def test_03_manual_attendance_alternative(self):
        """Test manual attendance path operates properly without camera."""
        self.service.enroll_student("CSE-003", "Karan Johar", "ECE", 4, True)
        s_id = self.service.get_all_students()[0]["id"]

        ok, msg = self.service.mark_attendance(s_id, session_name="Lab Practical", mode="MANUAL")
        self.assertTrue(ok)

        records = self.service.get_attendance_records()
        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["mode"], "MANUAL")

    def test_04_low_attendance_warning_threshold(self):
        """Test flagging students below the 75% attendance threshold."""
        self.service.enroll_student("CSE-101", "Student Full", "CSE", 6, True)
        self.service.enroll_student("CSE-102", "Student Defaulter", "CSE", 6, True)

        students = self.service.get_all_students()
        s1_id = students[0]["id"]
        s2_id = students[1]["id"]

        # Simulate 4 sessions
        with self.db.get_connection() as conn:
            cur = conn.cursor()
            # s1 attended 4 out of 4 (100%)
            for i in range(1, 5):
                cur.execute("INSERT INTO attendance (student_id, date, time, session_name, mode) VALUES (?, ?, '09:00:00', ?, 'MANUAL')", (s1_id, f"2026-10-0{i}", f"Session {i}"))
            # s2 only attended 1 out of 4 (25% < 75%)
            cur.execute("INSERT INTO attendance (student_id, date, time, session_name, mode) VALUES (?, '2026-10-01', '09:00:00', 'Session 1', 'MANUAL')", (s2_id,))
            conn.commit()

        stats = self.service.calculate_attendance_percentages(warning_threshold=75.0)
        s1_stat = next(s for s in stats if s["id"] == s1_id)
        s2_stat = next(s for s in stats if s["id"] == s2_id)

        self.assertFalse(s1_stat["low_attendance"])
        self.assertEqual(s1_stat["percentage"], 100.0)

        self.assertTrue(s2_stat["low_attendance"])
        self.assertEqual(s2_stat["percentage"], 25.0)

    def test_05_face_engine_signature_and_matching(self):
        """Test face signature extraction and similarity math."""
        synthetic_face = self.engine.generate_synthetic_face()
        sig = self.engine.extract_face_signature(synthetic_face)
        self.assertIsNotNone(sig)
        self.assertIn("histogram", sig)
        self.assertIn("image", sig)

        # Comparing image with itself should yield high similarity (> 0.85)
        sim = self.engine.compute_similarity(sig, sig)
        self.assertGreaterEqual(sim, 0.85)

    def test_06_csv_export(self):
        """Test exporting attendance logs to CSV."""
        self.service.enroll_student("CSE-201", "Anil Kapoor", "Mech", 2, True)
        s_id = self.service.get_all_students()[0]["id"]
        self.service.mark_attendance(s_id, session_name="Class A", mode="MANUAL")

        export_file = os.path.join(tempfile.gettempdir(), "test_att_export.csv")
        ok, res = self.service.export_csv(export_file)
        self.assertTrue(ok)
        self.assertTrue(os.path.exists(res))

        if os.path.exists(export_file):
            os.remove(export_file)

if __name__ == "__main__":
    unittest.main()
