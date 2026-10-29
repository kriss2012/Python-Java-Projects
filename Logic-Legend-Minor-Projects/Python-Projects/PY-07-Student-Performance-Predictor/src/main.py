"""
Logic Legend - PY-07 Student Performance Predictor
CLI Entrypoint and Runner
"""

import sys
import os
import argparse
import subprocess

# Ensure current dir is in Python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

from data_cleaner import DataCleaner
from model_engine import ModelEngine
from advisor import AcademicAdvisor

def run_cli_summary(attendance=85.0, study_hours=14.0, past_score=75.0, internal_score=78.0, assignments=90.0, extra=5.0):
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_path = os.path.join(base_dir, "data", "student_data.csv")
    models_dir = os.path.join(base_dir, "models")

    df = DataCleaner.load_and_clean(data_path)
    engine = ModelEngine(models_dir=models_dir)
    engine.train(df)

    input_data = {
        "attendance_pct": attendance,
        "study_hours_weekly": study_hours,
        "past_exam_score": past_score,
        "internal_test_score": internal_score,
        "assignments_completed": assignments,
        "extracurricular_hours": extra
    }

    pred = engine.predict_single(input_data)
    recs = AcademicAdvisor.generate_recommendations(input_data, pred)

    print("=" * 65)
    print("      LOGIC LEGEND PY-07 STUDENT PERFORMANCE PREDICTOR")
    print("=" * 65)
    print(f"Attendance       : {attendance} %")
    print(f"Weekly Study     : {study_hours} Hours")
    print(f"Past Exam Score  : {past_score} / 100")
    print(f"Internal Score   : {internal_score} / 100")
    print(f"Assignments Done : {assignments} %")
    print("-" * 65)
    print(f"Predicted Final Grade    : {pred['predicted_score']} %")
    print(f"Projected Standing       : {pred['predicted_standing']}")
    print(f"At-Risk Alert Status     : {'[CRITICAL AT-RISK]' if pred['is_at_risk'] else '[SATISFACTORY]'}")
    print(f"Model Confidence Estimate: {pred['confidence']} %")
    print("-" * 65)
    print("Personalized Study Recommendations:")
    for r in recs[:3]:
        print(f"  • {r['category']}: {r['message']}")
    print("=" * 65)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-07 Student Performance Predictor")
    parser.add_argument("--cli", action="store_true", help="Run in command-line evaluation mode")
    parser.add_argument("--attendance", type=float, default=85.0)
    parser.add_argument("--study-hours", type=float, default=14.0)
    parser.add_argument("--past-score", type=float, default=75.0)
    args = parser.parse_args()

    if args.cli:
        run_cli_summary(attendance=args.attendance, study_hours=args.study_hours, past_score=args.past_score)
        return

    # Launch Streamlit app
    app_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app.py")
    subprocess.run(["streamlit", "run", app_path])

if __name__ == "__main__":
    main()

