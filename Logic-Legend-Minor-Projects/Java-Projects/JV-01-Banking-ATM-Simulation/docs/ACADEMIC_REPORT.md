# Academic Project Report: JV-01 Online Banking and ATM Simulation

**Department of Computer Science and Engineering**  
*Undergraduate Minor Project Suite — Logic Legend*

---

## Abstract
Automated Teller Machines (ATMs) and digital banking platforms require stringent fault tolerance, strict transactional atomicity, and resilient security defenses against brute-force intrusion. The **Online Banking and ATM Simulation** is an enterprise-grade desktop software system developed in Java 21 utilizing Swing, JDBC, SQLite, and MySQL. It simulates full ATM lifecycle operations, including salted SHA-256 PIN authentication, an automated 3-strike lockout security state machine, denomination-aware cash dispensing, cash deposits, ACID-compliant atomic inter-bank fund transfers with automated rollback, chronological mini-statement generation, and printable receipts.

---

## System Architecture

```mermaid
flowchart TD
    User["Bank Customer / Examiner"] --> UI["ATMFrame (Java Swing UI / CLI)"]
    UI --> Service["BankingService (Business Logic & ACID Coordinator)"]
    Service --> Security["SecurityUtils (CSPRNG Salt & SHA-256 Hashing)"]
    Service --> DAO["DatabaseManager (JDBC Layer)"]
    DAO --> DB[(Relational Storage: SQLite / MySQL)]
    Service --> Receipt["ASCII Receipt & Statement Generator"]
```

---

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    ACCOUNTS ||--o{ TRANSACTIONS : "records"

    ACCOUNTS {
        int id PK
        string account_number UK
        string holder_name
        string email
        string phone
        string account_type
        string pin_hash
        string pin_salt
        float balance
        float daily_withdrawn
        int failed_pin_attempts
        string status
        timestamp created_at
    }

    TRANSACTIONS {
        int id PK
        string transaction_ref UK
        string account_number FK
        string tx_type
        float amount
        string source_dest_account
        float balance_after
        string description
        timestamp timestamp
    }
```

---

## State Diagram: 3-Strike PIN Lockout Security State Machine

```mermaid
stateDiagram-v2
    [*] --> CardInserted
    CardInserted --> ActiveAccount : Account Found
    ActiveAccount --> Authenticated : Correct PIN
    ActiveAccount --> Strike1 : Invalid PIN (Attempt 1)
    Strike1 --> Authenticated : Correct PIN
    Strike1 --> Strike2 : Invalid PIN (Attempt 2)
    Strike2 --> Authenticated : Correct PIN
    Strike2 --> AccountLocked : Invalid PIN (Attempt 3)
    AccountLocked --> CardEjected : Session Blocked
    AccountLocked --> ActiveAccount : Admin Unlock Reset
    Authenticated --> PerformingTransactions
    PerformingTransactions --> [*] : Card Ejected
```

---

## Sequence Diagram: Atomic Fund Transfer with ACID Guarantee

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Bank Customer
    participant UI as ATMFrame GUI
    participant Service as BankingService
    participant DB as SQLite / MySQL Database

    Customer->>UI: Request Fund Transfer (Receiver Acc, Amount)
    UI->>Service: transferFunds(senderAcc, receiverAcc, amount, remarks)
    Service->>DB: conn.setAutoCommit(false)
    Service->>DB: Query sender & receiver account details
    DB-->>Service: Return account records

    alt Sender Balance - Amount < Minimum Required
        Service->>DB: conn.rollback()
        Service-->>UI: Throw InsufficientFundsException
        UI-->>Customer: Display Transaction Failed (Balances Preserved)
    else Validation Successful
        Service->>DB: UPDATE sender SET balance = balance - amount
        Service->>DB: UPDATE receiver SET balance = balance + amount
        Service->>DB: INSERT INTO transactions (TRANSFER_OUT)
        Service->>DB: INSERT INTO transactions (TRANSFER_IN)
        Service->>DB: conn.commit()
        Service-->>UI: Return Transfer Success
        UI-->>Customer: Display Transfer Receipt & Updated Balance
    end
```

---

## Mathematical Denomination Dispenser Model
When dispensing an amount $W$, the ATM applies a greedy denomination strategy across standard Indian banknotes $D = \{d_1, d_2, d_3, d_4\} = \{2000, 500, 200, 100\}$:
$$\text{For } i = 1 \text{ to } 4:$$
$$n_i = \left\lfloor \frac{R_{i-1}}{d_i} \right\rfloor, \quad R_i = R_{i-1} \pmod{d_i}$$
Where $R_0 = W$.
For instance, given $W = 3,800$:
- $d_1 = 2000 \implies n_1 = \lfloor 3800 / 2000 \rfloor = 1$, Remainder $R_1 = 1800$
- $d_2 = 500 \implies n_2 = \lfloor 1800 / 500 \rfloor = 3$, Remainder $R_2 = 300$
- $d_3 = 200 \implies n_3 = \lfloor 300 / 200 \rfloor = 1$, Remainder $R_3 = 100$
- $d_4 = 100 \implies n_4 = \lfloor 100 / 100 \rfloor = 1$, Remainder $R_4 = 0$
Total notes dispensed: $1 + 3 + 1 + 1 = 6$ notes.

---

## Conclusion
The JV-01 Online Banking and ATM Simulation application provides a complete, fault-tolerant, and secure implementation of commercial ATM terminal software. With self-contained embedded SQLite persistence, 30 automated test assertions, and an intuitive Java Swing GUI, the project sets an exemplary standard for undergraduate academic evaluations.

