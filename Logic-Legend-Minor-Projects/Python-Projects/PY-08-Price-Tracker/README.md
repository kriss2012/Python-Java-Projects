# Logic Legend — PY-08: Autonomous Price Tracker with Web Scraping

An automated e-commerce price monitoring, trend prediction, and alert dispatch platform developed in Python. The system monitors retail items, tracks historical price movements in SQLite, visualizes fluctuation trends, provides intelligent "Buy vs Wait" purchasing advice, and automatically dispatches email notifications (real SMTP or mock simulation) when items hit user-defined budget thresholds.

---

## 1. Project Title and Identifier
- **Project Name**: Autonomous Price Tracker with Web Scraping
- **Project Code**: `PY-08`
- **Application Category**: Web Scraping, Automation, Data Analytics & Desktop GUI
- **Target Audience**: Smart shoppers, college students, e-commerce researchers, and faculty evaluators.

---

## 2. Executive Summary
Online retail pricing fluctuates constantly based on dynamic algorithmic pricing models, supply variations, flash sales, and festival discounts. Manually refreshing multiple tabs wastes time and frequently leads to missing brief price drops. The **Autonomous Price Tracker** solves this problem by scheduling automated web requests using Python's `requests` and `BeautifulSoup4`, caching historical snapshots in an ACID-compliant SQLite database, analyzing price velocity to advise the best time to purchase, and notifying the user the moment a deal becomes active.

---

## 3. Key Features
- **Dynamic HTML Parsing**: Extracts titles, prices, ratings, and stock status using multi-pattern heuristic CSS selectors (`.a-price-whole`, `h1.product-title`, etc.) and regex body scans.
- **Robust Network Fallback**: Detects mock/demo domains and network timeouts, falling back to deterministic fixtures for reliable offline vivas and unit tests.
- **Automated Threshold Triggers**: Checks prices and dispatches immediate price-drop alerts if current price drops below configured target thresholds.
- **Dual-Mode Email Notifier**: Supports authenticated TLS/SSL SMTP (Gmail, Outlook, custom SMTP) as well as an offline simulation dispatcher that logs dispatched alerts to SQLite.
- **Intelligent Purchasing Advisory**: Calculates minimum, maximum, and moving average prices to issue clear verdicts: `BUY NOW`, `WAIT FOR DROP`, or `FAIR VALUE`.
- **Desktop Tkinter Interface**: Modern desktop GUI with real-time Treeview table, product detail pane, interactive price history chart, status toggles, and manual scrapers.
- **CSV Data Export**: One-click generation of formatted CSV files capturing time-series price snapshots for offline analysis in Excel or Python.
- **Zero-Crash Safety**: Sanitizes price formats (currency symbols, commas, non-numeric strings) without crashing on deformed HTML.

---

## 4. System Architecture
The application adheres to a modular four-layer architecture:
1. **Presentation Layer (`src/gui.py`, `src/main.py`)**: Tkinter graphical dashboard and dual-mode CLI for headless environments.
2. **Business Logic Layer (`src/tracker_service.py`)**: Manages product lifecycles, runs scheduled price inspections, and computes time-series statistics.
3. **Data Acquisition Layer (`src/scraper_engine.py`)**: HTTP client with custom User-Agent headers, CSS selector heuristics, and fallback mock generator.
4. **Data Persistence & Messaging Layer (`src/database.py`, `src/notifier.py`)**: SQLite relational tables (`products`, `price_history`, `alert_logs`, `error_logs`) and SMTP notification engine.

---

## 5. Technology Stack and Prerequisites
- **Language**: Python 3.10+
- **Database**: SQLite 3 (Standard Library)
- **Web Scraping**: `requests >= 2.31.0`, `beautifulsoup4 >= 4.12.0`
- **GUI Framework**: Tkinter / ttk (Python Standard Library)
- **Task Scheduling**: `schedule >= 1.2.0`
- **Communication Protocol**: SMTP / TLS (`smtplib`, `email.mime`)

---

## 6. Directory Structure
```
PY-08-Price-Tracker/
├── .env.example              # Template for SMTP server credentials
├── .gitignore                # Git ignore rules for virtual environments and databases
├── requirements.txt          # Python dependency specifications
├── README.md                 # 19-section comprehensive documentation
├── data/                     # SQLite database storage directory
├── exports/                  # Exported time-series CSV reports
├── docs/                     # Academic deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   ├── __init__.py           # Package marker
│   ├── database.py           # SQLite schema and query interface
│   ├── scraper_engine.py     # HTML parser and selector heuristics
│   ├── notifier.py           # SMTP & simulated notification dispatcher
│   ├── tracker_service.py    # Price evaluation and analytical advice logic
│   ├── gui.py                # Tkinter visual application
│   └── main.py               # Application entry point (GUI + CLI)
└── tests/
    ├── __init__.py           # Test package marker
    └── test_tracker.py       # Automated unit test suite
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Python-Projects/PY-08-Price-Tracker
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv venv
   # On Windows:
   venv\Scripts\activate
   # On Linux/macOS:
   source venv/bin/activate
   ```
3. Install required packages:
   ```bash
   python -m pip install -r requirements.txt
   ```
4. (Optional) Set up email notifications in `.env`:
   ```bash
   copy .env.example .env
   # Update SMTP_SERVER, SENDER_EMAIL, and SENDER_PASSWORD
   ```

---

## 8. Usage Instructions
### Running the Graphical Application
Launch the Tkinter dashboard:
```bash
python src/main.py
```

### Running in Headless / CLI Mode
Populate demo products, run a scraper cycle, and export CSV:
```bash
python src/main.py --seed --check-all --export --cli
```

Track a custom product from the command line:
```bash
python src/main.py --cli --add-url "https://example.com/products/headphones" --title "ANC Headphones" --target 4500.0
```

---

## 9. Database Schema and Storage Design
The SQLite database (`data/price_tracker.db`) uses four relational tables with referential integrity:
- **`products`**: `id`, `url` (UNIQUE), `title`, `target_price`, `current_price`, `currency`, `rating`, `status` (`ACTIVE`/`PAUSED`), `created_at`.
- **`price_history`**: `id`, `product_id` (FK -> products.id ON DELETE CASCADE), `price`, `timestamp`.
- **`alert_logs`**: `id`, `product_id` (FK), `price_at_alert`, `target_price`, `recipient`, `status` (`SENT`/`SIMULATED`), `sent_at`.
- **`error_logs`**: `id`, `product_id`, `url`, `error_message`, `logged_at`.

---

## 10. Scraping Engine & Heuristic Parsing
Scraping modern e-commerce websites requires accommodating varying DOM hierarchies:
- **Multi-Selector Fallback**: Tries standard container selectors (`#productTitle`, `h1.product-title`, `.a-price-whole`, etc.).
- **Regular Expression Scanning**: Scans raw HTML tokens matching currency formats (`₹\s*([\d,]+)`) when class names are obfuscated.
- **Sanitization**: Strips non-numeric characters, currency symbols, and grouping commas before converting to floating-point values.
- **Ethical Crawling**: Sends browser-like User-Agent headers, respects request timeouts (6 seconds), and provides built-in rate delays.

---

## 11. Trend Analysis & Purchasing Recommendation
The analytical advice engine queries historical data points for a product and calculates:
1. **Minimum Recorded Price ($P_{min}$)**
2. **Maximum Recorded Price ($P_{max}$)**
3. **Moving Arithmetic Mean ($P_{avg}$)**

Rules:
- If $P_{current} \le P_{min} \times 1.02$: **`BUY NOW`** (Within 2% of all-time low).
- If $P_{current} \ge P_{avg}$: **`WAIT FOR DROP`** (Above historic average).
- Else: **`FAIR VALUE`** (Below average, standard purchasing window).

---

## 12. Testing and Verification
Run the automated test suite:
```bash
python tests/test_tracker.py
```
Test results verify:
- ✅ String-to-float price parser sanitization
- ✅ Mock offline scraping engine deterministic responses
- ✅ Product validation and database constraints
- ✅ Price check cycle, database updates, and threshold email triggering
- ✅ Best time to buy decision matrix
- ✅ CSV export file format and integrity

---

## 13. Security and Ethical Web Scraping
- **Simulated Email Security**: Real credentials are never required for testing; simulation mode securely logs dispatch events without exposing SMTP passwords.
- **Robots.txt & Rate Limiting**: Discourages high-frequency crawling; default batch intervals are paced.
- **Parameterized SQL**: All database operations use SQLite parameter substitution (`?`) to prevent SQL injection.
- **Input Validation**: Strict validation ensures URLs are non-empty and target thresholds are positive numbers.

---

## 14. Academic Relevance and College Submission
This project satisfies university curriculum criteria for:
- Object-Oriented Software Engineering
- Web Scraping and Automation
- Relational Database Management Systems (RDBMS)
- Data Analytics and Visualization
- Graphical User Interface (GUI) Design

---

## 15. Known Limitations
- Heavy JavaScript-rendered single-page applications (SPAs) that require browser rendering (e.g., Selenium/Playwright) are parsed via HTML heuristics or mock mode.
- E-commerce sites with aggressive anti-bot CAPTCHAs (Cloudflare, PerimeterX) may return 403 status codes unless proxied.

---

## 16. Future Enhancements
- Integration of Playwright/Selenium for client-side JavaScript hydration.
- Push notifications via Telegram Bot API or Discord Webhooks.
- Multi-retailer price comparison table across Amazon, Flipkart, and Croma.

---

## 17. Evaluator Quick Start
```bash
# 1. Run tests
python tests/test_tracker.py

# 2. Run CLI demo with seeded products and price drop check
python src/main.py --seed --check-all --export --cli

# 3. Launch GUI
python src/main.py
```

---

## 18. References
- BeautifulSoup 4 Documentation: `crummy.com/software/BeautifulSoup/bs4/doc/`
- Python Requests Library: `docs.python-requests.org`
- SQLite3 Documentation: `sqlite.org/docs.html`

---

## 19. License
Educational Open-Source License — Free for academic, learning, and college submission use.

