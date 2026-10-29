package com.logiclegend.expensetodo.service;

import com.logiclegend.expensetodo.model.BudgetEntity;
import com.logiclegend.expensetodo.model.BudgetStatus;
import com.logiclegend.expensetodo.model.CategorySummary;
import com.logiclegend.expensetodo.model.ExpenseEntity;
import com.logiclegend.expensetodo.repository.ExpenseRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Business Service Layer managing expense operations, budget threshold monitoring,
 * visual analytics aggregation, and data exports.
 */
public class ExpenseService {
    public static final String[] DEFAULT_CATEGORIES = {
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Bills & Utilities",
        "Healthcare",
        "Entertainment",
        "Education",
        "Other"
    };

    public static final String[] PAYMENT_METHODS = {
        "UPI",
        "Credit Card",
        "Debit Card",
        "Cash",
        "Net Banking"
    };

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public ExpenseEntity createExpense(String title, double amount, String category,
                                      String expenseDate, String paymentMethod, String notes) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense title cannot be empty.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        if (category == null || category.trim().isEmpty()) {
            category = "Other";
        }
        if (expenseDate == null || !expenseDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            expenseDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "UPI";
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ExpenseEntity expense = new ExpenseEntity(0, title.trim(), amount, category.trim(), expenseDate, paymentMethod.trim(), notes, now);
        int id = repository.addExpense(expense);
        expense.setId(id);
        return expense;
    }

    public boolean updateExpense(int id, String title, double amount, String category,
                                 String expenseDate, String paymentMethod, String notes) throws SQLException {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Expense title cannot be empty.");
        if (amount <= 0) throw new IllegalArgumentException("Expense amount must be positive.");

        ExpenseEntity expense = new ExpenseEntity(id, title.trim(), amount, category.trim(), expenseDate, paymentMethod.trim(), notes, null);
        return repository.updateExpense(expense);
    }

    public boolean deleteExpense(int id) throws SQLException {
        return repository.deleteExpense(id);
    }

    public ExpenseEntity getExpenseById(int id) throws SQLException {
        return repository.getExpenseById(id);
    }

    public List<ExpenseEntity> getAllExpenses() throws SQLException {
        return repository.getAllExpenses();
    }

    public List<ExpenseEntity> getExpensesByMonth(String monthYear) throws SQLException {
        return repository.getExpensesByMonth(monthYear);
    }

    public double getTotalSpentInMonth(String monthYear) throws SQLException {
        return repository.getTotalSpentInMonth(monthYear);
    }

    // -------------------------------------------------------------------------
    // BUDGET MANAGEMENT & THRESHOLD WARNINGS
    // -------------------------------------------------------------------------

    public boolean setBudget(String monthYear, String category, double limit) throws SQLException {
        if (limit <= 0) throw new IllegalArgumentException("Budget limit must be greater than zero.");
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("Category is required.");
        if (monthYear == null || !monthYear.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("Invalid month-year format. Expected YYYY-MM.");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return repository.saveBudget(monthYear, category.trim(), limit, now);
    }

    public BudgetStatus checkBudgetStatus(String monthYear, String category) throws SQLException {
        BudgetEntity b = repository.getBudget(monthYear, category);
        double limit = (b != null) ? b.getBudgetLimit() : 0.0;
        double spent = repository.getCategorySpentInMonth(monthYear, category);
        return new BudgetStatus(category, monthYear, limit, spent);
    }

    public List<BudgetStatus> getAllBudgetStatuses(String monthYear) throws SQLException {
        List<BudgetStatus> list = new ArrayList<>();
        List<BudgetEntity> budgets = repository.getBudgetsForMonth(monthYear);
        for (BudgetEntity b : budgets) {
            double spent = repository.getCategorySpentInMonth(monthYear, b.getCategory());
            list.add(new BudgetStatus(b.getCategory(), monthYear, b.getBudgetLimit(), spent));
        }
        return list;
    }

    public List<CategorySummary> getCategoryBreakdown(String monthYear) throws SQLException {
        return repository.getCategoryBreakdown(monthYear);
    }

    // -------------------------------------------------------------------------
    // EXPORT & REPORTING
    // -------------------------------------------------------------------------

    public File exportExpensesToCsv(String monthYear, String targetDir) throws SQLException, IOException {
        List<ExpenseEntity> list = (monthYear != null && !monthYear.isEmpty())
                ? getExpensesByMonth(monthYear)
                : getAllExpenses();

        File dir = new File(targetDir != null ? targetDir : "exports");
        if (!dir.exists()) dir.mkdirs();

        String suffix = (monthYear != null && !monthYear.isEmpty()) ? monthYear : "ALL";
        File file = new File(dir, "EXPENSES_" + suffix + ".csv");

        try (FileWriter out = new FileWriter(file)) {
            out.write("ID,Date,Title,Amount,Category,PaymentMethod,Notes\n");
            for (ExpenseEntity e : list) {
                out.write(String.format("%d,%s,\"%s\",%.2f,%s,%s,\"%s\"\n",
                        e.getId(),
                        e.getExpenseDate(),
                        e.getTitle().replace("\"", "\"\""),
                        e.getAmount(),
                        e.getCategory(),
                        e.getPaymentMethod(),
                        e.getNotes() != null ? e.getNotes().replace("\"", "\"\"") : ""
                ));
            }
        }
        return file;
    }

    // -------------------------------------------------------------------------
    // SEED DEMO DATA
    // -------------------------------------------------------------------------

    public void seedDemoExpenses() throws SQLException {
        if (!getAllExpenses().isEmpty()) return;

        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Set monthly budgets
        setBudget(currentMonth, "Food & Dining", 10000.0);
        setBudget(currentMonth, "Transportation", 5000.0);
        setBudget(currentMonth, "Bills & Utilities", 8000.0);
        setBudget(currentMonth, "Shopping", 6000.0);

        // Add expenses
        createExpense("Supermarket Grocery Haul", 3450.0, "Food & Dining", currentMonth + "-03", "Credit Card", "Organic produce & staples");
        createExpense("Metro Card Recharge", 1200.0, "Transportation", currentMonth + "-05", "UPI", "Monthly transit card");
        createExpense("High-Speed Fiber Internet Bill", 1499.0, "Bills & Utilities", currentMonth + "-07", "Net Banking", "Airtel Xstream broadband");
        createExpense("Casual Dining with Friends", 2150.0, "Food & Dining", currentMonth + "-10", "UPI", "Weekend dinner");
        createExpense("Running Shoes & Sports Gear", 4800.0, "Shopping", currentMonth + "-12", "Credit Card", "Decathlon sports apparel");
        createExpense("Electricity Bill", 2850.0, "Bills & Utilities", currentMonth + "-15", "UPI", "BESCOM bill payment");
        createExpense("Uber Cab Airport Ride", 1100.0, "Transportation", currentMonth + "-18", "UPI", "Terminal 2 ride");
    }
}

