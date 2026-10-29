# PY-04: Fake News and Spam Message Detector
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-04`
- **Project Title**: Fake News and Spam Message Detector
- **Domain**: Machine Learning, Natural Language Processing & Cybersecurity
- **Platform**: Web (Flask, scikit-learn, HTML5, CSS3, JavaScript)

---

## 2. Project Overview
The Fake News and Spam Message Detector is a natural language processing web platform engineered to evaluate and verify user-submitted text. It compares three prominent classification architectures (Multinomial Naive Bayes, Logistic Regression, and Support Vector Machines) to classify content under strict domain segregation: news items as `real` vs `fake`, and messages as `spam` vs `ham` (legitimate).

---

## 3. Problem Statement
The proliferation of digital misinformation and fraudulent phishing messages causes financial losses and reputational damage. Existing ad-hoc tools either confuse news veracity with spam mechanics or fail to present clear statistical evaluation metrics and confidence bounds to users.

---

## 4. Objectives
- Implement an end-to-end NLP feature extraction pipeline using TF-IDF.
- Train, evaluate, and benchmark three distinct algorithms: Naive Bayes, Logistic Regression, and SVM.
- Enforce strict domain segregation:
  - News classification: `real` vs `fake`
  - Message classification: `ham` vs `spam`
- Ensure zero data leakage during cross-validation by fitting vectorizers strictly on training folds.
- Display comprehensive performance benchmarks: Accuracy, Precision, Recall, F1-Score, and Confusion Matrices.
- Present predictions transparently as probabilistic machine-learning estimates rather than absolute truth.

---

## 5. Key Features
- **Dual Domain Verification**: Dedicated modes for News Headlines/Articles and SMS/Email Messages.
- **Model Comparison View**: One-click simultaneous inference across all 3 trained models.
- **Evaluation Dashboard**: Live rendering of Precision, Recall, F1, and Confusion Matrices on test data.
- **Sample Demonstration Inputs**: Pre-loaded authentic and deceptive examples for instant testing.
- **Model Serialization**: Persistent pipeline storage via `joblib`.

---

## 6. Technology Stack
- **Language**: Python 3.10+
- **Machine Learning**: scikit-learn (`TfidfVectorizer`, `MultinomialNB`, `LogisticRegression`, `SVC`)
- **Data Engineering**: Pandas, NumPy
- **Web Framework**: Flask
- **Frontend**: HTML5, CSS3, JavaScript (Fetch API)
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 150 MB free space.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-04-Fake-News-Spam-Detector

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Database & Model Setup
Training datasets (`data/news_dataset.csv` and `data/spam_dataset.csv`) are included. On the initial run, the system automatically trains and serializes all 6 model pipelines into `models/`.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
FLASK_APP=src/app.py
PORT=5001
SECRET_KEY=logic_legend_ml_classifier_2026
DEFAULT_MODEL=logistic_regression
```

---

## 11. Commands to Run the Application
```powershell
# Start the Flask web application
python src/app.py
```
*Access the interface at `http://127.0.0.1:5001`.*

---

## 12. Demo Credentials
No login required. Publicly accessible analysis dashboard.

---

## 13. Screenshots and Interface Description
1. **Classifier Form**: Domain selector tabs (`News` vs `Spam`), model dropdown, demo sample pills, text area, and classify button.
2. **Prediction Verdict Card**: Visual pill (`REAL`, `FAKE`, `SPAM`, `HAM`), confidence bar with percentage, and ethical disclaimer.
3. **Cross-Model Comparison**: Three-column comparison cards displaying consensus between Naive Bayes, Logistic Regression, and SVM.
4. **Performance Benchmark**: Table reporting Accuracy, Precision, Recall, F1, and confusion matrices.

---

## 14. API Documentation
- `POST /api/predict`: Accepts `{ text, domain, model }`, returns `{ prediction, confidence, probabilities, disclaimer }`.
- `GET /api/metrics`: Returns serialized cross-model performance metrics and confusion matrices.

---

## 15. Test Instructions
```powershell
python tests/test_classifier.py
```
*Executes 5 comprehensive unit tests verifying text cleaning, train/test isolation, metrics computation, news classification, spam detection, and cross-model comparison.*

---

## 16. Known Limitations
- Relies on textual linguistic cues and lexical patterns; novel factual assertions without linguistic anomalies require external knowledge-graph validation.

---

## 17. Future Enhancements
- Integration of a transformer architecture such as RoBERTa or DeBERTa for contextual sequence modeling.
- Browser extension for real-time social media feed annotation.

---

## 18. Troubleshooting
- **Port Conflict**: If port 5001 is in use, modify `PORT=5002` in `.env` or set environment variable.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines for academic evaluation.

