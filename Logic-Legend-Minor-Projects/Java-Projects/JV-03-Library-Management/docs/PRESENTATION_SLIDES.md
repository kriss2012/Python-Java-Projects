# Viva Presentation Slides: JV-03 Library Management System

---

### Slide 1: Title Slide
- **Project Title**: Library Management System with Fine Engine
- **Project Code**: JV-03 (Logic Legend Minor Project Suite)
- **Domain**: Educational Informatics, Inventory Management & Desktop GUI
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Academic libraries handle thousands of volumes across hundreds of active student and faculty borrowers.
- Manual card catalogs or static spreadsheets cause inventory loss, untracked overdue books, lost fine revenue, and borrowing quota violations.
- A centralized, automated software solution is required for reliable book tracking.

---

### Slide 3: Proposed Solution
- A standalone Java 21 desktop application built using Swing, JDBC, SQLite, and MySQL.
- Complete book catalog tracking (ISBN-13, title, author, category, total copies, available copies, shelf location).
- Student vs. Faculty membership rules (quota limits and loan periods).
- Automated book issue & return workflows with atomic copy decrement/increment.
- Real-time overdue fine engine (₹5/day).
- Circulation history, overdue reports, and popularity leaderboards.

---

### Slide 4: System Architecture
- **Layer 1: Presentation**: Java Swing GUI with multi-tab navigation + Headless CLI.
- **Layer 2: Service Layer**: `LibraryService` enforcing circulation limits, stock increments, and fine calculations.
- **Layer 3: Data Access Layer**: `DatabaseManager` managing dual SQLite / MySQL JDBC connections and schemas.
- **Layer 4: Data Model**: Strongly typed Java models (`Book`, `Member`, `BookIssue`).

---

### Slide 5: Membership Rules & Quota Control
- **Student Members**:
  - Maximum Books Allowed: 3 books simultaneously.
  - Loan Period: 14 days.
  - Prefix: `STU-XXXX`.
- **Faculty Members**:
  - Maximum Books Allowed: 5 books simultaneously.
  - Loan Period: 30 days.
  - Prefix: `FAC-XXXX`.
- Active issue count is verified prior to issuing; exceeding the quota throws an `IllegalStateException`.

---

### Slide 6: Inventory Integrity & Out-of-Stock Protection
- Every book tracks `total_copies` and `available_copies`.
- Database constraint: `CHECK(available_copies >= 0)`.
- **Atomic Operations**:
  - Issuing a book decrements `available_copies` by 1.
  - If `available_copies == 0`, issue request is rejected as `OUT OF STOCK`.
  - Returning a book increments `available_copies` by 1.
  - Catalog updates and issue records are committed atomically inside a single database transaction.

---

### Slide 7: Overdue Fine Engine
- Fine calculation formula:
  $$\text{Overdue Days} = \max(0, \text{Return Date} - \text{Due Date})$$
  $$\text{Fine Amount} = \text{Overdue Days} \times \text{INR 5.00}$$
- On-time returns: ₹0.00 fine, status `NONE`.
- Late returns: Fine calculated, status set to `PENDING`.
- Librarian can record fine payment (`fine_status = 'PAID'`).

---

### Slide 8: Live Demonstration Highlights
- Browsing pre-seeded catalog (Effective Java, Clean Code, CLRS Algorithms, Sapiens).
- Issuing a book to student member *Aakash Nair* and generating borrowing slip.
- Attempting to issue a book when quota is exceeded -> observing borrowing limit rejection.
- Returning an overdue book and observing automatic fine calculation.
- Inspecting the **Popular Books Leaderboard** and **Overdue Books Report**.

---

### Slide 9: Automated Verification & Testing
- 30/30 Unit test assertions passing:
  - Book catalog creation and inventory tracking.
  - Student and Faculty borrowing limit differentiation.
  - Book issue copy decrementing.
  - Due date calculation (14 days for student).
  - Out-of-stock copy rejection.
  - Member borrowing limit rejection.
  - On-time return with zero fine.
  - Overdue return fine calculation (5 days $\times$ ₹5 = ₹25).
  - Fine payment status transition to `PAID`.
  - Catalog keyword search and popularity ranking.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: A production-ready, fault-tolerant library circulation system.
- **Future Scope**:
  - Barcode / QR code scanner integration for fast book check-out.
  - Automated SMS/Email alerts sent to borrowers 2 days before due date.
  - Student online book reservation portal.

