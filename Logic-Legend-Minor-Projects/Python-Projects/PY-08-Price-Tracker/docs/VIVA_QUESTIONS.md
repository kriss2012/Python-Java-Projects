# Viva Examination Questions and Answers: PY-08 Autonomous Price Tracker

### Q1: What is web scraping, and how does your application extract product prices?
**Answer**: Web scraping is the programmatic extraction of data from websites. In this project, Python's `requests` library retrieves the raw HTML document from an e-commerce URL. `BeautifulSoup4` parses the DOM tree, navigating through a list of heuristic CSS selectors (e.g., `.a-price-whole`, `#productTitle`, `h1`). If selectors fail due to obfuscated class names, a regular expression pattern scans the HTML text for currency symbols followed by numbers.

### Q2: How do you handle dynamic or anti-bot protections like CAPTCHAs and 403 Forbidden errors?
**Answer**: We configure realistic browser User-Agent and Accept-Language headers in every request so the client appears as a standard desktop browser. Additionally, we enforce timeouts, avoid aggressive request loops, and implement deterministic mock fixtures so offline college demonstrations never fail even if an external site activates anti-bot protections.

### Q3: Why is price sanitization necessary, and how is it implemented?
**Answer**: E-commerce pages render prices with localized currency symbols (e.g., `₹`, `$`, `INR`), commas (`24,999.00`), or non-breaking spaces. If not cleaned, converting this string to a float raises a `ValueError`. Our `ScraperEngine.parse_price()` uses regular expressions to strip all non-digit and non-decimal characters before casting to float.

### Q4: Explain the difference between client-side JavaScript rendering and server-rendered HTML.
**Answer**: Server-rendered HTML sends full product information in the initial HTTP response, which `requests` can parse directly. Client-side Single Page Applications (SPAs) load an empty container and populate product data dynamically via JavaScript APIs. For SPAs, a headless browser like Selenium/Playwright or inspecting network XHR endpoints is typically used.

### Q5: How does your application prevent false or duplicate email alerts?
**Answer**: The system checks if `current_price <= target_price`. To prevent spamming the user on every check loop, alert occurrences are recorded in the `alert_logs` table with timestamps. The business logic can enforce a minimum cooldown interval before re-alerting for the same threshold.

### Q6: What happens if an evaluator does not have internet access during the viva?
**Answer**: The application includes built-in offline mock handling (`_get_mock_product_data`). URLs containing `example.com` or `test-store` resolve deterministically to realistic pre-seeded products without sending external network packets, ensuring a 100% reliable evaluation.

### Q7: Explain the mathematical logic behind the "Best Time to Buy" recommendation.
**Answer**: We compute the historical minimum ($P_{min}$), maximum ($P_{max}$), and average ($P_{avg}$) of all recorded price points for a product. If the current price is within 2% of $P_{min}$, the algorithm recommends `BUY NOW`. If it is above $P_{avg}$, it recommends `WAIT FOR DROP`. Otherwise, it marks the price as `FAIR VALUE`.

### Q8: How is data persisted, and how are historical price trends maintained?
**Answer**: We use an ACID-compliant SQLite database with foreign key relationships. The `products` table stores current metadata, while the `price_history` table appends a new record (`price`, `timestamp`, `product_id`) every time a scrape occurs, forming an immutable time-series log.

### Q9: What happens if a tracked product URL becomes invalid or returns a 404/500 error?
**Answer**: The exception is caught gracefully in `ScraperEngine.fetch_product_details()`. Instead of crashing, an error dictionary is returned, and an audit entry is logged to the `error_logs` table. The application continues running other checks smoothly.

### Q10: How does the application support both GUI and CLI modes?
**Answer**: `src/main.py` parses command-line arguments using `argparse`. If flags such as `--cli`, `--seed`, or `--check-all` are detected, it executes headless commands. If no flags are provided, it attempts to launch the Tkinter GUI. If Tkinter fails due to a missing display server (e.g., headless Linux), it falls back to CLI mode automatically.

### Q11: What is the purpose of `.env.example` in this project?
**Answer**: It acts as a template for environment variables, showing parameters like `SMTP_SERVER`, `SENDER_EMAIL`, and `SIMULATE_EMAIL`. Users configure sensitive passwords in a local `.env` file that is ignored by `.gitignore`, preventing hardcoded credentials in version control.

### Q12: Why did you choose SQLite over MySQL for this desktop tracker?
**Answer**: SQLite is serverless, zero-configuration, and embedded as a single file inside the application directory. It provides complete SQL and ACID transactional guarantees without requiring the user or college evaluator to install and configure an external MySQL service daemon.

### Q13: What is the role of `schedule` library in this system?
**Answer**: The `schedule` library allows defining periodic background tasks using human-readable syntax (e.g., `schedule.every(6).hours.do(check_all_prices)`). In GUI mode, this can be coupled with Tkinter's `root.after()` or background threads.

### Q14: How does the CSV export feature work?
**Answer**: The `TrackerService.export_csv()` executes an inner join between `products` and `price_history`, retrieving all temporal records sorted by product and timestamp. It utilizes Python's built-in `csv.DictWriter` to generate a structured `.csv` file.

### Q15: How can this system be ethically deployed on commercial websites?
**Answer**: By respecting `robots.txt` guidelines, throttling requests with randomized delays (polite crawling), avoiding scraping copyrighted or private data, and limiting automated checks to reasonable intervals (e.g., 2–4 times per day per product).

