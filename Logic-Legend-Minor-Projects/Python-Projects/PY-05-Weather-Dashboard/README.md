# PY-05: Weather Dashboard and Forecast App
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-05`
- **Project Title**: Weather Dashboard and Forecast App
- **Domain**: Web Application, Meteorological Analytics & API Engineering
- **Platform**: Web Dashboard (Python Streamlit & Plotly)

---

## 2. Project Overview
The Weather Dashboard and Forecast App is an interactive data visualization platform engineered to present real-time meteorological conditions, atmospheric indices, 5-day predictive forecasts, and precipitation risk alerts for worldwide cities. It seamlessly bridges live REST API integrations with a robust offline simulation mode.

---

## 3. Problem Statement
Weather impacts daily life, agriculture, logistics, and emergency management. Many generic apps suffer from cluttered user interfaces, lack interactive trend analytics, and fail completely when internet access is disrupted or third-party API credentials expire.

---

## 4. Objectives
- Provide an intuitive, modern web dashboard for global weather search and analytics.
- Render interactive Plotly charts showing 5-day temperature trajectories and humidity trends.
- Offer dynamic unit conversion between Celsius (°C) and Fahrenheit (°F).
- Provide a persistent favourite cities manager.
- Implement an ethical offline demonstration mode for 10+ major world cities without fabricating live connections.
- Compute configurable precipitation risk warnings and render interactive map projections.

---

## 5. Key Features
- **Live & Offline Dual Mode**: Connects to OpenWeather REST APIs or falls back to realistic offline mock datasets.
- **5-Day Interactive Trend Visuals**: Plotly charts for temperature ranges and humidity bar charts.
- **Configurable Rain Alerts**: Threshold slider alerting users when rain likelihood exceeds sensitivity limits.
- **Favourites Management**: One-click city switching with local JSON persistence.
- **Geographic Map Projection**: Displays city coordinates on interactive satellite maps.

---

## 6. Technology Stack
- **Language**: Python 3.10+
- **Dashboard Framework**: Streamlit
- **Visualizations**: Plotly Express & Plotly Graph Objects
- **Networking**: Requests
- **Data Manipulation**: Pandas
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 100 MB free space.
- **Web Browser**: Chrome, Firefox, Edge, or Safari.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-05-Weather-Dashboard

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Database & State Setup
User favourite cities are persisted in `data/favorites.json`. The file is initialized automatically upon first launch.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
OPENWEATHER_API_KEY=your_openweather_api_key_here
DEFAULT_CITY=New Delhi
TEMPERATURE_UNIT=metric
```
*(Leave `OPENWEATHER_API_KEY` empty to run immediately in Offline Demo Mode).*

---

## 11. Commands to Run the Application
```powershell
# Launch Streamlit Interactive Web Dashboard
streamlit run src/app.py

# Run in Headless CLI Summary Mode
python src/main.py --cli --city "New Delhi"
```
*Access the web dashboard in your browser at `http://localhost:8501`.*

---

## 12. Demo Credentials
No login required. Publicly accessible application.

---

## 13. Screenshots and Interface Description
1. **Header & Status Banner**: Displays search query, timestamp, and source indicator (`Connected to Live API` vs `Offline Demo Mode`).
2. **Current Weather Metrics**: 5 KPI cards for Temperature, Condition, Humidity, Wind Speed, and Atmospheric Pressure.
3. **5-Day Temperature Trajectory**: Plotly interactive line chart displaying Max and Min temperature bounds.
4. **Humidity & Wind Speed Chart**: Color-scaled bar chart representing atmospheric moisture.
5. **Geographic Map**: Pinpointing city coordinates via `st.map`.

---

## 14. API Documentation
Key methods in `WeatherClient`:
- `fetch_weather(city_name, unit='metric', force_demo=False)`
- `celsius_to_fahrenheit(c)`
- `fahrenheit_to_celsius(f)`
- `check_rain_alert(weather_data, threshold_prob=50)`

---

## 15. Test Instructions
```powershell
python tests/test_weather.py
```
*All 6 unit tests verify mathematical conversion formulas, empty city rejection, mock data schema integrity, Fahrenheit conversion, rain alert triggers, and favorite cities persistence.*

---

## 16. Known Limitations
- The free OpenWeather tier updates data on a 3-hour cycle; rapid sub-minute fluctuations require premium radar access.

---

## 17. Future Enhancements
- Air Quality Index (AQI) pollutant breakdown (PM2.5, PM10, NO2).
- Historical climate comparison across 10-year datasets.

---

## 18. Troubleshooting
- **Streamlit Port Conflict**: Run `streamlit run src/app.py --server.port 8502`.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines for academic viva evaluations.

