# JV-09 — Employee Payroll System

## 1. Project Overview
**Employee Payroll System** is a robust, enterprise-grade HR management and salary computation suite engineered in Java 17 and Swing. Designed for small-to-medium enterprises and academic evaluation, the system automates end-to-end payroll processing: employee onboarding, statutory allowances calculation (Basic, HRA, DA, Conveyance, Special Bonus), statutory deductions compliance (EPF, ESI, Professional Tax, TDS), attendance-based wage proration, appraisal salary revisions with full audit logging, printable salary slips, and department-wise financial analytics.

The application operates in **dual modes**: a rich, responsive **Swing Desktop GUI** for interactive payroll administration and an autonomous **Interactive Terminal CLI** for headless environments, server automation, and grading.

---

## 2. Key Features & Business Logic
- **Employee Lifecycle Management**: Complete CRUD operations for employee records including PAN, bank account number, designation, department, contact details, and base salary.
- **Attendance Proration Engine**: Computes wage deductions based on unpaid leaves against total monthly working days with ratio proration:
  $$\text{Basic Earned} = \text{Basic Salary} \times \left(\frac{\text{Days Worked}}{\text{Total Days}}\right)$$
- **Allowances Computation**:
  - House Rent Allowance (HRA): 40% of Basic Earned
  - Dearness Allowance (DA): 20% of Basic Earned
  - Conveyance Allowance: Fixed ₹2,000.00
  - Special Bonus / Merit Allowance: Configurable one-time/monthly bonus
- **Statutory Deductions Compliance**:
  - Employee Provident Fund (EPF): 12% of Basic Earned
  - Employee State Insurance (ESI): 0.75% of Gross Earnings if monthly gross $\le$ ₹21,000; 0% otherwise
  - Professional Tax (PT): Fixed ₹200.00 per month
  - Tax Deducted at Source (TDS): Indian New Tax Regime slab proration based on annualized gross income
- **Salary Revision Audit Trail**: Atomic database transactions that update base pay while permanently recording prior salary, revised salary, effective date, and reason in `salary_revisions`.
- **Official Payslip Generation & Export**: Generates professional ASCII payslips with employee credentials, itemized earnings and deductions, bank details, and exports to `exports/PAYSLIP_*.txt`.
- **Department Financial Analytics**: Aggregates employee headcount, total gross outlay, total deductions, and net salary payout across departments.

---

## 3. Technology Stack & Prerequisites
- **Language**: Java 17+ (LTS)
- **GUI Framework**: Java Swing (Native Look-and-Feel, Custom Styled Cards)
- **Database**: SQLite 3.36+ (Zero-dependency embedded JDBC driver bundled in `lib/`)
- **Production RDBMS**: MySQL 8.0+ compatible (`sql/schema_mysql.sql` included)
- **Build Tool**: Apache Maven / Standalone `javac`
- **Prerequisites**: JDK 17 or JDK 21 installed and added to `PATH`.

---

## 4. Directory Structure
```
JV-09-Employee-Payroll-System/
├── .env.example
├── .gitignore
├── pom.xml
├── lib/
│   └── sqlite-jdbc.jar
├── sql/
│   ├── schema_sqlite.sql
│   └── schema_mysql.sql
├── data/
│   └── payroll.db
├── exports/
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── src/
│   └── com/logiclegend/payroll/
│       ├── Main.java
│       ├── model/
│       │   ├── Employee.java
│       │   ├── PayrollRecord.java
│       │   └── SalaryRevision.java
│       ├── dao/
│       │   └── DatabaseManager.java
│       ├── service/
│       │   └── PayrollService.java
│       └── ui/
│           └── PayrollFrame.java
├── tests/
│   └── TestRunner.java
└── README.md
```

---

## 5. Database Architecture & Schema
The relational database enforces data integrity through foreign keys, check constraints, and unique indexes:
- `employees`: Stores core employee master data, email regex validation, PAN, and active/inactive status.
- `salary_revisions`: Audit history recording old vs new basic pay, timestamp, and appraisal justification.
- `payroll_records`: Snapshot of processed monthly payroll entries including attendance counters, itemized earnings, deductions, and net salary.

---

## 6. Installation & Quick Start
### 6.1 Clone & Navigate
```bash
cd "Logic-Legend-Minor-Projects/Java-Projects/JV-09-Employee-Payroll-System"
```

### 6.2 Compilation
```powershell
if (-not (Test-Path bin)) { New-Item -ItemType Directory -Path bin }
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\payroll\model\*.java src\com\logiclegend\payroll\dao\*.java src\com\logiclegend\payroll\service\*.java src\com\logiclegend\payroll\ui\*.java src\com\logiclegend\payroll\Main.java tests\TestRunner.java
```

### 6.3 Run Graphical Interface (Desktop GUI)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.payroll.Main
```

### 6.4 Run Interactive Command-Line Interface (CLI Mode)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.payroll.Main --cli
```

---

## 7. Running Automated Tests
The automated test runner executes 66 assertions covering validation, proration, statutory deductions, salary revisions, file exports, and analytical aggregations:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Expected output:
```
=================================================================
  TESTS FINISHED: 66 / 66 PASSED (100.0%)
=================================================================
>>> ALL JV-09 PAYROLL SYSTEM TESTS PASSED SUCCESSFULLY! <<<
```

---

## 8. Application Walkthrough & Screenshots
1. **Employee Directory**: Filter employees by department or search by code/name. View current base salaries and status.
2. **Onboard Employee**: Register new employee with email regex check, PAN validation, and designation.
3. **Salary Appraisal**: Enter new basic salary and justification; automatically applies changes and records in the audit log.
4. **Process Payroll**: Select employee, pay period (e.g., `2026-10`), working days (30), unpaid leaves (2), and optional bonus. System instantly computes gross, deductions, and net pay.
5. **View Payslip**: Displays two-column formatted salary slip with single-click export to `exports/`.
6. **Department Analytics**: Displays summary table with distinct headcount, gross outlay, and department net totals.

---

## 9. Statutory Calculations Breakdown
| Component | Classification | Formula / Rule |
| :--- | :--- | :--- |
| **Basic Earned** | Earning | $\text{Base Salary} \times (\text{Days Worked} / \text{Total Days})$ |
| **HRA** | Earning | $40\% \times \text{Basic Earned}$ |
| **DA** | Earning | $20\% \times \text{Basic Earned}$ |
| **Conveyance** | Earning | Fixed ₹2,000.00 |
| **EPF** | Deduction | $12\% \times \text{Basic Earned}$ |
| **ESI** | Deduction | $0.75\% \times \text{Gross}$ (Only if Gross $\le$ ₹21,000) |
| **Prof Tax** | Deduction | Fixed ₹200.00 |
| **TDS** | Deduction | Annualized income slab calculation / 12 |

---

## 10. Security & Data Integrity
- PreparedStatements prevent SQL injection attacks across all database queries.
- Strict input validation prevents negative salaries, impossible leave counts, and malformed emails.
- Database transactions (`conn.setAutoCommit(false)`) protect salary revisions to guarantee audit records are created atomically.
- Zero external network dependencies ensure 100% offline data confidentiality.

---

## 11. Troubleshooting & FAQs
- **Q: How to run on headless Linux server?**
  - Use `--cli` flag (`java -cp "bin:lib/sqlite-jdbc.jar" com.logiclegend.payroll.Main --cli`).
- **Q: Where are exported payslips saved?**
  - Text files are exported to the `exports/` folder named `PAYSLIP_PAY-<YYYYMM>-<EMP_CODE>.txt`.
- **Q: How to switch to MySQL?**
  - Execute `sql/schema_mysql.sql` in MySQL, update `.env` or `DatabaseManager.java` connection string to point to `jdbc:mysql://localhost:3306/payroll_db`.

---

## 12. Evaluation & Viva Guide
Refer to [docs/VIVA_QUESTIONS.md](file:///c:/D%20Drive/Python+Java=Projects/Logic-Legend-Minor-Projects/Java-Projects/JV-09-Employee-Payroll-System/docs/VIVA_QUESTIONS.md) for 15+ faculty viva questions and [docs/DEMO_GUIDE.md](file:///c:/D%20Drive/Python+Java=Projects/Logic-Legend-Minor-Projects/Java-Projects/JV-09-Employee-Payroll-System/docs/DEMO_GUIDE.md) for evaluator testing steps.

