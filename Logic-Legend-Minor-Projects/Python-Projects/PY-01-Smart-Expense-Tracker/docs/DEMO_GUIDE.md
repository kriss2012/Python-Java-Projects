# EVALUATOR DEMONSTRATION GUIDE
## PY-01: Smart Expense Tracker with Analytics
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open PowerShell or your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-01-Smart-Expense-Tracker
python src/main.py
```
*The graphical user interface opens loaded with pre-seeded demo records.*

---

## 2. Recommended Demonstration Sequence

### Step 1: Examine the Dashboard & KPI Cards
- Point out the 4 primary KPI cards at the top:
  - **Total Income**: Pre-seeded with ₹ 53,500.00
  - **Total Expenses**: Pre-seeded with ₹ 21,800.00
  - **Net Balance**: Displaying ₹ 31,700.00
  - **Savings Rate**: Showing healthy 59.3 %
- Highlight the **Budget Alert Banner**: It will display a Yellow Warning indicating categories approaching their threshold.
- Observe the **Category-Wise Spending Breakdown** bar chart dynamically drawn on the canvas.

### Step 2: Record a New Expense Transaction
- Switch to the **Transactions** tab.
- In the quick-add bar:
  - Type: Select `expense`
  - Amount: Enter `1200`
  - Category: Select `Food & Dining`
  - Date: Default is today's date (`YYYY-MM-DD`)
  - Mode: Select `UPI`
  - Description: Enter `Team Hackathon Dinner`
- Click **➕ Save Entry**.
- Point out:
  - Success message dialog confirms insertion.
  - The transaction immediately appears at the top of the table.
  - Switch back to the **Dashboard** tab: Total expenses increased to ₹ 23,000.00 and Food & Dining spending has increased.

### Step 3: Trigger a Budget Exceeded Alert
- Switch to the **Budgets & Alerts** tab.
- Observe the `Food & Dining` row. Note that limit is ₹ 5,000.00 and spending is now ₹ 5,400.00.
- Point out the status has switched from `WARNING` to `EXCEEDED`!
- Switch to the **Dashboard** tab: The top alert banner has turned **RED** with `🚨 ALERT: 1 Category Budget(s) EXCEEDED!`.

### Step 4: Demonstrate OCR Receipt Extraction (Extension)
- Switch to the **Receipt Scanner (OCR)** tab.
- Click **📄 Load Sample Receipt Text**.
- A realistic store tax invoice appears in the text area.
- Click **🔍 Extract & Review Transaction**.
- A modal dialog appears showing the extracted details:
  - Extracted Merchant: `CAMPUS TECH SUPERSTORE`
  - Extracted Total: `₹ 1709.82`
  - Date: `2026-10-25`
- Click **Yes** to confirm.
- Verify that this transaction was immediately added to the database.

### Step 5: Export Financial Statements
- Switch to the **Transactions** tab.
- Click **📄 Export Report**.
- Choose a file destination or accept default.
- Open the resulting `.txt` file to show the structured financial summary statement.
- Click **📥 Export CSV** to demonstrate raw data export for spreadsheet auditing.

---

## 3. Resetting Demo Data
To restore pristine demonstration records at any time:
```powershell
python src/main.py --seed
```
Or delete `data/expenses.db` and relaunch `python src/main.py`.

