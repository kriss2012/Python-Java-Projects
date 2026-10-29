"""
Logic Legend - PY-07 Student Performance Predictor
Data Loading, Cleaning & Validation Layer
"""

import os
import pandas as pd
import numpy as np

REQUIRED_FEATURES = [
    "attendance_pct",
    "study_hours_weekly",
    "past_exam_score",
    "internal_test_score",
    "assignments_completed",
    "extracurricular_hours"
]

class DataCleaner:
    @staticmethod
    def load_and_clean(csv_path):
        if not os.path.exists(csv_path):
            raise FileNotFoundError(f"Dataset not found at {csv_path}")

        df = pd.read_csv(csv_path)

        # Validate mandatory columns exist
        missing = [col for col in REQUIRED_FEATURES if col not in df.columns]
        if missing:
            raise ValueError(f"Dataset missing required feature columns: {missing}")

        # Drop duplicates
        df = df.drop_duplicates()

        # Handle missing values
        for col in REQUIRED_FEATURES:
            if df[col].isnull().any():
                df[col] = df[col].fillna(df[col].median())

        # Boundary checks
        df["attendance_pct"] = df["attendance_pct"].clip(0.0, 100.0)
        df["study_hours_weekly"] = df["study_hours_weekly"].clip(0.0, 60.0)
        df["past_exam_score"] = df["past_exam_score"].clip(0.0, 100.0)
        df["internal_test_score"] = df["internal_test_score"].clip(0.0, 100.0)
        df["assignments_completed"] = df["assignments_completed"].clip(0.0, 100.0)
        df["extracurricular_hours"] = df["extracurricular_hours"].clip(0.0, 40.0)

        # Ensure target labels
        if "final_score" in df.columns:
            df["final_score"] = df["final_score"].clip(0.0, 100.0)
            if "academic_standing" not in df.columns:
                df["academic_standing"] = df["final_score"].apply(
                    lambda s: "Distinction" if s >= 75 else ("Pass" if s >= 45 else "Fail")
                )

        return df

    @staticmethod
    def get_correlation_matrix(df):
        numeric_df = df.select_dtypes(include=[np.number])
        return numeric_df.corr().round(3)

