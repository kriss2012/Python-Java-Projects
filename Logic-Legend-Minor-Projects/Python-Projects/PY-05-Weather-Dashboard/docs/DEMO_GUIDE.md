# EVALUATOR DEMONSTRATION GUIDE
## PY-05: Weather Dashboard and Forecast App
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-05-Weather-Dashboard
streamlit run src/app.py
```
*Streamlit will automatically launch the dashboard in your default browser at `http://localhost:8501`.*

---

## 2. Recommended Demonstration Flow

### Step 1: Examine the Default City & KPI Cards
- The dashboard opens defaulted to **New Delhi**.
- Point out the status pill in the top right:
  - If no API key is entered: Notice the **"Offline Demo Mode"** badge.
  - Explain that this demonstrates ethical data handling—never faking a live server connection.
- Highlight the **5 Metric Cards**:
  - **Temperature**: e.g., `28.4 °C` (Feels like `29.8 °C`).
  - **Condition**: `Haze & Sunny`.
  - **Humidity**: `55 %`.
  - **Wind Speed**: `3.6 m/s`.
  - **Atmospheric Pressure**: `1012 hPa`.

### Step 2: Test Temperature Unit Toggle (Celsius vs Fahrenheit)
- In the left sidebar, click the **Fahrenheit (°F)** radio option.
- Point out:
  - The entire dashboard immediately re-renders dynamically.
  - New Delhi temperature updates to `83.1 °F` (Feels like `85.6 °F`).
  - The Plotly chart y-axis dynamically adapts to `Temperature (°F)`.

### Step 3: Explore Interactive Plotly Visualizations
- Scroll down to the **5-Day Meteorological Trends** section:
  1. Click the **📈 Temperature Forecast** tab:
     - Hover over data points to demonstrate Plotly's interactive tooltip showing Max and Min temperature bounds.
  2. Click the **💨 Humidity & Wind Speed** tab:
     - Show the blue-gradient bar chart showing moisture levels across the 5-day horizon.
  3. Click the **🗺️ Geographic Map** tab:
     - Show the interactive map pinned directly to New Delhi's coordinates (Lat `28.61`, Lon `77.21`).

### Step 4: Demonstrate Rain Alert Warning Sensitivity
- In the left sidebar, locate **Rain Alert Sensitivity (%)**.
- Change the city to **Mumbai** (or click `📍 Mumbai` in the favourites list).
- Notice Mumbai's forecast contains days with 70% and 80% rain likelihood.
- Observe the prominent yellow alert banner:
  `🌧️ Precipitation Warning: Forecast Alert on Day 3: 80% rain chance (Moderate Rain).`
- Slide the sensitivity up to 85%: Notice the alert dismisses itself because the threshold is no longer exceeded!

### Step 5: Test Favourites Management
- In the sidebar under **Add to favorites**, type: `Paris`.
- Click **➕ Save Favorite**.
- Notice `📍 Paris` immediately appears in the favorites list.
- Click `📍 Paris` to instantly view Paris weather.
- Click the `✖` next to Paris to demonstrate persistent deletion.

---

## 3. Headless CLI Verification
To verify the application without a browser:
```powershell
python src/main.py --cli --city "Mumbai"
```
*Outputs clean formatted text terminal report.*

## 4. Automated Unit Testing
Run the unit test suite:
```powershell
python tests/test_weather.py
```
*All 6 unit tests pass, verifying exact formulas, mock data integrity, unit conversions, and favorites persistence.*

