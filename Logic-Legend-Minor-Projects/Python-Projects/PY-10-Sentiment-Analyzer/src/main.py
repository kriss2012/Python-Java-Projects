"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Application Entry Point (CLI & Streamlit)
"""

import sys
import os
import subprocess
import argparse

# Configure UTF-8 stdout for Windows console safety
if sys.platform.startswith("win"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

# Ensure src is in sys.path
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from sentiment_engine import SentimentEngine
from analyzer_service import AnalyzerService

def run_cli_analysis(service, text: str):
    res = service.engine.analyze_full_review(text)
    print("\n" + "=" * 60)
    print("  SENTIMENT ANALYSIS RESULT")
    print("=" * 60)
    print(f"Input Text         : \"{text}\"")
    print(f"Classification     : {res['classification']}")
    print(f"VADER Compound     : {res['compound_score']:+.4f} (Pos: {res['positive_score']}, Neu: {res['neutral_score']}, Neg: {res['negative_score']})")
    print(f"TextBlob Polarity  : {res['polarity']:+.4f} (-1.0 to +1.0)")
    print(f"Subjectivity       : {res['subjectivity']:.4f} (0.0=Objective to 1.0=Subjective)")

    if res['aspects']:
        print("\nAspect-Based Sentiment Breakdown:")
        for aspect, d in res['aspects'].items():
            print(f"  * {aspect:<20} : {d['classification']:<10} (Score: {d['sentiment_score']:+.2f}, Mentions: {d['mention_count']})")
    print("=" * 60 + "\n")

def run_cli_summary(service, file_path=None, export_path=None):
    csv_file = file_path or os.path.join(parent_dir, "data", "sample_product_reviews.csv")
    print(f"\n[INFO] Loading dataset from: {csv_file}")
    df = service.load_dataset(csv_file)
    print(f"[INFO] Loaded {len(df)} reviews. Computing NLP features...")

    df_analyzed = service.analyze_dataframe(df, text_column="review_text")
    metrics = service.compute_summary_metrics(df_analyzed)

    print("\n" + "=" * 65)
    print("  LOGIC LEGEND PY-10: PRODUCT REVIEWS SENTIMENT CORPUS SUMMARY")
    print("=" * 65)
    print(f"Total Reviews Analyzed    : {metrics['total_reviews']}")
    print(f"Positive Sentiment Count  : {metrics['positive_count']} ({metrics['positive_pct']}%)")
    print(f"Neutral Sentiment Count   : {metrics['neutral_count']} ({metrics['neutral_pct']}%)")
    print(f"Negative Sentiment Count  : {metrics['negative_count']} ({metrics['negative_pct']}%)")
    print(f"Net Sentiment Score (NSS) : {metrics['net_sentiment_score']:+}%")
    print(f"Average Compound Score    : {metrics['avg_compound']:+.3f}")
    print(f"Average TextBlob Polarity : {metrics['avg_polarity']:+.3f}")
    print(f"Average Subjectivity      : {metrics['avg_subjectivity']:.3f}")
    if metrics.get("avg_rating"):
        print(f"Average Star Rating       : {metrics['avg_rating']} / 5.0")

    print("\nCategory Breakdown:")
    cat_df = service.compute_category_breakdown(df_analyzed)
    if not cat_df.empty:
        print(cat_df.to_string(index=False))

    print("\nProduct Ranking by Sentiment:")
    prod_df = service.compute_product_ranking(df_analyzed)
    if not prod_df.empty:
        print(prod_df.to_string(index=False))

    print("\nAspect-Based Sentiment Aggregation across Corpus:")
    aspects = service.compute_aggregate_aspects(df_analyzed, text_column="review_text")
    for aspect, d in aspects.items():
        print(f"  * {aspect:<22} : {d['classification']:<10} | Avg Score: {d['average_score']:+.3f} | Mentions: {d['total_mentions']}")

    if export_path:
        ok, out = service.export_report_csv(df_analyzed, export_path)
        print(f"\n[EXPORT] Full analyzed dataset exported to: {out}")
    print("=" * 65 + "\n")

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-10 Sentiment Analysis")
    parser.add_argument("--cli", action="store_true", help="Run in CLI mode")
    parser.add_argument("--analyze", type=str, help="Analyze single review text string")
    parser.add_argument("--summary", action="store_true", help="Analyze default dataset and print summary")
    parser.add_argument("--file", type=str, help="Path to custom CSV review file")
    parser.add_argument("--export", type=str, nargs="?", const="exports/sentiment_analysis_report.csv", help="Export analyzed CSV")

    args = parser.parse_args()
    engine = SentimentEngine()
    service = AnalyzerService(engine)

    if args.cli or args.analyze or args.summary or args.export:
        if args.analyze:
            run_cli_analysis(service, args.analyze)
        if args.summary or args.file or args.export:
            run_cli_summary(service, file_path=args.file, export_path=args.export)
    else:
        app_path = os.path.join(current_dir, "app.py")
        print(f"[INFO] Launching Streamlit Dashboard: streamlit run {app_path}")
        subprocess.run(["streamlit", "run", app_path])

if __name__ == "__main__":
    main()

