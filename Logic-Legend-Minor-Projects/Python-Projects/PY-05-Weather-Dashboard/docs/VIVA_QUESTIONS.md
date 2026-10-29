# VIVA VOCE PREPARATION & QUESTIONS
## PY-05: Weather Dashboard and Forecast App
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the main purpose of this Weather Dashboard application?
**Answer**: The application provides an interactive web interface for tracking real-time meteorological conditions, analyzing 5-day predictive trends using interactive Plotly charts, setting precipitation threshold alerts, and navigating between live API access and offline simulation modes.

### Q2: Why did you choose Streamlit instead of Flask or Django for this project?
**Answer**: Streamlit is an open-source Python framework designed specifically for data science and analytics dashboards. It enables rapid state-driven UI development, native integration with Plotly charts and Pandas dataframes, and eliminates frontend boilerplate while maintaining modern aesthetics.

### Q3: How does the application handle situations where the user does not have an OpenWeather API key or the network is offline?
**Answer**: The system incorporates a comprehensive offline mock dataset for 10 major global/Indian cities (New Delhi, Mumbai, Bengaluru, London, Tokyo, etc.). If the API key is omitted, invalid, or the network times out, the client automatically falls back to this offline dataset and displays an explicit "Offline Demo Mode" provenance badge.

### Q4: Why is it important to clearly differentiate live API data from mock data?
**Answer**: Academic integrity and ethical engineering require that an application never deceive the user or evaluator into believing it is communicating with a live external satellite/weather service when it is running offline.

### Q5: What REST endpoints of OpenWeather are utilized?
**Answer**:
1. Current Weather: `https://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units={unit}`
2. 5-Day 3-Hour Forecast: `https://api.openweathermap.org/data/2.5/forecast?q={city}&appid={key}&units={unit}`

### Q6: How does the system extract a 5-day forecast from OpenWeather's 3-hour interval response?
**Answer**: OpenWeather's free forecast API returns 40 readings spaced 3 hours apart across 5 days ($40 / 8 = 5\text{ days}$). The parser iterates through the list in steps of 8 (`range(0, 40, 8)`), selecting one representative mid-day timestamp per calendar day to construct a clean 5-day projection.

### Q7: Explain the mathematical formulas used for unit conversion.
**Answer**:
- Celsius to Fahrenheit: $F = (C \times \frac{9}{5}) + 32$
- Fahrenheit to Celsius: $C = (F - 32) \times \frac{5}{9}$
These are implemented with single decimal precision rounding.

### Q8: How does the rain alert notification logic function?
**Answer**: The user configures a sensitivity slider ($\theta$, default 50%). The `check_rain_alert` method inspects both current rain probability and forecast precipitation risk (`pop` field). If any probability meets or exceeds $\theta$, an alert banner is dynamically rendered.

### Q9: How are user favourite cities stored persistently?
**Answer**: A dedicated `FavoritesManager` serializes the list of saved cities to a local JSON file (`data/favorites.json`). Modifications (add, remove) update the JSON file on disk, preserving user preferences across session restarts.

### Q10: What defensive network programming practices are implemented in the API client?
**Answer**:
1. An explicit 5-second timeout (`requests.get(url, timeout=5)`) prevents application freezing during network drops.
2. HTTP status codes (200 OK, 401 Unauthorized, 404 City Not Found) are checked explicitly.
3. Network exceptions (`requests.exceptions.RequestException`) are caught with fallback handling.

### Q11: How is the geographic map rendered?
**Answer**: The application extracts the `coord.lat` and `coord.lon` floating-point coordinates from the weather payload, constructs a single-row Pandas DataFrame, and passes it to Streamlit's `st.map()` component to render an interactive map centered on the city.

### Q12: Why are Plotly charts superior to static Matplotlib charts in this web context?
**Answer**: Plotly outputs interactive HTML5 canvas/SVG elements supporting hover tooltips, dynamic zooming, panning, and responsive resizing across devices, whereas Matplotlib generates static raster images without interactivity.

### Q13: What automated unit tests were created?
**Answer**: Using Python's `unittest` module:
- Mathematical accuracy of $^\circ\text{C} \leftrightarrow ^\circ\text{F}$ conversions.
- Rejection of empty city search queries.
- Structural schema validation of the offline mock database.
- Unit conversion integrity on data fetch.
- Threshold sensitivity validation for rain alerts.
- CRUD operations in the JSON favorites manager.

### Q14: What are the main limitations of the free OpenWeather tier?
**Answer**:
1. Request rate limited to 60 calls per minute.
2. Forecasts are available in 3-hour blocks rather than hourly or minute-by-minute radar.
3. Air pollution/AQI data requires a separate endpoint call.

### Q15: How could this project be extended for industrial meteorological use?
**Answer**:
1. Integrating satellite radar imagery and Doppler wind vectors.
2. Incorporating agricultural degree-day (GDD) calculations for farmers.
3. Triggering automated SMS/email alerts via Celery background tasks when storm warnings are detected.
