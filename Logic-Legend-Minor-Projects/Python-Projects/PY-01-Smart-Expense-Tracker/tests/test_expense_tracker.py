"""
Logic Legend - PY-01 Smart Expense Tracker with Analytics
Automated Unit and Integration Test Suite
"""

import os
import sys
import tempfile
import unittest

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from database import DatabaseManager
from tracker_service import TrackerService

class TestExpenseTracker(unittest.TestCase):
    def setUp(self):
        # Create an isolated temporary database for each test
        self.temp_db_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        self.db_mgr = DatabaseManager(db_path=self.temp_db_path)
        self.service = TrackerService(self.db_mgr)

    def tearDown(self):
        os.close(self.temp_db_fd)
        if os.path.exists(self.temp_db_path):
            os.remove(self.temp_db_path)

    def test_01_validation_amount(self):
        """Test amount validation boundaries."""
        valid, val = self.service.validate_amount(150.50)
        self.assertTrue(valid)
        self.assertEqual(val, 150.50)

        valid, msg = self.service.validate_amount(-20)
        self.assertFalse(valid)

        valid, msg = self.service.validate_amount(0)
        self.assertFalse(valid)

        valid, msg = self.service.validate_amount("not_a_number")
        self.assertFalse(valid)

    def test_02_validation_date(self):
        """Test date validation format YYYY-MM-DD."""
        valid, d = self.service.validate_date("2026-10-25")
        self.assertTrue(valid)

        valid, msg = self.service.validate_date("25/10/2026")
        self.assertFalse(valid)

        valid, msg = self.service.validate_date("invalid-date")
        self.assertFalse(valid)

    def test_03_add_and_retrieve_transactions(self):
        """Test inserting income and expense records."""
        # Add income
        ok, _ = self.service.add_transaction("income", 50000.0, "Salary", "October Pay", "2026-10-01", "Bank Transfer")
        self.assertTrue(ok)

        # Add expense
        ok, _ = self.service.add_transaction("expense", 4500.0, "Food & Dining", "Grocery Shopping", "2026-10-05", "Card")
        self.assertTrue(ok)

        records = self.service.get_transactions()
        self.assertEqual(len(records), 2)
        self.assertEqual(records[0]["amount"], 4500.0) # Ordered desc date

    def test_04_financial_summary_calculation(self):
        """Test income, expense, balance, and savings rate math."""
        self.service.add_transaction("income", 10000.0, "Salary", "Test Stipend", "2026-10-01")
        self.service.add_transaction("expense", 4000.0, "Rent & Utilities", "Room Rent", "2026-10-02")
        self.service.add_transaction("expense", 1000.0, "Transportation", "Bus Pass", "2026-10-03")

        summary = self.service.get_financial_summary("2026-10")
        self.assertEqual(summary["income"], 10000.0)
        self.assertEqual(summary["expense"], 5000.0)
        self.assertEqual(summary["balance"], 5000.0)
        self.assertEqual(summary["savings_rate"], 50.0)

    def test_05_budget_threshold_alerts(self):
        """Test budget warning and exceeded statuses."""
        month = "2026-10"
        # Set budget of 1000 with 80% threshold
        self.service.set_budget("Food & Dining", month, 1000.0, 80.0)

        # 1. Under threshold: 500 spent (50%) -> OK
        self.service.add_transaction("expense", 500.0, "Food & Dining", "Dinner", f"{month}-05")
        alerts = self.service.check_budgets(month)
        self.assertEqual(len(alerts), 1)
        self.assertEqual(alerts[0]["status"], "OK")

        # 2. Reached threshold: +350 spent = 850 (85%) -> WARNING
        self.service.add_transaction("expense", 350.0, "Food & Dining", "Cafeteria", f"{month}-10")
        alerts = self.service.check_budgets(month)
        self.assertEqual(alerts[0]["status"], "WARNING")

        # 3. Exceeded: +200 spent = 1050 (105%) -> EXCEEDED
        self.service.add_transaction("expense", 200.0, "Food & Dining", "Snacks", f"{month}-15")
        alerts = self.service.check_budgets(month)
        self.assertEqual(alerts[0]["status"], "EXCEEDED")

    def test_06_delete_transaction(self):
        """Test removing a transaction."""
        self.service.add_transaction("expense", 250.0, "Miscellaneous", "Stationery", "2026-10-10")
        records = self.service.get_transactions()
        t_id = records[0]["id"]

        del_ok = self.service.delete_transaction(t_id)
        self.assertTrue(del_ok)
        self.assertEqual(len(self.service.get_transactions()), 0)

    def test_07_export_csv_and_report(self):
        """Test data export to CSV and formatted report."""
        self.service.add_transaction("income", 20000.0, "Salary", "Test", "2026-10-01")
        self.service.add_transaction("expense", 3500.0, "Food & Dining", "Dinner", "2026-10-02")

        csv_path = os.path.join(tempfile.gettempdir(), "test_export.csv")
        rpt_path = os.path.join(tempfile.gettempdir(), "test_report.txt")

        ok_csv, res_csv = self.service.export_csv(csv_path)
        self.assertTrue(ok_csv)
        self.assertTrue(os.path.exists(res_csv))

        ok_rpt, res_rpt = self.service.export_summary_report(rpt_path, "2026-10")
        self.assertTrue(ok_rpt)
        self.assertTrue(os.path.exists(res_rpt))

        # Clean up
        if os.path.exists(csv_path):
            os.remove(csv_path)
        if os.path.exists(rpt_path):
            os.remove(rpt_path)

    def test_08_ocr_receipt_regex_parser(self):
        """Test OCR extraction algorithm on raw receipt text."""
        receipt_text = """
        METRO ELECTRONICS & BOOKS
        Date: 2026-10-22
        Item: USB Type C Cable   Rs. 450.00
        Item: Notebook Pack      Rs. 150.00
        TOTAL AMOUNT: Rs. 600.00
        """
        extracted = self.service.parse_receipt_text(receipt_text)
        self.assertEqual(extracted["date"], "2026-10-22")
        self.assertEqual(extracted["amount"], 600.0)
        self.assertIn("METRO", extracted["merchant"].upper())

if __name__ == "__main__":
    unittest.main()
