# Evaluator Live Demonstration Guide: JV-03 Library Management System

This guide provides a rapid step-by-step walkthrough for faculty evaluators, viva examiners, and project mentors to compile, run, and test the JV-03 library application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Compile Java Sources | Clean build with `javac` into `bin/` | [ ] |
| 2 | Run Unit Tests | 30 assertions pass in < 0.2s (`TEST SUMMARY: 30 Passed, 0 Failed`) | [ ] |
| 3 | View Catalog Inventory | Books displayed with available/total copies and shelf racks | [ ] |
| 4 | Issue Book | Decrements copy count, calculates 14-day due date for student | [ ] |
| 5 | Test Borrow Limit | Exceeding 3 books for student throws quota rejection | [ ] |
| 6 | Return Overdue Book | Calculates late fine at ₹5/day overdue and restores copy | [ ] |

---

## 2. Compilation and Test Execution (Fastest & 100% Deterministic)

### Step 1: Compile Application and Test Suite
```bash
cd Logic-Legend-Minor-Projects/Java-Projects/JV-03-Library-Management
New-Item -ItemType Directory -Force -Path "bin"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\library\model\*.java src\com\logiclegend\library\dao\*.java src\com\logiclegend\library\service\*.java src\com\logiclegend\library\ui\*.java src\com\logiclegend\library\*.java tests\*.java
```

### Step 2: Run Automated Regression Test Suite
```bash
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
**Verification Note**: Look for:
```
-----------------------------------------------------------------
TEST SUMMARY: 30 Passed, 0 Failed (Total: 30)
=================================================================
```

---

## 3. Command Line Interface Demonstration (CLI Mode)

### Step 1: Launch Interactive Library Terminal
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.library.Main --cli
```

### Step 2: Test Library Workflows
1. **View Book Catalog (Option 1)**:
   - Observe pre-seeded titles: *Effective Java*, *Introduction to Algorithms*, *Clean Code*, *Feynman Physics*, *Sapiens*.
2. **Search Catalog (Option 2)**:
   - Search keyword: `Algorithms`
   - Observe instant match for *Introduction to Algorithms* (CLRS).
3. **View Members (Option 4)**:
   - Observe pre-seeded members: *Aakash Nair* (Student, Limit: 3), *Sneha Roy* (Student, Limit: 3), *Prof. D. K. Saraf* (Faculty, Limit: 5).
4. **Issue Book (Option 6)**:
   - Book ID: `3` (Clean Code)
   - Member ID: `1` (Aakash Nair)
   - Issue Date: Press Enter (today's date)
   - Observe generated borrowing slip with return due date 14 days later!
5. **View Circulation Records (Option 8)**:
   - Observe the active issue record.
6. **Return Book with Fine (Option 7)**:
   - Enter Issue Record ID: `2` (Pre-seeded overdue issue for Sneha Roy from September)
   - Return Date: Press Enter (today)
   - Observe fine calculation: `Book returned successfully. Overdue by XX days. Fine Payable: INR XXX.00`.
7. **View Popular Books Leaderboard (Option 10)**:
   - Observe borrow count rankings.
8. **Exit (Option 11)**.

---

## 4. Graphical Interface Demonstration (Swing GUI)

### Step 1: Launch Application
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.library.Main
```

### Step 2: Inspect GUI Elements
1. **Book Catalog Tab**:
   - Use search bar to filter books by title, author, or category.
   - Click **+ Add New Book** to open the catalog entry dialog.
2. **Members Tab**:
   - View members and click **+ Register Member** to register a student or faculty member.
3. **Circulation Tab**:
   - Click **📖 Issue Book** to open the borrowing modal.
   - Select an issued row and click **📥 Return Selected Book** to test fine calculations.
   - Click **💰 Pay Fine** to record fine payments.
4. **Reports Tab**:
   - View currently overdue books and the popular books ranking.

---

## 5. Evaluator Viva Questions to Ask the Student
1. *"How does the system ensure that two students don't take the last copy of a book at the exact same moment?"*
   - Student should explain `available_copies > 0` validation and atomic database transaction with `available_copies = available_copies - 1` in `LibraryService.issueBook()`.
2. *"How are late return fines calculated?"*
   - Student should explain `ChronoUnit.DAYS.between(dueDate, returnDate)` and multiplying overdue days by `DAILY_FINE_RATE` (₹5.00/day).
3. *"Why is the borrowing period different for faculty and students?"*
   - Student should explain role-based member limits (14 days for students vs 30 days for faculty) configured in `LibraryService`.

