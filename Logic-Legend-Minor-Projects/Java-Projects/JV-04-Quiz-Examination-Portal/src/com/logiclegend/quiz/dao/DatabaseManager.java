package com.logiclegend.quiz.dao;

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
        this("data/quiz.db");
    }

    public DatabaseManager(String dbPath) {
        String dbType = System.getenv().getOrDefault("DB_TYPE", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.isMySQL = true;
            String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
            String port = System.getenv().getOrDefault("MYSQL_PORT", "3306");
            String db = System.getenv().getOrDefault("MYSQL_DB", "logic_legend_quiz");
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
                    CREATE TABLE IF NOT EXISTS quizzes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        quiz_code TEXT UNIQUE NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        category TEXT DEFAULT 'Computer Science',
                        duration_minutes INTEGER NOT NULL CHECK(duration_minutes >= 1),
                        pass_percentage REAL DEFAULT 50.0,
                        status TEXT DEFAULT 'PUBLISHED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS questions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        quiz_id INTEGER NOT NULL,
                        question_text TEXT NOT NULL,
                        option_a TEXT NOT NULL,
                        option_b TEXT NOT NULL,
                        option_c TEXT NOT NULL,
                        option_d TEXT NOT NULL,
                        correct_option INTEGER NOT NULL,
                        explanation TEXT,
                        marks REAL DEFAULT 1.0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS exam_submissions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        submission_ref TEXT UNIQUE NOT NULL,
                        quiz_id INTEGER NOT NULL,
                        student_name TEXT NOT NULL,
                        student_email TEXT,
                        total_questions INTEGER NOT NULL,
                        correct_answers INTEGER NOT NULL,
                        score_obtained REAL NOT NULL,
                        max_score REAL NOT NULL,
                        percentage REAL NOT NULL,
                        result_status TEXT NOT NULL,
                        time_taken_seconds INTEGER DEFAULT 0,
                        answers_json TEXT,
                        submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
                    );
                """);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Quiz schema initialization error: " + e.getMessage());
        }
    }
}
