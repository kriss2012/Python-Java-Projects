# PRESENTATION SLIDES OUTLINE
## PY-01: Smart Expense Tracker with Analytics
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Introduction
- **Project**: Smart Expense Tracker with Analytics (PY-01)
- **Domain**: Personal FinTech & Intelligent Analytics
- **Technologies**: Python, Tkinter, SQLite, Matplotlib, Regex/OCR
- **Motto**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement
- Why personal budgeting fails:
  - Friction in recording daily small cash and digital transactions.
  - Absence of real-time warnings before budget exhaustion.
  - Fragmented receipts lost in wallets and chat history.
  - Lack of clear visual insights into discretionary expenses.

---

### Slide 3: Objectives & Solution Overview
- A desktop-native personal finance application:
  - Instant zero-latency CRUD for income and expenses.
  - Automatic calculation of net balance and savings rates.
  - Proactive two-tier budget alerts (Warning @ 80%, Critical @ 100%).
  - Standout Feature: OCR paper receipt scanning with user confirmation.

---

### Slide 4: System Architecture & Workflow
- Layered Model-View-Service Architecture:
  - **Presentation Layer**: Custom Tkinter UI with dynamic KPI cards & Canvas visualizer.
  - **Service Layer**: Business rules, budget computation, and export engines.
  - **Data Access Layer**: SQLite database with relational constraints and indexes.

---

### Slide 5: Database Schema Design
- Fully normalized relational schema:
  - `categories`: Predefined and custom user spending classes.
  - `transactions`: Date, amount, type, payment mode, description.
  - `budgets`: Category, month-year period, limit ceiling, warning threshold.

---

### Slide 6: Proactive Budget Monitoring Engine
- Real-time comparison of current calendar month spending against limits:
  - Mathematical state machine: `OK` -> `WARNING` -> `EXCEEDED`.
  - Color-coded notification banner prominently displayed on dashboard.
  - Detailed utilization percentage displayed in budget management table.

---

### Slide 7: OCR Receipt Ingestion (Extension)
- Eliminates manual data entry:
  - Regex pattern matcher extracts date, merchant, and total sums.
  - Safety mechanism: Mandatory user confirmation modal prevents corrupted or incorrect inserts.

---

### Slide 8: Demonstration & Visual Highlights
- Live demonstration showing:
  - Adding income and expense entries.
  - Real-time KPI update (Net Balance & Savings Rate).
  - Instant transition from `OK` to `WARNING` state when adding expenses.
  - One-click CSV and Textual summary statement exports.

---

### Slide 9: Testing & Quality Assurance
- Automated test suite with 100% passing results:
  - Boundary input testing (zero, negative, format mismatch).
  - ACID transaction integrity across application restarts.
  - Mathematical verification of net balances and percentage thresholds.

---

### Slide 10: Limitations & Future Enhancements
- **Current Limitations**: Manual entry required if receipt image is severely degraded.
- **Future Scope**:
  - SMS notification scraper for automated bank debit tracking.
  - Direct PDF export via ReportLab styling.
  - Multi-currency support for international transactions.

---

### Slide 11: Conclusion & Q&A
- Delivered a functional, resilient, well-tested personal finance solution.
- Open for questions and technical evaluation.

