"""
Logic Legend - PY-10 Sentiment Analysis of Product Reviews
Text Preprocessing, Cleaning, & N-Gram Tokenizer Layer
"""

import re
import string
from collections import Counter

# Standard English stopwords list for zero-dependency local fallback
STOPWORDS = {
    "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
    "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
    "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
    "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
    "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
    "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into",
    "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
    "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
    "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's",
    "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs",
    "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're",
    "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't",
    "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's",
    "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't",
    "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself",
    "yourselves"
}

class TextPreprocessor:
    @staticmethod
    def clean_text(raw_text: str) -> str:
        """Sanitizes text by stripping HTML, URLs, and punctuation while preserving sentence boundaries."""
        if not raw_text or not isinstance(raw_text, str):
            return ""

        # Remove HTML tags
        cleaned = re.sub(r'<[^>]+>', ' ', raw_text)
        # Remove URLs
        cleaned = re.sub(r'https?://\S+|www\.\S+', ' ', cleaned)
        # Normalize whitespace
        cleaned = re.sub(r'\s+', ' ', cleaned).strip()
        return cleaned

    @classmethod
    def tokenize(cls, text: str, remove_stopwords: bool = True) -> list[str]:
        """Converts text into filtered lowercase alphabetic tokens."""
        cleaned = cls.clean_text(text).lower()
        # Extract alphanumeric words
        words = re.findall(r'\b[a-z]{2,}\b', cleaned)
        if remove_stopwords:
            words = [w for w in words if w not in STOPWORDS]
        return words

    @classmethod
    def extract_ngrams(cls, texts: list[str], n: int = 2, top_k: int = 10) -> list[tuple[str, int]]:
        """Extracts most frequent n-grams from a collection of text documents."""
        ngram_counter = Counter()
        for t in texts:
            tokens = cls.tokenize(t, remove_stopwords=True)
            if len(tokens) >= n:
                for i in range(len(tokens) - n + 1):
                    gram = " ".join(tokens[i:i + n])
                    ngram_counter[gram] += 1
        return ngram_counter.most_common(top_k)

