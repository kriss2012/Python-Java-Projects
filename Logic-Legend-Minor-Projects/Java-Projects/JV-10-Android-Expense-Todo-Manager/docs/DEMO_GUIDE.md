# Evaluator Demo & Verification Guide: Android Expense and To-Do Manager (JV-10)

This guide provides step-by-step instructions for academic faculty, mentors, and technical evaluators to verify the functionality of the **Android Expense and To-Do Manager (JV-10)**.

---

## 1. Quick Verification (Automated Test Suite)
Open PowerShell or Terminal and execute:
```powershell
cd "Logic-Legend-Minor-Projects/Java-Projects/JV-10-Android-Expense-Todo-Manager"
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
- **Expected Result**: 54 assertions pass across 5 functional test groups with 100% pass rate.

---

## 2. Interactive Demonstration Steps

### Option A: Graphical Desktop Companion GUI (Instant Verification)
Run the desktop GUI on any machine with JDK 17+:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.expensetodo.Main
```

#### Step 1: Inspect KPI Cards & Pre-Seeded Data
1. The application window opens with top dashboard cards:
   - **THIS MONTH'S EXPENSES**: Displays current month total (e.g., ₹16,849.00).
   - **BUDGET HEALTH**: Displays overall status (`HEALTHY`, `THRESHOLD ALERT`, or `OVER BUDGET`).
   - **PENDING TASKS**: Displays count of active to-dos.
2. Pre-seeded demo expenses and to-dos appear immediately in their respective tables.

#### Step 2: Test Budget Threshold Warnings
1. Click the **"Monthly Budgets & Alerts"** tab.
2. Observe pre-configured category budgets:
   - `Food & Dining`: Budget ₹10,000, Spent ₹5,600 (56.0% - Status: **SAFE**, Green).
   - `Shopping`: Budget ₹6,000, Spent ₹4,800 (80.0% - Status: **WARNING**, Amber).
3. Now switch to the **"Expenses & Ledger"** tab.
4. Click **"+ Add Expense"**:
   - **Title**: `Supermarket Weekend Restock`
   - **Amount**: `3000`
   - **Category**: `Food & Dining`
   - **Payment Method**: `Credit Card`
5. Click **"OK"**.
6. Switch back to **"Monthly Budgets & Alerts"**:
   - Food & Dining spent increases to ₹8,600 (86.0%).
   - Observe status dynamically changes from `SAFE` to **`WARNING`** with amber text!
7. Add another expense of ₹2,000 under `Food & Dining`:
   - Total spent becomes ₹10,600 (106.0%).
   - Observe status dynamically turns to **`EXCEEDED`** with red warning text!

#### Step 3: Manage To-Do Tasks & Prioritization
1. Click the **"To-Do Task Manager"** tab.
2. Observe tasks ordered by priority (`HIGH` first, then `MEDIUM`, then `LOW`).
3. Click **"+ Add Task"**:
   - **Title**: `Submit Quarterly GST Invoices`
   - **Description**: `Compile vendor invoices and submit to tax advisor`
   - **Priority**: `HIGH`
   - **Due Date**: Today or tomorrow
   - **Linked Category**: `Bills & Utilities`
4. Click **"OK"**. The new high-priority task appears at the top of the list.
5. Select the task and click **"Toggle Complete"**:
   - Status updates from `[PENDING]` to `[DONE]`.
   - Top banner pending counter decreases by 1.

#### Step 4: Category Spending Analytics
1. Click the **"Category Analytics"** tab.
2. View the distribution table displaying total outlay, transaction count, and percentage share per category.
3. Observe percentages sum to 100%.

#### Step 5: Export Data to CSV
1. Switch to the **"Expenses & Ledger"** tab.
2. Click **"Export CSV"**.
3. A confirmation dialog displays the path to the exported CSV file in `exports/EXPENSES_ALL.csv`.
4. Open the CSV file to inspect sanitized columns and headers.

---

### Option B: Interactive CLI Demonstration
For headless servers or non-GUI evaluation, run:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.expensetodo.Main --cli
```
Menu options:
1. `1` - List All Expenses
2. `2` - Add New Expense
3. `3` - List To-Do Tasks
4. `4` - Add To-Do Task
5. `5` - Toggle Task Completion
6. `6` - Check Budget Health & Threshold Warnings
7. `7` - Set / Update Category Budget
8. `8` - View Category Analytics Breakdown
9. `9` - Export Expenses to CSV
10. `0` - Exit

---

### Option C: Mobile Android Studio Build (Optional)
1. Open the `JV-10-Android-Expense-Todo-Manager` directory in Android Studio.
2. Allow Gradle to sync dependencies (`Room`, `MPAndroidChart`, `Material`).
3. Select an Android Emulator (API 34) and click **Run**.
4. The native mobile UI launches with bottom tab navigation, ViewPager2, and MPAndroidChart pie rendering.

