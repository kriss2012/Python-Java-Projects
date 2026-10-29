# VIVA VOCE PREPARATION & QUESTIONS
## PY-01: Smart Expense Tracker with Analytics
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the primary motivation behind developing this Smart Expense Tracker?
**Answer**: Many individuals fail to control overspending because spreadsheets and notebooks lack real-time threshold warnings and automated analytics. This project provides a low-friction, offline-first desktop application that enforces budget ceilings, provides visual category breakdowns, and digitizes physical receipts.

### Q2: Why did you choose SQLite over MySQL or PostgreSQL for this project?
**Answer**: SQLite is a serverless, zero-configuration, self-contained relational database stored in a single cross-platform file. For a desktop personal finance tool, it eliminates the need to manage database background daemons or configure network sockets, while providing full ACID transactions and SQL compliance.

### Q3: Explain the architectural pattern implemented in this application.
**Answer**: The project follows a Layered MVC-like architecture:
1. **View**: Tkinter GUI (`gui.py`) handling user events and widget rendering.
2. **Service / Controller**: `TrackerService` (`tracker_service.py`) executing business logic, mathematical calculations, and validations.
3. **Model / Persistence**: `DatabaseManager` (`database.py`) encapsulating database connections and SQL execution.

### Q4: How does the budget monitoring engine calculate threshold alerts?
**Answer**: The system queries total spending for a specific category within the current calendar month (`YYYY-MM`). It calculates percentage usage: $(\text{spent} / \text{limit}) \times 100$. If spending reaches or exceeds the threshold (e.g., 80%), the status switches to `WARNING`. If spending exceeds 100%, the status becomes `EXCEEDED`.

### Q5: What input validation checks are performed before saving a transaction?
**Answer**:
1. Amount must be a positive non-zero float.
2. Date must strictly conform to ISO 8601 (`YYYY-MM-DD`).
3. Transaction type must be strictly `'income'` or `'expense'`.
4. Category must be selected and non-empty.

### Q6: How does the OCR receipt scanning extension work?
**Answer**: The scanner ingests receipt text (or OCR-extracted strings) and runs heuristic regex patterns. It identifies ISO/European date patterns, search keywords like `TOTAL`, `AMOUNT`, `NET`, or `Rs.`, and captures candidate monetary values. The highest monetary candidate is proposed as the total, and a user confirmation modal is displayed before persisting.

### Q7: Why do you require user confirmation before saving OCR-extracted data?
**Answer**: OCR is inherently subject to inaccuracies caused by camera angle, wrinkles, faded ink, or non-standard receipt layouts. Requiring user confirmation prevents bad data from corrupting the user's financial balance without their consent.

### Q8: How is data consistency maintained after application restarts?
**Answer**: All transactions, categories, and budget parameters are committed to `expenses.db` using SQLite transactions. When the application restarts, it connects to the same file, ensuring persistence.

### Q9: How do you prevent SQL injection vulnerabilities?
**Answer**: We use parameterized queries exclusively (e.g., `cursor.execute("SELECT ... WHERE type = ?", (t_type,))`). Raw strings are never concatenated directly into SQL query statements.

### Q10: How does your Tkinter GUI render the spending charts without external heavy browser engines?
**Answer**: We utilize Tkinter's native `Canvas` widget to render proportional bar charts dynamically based on category weights. This guarantees immediate zero-dependency rendering on any operating system without requiring web engines or heavy external binaries.

### Q11: What testing framework was used, and what scenarios were covered?
**Answer**: Python's `unittest` module was used. Tests cover:
- Negative and zero amount rejections.
- Invalid date format handling.
- Calculation of net balance and savings rates.
- Multi-stage budget alert transitions (`OK` -> `WARNING` -> `EXCEEDED`).
- CSV and textual statement export verification.
- Regex OCR extraction accuracy.

### Q12: How are database transactions handled when an error occurs during an update?
**Answer**: Database calls are wrapped in Python `with self.get_connection() as conn:` context managers, which automatically issue a `ROLLBACK` if an uncaught exception is raised, and commit only upon clean completion.

### Q13: What happens if a user enters an expense exceeding their remaining total balance?
**Answer**: The application allows the entry but calculates a negative net balance and displays an alert indicating a deficit. This ensures historical records reflect actual spending rather than artificially halting accounting.

### Q14: How does the application support custom categories?
**Answer**: Categories are stored as distinct relational entities in the `categories` table. Users can create new custom categories for either income or expense, which immediately become selectable across forms and budget settings.

### Q15: What are the main limitations of this application in its current state?
**Answer**:
1. It is a single-user local desktop application without multi-device cloud synchronization.
2. OCR relies on standard receipt formatting; cursive handwritten bills are not recognized reliably.

### Q16: How would you scale this application for enterprise or multi-user deployment?
**Answer**: We would migrate SQLite to a hosted PostgreSQL database, transition the business logic into a REST API (using FastAPI or Spring Boot), and provide multi-tenant authentication with JWT tokens.
