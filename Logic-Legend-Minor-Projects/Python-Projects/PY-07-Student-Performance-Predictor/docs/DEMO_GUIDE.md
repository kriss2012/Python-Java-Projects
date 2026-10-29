# EVALUATOR DEMONSTRATION GUIDE
## PY-07: Student Performance Predictor
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-07-Student-Performance-Predictor
streamlit run src/app.py
```
*Streamlit will automatically launch the web dashboard in your browser at `http://localhost:8501`.*

---

## 2. Recommended Demonstration Flow

### Step 1: Simulate a High-Performing Student
- Navigate to the **🔮 Predict Student Outcome** tab.
- Set the sliders to:
  - Class Attendance: `95.0%`
  - Weekly Self-Study: `20.0 Hours`
  - Past Semester Exam Marks: `88.0`
  - Internal Continuous Assessment: `90.0`
  - Assignments Completed: `95.0%`
- Click **🚀 Generate Academic Prediction**.
- Point out:
  - Predicted Final Grade: `~90.5 %`
  - Projected Academic Standing: `Distinction` (Green badge)
  - Model Confidence Estimate: `~95-100%`
  - Good Standing confirmation banner.

### Step 2: Simulate an At-Risk Struggling Student
- Now adjust the sliders to simulate a struggling student:
  - Class Attendance: `52.0%` (Below 75%)
  - Weekly Self-Study: `3.5 Hours`
  - Past Semester Exam Marks: `38.0`
  - Internal Assessment: `35.0`
  - Assignments Completed: `35.0%`
- Click **🚀 Generate Academic Prediction**.
- Point out:
  - Predicted Final Grade: `~35.0 %`
  - Projected Academic Standing: `Fail`
  - Prominent red alert:
    `🚨 Academic Alert: This student is projected to be AT RISK of underperforming or failing. Early faculty intervention recommended.`

### Step 3: Demonstrate Teacher At-Risk Dashboard
- Switch to the **🚨 Teacher At-Risk Dashboard** tab.
- Highlight the cohort summary cards:
  - Total Cohort Size: `50`
  - Identified At-Risk Students: `~12`
  - Cohort Risk Percentage: `~24%`
- Point out the filtered **At-Risk Student Intervention Table**:
  - Displays each struggling student's ID, attendance, study hours, past marks, and projected standing.
- Click **📥 Export At-Risk Student Roster (CSV)** to demonstrate ready-to-share counseling reports for department deans.

### Step 4: Demonstrate Personalized Study Plan Generator (Extension)
- Switch to the **💡 Personalized Study Recommendations** tab.
- Set the target goal to `75%` (Distinction).
- Observe the actionable guidance cards:
  - **Attendance Deficit**: Critical alert instructing student to attend all remaining hours to recover eligibility.
  - **Study Hours Scheduling**: High priority card recommending daily 2-hour Pomodoro study blocks.
  - **Target Goal Alignment**: Calculates exact percentage point gap to close.

### Step 5: Explore EDA & Model Comparison
- Switch to the **📊 Exploratory Data Analysis (EDA)** tab:
  - Point out the correlation heatmap showing strong positive correlation between internal scores and final marks.
- Switch to the **🤖 Model Benchmark & Feature Importance** tab:
  - Show the side-by-side comparison tables:
    - Regression: Linear Regression vs Ridge vs Random Forest ($R^2 \ge 0.90$).
    - Classification: Logistic Regression vs Random Forest (Accuracy $\ge 90\%$).
  - Point out the horizontal bar chart showing Random Forest Feature Importance weights.

---

## 3. Headless CLI Verification
Run the evaluation script from terminal:
```powershell
python src/main.py --cli --attendance 88 --study-hours 16 --past-score 82
```
*Outputs instant CLI grade prediction and study recommendations.*

## 4. Automated Unit Testing
Execute the test suite in terminal:
```powershell
python tests/test_predictor.py
```
*All 7 unit tests pass, verifying data cleaning, model accuracy, feature importance, and advisor recommendation triggers.*

