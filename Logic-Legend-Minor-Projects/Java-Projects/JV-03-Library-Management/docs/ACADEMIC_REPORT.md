# Academic Project Report: JV-03 Library Management System

**Department of Computer Science and Engineering**  
*Undergraduate Minor Project Suite — Logic Legend*

---

## Abstract
University libraries handle extensive catalogs spanning multiple academic disciplines and manage concurrent book borrowings across student and faculty communities. Manual circulation systems or disjointed spreadsheets frequently lead to lost inventory, uncollected overdue fines, and borrowing limit violations. This report presents the architectural design, implementation, and verification of **Library Management System**, an enterprise software application developed in Java 21, Swing, JDBC, SQLite, and MySQL. The system automates catalog indexing, enforces role-differentiated borrowing thresholds, guarantees atomic inventory decrements/increments upon issue/return, executes automatic daily fine calculations, and generates circulation analytics.

---

## System Architecture

```mermaid
flowchart TD
    User["Librarian / Student Member"] --> UI["LibraryFrame (Java Swing GUI / CLI)"]
    UI --> Service["LibraryService (Circulation & Fine Rules)"]
    Service --> DAO["DatabaseManager (JDBC Layer)"]
    DAO --> DB[(Relational Storage: SQLite / MySQL)]
    Service --> Report["Circulation & Overdue Reports Engine"]
```

---

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    BOOKS ||--o{ BOOK_ISSUES : "issued_as"
    MEMBERS ||--o{ BOOK_ISSUES : "borrows"

    BOOKS {
        int id PK
        string isbn UK
        string title
        string author
        string category
        string publisher
        string edition
        int total_copies
        int available_copies
        string shelf_location
        timestamp created_at
    }

    MEMBERS {
        int id PK
        string member_code UK
        string name
        string email
        string phone
        string member_type
        int max_books
        string status
        timestamp created_at
    }

    BOOK_ISSUES {
        int id PK
        string issue_ref UK
        int book_id FK
        int member_id FK
        date issue_date
        date due_date
        date return_date
        float fine_amount
        string fine_status
        string status
        timestamp created_at
    }
```

---

## Data Flow Diagram: Book Circulation & Return Fine Workflow

```mermaid
flowchart TD
    A["Member Requests to Issue Book"] --> B["LibraryService: Check available_copies > 0"]
    B --> C{"Copies Available?"}
    C -- No --> D["Abort: Throw IllegalStateException (OUT OF STOCK)"]
    C -- Yes --> E["Check Member Active Borrowing Count < max_books"]
    E --> F{"Limit OK?"}
    F -- No --> G["Abort: Borrowing Limit Reached"]
    F -- Yes --> H["Atomic Transaction: Decrement available_copies & INSERT book_issues"]
    H --> I["Issue Confirmed (Due Date: +14 / +30 Days)"]
    I --> J["Member Returns Book"]
    J --> K["Calculate Overdue Days = max(0, Return Date - Due Date)"]
    K --> L["Compute Fine = Overdue Days * INR 5.00"]
    L --> M["Atomic Transaction: Increment available_copies & UPDATE book_issues"]
    M --> N["Generate Return Slip & Record Fine Status"]
```

---

## Sequence Diagram: Book Issue and Return

```mermaid
sequenceDiagram
    autonumber
    actor Librarian as College Librarian
    participant UI as LibraryFrame GUI
    participant Service as LibraryService
    participant DB as SQLite / MySQL DB

    Librarian->>UI: Select Book & Member, Click 'Issue Book'
    UI->>Service: issueBook(bookId, memberId, issueDate)
    Service->>DB: conn.setAutoCommit(false)
    Service->>DB: UPDATE books SET available_copies = available_copies - 1
    Service->>DB: INSERT INTO book_issues (issue_ref, book_id, member_id, due_date, status)
    Service->>DB: conn.commit()
    Service-->>UI: Return BookIssue Object
    UI-->>Librarian: Display Borrowing Slip & Refreshed Catalog

    Librarian->>UI: Select Issued Book, Click 'Return Book'
    UI->>Service: returnBook(issueId, returnDate)
    Service->>DB: conn.setAutoCommit(false)
    Service->>DB: UPDATE books SET available_copies = available_copies + 1
    Service->>DB: UPDATE book_issues SET return_date, fine_amount, status='RETURNED'
    Service->>DB: conn.commit()
    Service-->>UI: ReturnResult (Fine Amount, Overdue Days)
    UI-->>Librarian: Confirmation Dialog & Receipt
```

---

## Mathematical Formulation of Fines
Let $D_{\text{due}}$ be the designated return due date and $D_{\text{return}}$ be the actual return date:
$$\Delta = \text{DAYS}(D_{\text{return}} - D_{\text{due}})$$
$$\text{Overdue Days } O = \begin{cases} \Delta & \text{if } \Delta > 0 \\ 0 & \text{if } \Delta \le 0 \end{cases}$$
$$\text{Late Fine Payable } F = O \times R_{\text{daily}}$$
Where $R_{\text{daily}} = \text{INR 5.00}$.

---

## Conclusion
The JV-03 Library Management System delivers an efficient, reliable, and mathematically verified solution for academic libraries. Incorporating strict transactional integrity, role-differentiated circulation policies, automatic overdue calculations, and zero-configuration portability, the project satisfies all university examination and practical demonstration standards.

