package com.logiclegend.payroll.dao;

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
        this("data/payroll.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_payroll");
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
            System.err.println("[WARN] JDBC Driver notice: " + e.getMessage());
        }

        initTables();
    }

    public Connection getConnection() throws SQLException {
        if (isMySQL) {
            return DriverManager.getConnection(dbUrl, user, password);
        } else {
            return DriverManager.getConnection(dbUrl);
        }
    }

    private void initTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (isMySQL) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        emp_code VARCHAR(30) UNIQUE NOT NULL,
                        name VARCHAR(150) NOT NULL,
                        designation VARCHAR(80) NOT NULL,
                        department VARCHAR(80) NOT NULL,
                        email VARCHAR(120) UNIQUE NOT NULL,
                        phone VARCHAR(30) NOT NULL,
                        bank_account VARCHAR(50) NOT NULL,
                        pan_number VARCHAR(30) NOT NULL,
                        date_of_joining VARCHAR(20) NOT NULL,
                        basic_salary DECIMAL(12,2) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS payroll_records (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        slip_number VARCHAR(50) UNIQUE NOT NULL,
                        employee_id INT NOT NULL,
                        emp_code VARCHAR(30) NOT NULL,
                        emp_name VARCHAR(150) NOT NULL,
                        department VARCHAR(80) NOT NULL,
                        designation VARCHAR(80) NOT NULL,
                        month_year VARCHAR(20) NOT NULL,
                        total_working_days INT NOT NULL DEFAULT 30,
                        days_worked INT NOT NULL,
                        unpaid_leaves INT NOT NULL DEFAULT 0,
                        basic_earned DECIMAL(12,2) NOT NULL,
                        hra DECIMAL(12,2) NOT NULL,
                        da DECIMAL(12,2) NOT NULL,
                        conveyance DECIMAL(12,2) NOT NULL,
                        special_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                        gross_earnings DECIMAL(12,2) NOT NULL,
                        epf DECIMAL(12,2) NOT NULL,
                        esi DECIMAL(12,2) NOT NULL,
                        professional_tax DECIMAL(12,2) NOT NULL DEFAULT 200.00,
                        tds DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                        total_deductions DECIMAL(12,2) NOT NULL,
                        net_salary DECIMAL(12,2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS salary_revisions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        employee_id INT NOT NULL,
                        old_basic DECIMAL(12,2) NOT NULL,
                        new_basic DECIMAL(12,2) NOT NULL,
                        effective_date VARCHAR(20) NOT NULL,
                        reason VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
            } else {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        emp_code TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        designation TEXT NOT NULL,
                        department TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        phone TEXT NOT NULL,
                        bank_account TEXT NOT NULL,
                        pan_number TEXT NOT NULL,
                        date_of_joining TEXT NOT NULL,
                        basic_salary REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS payroll_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        slip_number TEXT UNIQUE NOT NULL,
                        employee_id INTEGER NOT NULL,
                        emp_code TEXT NOT NULL,
                        emp_name TEXT NOT NULL,
                        department TEXT NOT NULL,
                        designation TEXT NOT NULL,
                        month_year TEXT NOT NULL,
                        total_working_days INTEGER NOT NULL DEFAULT 30,
                        days_worked INTEGER NOT NULL,
                        unpaid_leaves INTEGER NOT NULL DEFAULT 0,
                        basic_earned REAL NOT NULL,
                        hra REAL NOT NULL,
                        da REAL NOT NULL,
                        conveyance REAL NOT NULL,
                        special_allowance REAL NOT NULL DEFAULT 0.0,
                        gross_earnings REAL NOT NULL,
                        epf REAL NOT NULL,
                        esi REAL NOT NULL,
                        professional_tax REAL NOT NULL DEFAULT 200.0,
                        tds REAL NOT NULL DEFAULT 0.0,
                        total_deductions REAL NOT NULL,
                        net_salary REAL NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (employee_id) REFERENCES employees(id)
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS salary_revisions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        employee_id INTEGER NOT NULL,
                        old_basic REAL NOT NULL,
                        new_basic REAL NOT NULL,
                        effective_date TEXT NOT NULL,
                        reason TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
                    )
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }
}

