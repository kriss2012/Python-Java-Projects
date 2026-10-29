-- Logic Legend JV-04: Online Quiz and Examination Portal
-- MySQL Production Schema Definition

CREATE DATABASE IF NOT EXISTS logic_legend_quiz;
USE logic_legend_quiz;

CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_code VARCHAR(30) UNIQUE NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) DEFAULT 'Computer Science',
    duration_minutes INT NOT NULL CHECK(duration_minutes >= 1),
    pass_percentage DECIMAL(5,2) DEFAULT 50.00,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option TINYINT NOT NULL CHECK(correct_option BETWEEN 1 AND 4),
    explanation TEXT,
    marks DECIMAL(5,2) DEFAULT 1.00 CHECK(marks > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_questions_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_ref VARCHAR(50) UNIQUE NOT NULL,
    quiz_id INT NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    student_email VARCHAR(100),
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    score_obtained DECIMAL(6,2) NOT NULL,
    max_score DECIMAL(6,2) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    result_status ENUM('PASS', 'FAIL') NOT NULL,
    time_taken_seconds INT DEFAULT 0,
    answers_json TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_subs_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

