"""
Logic Legend - PY-04 Fake News and Spam Message Detector
Model Training, Cross-Model Comparison & Serialization Engine
"""

import os
import pandas as pd
import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.linear_model import LogisticRegression
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
from sklearn.pipeline import Pipeline

from preprocessor import TextPreprocessor

class ModelTrainer:
    def __init__(self, models_dir=None):
        if not models_dir:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            models_dir = os.path.join(base_dir, "models")
        self.models_dir = models_dir
        os.makedirs(self.models_dir, exist_ok=True)

    def train_domain(self, dataset_path, domain_name="news", target_col="label", text_col="text"):
        """
        Trains and compares Naive Bayes, Logistic Regression, and SVM for a specific domain.
        domain_name: 'news' or 'spam'
        """
        if not os.path.exists(dataset_path):
            raise FileNotFoundError(f"Dataset not found at {dataset_path}")

        df = pd.read_csv(dataset_path)
        # Apply preprocessing
        df["cleaned_text"] = df[text_col].apply(TextPreprocessor.clean)
        # Remove empty rows after cleaning
        df = df[df["cleaned_text"].str.len() > 0]

        X = df["cleaned_text"]
        y = df[target_col]

        # Train/Test Split (80% Train, 20% Test) with stratify
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.25, random_state=42, stratify=y
        )

        classifiers = {
            "naive_bayes": MultinomialNB(alpha=1.0),
            "logistic_regression": LogisticRegression(max_iter=1000, random_state=42),
            "svm": CalibratedClassifierCV(LinearSVC(random_state=42, dual=False))
        }

        results = {}

        for name, clf in classifiers.items():
            # Pipeline guarantees NO data leakage: fit vectorizer strictly on X_train
            pipeline = Pipeline([
                ("tfidf", TfidfVectorizer(ngram_range=(1, 2), sublinear_tf=True)),
                ("classifier", clf)
            ])

            pipeline.fit(X_train, y_train)
            y_pred = pipeline.predict(X_test)

            # Evaluate performance metrics
            # Determine positive label
            pos_label = "fake" if domain_name == "news" else "spam"
            labels_order = ["real", "fake"] if domain_name == "news" else ["ham", "spam"]

            acc = float(accuracy_score(y_test, y_pred))
            prec = float(precision_score(y_test, y_pred, pos_label=pos_label, zero_division=0))
            rec = float(recall_score(y_test, y_pred, pos_label=pos_label, zero_division=0))
            f1 = float(f1_score(y_test, y_pred, pos_label=pos_label, zero_division=0))
            cm = confusion_matrix(y_test, y_pred, labels=labels_order).tolist()

            results[name] = {
                "accuracy": round(acc, 4),
                "precision": round(prec, 4),
                "recall": round(rec, 4),
                "f1_score": round(f1, 4),
                "confusion_matrix": cm,
                "labels": labels_order,
                "test_samples": len(y_test)
            }

            # Save model pipeline
            model_file = os.path.join(self.models_dir, f"{domain_name}_{name}.joblib")
            joblib.dump(pipeline, model_file)

        return results

    def train_all(self, data_dir=None):
        if not data_dir:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            data_dir = os.path.join(base_dir, "data")

        news_path = os.path.join(data_dir, "news_dataset.csv")
        spam_path = os.path.join(data_dir, "spam_dataset.csv")

        news_metrics = self.train_domain(news_path, domain_name="news")
        spam_metrics = self.train_domain(spam_path, domain_name="spam")

        metrics_summary = {
            "news": news_metrics,
            "spam": spam_metrics
        }
        # Save metrics summary
        joblib.dump(metrics_summary, os.path.join(self.models_dir, "metrics_summary.joblib"))
        return metrics_summary
