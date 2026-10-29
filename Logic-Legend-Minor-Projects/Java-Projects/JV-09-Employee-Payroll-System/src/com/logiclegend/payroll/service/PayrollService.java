package com.logiclegend.payroll.service;

import com.logiclegend.payroll.dao.DatabaseManager;
import com.logiclegend.payroll.model.Employee;
import com.logiclegend.payroll.model.PayrollRecord;
import com.logiclegend.payroll.model.SalaryRevision;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Enterprise HR & Payroll Service Layer managing employee profiles,
 * attendance-prorated allowances, statutory deductions (EPF, ESI, TDS, PT),
 * salary revisions, payslips, and department financial summaries.
 */
public class PayrollService {
    public static final double HRA_RATE = 0.40;          // 40% of Basic
    public static final double DA_RATE = 0.20;           // 20% of Basic
    public static final double EPF_RATE = 0.12;          // 12% of Basic
    public static final double CONVEYANCE_ALLOWANCE = 2000.00;
    public static final double PROFESSIONAL_TAX = 200.00;

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final DatabaseManager dbManager;

    public PayrollService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------------------
    // EMPLOYEE MANAGEMENT & REVISIONS
    // -------------------------------------------------------------------------

    public Employee createEmployee(String empCode, String name, String designation, String department,
                                   String email, String phone, String bankAccount, String panNumber,
                                   String dateOfJoining, double basicSalary) throws SQLException {
        if (empCode == null || empCode.trim().isEmpty()) throw new IllegalArgumentException("Employee code is required.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Employee name is required.");
        if (basicSalary <= 0) throw new IllegalArgumentException("Basic salary must be greater than zero.");
        if (!EMAIL_REGEX.matcher(email != null ? email.trim() : "").matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        String sql = """
            INSERT INTO employees (emp_code, name, designation, department, email, phone, bank_account, pan_number, date_of_joining, basic_salary, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
        """;
        int empId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empCode.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.setString(3, designation.trim());
            ps.setString(4, department.trim());
            ps.setString(5, email.trim().toLowerCase());
            ps.setString(6, phone != null ? phone.trim() : "");
            ps.setString(7, bankAccount != null ? bankAccount.trim() : "");
            ps.setString(8, panNumber != null ? panNumber.trim().toUpperCase() : "");
            ps.setString(9, dateOfJoining != null ? dateOfJoining.trim() : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ps.setDouble(10, basicSalary);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                empId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getEmployeeById(empId);
    }

    public boolean updateEmployee(int id, String name, String designation, String department,
                                  String email, String phone, String bankAccount, String panNumber,
                                  double basicSalary, String status) throws SQLException {
        String sql = """
            UPDATE employees
            SET name = ?, designation = ?, department = ?, email = ?, phone = ?, bank_account = ?, pan_number = ?, basic_salary = ?, status = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, designation.trim());
            ps.setString(3, department.trim());
            ps.setString(4, email.trim().toLowerCase());
            ps.setString(5, phone.trim());
            ps.setString(6, bankAccount.trim());
            ps.setString(7, panNumber.trim().toUpperCase());
            ps.setDouble(8, basicSalary);
            ps.setString(9, status != null ? status.toUpperCase() : "ACTIVE");
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean reviseSalary(int employeeId, double newBasic, String effectiveDate, String reason) throws SQLException {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) throw new IllegalArgumentException("Employee ID " + employeeId + " not found.");
        if (newBasic <= 0) throw new IllegalArgumentException("New basic salary must be greater than zero.");

        double oldBasic = emp.getBasicSalary();
        String updateEmpSql = "UPDATE employees SET basic_salary = ? WHERE id = ?";
        String insertRevSql = "INSERT INTO salary_revisions (employee_id, old_basic, new_basic, effective_date, reason) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateEmpSql)) {
                    ps.setDouble(1, newBasic);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }
                try (PreparedStatement psRev = conn.prepareStatement(insertRevSql)) {
                    psRev.setInt(1, employeeId);
                    psRev.setDouble(2, oldBasic);
                    psRev.setDouble(3, newBasic);
                    psRev.setString(4, effectiveDate != null ? effectiveDate : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    psRev.setString(5, reason != null ? reason : "Annual appraisal revision");
                    psRev.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Employee getEmployeeById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployee(rs);
            }
        }
        return null;
    }

    public Employee getEmployeeByCode(String code) throws SQLException {
        if (code == null) return null;
        String sql = "SELECT * FROM employees WHERE UPPER(emp_code) = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployee(rs);
            }
        }
        return null;
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapEmployee(rs));
        }
        return list;
    }

    public List<Employee> searchEmployees(String keyword, String department) throws SQLException {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM employees WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (UPPER(emp_code) LIKE ? OR UPPER(name) LIKE ? OR UPPER(designation) LIKE ?) ");
            String q = "%" + keyword.trim().toUpperCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
            sql.append("AND department = ? ");
            params.add(department.trim());
        }

        sql.append("ORDER BY name ASC");
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEmployee(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // MONTHLY PAYROLL PROCESSING & DEDUCTIONS
    // -------------------------------------------------------------------------

    public PayrollRecord processPayroll(int employeeId, String monthYear,
                                        int totalWorkingDays, int unpaidLeaves,
                                        double specialAllowanceBonus) throws SQLException {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) throw new IllegalArgumentException("Employee not found.");
        if (totalWorkingDays <= 0) totalWorkingDays = 30;
        if (unpaidLeaves < 0 || unpaidLeaves > totalWorkingDays) {
            throw new IllegalArgumentException("Unpaid leaves cannot exceed total working days.");
        }

        int daysWorked = totalWorkingDays - unpaidLeaves;
        double attendanceRatio = (double) daysWorked / (double) totalWorkingDays;

        // Earnings
        double basicEarned = Math.round(emp.getBasicSalary() * attendanceRatio * 100.0) / 100.0;
        double hra = Math.round(basicEarned * HRA_RATE * 100.0) / 100.0;
        double da = Math.round(basicEarned * DA_RATE * 100.0) / 100.0;
        double conveyance = CONVEYANCE_ALLOWANCE;
        double specialAllowance = specialAllowanceBonus >= 0 ? specialAllowanceBonus : 0.0;
        double grossEarnings = Math.round((basicEarned + hra + da + conveyance + specialAllowance) * 100.0) / 100.0;

        // Deductions
        double epf = Math.round(basicEarned * EPF_RATE * 100.0) / 100.0;
        double esi = grossEarnings <= 21000.0 ? Math.round(grossEarnings * 0.0075 * 100.0) / 100.0 : 0.0;
        double profTax = PROFESSIONAL_TAX;
        double tds = computeMonthlyTds(grossEarnings);
        double totalDeductions = Math.round((epf + esi + profTax + tds) * 100.0) / 100.0;

        // Net Pay
        double netSalary = Math.round((grossEarnings - totalDeductions) * 100.0) / 100.0;

        String slipNumber = "PAY-" + monthYear.replace("-", "") + "-" + emp.getEmpCode();

        String sql = """
            INSERT INTO payroll_records (slip_number, employee_id, emp_code, emp_name, department, designation,
                                         month_year, total_working_days, days_worked, unpaid_leaves,
                                         basic_earned, hra, da, conveyance, special_allowance, gross_earnings,
                                         epf, esi, professional_tax, tds, total_deductions, net_salary)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int recordId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, slipNumber);
            ps.setInt(2, emp.getId());
            ps.setString(3, emp.getEmpCode());
            ps.setString(4, emp.getName());
            ps.setString(5, emp.getDepartment());
            ps.setString(6, emp.getDesignation());
            ps.setString(7, monthYear);
            ps.setInt(8, totalWorkingDays);
            ps.setInt(9, daysWorked);
            ps.setInt(10, unpaidLeaves);
            ps.setDouble(11, basicEarned);
            ps.setDouble(12, hra);
            ps.setDouble(13, da);
            ps.setDouble(14, conveyance);
            ps.setDouble(15, specialAllowance);
            ps.setDouble(16, grossEarnings);
            ps.setDouble(17, epf);
            ps.setDouble(18, esi);
            ps.setDouble(19, profTax);
            ps.setDouble(20, tds);
            ps.setDouble(21, totalDeductions);
            ps.setDouble(22, netSalary);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                recordId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getPayrollRecordById(recordId);
    }

    public PayrollRecord getPayrollRecordById(int id) throws SQLException {
        String sql = "SELECT * FROM payroll_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayrollRecord(rs);
            }
        }
        return null;
    }

    public PayrollRecord getPayrollRecordBySlipNumber(String slipNumber) throws SQLException {
        if (slipNumber == null) return null;
        String sql = "SELECT * FROM payroll_records WHERE slip_number = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayrollRecord(rs);
            }
        }
        return null;
    }

    public List<PayrollRecord> getAllPayrollRecords(int limit) throws SQLException {
        List<PayrollRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_records ORDER BY id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPayrollRecord(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // PAYSLIP FORMATTING & EXPORT
    // -------------------------------------------------------------------------

    public String generatePayslipText(PayrollRecord p, Employee e) {
        if (p == null || e == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                     LOGIC LEGEND TECHNOLOGIES PRIVATE LIMITED\n");
        sb.append("                 Corporate Tech Park, Outer Ring Road, Bangalore - 560103\n");
        sb.append("                             MONTHLY SALARY PAYSLIP\n");
        sb.append("========================================================================================\n");
        sb.append(String.format("Payslip Number: %-25s Pay Period:    %s\n", p.getSlipNumber(), p.getMonthYear()));
        sb.append(String.format("Employee ID:    %-25s Full Name:     %s\n", p.getEmpCode(), p.getEmpName()));
        sb.append(String.format("Designation:    %-25s Department:    %s\n", p.getDesignation(), p.getDepartment()));
        sb.append(String.format("Bank Account:   %-25s PAN Number:    %s\n", e.getBankAccount(), e.getPanNumber()));
        sb.append(String.format("Total Days:     %-25d Days Worked:   %d (Unpaid Leaves: %d)\n", p.getTotalWorkingDays(), p.getDaysWorked(), p.getUnpaidLeaves()));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-42s | %-42s\n", "EARNINGS & ALLOWANCES", "STATUTORY DEDUCTIONS"));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-28s: %,12.2f | %-28s: %,12.2f\n", "Basic Earned", p.getBasicEarned(), "Provident Fund (EPF 12%)", p.getEpf()));
        sb.append(String.format("%-28s: %,12.2f | %-28s: %,12.2f\n", "House Rent Allowance (HRA)", p.getHra(), "ESI Contribution", p.getEsi()));
        sb.append(String.format("%-28s: %,12.2f | %-28s: %,12.2f\n", "Dearness Allowance (DA)", p.getDa(), "Professional Tax (PT)", p.getProfessionalTax()));
        sb.append(String.format("%-28s: %,12.2f | %-28s: %,12.2f\n", "Conveyance Allowance", p.getConveyance(), "Tax Deducted (TDS)", p.getTds()));
        sb.append(String.format("%-28s: %,12.2f | %-28s: %12s\n", "Special Bonus Allowance", p.getSpecialAllowance(), "", ""));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-28s: %,12.2f | %-28s: %,12.2f\n", "GROSS EARNINGS", p.getGrossEarnings(), "TOTAL DEDUCTIONS", p.getTotalDeductions()));
        sb.append("========================================================================================\n");
        sb.append(String.format("                          NET SALARY PAYABLE: INR %,12.2f\n", p.getNetSalary()));
        sb.append("========================================================================================\n");
        sb.append("  Payment Mode: Direct Bank Credit | This is a computer-generated official payroll slip.\n");
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public File exportPayslipToFile(PayrollRecord p, Employee e, String targetDirectory) throws IOException {
        File dir = new File(targetDirectory != null ? targetDirectory : "exports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "PAYSLIP_" + p.getSlipNumber() + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generatePayslipText(p, e));
        }
        return file;
    }

    // -------------------------------------------------------------------------
    // DEPARTMENT & FINANCIAL ANALYTICS
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getDepartmentSalarySummary() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT department,
                   COUNT(DISTINCT emp_code) AS employee_count,
                   COALESCE(SUM(gross_earnings), 0.0) AS total_gross,
                   COALESCE(SUM(total_deductions), 0.0) AS total_deductions,
                   COALESCE(SUM(net_salary), 0.0) AS total_net,
                   COALESCE(AVG(net_salary), 0.0) AS avg_net
            FROM payroll_records
            GROUP BY department
            ORDER BY total_net DESC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("department", rs.getString("department"));
                map.put("employeeCount", rs.getInt("employee_count"));
                map.put("totalGross", rs.getDouble("total_gross"));
                map.put("totalDeductions", rs.getDouble("total_deductions"));
                map.put("totalNet", rs.getDouble("total_net"));
                map.put("avgNet", Math.round(rs.getDouble("avg_net") * 100.0) / 100.0);
                list.add(map);
            }
        }
        return list;
    }

    public Map<String, Object> getCompanyPayrollSummary() throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();
        String sql = """
            SELECT COUNT(*) AS total_slips,
                   COALESCE(SUM(gross_earnings), 0.0) AS total_gross,
                   COALESCE(SUM(total_deductions), 0.0) AS total_deductions,
                   COALESCE(SUM(net_salary), 0.0) AS total_net
            FROM payroll_records
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                summary.put("totalSlips", rs.getInt("total_slips"));
                summary.put("totalGross", rs.getDouble("total_gross"));
                summary.put("totalDeductions", rs.getDouble("total_deductions"));
                summary.put("totalNet", rs.getDouble("total_net"));
            }
        }
        return summary;
    }

    // -------------------------------------------------------------------------
    // HELPERS & SEED DATA
    // -------------------------------------------------------------------------

    private double computeMonthlyTds(double monthlyGross) {
        double annualized = monthlyGross * 12.0;
        if (annualized <= 300000.0) return 0.0;
        if (annualized <= 600000.0) return Math.round(((annualized - 300000.0) * 0.05 / 12.0) * 100.0) / 100.0;
        if (annualized <= 1200000.0) return Math.round(((15000.0 + (annualized - 600000.0) * 0.10) / 12.0) * 100.0) / 100.0;
        return Math.round(((75000.0 + (annualized - 1200000.0) * 0.15) / 12.0) * 100.0) / 100.0;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setEmpCode(rs.getString("emp_code"));
        e.setName(rs.getString("name"));
        e.setDesignation(rs.getString("designation"));
        e.setDepartment(rs.getString("department"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setBankAccount(rs.getString("bank_account"));
        e.setPanNumber(rs.getString("pan_number"));
        e.setDateOfJoining(rs.getString("date_of_joining"));
        e.setBasicSalary(rs.getDouble("basic_salary"));
        e.setStatus(rs.getString("status"));
        e.setCreatedAt(rs.getString("created_at"));
        return e;
    }

    private PayrollRecord mapPayrollRecord(ResultSet rs) throws SQLException {
        PayrollRecord p = new PayrollRecord();
        p.setId(rs.getInt("id"));
        p.setSlipNumber(rs.getString("slip_number"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setEmpCode(rs.getString("emp_code"));
        p.setEmpName(rs.getString("emp_name"));
        p.setDepartment(rs.getString("department"));
        p.setDesignation(rs.getString("designation"));
        p.setMonthYear(rs.getString("month_year"));
        p.setTotalWorkingDays(rs.getInt("total_working_days"));
        p.setDaysWorked(rs.getInt("days_worked"));
        p.setUnpaidLeaves(rs.getInt("unpaid_leaves"));
        p.setBasicEarned(rs.getDouble("basic_earned"));
        p.setHra(rs.getDouble("hra"));
        p.setDa(rs.getDouble("da"));
        p.setConveyance(rs.getDouble("conveyance"));
        p.setSpecialAllowance(rs.getDouble("special_allowance"));
        p.setGrossEarnings(rs.getDouble("gross_earnings"));
        p.setEpf(rs.getDouble("epf"));
        p.setEsi(rs.getDouble("esi"));
        p.setProfessionalTax(rs.getDouble("professional_tax"));
        p.setTds(rs.getDouble("tds"));
        p.setTotalDeductions(rs.getDouble("total_deductions"));
        p.setNetSalary(rs.getDouble("net_salary"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    public void seedDemoData() throws SQLException {
        if (!getAllEmployees().isEmpty()) return;

        Employee e1 = createEmployee("EMP-1001", "Vikramaditya Rao", "Principal Architect", "Engineering", "vikram@logiclegend.com", "+91 9876543201", "HDFC00012345678", "ABCDE1234F", "2022-01-15", 95000.0);
        Employee e2 = createEmployee("EMP-1002", "Ananya Deshpande", "Senior Software Engineer", "Engineering", "ananya@logiclegend.com", "+91 9876543202", "ICIC00098765432", "PQRSF5678G", "2023-04-10", 65000.0);
        Employee e3 = createEmployee("EMP-1003", "Suresh Menon", "HR Manager", "Human Resources", "suresh@logiclegend.com", "+91 9876543203", "SBIN00045678901", "LMNOP9012H", "2021-08-01", 50000.0);
        Employee e4 = createEmployee("EMP-1004", "Fatima Zahra", "Marketing Specialist", "Marketing", "fatima@logiclegend.com", "+91 9876543204", "UTIB00023456789", "ZXCVB3456K", "2024-02-18", 40000.0);

        String currentMonth = DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDateTime.now());
        processPayroll(e1.getId(), currentMonth, 30, 0, 5000.0);
        processPayroll(e2.getId(), currentMonth, 30, 2, 2000.0);
        processPayroll(e3.getId(), currentMonth, 30, 1, 0.0);
    }
}

