# Logic Legend — JV-01: Online Banking and ATM Simulation

A robust, enterprise-grade Java desktop banking and ATM terminal simulation application developed using Java 21, Swing, JDBC, SQLite, and MySQL. The system emulates physical ATM operations including secure PIN verification, 3-strike lockout protection, cash withdrawals with currency note denomination calculations, cash deposits, inter-bank atomic fund transfers with ACID transaction rollback, transaction mini-statements, and printable receipt generation.

---

## 1. Project Title and Identifier
- **Project Name**: Online Banking and ATM Simulation
- **Project Code**: `JV-01`
- **Application Category**: Enterprise Banking Systems, FinTech & Desktop GUI
- **Target Audience**: Banking software engineers, college examiners, computer science students, and faculty evaluators.

---

## 2. Executive Summary
ATM software requires rigorous transactional integrity, defense-in-depth security, and error-tolerant architectural design. In financial software, race conditions, partial database writes, or unhandled exceptions can lead to severe balance discrepancies. The **Online Banking and ATM Simulation** solves these challenges by implementing strict database-level ACID transactions (`setAutoCommit(false)`, `commit()`, `rollback()`), cryptographic SHA-256 PIN hashing with unique salts, automated 3-strike lockout mechanisms, greedy denomination note dispensers (₹2000, ₹500, ₹200, ₹100), and dual-database support (embedded zero-config SQLite for instant portability and MySQL for production deployment).

---

## 3. Key Features
- **3-Strike PIN Lockout Protection**: Tracks failed PIN attempts; automatically transitions account status to `LOCKED` upon 3 consecutive failures to defeat brute-force PIN guessing.
- **Cryptographic PIN Security**: PINs are never stored in plaintext; salted SHA-256 hashes with constant-time verification defeat rainbow table and timing attacks.
- **ACID Transactional Fund Transfers**: Inter-account transfers operate under strict atomic database transactions (`conn.setAutoCommit(false)`), guaranteeing that funds are deducted from the sender and credited to the recipient simultaneously, with automatic rollback if any validation fails.
- **Intelligent ATM Denomination Dispenser**: Calculates the exact note breakdown for cash withdrawals (e.g. ₹3,800 is dispensed as ₹2000 × 1, ₹500 × 3, ₹200 × 1, ₹100 × 1).
- **Daily Withdrawal & Minimum Balance Limits**: Enforces a ₹25,000 daily withdrawal cap and preserves minimum balance requirements (₹500 for Savings accounts).
- **Mini-Statement & Transaction Audit Trail**: Maintains a complete immutable historical ledger (`transactions` table) tracking reference IDs, types, amounts, and post-transaction balances.
- **Formatted ATM Receipt Generator**: Generates formatted ASCII banking receipts showing ATM terminal ID, masked account numbers, transaction reference, and notes dispensed.
- **Dual Presentation Layer**: Offers a Java Swing GUI mimicking a physical ATM terminal (screen card layout + tactile PIN pad) alongside an interactive CLI for headless server environments.

---

## 4. System Architecture
The application adheres to a clean, four-layer Object-Oriented architecture:
1. **Presentation Layer (`ui/ATMFrame.java`, `Main.java`)**: Java Swing GUI with card-layout digital display, numeric keypad, and terminal CLI.
2. **Business Service Layer (`service/BankingService.java`, `service/SecurityUtils.java`)**: Account lifecycle, PIN authentication, withdrawal denomination algorithms, daily limits, and atomic transfer coordination.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: JDBC connection management, automatic schema initialization, and transactional connection handling.
4. **Data Persistence Layer (`model/Account.java`, `model/Transaction.java`, SQLite / MySQL)**: Relational tables (`accounts`, `transactions`) enforcing foreign keys and check constraints.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **GUI Framework**: Java Swing & AWT (Standard JDK)
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Embedded via self-contained `sqlite-jdbc.jar` — zero external configuration required!)
- **Production Database**: MySQL 8.x (Supported via `sql/schema_mysql.sql`)
- **Security Primitives**: `java.security.MessageDigest`, `java.security.SecureRandom`

---

## 6. Directory Structure
```
JV-01-Banking-ATM-Simulation/
├── .env.example              # Configuration template for database settings
├── .gitignore                # Git exclusions for compiled classes and databases
├── pom.xml                   # Maven project descriptor
├── README.md                 # 19-section comprehensive documentation
├── lib/
│   └── sqlite-jdbc.jar       # Self-contained zero-dependency SQLite JDBC driver
├── data/                     # Local SQLite database storage directory
├── exports/                  # Exported transaction receipts and statements
├── sql/
│   ├── schema_sqlite.sql     # SQLite relational table definitions
│   └── schema_mysql.sql      # MySQL production database schema
├── docs/                     # Academic evaluation deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   └── com/logiclegend/banking/
│       ├── model/
│       │   ├── Account.java     # Account entity model
│       │   └── Transaction.java # Transaction entity model
│       ├── dao/
│       │   └── DatabaseManager.java # Dual SQLite / MySQL connection manager
│       ├── service/
│       │   ├── SecurityUtils.java  # CSPRNG salt & SHA-256 PIN hashing
│       │   └── BankingService.java # Business rules, withdrawal & ACID transfers
│       ├── ui/
│       │   └── ATMFrame.java       # Java Swing ATM interface & keypad
│       └── Main.java               # Application entry point (GUI + CLI)
└── tests/
    └── TestRunner.java       # Automated standalone unit test suite (30 assertions)
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Java-Projects/JV-01-Banking-ATM-Simulation
   ```
2. Compile all source and test files using JDK 21:
   ```bash
   # On Windows PowerShell:
   New-Item -ItemType Directory -Force -Path "bin"
   javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\banking\model\*.java src\com\logiclegend\banking\dao\*.java src\com\logiclegend\banking\service\*.java src\com\logiclegend\banking\ui\*.java src\com\logiclegend\banking\*.java tests\*.java
   ```

---

## 8. Usage Instructions
### Running the Graphical Application (Swing GUI)
Launch the graphical ATM terminal:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.banking.Main
```
- Pre-seeded Demo Accounts:
  - **Arjun Sharma**: Account Number: `1001001001`, PIN: `1234` (Balance: ₹15,000)
  - **Priya Patel**: Account Number: `1001002002`, PIN: `4321` (Balance: ₹45,000)
  - **TechCorp**: Account Number: `1001003003`, PIN: `9999` (Balance: ₹125,000)

### Running in Headless / CLI Mode
Launch the interactive terminal interface:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.banking.Main --cli
```

---

## 9. Database Design and Schema
### Accounts Table
- `id`: Primary key autoincrement
- `account_number`: Unique 10-digit identifier (VARCHAR/TEXT)
- `holder_name`, `email`, `phone`, `account_type` (`SAVINGS`/`CURRENT`)
- `pin_hash`: Hexadecimal SHA-256 digest
- `pin_salt`: Hexadecimal 16-byte random salt
- `balance`: Numeric balance (CHECK `balance >= 0`)
- `daily_withdrawn`: Cumulative withdrawals for current cycle
- `failed_pin_attempts`: Integer counter (triggers lockout at 3)
- `status`: `ACTIVE`, `LOCKED`, or `CLOSED`

### Transactions Table
- `id`: Primary key autoincrement
- `transaction_ref`: Unique reference code (`TX...`)
- `account_number`: Foreign key referencing `accounts(account_number)`
- `tx_type`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_OUT`, `TRANSFER_IN`
- `amount`, `balance_after`, `description`, `timestamp`

---

## 10. ACID Transaction Implementation
In `BankingService.transferFunds()`:
```java
conn.setAutoCommit(false);
try {
    // 1. Verify sender balance >= minimum balance
    // 2. Verify receiver status is ACTIVE
    // 3. Deduct from sender balance
    // 4. Credit to receiver balance
    // 5. Insert TRANSFER_OUT transaction
    // 6. Insert TRANSFER_IN transaction
    conn.commit(); // Atomic success
} catch (Exception e) {
    conn.rollback(); // Complete reversal if anything fails
    throw e;
}
```

---

## 11. Testing and Verification
Execute the automated test suite:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
**30/30 Unit Test Assertions Passing**:
- ✅ Account creation, unique account generation, initial deposit recording
- ✅ Salted SHA-256 PIN hashing validation (non-plaintext)
- ✅ Successful authentication on valid PIN
- ✅ Strike 1, Strike 2 tracking on invalid PINs
- ✅ 3-strike security lockout enforcement
- ✅ Administrative unlock workflow resetting strike counter to 0
- ✅ Deposit balance increment and ledger recording
- ✅ Currency denomination note breakdown (₹2000, ₹500, ₹200, ₹100)
- ✅ Daily withdrawal limit rejection (exceeding ₹25,000)
- ✅ Minimum savings balance violation rejection
- ✅ Atomic fund transfer between accounts
- ✅ ACID rollback verification: balances remain untouched upon transaction failure
- ✅ Mini-statement chronological audit trail

---

## 12. Security Architecture
| Vulnerability | Architectural Defense |
|---|---|
| PIN Snooping | Masked `JPasswordField` on screen; salted SHA-256 on disk |
| Brute-Force Attacks | Automatic account lockout after 3 consecutive wrong PIN entries |
| Partial Write Discrepancy | Database-level atomic transactions (`commit` / `rollback`) |
| SQL Injection | 100% Parameterized queries (`PreparedStatement`) |
| Negative Balance Exploits | Database CHECK constraint (`balance >= 0.0`) + service-level checks |

---

## 13. Academic Relevance and College Submission
This project satisfies curriculum criteria for:
- Advanced Java Programming (Core Java, Swing, JDBC)
- Database Management Systems (ACID Transactions, Foreign Keys, Rollbacks)
- Software Engineering & Design Patterns (DAO, MVC, Service Layer)
- Cryptography & System Security (Salting, Hashing, Lockout State Machines)

---

## 14. Known Limitations
- Networked ATM protocols (e.g. ISO 8583 / NDC) are simulated in-process rather than over wide-area banking networks.
- Physical biometric thumbprint scanners are emulated via PIN security.

---

## 15. Future Enhancements
- Integration of OTP two-factor verification over SMS/Email.
- Cheque clearance deposit scanner simulation.
- Support for multiple currency conversions (USD, EUR, GBP).

---

## 16. Evaluator Quick Start
```bash
# 1. Compile
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\banking\model\*.java src\com\logiclegend\banking\dao\*.java src\com\logiclegend\banking\service\*.java src\com\logiclegend\banking\ui\*.java src\com\logiclegend\banking\*.java tests\*.java

# 2. Run unit tests
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner

# 3. Launch Swing GUI
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.banking.Main
```

---

## 17. References
- Oracle Java 21 SE Documentation: `docs.oracle.com/en/java/javase/21/`
- SQLite JDBC Documentation: `github.com/xerial/sqlite-jdbc`
- NIST SP 800-63B Authentication Guidelines

---

## 18. License
Educational Open-Source License — Free for academic, learning, and college submission use.

