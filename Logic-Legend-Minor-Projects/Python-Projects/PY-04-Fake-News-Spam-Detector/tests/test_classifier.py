"""
Logic Legend - PY-04 Fake News and Spam Message Detector
Automated Unit and ML Evaluation Test Suite
"""

import os
import sys
import tempfile
import unittest
import shutil

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from preprocessor import TextPreprocessor
from model_trainer import ModelTrainer
from classifier_service import ClassifierService

class TestClassifier(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp_dir = tempfile.mkdtemp()
        # Train pipelines into temp directory
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        data_dir = os.path.join(base_dir, "data")
        cls.trainer = ModelTrainer(models_dir=cls.temp_dir)
        cls.metrics = cls.trainer.train_all(data_dir=data_dir)
        cls.service = ClassifierService(models_dir=cls.temp_dir)

    @classmethod
    def tearDownClass(cls):
        if os.path.exists(cls.temp_dir):
            shutil.rmtree(cls.temp_dir)

    def test_01_text_preprocessor(self):
        """Test URL stripping, stopword removal, and normalization."""
        raw = "URGENT: Click http://bit.ly/claim-link to win a cash prize of $1,000,000 NOW and claim it!"
        cleaned = TextPreprocessor.clean(raw)
        self.assertNotIn("http", cleaned)
        self.assertNotIn("and", cleaned.split())
        self.assertNotIn("to", cleaned.split())
        self.assertIn("urgent", cleaned)
        self.assertIn("prize", cleaned)

    def test_02_model_evaluation_metrics_integrity(self):
        """Verify that training produced valid metrics and confusion matrices."""
        self.assertIn("news", self.metrics)
        self.assertIn("spam", self.metrics)

        for domain in ("news", "spam"):
            for m in ("naive_bayes", "logistic_regression", "svm"):
                m_stat = self.metrics[domain][m]
                self.assertGreaterEqual(m_stat["accuracy"], 0.70)
                self.assertGreaterEqual(m_stat["f1_score"], 0.70)
                self.assertEqual(len(m_stat["confusion_matrix"]), 2)

    def test_03_news_real_vs_fake_prediction(self):
        """Test news classification labels (real vs fake)."""
        real_text = "Reserve Bank of India maintains benchmark repo rate at 6.5 percent amid stabilizing inflation"
        res_real = self.service.predict(real_text, domain="news", model_name="logistic_regression")
        self.assertEqual(res_real["prediction"], "real")
        self.assertGreater(res_real["confidence"], 50.0)

        fake_text = "SHOCKING: Drinking boiled lemon juice and hot water cures cancer completely in forty eight hours doctors furious"
        res_fake = self.service.predict(fake_text, domain="news", model_name="logistic_regression")
        self.assertEqual(res_fake["prediction"], "fake")
        self.assertGreater(res_fake["confidence"], 50.0)

    def test_04_message_spam_vs_ham_prediction(self):
        """Test message classification labels (spam vs ham)."""
        spam_text = "CONGRATULATIONS! You have been selected as winner of 1,000,000 lottery cash prize! Call 1800-CLAIM-NOW"
        res_spam = self.service.predict(spam_text, domain="spam", model_name="svm")
        self.assertEqual(res_spam["prediction"], "spam")
        self.assertGreater(res_spam["confidence"], 50.0)

        ham_text = "Dear Professor, please find attached my assignment submission for the Distributed Systems semester course."
        res_ham = self.service.predict(ham_text, domain="spam", model_name="svm")
        self.assertEqual(res_ham["prediction"], "ham")
        self.assertGreater(res_ham["confidence"], 50.0)

    def test_05_cross_model_comparison(self):
        """Test comparing all three classifiers simultaneously."""
        sample = "Click this urgent link to claim free government laptop scheme before deadline closes tonight"
        comparisons = self.service.compare_all_models(sample, domain="news")

        self.assertIn("naive_bayes", comparisons)
        self.assertIn("logistic_regression", comparisons)
        self.assertIn("svm", comparisons)

        # All models should agree or provide valid predictions
        for m, r in comparisons.items():
            self.assertIn(r["prediction"], ["real", "fake"])
            self.assertIn("disclaimer", r)

if __name__ == "__main__":
    unittest.main()
