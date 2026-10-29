# Faculty Viva Questions & Answers: Employee Payroll System (JV-09)

### Q1: What is the primary purpose and scope of the Employee Payroll System?
**Answer**: The system automates end-to-end human resource salary computations, statutory tax/provident deductions, attendance-based proration, appraisal history tracking, and monthly payslip generation for enterprise employees.

### Q2: How does the system handle wage proration for employees with unpaid leaves?
**Answer**: The system computes an attendance ratio:
$$\text{Attendance Ratio} = \frac{\text{Total Working Days} - \text{Unpaid Leaves}}{\text{Total Working Days}}$$
This ratio is multiplied by the employee's contracted basic salary to compute `Basic Earned`. Subsequent percentage allowances (such as HRA and DA) and deductions (such as EPF) are calculated strictly on `Basic Earned`, ensuring fair prorated compensation.

### Q3: What statutory deductions are implemented in the project?
**Answer**:
1. **EPF (Employee Provident Fund)**: 12% of Basic Earned.
2. **ESI (Employee State Insurance)**: 0.75% of Gross Earnings, dynamically applied only if gross monthly salary does not exceed ₹21,000.
3. **Professional Tax (PT)**: Statutory local tax of ₹200.00 per month.
4. **TDS (Tax Deducted at Source)**: Tiered monthly tax based on annualized gross income slabs.

### Q4: Why is ESI conditional on gross salary?
**Answer**: Under statutory Indian labor laws, the Employee State Insurance Act mandates coverage only for employees whose monthly gross remuneration is at or below the wage ceiling of ₹21,000. For employees earning above ₹21,000, ESI is ₹0.00.

### Q5: How is data consistency maintained during a salary revision?
**Answer**: The system uses JDBC transaction boundaries (`conn.setAutoCommit(false)`). Both updating the `basic_salary` in the `employees` table and inserting an audit record into the `salary_revisions` table are executed within a single transaction. If either operation fails, `conn.rollback()` is invoked, preventing orphaned records or unrecorded pay raises.

### Q6: How does the application avoid SQL injection attacks?
**Answer**: All database interactions use `PreparedStatement` with parameterized placeholders (`?`). Values are bound using typed setters (`setString`, `setInt`, `setDouble`), preventing user-supplied strings from executing as SQL commands.

### Q7: Why is SQLite used for academic evaluation, and how can the system use MySQL?
**Answer**: SQLite provides zero-configuration, zero-installation embedded storage bundled directly via `lib/sqlite-jdbc.jar`, allowing the project to compile and run immediately on any machine with JDK 17+. For production MySQL deployments, `sql/schema_mysql.sql` is provided, and the connection URL in `DatabaseManager.java` can simply point to MySQL.

### Q8: What design pattern is used in the architecture?
**Answer**: The **Layered Architecture Pattern (Model-DAO-Service-View)**:
- **Model**: `Employee`, `PayrollRecord`, `SalaryRevision`
- **DAO**: `DatabaseManager`
- **Service**: `PayrollService` (contains all formulas and business logic)
- **View / Presentation**: `PayrollFrame` (Swing GUI) and `Main` (CLI)

### Q9: How are duplicate employee codes prevented?
**Answer**: The `employees` table schema enforces a `UNIQUE` constraint on the `emp_code` column (`emp_code TEXT NOT NULL UNIQUE`). Furthermore, the application code normalizes all codes to uppercase before querying or inserting.

### Q10: How does the application handle headless environments without a graphical display?
**Answer**: The application includes a fallback detection mechanism in `Main.java` and supports the `--cli` argument. If graphical rendering fails (e.g., in SSH sessions, Docker containers, or headless Linux servers), it launches an interactive command-line interface.

### Q11: How is floating-point drift avoided during currency calculations?
**Answer**: Every calculation step rounds numbers to two decimal places using `Math.round(value * 100.0) / 100.0`. In production enterprise banking, `BigDecimal` with `RoundingMode.HALF_UP` is typically used; here `Math.round` provides standard two-decimal precision.

### Q12: How are generated payslips exported?
**Answer**: The `PayrollService.exportPayslipToFile` method takes the processed `PayrollRecord` and `Employee` objects, formats a 90-character wide ASCII payslip document with boxed borders, and writes it directly to the `exports/` folder as `PAYSLIP_<SLIP_NUMBER>.txt`.

### Q13: What checks prevent impossible input values?
**Answer**:
- Unpaid leaves cannot be negative or exceed total working days.
- Basic salary must be greater than zero.
- Employee email must match the standard regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`.
- Employee name and code cannot be blank.

### Q14: How does the department analytics feature work?
**Answer**: The system executes an SQL aggregation query:
```sql
SELECT department,
       COUNT(DISTINCT emp_code) AS employee_count,
       SUM(gross_earnings) AS total_gross,
       SUM(net_salary) AS total_net,
       AVG(net_salary) AS avg_net
FROM payroll_records
GROUP BY department
ORDER BY total_net DESC
```
This produces instant departmental financial metrics without loading individual rows into memory.

### Q15: How were the automated tests constructed?
**Answer**: `TestRunner.java` contains 66 self-contained assertions that initialize an isolated in-memory test database, create test employees, test edge-case validations, verify statutory formulas, check salary revisions, and validate file export operations.

