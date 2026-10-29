# Evaluator Live Demonstration Guide: JV-01 Online Banking & ATM Simulation

This guide provides a rapid step-by-step walkthrough for faculty evaluators, viva examiners, and project mentors to compile, run, and test the JV-01 banking application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Compile Java Sources | Clean build with `javac` into `bin/` | [ ] |
| 2 | Run Unit Tests | 30 assertions pass in < 0.2s (`TEST SUMMARY: 30 Passed, 0 Failed`) | [ ] |
| 3 | Authenticate Demo Account | Arjun Sharma logs in with Acc: `1001001001`, PIN: `1234` | [ ] |
| 4 | Test 3-Strike Lockout | 3 wrong PIN attempts lock account; Admin unlocks | [ ] |
| 5 | Test Cash Withdrawal & Notes | Dispenses ₹3,800 as ₹2000x1, ₹500x3, ₹200x1, ₹100x1 | [ ] |
| 6 | Test Atomic Fund Transfer | Funds transfer between accounts with ACID safety | [ ] |

---

## 2. Compilation and Test Execution (Fastest & 100% Deterministic)

### Step 1: Compile Application and Test Suite
```bash
cd Logic-Legend-Minor-Projects/Java-Projects/JV-01-Banking-ATM-Simulation
New-Item -ItemType Directory -Force -Path "bin"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\banking\model\*.java src\com\logiclegend\banking\dao\*.java src\com\logiclegend\banking\service\*.java src\com\logiclegend\banking\ui\*.java src\com\logiclegend\banking\*.java tests\*.java
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

### Step 1: Launch Interactive Banking Terminal
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.banking.Main --cli
```

### Step 2: Test ATM Workflows
1. **Login with Demo Account**:
   - Enter Account Number: `1001001001`
   - Enter Security PIN: `1234`
   - Observe authentication success message for *Arjun Sharma*.
2. **Check Balance (Option 1)**:
   - Available Balance: ₹15,000.00.
3. **Withdraw Cash (Option 2)**:
   - Enter amount: `3800`
   - Observe the printed ATM receipt displaying:
     - `INR 2000 x 1  = INR 2,000`
     - `INR 500  x 3  = INR 1,500`
     - `INR 200  x 1  = INR 200`
     - `INR 100  x 1  = INR 100`
     - Updated Balance: ₹11,200.00.
4. **Transfer Funds (Option 4)**:
   - Recipient Account: `1001002002` (Priya Patel)
   - Transfer Amount: `2000`
   - Remarks: `Semester Fee Contribution`
   - Observe transfer confirmation and updated balance (₹9,200.00).
5. **View Mini-Statement (Option 5)**:
   - Observe chronological audit records showing Initial Deposit, Cash Withdrawal, and Transfer Out.
6. **Logout (Option 6)**:
   - Card ejected, returns to ATM login screen.

### Step 3: Test 3-Strike Lockout & Admin Unlock
1. Enter Account Number: `1001001001`
2. Enter Invalid PIN `0000` (Attempt 1 of 3)
3. Enter Invalid PIN `0000` (Attempt 2 of 3)
4. Enter Invalid PIN `0000` (Attempt 3 of 3 -> **SECURITY LOCKOUT: Account has been LOCKED**)
5. Try logging in again: observe immediate rejection (`ACCOUNT LOCKED`).
6. At the login prompt, enter `admin` and unlock account `1001001001`.
7. Log in with PIN `1234` to verify successful recovery.

---

## 4. Graphical Interface Demonstration (Swing GUI)

### Step 1: Launch Application
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.banking.Main
```

### Step 2: Inspect GUI Elements
1. **ATM Screen**: Clean white display area with card layout.
2. **Physical Keypad**: Numeric buttons (0-9), Yellow CLR button, Green OK button.
3. **Navigation**: Click menu buttons for Fast Cash, Deposits, Transfers, and Mini-Statements.
4. **Receipt Dialog**: Pop-up receipt with banknote breakdowns after successful withdrawals.

---

## 5. Evaluator Viva Questions to Ask the Student
1. *"What guarantees that money isn't lost if the database crashes during a fund transfer?"*
   - Student should explain `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` in `BankingService.transferFunds()`.
2. *"Can someone crack your PIN by stealing the database file?"*
   - Student should explain salted SHA-256 hashing and unique random salts preventing rainbow table lookups.
3. *"How does the ATM decide how many 500 or 100 rupee notes to give you?"*
   - Student should explain the greedy denomination algorithm in `BankingService.calculateDenominations()`.
