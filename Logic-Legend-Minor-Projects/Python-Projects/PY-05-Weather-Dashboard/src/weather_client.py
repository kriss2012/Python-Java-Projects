"""
Logic Legend - PY-05 Weather Dashboard and Forecast App
Weather API Client & Realistic Offline Simulation Engine
"""

import os
import requests
from datetime import datetime, timedelta

MOCK_CITIES = {
    "New Delhi": {
        "lat": 28.6139, "lon": 77.2090, "country": "IN",
        "current": {"temp": 28.4, "feels_like": 29.8, "humidity": 55, "wind_speed": 3.6, "pressure": 1012, "desc": "Haze & Sunny", "icon": "01d", "rain_prob": 10},
        "forecast": [
            {"day": "Day 1", "temp": 29.0, "temp_min": 21.0, "temp_max": 31.0, "humidity": 52, "wind": 4.1, "desc": "Clear Sky", "icon": "01d", "rain": 5},
            {"day": "Day 2", "temp": 30.5, "temp_min": 22.5, "temp_max": 32.5, "humidity": 48, "wind": 3.8, "desc": "Warm & Sunny", "icon": "01d", "rain": 0},
            {"day": "Day 3", "temp": 27.2, "temp_min": 20.0, "temp_max": 29.0, "humidity": 68, "wind": 5.2, "desc": "Scattered Clouds", "icon": "03d", "rain": 25},
            {"day": "Day 4", "temp": 25.0, "temp_min": 19.0, "temp_max": 26.5, "humidity": 75, "wind": 6.0, "desc": "Light Rain Showers", "icon": "10d", "rain": 65},
            {"day": "Day 5", "temp": 26.8, "temp_min": 19.5, "temp_max": 28.0, "humidity": 60, "wind": 4.5, "desc": "Partly Cloudy", "icon": "02d", "rain": 15}
        ]
    },
    "Mumbai": {
        "lat": 19.0760, "lon": 72.8777, "country": "IN",
        "current": {"temp": 31.2, "feels_like": 35.6, "humidity": 78, "wind_speed": 4.8, "pressure": 1009, "desc": "Humid & Partly Cloudy", "icon": "02d", "rain_prob": 40},
        "forecast": [
            {"day": "Day 1", "temp": 31.5, "temp_min": 26.0, "temp_max": 33.0, "humidity": 76, "wind": 5.0, "desc": "Partly Cloudy", "icon": "02d", "rain": 30},
            {"day": "Day 2", "temp": 30.0, "temp_min": 25.5, "temp_max": 31.5, "humidity": 82, "wind": 6.2, "desc": "Passing Coastal Showers", "icon": "10d", "rain": 70},
            {"day": "Day 3", "temp": 29.5, "temp_min": 25.0, "temp_max": 31.0, "humidity": 80, "wind": 5.5, "desc": "Moderate Rain", "icon": "10d", "rain": 80},
            {"day": "Day 4", "temp": 31.0, "temp_min": 26.0, "temp_max": 32.5, "humidity": 74, "wind": 4.2, "desc": "Breezy & Sunny", "icon": "01d", "rain": 20},
            {"day": "Day 5", "temp": 31.8, "temp_min": 26.5, "temp_max": 33.0, "humidity": 72, "wind": 4.6, "desc": "Sunny", "icon": "01d", "rain": 10}
        ]
    },
    "Bengaluru": {
        "lat": 12.9716, "lon": 77.5946, "country": "IN",
        "current": {"temp": 24.5, "feels_like": 24.8, "humidity": 62, "wind_speed": 4.0, "pressure": 1014, "desc": "Pleasant & Breezy", "icon": "02d", "rain_prob": 20},
        "forecast": [
            {"day": "Day 1", "temp": 25.0, "temp_min": 18.0, "temp_max": 27.0, "humidity": 60, "wind": 4.2, "desc": "Pleasant Breeze", "icon": "02d", "rain": 15},
            {"day": "Day 2", "temp": 24.2, "temp_min": 18.5, "temp_max": 26.0, "humidity": 65, "wind": 4.5, "desc": "Afternoon Clouds", "icon": "03d", "rain": 35},
            {"day": "Day 3", "temp": 23.5, "temp_min": 17.5, "temp_max": 25.0, "humidity": 72, "wind": 5.0, "desc": "Evening Thundershower", "icon": "11d", "rain": 75},
            {"day": "Day 4", "temp": 24.0, "temp_min": 18.0, "temp_max": 26.0, "humidity": 68, "wind": 4.0, "desc": "Partly Cloudy", "icon": "02d", "rain": 25},
            {"day": "Day 5", "temp": 25.5, "temp_min": 18.5, "temp_max": 27.5, "humidity": 58, "wind": 3.8, "desc": "Sunny & Clear", "icon": "01d", "rain": 5}
        ]
    },
    "London": {
        "lat": 51.5074, "lon": -0.1278, "country": "GB",
        "current": {"temp": 14.2, "feels_like": 13.5, "humidity": 72, "wind_speed": 5.4, "pressure": 1018, "desc": "Overcast with Drizzle", "icon": "09d", "rain_prob": 60},
        "forecast": [
            {"day": "Day 1", "temp": 14.5, "temp_min": 9.0, "temp_max": 16.0, "humidity": 70, "wind": 5.0, "desc": "Light Drizzle", "icon": "09d", "rain": 55},
            {"day": "Day 2", "temp": 13.8, "temp_min": 8.5, "temp_max": 15.0, "humidity": 75, "wind": 6.1, "desc": "Cloudy & Cool", "icon": "04d", "rain": 40},
            {"day": "Day 3", "temp": 15.0, "temp_min": 9.5, "temp_max": 17.0, "humidity": 65, "wind": 4.2, "desc": "Sunny Intervals", "icon": "02d", "rain": 20},
            {"day": "Day 4", "temp": 12.5, "temp_min": 7.0, "temp_max": 14.0, "humidity": 80, "wind": 7.0, "desc": "Gusty Winds & Rain", "icon": "10d", "rain": 85},
            {"day": "Day 5", "temp": 13.0, "temp_min": 7.5, "temp_max": 14.5, "humidity": 70, "wind": 5.2, "desc": "Scattered Clouds", "icon": "03d", "rain": 30}
        ]
    },
    "Tokyo": {
        "lat": 35.6762, "lon": 139.6503, "country": "JP",
        "current": {"temp": 19.0, "feels_like": 18.8, "humidity": 58, "wind_speed": 3.5, "pressure": 1020, "desc": "Clear & Sunny", "icon": "01d", "rain_prob": 10},
        "forecast": [
            {"day": "Day 1", "temp": 20.0, "temp_min": 13.0, "temp_max": 22.0, "humidity": 55, "wind": 3.8, "desc": "Clear Skies", "icon": "01d", "rain": 5},
            {"day": "Day 2", "temp": 18.5, "temp_min": 12.5, "temp_max": 20.5, "humidity": 62, "wind": 4.0, "desc": "Mild & Pleasant", "icon": "02d", "rain": 15},
            {"day": "Day 3", "temp": 17.0, "temp_min": 11.0, "temp_max": 18.5, "humidity": 70, "wind": 4.8, "desc": "Cloudy", "icon": "04d", "rain": 45},
            {"day": "Day 4", "temp": 16.5, "temp_min": 11.5, "temp_max": 18.0, "humidity": 75, "wind": 5.5, "desc": "Light Rain", "icon": "10d", "rain": 70},
            {"day": "Day 5", "temp": 19.5, "temp_min": 12.0, "temp_max": 21.0, "humidity": 58, "wind": 3.6, "desc": "Sunny Return", "icon": "01d", "rain": 10}
        ]
    }
}

class WeatherClient:
    def __init__(self, api_key=None):
        self.api_key = api_key or os.environ.get("OPENWEATHER_API_KEY", "").strip()

    @staticmethod
    def celsius_to_fahrenheit(c):
        return round((c * 9/5) + 32, 1)

    @staticmethod
    def fahrenheit_to_celsius(f):
        return round((f - 32) * 5/9, 1)

    def is_live_configured(self):
        return bool(self.api_key)

    def fetch_weather(self, city_name, unit="metric", force_demo=False):
        city = city_name.strip().title()
        if not city:
            return {"error": "City name cannot be empty."}

        # Use Live OpenWeather API if key available and not forcing demo
        if self.is_live_configured() and not force_demo:
            try:
                url_curr = f"https://api.openweathermap.org/data/2.5/weather?q={city}&appid={self.api_key}&units={unit}"
                resp_curr = requests.get(url_curr, timeout=5)

                if resp_curr.status_code == 200:
                    d = resp_curr.json()
                    # Also fetch 5-day forecast
                    url_fc = f"https://api.openweathermap.org/data/2.5/forecast?q={city}&appid={self.api_key}&units={unit}"
                    resp_fc = requests.get(url_fc, timeout=5)
                    forecast_list = []
                    if resp_fc.status_code == 200:
                        fc_data = resp_fc.json()
                        # Extract 1 forecast reading per day (e.g. at index 0, 8, 16, 24, 32)
                        for i in range(0, min(len(fc_data["list"]), 40), 8):
                            entry = fc_data["list"][i]
                            dt = datetime.fromtimestamp(entry["dt"]).strftime("%a, %d %b")
                            forecast_list.append({
                                "day": dt,
                                "temp": round(entry["main"]["temp"], 1),
                                "temp_min": round(entry["main"]["temp_min"], 1),
                                "temp_max": round(entry["main"]["temp_max"], 1),
                                "humidity": entry["main"]["humidity"],
                                "wind": round(entry["wind"]["speed"], 1),
                                "desc": entry["weather"][0]["description"].title(),
                                "icon": entry["weather"][0]["icon"],
                                "rain": int(entry.get("pop", 0) * 100)
                            })

                    return {
                        "city": d["name"],
                        "country": d["sys"].get("country", ""),
                        "lat": d["coord"]["lat"],
                        "lon": d["coord"]["lon"],
                        "current": {
                            "temp": round(d["main"]["temp"], 1),
                            "feels_like": round(d["main"]["feels_like"], 1),
                            "humidity": d["main"]["humidity"],
                            "wind_speed": round(d["wind"]["speed"], 1),
                            "pressure": d["main"]["pressure"],
                            "desc": d["weather"][0]["description"].title(),
                            "icon": d["weather"][0]["icon"],
                            "rain_prob": 30 if "rain" in d else 0
                        },
                        "forecast": forecast_list,
                        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        "unit": unit,
                        "source": "OpenWeather API (Live)",
                        "is_mock": False
                    }
                elif resp_curr.status_code == 404:
                    return {"error": f"City '{city}' not found in global meteorological directory."}
                elif resp_curr.status_code == 401:
                    # Fallback to mock with notice
                    pass
            except Exception:
                # Fallback to mock mode on network timeout/error
                pass

        # Return Offline Mock / Demo Mode Data
        if city in MOCK_CITIES:
            base_data = MOCK_CITIES[city]
        else:
            # Fallback to New Delhi if city not in predefined mock list
            base_data = MOCK_CITIES["New Delhi"]
            city = f"{city} (Demo Simulation)"

        # Handle units conversion for mock data
        curr = dict(base_data["current"])
        fc_list = [dict(f) for f in base_data["forecast"]]

        if unit == "imperial":
            curr["temp"] = self.celsius_to_fahrenheit(curr["temp"])
            curr["feels_like"] = self.celsius_to_fahrenheit(curr["feels_like"])
            for f in fc_list:
                f["temp"] = self.celsius_to_fahrenheit(f["temp"])
                f["temp_min"] = self.celsius_to_fahrenheit(f["temp_min"])
                f["temp_max"] = self.celsius_to_fahrenheit(f["temp_max"])

        return {
            "city": city,
            "country": base_data["country"],
            "lat": base_data["lat"],
            "lon": base_data["lon"],
            "current": curr,
            "forecast": fc_list,
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "unit": unit,
            "source": "Offline Simulation / Demo Dataset",
            "is_mock": True
        }

    @staticmethod
    def check_rain_alert(weather_data, threshold_prob=50):
        """Identifies rain risk from current condition and forecast."""
        curr_prob = weather_data.get("current", {}).get("rain_prob", 0)
        fc = weather_data.get("forecast", [])

        alerts = []
        if curr_prob >= threshold_prob:
            alerts.append(f"Immediate Rain Alert: {curr_prob}% precipitation probability reported currently.")

        for day in fc:
            if day.get("rain", 0) >= threshold_prob:
                alerts.append(f"Forecast Alert on {day['day']}: {day['rain']}% rain chance ({day['desc']}).")

        return alerts

