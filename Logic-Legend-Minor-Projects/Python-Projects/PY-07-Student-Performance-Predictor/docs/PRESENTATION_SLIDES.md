# PRESENTATION SLIDES OUTLINE
## PY-07: Student Performance Predictor
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: Student Performance Predictor & Academic Early Warning System (PY-07)
- **Domain**: Educational Data Mining (EdTech), Machine Learning & Analytics
- **Technologies**: Python, Streamlit, scikit-learn, Plotly, Pandas, Seaborn
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Millions of college students struggle academically without early faculty awareness.
- Traditional semester evaluations are retroactive, occurring too late for remediation.
- Educational institutions lack transparent AI tools to simulate the impact of study habits.

---

### Slide 3: Objectives & Solution Highlights
- A full-featured predictive analytics web portal:
  - Forecasts final exam marks (0–100%) and categorizes academic standing.
  - Benchmarks multiple regression and classification algorithms.
  - Generates transparent feature importance weightings.
  - **Standout Feature**: Actionable, personalized study recommendations and a teacher-facing early warning intervention roster.

---

### Slide 4: Data Pipeline & Feature Selection
- Core academic features analyzed:
  - Class Attendance (`attendance_pct`).
  - Weekly Self-Study Hours (`study_hours_weekly`).
  - Past Semester Marks (`past_exam_score`).
  - Continuous Internal Assessment (`internal_test_score`).
  - Assignment Completion Rate (`assignments_completed`).
  - Extracurricular Engagement (`extracurricular_hours`).

---

### Slide 5: Machine Learning Models Compared
- **Regression Suite**: Linear Regression, Ridge Regression, Random Forest Regressor.
- **Classification Suite**: Logistic Regression (with StandardScaler), Random Forest Classifier.
- **Evaluation Metrics**: $R^2$ Score, MAE, RMSE, Accuracy, and Macro F1-Score on an 80/20 train-test split.

---

### Slide 6: Feature Importance Analysis
- Random Forest feature importance weights:
  - Past exam scores and internal assessments contribute over 55% of predictive power.
  - Weekly study hours and attendance account for over 35%.
  - High transparency ensures educators understand *why* predictions are made.

---

### Slide 7: Teacher At-Risk Intervention Dashboard
- Real-time cohort monitoring:
  - Flags students falling below 45% predicted final marks or 75% class attendance.
  - Cohort summary KPIs and one-click CSV export of at-risk rosters for departmental counseling.

---

### Slide 8: Explainable Study Advisor Engine
- Converts predictions into concrete behavioral adjustments:
  - Specific weekly study hour targets (e.g., +4 hours/week).
  - Remedial suggestions for low internal assessment scores.
  - Attendance deficit warnings to prevent exam debarment.

---

### Slide 9: Responsible AI & Ethics
- Adheres to ethical educational guidelines:
  - Predictions are decision-support estimates, not immutable verdicts.
  - Prohibits sensitive demographic attributes as unjust proxies.

---

### Slide 10: Testing & Verification
- 7 automated unit tests verifying:
  - Data sanitization and boundary clipping.
  - Regression $R^2 \ge 0.80$ and classification accuracy $\ge 0.80$.
  - High-performing vs at-risk student predictions.
  - Advisor recommendation trigger logic.

---

### Slide 11: Conclusion & Q&A
- Delivered an ethical, high-precision, and actionable student performance prediction platform.
- Open for questions and evaluator inspection.

