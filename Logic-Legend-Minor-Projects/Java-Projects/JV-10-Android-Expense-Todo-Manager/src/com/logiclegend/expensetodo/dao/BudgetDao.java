package com.logiclegend.expensetodo.dao;

import com.logiclegend.expensetodo.model.BudgetEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Budget threshold persistence.
 */
public class BudgetDao {
    private final DatabaseManager dbManager;

    public BudgetDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean setBudget(String monthYear, String category, double budgetLimit, String createdAt) throws SQLException {
        String sql = """
            INSERT INTO budgets (month_year, category, budget_limit, created_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(month_year, category) DO UPDATE SET budget_limit = excluded.budget_limit
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear);
            ps.setString(2, category);
            ps.setDouble(3, budgetLimit);
            ps.setString(4, createdAt);
            return ps.executeUpdate() > 0;
        }
    }

    public BudgetEntity getBudget(String monthYear, String category) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE month_year = ? AND category = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBudget(rs);
            }
        }
        return null;
    }

    public List<BudgetEntity> getBudgetsForMonth(String monthYear) throws SQLException {
        List<BudgetEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE month_year = ? ORDER BY category ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBudget(rs));
            }
        }
        return list;
    }

    public boolean deleteBudget(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private BudgetEntity mapBudget(ResultSet rs) throws SQLException {
        return new BudgetEntity(
                rs.getInt("id"),
                rs.getString("month_year"),
                rs.getString("category"),
                rs.getDouble("budget_limit"),
                rs.getString("created_at")
        );
    }
}

