package com.logiclegend.expensetodo.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Portable Database Connection Manager for Android Room and JDBC environments.
 * Initializes embedded SQLite tables and ensures schema integrity.
 */
public class DatabaseManager {
    private final String dbUrl;

    public DatabaseManager() {
        this("data/expensetodo.db");
    }

    public DatabaseManager(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath", e);
        }

        File dbFile = new File(dbPath);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initializeSchema();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initializeSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Expenses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL CHECK(amount > 0),
                    category TEXT NOT NULL,
                    expense_date TEXT NOT NULL,
                    payment_method TEXT NOT NULL,
                    notes TEXT,
                    created_at TEXT NOT NULL
                );
            """);

            // Tasks table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    priority TEXT NOT NULL CHECK(priority IN ('HIGH', 'MEDIUM', 'LOW')),
                    due_date TEXT NOT NULL,
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    linked_category TEXT,
                    created_at TEXT NOT NULL
                );
            """);

            // Budgets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    month_year TEXT NOT NULL,
                    category TEXT NOT NULL,
                    budget_limit REAL NOT NULL CHECK(budget_limit > 0),
                    created_at TEXT NOT NULL,
                    UNIQUE(month_year, category)
                );
            """);

            // Indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_expense_date ON expenses(expense_date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_expense_cat ON expenses(category);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_due ON tasks(due_date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_completed ON tasks(is_completed);");

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database schema", e);
        }
    }
}

