# PY-07: Student Performance Predictor
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-07`
- **Project Title**: Student Performance Predictor & Academic Early Warning System
- **Domain**: Educational Data Mining (EdTech), Machine Learning & Decision Support
- **Platform**: Web Dashboard (Python Streamlit, scikit-learn, Plotly, Pandas)

---

## 2. Project Overview
The Student Performance Predictor is an intelligent academic analytics web dashboard that evaluates student behavioral and academic features (class attendance, self-study hours, continuous internal assessments, assignment submission rate, and past semester marks) to forecast final examination scores, classify academic standing (`Pass`, `Fail`, `Distinction`), flag at-risk students for faculty intervention, and synthesize explainable personalized study recommendations.

---

## 3. Problem Statement
Academic failure and course dropouts often occur because students fall behind gradually without timely detection. By the time semester-end examinations take place, remediation is impossible. Faculty members need automated tools to identify struggling students weeks before exams, understand key risk drivers, and provide personalized study plans.

---

## 4. Objectives
- Clean and validate academic datasets ensuring zero missing values and boundary validity.
- Train and compare three regression models (Linear Regression, Ridge, Random Forest Regressor) and two classification models (Logistic Regression, Random Forest Classifier).
- Identify and visualize primary feature importance drivers using tree-based ensembles.
- Provide an interactive web dashboard for real-time grade simulation.
- Equip faculty with an "At-Risk Early Warning Roster" identifying students needing intervention.
- Enforce ethical AI guidelines: Frame predictions as decision-support indicators rather than punitive judgments.

---

## 5. Key Features
- **Dual Learning Paradigms**: Continuous grade regression ($R^2$, RMSE, MAE) and categorical academic standing classification (Accuracy, F1-Score).
- **Interactive Grade Simulator**: Responsive sliders forecasting marks and displaying risk status.
- **Teacher At-Risk Roster**: Automatic cohort filtering of students with predicted grade $< 45\%$ or attendance $< 75\%$, with CSV export.
- **Feature Importance Visuals**: Transparent breakdown of weightings (Study Hours vs Attendance vs Past Marks).
- **Personalized Study Advisor (Extension)**: Generates actionable advice based on attendance, study deficits, and target goals.

---

## 6. Technology Stack
- **Language**: Python 3.10+
- **Machine Learning**: scikit-learn (Random Forest, Ridge, Linear Regression, Logistic Regression)
- **Data Engineering**: Pandas, NumPy
- **Dashboard & Visuals**: Streamlit, Plotly, Seaborn, Matplotlib
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 150 MB free space.
- **Web Browser**: Chrome, Edge, Firefox, or Safari.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-07-Student-Performance-Predictor

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Dataset & Model Setup
The pre-seeded student dataset (`data/student_data.csv`) is automatically loaded, validated, and used to train and serialize the model pipelines into `models/`.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
DATA_PATH=data/student_data.csv
MODELS_DIR=models/
AT_RISK_GRADE_THRESHOLD=45.0
AT_RISK_ATTENDANCE_THRESHOLD=75.0
DEFAULT_TARGET=final_score
```

---

## 11. Commands to Run the Application
```powershell
# Launch Streamlit Interactive Web Dashboard
streamlit run src/app.py

# Run in Headless CLI Evaluation Mode
python src/main.py --cli --attendance 85 --study-hours 14 --past-score 75
```
*Access the dashboard at `http://localhost:8501`.*

---

## 12. Demo Credentials
No login required. Publicly accessible academic dashboard.

---

## 13. Screenshots and Interface Description
1. **Interactive Predictor Tab**: Sliders for academic inputs, predicted final grade gauge, academic standing badge, and risk alert.
2. **Teacher At-Risk Dashboard**: Metric summaries, filtered intervention roster table, and one-click CSV export.
3. **Personalized Study Advisor**: Actionable recommendations categorized by urgency (Attendance, Study Scheduling, Target Gap).
4. **EDA & Correlation Heatmap**: Interactive matrix showing Pearson correlation coefficients between features and grades.
5. **Model Benchmark**: Comparison tables showing $R^2$, RMSE, MAE, Accuracy, and Feature Importance bar charts.

---

## 14. API Documentation
Key methods:
- `DataCleaner.load_and_clean(csv_path)`
- `ModelEngine.train(df)`
- `ModelEngine.predict_single(features_dict)`
- `AcademicAdvisor.generate_recommendations(features, prediction, target_goal)`

---

## 15. Test Instructions
```powershell
python tests/test_predictor.py
```
*All 7 unit tests verify dataset cleaning, regression metrics integrity ($R^2 \ge 0.80$), classification accuracy ($\ge 0.80$), feature importance sums, high-performing vs at-risk student predictions, and advisor recommendation triggers.*

---

## 16. Known Limitations
- Does not measure qualitative personal factors (e.g. unexpected family emergencies, acute illness on exam day).

---

## 17. Future Enhancements
- Integration with Learning Management Systems (Moodle, Canvas) via LTI standards.
- Time-series tracking of weekly assignment progress over a full semester.

---

## 18. Troubleshooting
- **Streamlit Port Conflict**: Run with `streamlit run src/app.py --server.port 8503`.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines for academic evaluation.

