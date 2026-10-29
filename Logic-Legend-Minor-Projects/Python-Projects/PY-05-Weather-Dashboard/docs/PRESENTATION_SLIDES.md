# PRESENTATION SLIDES OUTLINE
## PY-05: Weather Dashboard and Forecast App
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: Weather Dashboard and Forecast App (PY-05)
- **Domain**: Web Engineering, Meteorological Data Visualization & APIs
- **Technologies**: Python, Streamlit, Plotly, Requests, Pandas
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Weather volatility directly impacts agriculture, transport, and energy systems.
- Existing commercial portals feature cluttered interfaces and heavy advertisements.
- Cloud API quotas frequently break academic student demonstrations when keys expire.

---

### Slide 3: Objectives & Solution Highlights
- Rapid, interactive meteorological web dashboard built with Streamlit.
- Integration of OpenWeather REST APIs with robust timeout handling.
- **Standout Feature**: Seamless offline simulation mode for 10+ major world/Indian cities with provenance transparency.
- Interactive Plotly 5-day temperature trajectory charts.
- Configurable rain alert threshold warnings and map projections.

---

### Slide 4: System Architecture & Workflow
- **Frontend Layer**: Streamlit reactive dashboard with Plotly charts and sidebar controls.
- **Service Layer**: `WeatherClient` performing HTTP queries and mathematical conversions.
- **Persistence Layer**: JSON storage managing user favourite cities.
- **Data Source Engine**: Dual routing between live OpenWeather endpoints and offline mock datasets.

---

### Slide 5: Data Provenance & Ethical Engineering
- The system never fabricates live API connectivity:
  - If connected to live endpoints: Displays **"Connected to Live API"** (Green).
  - If running in offline fallback mode: Displays **"Offline Demo Mode"** (Yellow).
- Ensures integrity during college evaluations and lab demonstrations.

---

### Slide 6: Interactive Plotly Visualizations
- **Temperature Trajectory**: Multi-trace line chart plotting maximum and minimum temperature projections across 5 days.
- **Humidity & Wind Analytics**: Color-scaled bar chart showing atmospheric moisture.
- **Geographic Satellite Map**: Plotting exact latitude and longitude coordinates.

---

### Slide 7: Configurable Rain Alert Sensitivity
- Dynamic threshold slider ($20\%$ to $90\%$ sensitivity):
  - Ingests current rain probability and forecast precipitation risk.
  - Triggers visual warning banners if rainfall risk surpasses user tolerance.

---

### Slide 8: Demonstration Highlights
- Searching global cities (New Delhi, Mumbai, London, Tokyo).
- Instant toggle between Celsius (°C) and Fahrenheit (°F).
- Adding and deleting favourite cities with persistent storage.
- Interacting with Plotly tooltips, pan, and zoom controls.

---

### Slide 9: Testing & Quality Assurance
- Automated test suite validating:
  - Exact mathematical formulas for Celsius and Fahrenheit.
  - Rejection of invalid and empty inputs.
  - Schema integrity of the offline mock database.
  - Precipitation alert boundary checks.

---

### Slide 10: Limitations & Future Enhancements
- **Limitations**: Free API tier provides 3-hour forecast intervals rather than minute-by-minute radar.
- **Future Scope**:
  - Detailed Air Quality Index (AQI) with PM2.5 and PM10 particulate levels.
  - Historical weather comparisons over 10-year archival records.

---

### Slide 11: Conclusion & Q&A
- Delivered a functional, resilient, and beautifully visualized weather application.
- Open for evaluator questions and demonstration.
