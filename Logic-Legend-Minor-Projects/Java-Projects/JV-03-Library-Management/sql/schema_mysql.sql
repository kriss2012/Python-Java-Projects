-- Logic Legend JV-03: Library Management System
-- MySQL Production Schema Definition

CREATE DATABASE IF NOT EXISTS logic_legend_library;
USE logic_legend_library;

CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    publisher VARCHAR(100),
    edition VARCHAR(20),
    total_copies INT NOT NULL CHECK(total_copies >= 1),
    available_copies INT NOT NULL CHECK(available_copies >= 0),
    shelf_location VARCHAR(50) DEFAULT 'General Stacks',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    member_type ENUM('STUDENT', 'FACULTY', 'COMMUNITY') DEFAULT 'STUDENT',
    max_books INT DEFAULT 3,
    status ENUM('ACTIVE', 'SUSPENDED', 'EXPIRED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS book_issues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    issue_ref VARCHAR(30) UNIQUE NOT NULL,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount DECIMAL(10,2) DEFAULT 0.00 CHECK(fine_amount >= 0.00),
    fine_status ENUM('NONE', 'PAID', 'PENDING') DEFAULT 'NONE',
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') DEFAULT 'ISSUED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_issue_book (book_id),
    INDEX idx_issue_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

