"""
Logic Legend - PY-08 Price Tracker with Web Scraping
Automated Unit Test Suite
"""

import unittest
import os
import sys
import tempfile
import gc

# Add src to sys.path
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(BASE_DIR, "src")
if SRC_DIR not in sys.path:
    sys.path.insert(0, SRC_DIR)

from database import PriceTrackerDB
from scraper_engine import ScraperEngine
from notifier import Notifier
from tracker_service import TrackerService

class TestPriceTracker(unittest.TestCase):
    def setUp(self):
        # Create temp database file
        self.temp_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        os.close(self.temp_fd)

        self.db = PriceTrackerDB(db_path=self.temp_db_path)
        self.scraper = ScraperEngine()
        self.notifier = Notifier(self.db)
        self.service = TrackerService(self.db, self.scraper, self.notifier)

    def tearDown(self):
        del self.service
        del self.notifier
        del self.scraper
        del self.db
        gc.collect()
        if os.path.exists(self.temp_db_path):
            try:
                os.remove(self.temp_db_path)
            except OSError:
                pass

    def test_scraper_parse_price(self):
        """Verify price parsing strips currency symbols and commas."""
        self.assertEqual(self.scraper.parse_price("₹24,999.00"), 24999.0)
        self.assertEqual(self.scraper.parse_price("$1,299.95"), 1299.95)
        self.assertEqual(self.scraper.parse_price("INR 450"), 450.0)
        self.assertIsNone(self.scraper.parse_price("Out of Stock"))
        self.assertIsNone(self.scraper.parse_price(""))

    def test_mock_product_scraper(self):
        """Verify deterministic mock scraper retrieves title and price for demo URLs."""
        res = self.scraper.fetch_product_details("https://example.com/products/wireless-headphones")
        self.assertIn("title", res)
        self.assertIn("price", res)
        self.assertGreater(res["price"], 0)
        self.assertGreater(res["rating"], 0)

    def test_add_product_validation(self):
        """Verify product addition and validation of target prices."""
        # Invalid target price <= 0
        success, msg = self.service.add_product("https://example.com/p1", "Test Item", -50)
        self.assertFalse(success)
        self.assertIn("greater than zero", msg)

        # Valid addition
        success, msg = self.service.add_product("https://example.com/p1", "Smart Watch", 4500.0)
        self.assertTrue(success)
        self.assertIn("added to tracker", msg)

        products = self.service.get_all_products()
        self.assertEqual(len(products), 1)
        self.assertEqual(products[0]["title"], "Smart Watch")

    def test_price_check_and_alert_trigger(self):
        """Verify price checks update history and trigger alerts when target is reached."""
        # Add product with target price higher than current 37,990 so alert fires immediately
        self.service.add_product("https://example.com/products/4k-monitor", "4K Monitor", 40000.0)
        products = self.service.get_all_products()
        pid = products[0]["id"]

        # Run check
        result = self.service.check_product_price(pid)
        self.assertIn("new_price", result)
        self.assertTrue(result["alert_triggered"])
        self.assertEqual(result["alert_details"]["mode"], "SIMULATED")

        # Verify history was recorded
        history = self.service.get_price_history(pid)
        self.assertGreaterEqual(len(history), 2)  # Initial + check

    def test_best_time_to_buy_analytics(self):
        """Verify analytical advice engine detects price drops and gives actionable advice."""
        self.service.add_product("https://example.com/p2", "Mechanical Keyboard", 5000.0)
        products = self.service.get_all_products()
        pid = products[0]["id"]

        # Insert manual price history to simulate price movement
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO price_history (product_id, price) VALUES (?, ?)", (pid, 2500.0))
            cursor.execute("INSERT INTO price_history (product_id, price) VALUES (?, ?)", (pid, 2100.0))
            cursor.execute("INSERT INTO price_history (product_id, price) VALUES (?, ?)", (pid, 1850.0))
            conn.commit()

        analysis = self.service.analyze_best_time_to_buy(pid)
        self.assertIn(analysis["verdict"], ["BUY NOW", "WAIT FOR DROP", "FAIR VALUE"])
        self.assertEqual(analysis["min_price"], 1850.0)
        self.assertGreater(analysis["avg_price"], 0)

    def test_csv_export(self):
        """Verify CSV export writes formatted price history records."""
        self.service.add_product("https://example.com/p3", "Gaming Mouse", 3000.0)
        temp_export_dir = tempfile.mkdtemp()
        export_file = os.path.join(temp_export_dir, "test_export.csv")

        ok, path = self.service.export_csv(filepath=export_file)
        self.assertTrue(ok)
        self.assertTrue(os.path.exists(export_file))

        with open(export_file, "r", encoding="utf-8") as f:
            content = f.read()
            self.assertIn("Gaming Mouse", content)
            self.assertIn("price", content)

        try:
            os.remove(export_file)
            os.rmdir(temp_export_dir)
        except OSError:
            pass

if __name__ == "__main__":
    unittest.main()
