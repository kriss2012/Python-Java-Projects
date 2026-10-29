package tests;

import com.logiclegend.payroll.dao.DatabaseManager;
import com.logiclegend.payroll.model.Employee;
import com.logiclegend.payroll.model.PayrollRecord;
import com.logiclegend.payroll.service.PayrollService;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Automated Test Suite for JV-09 Employee Payroll System.
 * Verifies employee management, salary revisions, attendance proration,
 * statutory deductions (EPF, ESI, TDS, PT), payslip generation and exports.
 */
public class TestRunner {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %-55s%n", testName);
        } else {
            System.out.printf("  [FAIL] %-55s%n", testName);
            new Exception("Assertion failure: " + testName).printStackTrace(System.out);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        testsTotal++;
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (match) {
            testsPassed++;
            System.out.printf("  [PASS] %-55s%n", testName);
        } else {
            System.out.printf("  [FAIL] %-55s (Expected: %s, Actual: %s)%n", testName, expected, actual);
            new Exception("Assertion failure: " + testName).printStackTrace(System.out);
        }
    }

    private static void assertDoubleEquals(String testName, double expected, double actual, double delta) {
        testsTotal++;
        if (Math.abs(expected - actual) <= delta) {
            testsPassed++;
            System.out.printf("  [PASS] %-55s%n", testName);
        } else {
            System.out.printf("  [FAIL] %-55s (Expected: %.2f, Actual: %.2f)%n", testName, expected, actual);
            new Exception("Assertion failure: " + testName).printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND TEST SUITE: JV-09 EMPLOYEE PAYROLL SYSTEM");
        System.out.println("=================================================================\n");

        String testDb = "data/test_payroll.db";
        File f = new File(testDb);
        if (f.exists()) f.delete();

        DatabaseManager db = new DatabaseManager(testDb);
        PayrollService service = new PayrollService(db);

        // -------------------------------------------------------------
        // Group 1: Employee CRUD & Validations
        // -------------------------------------------------------------
        System.out.println("Group 1: Employee Management & Input Validation");
        try {
            Employee emp1 = service.createEmployee("EMP-2001", "Aarav Sharma", "Senior Backend Engineer",
                    "Engineering", "aarav@logiclegend.com", "+91 9876500001", "HDFC001234", "ABCDE1234F", "2023-01-15", 50000.0);
            assertTrue("Employee creation returns non-null record", emp1 != null);
            assertEquals("Employee code uppercase normalized", "EMP-2001", emp1.getEmpCode());
            assertEquals("Employee designation matches", "Senior Backend Engineer", emp1.getDesignation());
            assertDoubleEquals("Employee basic salary stored accurately", 50000.0, emp1.getBasicSalary(), 0.01);

            // Validation: Blank code
            boolean caughtBlankCode = false;
            try {
                service.createEmployee(" ", "John Doe", "Dev", "IT", "john@mail.com", "123", "B1", "PAN1", "2023-01-01", 30000);
            } catch (IllegalArgumentException e) {
                caughtBlankCode = true;
            }
            assertTrue("Rejects empty employee code", caughtBlankCode);

            // Validation: Invalid email format
            boolean caughtBadEmail = false;
            try {
                service.createEmployee("EMP-2002", "Jane Doe", "Dev", "IT", "not-an-email", "123", "B1", "PAN1", "2023-01-01", 30000);
            } catch (IllegalArgumentException e) {
                caughtBadEmail = true;
            }
            assertTrue("Rejects invalid email format", caughtBadEmail);

            // Validation: Negative or zero salary
            boolean caughtBadSalary = false;
            try {
                service.createEmployee("EMP-2003", "Bob Smith", "Dev", "IT", "bob@mail.com", "123", "B1", "PAN1", "2023-01-01", -500.0);
            } catch (IllegalArgumentException e) {
                caughtBadSalary = true;
            }
            assertTrue("Rejects non-positive basic salary", caughtBadSalary);

            // Duplicate code rejection
            boolean caughtDuplicate = false;
            try {
                service.createEmployee("EMP-2001", "Duplicate Code", "Dev", "IT", "dup@mail.com", "123", "B1", "PAN1", "2023-01-01", 40000);
            } catch (Exception e) {
                caughtDuplicate = true;
            }
            assertTrue("Enforces unique constraint on employee code", caughtDuplicate);

            // Lookup by ID & Code
            Employee lookup = service.getEmployeeById(emp1.getId());
            assertEquals("Lookup by ID matches employee name", "Aarav Sharma", lookup.getName());
            Employee codeLookup = service.getEmployeeByCode("emp-2001");
            assertEquals("Case-insensitive lookup by code matches ID", emp1.getId(), codeLookup.getId());

            // Update Employee
            boolean updated = service.updateEmployee(emp1.getId(), "Aarav K. Sharma", "Lead Architect", "Engineering",
                    "aarav.sharma@logiclegend.com", "+91 9876500001", "HDFC001234", "ABCDE1234F", 60000.0, "ACTIVE");
            assertTrue("Employee profile update executes successfully", updated);
            Employee reloaded = service.getEmployeeById(emp1.getId());
            assertEquals("Updated name persisted", "Aarav K. Sharma", reloaded.getName());
            assertEquals("Updated designation persisted", "Lead Architect", reloaded.getDesignation());
            assertDoubleEquals("Updated basic salary persisted", 60000.0, reloaded.getBasicSalary(), 0.01);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 2: Salary Revision & Audit History
        // -------------------------------------------------------------
        System.out.println("\nGroup 2: Salary Revision & Transactional Audit");
        try {
            Employee emp = service.getEmployeeByCode("EMP-2001");
            boolean revised = service.reviseSalary(emp.getId(), 75000.0, "2024-04-01", "Annual Performance Merit Appraisal");
            assertTrue("Salary revision transaction succeeds", revised);

            Employee postRevise = service.getEmployeeById(emp.getId());
            assertDoubleEquals("Employee basic updated to 75,000", 75000.0, postRevise.getBasicSalary(), 0.01);

            // Revisions table audit
            try (Connection conn = db.getConnection();
                 Statement st = conn.createStatement();
                 var rs = st.executeQuery("SELECT old_basic, new_basic, reason FROM salary_revisions WHERE employee_id = " + emp.getId())) {
                assertTrue("Audit record created in salary_revisions", rs.next());
                assertDoubleEquals("Audit records prior basic 60,000", 60000.0, rs.getDouble("old_basic"), 0.01);
                assertDoubleEquals("Audit records revised basic 75,000", 75000.0, rs.getDouble("new_basic"), 0.01);
                assertEquals("Audit records revision reason", "Annual Performance Merit Appraisal", rs.getString("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 3: Payroll Calculation & Full Attendance
        // -------------------------------------------------------------
        System.out.println("\nGroup 3: Full Attendance Payroll Calculation (Statutory Formulas)");
        try {
            Employee emp = service.getEmployeeByCode("EMP-2001"); // Basic: 75,000
            PayrollRecord pr = service.processPayroll(emp.getId(), "2026-10", 30, 0, 5000.0);

            assertTrue("Payroll record successfully created", pr != null);
            assertEquals("Slip number format contains month and code", "PAY-202610-EMP-2001", pr.getSlipNumber());
            assertEquals("Days worked equals total working days", 30, pr.getDaysWorked());
            assertEquals("Zero unpaid leaves recorded", 0, pr.getUnpaidLeaves());

            // Expected calculations:
            // basicEarned = 75,000
            // HRA = 40% of 75,000 = 30,000
            // DA = 20% of 75,000 = 15,000
            // Conveyance = 2,000
            // Bonus = 5,000
            // Gross = 75000 + 30000 + 15000 + 2000 + 5000 = 127,000
            assertDoubleEquals("Basic earned is 75,000", 75000.0, pr.getBasicEarned(), 0.01);
            assertDoubleEquals("HRA is 40% (30,000)", 30000.0, pr.getHra(), 0.01);
            assertDoubleEquals("DA is 20% (15,000)", 15000.0, pr.getDa(), 0.01);
            assertDoubleEquals("Conveyance is standard 2,000", 2000.0, pr.getConveyance(), 0.01);
            assertDoubleEquals("Special bonus matches 5,000", 5000.0, pr.getSpecialAllowance(), 0.01);
            assertDoubleEquals("Gross earnings match 127,000", 127000.0, pr.getGrossEarnings(), 0.01);

            // Deductions:
            // EPF = 12% of 75,000 = 9,000
            // ESI = 0.0 (gross > 21,000)
            // Prof Tax = 200.00
            // TDS: annualized = 127000 * 12 = 1,524,000. (>1.2M: 75000 + (1524000-1200000)*0.15 = 75000 + 48600 = 123,600 / 12 = 10,300)
            // Total deductions = 9000 + 0 + 200 + 10300 = 19,500
            // Net salary = 127000 - 19500 = 107,500
            assertDoubleEquals("EPF deduction is 12% (9,000)", 9000.0, pr.getEpf(), 0.01);
            assertDoubleEquals("ESI is zero for gross > 21,000", 0.0, pr.getEsi(), 0.01);
            assertDoubleEquals("Professional tax is standard 200", 200.0, pr.getProfessionalTax(), 0.01);
            assertDoubleEquals("TDS monthly tax deduction accurate", 10300.0, pr.getTds(), 1.0);
            assertDoubleEquals("Total deductions match sum of components", 19500.0, pr.getTotalDeductions(), 1.0);
            assertDoubleEquals("Net salary equals gross minus deductions", 107500.0, pr.getNetSalary(), 1.0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 4: Attendance Proration & ESI Eligibility
        // -------------------------------------------------------------
        System.out.println("\nGroup 4: Attendance Proration & ESI Applicability (< INR 21,000)");
        try {
            // Employee with lower basic salary eligible for ESI
            Employee intern = service.createEmployee("EMP-2002", "Pooja Hegde", "Junior Associate",
                    "Operations", "pooja@logiclegend.com", "+91 9876500002", "ICIC001234", "XYZAB7890C", "2024-01-01", 10000.0);

            // 5 unpaid leaves out of 25 working days -> 20 days worked (ratio = 20/25 = 0.8)
            PayrollRecord prProrated = service.processPayroll(intern.getId(), "2026-10", 25, 5, 0.0);

            assertEquals("Days worked is 20", 20, prProrated.getDaysWorked());
            assertEquals("Unpaid leaves recorded as 5", 5, prProrated.getUnpaidLeaves());
            assertDoubleEquals("Prorated basic earned is 8,000 (0.8 * 10,000)", 8000.0, prProrated.getBasicEarned(), 0.01);
            assertDoubleEquals("Prorated HRA is 40% of 8,000 (3,200)", 3200.0, prProrated.getHra(), 0.01);
            assertDoubleEquals("Prorated DA is 20% of 8,000 (1,600)", 1600.0, prProrated.getDa(), 0.01);
            // Gross = 8000 + 3200 + 1600 + 2000 = 14,800
            assertDoubleEquals("Gross earnings match 14,800", 14800.0, prProrated.getGrossEarnings(), 0.01);

            // ESI applicable since gross (14,800) <= 21,000: 14800 * 0.0075 = 111.00
            assertDoubleEquals("ESI is 0.75% of gross earnings (111.00)", 111.0, prProrated.getEsi(), 0.01);
            assertDoubleEquals("EPF is 12% of basic earned (960.00)", 960.0, prProrated.getEpf(), 0.01);
            assertDoubleEquals("Professional tax standard 200", 200.0, prProrated.getProfessionalTax(), 0.01);
            assertDoubleEquals("TDS is zero for annualized under 300,000", 0.0, prProrated.getTds(), 0.01);

            // Total deductions: 960 + 111 + 200 = 1271.00
            assertDoubleEquals("Prorated total deductions match 1,271", 1271.0, prProrated.getTotalDeductions(), 0.01);
            // Net salary: 14800 - 1271 = 13,529.00
            assertDoubleEquals("Prorated net salary matches 13,529", 13529.0, prProrated.getNetSalary(), 0.01);

            // Validation: Leaves exceeding total days throws exception
            boolean caughtExcessLeaves = false;
            try {
                service.processPayroll(intern.getId(), "2026-10", 20, 25, 0.0);
            } catch (IllegalArgumentException e) {
                caughtExcessLeaves = true;
            }
            assertTrue("Rejects unpaid leaves exceeding total working days", caughtExcessLeaves);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 5: Payslip Text Formatting & File Export
        // -------------------------------------------------------------
        System.out.println("\nGroup 5: Payslip Document Generation & Export");
        try {
            Employee emp = service.getEmployeeByCode("EMP-2001");
            PayrollRecord pr = service.getPayrollRecordBySlipNumber("PAY-202610-EMP-2001");

            String slipText = service.generatePayslipText(pr, emp);
            assertTrue("Payslip text contains company header", slipText.contains("LOGIC LEGEND TECHNOLOGIES"));
            assertTrue("Payslip contains slip number", slipText.contains("PAY-202610-EMP-2001"));
            assertTrue("Payslip contains employee name", slipText.contains("Aarav K. Sharma"));
            assertTrue("Payslip contains PAN number", slipText.contains("ABCDE1234F"));
            assertTrue("Payslip contains Gross Earnings line", slipText.contains("GROSS EARNINGS"));
            assertTrue("Payslip contains NET SALARY PAYABLE line", slipText.contains("NET SALARY PAYABLE"));

            // Export to file
            File exported = service.exportPayslipToFile(pr, emp, "exports/test_slips");
            assertTrue("Exported payslip file exists on disk", exported.exists() && exported.length() > 0);
            assertEquals("Export file name format is correct", "PAYSLIP_PAY-202610-EMP-2001.txt", exported.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 6: Department & Financial Analytics
        // -------------------------------------------------------------
        System.out.println("\nGroup 6: Financial Summaries & Department Analytics");
        try {
            List<Map<String, Object>> deptSummaries = service.getDepartmentSalarySummary();
            assertTrue("Department summaries return non-empty list", !deptSummaries.isEmpty());

            Map<String, Object> engDept = deptSummaries.stream()
                    .filter(d -> "Engineering".equalsIgnoreCase((String) d.get("department")))
                    .findFirst().orElse(null);
            assertTrue("Engineering department summary found", engDept != null);
            assertEquals("Engineering distinct employee count is 1", 1, engDept.get("employeeCount"));
            assertDoubleEquals("Engineering total net salary matches slip", 107500.0, (Double) engDept.get("totalNet"), 1.0);

            Map<String, Object> companySummary = service.getCompanyPayrollSummary();
            assertEquals("Company total slips processed is 2", 2, companySummary.get("totalSlips"));
            double totalGross = (Double) companySummary.get("totalGross");
            assertDoubleEquals("Company total gross equals sum (127000 + 14800 = 141,800)", 141800.0, totalGross, 1.0);

            // Search filter by keyword and department
            List<Employee> engList = service.searchEmployees("Aarav", "Engineering");
            assertEquals("Keyword search by name in Engineering returns 1 record", 1, engList.size());

            List<Employee> allEng = service.searchEmployees("", "Engineering");
            assertEquals("Department filter returns 1 employee", 1, allEng.size());

            List<Employee> ops = service.searchEmployees("", "Operations");
            assertEquals("Department filter for Operations returns 1 employee", 1, ops.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // FINAL SUMMARY
        // -------------------------------------------------------------
        System.out.println("\n=================================================================");
        System.out.printf("  TESTS FINISHED: %d / %d PASSED (%.1f%%)%n",
                testsPassed, testsTotal, (100.0 * testsPassed / testsTotal));
        System.out.println("=================================================================");

        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL JV-09 PAYROLL SYSTEM TESTS PASSED SUCCESSFULLY! <<<");
            System.exit(0);
        } else {
            System.err.println(">>> SOME TESTS FAILED! <<<");
            System.exit(1);
        }
    }
}

