-- Logic Legend JV-09: Employee Payroll System Schema (MySQL)

CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emp_code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    designation VARCHAR(80) NOT NULL,
    department VARCHAR(80) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    phone VARCHAR(30) NOT NULL,
    bank_account VARCHAR(50) NOT NULL,
    pan_number VARCHAR(30) NOT NULL,
    date_of_joining VARCHAR(20) NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payroll_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slip_number VARCHAR(50) UNIQUE NOT NULL,
    employee_id INT NOT NULL,
    emp_code VARCHAR(30) NOT NULL,
    emp_name VARCHAR(150) NOT NULL,
    department VARCHAR(80) NOT NULL,
    designation VARCHAR(80) NOT NULL,
    month_year VARCHAR(20) NOT NULL,
    total_working_days INT NOT NULL DEFAULT 30,
    days_worked INT NOT NULL,
    unpaid_leaves INT NOT NULL DEFAULT 0,
    basic_earned DECIMAL(12,2) NOT NULL,
    hra DECIMAL(12,2) NOT NULL,
    da DECIMAL(12,2) NOT NULL,
    conveyance DECIMAL(12,2) NOT NULL,
    special_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    gross_earnings DECIMAL(12,2) NOT NULL,
    epf DECIMAL(12,2) NOT NULL,
    esi DECIMAL(12,2) NOT NULL,
    professional_tax DECIMAL(12,2) NOT NULL DEFAULT 200.00,
    tds DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_deductions DECIMAL(12,2) NOT NULL,
    net_salary DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS salary_revisions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    old_basic DECIMAL(12,2) NOT NULL,
    new_basic DECIMAL(12,2) NOT NULL,
    effective_date VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

