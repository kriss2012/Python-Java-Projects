"""
Logic Legend - PY-04 Fake News and Spam Message Detector
Inference Service & Model Prediction Layer
"""

import os
import joblib
from preprocessor import TextPreprocessor
from model_trainer import ModelTrainer

SAMPLE_NEWS_INPUTS = [
    {
        "title": "Legitimate Fiscal Policy",
        "text": "Reserve Bank of India maintains benchmark repo rate at 6.5 percent amid stabilizing inflation and economic resilience.",
        "expected": "real"
    },
    {
        "title": "Viral Health Hoax",
        "text": "SHOCKING: Drinking boiled lemon juice and hot water cures cancer completely in forty eight hours doctors furious!",
        "expected": "fake"
    },
    {
        "title": "Space Science Discovery",
        "text": "NASA James Webb Space Telescope discovers atmospheric water vapor on rocky habitable-zone exoplanet.",
        "expected": "real"
    },
    {
        "title": "Financial Scheme Scam",
        "text": "Government will deposit twenty five thousand rupees into bank account of every citizen who shares this link on WhatsApp.",
        "expected": "fake"
    }
]

SAMPLE_SPAM_INPUTS = [
    {
        "title": "Phishing Bank Suspension",
        "text": "URGENT: Your bank account has been suspended due to unauthorized activity. Click bit.ly/bank-verify-now immediately.",
        "expected": "spam"
    },
    {
        "title": "Academic Reminder",
        "text": "Dear Professor, please find attached my assignment submission for the Distributed Systems semester course.",
        "expected": "ham"
    },
    {
        "title": "Lottery Fraud SMS",
        "text": "CONGRATULATIONS! You have been selected as winner of 1,000,000 lottery cash prize! Call 1800-CLAIM-NOW to redeem.",
        "expected": "spam"
    },
    {
        "title": "Hostel Check-in Note",
        "text": "Hi Mom, I reached the hostel safely. The train was on time. Will call you after having dinner at the cafeteria.",
        "expected": "ham"
    }
]

class ClassifierService:
    def __init__(self, models_dir=None):
        if not models_dir:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            models_dir = os.path.join(base_dir, "models")
        self.models_dir = models_dir
        self.models = {}
        self.metrics = {}
        self._ensure_models_trained()

    def _ensure_models_trained(self):
        summary_file = os.path.join(self.models_dir, "metrics_summary.joblib")
        if not os.path.exists(summary_file):
            trainer = ModelTrainer(self.models_dir)
            self.metrics = trainer.train_all()
        else:
            self.metrics = joblib.load(summary_file)

    def get_pipeline(self, domain, model_name):
        key = f"{domain}_{model_name}"
        if key not in self.models:
            model_path = os.path.join(self.models_dir, f"{key}.joblib")
            if not os.path.exists(model_path):
                self._ensure_models_trained()
            self.models[key] = joblib.load(model_path)
        return self.models[key]

    def predict(self, text, domain="news", model_name="logistic_regression"):
        cleaned = TextPreprocessor.clean(text)
        if not cleaned:
            return {
                "error": "Input text contains insufficient meaningful words after preprocessing."
            }

        pipeline = self.get_pipeline(domain, model_name)
        pred_label = pipeline.predict([cleaned])[0]

        # Get calibrated prediction probabilities
        probabilities = {}
        confidence = 0.0
        if hasattr(pipeline, "predict_proba"):
            probs = pipeline.predict_proba([cleaned])[0]
            classes = pipeline.classes_
            for cls, p in zip(classes, probs):
                probabilities[str(cls)] = round(float(p) * 100, 1)
            confidence = probabilities.get(str(pred_label), 50.0)
        else:
            confidence = 85.0

        disclaimer = (
            "Note: This verdict is a statistical machine-learning classification estimate "
            "based on linguistic features and TF-IDF patterns, not definitive proof of factual truth."
        )

        return {
            "domain": domain,
            "model": model_name,
            "prediction": str(pred_label),
            "confidence": confidence,
            "probabilities": probabilities,
            "cleaned_text": cleaned,
            "disclaimer": disclaimer
        }

    def compare_all_models(self, text, domain="news"):
        models = ["naive_bayes", "logistic_regression", "svm"]
        comparisons = {}
        for m in models:
            res = self.predict(text, domain=domain, model_name=m)
            comparisons[m] = res
        return comparisons

    def get_metrics(self):
        return self.metrics

    def get_samples(self, domain="news"):
        return SAMPLE_NEWS_INPUTS if domain == "news" else SAMPLE_SPAM_INPUTS
