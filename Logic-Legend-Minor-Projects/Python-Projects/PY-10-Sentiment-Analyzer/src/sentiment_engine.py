"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Sentiment Engine (VADER, TextBlob, & Aspect-Based Sentiment Analysis)
"""

import re
from textblob import TextBlob

# Initialize VADER with offline heuristic fallback
try:
    import nltk
    from nltk.sentiment.vader import SentimentIntensityAnalyzer
    try:
        _vader = SentimentIntensityAnalyzer()
    except LookupError:
        nltk.download("vader_lexicon", quiet=True)
        _vader = SentimentIntensityAnalyzer()
except Exception:
    _vader = None

# Aspect Lexicon Mapping
ASPECT_KEYWORDS = {
    "Battery & Power": ["battery", "charge", "charging", "drain", "power", "runtime", "endurance"],
    "Audio & Sound": ["sound", "audio", "bass", "treble", "volume", "speaker", "speakers", "mic", "microphone", "earcups"],
    "Display & Visuals": ["screen", "display", "panel", "resolution", "4k", "oled", "amoled", "srgb", "colors", "pixel", "pixels"],
    "Performance & Speed": ["performance", "speed", "fast", "slow", "lag", "cooling", "fan", "heat", "overheats", "processor", "ram", "ssd"],
    "Build & Design": ["build", "design", "durability", "hinge", "frame", "strap", "chassis", "sturdy", "flimsy", "plastic", "aluminum", "ergonomic"],
    "Price & Value": ["price", "cost", "value", "worth", "money", "expensive", "cheap", "affordable", "deal"],
    "Customer Support": ["support", "service", "warranty", "refund", "return", "replacement", "agent"]
}

# Heuristic sentiment word lists for guaranteed zero-dependency fallback
FALLBACK_POSITIVE = {
    "excellent", "outstanding", "fantastic", "mind-blowing", "great", "good", "super", "fast",
    "gorgeous", "bright", "accurate", "satisfying", "vibrant", "crisp", "convenient", "smooth",
    "solid", "reliable", "best", "love", "awesome", "perfect", "worth", "sturdy", "comfortable"
}

FALLBACK_NEGATIVE = {
    "terrible", "horrible", "awful", "disappointed", "disappointing", "broke", "broken",
    "dropping", "overheats", "loud", "poor", "unhelpful", "crashes", "waste", "distortion",
    "dead", "bleeding", "defective", "cheap", "burnt", "dangerous", "jittery", "stutters",
    "died", "flimsy", "loose", "regret", "bad", "worst", "slow"
}

class SentimentEngine:
    def __init__(self):
        self.vader = _vader

    def _fallback_vader_scores(self, text: str) -> dict:
        """Deterministic rule-based sentiment scorer if NLTK VADER data is unreachable."""
        tokens = re.findall(r'\b[a-z]{2,}\b', text.lower())
        pos_hits = sum(1 for t in tokens if t in FALLBACK_POSITIVE)
        neg_hits = sum(1 for t in tokens if t in FALLBACK_NEGATIVE)
        total = max(1, len(tokens))

        pos_ratio = round(pos_hits / total, 3)
        neg_ratio = round(neg_hits / total, 3)
        neu_ratio = round(max(0.0, 1.0 - (pos_ratio + neg_ratio)), 3)

        diff = pos_hits - neg_hits
        compound = max(-1.0, min(1.0, diff / max(1, (pos_hits + neg_hits + 1))))
        return {
            "neg": neg_ratio,
            "neu": neu_ratio,
            "pos": pos_ratio,
            "compound": round(compound, 4)
        }

    def analyze_vader(self, text: str) -> dict:
        """Calculates VADER intensity scores (negative, neutral, positive, compound)."""
        if not text:
            return {"neg": 0.0, "neu": 1.0, "pos": 0.0, "compound": 0.0}

        if self.vader:
            try:
                return self.vader.polarity_scores(text)
            except Exception:
                pass
        return self._fallback_vader_scores(text)

    @staticmethod
    def analyze_textblob(text: str) -> dict:
        """Calculates TextBlob polarity (-1.0 to 1.0) and subjectivity (0.0 to 1.0)."""
        if not text:
            return {"polarity": 0.0, "subjectivity": 0.0}
        try:
            blob = TextBlob(text)
            return {
                "polarity": round(float(blob.sentiment.polarity), 4),
                "subjectivity": round(float(blob.sentiment.subjectivity), 4)
            }
        except Exception:
            return {"polarity": 0.0, "subjectivity": 0.0}

    def classify_sentiment(self, compound_score: float) -> str:
        """Categorizes compound sentiment score into standard classes."""
        if compound_score >= 0.05:
            return "POSITIVE"
        elif compound_score <= -0.05:
            return "NEGATIVE"
        else:
            return "NEUTRAL"

    def analyze_full_review(self, text: str) -> dict:
        """Runs complete composite sentiment analysis on a single text string."""
        vader_res = self.analyze_vader(text)
        blob_res = self.analyze_textblob(text)
        classification = self.classify_sentiment(vader_res["compound"])

        # Run Aspect-Based Sentiment Analysis
        aspects = self.analyze_aspects(text)

        return {
            "text": text,
            "classification": classification,
            "compound_score": vader_res["compound"],
            "positive_score": vader_res["pos"],
            "neutral_score": vader_res["neu"],
            "negative_score": vader_res["neg"],
            "polarity": blob_res["polarity"],
            "subjectivity": blob_res["subjectivity"],
            "aspects": aspects
        }

    def analyze_aspects(self, text: str) -> dict:
        """
        Splits text into sentences, identifies sentences referencing specific product aspects,
        and computes aspect-level sentiment.
        """
        sentences = re.split(r'[.!?]+', text)
        aspect_results = {}

        for aspect_name, keywords in ASPECT_KEYWORDS.items():
            matched_sentences = []
            aspect_compounds = []

            for s in sentences:
                s_clean = s.strip()
                if not s_clean:
                    continue
                words = set(re.findall(r'\b[a-z]{3,}\b', s_clean.lower()))
                if words.intersection(keywords):
                    v_score = self.analyze_vader(s_clean)["compound"]
                    matched_sentences.append(s_clean)
                    aspect_compounds.append(v_score)

            if aspect_compounds:
                avg_score = round(sum(aspect_compounds) / len(aspect_compounds), 4)
                aspect_results[aspect_name] = {
                    "detected": True,
                    "sentiment_score": avg_score,
                    "classification": self.classify_sentiment(avg_score),
                    "mention_count": len(aspect_compounds),
                    "sample_sentences": matched_sentences[:2]
                }

        return aspect_results

