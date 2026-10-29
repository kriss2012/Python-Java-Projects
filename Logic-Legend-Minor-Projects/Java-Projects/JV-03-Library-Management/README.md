# Logic Legend — JV-03: Library Management System

A comprehensive, automated desktop library cataloging, circulation, and overdue fine management system developed in Java 21 using Swing, JDBC, SQLite, and MySQL. The system provides book catalog inventory tracking, student and faculty membership management with differential borrowing limits, automated book issue and return workflows with real-time stock decrements/increments, overdue fine calculation engines, and circulation analytics.

---

## 1. Project Title and Identifier
- **Project Name**: Library Management System
- **Project Code**: `JV-03`
- **Application Category**: Educational ERP, Information Retrieval & Desktop GUI
- **Target Audience**: College librarians, academic administrators, computer science students, and faculty evaluators.

---

## 2. Executive Summary
University libraries manage thousands of titles across diverse academic disciplines. Manual card-catalogs or disconnected spreadsheets frequently lead to misplaced books, inventory discrepancies, uncollected late fines, and student borrowing limit violations. The **Library Management System** addresses these issues with an ACID-compliant database architecture, automated stock decrement/increment on book circulation, differential membership rules (Students: 3 books for 14 days; Faculty: 5 books for 30 days), automatic fine calculation at ₹5/day overdue, popularity rankings, and dual desktop GUI and CLI interfaces.

---

## 3. Key Features
- **Book Catalog & Inventory Tracking**: Tracks ISBN-13, title, author, category, publisher, edition, shelf rack locations, total copies, and real-time available copies.
- **Differential Membership Roles**:
  - **Student Members**: Maximum 3 books allowed simultaneously; 14-day loan period.
  - **Faculty Members**: Maximum 5 books allowed simultaneously; 30-day loan period.
- **Automated Book Issue Workflow**:
  - Checks copy availability (rejects if `available_copies == 0` with `OUT OF STOCK`).
  - Verifies member borrowing limit (rejects if member already has reached max active issues).
  - Automatically calculates future due date based on membership type.
  - Atomically decrements `available_copies` in `books` table.
- **Overdue Fine Engine & Return Workflow**:
  - Automatically computes overdue days: $\max(0, \text{Return Date} - \text{Due Date})$.
  - Calculates late fine: $\text{Overdue Days} \times \text{INR 5.00/day}$.
  - Atomically increments `available_copies` back to the catalog.
  - Supports fine collection workflow (`fine_status = 'PAID'`).
- **Circulation Analytics & Reports**:
  - **Overdue Books Report**: Surfaces all unreturned books currently past their due dates.
  - **Popular Books Leaderboard**: Ranks most frequently borrowed titles across categories.
- **Search Engine**: Wildcard search across titles, authors, categories, and ISBN numbers.
- **Formatted Borrowing Slips**: Generates clean ASCII borrowing slips with due dates and rules.
- **Dual Presentation Layer**: Modern multi-tab Java Swing GUI alongside an interactive CLI terminal.

---

## 4. System Architecture
The application is structured into four modular layers:
1. **Presentation Layer (`ui/LibraryFrame.java`, `Main.java`)**: Java Swing GUI with tabbed views and terminal CLI.
2. **Business Service Layer (`service/LibraryService.java`)**: Issue/return rules, inventory updates, fine algorithms, and analytics.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: JDBC connection management and automated schema initialization.
4. **Data Persistence Layer (`model/`, SQLite / MySQL)**: Relational schema across `books`, `members`, and `book_issues`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **GUI Framework**: Java Swing & AWT (Standard JDK)
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Embedded via self-contained `sqlite-jdbc.jar` — zero external configuration required!)
- **Production Database**: MySQL 8.x (Supported via `sql/schema_mysql.sql`)

---

## 6. Directory Structure
```
JV-03-Library-Management/
├── .env.example              # Configuration template for fine rates and DB
├── .gitignore                # Git exclusions for compiled classes and databases
├── pom.xml                   # Maven project descriptor
├── README.md                 # 19-section comprehensive documentation
├── lib/
│   └── sqlite-jdbc.jar       # Self-contained zero-dependency SQLite JDBC driver
├── data/                     # Local SQLite database storage directory
├── exports/                  # Exported borrowing slips and circulation reports
├── sql/
│   ├── schema_sqlite.sql     # SQLite relational table definitions
│   └── schema_mysql.sql      # MySQL production database schema
├── docs/                     # Academic deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   └── com/logiclegend/library/
│       ├── model/
│       │   ├── Book.java        # Book catalog entity
│       │   ├── Member.java      # Member entity
│       │   └── BookIssue.java   # Circulation issue entity
│       ├── dao/
│       │   └── DatabaseManager.java # Dual SQLite / MySQL connection manager
│       ├── service/
│       │   └── LibraryService.java  # Business logic, fines & circulation
│       ├── ui/
│       │   └── LibraryFrame.java    # Java Swing GUI with tabbed dashboard
│       └── Main.java                # Application entry point (GUI + CLI)
└── tests/
    └── TestRunner.java       # Automated standalone unit test suite (30 assertions)
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Java-Projects/JV-03-Library-Management
   ```
2. Compile all source and test files using JDK 21:
   ```bash
   New-Item -ItemType Directory -Force -Path "bin"
   javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\library\model\*.java src\com\logiclegend\library\dao\*.java src\com\logiclegend\library\service\*.java src\com\logiclegend\library\ui\*.java src\com\logiclegend\library\*.java tests\*.java
   ```

---

## 8. Usage Instructions
### Running the Graphical Application (Swing GUI)
Launch the library dashboard:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.library.Main
```

### Running in Headless / CLI Mode
Launch the interactive terminal interface:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.library.Main --cli
```

---

## 9. Database Design and Schema
- **`books`**: `id`, `isbn` (UNIQUE), `title`, `author`, `category`, `publisher`, `edition`, `total_copies`, `available_copies` (CHECK `>= 0`), `shelf_location`, `created_at`.
- **`members`**: `id`, `member_code` (UNIQUE), `name`, `email`, `phone`, `member_type` (`STUDENT`/`FACULTY`), `max_books`, `status` (`ACTIVE`/`SUSPENDED`), `created_at`.
- **`book_issues`**: `id`, `issue_ref` (UNIQUE), `book_id` (FK), `member_id` (FK), `issue_date`, `due_date`, `return_date`, `fine_amount`, `fine_status` (`NONE`/`PAID`/`PENDING`), `status` (`ISSUED`/`RETURNED`/`OVERDUE`).

---

## 10. Fine Calculation Algorithm
Given Return Date $D_{\text{return}}$ and Due Date $D_{\text{due}}$:
$$\Delta = \text{ChronoUnit.DAYS.between}(D_{\text{due}}, D_{\text{return}})$$
$$\text{Overdue Days} = \max(0, \Delta)$$
$$\text{Fine Amount} = \text{Overdue Days} \times \text{DAILY\_FINE\_RATE} \quad (\text{₹5.00})$$
If $\text{Fine Amount} > 0 \implies \text{fine\_status} = \text{'PENDING'}$; otherwise $\text{'NONE'}$.

---

## 11. Testing and Verification
Run the automated test suite:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
**30/30 Unit Test Assertions Passing**:
- ✅ Book creation with inventory tracking and availability flags
- ✅ Student membership limit (3 books) and code prefix (`STU-`)
- ✅ Faculty membership limit (5 books) and code prefix (`FAC-`)
- ✅ Book issue copy decrementing from available inventory
- ✅ Due date calculation (14 days for student)
- ✅ Out-of-stock rejection when `available_copies == 0`
- ✅ Borrowing limit rejection when member exceeds `maxBooks`
- ✅ On-time book return with ₹0.00 fine and inventory restoration
- ✅ Overdue book return fine calculation (5 days $\times$ ₹5.00 = ₹25.00)
- ✅ Fine payment status transition to `PAID`
- ✅ Keyword search across catalog
- ✅ Popular books borrow frequency ranking

---

## 12. Security and Error Handling
- **Check Constraints**: `CHECK(available_copies >= 0)` prevents phantom negative inventory counts.
- **Atomic Transactions**: Book issue and return operations wrap catalog updates and circulation inserts inside atomic `conn.setAutoCommit(false)` blocks.
- **Parameterized SQL**: All database operations use `PreparedStatement` to prevent SQL injection.

---

## 13. Academic Relevance and College Submission
This project satisfies curriculum criteria for:
- Object-Oriented Software Engineering (Encapsulation, Polymorphism)
- Database Management Systems (Foreign Keys, Relational Normalization, Indexing)
- Business Logic Automation & Transaction Processing
- Desktop User Interface Design with Java Swing

---

## 14. Known Limitations
- RFID gate anti-theft hardware integration is simulated via software barcode/ISBN lookup.
- E-book PDF digital rights management (DRM) is not implemented.

---

## 15. Future Enhancements
- Integration of Barcode / QR Code scanner for automated book check-in.
- Email / SMS automated overdue reminders sent to students 2 days before due date.
- Student self-service online renewal portal.

---

## 16. Evaluator Quick Start
```bash
# 1. Compile
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\library\model\*.java src\com\logiclegend\library\dao\*.java src\com\logiclegend\library\service\*.java src\com\logiclegend\library\ui\*.java src\com\logiclegend\library\*.java tests\*.java

# 2. Run automated tests
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner

# 3. Launch Swing GUI
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.library.Main
```

---

## 17. References
- Oracle Java 21 Documentation: `docs.oracle.com/en/java/javase/21/`
- SQLite JDBC Documentation: `github.com/xerial/sqlite-jdbc`

---

## 18. License
Educational Open-Source License — Free for academic, learning, and college submission use.

