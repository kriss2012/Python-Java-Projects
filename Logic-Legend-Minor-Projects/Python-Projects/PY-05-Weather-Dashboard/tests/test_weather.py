"""
Logic Legend - PY-05 Weather Dashboard and Forecast App
Automated Unit and Meteorological Conversion Test Suite
"""

import os
import sys
import tempfile
import unittest

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from weather_client import WeatherClient
from favorites_manager import FavoritesManager

class TestWeatherDashboard(unittest.TestCase):
    def setUp(self):
        self.client = WeatherClient()
        self.temp_fav_fd, self.temp_fav_path = tempfile.mkstemp(suffix=".json")
        os.close(self.temp_fav_fd)
        self.fav_mgr = FavoritesManager(filepath=self.temp_fav_path)

    def tearDown(self):
        if os.path.exists(self.temp_fav_path):
            os.remove(self.temp_fav_path)

    def test_01_temperature_conversions(self):
        """Test exact mathematical formulas for C and F."""
        c = 25.0
        f = self.client.celsius_to_fahrenheit(c)
        self.assertEqual(f, 77.0)

        c_back = self.client.fahrenheit_to_celsius(f)
        self.assertEqual(c_back, 25.0)

        self.assertEqual(self.client.celsius_to_fahrenheit(0), 32.0)
        self.assertEqual(self.client.celsius_to_fahrenheit(100), 212.0)

    def test_02_empty_city_validation(self):
        """Test rejection of empty or blank city input."""
        res = self.client.fetch_weather("   ")
        self.assertIn("error", res)

    def test_03_offline_mock_data_structure(self):
        """Test offline demo dataset integrity for major cities."""
        data = self.client.fetch_weather("Mumbai", unit="metric", force_demo=True)
        self.assertNotIn("error", data)
        self.assertEqual(data["city"], "Mumbai")
        self.assertTrue(data["is_mock"])
        self.assertIn("current", data)
        self.assertIn("forecast", data)
        self.assertEqual(len(data["forecast"]), 5)

        curr = data["current"]
        self.assertIn("temp", curr)
        self.assertIn("humidity", curr)
        self.assertIn("wind_speed", curr)
        self.assertIn("pressure", curr)

    def test_04_unit_conversion_in_fetch(self):
        """Test that requesting imperial units converts metrics to Fahrenheit."""
        data_c = self.client.fetch_weather("Tokyo", unit="metric", force_demo=True)
        data_f = self.client.fetch_weather("Tokyo", unit="imperial", force_demo=True)

        temp_c = data_c["current"]["temp"]
        temp_f = data_f["current"]["temp"]
        self.assertEqual(temp_f, self.client.celsius_to_fahrenheit(temp_c))

    def test_05_rain_alert_threshold(self):
        """Test rain warning trigger when precipitation exceeds threshold."""
        mock_data = {
            "current": {"rain_prob": 70},
            "forecast": [{"day": "Tomorrow", "rain": 85, "desc": "Heavy Downpour"}]
        }
        alerts = self.client.check_rain_alert(mock_data, threshold_prob=50)
        self.assertGreaterEqual(len(alerts), 2)
        self.assertTrue(any("Immediate" in a for a in alerts))
        self.assertTrue(any("Tomorrow" in a for a in alerts))

    def test_06_favorites_manager(self):
        """Test adding, persisting, and removing favorite cities."""
        # Add new city
        ok, msg = self.fav_mgr.add_favorite("Singapore")
        self.assertTrue(ok)
        self.assertIn("Singapore", self.fav_mgr.get_favorites())

        # Duplicate add should be rejected
        ok_dup, _ = self.fav_mgr.add_favorite("Singapore")
        self.assertFalse(ok_dup)

        # Remove city
        ok_del, _ = self.fav_mgr.remove_favorite("Singapore")
        self.assertTrue(ok_del)
        self.assertNotIn("Singapore", self.fav_mgr.get_favorites())

if __name__ == "__main__":
    unittest.main()

