package com.logiclegend.expensetodo.dao;

import com.logiclegend.expensetodo.model.CategorySummary;
import com.logiclegend.expensetodo.model.ExpenseEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Expense persistence.
 * Compatible with Android Room @Dao specifications.
 */
public class ExpenseDao {
    private final DatabaseManager dbManager;

    public ExpenseDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public int insert(ExpenseEntity expense) throws SQLException {
        String sql = """
            INSERT INTO expenses (title, amount, category, expense_date, payment_method, notes, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setString(4, expense.getExpenseDate());
            ps.setString(5, expense.getPaymentMethod());
            ps.setString(6, expense.getNotes());
            ps.setString(7, expense.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    expense.setId(rs.getInt(1));
                    return expense.getId();
                }
            }
        }
        return -1;
    }

    public boolean update(ExpenseEntity expense) throws SQLException {
        String sql = """
            UPDATE expenses
            SET title = ?, amount = ?, category = ?, expense_date = ?, payment_method = ?, notes = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setString(4, expense.getExpenseDate());
            ps.setString(5, expense.getPaymentMethod());
            ps.setString(6, expense.getNotes());
            ps.setInt(7, expense.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public ExpenseEntity getById(int id) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapExpense(rs);
            }
        }
        return null;
    }

    public List<ExpenseEntity> getAll() throws SQLException {
        List<ExpenseEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC, id DESC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapExpense(rs));
        }
        return list;
    }

    public List<ExpenseEntity> getByMonth(String monthYear) throws SQLException {
        List<ExpenseEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE expense_date LIKE ? ORDER BY expense_date DESC, id DESC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapExpense(rs));
            }
        }
        return list;
    }

    public double getTotalSpentInMonth(String monthYear) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE expense_date LIKE ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    public double getCategorySpentInMonth(String monthYear, String category) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE expense_date LIKE ? AND category = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear + "%");
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    public List<CategorySummary> getCategoryBreakdown(String monthYear) throws SQLException {
        List<CategorySummary> summaries = new ArrayList<>();
        double totalMonth = getTotalSpentInMonth(monthYear);
        if (totalMonth <= 0) return summaries;

        String sql = """
            SELECT category, SUM(amount) AS cat_total, COUNT(*) AS cat_count
            FROM expenses
            WHERE expense_date LIKE ?
            GROUP BY category
            ORDER BY cat_total DESC
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("category");
                    double amt = rs.getDouble("cat_total");
                    int count = rs.getInt("cat_count");
                    double pct = (amt / totalMonth) * 100.0;
                    summaries.add(new CategorySummary(cat, amt, count, pct));
                }
            }
        }
        return summaries;
    }

    private ExpenseEntity mapExpense(ResultSet rs) throws SQLException {
        return new ExpenseEntity(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getDouble("amount"),
                rs.getString("category"),
                rs.getString("expense_date"),
                rs.getString("payment_method"),
                rs.getString("notes"),
                rs.getString("created_at")
        );
    }
}

