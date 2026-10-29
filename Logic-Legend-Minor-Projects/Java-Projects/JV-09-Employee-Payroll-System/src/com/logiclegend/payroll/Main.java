package com.logiclegend.payroll;

import com.logiclegend.payroll.dao.DatabaseManager;
import com.logiclegend.payroll.model.Employee;
import com.logiclegend.payroll.model.PayrollRecord;
import com.logiclegend.payroll.service.PayrollService;
import com.logiclegend.payroll.ui.PayrollFrame;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application launcher for JV-09 Employee Payroll System.
 * Supports modern Swing desktop GUI and interactive terminal CLI.
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        PayrollService service = new PayrollService(dbManager);

        try {
            service.seedDemoData();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding demo payroll data notice: " + e.getMessage());
        }

        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode) {
            runCli(service);
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    PayrollFrame frame = new PayrollFrame(service);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable. Running in terminal CLI mode.");
                runCli(service);
            }
        }
    }

    public static void runCli(PayrollService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-09: EMPLOYEE PAYROLL SYSTEM (CLI)");
        System.out.println("=================================================================");

        while (true) {
            System.out.println("\n[MENU]");
            System.out.println("  1. List All Employees in Master Directory");
            System.out.println("  2. Search Employee by Code / Name");
            System.out.println("  3. Onboard New Employee Profile");
            System.out.println("  4. Revise Employee Basic Salary (Appraisal)");
            System.out.println("  5. Process Monthly Payroll & Generate Payslip");
            System.out.println("  6. View Payslip by Slip Number");
            System.out.println("  7. Department-Wise Payroll Financial Summary");
            System.out.println("  0. Exit / Shutdown");
            System.out.print("Select an option (0-7): ");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (choice) {
                case "1":
                    listEmployeesCli(service);
                    break;
                case "2":
                    searchEmployeesCli(service, scanner);
                    break;
                case "3":
                    onboardEmployeeCli(service, scanner);
                    break;
                case "4":
                    reviseSalaryCli(service, scanner);
                    break;
                case "5":
                    processPayrollCli(service, scanner);
                    break;
                case "6":
                    viewPayslipCli(service, scanner);
                    break;
                case "7":
                    viewDepartmentSummaryCli(service);
                    break;
                case "0":
                    System.out.println("[INFO] Exiting Employee Payroll System. Goodbye!");
                    return;
                default:
                    System.out.println("[ERROR] Invalid choice. Choose 0 to 7.");
            }
        }
    }

    private static void listEmployeesCli(PayrollService service) {
        try {
            List<Employee> list = service.getAllEmployees();
            printEmployeeTable(list);
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch employees: " + e.getMessage());
        }
    }

    private static void searchEmployeesCli(PayrollService service, Scanner scanner) {
        System.out.print("Enter name, employee code, or designation to search: ");
        String query = scanner.nextLine().trim();
        try {
            List<Employee> list = service.searchEmployees(query, null);
            printEmployeeTable(list);
        } catch (SQLException e) {
            System.out.println("[ERROR] Search error: " + e.getMessage());
        }
    }

    private static void onboardEmployeeCli(PayrollService service, Scanner scanner) {
        try {
            System.out.print("Employee Code (e.g. EMP-2005): ");
            String code = scanner.nextLine().trim();
            System.out.print("Full Legal Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Designation: ");
            String desig = scanner.nextLine().trim();
            System.out.print("Department (e.g. Engineering, Sales, HR): ");
            String dept = scanner.nextLine().trim();
            System.out.print("Corporate Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Phone Contact: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Bank Account Details: ");
            String bank = scanner.nextLine().trim();
            System.out.print("PAN Number: ");
            String pan = scanner.nextLine().trim();
            System.out.print("Monthly Basic Salary (₹): ");
            double basic = Double.parseDouble(scanner.nextLine().trim());

            Employee emp = service.createEmployee(code, name, desig, dept, email, phone, bank, pan, null, basic);
            System.out.println("[SUCCESS] Onboarded Employee ID #" + emp.getId() + " - " + emp.getName() + " (" + emp.getEmpCode() + ")");
        } catch (Exception e) {
            System.out.println("[ERROR] Onboarding failed: " + e.getMessage());
        }
    }

    private static void reviseSalaryCli(PayrollService service, Scanner scanner) {
        try {
            System.out.print("Enter Employee Code to Revise: ");
            String code = scanner.nextLine().trim();
            Employee emp = service.getEmployeeByCode(code);
            if (emp == null) {
                System.out.println("[ERROR] Employee not found.");
                return;
            }

            System.out.printf("Current Basic Salary for %s: ₹%,.2f%n", emp.getName(), emp.getBasicSalary());
            System.out.print("Enter New Monthly Basic Salary (₹): ");
            double newBasic = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Reason / Appraisal Milestone: ");
            String reason = scanner.nextLine().trim();

            service.reviseSalary(emp.getId(), newBasic, null, reason);
            System.out.println("[SUCCESS] Basic salary revised to ₹" + newBasic + " for " + emp.getName());
        } catch (Exception e) {
            System.out.println("[ERROR] Salary revision failed: " + e.getMessage());
        }
    }

    private static void processPayrollCli(PayrollService service, Scanner scanner) {
        try {
            System.out.print("Enter Employee Code: ");
            String code = scanner.nextLine().trim();
            Employee emp = service.getEmployeeByCode(code);
            if (emp == null) {
                System.out.println("[ERROR] Employee not found.");
                return;
            }

            System.out.print("Pay Period Month (YYYY-MM, e.g. 2026-10): ");
            String month = scanner.nextLine().trim();
            if (month.isEmpty()) month = DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDateTime.now());

            System.out.print("Total Working Days in Month [30]: ");
            String daysStr = scanner.nextLine().trim();
            int totalDays = daysStr.isEmpty() ? 30 : Integer.parseInt(daysStr);

            System.out.print("Unpaid Leaves Taken [0]: ");
            String leavesStr = scanner.nextLine().trim();
            int unpaid = leavesStr.isEmpty() ? 0 : Integer.parseInt(leavesStr);

            System.out.print("Special Bonus / Performance Incentive (₹) [0]: ");
            String bonusStr = scanner.nextLine().trim();
            double bonus = bonusStr.isEmpty() ? 0.0 : Double.parseDouble(bonusStr);

            PayrollRecord rec = service.processPayroll(emp.getId(), month, totalDays, unpaid, bonus);
            File file = service.exportPayslipToFile(rec, emp, "exports");

            System.out.println("\n" + service.generatePayslipText(rec, emp));
            System.out.println("[SUCCESS] Official monthly payslip exported to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[ERROR] Payroll processing failed: " + e.getMessage());
        }
    }

    private static void viewPayslipCli(PayrollService service, Scanner scanner) {
        try {
            System.out.print("Enter Payslip Number (e.g. PAY-202610-EMP-1001): ");
            String slipNum = scanner.nextLine().trim();
            PayrollRecord rec = service.getPayrollRecordBySlipNumber(slipNum);
            if (rec == null) {
                System.out.println("[ERROR] Payslip record not found.");
                return;
            }
            Employee emp = service.getEmployeeById(rec.getEmployeeId());
            System.out.println("\n" + service.generatePayslipText(rec, emp));
        } catch (SQLException e) {
            System.out.println("[ERROR] Lookup failed: " + e.getMessage());
        }
    }

    private static void viewDepartmentSummaryCli(PayrollService service) {
        try {
            List<Map<String, Object>> list = service.getDepartmentSalarySummary();
            System.out.println("\n-----------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-22s | %-6s | %-16s | %-16s | %-16s | %-16s%n",
                    "Department", "Staff", "Gross Payroll", "Deductions", "Net Disbursed", "Avg Net Pay");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            for (Map<String, Object> map : list) {
                System.out.printf("%-22s | %-6d | ₹%,14.2f | ₹%,14.2f | ₹%,14.2f | ₹%,14.2f%n",
                        truncate((String) map.get("department"), 22),
                        map.get("employeeCount"),
                        map.get("totalGross"),
                        map.get("totalDeductions"),
                        map.get("totalNet"),
                        map.get("avgNet"));
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[ERROR] Summary failed: " + e.getMessage());
        }
    }

    private static void printEmployeeTable(List<Employee> list) {
        System.out.println("\n---------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-10s | %-24s | %-22s | %-16s | %-12s | %-10s%n",
                "ID", "Code", "Full Name", "Designation", "Department", "Basic Salary", "Status");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        for (Employee e : list) {
            System.out.printf("%-4d | %-10s | %-24s | %-22s | %-16s | ₹%,10.2f | %-10s%n",
                    e.getId(), e.getEmpCode(), truncate(e.getName(), 24),
                    truncate(e.getDesignation(), 22), truncate(e.getDepartment(), 16),
                    e.getBasicSalary(), e.getStatus());
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

