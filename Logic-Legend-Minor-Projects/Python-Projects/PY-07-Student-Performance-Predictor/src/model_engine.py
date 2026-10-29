"""
Logic Legend - PY-07 Student Performance Predictor
Regression & Classification Modeling Engine
"""

import os
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression, Ridge, LogisticRegression
from sklearn.ensemble import RandomForestRegressor, RandomForestClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.metrics import r2_score, mean_absolute_error, root_mean_squared_error, accuracy_score, f1_score
from data_cleaner import REQUIRED_FEATURES

class ModelEngine:
    def __init__(self, models_dir=None):
        if not models_dir:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            models_dir = os.path.join(base_dir, "models")
        self.models_dir = models_dir
        os.makedirs(self.models_dir, exist_ok=True)
        self.regressors = {}
        self.classifiers = {}
        self.feature_importances = {}
        self.metrics = {}

    def train(self, df):
        X = df[REQUIRED_FEATURES]
        y_reg = df["final_score"]
        y_cls = df["academic_standing"]

        # Train/Test Split (80% Train, 20% Test) passing all arrays simultaneously for aligned indices
        X_train, X_test, y_train_reg, y_test_reg, y_train_cls, y_test_cls = train_test_split(
            X, y_reg, y_cls, test_size=0.20, random_state=42, stratify=y_cls
        )

        # 1. Train Regression Models
        reg_models = {
            "linear_regression": Pipeline([("scaler", StandardScaler()), ("model", LinearRegression())]),
            "ridge_regression": Pipeline([("scaler", StandardScaler()), ("model", Ridge(alpha=1.0))]),
            "random_forest_reg": RandomForestRegressor(n_estimators=100, random_state=42)
        }
        reg_metrics = {}
        for name, model in reg_models.items():
            model.fit(X_train, y_train_reg)
            preds = model.predict(X_test)
            r2 = float(r2_score(y_test_reg, preds))
            mae = float(mean_absolute_error(y_test_reg, preds))
            rmse = float(root_mean_squared_error(y_test_reg, preds))
            reg_metrics[name] = {
                "r2_score": round(r2, 4),
                "mae": round(mae, 2),
                "rmse": round(rmse, 2)
            }
            self.regressors[name] = model
            joblib.dump(model, os.path.join(self.models_dir, f"{name}.joblib"))

        # 2. Train Classification Models
        cls_models = {
            "logistic_regression": Pipeline([("scaler", StandardScaler()), ("model", LogisticRegression(max_iter=1000, random_state=42))]),
            "random_forest_cls": RandomForestClassifier(n_estimators=100, random_state=42)
        }
        cls_metrics = {}
        for name, model in cls_models.items():
            model.fit(X_train, y_train_cls)
            preds = model.predict(X_test)
            acc = float(accuracy_score(y_test_cls, preds))
            f1 = float(f1_score(y_test_cls, preds, average="macro"))
            cls_metrics[name] = {
                "accuracy": round(acc, 4),
                "f1_score": round(f1, 4)
            }
            self.classifiers[name] = model
            joblib.dump(model, os.path.join(self.models_dir, f"{name}.joblib"))

        # Extract Feature Importance from Random Forest Regressor
        rf = self.regressors["random_forest_reg"]
        importances = rf.feature_importances_
        self.feature_importances = {
            feat: round(float(imp), 4) for feat, imp in zip(REQUIRED_FEATURES, importances)
        }

        self.metrics = {
            "regression": reg_metrics,
            "classification": cls_metrics,
            "feature_importance": self.feature_importances
        }
        joblib.dump(self.metrics, os.path.join(self.models_dir, "model_metrics.joblib"))
        return self.metrics

    def predict_single(self, features_dict, reg_model_name="random_forest_reg", cls_model_name="random_forest_cls"):
        """Accepts dict of feature inputs and predicts final score & standing."""
        row_df = pd.DataFrame([features_dict])[REQUIRED_FEATURES]

        if reg_model_name not in self.regressors:
            reg_path = os.path.join(self.models_dir, f"{reg_model_name}.joblib")
            if os.path.exists(reg_path):
                self.regressors[reg_model_name] = joblib.load(reg_path)
            else:
                raise RuntimeError(f"Model {reg_model_name} not trained yet.")

        if cls_model_name not in self.classifiers:
            cls_path = os.path.join(self.models_dir, f"{cls_model_name}.joblib")
            if os.path.exists(cls_path):
                self.classifiers[cls_model_name] = joblib.load(cls_path)
            else:
                raise RuntimeError(f"Model {cls_model_name} not trained yet.")

        pred_score = float(self.regressors[reg_model_name].predict(row_df)[0])
        pred_standing = str(self.classifiers[cls_model_name].predict(row_df)[0])

        confidence = 88.0
        if hasattr(self.classifiers[cls_model_name], "predict_proba"):
            probs = self.classifiers[cls_model_name].predict_proba(row_df)[0]
            confidence = round(float(np.max(probs)) * 100, 1)

        is_at_risk = pred_score < 45.0 or pred_standing == "Fail" or features_dict.get("attendance_pct", 100) < 75.0

        return {
            "predicted_score": round(max(0.0, min(100.0, pred_score)), 1),
            "predicted_standing": pred_standing,
            "confidence": confidence,
            "is_at_risk": is_at_risk
        }
