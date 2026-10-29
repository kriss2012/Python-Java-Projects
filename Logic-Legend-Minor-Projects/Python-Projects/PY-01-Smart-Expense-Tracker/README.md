# PY-01: Smart Expense Tracker with Analytics
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-01`
- **Project Title**: Smart Expense Tracker with Analytics
- **Domain**: Personal FinTech & Decision Support Systems
- **Platform**: Desktop GUI (Python Tkinter) & Embedded SQLite

---

## 2. Project Overview
The Smart Expense Tracker is an intelligent desktop application engineered to help individuals, students, and households record daily transactions, monitor cash flows, enforce monthly category budgets, and analyze spending habits through interactive visualizations and automated exports.

---

## 3. Problem Statement
Many individuals struggle with financial discipline due to manual tracking friction, lack of real-time visibility into category-specific spending, and the absence of timely threshold alerts before budgets are completely depleted. Existing spreadsheet approaches are error-prone and lack automated OCR receipt ingestion.

---

## 4. Objectives
- Provide an intuitive GUI to record, categorize, filter, and inspect income and expense transactions.
- Enforce proactive budget thresholds (Warning at 80%, Alert at 100%).
- Deliver visual breakdowns of expenses across categories and months.
- Generate exportable financial statements (CSV and formatted reports).
- Provide an OCR extension to parse paper receipts into digital transactions.

---

## 5. Key Features
- **Transaction CRUD**: Full creation, editing, deletion, and filtering of monetary transactions.
- **Budget Monitoring**: Dynamic tracking of spending against user-defined monthly limits.
- **Visual Analytics**: Interactive canvas bar charts depicting category spending.
- **Proactive Notifications**: Color-coded banners indicating healthy, approaching, or exceeded budgets.
- **Data Export**: Comprehensive CSV exports and text/PDF financial statements.
- **Extension (OCR Scanner)**: Automated regex-powered heuristic parser for scanned receipts.

---

## 6. Technology Stack
- **Programming Language**: Python 3.10+
- **GUI Toolkit**: Tkinter & TTK
- **Database**: SQLite3 (ACID compliant, zero-config)
- **Data Processing**: Pandas, Regular Expressions
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS 11+, or Linux.
- **Processor**: Intel Core i3 / AMD Ryzen 3 or higher.
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 100 MB free space.

---

## 8. Prerequisites and Installation
```powershell
# Clone or navigate to the project directory
cd Logic-Legend-Minor-Projects/Python-Projects/PY-01-Smart-Expense-Tracker

# (Optional) Create virtual environment
python -m venv venv
.\venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

---

## 9. Database Setup
The SQLite database (`data/expenses.db`) is automatically initialized and seeded upon first launch. Manual migrations are not required.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
DB_PATH=data/expenses.db
CURRENCY_SYMBOL=INR
DEFAULT_BUDGET_THRESHOLD=80
```

---

## 11. Commands to Run the Application
```powershell
# Launch Graphical Desktop Application
python src/main.py

# Launch Headless CLI Summary Mode
python src/main.py --cli

# Re-seed Initial Demo Data
python src/main.py --seed

# Export Financial Summary Report
python src/main.py --export
```

---

## 12. Demo Credentials
No remote login credentials required. Local administrative privileges are granted by default.

---

## 13. Screenshots and GUI Capture
1. **Dashboard Tab**: Displays 4 KPI cards (Income, Expense, Balance, Savings Rate) and Category Bar Chart.
2. **Transactions Tab**: Displays filterable table and entry form.
3. **Budgets & Alerts Tab**: Shows threshold status (`OK`, `WARNING`, `EXCEEDED`).
4. **Receipt Scanner Tab**: Shows OCR parser with preview confirmation dialog.

---

## 14. API Documentation
Internal service methods provided by `TrackerService`:
- `add_transaction(t_type, amount, category, description, date_str, payment_method)`
- `get_financial_summary(month_year=None)`
- `set_budget(category, month_year, limit_amount, threshold_percent)`
- `check_budgets(month_year=None)`
- `parse_receipt_text(text)`

---

## 15. Test Instructions
```powershell
python tests/test_expense_tracker.py
```
*All 8 unit tests verify CRUD logic, boundary numbers, negative amount rejection, budget calculations, and OCR regex.*

---

## 16. Known Limitations
- The OCR text parser uses regex heuristics; heavily damaged physical receipts with missing totals require manual user correction.
- Multi-currency conversion is not built-in (assumes uniform base currency).

---

## 17. Future Enhancements
- Integration with bank SMS webhooks for automatic expense capture.
- Cloud database synchronization with PostgreSQL / Supabase.
- Mobile responsive companion web client using FastAPI and React.

---

## 18. Troubleshooting
- **Unicode Error on Windows Terminal**: Terminal output is standardized to UTF-8 and uses `INR` prefix for compatibility.
- **Database Locked Error**: Ensure no external SQLite browser has opened `data/expenses.db` in exclusive lock mode.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines. Permitted for college project submissions and viva evaluations.
