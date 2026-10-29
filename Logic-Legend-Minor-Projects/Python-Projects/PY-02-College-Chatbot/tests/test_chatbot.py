"""
Logic Legend - PY-02 AI Chatbot for College Queries
Automated Unit and Integration Test Suite
"""

import os
import sys
import tempfile
import unittest
import json
import gc

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from database import ChatbotDatabase
from nlp_engine import NLPEngine

class TestCollegeChatbot(unittest.TestCase):
    def setUp(self):
        # Create temp database
        self.temp_db_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        os.close(self.temp_db_fd)
        self.db = ChatbotDatabase(db_path=self.temp_db_path)

        # Create a mock knowledge base
        self.mock_kb_fd, self.mock_kb_path = tempfile.mkstemp(suffix=".json")
        os.close(self.mock_kb_fd)
        mock_data = {
            "intents": [
                {
                    "tag": "admissions",
                    "patterns": ["how to apply for admission", "admission dates", "eligibility btech"],
                    "responses": ["Admissions open online. Eligibility is 60% in 10+2."]
                },
                {
                    "tag": "fees",
                    "patterns": ["what is the fee structure", "tuition fees", "hostel fee"],
                    "responses": ["Tuition fees is INR 1,25,000 per year."]
                },
                {
                    "tag": "placements",
                    "patterns": ["highest package", "placement statistics", "top recruiters"],
                    "responses": ["Highest package was INR 44 LPA with 92% placements."]
                }
            ]
        }
        with open(self.mock_kb_path, "w", encoding="utf-8") as f:
            json.dump(mock_data, f)

        self.nlp = NLPEngine(kb_path=self.mock_kb_path, confidence_threshold=0.30)

    def tearDown(self):
        del self.nlp
        del self.db
        gc.collect()
        try:
            if os.path.exists(self.temp_db_path):
                os.remove(self.temp_db_path)
            if os.path.exists(self.mock_kb_path):
                os.remove(self.mock_kb_path)
        except OSError:
            pass

    def test_01_nlp_intent_matching(self):
        """Test accurate intent classification for standard college queries."""
        pred = self.nlp.predict_intent("Tell me about admission eligibility for btech")
        self.assertEqual(pred["tag"], "admissions")
        self.assertTrue(pred["verified"])
        self.assertGreater(pred["confidence"], 0.30)
        self.assertIn("60%", pred["response"])

        pred_fee = self.nlp.predict_intent("What is the annual tuition fees?")
        self.assertEqual(pred_fee["tag"], "fees")
        self.assertTrue(pred_fee["verified"])

        pred_place = self.nlp.predict_intent("What was the highest package last year?")
        self.assertEqual(pred_place["tag"], "placements")
        self.assertTrue(pred_place["verified"])

    def test_02_nlp_fallback_handling(self):
        """Test fallback triggered on unmapped queries and low confidence."""
        pred = self.nlp.predict_intent("quantum entanglement astrophysics rocket propulsion")
        self.assertEqual(pred["tag"], "fallback")
        self.assertFalse(pred["verified"])
        self.assertTrue(pred["suggest_escalation"])
        self.assertIn("Talk to Admin", pred["response"])

    def test_03_database_chat_logging(self):
        """Test interaction persistence in SQLite."""
        self.db.log_interaction("test_session_101", "Admission details?", "Admissions open online.", "admissions", 0.85)
        history = self.db.get_chat_history("test_session_101")
        self.assertEqual(len(history), 1)
        self.assertEqual(history[0]["detected_intent"], "admissions")
        self.assertEqual(history[0]["user_message"], "Admission details?")

    def test_04_escalation_workflow(self):
        """Test student escalation creation and administrative resolution."""
        esc_id = self.db.create_escalation(
            name="Aman Verma",
            email="aman@example.com",
            phone="9876543210",
            query="Inquiry regarding special sports quota admission"
        )
        self.assertGreater(esc_id, 0)

        # Check pending list
        pending = self.db.get_escalations(status="PENDING")
        self.assertEqual(len(pending), 1)
        self.assertEqual(pending[0]["student_name"], "Aman Verma")

        # Resolve escalation
        resolved = self.db.resolve_escalation(esc_id)
        self.assertTrue(resolved)

        resolved_list = self.db.get_escalations(status="RESOLVED")
        self.assertEqual(len(resolved_list), 1)

    def test_05_admin_authentication(self):
        """Test administrator credential verification."""
        user = self.db.verify_admin("admin", "admin123")
        self.assertIsNotNone(user)
        self.assertEqual(user["username"], "admin")

        invalid_user = self.db.verify_admin("admin", "wrongpassword")
        self.assertIsNone(invalid_user)

    def test_06_dynamic_knowledge_base_crud(self):
        """Test adding and deleting an intent dynamically."""
        ok, msg = self.nlp.add_intent(
            tag="hostel_rules",
            patterns=["hostel gate closing time", "hostel curfew", "night entry rules"],
            responses=["Hostel gates close strictly at 9:30 PM."]
        )
        self.assertTrue(ok)

        # Verify new intent prediction
        pred = self.nlp.predict_intent("What is the hostel gate closing time?")
        self.assertEqual(pred["tag"], "hostel_rules")
        self.assertIn("9:30 PM", pred["response"])

        # Delete intent
        del_ok, _ = self.nlp.delete_intent("hostel_rules")
        self.assertTrue(del_ok)

if __name__ == "__main__":
    unittest.main()

