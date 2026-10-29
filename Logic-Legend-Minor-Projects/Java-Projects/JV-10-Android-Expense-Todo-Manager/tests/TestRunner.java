package tests;

import com.logiclegend.expensetodo.dao.BudgetDao;
import com.logiclegend.expensetodo.dao.DatabaseManager;
import com.logiclegend.expensetodo.dao.ExpenseDao;
import com.logiclegend.expensetodo.dao.TaskDao;
import com.logiclegend.expensetodo.model.*;
import com.logiclegend.expensetodo.repository.ExpenseRepository;
import com.logiclegend.expensetodo.repository.TaskRepository;
import com.logiclegend.expensetodo.service.ExpenseService;
import com.logiclegend.expensetodo.service.TaskService;

import java.io.File;
import java.util.List;

/**
 * Automated Test Suite for JV-10 Android Expense and To-Do Manager.
 * Validates Room-compatible SQLite schema, expense tracking, budget threshold alert logic,
 * task state machine, priority sorting, analytical breakdowns, and CSV export.
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
        System.out.println("  LOGIC LEGEND TEST SUITE: JV-10 ANDROID EXPENSE & TO-DO MANAGER");
        System.out.println("=================================================================\n");

        String testDb = "data/test_expensetodo.db";
        File f = new File(testDb);
        if (f.exists()) f.delete();

        DatabaseManager db = new DatabaseManager(testDb);
        ExpenseDao expenseDao = new ExpenseDao(db);
        BudgetDao budgetDao = new BudgetDao(db);
        TaskDao taskDao = new TaskDao(db);

        ExpenseRepository expenseRepo = new ExpenseRepository(expenseDao, budgetDao);
        TaskRepository taskRepo = new TaskRepository(taskDao);

        ExpenseService expenseService = new ExpenseService(expenseRepo);
        TaskService taskService = new TaskService(taskRepo);

        // -------------------------------------------------------------
        // Group 1: Expense CRUD & Validations
        // -------------------------------------------------------------
        System.out.println("Group 1: Expense Operations & Data Integrity");
        try {
            ExpenseEntity exp1 = expenseService.createExpense("Organic Grocery Store", 2500.0,
                    "Food & Dining", "2026-10-05", "UPI", "Vegetables & dairy");
            assertTrue("Expense created returns valid ID", exp1.getId() > 0);
            assertEquals("Title stored properly", "Organic Grocery Store", exp1.getTitle());
            assertDoubleEquals("Amount stored properly", 2500.0, exp1.getAmount(), 0.01);
            assertEquals("Category stored properly", "Food & Dining", exp1.getCategory());
            assertEquals("Payment method stored properly", "UPI", exp1.getPaymentMethod());

            // Validation: Empty title
            boolean caughtEmptyTitle = false;
            try {
                expenseService.createExpense("  ", 500.0, "Food", "2026-10-05", "Cash", null);
            } catch (IllegalArgumentException e) {
                caughtEmptyTitle = true;
            }
            assertTrue("Rejects empty expense title", caughtEmptyTitle);

            // Validation: Non-positive amount
            boolean caughtBadAmount = false;
            try {
                expenseService.createExpense("Bad Amount", -100.0, "Food", "2026-10-05", "Cash", null);
            } catch (IllegalArgumentException e) {
                caughtBadAmount = true;
            }
            assertTrue("Rejects non-positive expense amount", caughtBadAmount);

            // Update Expense
            boolean updated = expenseService.updateExpense(exp1.getId(), "Supermarket Grocery Store", 2800.0,
                    "Food & Dining", "2026-10-05", "Credit Card", "Updated notes");
            assertTrue("Expense update executed", updated);
            ExpenseEntity reloaded = expenseService.getExpenseById(exp1.getId());
            assertEquals("Updated title persisted", "Supermarket Grocery Store", reloaded.getTitle());
            assertDoubleEquals("Updated amount persisted", 2800.0, reloaded.getAmount(), 0.01);

            // Add second expense
            ExpenseEntity exp2 = expenseService.createExpense("Metro Train Card Recharge", 1000.0,
                    "Transportation", "2026-10-08", "UPI", "Transit pass");
            assertTrue("Second expense created", exp2.getId() > 0);

            // List by month
            List<ExpenseEntity> octList = expenseService.getExpensesByMonth("2026-10");
            assertEquals("Found 2 expenses in month 2026-10", 2, octList.size());

            // Monthly Total
            double totalSpent = expenseService.getTotalSpentInMonth("2026-10");
            assertDoubleEquals("Monthly total matches 3,800 (2800 + 1000)", 3800.0, totalSpent, 0.01);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 2: Budget Thresholds & Alert Levels
        // -------------------------------------------------------------
        System.out.println("\nGroup 2: Monthly Budgets & Threshold Alert Engine");
        try {
            // Set budgets
            boolean b1 = expenseService.setBudget("2026-10", "Food & Dining", 3000.0);
            assertTrue("Budget set successfully", b1);

            boolean b2 = expenseService.setBudget("2026-10", "Transportation", 2000.0);
            assertTrue("Transportation budget set", b2);

            // Test SAFE status: Transportation spent 1000 out of 2000 = 50%
            BudgetStatus transStatus = expenseService.checkBudgetStatus("2026-10", "Transportation");
            assertDoubleEquals("Transportation spent is 1000", 1000.0, transStatus.getTotalSpent(), 0.01);
            assertDoubleEquals("Transportation remaining is 1000", 1000.0, transStatus.getRemaining(), 0.01);
            assertDoubleEquals("Usage percent is 50.0%", 50.0, transStatus.getPercentageUsed(), 0.1);
            assertEquals("Status is SAFE (< 80%)", "SAFE", transStatus.getAlertStatus());
            assertTrue("isWarning is false", !transStatus.isWarning());
            assertTrue("isExceeded is false", !transStatus.isExceeded());

            // Test WARNING status: Food spent 2800 out of 3000 = 93.3% (>= 80% and < 100%)
            BudgetStatus foodStatus = expenseService.checkBudgetStatus("2026-10", "Food & Dining");
            assertDoubleEquals("Food spent is 2800", 2800.0, foodStatus.getTotalSpent(), 0.01);
            assertDoubleEquals("Usage percent is 93.3%", 93.3, foodStatus.getPercentageUsed(), 0.1);
            assertEquals("Status is WARNING (>= 80%)", "WARNING", foodStatus.getAlertStatus());
            assertTrue("isWarning is true", foodStatus.isWarning());
            assertTrue("isExceeded is false", !foodStatus.isExceeded());

            // Test EXCEEDED status: Add 500 to food -> 3300 out of 3000 = 110% (> 100%)
            expenseService.createExpense("Restaurant Dinner", 500.0, "Food & Dining", "2026-10-10", "Cash", null);
            BudgetStatus exceededStatus = expenseService.checkBudgetStatus("2026-10", "Food & Dining");
            assertDoubleEquals("Total food spent now 3300", 3300.0, exceededStatus.getTotalSpent(), 0.01);
            assertDoubleEquals("Remaining is -300", -300.0, exceededStatus.getRemaining(), 0.01);
            assertEquals("Status is EXCEEDED", "EXCEEDED", exceededStatus.getAlertStatus());
            assertTrue("isExceeded is true", exceededStatus.isExceeded());

            // Validation: Bad budget parameters
            boolean caughtBadBudget = false;
            try {
                expenseService.setBudget("2026-10", "Food & Dining", -50.0);
            } catch (IllegalArgumentException e) {
                caughtBadBudget = true;
            }
            assertTrue("Rejects negative budget limit", caughtBadBudget);

            boolean caughtBadMonth = false;
            try {
                expenseService.setBudget("invalid-month", "Food & Dining", 1000.0);
            } catch (IllegalArgumentException e) {
                caughtBadMonth = true;
            }
            assertTrue("Rejects invalid month-year format", caughtBadMonth);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 3: To-Do Task Management & State Transitions
        // -------------------------------------------------------------
        System.out.println("\nGroup 3: To-Do Tasks, Priorities & Life Cycle");
        try {
            TaskEntity t1 = taskService.createTask("Pay High-Speed Internet Bill",
                    "Pay broadband bill before late fee applies", "HIGH", "2026-10-20", "Bills & Utilities");
            assertTrue("Task creation returns ID", t1.getId() > 0);
            assertEquals("Task title stored properly", "Pay High-Speed Internet Bill", t1.getTitle());
            assertEquals("Task priority stored as HIGH", "HIGH", t1.getPriority());
            assertTrue("Default task completion is false", !t1.isCompleted());

            // Validation: Empty title
            boolean caughtEmptyTaskTitle = false;
            try {
                taskService.createTask("   ", "desc", "LOW", "2026-10-20", null);
            } catch (IllegalArgumentException e) {
                caughtEmptyTaskTitle = true;
            }
            assertTrue("Rejects empty task title", caughtEmptyTaskTitle);

            // Default priority assignment if invalid
            TaskEntity t2 = taskService.createTask("Buy Coffee Beans", "Espresso roast", "INVALID_PRIORITY", "2026-10-22", "Food & Dining");
            assertEquals("Invalid priority defaults to MEDIUM", "MEDIUM", t2.getPriority());

            // Toggle task completion
            boolean toggled = taskService.toggleTaskCompletion(t1.getId(), true);
            assertTrue("Task toggled to completed", toggled);
            TaskEntity t1Reloaded = taskService.getTaskById(t1.getId());
            assertTrue("Task completion status is true", t1Reloaded.isCompleted());

            // Toggle back to pending
            taskService.toggleTaskCompletion(t1.getId(), false);
            assertTrue("Task toggled back to pending", !taskService.getTaskById(t1.getId()).isCompleted());

            // Overdue detection test
            TaskEntity overdueTask = taskService.createTask("Past Task", "Old reminder", "LOW", "2020-01-01", null);
            List<TaskEntity> overdueList = taskService.getOverdueTasks();
            boolean foundOverdue = overdueList.stream().anyMatch(t -> t.getId() == overdueTask.getId());
            assertTrue("Past task identified in overdue list", foundOverdue);

            // Delete task
            boolean deleted = taskService.deleteTask(overdueTask.getId());
            assertTrue("Task deleted successfully", deleted);
            assertTrue("Deleted task lookup returns null", taskService.getTaskById(overdueTask.getId()) == null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 4: Category Analytics & MPAndroidChart Data Source
        // -------------------------------------------------------------
        System.out.println("\nGroup 4: Category Breakdown & Visual Analytics");
        try {
            List<CategorySummary> summaries = expenseService.getCategoryBreakdown("2026-10");
            assertTrue("Category breakdown returns non-empty list", !summaries.isEmpty());

            CategorySummary foodSummary = summaries.stream()
                    .filter(s -> "Food & Dining".equalsIgnoreCase(s.getCategory()))
                    .findFirst().orElse(null);
            assertTrue("Food & Dining category found in analytics", foodSummary != null);
            assertDoubleEquals("Food total is 3,300", 3300.0, foodSummary.getTotalAmount(), 0.01);
            assertEquals("Food transaction count is 2", 2, foodSummary.getCount());

            CategorySummary transSummary = summaries.stream()
                    .filter(s -> "Transportation".equalsIgnoreCase(s.getCategory()))
                    .findFirst().orElse(null);
            assertTrue("Transportation category found in analytics", transSummary != null);
            assertDoubleEquals("Transportation total is 1,000", 1000.0, transSummary.getTotalAmount(), 0.01);
            assertEquals("Transportation transaction count is 1", 1, transSummary.getCount());

            // Sum of percentages should approximate 100%
            double totalPct = summaries.stream().mapToDouble(CategorySummary::getPercentage).sum();
            assertDoubleEquals("Percentages sum to ~100%", 100.0, totalPct, 1.0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // Group 5: Data Export to CSV
        // -------------------------------------------------------------
        System.out.println("\nGroup 5: Data Serialization & File Export");
        try {
            File exportedCsv = expenseService.exportExpensesToCsv("2026-10", "exports/test_exports");
            assertTrue("Exported CSV file exists on disk", exportedCsv.exists() && exportedCsv.length() > 0);
            assertEquals("Export file name format is correct", "EXPENSES_2026-10.csv", exportedCsv.getName());

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
            System.out.println(">>> ALL JV-10 EXPENSE & TO-DO MANAGER TESTS PASSED! <<<");
            System.exit(0);
        } else {
            System.err.println(">>> SOME TESTS FAILED! <<<");
            System.exit(1);
        }
    }
}

