package com.logiclegend.inventory.dao;

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
        this("data/inventory.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_inventory");
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
            System.err.println("[WARN] JDBC Driver class loading notice: " + e.getMessage());
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
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_code VARCHAR(50) UNIQUE NOT NULL,
                        name VARCHAR(150) NOT NULL,
                        category VARCHAR(80) NOT NULL,
                        cost_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        unit_price DECIMAL(10,2) NOT NULL,
                        stock_quantity INT NOT NULL DEFAULT 0,
                        reorder_level INT NOT NULL DEFAULT 5,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        phone VARCHAR(20) UNIQUE NOT NULL,
                        email VARCHAR(100),
                        loyalty_points INT NOT NULL DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        invoice_number VARCHAR(50) UNIQUE NOT NULL,
                        customer_id INT,
                        customer_name VARCHAR(100) NOT NULL DEFAULT 'Walk-in Customer',
                        subtotal DECIMAL(10,2) NOT NULL,
                        discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        grand_total DECIMAL(10,2) NOT NULL,
                        payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
                        loyalty_points_earned INT NOT NULL DEFAULT 0,
                        loyalty_points_redeemed INT NOT NULL DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS invoice_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        invoice_id INT NOT NULL,
                        product_id INT NOT NULL,
                        product_code VARCHAR(50) NOT NULL,
                        product_name VARCHAR(150) NOT NULL,
                        unit_price DECIMAL(10,2) NOT NULL,
                        quantity INT NOT NULL,
                        total_price DECIMAL(10,2) NOT NULL
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS stock_logs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_id INT NOT NULL,
                        change_type VARCHAR(30) NOT NULL,
                        quantity_changed INT NOT NULL,
                        final_stock INT NOT NULL,
                        remarks VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
            } else {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_code TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        cost_price REAL NOT NULL DEFAULT 0.0,
                        unit_price REAL NOT NULL,
                        stock_quantity INTEGER NOT NULL DEFAULT 0,
                        reorder_level INTEGER NOT NULL DEFAULT 5,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        phone TEXT UNIQUE NOT NULL,
                        email TEXT,
                        loyalty_points INTEGER NOT NULL DEFAULT 0,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        invoice_number TEXT UNIQUE NOT NULL,
                        customer_id INTEGER,
                        customer_name TEXT NOT NULL DEFAULT 'Walk-in Customer',
                        subtotal REAL NOT NULL,
                        discount_amount REAL NOT NULL DEFAULT 0.0,
                        tax_amount REAL NOT NULL DEFAULT 0.0,
                        grand_total REAL NOT NULL,
                        payment_method TEXT NOT NULL DEFAULT 'CASH',
                        loyalty_points_earned INTEGER NOT NULL DEFAULT 0,
                        loyalty_points_redeemed INTEGER NOT NULL DEFAULT 0,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS invoice_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        invoice_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        product_code TEXT NOT NULL,
                        product_name TEXT NOT NULL,
                        unit_price REAL NOT NULL,
                        quantity INTEGER NOT NULL,
                        total_price REAL NOT NULL
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS stock_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER NOT NULL,
                        change_type TEXT NOT NULL,
                        quantity_changed INTEGER NOT NULL,
                        final_stock INTEGER NOT NULL,
                        remarks TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }
}

