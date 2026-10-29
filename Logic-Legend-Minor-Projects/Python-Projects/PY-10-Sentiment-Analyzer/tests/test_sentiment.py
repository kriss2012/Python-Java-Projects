"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Automated Unit Test Suite
"""

import unittest
import os
import sys
import tempfile
import pandas as pd

# Add src to path
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(BASE_DIR, "src")
if SRC_DIR not in sys.path:
    sys.path.insert(0, SRC_DIR)

from text_preprocessor import TextPreprocessor
from sentiment_engine import SentimentEngine
from analyzer_service import AnalyzerService

class TestSentimentAnalyzer(unittest.TestCase):
    def setUp(self):
        self.engine = SentimentEngine()
        self.service = AnalyzerService(self.engine)

    def test_text_preprocessor(self):
        """Verify URL stripping, HTML cleaning, tokenization, and bigram extraction."""
        raw = "<p>Check this review at https://example.com! The battery life is absolutely incredible.</p>"
        cleaned = TextPreprocessor.clean_text(raw)
        self.assertNotIn("<p>", cleaned)
        self.assertNotIn("https://", cleaned)
        self.assertIn("battery life", cleaned)

        tokens = TextPreprocessor.tokenize(raw, remove_stopwords=True)
        self.assertIn("battery", tokens)
        self.assertIn("incredible", tokens)
        self.assertNotIn("this", tokens)  # Stopword removed

        ngrams = TextPreprocessor.extract_ngrams([raw], n=2, top_k=5)
        self.assertTrue(any(gram[0] == "battery life" for gram in ngrams))

    def test_vader_positive_negative_scoring(self):
        """Verify VADER intensity analyzer scores positive vs negative sentences accurately."""
        pos_text = "The audio quality is outstanding, crystal clear, and completely delightful!"
        pos_res = self.engine.analyze_vader(pos_text)
        self.assertGreater(pos_res["compound"], 0.4)
        self.assertEqual(self.engine.classify_sentiment(pos_res["compound"]), "POSITIVE")

        neg_text = "Terrible experience. The screen was broken on arrival and battery drains in 30 minutes."
        neg_res = self.engine.analyze_vader(neg_text)
        self.assertLess(neg_res["compound"], -0.4)
        self.assertEqual(self.engine.classify_sentiment(neg_res["compound"]), "NEGATIVE")

    def test_textblob_polarity_subjectivity(self):
        """Verify TextBlob polarity and subjectivity calculations."""
        text = "This monitor is extremely bright and very beautiful."
        res = self.engine.analyze_textblob(text)
        self.assertGreater(res["polarity"], 0.0)
        self.assertGreater(res["subjectivity"], 0.0)

    def test_aspect_based_sentiment_extraction(self):
        """Verify aspect identification and targeted sentence sentiment scoring."""
        review = "The sound quality is fantastic and loud. But the battery life is terrible and dies quickly."
        aspects = self.engine.analyze_aspects(review)

        self.assertIn("Audio & Sound", aspects)
        self.assertIn("Battery & Power", aspects)

        self.assertEqual(aspects["Audio & Sound"]["classification"], "POSITIVE")
        self.assertEqual(aspects["Battery & Power"]["classification"], "NEGATIVE")

    def test_dataframe_batch_analysis(self):
        """Verify batch DataFrame processing appends required sentiment features."""
        df = pd.DataFrame({
            "review_id": [1, 2],
            "product_name": ["Widget A", "Widget B"],
            "category": ["Electronics", "Electronics"],
            "rating": [5, 1],
            "review_text": [
                "Exceptional build and brilliant display colors!",
                "Horrible device. Overheats constantly and customer support was useless."
            ]
        })

        df_res = self.service.analyze_dataframe(df, text_column="review_text")
        for col in ["compound_score", "positive_score", "negative_score", "polarity", "subjectivity", "sentiment"]:
            self.assertIn(col, df_res.columns)

        self.assertEqual(df_res["sentiment"].iloc[0], "POSITIVE")
        self.assertEqual(df_res["sentiment"].iloc[1], "NEGATIVE")

    def test_summary_metrics_calculation(self):
        """Verify statistical metrics and Net Sentiment Score (NSS)."""
        df = pd.DataFrame({
            "review_text": ["Great product!", "Good value.", "Worst ever."],
            "compound_score": [0.6, 0.4, -0.7],
            "polarity": [0.8, 0.5, -0.8],
            "subjectivity": [0.6, 0.4, 0.9],
            "sentiment": ["POSITIVE", "POSITIVE", "NEGATIVE"],
            "rating": [5, 4, 1]
        })

        metrics = self.service.compute_summary_metrics(df)
        self.assertEqual(metrics["total_reviews"], 3)
        self.assertEqual(metrics["positive_count"], 2)
        self.assertEqual(metrics["negative_count"], 1)
        self.assertAlmostEqual(metrics["positive_pct"], 66.7, places=1)
        self.assertAlmostEqual(metrics["negative_pct"], 33.3, places=1)
        self.assertAlmostEqual(metrics["net_sentiment_score"], 33.4, places=1)

    def test_category_breakdown_and_ranking(self):
        """Verify category breakdown and product ranking DataFrame outputs."""
        df = pd.DataFrame({
            "product_name": ["Phone A", "Phone B"],
            "category": ["Mobile", "Mobile"],
            "rating": [5, 2],
            "compound_score": [0.75, -0.30],
            "sentiment": ["POSITIVE", "NEGATIVE"]
        })

        cat_df = self.service.compute_category_breakdown(df)
        self.assertEqual(len(cat_df), 1)
        self.assertEqual(cat_df["Reviews"].iloc[0], 2)

        prod_df = self.service.compute_product_ranking(df)
        self.assertEqual(len(prod_df), 2)
        self.assertEqual(prod_df["Product"].iloc[0], "Phone A")

    def test_csv_export(self):
        """Verify analyzed report export to CSV."""
        df = pd.DataFrame({
            "review_id": [101],
            "product_name": ["Smart Speaker"],
            "review_text": ["Super loud and crisp audio."],
            "sentiment": ["POSITIVE"],
            "compound_score": [0.7]
        })
        temp_dir = tempfile.mkdtemp()
        temp_csv = os.path.join(temp_dir, "test_report.csv")

        ok, path = self.service.export_report_csv(df, temp_csv)
        self.assertTrue(ok)
        self.assertTrue(os.path.exists(temp_csv))

        with open(temp_csv, "r", encoding="utf-8") as f:
            content = f.read()
            self.assertIn("Smart Speaker", content)
            self.assertIn("POSITIVE", content)

        try:
            os.remove(temp_csv)
            os.rmdir(temp_dir)
        except OSError:
            pass

if __name__ == "__main__":
    unittest.main()

