-- Logic Legend JV-03: Library Management System
-- SQLite Schema Definition

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

CREATE TABLE IF NOT EXISTS members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    member_type TEXT DEFAULT 'STUDENT' CHECK(member_type IN ('STUDENT', 'FACULTY', 'COMMUNITY')),
    max_books INTEGER DEFAULT 3,
    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED', 'EXPIRED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_issues (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    issue_ref TEXT UNIQUE NOT NULL,
    book_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    issue_date TEXT NOT NULL,   -- YYYY-MM-DD
    due_date TEXT NOT NULL,     -- YYYY-MM-DD
    return_date TEXT,           -- YYYY-MM-DD (NULL if currently borrowed)
    fine_amount REAL DEFAULT 0.0 CHECK(fine_amount >= 0.0),
    fine_status TEXT DEFAULT 'NONE' CHECK(fine_status IN ('NONE', 'PAID', 'PENDING')),
    status TEXT DEFAULT 'ISSUED' CHECK(status IN ('ISSUED', 'RETURNED', 'OVERDUE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_issue_book ON book_issues(book_id);
CREATE INDEX IF NOT EXISTS idx_issue_member ON book_issues(member_id);

