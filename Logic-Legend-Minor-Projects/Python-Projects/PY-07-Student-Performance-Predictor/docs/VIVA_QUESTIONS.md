# VIVA VOCE PREPARATION & QUESTIONS
## PY-07: Student Performance Predictor
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the primary academic goal of this project?
**Answer**: The project builds an early warning decision-support system that predicts student final exam performance and academic standing using continuous engagement and past assessment features. It identifies struggling students weeks before semester finals and provides personalized study recommendations.

### Q2: Why did you implement both regression and classification models?
**Answer**:
- **Regression** estimates a continuous numerical percentage grade (0–100%), allowing educators to gauge exact score trajectories and calculate point gaps.
- **Classification** categorizes students into actionable academic tiers (`Fail`, `Pass`, `Distinction`), simplifying institutional triage and identifying students needing counseling.

### Q3: What features are used for model training, and why?
**Answer**:
1. `attendance_pct`: Measures engagement and eligibility.
2. `study_hours_weekly`: Reflects self-discipline and mastery depth.
3. `past_exam_score`: Captures foundational knowledge and academic baseline.
4. `internal_test_score`: Reflects current semester concept retention.
5. `assignments_completed`: Measures continuous work ethic and practice.
6. `extracurricular_hours`: Provides context on student balance and extracurricular commitments.

### Q4: How did you prevent data leakage during model training?
**Answer**: We split the dataset into 80% training and 20% test partitions simultaneously (`train_test_split(X, y_reg, y_cls)`). All feature scalers (`StandardScaler`) and estimators are fitted strictly on `X_train` and evaluated on `X_test`.

### Q5: What evaluation metrics are used for regression, and what do they indicate?
**Answer**:
1. **$R^2$ Score**: Proportion of variance in final marks explained by engagement features (closer to 1.0 is superior).
2. **Mean Absolute Error (MAE)**: Average magnitude of prediction errors in raw percentage marks.
3. **Root Mean Squared Error (RMSE)**: Penalizes large outliers, measuring standard deviation of residuals.

### Q6: How does Random Forest calculate Feature Importance?
**Answer**: It measures Mean Decrease in Impurity (Gini importance for classification, Mean Squared Error reduction for regression) across all decision tree splits where a given feature was chosen. Features that yield the greatest variance reduction across trees receive higher importance weights.

### Q7: What feature had the highest predictive power in your empirical tests?
**Answer**: Past semester exam marks and continuous internal assessment scores contributed over 55% of the total predictive weighting, followed closely by weekly study hours and class attendance.

### Q8: How does the teacher at-risk dashboard define an "At-Risk" student?
**Answer**: A student is flagged as `AT_RISK` if:
1. Their predicted final score is below $45.0\%$ (failing threshold).
2. Their projected academic standing is `Fail`.
3. Their class attendance is below the university statutory minimum of $75.0\%$.

### Q9: How does the Personalized Study Advisor generate explainable guidance?
**Answer**: The advisor compares student feature values against empirical baselines:
- If attendance $< 75\%$, it warns of exam debarment risk.
- If weekly study hours $< 10$, it suggests incremental daily pomodoro blocks.
- It calculates the gap between the student's predicted score and their target goal (e.g., 75% for Distinction) and recommends specific weekly study increments.

### Q10: What ethical guidelines are implemented regarding algorithmic bias?
**Answer**: In compliance with responsible AI standards, the system explicitly excludes sensitive, unmodifiable personal attributes (gender, caste, religion, socioeconomic background). Predictions are presented as advisory decision-support estimates rather than definitive judgments of student intelligence.

### Q11: Why is StandardScaler used inside a Pipeline for Logistic Regression?
**Answer**: Logistic Regression utilizes gradient-based optimization (`lbfgs`). When features have differing scales (e.g. study hours $0–35$ vs attendance $0–100$), unscaled features cause slow or failed convergence. Standardizing features to zero mean and unit variance ensures rapid, stable convergence.

### Q12: Why did you choose Streamlit for this application?
**Answer**: Streamlit provides native interactive widgets (sliders, metrics, tabs) and integrates directly with Plotly and Pandas, allowing educators to simulate "what-if" scenarios (e.g. "What if the student increases study hours from 6 to 14?") in real time.

### Q13: What automated unit tests were conducted?
**Answer**: Using Python's `unittest` framework:
- Verification of zero null values and boundary clipping in data cleaning.
- Regression $R^2 \ge 0.80$ verification on test data.
- Classification accuracy $\ge 0.80$ verification.
- Feature importance sums to $1.0$.
- High-performing vs at-risk student inference checks.
- Targeted academic advisor recommendation triggers.

### Q14: What happens if a student has low study hours but high past marks?
**Answer**: The ensemble model accounts for multi-feature interactions. While high past marks raise the baseline, low study hours drag down the projected score, resulting in a `Pass` rather than `Distinction`, and the advisor warns of study deficit.

### Q15: How can this application be extended for university-wide deployment?
**Answer**:
1. Integration with campus Learning Management Systems (Canvas, Moodle) via automated REST APIs.
2. Incorporating NLP sentiment analysis of student course feedback and discussion board engagement.
3. Automated alert notifications sent to academic counselors and mentors via email/SMS.

