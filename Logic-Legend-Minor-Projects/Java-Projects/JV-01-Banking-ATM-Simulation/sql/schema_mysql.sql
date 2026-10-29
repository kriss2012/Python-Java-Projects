-- Logic Legend JV-01: Online Banking and ATM Simulation
-- MySQL Production Schema Definition

CREATE DATABASE IF NOT EXISTS logic_legend_bank;
USE logic_legend_bank;

CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    holder_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    account_type ENUM('SAVINGS', 'CURRENT') DEFAULT 'SAVINGS',
    pin_hash VARCHAR(128) NOT NULL,
    pin_salt VARCHAR(64) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00 CHECK (balance >= 0.00),
    daily_withdrawn DECIMAL(15,2) DEFAULT 0.00,
    failed_pin_attempts INT DEFAULT 0,
    status ENUM('ACTIVE', 'LOCKED', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_ref VARCHAR(50) UNIQUE NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    tx_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN') NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    source_dest_account VARCHAR(20),
    balance_after DECIMAL(15,2) NOT NULL,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE,
    INDEX idx_tx_account (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

