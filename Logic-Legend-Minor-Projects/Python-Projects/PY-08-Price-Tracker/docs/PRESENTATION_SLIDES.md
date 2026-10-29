# Viva Presentation Slides: PY-08 Autonomous Price Tracker

---

### Slide 1: Title Slide
- **Project Title**: Autonomous Price Tracker with Web Scraping
- **Project Code**: PY-08 (Logic Legend Minor Project Suite)
- **Domain**: Web Automation, Heuristic Parsing, Data Analytics & GUI
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Dynamic e-commerce pricing makes manual discount hunting inefficient.
- Lightning deals and flash drops frequently last for mere hours.
- Consumers need an automated agent to monitor desired products, log historical price trends, and provide objective buy/wait guidance.

---

### Slide 3: Proposed Solution
- A standalone desktop and CLI application developed in Python.
- Multi-tier CSS and regex heuristic scraper (`requests` + `BeautifulSoup`).
- SQLite storage for products, time-series history, alert logs, and error logs.
- Automated threshold monitoring with dual-mode email notifications (SMTP + Mock Simulation).
- Historical statistical engine providing "Buy vs Wait" purchasing advice.

---

### Slide 4: System Architecture
- **Layer 1: Presentation**: Tkinter graphical UI + headless CLI.
- **Layer 2: Service / Logic**: `TrackerService` coordinating scrapers and alerts.
- **Layer 3: Scraper Engine**: `ScraperEngine` handling network requests, User-Agent rotation, and HTML heuristic parsing.
- **Layer 4: Data & Messaging**: SQLite database and SMTP notification client.

---

### Slide 5: Web Scraping & Heuristics
- Heuristic fallback across major selector patterns:
  - Titles: `#productTitle`, `h1.product-title`, `.pdp-title`
  - Prices: `.a-price-whole`, `.price`, `span.a-price span`, `._30jeq3`
  - Regex fallback on whole DOM: `(?:₹|\$)\s*([\d,]+(?:\.\d{1,2})?)`
- Sanitization: Strips currency symbols and commas to yield pure floats.
- Offline Demo Mode: Deterministic mock fixtures for guaranteed evaluation reliability.

---

### Slide 6: Purchasing Trend Analytics
- Analyzes all temporal price points for a product:
  - $P_{min}$, $P_{max}$, $P_{avg}$, and $P_{current}$.
- **Decision Engine**:
  - `BUY NOW`: Current price is within 2% of the all-time recorded low.
  - `WAIT FOR DROP`: Current price exceeds the historical average.
  - `FAIR VALUE`: Current price is below average but above historical minimum.

---

### Slide 7: Database Design & Schema
- Relational schema designed in SQLite:
  - `products`: Product metadata, URL, current price, target threshold.
  - `price_history`: Temporal audit trail of all scraped prices.
  - `alert_logs`: Record of dispatched notifications with price timestamps.
  - `error_logs`: Network and parsing exception logging.

---

### Slide 8: Live Demonstration Highlights
- Adding a new product URL with target budget.
- Running immediate price check across all active products.
- Observation of automated price drop alert trigger.
- Viewing time-series price fluctuation chart and statistical verdict.
- One-click CSV export of historical records.

---

### Slide 9: Automated Verification & QA
- 6/6 Unit tests passing:
  - Selector parsing and price sanitization.
  - Mock scraper determinism.
  - Input validation and budget constraints.
  - Price checking and threshold alert dispatch.
  - Purchasing analytics decision matrix.
  - CSV export file structure.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: Built an autonomous, dependable, ethical web scraping platform.
- **Future Scope**:
  - Headless browser rendering using Playwright for heavy SPAs.
  - Multi-retailer cross-site price comparison tables.
  - Direct instant messaging alerts via Telegram and WhatsApp bots.

