"""
Logic Legend - PY-07 Student Performance Predictor
Automated Unit and Machine Learning Evaluation Test Suite
"""

import os
import sys
import tempfile
import unittest
import shutil

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from data_cleaner import DataCleaner, REQUIRED_FEATURES
from model_engine import ModelEngine
from advisor import AcademicAdvisor

class TestStudentPredictor(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp_dir = tempfile.mkdtemp()
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        csv_path = os.path.join(base_dir, "data", "student_data.csv")
        cls.df = DataCleaner.load_and_clean(csv_path)
        cls.engine = ModelEngine(models_dir=cls.temp_dir)
        cls.metrics = cls.engine.train(cls.df)

    @classmethod
    def tearDownClass(cls):
        if os.path.exists(cls.temp_dir):
            shutil.rmtree(cls.temp_dir)

    def test_01_data_cleaner_validation(self):
        """Test dataset shape, column integrity, and non-empty values."""
        self.assertGreaterEqual(len(self.df), 40)
        for col in REQUIRED_FEATURES:
            self.assertIn(col, self.df.columns)
            self.assertFalse(self.df[col].isnull().any())
            self.assertTrue((self.df[col] >= 0).all())

    def test_02_regression_metrics_integrity(self):
        """Verify that regression models achieve strong R2 scores on test partition."""
        reg_metrics = self.metrics["regression"]
        self.assertIn("random_forest_reg", reg_metrics)
        rf_r2 = reg_metrics["random_forest_reg"]["r2_score"]
        self.assertGreaterEqual(rf_r2, 0.80)

    def test_03_classification_metrics_integrity(self):
        """Verify that classification models achieve high accuracy on test partition."""
        cls_metrics = self.metrics["classification"]
        self.assertIn("random_forest_cls", cls_metrics)
        rf_acc = cls_metrics["random_forest_cls"]["accuracy"]
        self.assertGreaterEqual(rf_acc, 0.80)

    def test_04_feature_importance_validity(self):
        """Verify that feature importances sum to approximately 1.0 and contain no negative weights."""
        feat_imp = self.metrics["feature_importance"]
        self.assertEqual(len(feat_imp), len(REQUIRED_FEATURES))
        total_imp = sum(feat_imp.values())
        self.assertAlmostEqual(total_imp, 1.0, delta=0.05)

    def test_05_high_performing_student_prediction(self):
        """Test prediction for high-performing student profile."""
        features = {
            "attendance_pct": 95.0,
            "study_hours_weekly": 22.0,
            "past_exam_score": 92.0,
            "internal_test_score": 90.0,
            "assignments_completed": 98.0,
            "extracurricular_hours": 3.0
        }
        res = self.engine.predict_single(features)
        self.assertGreaterEqual(res["predicted_score"], 80.0)
        self.assertEqual(res["predicted_standing"], "Distinction")
        self.assertFalse(res["is_at_risk"])

    def test_06_at_risk_student_prediction(self):
        """Test prediction and flagging for at-risk struggling student profile."""
        features = {
            "attendance_pct": 50.0, # Low attendance
            "study_hours_weekly": 3.0, # Low study
            "past_exam_score": 35.0,
            "internal_test_score": 32.0,
            "assignments_completed": 30.0,
            "extracurricular_hours": 15.0
        }
        res = self.engine.predict_single(features)
        self.assertLess(res["predicted_score"], 50.0)
        self.assertTrue(res["is_at_risk"])

    def test_07_academic_advisor_recommendations(self):
        """Test tailored advisor recommendation triggers."""
        features = {
            "attendance_pct": 65.0, # < 75% -> triggers critical attendance
            "study_hours_weekly": 4.0, # < 8 -> triggers high study scheduling
            "past_exam_score": 40.0,
            "internal_test_score": 45.0,
            "assignments_completed": 50.0,
            "extracurricular_hours": 8.0
        }
        pred = {"predicted_score": 42.0, "is_at_risk": True}
        recs = AcademicAdvisor.generate_recommendations(features, pred, target_goal=75.0)

        # Must flag attendance deficit
        self.assertTrue(any("Attendance" in r["category"] for r in recs))
        # Must flag study hours
        self.assertTrue(any("Study" in r["category"] for r in recs))

if __name__ == "__main__":
    unittest.main()

