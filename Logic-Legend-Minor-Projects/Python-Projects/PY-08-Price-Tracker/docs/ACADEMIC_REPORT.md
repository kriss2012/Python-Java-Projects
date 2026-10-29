# Academic Project Report: PY-08 Autonomous Price Tracker with Web Scraping

**Department of Computer Science and Engineering**  
*Undergraduate Minor Project Suite — Logic Legend*

---

## Abstract
Dynamic pricing algorithms implemented by online retailers cause rapid, unpredictable fluctuations in product pricing. Manually monitoring e-commerce websites for discounts is labor-intensive and frequently results in missed buying opportunities. This report details the architecture, design, and implementation of **Autonomous Price Tracker**, a robust desktop and CLI system built using Python, SQLite, Requests, and BeautifulSoup4. The system automatically scrapes e-commerce product pages, logs temporal price variations into a relational database, performs statistical trend analysis to advise prospective buyers, and dispatches real or simulated email notifications when product prices fall below pre-configured thresholds.

---

## System Architecture

```mermaid
flowchart TD
    User["User / Evaluator"] --> GUI["Tkinter Desktop UI / CLI Entry"]
    GUI --> Service["TrackerService (Business Logic)"]
    Service --> Scraper["ScraperEngine (Requests & BS4)"]
    Scraper --> Web["E-Commerce Web Target / Mock Fixture"]
    Service --> DB[(SQLite Database: data/price_tracker.db)]
    Service --> Notifier["Notifier (SMTP & Simulation)"]
    Notifier --> UserEmail["Recipient Email Inbox / Alert Log"]
```

---

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    PRODUCTS ||--o{ PRICE_HISTORY : "has historical"
    PRODUCTS ||--o{ ALERT_LOGS : "triggers"
    PRODUCTS ||--o{ ERROR_LOGS : "records"

    PRODUCTS {
        int id PK
        string url UK
        string title
        float target_price
        float current_price
        string currency
        float rating
        string status
        timestamp created_at
    }

    PRICE_HISTORY {
        int id PK
        int product_id FK
        float price
        timestamp timestamp
    }

    ALERT_LOGS {
        int id PK
        int product_id FK
        float price_at_alert
        float target_price
        string recipient
        string status
        timestamp sent_at
    }

    ERROR_LOGS {
        int id PK
        int product_id FK
        string url
        string error_message
        timestamp logged_at
    }
```

---

## Data Flow Diagram (DFD Level 1)

```mermaid
flowchart LR
    A["User Inputs Product URL & Target Price"] --> B["ScraperEngine: Fetch & Parse DOM"]
    B --> C["Extract Title, Price & Currency"]
    C --> D[("Store in SQLite products Table")]
    E["Automated Check Trigger"] --> F["Fetch Updated Price from Web"]
    F --> G["Append Record in price_history Table"]
    G --> H{"Is Price <= Target Price?"}
    H -- Yes --> I["Notifier: Dispatch Price Drop Alert Email"]
    I --> J[("Log to alert_logs Table")]
    H -- No --> K["No Alert: Await Next Scheduled Check"]
```

---

## Sequence Diagram: Price Check and Alert Dispatch

```mermaid
sequenceDiagram
    autonumber
    actor Evaluator as Evaluator / User
    participant GUI as Desktop GUI / CLI
    participant Service as TrackerService
    participant Scraper as ScraperEngine
    participant DB as SQLite DB
    participant Notifier as Notifier

    Evaluator->>GUI: Click 'Check All Now'
    GUI->>Service: check_all_active()
    loop For Each Active Product
        Service->>DB: Query product URL and target_price
        DB-->>Service: Return product details
        Service->>Scraper: fetch_product_details(url)
        Scraper-->>Service: Extracted new_price
        Service->>DB: UPDATE current_price & INSERT INTO price_history
        alt new_price <= target_price
            Service->>Notifier: send_price_drop_alert()
            Notifier->>DB: INSERT INTO alert_logs
            Notifier-->>Service: Alert confirmation details
        end
    end
    Service-->>GUI: Inspection results summary
    GUI-->>Evaluator: Display updated table & trend charts
```

---

## Mathematical Model for Trend Advice
Given historical prices $P = \{p_1, p_2, \dots, p_n\}$:
- Minimum Historical Price: $P_{min} = \min(P)$
- Maximum Historical Price: $P_{max} = \max(P)$
- Moving Arithmetic Mean: $P_{avg} = \frac{1}{n} \sum_{i=1}^n p_i$
- Current Price: $P_{curr} = p_n$

**Decision Matrix**:
1. If $P_{curr} \le 1.02 \times P_{min} \implies \text{\textbf{BUY NOW}}$ (Exceptional value).
2. If $P_{curr} \ge P_{avg} \implies \text{\textbf{WAIT FOR DROP}}$ (Overpriced relative to historical average).
3. If $P_{min} < P_{curr} < P_{avg} \implies \text{\textbf{FAIR VALUE}}$ (Standard price range).

---

## Conclusion
The PY-08 Autonomous Price Tracker delivers a complete, production-ready solution that combines automated data ingestion, persistent relational data management, heuristic web scraping, and automated communication. The inclusion of offline deterministic mock fixtures guarantees seamless, reliable demonstrations during academic vivas.

