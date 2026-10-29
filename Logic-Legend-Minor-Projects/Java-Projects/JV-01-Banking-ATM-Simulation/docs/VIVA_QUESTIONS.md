# Viva Examination Questions and Answers: JV-01 Online Banking & ATM Simulation

### Q1: What are ACID properties, and how does your application enforce them during fund transfers?
**Answer**: ACID stands for Atomicity, Consistency, Isolation, and Durability:
- **Atomicity**: Either both the sender debit and receiver credit occur, or neither does. We enforce this by disabling auto-commit (`conn.setAutoCommit(false)`), executing both operations, and calling `conn.commit()` only if all steps succeed. If any step fails, `conn.rollback()` restores the original state.
- **Consistency**: Database constraints (e.g. `CHECK(balance >= 0)`) ensure accounts never enter illegal negative states.
- **Isolation**: The transaction runs in an isolated database context, preventing concurrent threads from reading dirty intermediate balances.
- **Durability**: Once `conn.commit()` finishes, the changes are permanently persisted to the SQLite/MySQL storage engine.

### Q2: Why is SHA-256 with a unique salt used for PIN storage instead of plaintext or unsalted MD5?
**Answer**: Storing PINs in plaintext is a catastrophic security violation. Storing simple unsalted MD5 or SHA-256 hashes is vulnerable to pre-computed rainbow table lookups because 4-digit PINs have only 10,000 combinations (`0000` to `9999`). By generating a unique 16-byte cryptographically random salt (`pin_salt`) for every account, rainbow tables become useless because an attacker would have to compute a new dictionary for each unique salt.

### Q3: How does the 3-strike PIN lockout mechanism work?
**Answer**: The `accounts` table stores an integer column `failed_pin_attempts`. When an invalid PIN is entered, the server increments this counter. If `failed_pin_attempts >= 3`, the application sets the account `status = 'LOCKED'`. Any subsequent authentication attempts immediately throw an `IllegalStateException` and abort without checking the PIN. An administrator must explicitly invoke `unlockAccount()` to reset the counter to 0 and re-activate the status.

### Q4: Explain the mathematical algorithm used by the ATM to dispense currency notes.
**Answer**: The application uses a greedy denomination dispensing algorithm across denominations `[2000, 500, 200, 100]`. Starting from the highest denomination (₹2000), it calculates the integer division quotient $n_i = \lfloor R / d_i \rfloor$ and sets the remaining amount to $R \pmod{d_i}$. This ensures the customer receives the minimum total number of physical banknotes for their withdrawal.

### Q5: How does the system prevent SQL injection?
**Answer**: All database interactions throughout `BankingService` and `DatabaseManager` exclusively use `PreparedStatement` with parameterized placeholders (`?`). User inputs are never concatenated directly into raw SQL strings, ensuring that SQL syntax and user data are strictly isolated.

### Q6: Why did you choose SQLite as the default database for this project?
**Answer**: SQLite requires zero installation, configuration, or active server daemon processes. By bundling a self-contained `sqlite-jdbc.jar` in the project, the application compiles and runs out-of-the-box on any evaluator's machine without requiring a running MySQL server. At the same time, we provide a complete `schema_mysql.sql` file so enterprise MySQL can be enabled by setting `DB_TYPE=mysql`.

### Q7: What is the purpose of constant-time comparison in PIN verification?
**Answer**: Standard string comparison methods (`str.equals()`) terminate early upon encountering the first non-matching character, resulting in measurable execution time differences (timing side-channel attacks). We use `MessageDigest.isEqual()`, which compares all bytes in constant time regardless of where the mismatch occurs, defeating timing attacks.

### Q8: What happens if a user tries to withdraw more than their available balance or daily limit?
**Answer**: The `BankingService.withdraw()` method validates both constraints prior to modifying database state:
1. Daily limit constraint: Checks if `daily_withdrawn + amount > DAILY_WITHDRAWAL_LIMIT` (₹25,000).
2. Minimum balance constraint: Checks if `balance - amount < MINIMUM_BALANCE` (₹500 for Savings).
If either rule is violated, the withdrawal is rejected with an informative error message, and no database write occurs.

### Q9: How does the application support both graphical (GUI) and command-line (CLI) modes?
**Answer**: `com.logiclegend.banking.Main` inspects command-line arguments. If `--cli` or `-c` is passed, or if the runtime environment lacks a graphical display server (headless), it launches `runCli()`, providing an interactive terminal loop. Otherwise, it initializes the Java Swing `ATMFrame` using `SwingUtilities.invokeLater()`.

### Q10: How does the system maintain an audit trail of transactions?
**Answer**: Every financial operation (account opening, deposit, withdrawal, transfer-in, transfer-out) automatically writes an immutable record to the `transactions` table with a unique alphanumeric reference (`TX...`), the source/destination account number, the transaction type, the amount, the post-transaction balance, and a timestamp.

### Q11: What design patterns are implemented in this application?
**Answer**:
1. **DAO (Data Access Object) Pattern**: `DatabaseManager` encapsulates low-level connection pooling and database schema management.
2. **Service Layer Pattern**: `BankingService` isolates business logic from the user interface.
3. **MVC (Model-View-Controller)**: Models (`Account`, `Transaction`) represent state, `ATMFrame` represents the view, and event listeners act as controllers.
4. **State Pattern**: Account states transition between `ACTIVE`, `LOCKED`, and `CLOSED`.

### Q12: Why must withdrawals be a multiple of ₹100?
**Answer**: Physical ATM cash cassettes only hold discrete banknote denominations (₹100, ₹200, ₹500, ₹2000). Since ₹100 is the smallest available physical denomination, any requested amount with change (e.g. ₹450 or ₹1,234.50) cannot be physically dispensed by the hardware.

### Q13: How does the application handle concurrent database connections?
**Answer**: The database connection manager creates short-lived connections wrapped in Java `try-with-resources` blocks. SQLite operates with database-level write locks (WAL mode), while MySQL utilizes InnoDB row-level locking to ensure high concurrency without race conditions.

### Q14: What is the purpose of the test suite in `tests/TestRunner.java`?
**Answer**: It acts as an automated regression test harness containing 30 assertions. It spins up an isolated temporary database, creates accounts, tests PIN authentication, asserts 3-strike lockout, tests administrative unlock, performs deposits, verifies banknote denomination calculation, and tests ACID transaction rollbacks without polluting the production database.

### Q15: How can this system be extended for real-world banking integration?
**Answer**: By integrating an ISO 8583 financial transaction messaging protocol gateway, connecting to centralized core banking systems (CBS) such as Finacle or Temenos, and adding Time-based One-Time Password (TOTP) two-factor authentication via SMS/Email.

