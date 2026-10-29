-- Logic Legend JV-01: Online Banking and ATM Simulation
-- SQLite Schema Definition

CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT UNIQUE NOT NULL,
    holder_name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    account_type TEXT DEFAULT 'SAVINGS' CHECK(account_type IN ('SAVINGS', 'CURRENT')),
    pin_hash TEXT NOT NULL,
    pin_salt TEXT NOT NULL,
    balance REAL DEFAULT 0.0 CHECK(balance >= 0.0),
    daily_withdrawn REAL DEFAULT 0.0,
    failed_pin_attempts INTEGER DEFAULT 0,
    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'LOCKED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_ref TEXT UNIQUE NOT NULL,
    account_number TEXT NOT NULL,
    tx_type TEXT NOT NULL CHECK(tx_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN')),
    amount REAL NOT NULL CHECK(amount > 0),
    source_dest_account TEXT,
    balance_after REAL NOT NULL,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tx_account ON transactions(account_number);

