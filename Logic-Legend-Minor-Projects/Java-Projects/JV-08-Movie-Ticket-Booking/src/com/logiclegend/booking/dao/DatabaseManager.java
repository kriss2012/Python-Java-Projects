package com.logiclegend.booking.dao;

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
        this("data/booking.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_booking");
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
                    CREATE TABLE IF NOT EXISTS movies (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(150) NOT NULL,
                        genre VARCHAR(60) NOT NULL,
                        duration_minutes INT NOT NULL,
                        language VARCHAR(40) NOT NULL,
                        rating VARCHAR(10) NOT NULL DEFAULT 'UA',
                        synopsis TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS shows (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        movie_id INT NOT NULL,
                        venue VARCHAR(100) NOT NULL,
                        screen_name VARCHAR(50) NOT NULL,
                        show_date VARCHAR(20) NOT NULL,
                        show_time VARCHAR(20) NOT NULL,
                        silver_price DECIMAL(10,2) NOT NULL DEFAULT 150.00,
                        gold_price DECIMAL(10,2) NOT NULL DEFAULT 220.00,
                        platinum_price DECIMAL(10,2) NOT NULL DEFAULT 350.00,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS seats (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        show_id INT NOT NULL,
                        seat_code VARCHAR(10) NOT NULL,
                        row_name VARCHAR(5) NOT NULL,
                        seat_number INT NOT NULL,
                        tier VARCHAR(20) NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                        hold_expiry_ms BIGINT DEFAULT 0,
                        hold_user VARCHAR(100),
                        UNIQUE KEY uq_show_seat (show_id, seat_code),
                        FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        booking_reference VARCHAR(50) UNIQUE NOT NULL,
                        show_id INT NOT NULL,
                        movie_title VARCHAR(150) NOT NULL,
                        customer_name VARCHAR(100) NOT NULL,
                        customer_email VARCHAR(120) NOT NULL,
                        customer_phone VARCHAR(30) NOT NULL,
                        seats_csv VARCHAR(255) NOT NULL,
                        subtotal DECIMAL(10,2) NOT NULL,
                        convenience_fee DECIMAL(10,2) NOT NULL,
                        gst_tax DECIMAL(10,2) NOT NULL,
                        discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        grand_total DECIMAL(10,2) NOT NULL,
                        booking_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
            } else {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS movies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        genre TEXT NOT NULL,
                        duration_minutes INTEGER NOT NULL,
                        language TEXT NOT NULL,
                        rating TEXT NOT NULL DEFAULT 'UA',
                        synopsis TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS shows (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        movie_id INTEGER NOT NULL,
                        venue TEXT NOT NULL,
                        screen_name TEXT NOT NULL,
                        show_date TEXT NOT NULL,
                        show_time TEXT NOT NULL,
                        silver_price REAL NOT NULL DEFAULT 150.0,
                        gold_price REAL NOT NULL DEFAULT 220.0,
                        platinum_price REAL NOT NULL DEFAULT 350.0,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (movie_id) REFERENCES movies(id)
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS seats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        show_id INTEGER NOT NULL,
                        seat_code TEXT NOT NULL,
                        row_name TEXT NOT NULL,
                        seat_number INTEGER NOT NULL,
                        tier TEXT NOT NULL,
                        price REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'AVAILABLE',
                        hold_expiry_ms INTEGER DEFAULT 0,
                        hold_user TEXT,
                        UNIQUE(show_id, seat_code),
                        FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        booking_reference TEXT UNIQUE NOT NULL,
                        show_id INTEGER NOT NULL,
                        movie_title TEXT NOT NULL,
                        customer_name TEXT NOT NULL,
                        customer_email TEXT NOT NULL,
                        customer_phone TEXT NOT NULL,
                        seats_csv TEXT NOT NULL,
                        subtotal REAL NOT NULL,
                        convenience_fee REAL NOT NULL,
                        gst_tax REAL NOT NULL,
                        discount_amount REAL NOT NULL DEFAULT 0.0,
                        grand_total REAL NOT NULL,
                        booking_status TEXT NOT NULL DEFAULT 'CONFIRMED',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (show_id) REFERENCES shows(id)
                    )
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }
}

