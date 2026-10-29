package com.logiclegend.banking.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String dbUrl;
    private final String user;
    private final String password;
    private final boolean isMySQL;

    public DatabaseManager() {
        this("data/bank.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_bank");
            this.dbUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port, db);
            this.user = System.getenv().getOrDefault("MYSQL_USER", "root");
            this.password = System.getenv().getOrDefault("MYSQL_PASSWORD", "root");
        } else {
            this.isMySQL = false;
            File dbFile = new File(dbPath);
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            this.dbUrl = "jdbc:sqlite:" + dbPath;
            this.user = null;
            this.password = null;
        }

        try {
            if (isMySQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] JDBC Driver class not found: " + e.getMessage());
        }

        initDatabase();
    }

    public Connection getConnection() throws SQLException {
        if (isMySQL) {
            return DriverManager.getConnection(dbUrl, user, password);
        } else {
            return DriverManager.getConnection(dbUrl);
        }
    }

    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (!isMySQL) {
                // SQLite Schema
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        account_number TEXT UNIQUE NOT NULL,
                        holder_name TEXT NOT NULL,
                        email TEXT,
                        phone TEXT,
                        account_type TEXT DEFAULT 'SAVINGS',
                        pin_hash TEXT NOT NULL,
                        pin_salt TEXT NOT NULL,
                        balance REAL DEFAULT 0.0 CHECK(balance >= 0.0),
                        daily_withdrawn REAL DEFAULT 0.0,
                        failed_pin_attempts INTEGER DEFAULT 0,
                        status TEXT DEFAULT 'ACTIVE',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transaction_ref TEXT UNIQUE NOT NULL,
                        account_number TEXT NOT NULL,
                        tx_type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        source_dest_account TEXT,
                        balance_after REAL NOT NULL,
                        description TEXT,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
                    );
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Schema initialization error: " + e.getMessage());
        }
    }
}
