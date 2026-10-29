# EVALUATOR DEMONSTRATION GUIDE
## PY-04: Fake News and Spam Message Detector
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-04-Fake-News-Spam-Detector
python src/app.py
```
*The Flask web application will start at `http://127.0.0.1:5001`.*
Open your web browser and navigate to `http://127.0.0.1:5001`.

---

## 2. Recommended Demonstration Sequence

### Step 1: Demonstrate Real News Verification
- Ensure the **News Verification (Real vs Fake)** tab is active.
- Under **Quick Demo Samples**, click **"Space Science Discovery"**.
- Text loads: *"NASA James Webb Space Telescope discovers atmospheric water vapor on rocky habitable-zone exoplanet."*
- Select Model: `Logistic Regression`.
- Click **Classify Text**.
- Point out:
  - Visual verdict badge: **REAL** in vibrant green.
  - High confidence gauge (~85-95%).
  - Probability distribution across classes.
  - Statistical disclaimer box at the bottom.

### Step 2: Demonstrate Fake News Hoax Detection
- Click the demo sample: **"Viral Health Hoax"**.
- Text loads: *"SHOCKING: Drinking boiled lemon juice and hot water cures cancer completely in forty eight hours doctors furious!"*
- Click **Classify Text**.
- Point out:
  - Visual verdict badge: **FAKE** in bold red.
  - High classification confidence.

### Step 3: Demonstrate "Compare All 3 Models" Mode
- With the hoax text still in the text area, change the model selector to:
  **"⚡ Compare All 3 Models Simultaneously"**.
- Click **Classify Text**.
- Point out:
  - Three comparison cards appear:
    1. **NAIVE BAYES**: Shows prediction (FAKE) & confidence.
    2. **LOGISTIC REGRESSION**: Shows prediction (FAKE) & confidence.
    3. **SUPPORT VECTOR MACHINE (SVM)**: Shows prediction (FAKE) & confidence.
  - Explain to the evaluator how cross-model consensus eliminates algorithmic bias.

### Step 4: Switch to Message Domain (Spam vs Non-Spam/Ham)
- Click the top tab: **Message Classification (Spam vs Non-Spam)**.
- Notice the UI switches domain immediately.
- Click demo sample: **"Phishing Bank Suspension"**.
- Text loads: *"URGENT: Your bank account has been suspended due to unauthorized activity. Click bit.ly/bank-verify-now immediately."*
- Click **Classify Text**.
- Result: **SPAM** (Confidence: ~95%).
- Click demo sample: **"Academic Reminder"**.
- Text loads: *"Dear Professor, please find attached my assignment submission for the Distributed Systems semester course."*
- Click **Classify Text**.
- Result: **HAM** (Legitimate communication).

### Step 5: Review Model Evaluation & Performance Benchmark
- Scroll down to the **Model Evaluation & Performance Benchmark** section.
- Point out the comprehensive metrics table:
  - Accuracy, Precision, Recall, and F1-Scores for both News and Spam across all 3 architectures.
  - The actual confusion matrices `[TN, FP, FN, TP]` computed on held-out test data.
  - Explain that these metrics prove the system was evaluated on unseen test data without data leakage.

---

## 3. Unit Test Verification
Execute the test suite in terminal:
```powershell
python tests/test_classifier.py
```
*All 5 unit tests pass, verifying text preprocessing, evaluation metrics, domain classification, and cross-model comparison.*
