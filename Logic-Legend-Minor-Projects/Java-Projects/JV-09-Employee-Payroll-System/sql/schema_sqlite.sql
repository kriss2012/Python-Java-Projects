-- Logic Legend JV-09: Employee Payroll System Schema (SQLite)

CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    designation TEXT NOT NULL,
    department TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT NOT NULL,
    bank_account TEXT NOT NULL,
    pan_number TEXT NOT NULL,
    date_of_joining TEXT NOT NULL,
    basic_salary REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payroll_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    slip_number TEXT UNIQUE NOT NULL,
    employee_id INTEGER NOT NULL,
    emp_code TEXT NOT NULL,
    emp_name TEXT NOT NULL,
    department TEXT NOT NULL,
    designation TEXT NOT NULL,
    month_year TEXT NOT NULL, -- YYYY-MM
    total_working_days INTEGER NOT NULL DEFAULT 30,
    days_worked INTEGER NOT NULL,
    unpaid_leaves INTEGER NOT NULL DEFAULT 0,
    basic_earned REAL NOT NULL,
    hra REAL NOT NULL,
    da REAL NOT NULL,
    conveyance REAL NOT NULL,
    special_allowance REAL NOT NULL DEFAULT 0.0,
    gross_earnings REAL NOT NULL,
    epf REAL NOT NULL,
    esi REAL NOT NULL,
    professional_tax REAL NOT NULL DEFAULT 200.0,
    tds REAL NOT NULL DEFAULT 0.0,
    total_deductions REAL NOT NULL,
    net_salary REAL NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE IF NOT EXISTS salary_revisions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    old_basic REAL NOT NULL,
    new_basic REAL NOT NULL,
    effective_date TEXT NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

