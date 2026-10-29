package com.logiclegend.expensetodo;

import com.logiclegend.expensetodo.dao.BudgetDao;
import com.logiclegend.expensetodo.dao.DatabaseManager;
import com.logiclegend.expensetodo.dao.ExpenseDao;
import com.logiclegend.expensetodo.dao.TaskDao;
import com.logiclegend.expensetodo.model.*;
import com.logiclegend.expensetodo.repository.ExpenseRepository;
import com.logiclegend.expensetodo.repository.TaskRepository;
import com.logiclegend.expensetodo.service.ExpenseService;
import com.logiclegend.expensetodo.service.TaskService;
import com.logiclegend.expensetodo.ui.DesktopManagerFrame;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Main application launcher for JV-10 Android Expense and To-Do Manager.
 * Features dual execution modes:
 * 1. Desktop Swing Companion GUI
 * 2. Interactive Terminal CLI
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        ExpenseDao expenseDao = new ExpenseDao(dbManager);
        BudgetDao budgetDao = new BudgetDao(dbManager);
        TaskDao taskDao = new TaskDao(dbManager);

        ExpenseRepository expenseRepo = new ExpenseRepository(expenseDao, budgetDao);
        TaskRepository taskRepo = new TaskRepository(taskDao);

        ExpenseService expenseService = new ExpenseService(expenseRepo);
        TaskService taskService = new TaskService(taskRepo);

        try {
            expenseService.seedDemoExpenses();
            taskService.seedDemoTasks();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding notice: " + e.getMessage());
        }

        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode) {
            runCli(expenseService, taskService);
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    DesktopManagerFrame frame = new DesktopManagerFrame(expenseService, taskService);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Graphical display unavailable. Running in terminal CLI mode.");
                runCli(expenseService, taskService);
            }
        }
    }

    public static void runCli(ExpenseService expenseService, TaskService taskService) {
        Scanner scanner = new Scanner(System.in);
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-10: EXPENSE & TO-DO MANAGER (CLI)");
        System.out.println("  Android Architecture Components & Desktop Verification Terminal");
        System.out.println("=================================================================\n");

        while (true) {
            System.out.println("\n---------------- MAIN MENU ----------------");
            System.out.println("1. List All Expenses");
            System.out.println("2. Add New Expense");
            System.out.println("3. List To-Do Tasks");
            System.out.println("4. Add To-Do Task");
            System.out.println("5. Toggle Task Completion");
            System.out.println("6. Check Budget Health & Threshold Warnings");
            System.out.println("7. Set / Update Category Budget");
            System.out.println("8. View Category Analytics Breakdown");
            System.out.println("9. Export Expenses to CSV");
            System.out.println("0. Exit");
            System.out.print("Select an option (0-9): ");

            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                System.out.println("Exiting Expense and To-Do Manager. Goodbye!");
                break;
            }

            try {
                switch (choice) {
                    case "1" -> listExpenses(expenseService);
                    case "2" -> addExpense(scanner, expenseService);
                    case "3" -> listTasks(taskService);
                    case "4" -> addTask(scanner, taskService);
                    case "5" -> toggleTask(scanner, taskService);
                    case "6" -> checkBudgets(expenseService, currentMonth);
                    case "7" -> setBudget(scanner, expenseService, currentMonth);
                    case "8" -> viewAnalytics(expenseService, currentMonth);
                    case "9" -> exportCsv(expenseService, currentMonth);
                    default -> System.out.println("[WARN] Invalid option. Please select 0-9.");
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Operation failed: " + e.getMessage());
            }
        }
    }

    private static void listExpenses(ExpenseService service) throws SQLException {
        List<ExpenseEntity> list = service.getAllExpenses();
        System.out.println("\n--- EXPENSE TRANSACTION LEDGER ---");
        if (list.isEmpty()) {
            System.out.println("No expense records found.");
            return;
        }
        System.out.printf("%-4s | %-10s | %-28s | %-12s | %-16s | %-10s%n",
                "ID", "Date", "Title", "Amount (INR)", "Category", "Payment");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (ExpenseEntity e : list) {
            System.out.printf("%-4d | %-10s | %-28s | %,12.2f | %-16s | %-10s%n",
                    e.getId(), e.getExpenseDate(),
                    truncate(e.getTitle(), 28),
                    e.getAmount(),
                    truncate(e.getCategory(), 16),
                    e.getPaymentMethod());
        }
    }

    private static void addExpense(Scanner sc, ExpenseService service) throws SQLException {
        System.out.println("\n--- RECORD NEW EXPENSE ---");
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Amount (INR): ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        System.out.println("Categories: Food & Dining, Transportation, Shopping, Bills & Utilities, Healthcare, Other");
        System.out.print("Category: ");
        String category = sc.nextLine().trim();
        System.out.print("Date (YYYY-MM-DD or press Enter for today): ");
        String date = sc.nextLine().trim();
        System.out.print("Payment Method (UPI, Credit Card, Cash, etc.): ");
        String pay = sc.nextLine().trim();
        System.out.print("Notes: ");
        String notes = sc.nextLine().trim();

        ExpenseEntity created = service.createExpense(title, amount, category, date, pay, notes);
        System.out.println("[SUCCESS] Expense recorded with ID #" + created.getId());
    }

    private static void listTasks(TaskService service) throws SQLException {
        List<TaskEntity> list = service.getAllTasks();
        System.out.println("\n--- TO-DO TASK LIST ---");
        if (list.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        System.out.printf("%-4s | %-10s | %-8s | %-10s | %-32s%n",
                "ID", "Status", "Priority", "Due Date", "Title");
        System.out.println("----------------------------------------------------------------------------");
        for (TaskEntity t : list) {
            System.out.printf("%-4d | %-10s | %-8s | %-10s | %-32s%n",
                    t.getId(),
                    t.isCompleted() ? "[DONE]" : "[PENDING]",
                    t.getPriority(),
                    t.getDueDate(),
                    truncate(t.getTitle(), 32));
        }
    }

    private static void addTask(Scanner sc, TaskService service) throws SQLException {
        System.out.println("\n--- ADD TO-DO TASK ---");
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Description: ");
        String desc = sc.nextLine().trim();
        System.out.print("Priority (HIGH, MEDIUM, LOW): ");
        String pri = sc.nextLine().trim();
        System.out.print("Due Date (YYYY-MM-DD or press Enter for today): ");
        String due = sc.nextLine().trim();
        System.out.print("Linked Category (optional): ");
        String cat = sc.nextLine().trim();

        TaskEntity task = service.createTask(title, desc, pri, due, cat);
        System.out.println("[SUCCESS] Task created with ID #" + task.getId());
    }

    private static void toggleTask(Scanner sc, TaskService service) throws SQLException {
        System.out.print("Enter Task ID to toggle completion: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        TaskEntity t = service.getTaskById(id);
        if (t == null) {
            System.out.println("[ERROR] Task ID not found.");
            return;
        }
        boolean newStatus = !t.isCompleted();
        service.toggleTaskCompletion(id, newStatus);
        System.out.printf("[SUCCESS] Task #%d marked as %s%n", id, newStatus ? "COMPLETED" : "PENDING");
    }

    private static void checkBudgets(ExpenseService service, String currentMonth) throws SQLException {
        List<BudgetStatus> list = service.getAllBudgetStatuses(currentMonth);
        System.out.println("\n--- MONTHLY BUDGET HEALTH & THRESHOLD ALERTS (" + currentMonth + ") ---");
        if (list.isEmpty()) {
            System.out.println("No category budgets configured for this month.");
            return;
        }
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %-8s | %-10s%n",
                "Category", "Limit (INR)", "Spent (INR)", "Remaining", "Usage %", "Status");
        System.out.println("----------------------------------------------------------------------------------------");
        for (BudgetStatus b : list) {
            System.out.printf("%-18s | %,12.2f | %,12.2f | %,12.2f | %7.1f%% | %-10s%n",
                    truncate(b.getCategory(), 18),
                    b.getBudgetLimit(),
                    b.getTotalSpent(),
                    b.getRemaining(),
                    b.getPercentageUsed(),
                    b.getAlertStatus());
        }
    }

    private static void setBudget(Scanner sc, ExpenseService service, String currentMonth) throws SQLException {
        System.out.println("\n--- CONFIGURE CATEGORY BUDGET ---");
        System.out.printf("Month [%s]: ", currentMonth);
        String m = sc.nextLine().trim();
        if (m.isEmpty()) m = currentMonth;
        System.out.print("Category Name: ");
        String cat = sc.nextLine().trim();
        System.out.print("Monthly Budget Limit (INR): ");
        double limit = Double.parseDouble(sc.nextLine().trim());

        service.setBudget(m, cat, limit);
        System.out.println("[SUCCESS] Budget limit updated successfully.");
    }

    private static void viewAnalytics(ExpenseService service, String currentMonth) throws SQLException {
        List<CategorySummary> summaries = service.getCategoryBreakdown(currentMonth);
        System.out.println("\n--- CATEGORY BREAKDOWN ANALYTICS (" + currentMonth + ") ---");
        if (summaries.isEmpty()) {
            System.out.println("No expenses recorded for this month.");
            return;
        }
        System.out.printf("%-20s | %-14s | %-8s | %-8s%n", "Category", "Total (INR)", "Entries", "Share %");
        System.out.println("----------------------------------------------------------------");
        for (CategorySummary s : summaries) {
            System.out.printf("%-20s | %,14.2f | %-8d | %6.1f%%%n",
                    truncate(s.getCategory(), 20),
                    s.getTotalAmount(),
                    s.getCount(),
                    s.getPercentage());
        }
    }

    private static void exportCsv(ExpenseService service, String currentMonth) throws Exception {
        File f = service.exportExpensesToCsv(currentMonth, "exports");
        System.out.println("[SUCCESS] Exported CSV to: " + f.getAbsolutePath());
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}

