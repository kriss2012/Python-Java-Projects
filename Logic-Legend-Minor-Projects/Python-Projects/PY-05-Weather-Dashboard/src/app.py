"""
Logic Legend - PY-05 Weather Dashboard and Forecast App
Interactive Streamlit & Plotly Dashboard
"""

import os
import sys
import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go

# Ensure src in path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from weather_client import WeatherClient
from favorites_manager import FavoritesManager

st.set_page_config(
    page_title="Logic Legend — Weather Dashboard & Forecast",
    page_icon="⛅",
    layout="wide"
)

# Initialize Services
base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
fav_path = os.path.join(base_dir, "data", "favorites.json")
fav_manager = FavoritesManager(filepath=fav_path)

# Custom CSS
st.markdown("""
<style>
    .metric-card {
        background-color: #FFFFFF;
        border: 1px solid #E2E8F0;
        border-radius: 8px;
        padding: 16px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.04);
        text-align: center;
    }
    .badge-live {
        background-color: #DCFCE7;
        color: #166534;
        padding: 4px 10px;
        border-radius: 12px;
        font-weight: bold;
        font-size: 12px;
    }
    .badge-mock {
        background-color: #FEF3C7;
        color: #92400E;
        padding: 4px 10px;
        border-radius: 12px;
        font-weight: bold;
        font-size: 12px;
    }
</style>
""", unsafe_allow_html=True)

# ----------------- SIDEBAR -----------------
st.sidebar.title("⚙️ Dashboard Controls")

# API Configuration
api_key_input = st.sidebar.text_input(
    "OpenWeather API Key (optional):",
    value=os.environ.get("OPENWEATHER_API_KEY", ""),
    type="password",
    help="Leave empty to use realistic offline simulation mode."
)

force_demo = st.sidebar.checkbox("Force Offline Demo Mode", value=not bool(api_key_input))

# Unit Selection
unit_choice = st.sidebar.radio("Temperature Unit:", ["Celsius (°C)", "Fahrenheit (°F)"], index=0)
unit = "metric" if "Celsius" in unit_choice else "imperial"
unit_symbol = "°C" if unit == "metric" else "°F"

# Rain Alert Threshold
rain_thresh = st.sidebar.slider("Rain Alert Sensitivity (%):", min_value=20, max_value=90, value=50, step=5)

# Favorites Section
st.sidebar.markdown("---")
st.sidebar.subheader("⭐ Favourite Cities")
favorites = fav_manager.get_favorites()

if "selected_city" not in st.session_state:
    st.session_state["selected_city"] = "New Delhi"

for fav in favorites:
    col1, col2 = st.sidebar.columns([4, 1])
    if col1.button(f"📍 {fav}", key=f"fav_{fav}"):
        st.session_state["selected_city"] = fav
    if col2.button("✖", key=f"del_{fav}", help=f"Remove {fav}"):
        fav_manager.remove_favorite(fav)
        st.rerun()

new_fav = st.sidebar.text_input("Add to favorites:", placeholder="e.g. Paris")
if st.sidebar.button("➕ Save Favorite"):
    if new_fav:
        ok, msg = fav_manager.add_favorite(new_fav)
        if ok:
            st.sidebar.success(msg)
            st.rerun()
        else:
            st.sidebar.warning(msg)

# ----------------- MAIN VIEW -----------------
st.title("⛅ Logic Legend Weather Dashboard")
st.caption("Real-Time Meteorological Analysis, 5-Day Forecasts & Atmospheric Analytics")

# Top Search Bar
search_col, btn_col = st.columns([5, 1])
city_query = search_col.text_input("Search Global City:", value=st.session_state["selected_city"])
if btn_col.button("🔍 Search Weather", use_container_width=True):
    st.session_state["selected_city"] = city_query

active_city = st.session_state["selected_city"]
client = WeatherClient(api_key=api_key_input)
data = client.fetch_weather(active_city, unit=unit, force_demo=force_demo)

if "error" in data:
    st.error(f"❌ {data['error']}")
else:
    # Status Header
    stat_col1, stat_col2 = st.columns([3, 1])
    with stat_col1:
        st.subheader(f"📍 {data['city']}, {data['country']}")
        st.caption(f"Last updated: {data['timestamp']}")
    with stat_col2:
        if data["is_mock"]:
            st.markdown('<span class="badge-mock">Offline Demo Mode</span>', unsafe_allow_html=True)
        else:
            st.markdown('<span class="badge-live">Connected to Live API</span>', unsafe_allow_html=True)

    # Rain & Severe Weather Alerts
    alerts = client.check_rain_alert(data, threshold_prob=rain_thresh)
    if alerts:
        for alert_msg in alerts:
            st.warning(f"🌧️ **Precipitation Warning**: {alert_msg}")

    # Current Weather KPI Cards
    curr = data["current"]
    kpi1, kpi2, kpi3, kpi4, kpi5 = st.columns(5)
    kpi1.metric("Temperature", f"{curr['temp']} {unit_symbol}", f"Feels like {curr['feels_like']} {unit_symbol}")
    kpi2.metric("Condition", curr["desc"])
    kpi3.metric("Humidity", f"{curr['humidity']} %")
    kpi4.metric("Wind Speed", f"{curr['wind_speed']} m/s")
    kpi5.metric("Atmospheric Pressure", f"{curr['pressure']} hPa")

    # ----------------- CHARTS & ANALYTICS -----------------
    st.markdown("---")
    st.subheader("📊 5-Day Meteorological Trends & Visualizations")

    fc_list = data["forecast"]
    if fc_list:
        df_fc = pd.DataFrame(fc_list)

        tab_temp, tab_humidity, tab_map = st.tabs(["📈 Temperature Forecast", "💨 Humidity & Wind Speed", "🗺️ Geographic Map"])

        with tab_temp:
            fig_temp = go.Figure()
            fig_temp.add_trace(go.Scatter(
                x=df_fc["day"], y=df_fc["temp_max"],
                name="Max Temp", line=dict(color="#EF4444", width=3)
            ))
            fig_temp.add_trace(go.Scatter(
                x=df_fc["day"], y=df_fc["temp_min"],
                name="Min Temp", line=dict(color="#3B82F6", width=2, dash="dash")
            ))
            fig_temp.update_layout(
                title=f"5-Day Temperature Trajectory ({unit_symbol})",
                xaxis_title="Forecast Day",
                yaxis_title=f"Temperature ({unit_symbol})",
                template="plotly_white",
                height=380
            )
            st.plotly_chart(fig_temp, use_container_width=True)

        with tab_humidity:
            fig_bar = px.bar(
                df_fc, x="day", y="humidity",
                title="Relative Humidity Trajectory (%)",
                color="humidity", color_continuous_scale="Blues",
                labels={"humidity": "Humidity (%)", "day": "Forecast Day"},
                height=380
            )
            st.plotly_chart(fig_bar, use_container_width=True)

        with tab_map:
            st.write(f"Geographic Coordinates: Latitude **{data['lat']}**, Longitude **{data['lon']}**")
            map_df = pd.DataFrame([{"lat": data["lat"], "lon": data["lon"]}])
            st.map(map_df, zoom=9)

        # 5-Day Cards
        st.markdown("### 🗓️ 5-Day Weather Outlook")
        cols = st.columns(len(fc_list))
        for idx, day in enumerate(fc_list):
            with cols[idx]:
                st.markdown(f"**{day['day']}**")
                st.write(f"🌡️ {day['temp_max']}{unit_symbol} / {day['temp_min']}{unit_symbol}")
                st.caption(f"{day['desc']}")
                st.write(f"💧 {day['rain']}% rain")
