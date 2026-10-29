"""
Logic Legend - PY-04 Fake News and Spam Message Detector
Flask Web Server & API Controller
"""

import os
import sys
from flask import Flask, render_template, request, jsonify

# Ensure src in path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from classifier_service import ClassifierService

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", "logic_legend_detector_secret_2026")

base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
models_dir = os.path.join(base_dir, "models")
service = ClassifierService(models_dir=models_dir)

@app.route("/")
def index():
    metrics = service.get_metrics()
    news_samples = service.get_samples("news")
    spam_samples = service.get_samples("spam")
    return render_template(
        "index.html",
        metrics=metrics,
        news_samples=news_samples,
        spam_samples=spam_samples
    )

@app.route("/api/predict", methods=["POST"])
def api_predict():
    data = request.get_json() or {}
    text = data.get("text", "").strip()
    domain = data.get("domain", "news").lower()
    model_name = data.get("model", "logistic_regression").lower()

    if not text:
        return jsonify({"error": "Please provide input text to analyze."}), 400

    if domain not in ("news", "spam"):
        return jsonify({"error": "Domain must be 'news' or 'spam'."}), 400

    if model_name == "compare_all":
        comparisons = service.compare_all_models(text, domain=domain)
        return jsonify({"domain": domain, "mode": "comparison", "results": comparisons})

    result = service.predict(text, domain=domain, model_name=model_name)
    return jsonify(result)

@app.route("/api/metrics")
def api_metrics():
    return jsonify(service.get_metrics())

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5001))
    app.run(host="127.0.0.1", port=port, debug=True)

