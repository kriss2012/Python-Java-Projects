package com.logiclegend.expensetodo.repository;

import com.logiclegend.expensetodo.dao.BudgetDao;
import com.logiclegend.expensetodo.dao.ExpenseDao;
import com.logiclegend.expensetodo.model.BudgetEntity;
import com.logiclegend.expensetodo.model.CategorySummary;
import com.logiclegend.expensetodo.model.ExpenseEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository layer orchestrating Expense and Budget data sources.
 * Adheres to modern Android Clean Architecture principles.
 */
public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;

    public ExpenseRepository(ExpenseDao expenseDao, BudgetDao budgetDao) {
        this.expenseDao = expenseDao;
        this.budgetDao = budgetDao;
    }

    public int addExpense(ExpenseEntity expense) throws SQLException {
        return expenseDao.insert(expense);
    }

    public boolean updateExpense(ExpenseEntity expense) throws SQLException {
        return expenseDao.update(expense);
    }

    public boolean deleteExpense(int id) throws SQLException {
        return expenseDao.delete(id);
    }

    public ExpenseEntity getExpenseById(int id) throws SQLException {
        return expenseDao.getById(id);
    }

    public List<ExpenseEntity> getAllExpenses() throws SQLException {
        return expenseDao.getAll();
    }

    public List<ExpenseEntity> getExpensesByMonth(String monthYear) throws SQLException {
        return expenseDao.getByMonth(monthYear);
    }

    public double getTotalSpentInMonth(String monthYear) throws SQLException {
        return expenseDao.getTotalSpentInMonth(monthYear);
    }

    public double getCategorySpentInMonth(String monthYear, String category) throws SQLException {
        return expenseDao.getCategorySpentInMonth(monthYear, category);
    }

    public List<CategorySummary> getCategoryBreakdown(String monthYear) throws SQLException {
        return expenseDao.getCategoryBreakdown(monthYear);
    }

    public boolean saveBudget(String monthYear, String category, double limit, String createdAt) throws SQLException {
        return budgetDao.setBudget(monthYear, category, limit, createdAt);
    }

    public BudgetEntity getBudget(String monthYear, String category) throws SQLException {
        return budgetDao.getBudget(monthYear, category);
    }

    public List<BudgetEntity> getBudgetsForMonth(String monthYear) throws SQLException {
        return budgetDao.getBudgetsForMonth(monthYear);
    }
}

