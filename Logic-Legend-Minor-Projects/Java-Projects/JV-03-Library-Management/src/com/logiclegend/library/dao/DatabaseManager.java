package com.logiclegend.library.dao;

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
        this("data/library.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_library");
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
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        isbn TEXT UNIQUE NOT NULL,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        category TEXT NOT NULL,
                        publisher TEXT,
                        edition TEXT,
                        total_copies INTEGER NOT NULL CHECK(total_copies >= 1),
                        available_copies INTEGER NOT NULL CHECK(available_copies >= 0),
                        shelf_location TEXT DEFAULT 'General Stacks',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member_code TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT,
                        phone TEXT,
                        member_type TEXT DEFAULT 'STUDENT',
                        max_books INTEGER DEFAULT 3,
                        status TEXT DEFAULT 'ACTIVE',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS book_issues (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        issue_ref TEXT UNIQUE NOT NULL,
                        book_id INTEGER NOT NULL,
                        member_id INTEGER NOT NULL,
                        issue_date TEXT NOT NULL,
                        due_date TEXT NOT NULL,
                        return_date TEXT,
                        fine_amount REAL DEFAULT 0.0 CHECK(fine_amount >= 0.0),
                        fine_status TEXT DEFAULT 'NONE',
                        status TEXT DEFAULT 'ISSUED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                        FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
                    );
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Library schema initialization error: " + e.getMessage());
        }
    }
}

