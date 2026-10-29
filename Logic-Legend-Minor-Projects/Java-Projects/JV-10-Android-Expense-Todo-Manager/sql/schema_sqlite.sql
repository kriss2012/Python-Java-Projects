-- JV-10 Android Expense and To-Do Manager (SQLite Schema mirroring Android Room)

CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    amount REAL NOT NULL CHECK(amount > 0),
    category TEXT NOT NULL,
    expense_date TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    priority TEXT NOT NULL CHECK(priority IN ('HIGH', 'MEDIUM', 'LOW')),
    due_date TEXT NOT NULL,
    is_completed INTEGER NOT NULL DEFAULT 0,
    linked_category TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    month_year TEXT NOT NULL,
    category TEXT NOT NULL,
    budget_limit REAL NOT NULL CHECK(budget_limit > 0),
    created_at TEXT NOT NULL,
    UNIQUE(month_year, category)
);

CREATE INDEX IF NOT EXISTS idx_expense_date ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expense_cat ON expenses(category);
CREATE INDEX IF NOT EXISTS idx_task_due ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_task_completed ON tasks(is_completed);

