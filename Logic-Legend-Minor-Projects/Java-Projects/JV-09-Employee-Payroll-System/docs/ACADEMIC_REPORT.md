# Academic Project Report: Employee Payroll System (JV-09)

## 1. Abstract
The **Employee Payroll System** is an enterprise-grade desktop and console software application designed to automate corporate human resource compensation workflows. Developed in Java 17 and SQLite/MySQL, the system encapsulates employee master data management, attendance-prorated wage calculations, statutory allowances (HRA, DA, Conveyance), statutory tax and provident deductions (EPF, ESI, Professional Tax, TDS), salary increment audit tracking, and automated document generation. This report details the theoretical architecture, system design, entity-relationship modeling, data flow, implementation mechanics, and verification methodologies.

---

## 2. System Architecture

```mermaid
flowchart TD
    User([HR Administrator])
    UI[Swing GUI / Terminal CLI]
    Service[PayrollService Layer]
    DAO[DatabaseManager DAO]
    DB[(SQLite / MySQL Relational Store)]
    Disk[Filesystem: exports/PAYSLIP_*.txt]

    User -->|Interacts with| UI
    UI -->|Invokes business logic| Service
    Service -->|Executes ACID transactions| DAO
    DAO -->|CRUD / Queries| DB
    Service -->|Generates printable payslips| Disk
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    EMPLOYEES ||--o{ SALARY_REVISIONS : "has appraisal history"
    EMPLOYEES ||--o{ PAYROLL_RECORDS : "generates monthly slips"

    EMPLOYEES {
        int id PK
        string emp_code UK
        string name
        string designation
        string department
        string email
        string phone
        string bank_account
        string pan_number
        string date_of_joining
        double basic_salary
        string status
        string created_at
    }

    SALARY_REVISIONS {
        int id PK
        int employee_id FK
        double old_basic
        double new_basic
        string effective_date
        string reason
        string created_at
    }

    PAYROLL_RECORDS {
        int id PK
        string slip_number UK
        int employee_id FK
        string emp_code
        string emp_name
        string department
        string designation
        string month_year
        int total_working_days
        int days_worked
        int unpaid_leaves
        double basic_earned
        double hra
        double da
        double conveyance
        double special_allowance
        double gross_earnings
        double epf
        double esi
        double professional_tax
        double tds
        double total_deductions
        double net_salary
        string created_at
    }
```

---

## 4. Data Flow Diagram (Level 1 DFD)

```mermaid
flowchart LR
    HR([HR Admin]) -->|1. Submit Employee & Attendance| PS[Payroll Service]
    PS -->|2. Query Base Pay & PAN| EDB[(Employee Master)]
    PS -->|3. Compute Allowances & Proration| CALC[Wage Engine]
    CALC -->|4. Apply EPF/ESI/TDS Deductions| CALC
    CALC -->|5. Store Payslip Record| PRDB[(Payroll Records)]
    PRDB -->|6. Render Payslip Slip| PS
    PS -->|7. Export Text File & Display UI| HR
```

---

## 5. Sequence Diagram: Monthly Payroll Processing

```mermaid
sequenceDiagram
    autonumber
    actor Admin as HR Administrator
    participant UI as PayrollFrame / CLI
    participant Service as PayrollService
    participant DB as SQLite Database
    participant FS as Local Filesystem

    Admin->>UI: Select Employee, Month (2026-10), Days Worked (28/30)
    UI->>Service: processPayroll(empId, "2026-10", 30, 2, bonus)
    Service->>DB: SELECT * FROM employees WHERE id = empId
    DB-->>Service: Employee Master Data (Basic: 60,000)
    Service->>Service: Prorate Basic (60,000 * 28/30 = 56,000)
    Service->>Service: Compute Allowances (HRA: 22,400, DA: 11,200, Conv: 2,000)
    Service->>Service: Compute Deductions (EPF: 6,720, ESI: 0, PT: 200, TDS: 7,500)
    Service->>DB: INSERT INTO payroll_records (...)
    DB-->>Service: Generated ID & Slip Number
    Service->>FS: exportPayslipToFile(record, emp, "exports")
    FS-->>Service: File reference (exports/PAYSLIP_*.txt)
    Service-->>UI: Return PayrollRecord & Export Status
    UI-->>Admin: Display Itemized Earnings/Deductions & Net Pay
```

---

## 6. Implementation Highlights
1. **Mathematical Accuracy & Precision**: Monetary values are stored as double-precision floating-point numbers and rounded to 2 decimal places using `Math.round(val * 100.0) / 100.0` at each step to avoid floating-point drift.
2. **ACID Transaction Management**: Salary revisions alter employee master records and insert immutable historical logs within an explicit transaction boundary (`conn.setAutoCommit(false)`), rolling back on any failure.
3. **Statutory Legal Compliance**:
   - EPF is computed strictly as 12% of earned basic.
   - ESI eligibility is dynamically gated by gross monthly earnings ($\le$ ₹21,000).
   - TDS implements a tiered annualized slab bracket conforming to income tax regulations.

---

## 7. Experimental Results & Verification
The system was verified via an automated test runner executing 66 assertions:
- **Employee Operations**: 14 tests verifying creation, unique code enforcement, email regex, update, and lookups.
- **Audit Trails**: 6 tests verifying salary revision transactions, old/new balance auditing, and reasoning tags.
- **Calculations & Proration**: 29 tests verifying full and prorated attendance, allowances, ESI applicability, EPF, and net salary.
- **Formatting & Exporting**: 8 tests verifying ASCII formatting, company branding, and filesystem exports.
- **Analytics**: 9 tests verifying department grouping, employee distinct counts, and gross/net company summaries.
- **Result**: 66 / 66 tests passed (100.0% pass rate).

---

## 8. Conclusion
The **Employee Payroll System** demonstrates modern Java design patterns, separation of concerns (Model-DAO-Service-View), database integrity, and robust business rule enforcement. It provides an optimal blueprint for industrial HR compensation software and academic evaluation.

