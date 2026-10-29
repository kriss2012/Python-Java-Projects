package com.logiclegend.student.dao;

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
        this("data/students.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_students");
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
                    CREATE TABLE IF NOT EXISTS students (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        reg_number VARCHAR(50) UNIQUE NOT NULL,
                        name VARCHAR(150) NOT NULL,
                        email VARCHAR(150) UNIQUE NOT NULL,
                        phone VARCHAR(30) NOT NULL,
                        department VARCHAR(80) NOT NULL,
                        academic_year INT NOT NULL DEFAULT 1,
                        cgpa DECIMAL(4,2) NOT NULL DEFAULT 0.00,
                        status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS course_grades (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        student_id INT NOT NULL,
                        course_code VARCHAR(30) NOT NULL,
                        course_name VARCHAR(150) NOT NULL,
                        credits INT NOT NULL,
                        letter_grade VARCHAR(5) NOT NULL,
                        grade_points DECIMAL(4,2) NOT NULL,
                        semester INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
            } else {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        reg_number TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        phone TEXT NOT NULL,
                        department TEXT NOT NULL,
                        academic_year INTEGER NOT NULL DEFAULT 1,
                        cgpa REAL NOT NULL DEFAULT 0.0,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS course_grades (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id INTEGER NOT NULL,
                        course_code TEXT NOT NULL,
                        course_name TEXT NOT NULL,
                        credits INTEGER NOT NULL,
                        letter_grade TEXT NOT NULL,
                        grade_points REAL NOT NULL,
                        semester INTEGER NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                    )
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }
}

