# Viva Presentation Slides: JV-01 Online Banking & ATM Simulation

---

### Slide 1: Title Slide
- **Project Title**: Online Banking and ATM Simulation
- **Project Code**: JV-01 (Logic Legend Minor Project Suite)
- **Domain**: Enterprise Java, FinTech, Relational Databases & GUI
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Financial applications cannot tolerate race conditions, partial writes, or security loopholes.
- ATMs must protect accounts against brute-force PIN guessing while offering accurate physical cash dispensing logic.
- Partial transaction failures during inter-account transfers must cleanly rollback without leaving money in limbo.

---

### Slide 3: Proposed Solution
- A standalone Java 21 desktop banking terminal built using Swing and JDBC.
- Zero-configuration embedded SQLite storage (`sqlite-jdbc.jar`) + MySQL production script.
- Cryptographic SHA-256 PIN hashing with 16-byte random salts.
- Automatic 3-strike PIN lockout security state machine.
- Greedy currency note denomination dispenser (₹2000, ₹500, ₹200, ₹100).
- ACID transactional fund transfers with guaranteed rollback on failure.

---

### Slide 4: System Architecture
- **Layer 1: Presentation**: Java Swing GUI (`ATMFrame`) with CardLayout and tactile keypad + Headless CLI.
- **Layer 2: Service / Business Logic**: `BankingService` managing withdrawal limits, balances, and atomic transfers.
- **Layer 3: Security Utilities**: `SecurityUtils` handling CSPRNG salt generation and constant-time SHA-256 verification.
- **Layer 4: Data Access**: `DatabaseManager` managing dual SQLite / MySQL JDBC connections and schemas.

---

### Slide 5: Security Architecture & 3-Strike Lockout
- PIN is never stored in plaintext on disk:
  $$\text{Hash} = \text{SHA-256}(\text{PIN} + \text{Salt})$$
- Verification uses constant-time string byte comparison (`MessageDigest.isEqual`) to defeat timing attacks.
- Failed PIN attempts are tracked in the database:
  - Attempt 1: Warning issued.
  - Attempt 2: Final warning issued.
  - Attempt 3: Account status transitions to `LOCKED`.
  - Administrative unlock workflow resets strike counter.

---

### Slide 6: Cash Dispensing & Denomination Algorithm
- Withdrawals must be multiples of ₹100 and respect a ₹25,000 daily limit.
- Greedy algorithm calculates the exact physical banknotes dispensed:
  - High denominations (₹2000, ₹500) prioritized to minimize note count.
  - Dispenses ₹200 and ₹100 notes for exact change.
- Printed ATM receipt displays the itemized note count breakdown.

---

### Slide 7: ACID Transactional Fund Transfers
- Operates under strict transactional isolation:
  ```java
  conn.setAutoCommit(false);
  try {
      deductSenderBalance();
      creditReceiverBalance();
      logTransferOutTransaction();
      logTransferInTransaction();
      conn.commit(); // Atomic commit
  } catch (Exception e) {
      conn.rollback(); // Complete reversal
  }
  ```
- Prevents partial transfers if network fails, receiver is invalid, or balance is insufficient.

---

### Slide 8: Live Demonstration Highlights
- Inserting demo card for Arjun Sharma (Acc: `1001001001`, PIN: `1234`).
- Testing 3-strike lockout with an incorrect PIN.
- Performing a ₹3,800 cash withdrawal and observing currency note breakdown.
- Executing an atomic fund transfer to Priya Patel (Acc: `1001002002`).
- Inspecting the mini-statement table and printable ATM receipt.

---

### Slide 9: Automated Verification & Testing
- 30/30 Unit test assertions passing:
  - Account creation and unique number generation.
  - Non-plaintext salted PIN verification.
  - 3-strike lockout security state machine.
  - Administrative unlock reset.
  - Cash deposit and balance updates.
  - Denomination note breakdown accuracy.
  - Daily withdrawal limit enforcement.
  - Atomic transfer execution and ACID rollback verification.
  - Mini-statement audit log retrieval.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: A production-quality, fault-tolerant, secure banking simulator.
- **Future Scope**:
  - Two-factor OTP authentication via SMS/Email.
  - Multi-currency forex exchange (USD, EUR, GBP).
  - Web/Mobile companion app using Spring Boot REST APIs.

