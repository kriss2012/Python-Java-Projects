# Academic Project Report — Movie or Event Ticket Booking System (JV-08)

**Project Identifier**: `JV-08`  
**Domain**: Entertainment Informatics & High-Concurrency Reservation Systems  
**Academic Level**: Bachelor of Technology / Master of Computer Applications (Minor Project)

---

## 1. Abstract
The **Movie or Event Ticket Booking System** is an interactive cinema reservation and ticketing platform engineered in Java 21. By combining visual 2D seating layouts, multi-tier pricing structures (Platinum, Gold, Silver), expirable temporary seat holds, and atomic ACID transactions, the system prevents concurrent double-booking collisions. The platform integrates promotional coupon discounting, statutory 18% entertainment tax calculations, digital e-ticket generation with simulated barcodes, and a self-service cancellation portal that restores released seats back into active inventory.

---

## 2. System Architecture

```mermaid
flowchart TD
    Guest[Movie Guest / Web Browser] -->|HTTP GET / POST| Controller[BookingWebServer / com.sun.net.httpserver]
    Admin[Cinema Manager] -->|HTTP / CLI Admin| Controller
    
    subgraph Web Controller Layer
        Controller -->|Route /| ShowsHandler[Showtimes Catalog Controller]
        Controller -->|Route /shows/view| SeatMatrixHandler[Visual 2D Seat Matrix Controller]
        Controller -->|Route /checkout| CheckoutHandler[Transactional Booking Controller]
        Controller -->|Route /ticket| TicketHandler[Digital E-Ticket Controller]
        Controller -->|Route /cancel| CancelHandler[Cancellation & Seat Release Controller]
    end
    
    subgraph Business Service Layer
        SeatMatrixHandler --> Service[BookingService]
        CheckoutHandler --> Service
        TicketHandler --> Service
        CancelHandler --> Service
        
        Service --> SeatHoldEngine[Expirable Hold Sweeper]
        Service --> PricingEngine[Dynamic Tier Pricing & Coupon Engine]
        Service --> TransactionCoordinator[ACID Double-Booking Guard]
        Service --> ETicketFormatter[ASCII E-Ticket Generator]
    end
    
    subgraph Persistence Layer
        TransactionCoordinator --> DatabaseManager[DatabaseManager JDBC]
        DatabaseManager --> SQLite[(SQLite Embedded DB)]
        DatabaseManager -.-> MySQL[(MySQL Enterprise DB)]
    end
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    MOVIES ||--o{ SHOWS : schedules
    SHOWS ||--|{ SEATS : configures
    SHOWS ||--o{ BOOKINGS : confirms

    MOVIES {
        int id PK
        string title
        string genre
        int duration_minutes
        string language
        string rating
        text synopsis
        datetime created_at
    }

    SHOWS {
        int id PK
        int movie_id FK
        string venue
        string screen_name
        string show_date
        string show_time
        double silver_price
        double gold_price
        double platinum_price
        datetime created_at
    }

    SEATS {
        int id PK
        int show_id FK
        string seat_code UK
        string row_name
        int seat_number
        string tier
        double price
        string status
        bigint hold_expiry_ms
        string hold_user
    }

    BOOKINGS {
        int id PK
        string booking_reference UK
        int show_id FK
        string movie_title
        string customer_name
        string customer_email
        string customer_phone
        string seats_csv
        double subtotal
        double convenience_fee
        double gst_tax
        double discount_amount
        double grand_total
        string booking_status
        datetime created_at
    }
```

---

## 4. Data Flow Diagram (DFD)

### Level 0: Context Diagram
```mermaid
flowchart LR
    Customer[Guest / Customer] -->|1. Pick Show, Select Seats & Enter Details| System((JV-08 Ticket Reservation System))
    System -->|2. Return Booking Reference & Digital E-Ticket| Customer
    Admin[Cinema Administrator] -->|3. Configure Movies, Screens & Pricing Tiers| System
    System -->|4. Update Seating Status & Booking Audits| Admin
```

### Level 1: Subsystem Decomposition
```mermaid
flowchart TD
    P1[1.0 Movie & Show Scheduling] -->|Generate 80 Seats| DB[(Relational Persistence)]
    P2[2.0 Visual 2D Seating Matrix] -->|Seat Codes| P3[3.0 Temporary Hold & Expire Sweeper]
    P3 --> P4[4.0 Transactional Double-Booking Prevention]
    P4 -->|Write Booking| DB
    P4 --> P5[5.0 Coupon & Tax Financial Computation]
    P5 --> P6[6.0 Digital E-Ticket Formatter & Export]
    DB --> P7[7.0 Cancellation & Inventory Release Engine]
```

---

## 5. Sequence Diagram: Visual Seat Selection and Transactional Checkout

```mermaid
sequenceDiagram
    autonumber
    actor Guest as Movie Guest
    participant Browser as Web Browser (Visual Seat Map)
    participant Web as BookingWebServer
    participant Service as BookingService
    participant DB as Relational Database

    Guest->>Browser: Select Movie & Click 'Select Seats'
    Browser->>Web: GET /shows/view?id=1
    Web->>Service: getSeatsForShow(1)
    Service->>DB: UPDATE seats SET status='AVAILABLE' WHERE status='HOLD' AND expiry < now
    Service->>DB: SELECT * FROM seats WHERE show_id = 1 ORDER BY row_name, seat_number
    DB-->>Service: List<Seat> (80 seats)
    Service-->>Web: Return Seat Entities
    Web-->>Browser: Render 2D Grid (Rows A-H, Cols 1-10) with Screen Banner
    
    Guest->>Browser: Click Seats [A1, A2], Enter Name & Coupon "CINEMA20"
    Guest->>Browser: Click 'Confirm Booking & Pay'
    Browser->>Web: POST /checkout (showId, name, email, phone, seats=[A1, A2], coupon)
    
    Web->>Service: confirmBooking(showId, name, email, phone, seats, coupon)
    Service->>DB: BEGIN TRANSACTION (conn.setAutoCommit(false))
    
    loop For each seat in [A1, A2]
        Service->>DB: SELECT status, price FROM seats WHERE show_id=1 AND seat_code=?
        DB-->>Service: Seat Record
        alt Seat status == 'BOOKED'
            Service-->>DB: ROLLBACK TRANSACTION
            Service-->>Web: Throw DoubleBookingException
        end
    end
    
    Note over Service: Base Fare = 350 + 350 = 700. Coupon = 20% (140). Fee = 60. GST (18%) = 111.60. Total = 731.60
    
    Service->>DB: INSERT INTO bookings VALUES (BK-XXXXXX, ...)
    Service->>DB: UPDATE seats SET status='BOOKED' WHERE show_id=1 AND seat_code IN ('A1', 'A2')
    Service->>DB: COMMIT TRANSACTION
    
    Service->>Service: Generate Digital E-Ticket & Save exports/TICKET_BK-XXXXXX.txt
    Service-->>Web: Booking Entity
    Web-->>Browser: HTTP 302 Redirect to /ticket?ref=BK-XXXXXX
    Browser->>Web: GET /ticket?ref=BK-XXXXXX
    Web-->>Browser: Render Digital E-Ticket with Barcode & Print Action
```

---

## 6. Temporary Expirable Seat Hold Algorithm
To prevent race conditions during customer form input while avoiding permanently locked seats if a user abandons their browser tab:
1. When seats are held, the system sets:
   $$\text{hold\_expiry\_ms} = \text{System.currentTimeMillis()} + (\Delta t \times 1000)$$
2. Whenever `getSeatsForShow()` or `confirmBooking()` is invoked, an automated sweep releases stale holds:
   ```sql
   UPDATE seats SET status = 'AVAILABLE', hold_expiry_ms = 0, hold_user = NULL
   WHERE show_id = ? AND status = 'HOLD' AND hold_expiry_ms < ?;
   ```
3. This eliminates background polling threads while maintaining zero stale holds.

---

## 7. Financial & Statutory Tax Formulations
$$\text{Base Fare} = \sum_{s \in \text{Seats}} \text{Price}(s)$$
$$\text{Convenience Fee} = |\text{Seats}| \times 30.00$$
$$\text{Discount} = \begin{cases} \min(150, \text{Base Fare} \times 0.20), & \text{if Coupon} = \text{"CINEMA20"} \\ \min(\text{Base Fare}, 50), & \text{if Coupon} = \text{"MOVIE50"} \\ 0, & \text{otherwise} \end{cases}$$
$$\text{Taxable Amount} = \max(0, \text{Base Fare} - \text{Discount}) + \text{Convenience Fee}$$
$$\text{GST Amount (18\%)} = \text{Taxable Amount} \times 0.18$$
$$\text{Grand Total} = \text{Taxable Amount} + \text{GST Amount}$$

---

## 8. Verification and Test Results
The automated test runner (`tests/TestRunner.java`) executed 44 assertions across 8 test suites:
- Movie and show scheduling with automated 80-seat matrix initialization.
- Dynamic pricing tier distribution (Rows A-B Platinum, C-F Gold, G-H Silver).
- Successful seat reservation and status locking.
- Rejection and transactional rollback of concurrent double-booking attempts.
- Temporary expirable seat holds and automatic post-expiration release.
- Financial arithmetic: promotional coupons, convenience fees, and 18% GST.
- Booking cancellation and immediate seat release back to active inventory.
- Digital e-ticket formatting, ASCII barcode rendering, and filesystem exports.
- **Result: 44 / 44 assertions passed (100% success rate)**.

