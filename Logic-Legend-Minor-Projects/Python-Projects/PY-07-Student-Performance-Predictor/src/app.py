"""
Logic Legend - PY-07 Student Performance Predictor
Interactive Streamlit Machine Learning Dashboard
"""

import os
import sys
import streamlit as st
import pandas as pd
import numpy as np
import plotly.express as px
import plotly.graph_objects as go

# Ensure src in path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from data_cleaner import DataCleaner, REQUIRED_FEATURES
from model_engine import ModelEngine
from advisor import AcademicAdvisor

st.set_page_config(
    page_title="Logic Legend — Student Performance Predictor",
    page_icon="🎓",
    layout="wide"
)

# Custom Styling
st.markdown("""
<style>
    .badge-atrisk { background-color: #FEE2E2; color: #991B1B; padding: 4px 10px; border-radius: 12px; font-weight: bold; }
    .badge-pass { background-color: #DBEAFE; color: #1E40AF; padding: 4px 10px; border-radius: 12px; font-weight: bold; }
    .badge-dist { background-color: #DCFCE7; color: #166534; padding: 4px 10px; border-radius: 12px; font-weight: bold; }
</style>
""", unsafe_allow_html=True)

# Initialize Paths
base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
data_path = os.path.join(base_dir, "data", "student_data.csv")
models_dir = os.path.join(base_dir, "models")

# Load and train
df = DataCleaner.load_and_clean(data_path)
engine = ModelEngine(models_dir=models_dir)
metrics = engine.train(df)

# Header
st.title("🎓 Student Performance Predictor & Academic Early Warning System")
st.caption("Predictive Machine Learning, Feature Importance Analysis & Personalized Study Advising")

# Main Tabs
tab_pred, tab_atrisk, tab_advisor, tab_eda, tab_models = st.tabs([
    "🔮 Predict Student Outcome",
    "🚨 Teacher At-Risk Dashboard",
    "💡 Personalized Study Recommendations",
    "📊 Exploratory Data Analysis (EDA)",
    "🤖 Model Benchmark & Feature Importance"
])

# ----------------- TAB 1: PREDICTOR -----------------
with tab_pred:
    st.subheader("Interactive Performance Estimator")
    st.write("Adjust student academic parameters to generate real-time grade forecasts and risk assessments.")

    col1, col2 = st.columns(2)
    with col1:
        att = st.slider("Class Attendance (%):", min_value=30.0, max_value=100.0, value=82.0, step=0.5)
        study_hrs = st.slider("Weekly Self-Study (Hours):", min_value=1.0, max_value=35.0, value=12.0, step=0.5)
        past_marks = st.slider("Past Semester Exam Marks (out of 100):", min_value=20.0, max_value=100.0, value=72.0, step=1.0)
    with col2:
        internal_marks = st.slider("Internal Test / Continuous Assessment (out of 100):", min_value=20.0, max_value=100.0, value=70.0, step=1.0)
        assignments = st.slider("Assignments Completed (%):", min_value=20.0, max_value=100.0, value=80.0, step=1.0)
        extracurricular = st.slider("Extracurricular / Club Activity (Hours/Week):", min_value=0.0, max_value=20.0, value=5.0, step=0.5)

    input_data = {
        "attendance_pct": att,
        "study_hours_weekly": study_hrs,
        "past_exam_score": past_marks,
        "internal_test_score": internal_marks,
        "assignments_completed": assignments,
        "extracurricular_hours": extracurricular
    }

    if st.button("🚀 Generate Academic Prediction", type="primary"):
        pred = engine.predict_single(input_data)
        st.markdown("---")

        r_col1, r_col2, r_col3 = st.columns(3)
        r_col1.metric("Predicted Final Grade", f"{pred['predicted_score']} %")
        r_col2.metric("Projected Academic Standing", pred["predicted_standing"])
        r_col3.metric("Model Confidence Estimate", f"{pred['confidence']} %")

        if pred["is_at_risk"]:
            st.error("🚨 **Academic Alert**: This student is projected to be **AT RISK** of underperforming or failing. Early faculty intervention recommended.")
        else:
            st.success("✅ **Good Standing**: Student is on track to successfully meet academic credit requirements.")

        st.caption("Responsible AI Notice: Predictions are statistical estimates for academic decision support and must not be used as punitive or definitive determinants of a student's cognitive capabilities.")

# ----------------- TAB 2: TEACHER AT-RISK DASHBOARD -----------------
with tab_atrisk:
    st.subheader("Faculty Early Intervention Roster")
    st.write("Identifies students in the cohort who fall below the statutory 75% attendance threshold or predicted 45% passing mark.")

    # Compute predictions for all cohort rows
    cohort_list = []
    for idx, row in df.iterrows():
        feat = {k: row[k] for k in REQUIRED_FEATURES}
        p = engine.predict_single(feat)
        cohort_list.append({
            "Student ID": row["student_id"],
            "Attendance": f"{row['attendance_pct']}%",
            "Study Hours": f"{row['study_hours_weekly']}h",
            "Past Score": row["past_exam_score"],
            "Internal Score": row["internal_test_score"],
            "Predicted Score": f"{p['predicted_score']}%",
            "Projected Standing": p["predicted_standing"],
            "At-Risk Flag": "⚠️ AT RISK" if p["is_at_risk"] else "NORMAL"
        })

    cohort_df = pd.DataFrame(cohort_list)
    at_risk_df = cohort_df[cohort_df["At-Risk Flag"] == "⚠️ AT RISK"]

    m1, m2, m3 = st.columns(3)
    m1.metric("Total Cohort Size", len(cohort_df))
    m2.metric("Identified At-Risk Students", len(at_risk_df))
    m3.metric("Cohort Risk Percentage", f"{round((len(at_risk_df)/len(cohort_df))*100, 1)} %")

    st.markdown("### At-Risk Student Intervention Table")
    st.dataframe(at_risk_df, use_container_width=True)

    csv_data = at_risk_df.to_csv(index=False).encode('utf-8')
    st.download_button(
        "📥 Export At-Risk Student Roster (CSV)",
        data=csv_data,
        file_name="at_risk_students_report.csv",
        mime="text/csv"
    )

# ----------------- TAB 3: ADVISOR & RECOMMENDATIONS -----------------
with tab_advisor:
    st.subheader("💡 Explainable Personalized Study Plan Generator")
    target_goal = st.slider("Target Academic Goal (%):", min_value=50.0, max_value=95.0, value=75.0, step=5.0)

    # Use current input parameters
    pred_adv = engine.predict_single(input_data)
    recommendations = AcademicAdvisor.generate_recommendations(input_data, pred_adv, target_goal=target_goal)

    for rec in recommendations:
        urg = rec["urgency"]
        if urg == "CRITICAL":
            st.error(f"**{rec['category']}**: {rec['message']}")
        elif urg == "HIGH":
            st.warning(f"**{rec['category']}**: {rec['message']}")
        elif urg == "MEDIUM":
            st.info(f"**{rec['category']}**: {rec['message']}")
        else:
            st.success(f"**{rec['category']}**: {rec['message']}")

# ----------------- TAB 4: EDA -----------------
with tab_eda:
    st.subheader("Dataset Exploratory Data Analysis (EDA)")
    st.dataframe(df.head(10), use_container_width=True)

    col_e1, col_e2 = st.columns(2)
    with col_e1:
        st.markdown("#### Feature Correlation Heatmap")
        corr = DataCleaner.get_correlation_matrix(df)
        fig_corr = px.imshow(corr, text_auto=True, color_continuous_scale="RdBu_r", aspect="auto")
        st.plotly_chart(fig_corr, use_container_width=True)

    with col_e2:
        st.markdown("#### Study Hours vs Final Score")
        fig_scatter = px.scatter(
            df, x="study_hours_weekly", y="final_score",
            color="academic_standing",
            title="Study Hours vs Final Score by Standing",
            labels={"study_hours_weekly": "Study Hours/Week", "final_score": "Final Score (%)"}
        )
        st.plotly_chart(fig_scatter, use_container_width=True)

# ----------------- TAB 5: MODEL COMPARISON -----------------
with tab_models:
    st.subheader("Supervised Learning Model Comparison")
    st.write("Rigorous cross-model evaluation metrics computed on held-out test data (80/20 train-test split).")

    r_col, c_col = st.columns(2)
    with r_col:
        st.markdown("#### 1. Regression Models (Predicting Continuous Score)")
        reg_df = pd.DataFrame(metrics["regression"]).T
        st.dataframe(reg_df.style.highlight_max(axis=0, subset=["r2_score"]), use_container_width=True)

    with c_col:
        st.markdown("#### 2. Classification Models (Predicting Standing)")
        cls_df = pd.DataFrame(metrics["classification"]).T
        st.dataframe(cls_df.style.highlight_max(axis=0), use_container_width=True)

    st.markdown("#### Feature Importance Analysis (Random Forest)")
    feat_df = pd.DataFrame(list(metrics["feature_importance"].items()), columns=["Feature", "Importance"]).sort_values("Importance", ascending=True)
    fig_imp = px.bar(feat_df, x="Importance", y="Feature", orientation="h", title="Feature Importance Weightings", color="Importance", color_continuous_scale="Viridis")
    st.plotly_chart(fig_imp, use_container_width=True)

