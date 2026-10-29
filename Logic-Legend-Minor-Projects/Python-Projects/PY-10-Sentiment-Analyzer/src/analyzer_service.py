"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Analyzer Service & Statistical Aggregation Layer
"""

import os
import pandas as pd
from text_preprocessor import TextPreprocessor
from sentiment_engine import SentimentEngine

class AnalyzerService:
    def __init__(self, engine=None):
        self.engine = engine or SentimentEngine()

    def load_dataset(self, filepath: str = "data/sample_product_reviews.csv") -> pd.DataFrame:
        if not os.path.exists(filepath):
            raise FileNotFoundError(f"Review dataset not found at: {filepath}")
        return pd.read_csv(filepath)

    def analyze_dataframe(self, df: pd.DataFrame, text_column: str = "review_text") -> pd.DataFrame:
        """Processes entire DataFrame and appends comprehensive NLP sentiment features."""
        df_out = df.copy()
        if text_column not in df_out.columns:
            raise ValueError(f"Column '{text_column}' not found in DataFrame.")

        compounds = []
        positives = []
        neutrals = []
        negatives = []
        polarities = []
        subjectivities = []
        classifications = []

        for text in df_out[text_column]:
            t_str = str(text) if pd.notna(text) else ""
            res = self.engine.analyze_full_review(t_str)
            compounds.append(res["compound_score"])
            positives.append(res["positive_score"])
            neutrals.append(res["neutral_score"])
            negatives.append(res["negative_score"])
            polarities.append(res["polarity"])
            subjectivities.append(res["subjectivity"])
            classifications.append(res["classification"])

        df_out["compound_score"] = compounds
        df_out["positive_score"] = positives
        df_out["neutral_score"] = neutrals
        df_out["negative_score"] = negatives
        df_out["polarity"] = polarities
        df_out["subjectivity"] = subjectivities
        df_out["sentiment"] = classifications

        return df_out

    @staticmethod
    def compute_summary_metrics(df: pd.DataFrame) -> dict:
        total = len(df)
        if total == 0:
            return {}

        counts = df["sentiment"].value_counts().to_dict()
        pos_cnt = counts.get("POSITIVE", 0)
        neu_cnt = counts.get("NEUTRAL", 0)
        neg_cnt = counts.get("NEGATIVE", 0)

        pos_pct = round((pos_cnt / total) * 100, 1)
        neu_pct = round((neu_cnt / total) * 100, 1)
        neg_pct = round((neg_cnt / total) * 100, 1)

        # Net Sentiment Score (NSS) = % Positive - % Negative
        nss = round(pos_pct - neg_pct, 1)

        return {
            "total_reviews": total,
            "positive_count": pos_cnt,
            "positive_pct": pos_pct,
            "neutral_count": neu_cnt,
            "neutral_pct": neu_pct,
            "negative_count": neg_cnt,
            "negative_pct": neg_pct,
            "net_sentiment_score": nss,
            "avg_compound": round(df["compound_score"].mean(), 3),
            "avg_polarity": round(df["polarity"].mean(), 3),
            "avg_subjectivity": round(df["subjectivity"].mean(), 3),
            "avg_rating": round(df["rating"].mean(), 2) if "rating" in df.columns else None
        }

    @staticmethod
    def compute_category_breakdown(df: pd.DataFrame) -> pd.DataFrame:
        if "category" not in df.columns:
            return pd.DataFrame()

        records = []
        for cat, group in df.groupby("category"):
            tot = len(group)
            pos = (group["sentiment"] == "POSITIVE").sum()
            neg = (group["sentiment"] == "NEGATIVE").sum()
            records.append({
                "Category": cat,
                "Reviews": tot,
                "Positive %": round((pos / tot) * 100, 1),
                "Negative %": round((neg / tot) * 100, 1),
                "Avg Compound": round(group["compound_score"].mean(), 3),
                "Avg Rating": round(group["rating"].mean(), 2) if "rating" in group.columns else 0.0
            })
        return pd.DataFrame(records).sort_values(by="Reviews", ascending=False)

    @staticmethod
    def compute_product_ranking(df: pd.DataFrame) -> pd.DataFrame:
        if "product_name" not in df.columns:
            return pd.DataFrame()

        records = []
        for prod, group in df.groupby("product_name"):
            tot = len(group)
            pos = (group["sentiment"] == "POSITIVE").sum()
            records.append({
                "Product": prod,
                "Category": group["category"].iloc[0] if "category" in group.columns else "N/A",
                "Reviews": tot,
                "Positive %": round((pos / tot) * 100, 1),
                "Avg Compound": round(group["compound_score"].mean(), 3),
                "Avg Rating": round(group["rating"].mean(), 2) if "rating" in group.columns else 0.0
            })
        return pd.DataFrame(records).sort_values(by="Avg Compound", ascending=False)

    def compute_aggregate_aspects(self, df: pd.DataFrame, text_column: str = "review_text") -> dict:
        """Aggregates aspect-level scores across all reviews in the corpus."""
        aspect_accumulator = {}

        for text in df[text_column]:
            t_str = str(text) if pd.notna(text) else ""
            res = self.engine.analyze_aspects(t_str)
            for aspect, details in res.items():
                if aspect not in aspect_accumulator:
                    aspect_accumulator[aspect] = {"scores": [], "count": 0}
                aspect_accumulator[aspect]["scores"].append(details["sentiment_score"])
                aspect_accumulator[aspect]["count"] += details["mention_count"]

        summary = {}
        for aspect, data in aspect_accumulator.items():
            if data["scores"]:
                avg_s = round(sum(data["scores"]) / len(data["scores"]), 3)
                summary[aspect] = {
                    "average_score": avg_s,
                    "classification": self.engine.classify_sentiment(avg_s),
                    "total_mentions": data["count"]
                }
        return summary

    @staticmethod
    def extract_key_phrases(df: pd.DataFrame, text_column: str = "review_text", top_k: int = 8) -> dict:
        texts_pos = df[df["sentiment"] == "POSITIVE"][text_column].dropna().astype(str).tolist()
        texts_neg = df[df["sentiment"] == "NEGATIVE"][text_column].dropna().astype(str).tolist()

        return {
            "top_positive_bigrams": TextPreprocessor.extract_ngrams(texts_pos, n=2, top_k=top_k),
            "top_negative_bigrams": TextPreprocessor.extract_ngrams(texts_neg, n=2, top_k=top_k)
        }

    @staticmethod
    def export_report_csv(df: pd.DataFrame, filepath: str = "exports/sentiment_analysis_report.csv") -> tuple[bool, str]:
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        df.to_csv(filepath, index=False, encoding="utf-8")
        return True, filepath

