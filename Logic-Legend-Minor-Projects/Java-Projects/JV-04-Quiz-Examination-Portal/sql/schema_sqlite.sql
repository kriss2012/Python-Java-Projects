-- Logic Legend JV-04: Online Quiz and Examination Portal
-- SQLite Schema Definition

CREATE TABLE IF NOT EXISTS quizzes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_code TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT DEFAULT 'Computer Science',
    duration_minutes INTEGER NOT NULL CHECK(duration_minutes >= 1),
    pass_percentage REAL DEFAULT 50.0,
    status TEXT DEFAULT 'PUBLISHED' CHECK(status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option INTEGER NOT NULL CHECK(correct_option BETWEEN 1 AND 4),
    explanation TEXT,
    marks REAL DEFAULT 1.0 CHECK(marks > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

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
    result_status TEXT NOT NULL CHECK(result_status IN ('PASS', 'FAIL')),
    time_taken_seconds INTEGER DEFAULT 0,
    answers_json TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_subs_quiz ON exam_submissions(quiz_id);

