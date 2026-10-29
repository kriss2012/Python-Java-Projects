"""
Logic Legend - PY-03 Face Recognition Attendance System
Main Application Entrypoint
"""

import sys
import os
import argparse
import tkinter as tk

# Ensure current dir is in Python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Safe console reconfigure
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

from database import AttendanceDatabase
from face_engine import FaceEngine
from attendance_service import AttendanceService
from gui import AttendanceGUI

def run_cli_summary(service):
    print("=" * 65)
    print("   LOGIC LEGEND PY-03 FACE ATTENDANCE - CLI SUMMARY")
    print("=" * 65)
    records = service.get_attendance_records()
    print(f"Total Attendance Logs Recorded: {len(records)}")
    print("-" * 65)
    print(f"{'Roll Number':<16} | {'Student Name':<20} | {'Attendance %':<14} | Status")
    print("-" * 65)
    stats = service.calculate_attendance_percentages()
    for s in stats:
        flag = "[LOW ALERT]" if s["low_attendance"] else "[COMPLIANT]"
        print(f"{s['roll_number']:<16} | {s['name']:<20} | {s['percentage']:>10.1f} % | {flag}")
    print("=" * 65)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-03 Face Recognition Attendance System")
    parser.add_argument("--cli", action="store_true", help="Run in command-line summary mode")
    parser.add_argument("--seed", action="store_true", help="Seed demo students and attendance logs")
    parser.add_argument("--export", action="store_true", help="Export attendance records to CSV")
    args = parser.parse_args()

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    db_path = os.path.join(base_dir, "data", "attendance.db")
    samples_dir = os.path.join(base_dir, "samples")

    db = AttendanceDatabase(db_path=db_path)
    engine = FaceEngine(confidence_threshold=0.60)
    service = AttendanceService(db, engine, samples_dir=samples_dir)

    if args.seed:
        db.seed_demo_data()
        print("Demo students and attendance logs seeded.")

    if args.export:
        out_path = os.path.join(base_dir, "exports", "attendance_export.csv")
        ok, res = service.export_csv(out_path)
        print(f"Export status: {res}")

    if args.cli:
        run_cli_summary(service)
        return

    # Seed data if database is fresh
    db.seed_demo_data()

    # Launch GUI
    root = tk.Tk()
    app = AttendanceGUI(root, service)
    root.mainloop()

if __name__ == "__main__":
    main()
