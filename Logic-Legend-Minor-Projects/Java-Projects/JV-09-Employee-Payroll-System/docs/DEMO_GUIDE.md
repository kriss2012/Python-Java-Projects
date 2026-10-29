# Evaluator Demo & Verification Guide: Employee Payroll System (JV-09)

This guide provides step-by-step instructions for academic faculty, mentors, and technical evaluators to verify the functionality of the **Employee Payroll System (JV-09)**.

---

## 1. Quick Verification (Automated Test Suite)
Open PowerShell or Terminal and execute:
```powershell
cd "Logic-Legend-Minor-Projects/Java-Projects/JV-09-Employee-Payroll-System"
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
- **Expected Result**: 66 assertions pass across 6 functional test groups with 100% pass rate.

---

## 2. Interactive Demonstration Steps

### Option A: Graphical Desktop GUI
Launch the application:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.payroll.Main
```

#### Step 1: Browse Employee Master Directory
1. The main window opens with the **Employees** tab active.
2. Observe pre-seeded demo employees:
   - Vikramaditya Rao (Engineering, Principal Architect, ₹95,000)
   - Ananya Deshpande (Engineering, Senior Software Engineer, ₹65,000)
   - Suresh Menon (HR, HR Manager, ₹50,000)
   - Fatima Zahra (Marketing, Marketing Specialist, ₹40,000)
3. Select "Engineering" in the department filter dropdown to filter the table.
4. Type "Ananya" in the search box to test instant real-time filtering.

#### Step 2: Onboard a New Employee
1. Click the **"Onboard Employee"** button on the toolbar.
2. Fill in the modal form:
   - **Emp Code**: `EMP-1005`
   - **Name**: `Karan Malhotra`
   - **Designation**: `DevOps Engineer`
   - **Department**: `Infrastructure`
   - **Email**: `karan@logiclegend.com`
   - **Phone**: `+91 9876543205`
   - **Bank Account**: `HDFC00998877`
   - **PAN**: `ABCDE9876K`
   - **Joining Date**: `2024-05-01`
   - **Basic Salary**: `55000`
3. Click **"Save Employee"**. Observe the success message and verification that Karan Malhotra appears in the table.

#### Step 3: Revise Employee Salary (Appraisal Audit)
1. Select `EMP-1005 (Karan Malhotra)` from the table.
2. Click **"Revise Salary"**.
3. Enter:
   - **New Basic Salary**: `70000`
   - **Effective Date**: `2026-10-01`
   - **Reason**: `Promoted to Senior DevOps Engineer`
4. Click **"Confirm Revision"**.
5. Observe the base salary in the table immediately updates to `₹70,000.00`.

#### Step 4: Process Monthly Payroll
1. Click the **"Process Payroll"** tab or toolbar button.
2. Select employee `EMP-1005 - Karan Malhotra`.
3. Set:
   - **Pay Period**: `2026-10`
   - **Total Working Days**: `30`
   - **Unpaid Leaves**: `2` (Days worked = 28)
   - **Special Bonus**: `5000`
4. Click **"Calculate & Process Payroll"**.
5. Observe the instant breakdown displayed:
   - **Basic Earned**: $70000 \times (28 / 30) = ₹65,333.33$
   - **HRA (40%)**: $₹26,133.33$
   - **DA (20%)**: $₹13,066.67$
   - **Conveyance**: $₹2,000.00$
   - **Bonus**: $₹5,000.00$
   - **Gross Earnings**: $₹111,533.33$
   - **EPF (12%)**: $₹7,840.00$
   - **ESI**: $₹0.00$ (Gross > ₹21,000)
   - **Professional Tax**: $₹200.00$
   - **Net Salary**: Accurately calculated and credited.

#### Step 5: Export & Inspect Payslip
1. Switch to the **"Payslip History"** tab.
2. Select the latest payslip for `EMP-1005`.
3. Click **"Export Payslip to File"**.
4. Open the generated file in `exports/` (`PAYSLIP_PAY-202610-EMP-1005.txt`) to inspect the formatted ASCII slip with all line items and company header.

#### Step 6: Department Financial Summary
1. Click the **"Department Analytics"** tab.
2. Inspect the breakdown showing headcount, gross outlay, deductions, and net payout per department.

---

### Option B: Interactive CLI Demonstration
For headless servers or non-GUI evaluation, run:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.payroll.Main --cli
```
Menu options:
1. `1` - List All Employees
2. `2` - Add New Employee
3. `3` - Revise Employee Salary
4. `4` - Process Monthly Payroll
5. `5` - View & Export Payslip
6. `6` - View Department Financial Analytics
7. `0` - Exit

All steps execute in real time using the identical backend database and service layer.

