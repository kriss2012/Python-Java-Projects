"""
Logic Legend - PY-02 AI Chatbot for College Queries
NLP Intent Classification and Response Engine
"""

import json
import os
import re
import random
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

class NLPEngine:
    def __init__(self, kb_path=None, confidence_threshold=0.30):
        if not kb_path:
            kb_path = os.path.join(os.path.dirname(__file__), "knowledge_base.json")
        self.kb_path = kb_path
        self.confidence_threshold = confidence_threshold
        self.vectorizer = None
        self.pattern_vectors = None
        self.patterns = []
        self.pattern_tags = []
        self.intents = {}
        self.load_knowledge_base()

    def clean_text(self, text):
        """Lowercases and cleans special characters for robust matching."""
        text = text.lower()
        text = re.sub(r'[^a-zA-Z0-9\s]', ' ', text)
        return ' '.join(text.split())

    def load_knowledge_base(self):
        if not os.path.exists(self.kb_path):
            raise FileNotFoundError(f"Knowledge base not found at: {self.kb_path}")

        with open(self.kb_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        self.patterns = []
        self.pattern_tags = []
        self.intents = {}

        for item in data.get("intents", []):
            tag = item["tag"]
            self.intents[tag] = {
                "responses": item.get("responses", []),
                "patterns": item.get("patterns", [])
            }
            for p in item.get("patterns", []):
                cleaned = self.clean_text(p)
                if cleaned:
                    self.patterns.append(cleaned)
                    self.pattern_tags.append(tag)

        # Train TF-IDF vectorizer on all patterns
        if self.patterns:
            self.vectorizer = TfidfVectorizer(ngram_range=(1, 2), sublinear_tf=True)
            self.pattern_vectors = self.vectorizer.fit_transform(self.patterns)

    def save_knowledge_base(self):
        data = {"intents": []}
        for tag, val in self.intents.items():
            data["intents"].append({
                "tag": tag,
                "patterns": val["patterns"],
                "responses": val["responses"]
            })
        with open(self.kb_path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
        # Retrain in memory
        self.load_knowledge_base()

    def predict_intent(self, user_message):
        cleaned = self.clean_text(user_message)
        if not cleaned or not self.vectorizer or not self.patterns:
            return {
                "tag": "unknown",
                "confidence": 0.0,
                "response": "I didn't quite catch that. Could you please rephrase your college query?",
                "verified": False,
                "suggest_escalation": False
            }

        user_vec = self.vectorizer.transform([cleaned])
        similarities = cosine_similarity(user_vec, self.pattern_vectors)[0]
        max_idx = similarities.argmax()
        best_score = float(similarities[max_idx])
        best_tag = self.pattern_tags[max_idx]

        if best_score < self.confidence_threshold:
            return {
                "tag": "fallback",
                "confidence": round(best_score, 2),
                "response": "I do not have verified official records matching that specific question. Would you like to connect with a college administrator via our 'Talk to Admin' escalation desk?",
                "verified": False,
                "suggest_escalation": True
            }

        responses = self.intents[best_tag]["responses"]
        chosen_resp = random.choice(responses) if responses else "Information not yet configured."
        return {
            "tag": best_tag,
            "confidence": round(best_score, 2),
            "response": chosen_resp,
            "verified": True,
            "suggest_escalation": False
        }

    # Administrative FAQ Management
    def add_intent(self, tag, patterns, responses):
        tag = tag.strip().lower()
        if not tag:
            return False, "Intent tag cannot be empty."
        if tag in self.intents:
            return False, f"Intent '{tag}' already exists. Use update."

        self.intents[tag] = {
            "patterns": [p.strip() for p in patterns if p.strip()],
            "responses": [r.strip() for r in responses if r.strip()]
        }
        self.save_knowledge_base()
        return True, f"Intent '{tag}' added successfully."

    def update_intent(self, tag, patterns, responses):
        if tag not in self.intents:
            return False, f"Intent '{tag}' does not exist."
        self.intents[tag]["patterns"] = [p.strip() for p in patterns if p.strip()]
        self.intents[tag]["responses"] = [r.strip() for r in responses if r.strip()]
        self.save_knowledge_base()
        return True, f"Intent '{tag}' updated."

    def delete_intent(self, tag):
        if tag not in self.intents:
            return False, f"Intent '{tag}' not found."
        del self.intents[tag]
        self.save_knowledge_base()
        return True, f"Intent '{tag}' deleted."

    def get_all_intents(self):
        return self.intents
