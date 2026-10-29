"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Interactive Streamlit Web Dashboard
"""

import os
import sys
import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go

# Add src to path
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from sentiment_engine import SentimentEngine
from analyzer_service import AnalyzerService

# Page Setup
st.set_page_config(
    page_title="Logic Legend | PY-10 Sentiment Analysis",
    page_icon="💬",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom Styling
st.markdown("""
<style>
    .main-title { font-size: 2.1rem; font-weight: 800; color: #0F172A; margin-bottom: 0px; }
    .sub-title { font-size: 1rem; color: #64748B; margin-bottom: 20px; }
    .kpi-card { background: white; border-radius: 8px; padding: 15px; border: 1px solid #E2E8F0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); text-align: center; }
    .kpi-value { font-size: 1.8rem; font-weight: 700; color: #1E293B; }
    .kpi-label { font-size: 0.85rem; color: #64748B; text-transform: uppercase; letter-spacing: 0.5px; }
</style>
""", unsafe_allow_html=True)

@st.cache_resource
def get_service():
    engine = SentimentEngine()
    return AnalyzerService(engine)

service = get_service()

# Sidebar Configuration
st.sidebar.image("https://img.icons8.com/fluency/96/chat-bubble.png", width=64)
st.sidebar.title("Configuration")
data_source = st.sidebar.radio("Data Source:", ["Pre-loaded Tech Reviews Dataset", "Upload Custom CSV File"])

df_raw = None
if data_source == "Pre-loaded Tech Reviews Dataset":
    default_csv = os.path.join(os.path.dirname(current_dir), "data", "sample_product_reviews.csv")
    if os.path.exists(default_csv):
        df_raw = pd.read_csv(default_csv)
    else:
        st.sidebar.error("Default dataset not found.")
else:
    uploaded = st.sidebar.file_uploader("Upload CSV file containing reviews", type=["csv"])
    if uploaded:
        df_raw = pd.read_csv(uploaded)

st.markdown('<div class="main-title">LOGIC LEGEND  |  Product Review Sentiment Analyzer</div>', unsafe_allow_html=True)
st.markdown('<div class="sub-title">Natural Language Processing with VADER, TextBlob & Aspect-Based Sentiment Analysis (ABSA)</div>', unsafe_allow_html=True)

if df_raw is not None and not df_raw.empty:
    text_cols = [c for c in df_raw.columns if "text" in c.lower() or "review" in c.lower() or "comment" in c.lower()]
    sel_col = text_cols[0] if text_cols else df_raw.columns[0]
    
    # Process dataframe
    with st.spinner("Analyzing text polarity and aspect sentiment..."):
        df_analyzed = service.analyze_dataframe(df_raw, text_column=sel_col)
        metrics = service.compute_summary_metrics(df_analyzed)

    tab1, tab2, tab3, tab4, tab5 = st.tabs([
        "📊 Corpus Overview",
        "🔍 Aspect-Based Sentiment (ABSA)",
        "🔤 Key Phrases & N-Grams",
        "✍️ Live Review Inspector",
        "💾 Data Export"
    ])

    with tab1:
        # KPI Row
        k1, k2, k3, k4, k5 = st.columns(5)
        with k1:
            st.markdown(f'<div class="kpi-card"><div class="kpi-value">{metrics["total_reviews"]}</div><div class="kpi-label">Total Reviews</div></div>', unsafe_allow_html=True)
        with k2:
            st.markdown(f'<div class="kpi-card"><div class="kpi-value" style="color:#10B981;">{metrics["positive_pct"]}%</div><div class="kpi-label">Positive Sentiment</div></div>', unsafe_allow_html=True)
        with k3:
            st.markdown(f'<div class="kpi-card"><div class="kpi-value" style="color:#EF4444;">{metrics["negative_pct"]}%</div><div class="kpi-label">Negative Sentiment</div></div>', unsafe_allow_html=True)
        with k4:
            nss_color = "#10B981" if metrics["net_sentiment_score"] > 0 else "#EF4444"
            st.markdown(f'<div class="kpi-card"><div class="kpi-value" style="color:{nss_color};">{metrics["net_sentiment_score"]}</div><div class="kpi-label">Net Sentiment Score</div></div>', unsafe_allow_html=True)
        with k5:
            avg_rt = f"{metrics['avg_rating']} ★" if metrics["avg_rating"] else "N/A"
            st.markdown(f'<div class="kpi-card"><div class="kpi-value" style="color:#F59E0B;">{avg_rt}</div><div class="kpi-label">Avg Rating</div></div>', unsafe_allow_html=True)

        st.markdown("<br>", unsafe_allow_html=True)

        col_left, col_right = st.columns([1, 1])
        with col_left:
            st.subheader("Sentiment Distribution")
            fig_donut = px.pie(
                df_analyzed,
                names="sentiment",
                color="sentiment",
                color_discrete_map={"POSITIVE": "#10B981", "NEUTRAL": "#94A3B8", "NEGATIVE": "#EF4444"},
                hole=0.45
            )
            fig_donut.update_layout(margin=dict(t=10, b=10, l=10, r=10), height=320)
            st.plotly_chart(fig_donut, use_container_width=True)

        with col_right:
            st.subheader("VADER Compound Score Distribution")
            fig_hist = px.histogram(
                df_analyzed,
                x="compound_score",
                nbins=20,
                color="sentiment",
                color_discrete_map={"POSITIVE": "#10B981", "NEUTRAL": "#94A3B8", "NEGATIVE": "#EF4444"},
                labels={"compound_score": "Compound Sentiment Score (-1.0 to +1.0)"}
            )
            fig_hist.update_layout(margin=dict(t=10, b=10, l=10, r=10), height=320)
            st.plotly_chart(fig_hist, use_container_width=True)

        # Category Breakdown
        cat_df = service.compute_category_breakdown(df_analyzed)
        if not cat_df.empty:
            st.subheader("Category-Wise Sentiment Breakdown")
            fig_cat = px.bar(
                cat_df,
                x="Category",
                y=["Positive %", "Negative %"],
                barmode="group",
                color_discrete_map={"Positive %": "#10B981", "Negative %": "#EF4444"}
            )
            fig_cat.update_layout(height=340, margin=dict(t=20, b=10))
            st.plotly_chart(fig_cat, use_container_width=True)

        # Product Ranking
        prod_df = service.compute_product_ranking(df_analyzed)
        if not prod_df.empty:
            st.subheader("Product Performance Ranking")
            st.dataframe(prod_df, use_container_width=True)

    with tab2:
        st.subheader("Aspect-Based Sentiment Analysis (ABSA)")
        st.write("Granular extraction of sentiments associated with specific hardware and service attributes.")
        aspect_data = service.compute_aggregate_aspects(df_analyzed, text_column=sel_col)

        if aspect_data:
            aspect_rows = []
            for aspect, d in aspect_data.items():
                aspect_rows.append({
                    "Aspect": aspect,
                    "Mentions": d["total_mentions"],
                    "Average Score": d["average_score"],
                    "Classification": d["classification"]
                })
            asp_df = pd.DataFrame(aspect_rows).sort_values(by="Average Score", ascending=False)

            fig_asp = px.bar(
                asp_df,
                x="Average Score",
                y="Aspect",
                orientation="h",
                color="Classification",
                color_discrete_map={"POSITIVE": "#10B981", "NEUTRAL": "#94A3B8", "NEGATIVE": "#EF4444"},
                labels={"Average Score": "Mean Compound Score (-1 to +1)"}
            )
            fig_asp.update_layout(height=380, margin=dict(t=10, b=10))
            st.plotly_chart(fig_asp, use_container_width=True)

            st.dataframe(asp_df, use_container_width=True)
        else:
            st.info("No specific aspect keywords detected in the current text corpus.")

    with tab3:
        st.subheader("Top Extracted N-Grams & Recurring Phrases")
        col_p, col_n = st.columns(2)
        phrases = service.extract_key_phrases(df_analyzed, text_column=sel_col)

        with col_p:
            st.markdown("#### 🟢 Top Positive Key Phrases")
            pos_grams = phrases["top_positive_bigrams"]
            if pos_grams:
                df_p = pd.DataFrame(pos_grams, columns=["Phrase", "Count"])
                st.dataframe(df_p, use_container_width=True)
            else:
                st.write("No positive bigrams found.")

        with col_n:
            st.markdown("#### 🔴 Top Negative Key Phrases")
            neg_grams = phrases["top_negative_bigrams"]
            if neg_grams:
                df_n = pd.DataFrame(neg_grams, columns=["Phrase", "Count"])
                st.dataframe(df_n, use_container_width=True)
            else:
                st.write("No negative bigrams found.")

    with tab4:
        st.subheader("Single Review Live Inspector")
        user_input = st.text_area(
            "Enter or paste a customer review:",
            value="The sound quality and deep bass are unbelievable! However, the battery life drains too fast after 3 hours."
        )

        if user_input:
            res = service.engine.analyze_full_review(user_input)
            cls_color = "#10B981" if res["classification"] == "POSITIVE" else ("#EF4444" if res["classification"] == "NEGATIVE" else "#94A3B8")

            col_s1, col_s2, col_s3, col_s4 = st.columns(4)
            with col_s1:
                st.metric("Predicted Sentiment", res["classification"])
            with col_s2:
                st.metric("Compound Score", f"{res['compound_score']:+.4f}")
            with col_s3:
                st.metric("Polarity", f"{res['polarity']:+.2f}")
            with col_s4:
                st.metric("Subjectivity", f"{res['subjectivity']:.2f}")

            # Aspect detections
            if res["aspects"]:
                st.markdown("#### Detected Aspect Sentiment Breakdown:")
                for aspect, d in res["aspects"].items():
                    asp_color = "green" if d["classification"] == "POSITIVE" else ("red" if d["classification"] == "NEGATIVE" else "gray")
                    st.markdown(f"- **{aspect}**: :{asp_color}[**{d['classification']}**] (Score: {d['sentiment_score']})")
                    for sent in d["sample_sentences"]:
                        st.caption(f'  "{sent}"')

    with tab5:
        st.subheader("Data Export")
        st.write("Download the complete analysis including compound, positive, neutral, negative, polarity, subjectivity, and predicted sentiment labels.")
        csv_bytes = df_analyzed.to_csv(index=False).encode("utf-8")
        st.download_button(
            label="📥 Download Augmented Dataset (CSV)",
            data=csv_bytes,
            file_name="sentiment_analysis_results.csv",
            mime="text/csv"
        )
        st.dataframe(df_analyzed, use_container_width=True)
else:
    st.info("Please select or upload a dataset to begin sentiment analysis.")

