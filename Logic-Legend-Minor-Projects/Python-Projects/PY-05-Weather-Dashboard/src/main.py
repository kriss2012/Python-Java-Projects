"""
Logic Legend - PY-05 Weather Dashboard and Forecast App
CLI Entrypoint and Runner
"""

import sys
import os
import argparse
import subprocess

# Ensure current dir is in Python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

from weather_client import WeatherClient
from favorites_manager import FavoritesManager

def run_cli_summary(city="New Delhi", unit="metric"):
    client = WeatherClient()
    data = client.fetch_weather(city, unit=unit)
    unit_symbol = "°C" if unit == "metric" else "°F"

    print("=" * 60)
    print("      LOGIC LEGEND PY-05 WEATHER DASHBOARD - CLI")
    print("=" * 60)
    if "error" in data:
        print(f"Error: {data['error']}")
        return

    curr = data["current"]
    print(f"Location    : {data['city']}, {data['country']}")
    print(f"Coordinates : Lat {data['lat']}, Lon {data['lon']}")
    print(f"Data Source : {data['source']}")
    print(f"Timestamp   : {data['timestamp']}")
    print("-" * 60)
    print(f"Temperature : {curr['temp']} {unit_symbol} (Feels like {curr['feels_like']} {unit_symbol})")
    print(f"Condition   : {curr['desc']}")
    print(f"Humidity    : {curr['humidity']} %")
    print(f"Wind Speed  : {curr['wind_speed']} m/s")
    print(f"Pressure    : {curr['pressure']} hPa")
    print("-" * 60)
    print("5-Day Outlook:")
    for f in data["forecast"]:
        print(f"  {f['day']:<10} | {f['temp_max']:>4.1f}{unit_symbol} / {f['temp_min']:>4.1f}{unit_symbol} | {f['desc']:<20} | Rain: {f['rain']}%")
    print("=" * 60)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-05 Weather Dashboard")
    parser.add_argument("--cli", action="store_true", help="Run in command-line summary mode")
    parser.add_argument("--city", type=str, default="New Delhi", help="City name for query")
    parser.add_argument("--unit", type=str, choices=["metric", "imperial"], default="metric")
    args = parser.parse_args()

    if args.cli:
        run_cli_summary(city=args.city, unit=args.unit)
        return

    # Launch Streamlit app
    app_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app.py")
    subprocess.run(["streamlit", "run", app_path])

if __name__ == "__main__":
    main()

