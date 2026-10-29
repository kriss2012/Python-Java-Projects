# Logic Legend — Testing Summary & Verification Matrix

This matrix documents the verification methodology across all 20 projects in the suite. Every project contains an automated test suite verifying core business logic, database transactions, boundary validations, and edge cases.

## Test Verification Status

| Project ID | Project Name | Test Framework | Test Suite File | Coverage Scope | Status |
|---|---|---|---|---|---|
| **PY-01** | Smart Expense Tracker | pytest / unittest | `tests/test_expense_tracker.py` | CRUD, Budget limits, CSV/PDF export, Analytics | Verified |
| **PY-02** | College AI Chatbot | pytest / unittest | `tests/test_chatbot.py` | Intent parsing, fallback handling, admin QA ops | Verified |
| **PY-03** | Face Recognition Attendance | pytest / unittest | `tests/test_attendance.py` | Biometric matching, duplicate suppression, exports | Verified |
| **PY-04** | Fake News & Spam Detector | pytest / unittest | `tests/test_classifier.py` | TF-IDF pipeline, model comparison, inference | Verified |
| **PY-05** | Weather Dashboard | pytest / unittest | `tests/test_weather.py` | API client, mock data fallback, unit conversions | Verified |
| **PY-06** | Personal Voice Assistant | pytest / unittest | `tests/test_assistant.py` | Command parser, security allowlist, reminders | Verified |
| **PY-07** | Student Performance Predictor | pytest / unittest | `tests/test_predictor.py` | Data preprocessing, regression, classification, at-risk flags | Verified |
| **PY-08** | Price Tracker | pytest / unittest | `tests/test_tracker.py` | HTML parsing, price threshold triggers, history logging | Verified |
| **PY-09** | Secure Password Manager | pytest / unittest | `tests/test_password_manager.py` | PBKDF2 hashing, AES encryption/decryption, generator | Verified |
| **PY-10** | Sentiment Analysis | pytest / unittest | `tests/test_sentiment.py` | VADER scoring, aspect extraction, summary reports | Verified |
| **JV-01** | Online Banking & ATM | JUnit 5 | `src/test/java/BankingTests.java` | Deposits, withdrawals, fund transfers, PIN lockout | Verified |
| **JV-02** | Hospital Management | JUnit 5 | `src/test/java/HospitalTests.java` | Patient registry, appointment conflict check, billing | Verified |
| **JV-03** | Library Management | JUnit 5 | `src/test/java/LibraryTests.java` | Book checkout, return, overdue fine calculation | Verified |
| **JV-04** | Quiz Examination Portal | JUnit 5 | `src/test/java/QuizTests.java` | Question randomization, scoring, timed auto-submit | Verified |
| **JV-05** | Multi-User Chat | JUnit 5 | `src/test/java/ChatTests.java` | Multithreaded message broadcast, private messaging | Verified |
| **JV-06** | Inventory & Billing | JUnit 5 | `src/test/java/InventoryTests.java` | Stock deduction, zero/negative stock guards, invoicing | Verified |
| **JV-07** | Student Management Portal | JUnit 5 | `src/test/java/StudentPortalTests.java` | JPA CRUD, role authentication, REST endpoints | Verified |
| **JV-08** | Ticket Booking System | JUnit 5 | `src/test/java/TicketBookingTests.java` | Double-booking lock, temporary hold timeout, refunds | Verified |
| **JV-09** | Employee Payroll System | JUnit 5 | `src/test/java/PayrollTests.java` | Allowance/deduction computation, net pay, PDF exporter | Verified |
| **JV-10** | Android Expense & To-Do | JUnit 4 / Room Test | `src/test/java/AndroidExpenseTodoTests.java` | Room DAO operations, task filtering, expense sums | Verified |

---

## Testing Principles

1. **Deterministic Execution**: Tests use isolated temporary databases (e.g., `:memory:` SQLite or isolated test DB files) that clean up automatically.
2. **Boundary Testing**: Financial and computational logic is tested with negative numbers, zero balances, overflow conditions, and empty strings.
3. **No Mocking Away The Real World**: Persistence layers are verified by inserting and retrieving actual records rather than mocking queries to always return true.
