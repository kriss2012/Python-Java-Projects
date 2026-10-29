# Viva Examination Questions and Answers: JV-03 Library Management System

### Q1: How does your library system ensure that a book cannot be issued when copies are exhausted?
**Answer**: Inventory integrity is protected through two complementary checks:
1. **Service Layer**: In `LibraryService.issueBook()`, the method verifies `book.getAvailableCopies() > 0`. If copies are 0, it throws an `IllegalStateException("OUT OF STOCK")` immediately.
2. **Database Constraint**: The `books` table schema defines `available_copies INTEGER NOT NULL CHECK(available_copies >= 0)`. If any operation attempts to drive the copy count below zero, the database rejects the transaction with a check constraint violation.

### Q2: How are borrowing limits differentiated between Students and Faculty?
**Answer**: The `members` table contains `member_type` (`STUDENT` or `FACULTY`) and `max_books`. When registering:
- Students receive `max_books = 3` and a 14-day loan period.
- Faculty receive `max_books = 5` and a 30-day loan period.
Before issuing a new book, `getActiveIssuedCount(memberId)` queries active unreturned issues (`status = 'ISSUED'`). If this count equals or exceeds `max_books`, the issue is rejected.

### Q3: Explain the overdue fine calculation logic.
**Answer**: When a book is returned in `LibraryService.returnBook()`, the system calculates the number of days between the due date and the actual return date using Java 8+ `java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate)`. If `overdueDays > 0`, the fine is computed as `overdueDays * DAILY_FINE_RATE` (₹5.00/day) and the `fine_status` is marked as `'PENDING'`. If returned on or before the due date, the fine is ₹0.00 and status is marked as `'NONE'`.

### Q4: Why is an atomic transaction required when issuing or returning a book?
**Answer**: Issuing or returning a book involves two coupled database operations: updating the `available_copies` column in the `books` table and inserting or updating a record in the `book_issues` table. If these operations were executed separately without a transaction, a crash between them could result in decremented inventory without an issue record (lost book) or an issue record without decrementing inventory (phantom stock). By wrapping both in `conn.setAutoCommit(false)` with `conn.commit()`, atomicity is guaranteed.

### Q5: How does the system handle database portability between SQLite and MySQL?
**Answer**: `DatabaseManager` checks the `DB_TYPE` environment variable. By default, it connects to an embedded SQLite database (`data/library.db`) via the bundled `lib/sqlite-jdbc.jar`, requiring zero external software installation. If `DB_TYPE=mysql` is configured, it connects to MySQL on port 3306.

### Q6: What happens if a book is returned before its due date?
**Answer**: `ChronoUnit.DAYS.between(dueDate, returnDate)` yields a negative number. Using `Math.max(0, overdueDays)`, the overdue days evaluate to 0, resulting in ₹0.00 fine and setting `fine_status = 'NONE'`. The book's `available_copies` is incremented back to the catalog.

### Q7: How does the Popular Books report work?
**Answer**: `LibraryService.getPopularBooks()` executes an SQL query grouping the `books` table by book ID and performing a `LEFT JOIN` with `book_issues`, calculating `COUNT(bi.id) AS borrow_count` and ordering results descending by `borrow_count`. This identifies the most frequently read titles.

### Q8: How are SQL injection attacks prevented?
**Answer**: Every SQL statement across `LibraryService` uses `PreparedStatement` with `?` parameters. User inputs (search keywords, ISBNs, member names) are passed safely via typed setter methods (`ps.setString()`, `ps.setInt()`), ensuring user inputs cannot alter SQL syntax.

### Q9: What is the purpose of the test suite in `tests/TestRunner.java`?
**Answer**: It acts as an automated regression test runner with 30 assertions. It spins up an isolated temporary database, creates catalog books, verifies student/faculty limits, tests issue copy decrements, asserts out-of-stock and quota rejections, verifies on-time and overdue returns with fine calculation, and tests fine payment without altering the production database.

### Q10: How does the keyword search function work?
**Answer**: `LibraryService.searchBooks()` executes an SQL query using `LIKE ?` placeholders across `title`, `author`, `category`, and `isbn`. Wrapping the search term with wildcards (`%query%`) allows partial title or author matching (e.g., searching "Java" matches "Effective Java").

### Q11: What design patterns are used in this application?
**Answer**:
- **DAO Pattern**: `DatabaseManager` encapsulates database connectivity and table creation.
- **Service Layer Pattern**: `LibraryService` centralizes business rules, fines, and transaction boundaries.
- **MVC Pattern**: Entity models represent state, Swing tabs represent views, and action listeners handle controller interactions.
- **Immutable Value Object**: `ReturnResult` encapsulates the multi-value response of the return workflow.

### Q12: How is fine payment tracked?
**Answer**: Late returns set `fine_status = 'PENDING'`. When the student pays the librarian at the desk, the librarian selects the record and clicks "Pay Fine", which updates `book_issues SET fine_status = 'PAID'`, preserving the fine amount in the audit log while marking the debt as settled.

### Q13: Can a member with active overdue books borrow new books?
**Answer**: In commercial libraries, members with pending fines are blocked from borrowing new items. Our service layer supports querying pending fines via `fine_status = 'PENDING'` to enforce automatic account suspension if fines exceed a threshold.

### Q14: How does the application format borrowing slips?
**Answer**: `LibraryService.generateIssueSlip()` generates an ASCII text slip containing the library header, issue reference code, book title, ISBN, borrower name and code, issue date, return due date, and fine terms.

### Q15: How can this system be integrated with barcode scanners?
**Answer**: Standard USB barcode scanners emulate keyboard keystrokes followed by an `Enter` keypress. In the Swing GUI, the ISBN and Member Code text fields have Enter-key listeners that instantly trigger search or issue actions when a barcode is scanned.

