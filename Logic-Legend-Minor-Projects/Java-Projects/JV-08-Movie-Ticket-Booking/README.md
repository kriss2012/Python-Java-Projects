# Logic Legend — JV-08: Movie or Event Ticket Booking System

A cinema and event ticket reservation platform engineered in Java 21, native embedded HTTP web server, JDBC, SQLite, and MySQL. The system provides an interactive 2D visual theater seating matrix, strict ACID double-booking prevention, temporary expirable seat hold timers, multi-tiered dynamic seat pricing (Silver, Gold, Platinum), promotional coupon discount processing, automated entertainment GST calculations, booking cancellations with automatic seat release, and digital e-ticket summaries with ASCII barcode simulation.

---

## 1. Project Title and Identifier
- **Project Name**: Movie or Event Ticket Booking System
- **Project Code**: `JV-08`
- **Application Category**: Entertainment Informatics, Reservation Systems & Web Applications
- **Target Audience**: Cinema multiplex operators, event organizers, ticketing agents, computer science students, and faculty evaluators.

---

## 2. Executive Summary
High-traffic cinema and event ticketing systems face concurrent booking collisions ("double-booking"), stale seat holds during abandoned checkouts, and complex multi-tiered pricing calculation errors. The **Movie or Event Ticket Booking System** implements an ACID-compliant reservation engine backed by relational databases. It features an interactive visual seat map with real-time availability badges, auto-expiring temporary seat holds, coupon verification, instantaneous digital e-ticket generation, and a ticket cancellation portal.

---

## 3. Key Features
- **Visual 2D Interactive Seating Matrix**:
  - Displays cinema screen orientation ("🎬 SCREEN THIS WAY 🎬").
  - Categorized seating tiers:
    - **Rows A, B**: *Platinum VIP Recliners* (Top tier pricing).
    - **Rows C, D, E, F**: *Gold Club* (Mid tier pricing).
    - **Rows G, H**: *Silver Classic* (Value tier pricing).
  - Color-coded badges: White (Available), Red (Booked/Occupied), Blue (Currently Selected).
- **Concurrent Double-Booking Prevention**:
  - Employs database transactions (`conn.setAutoCommit(false)`): checks seat status before confirmation.
  - Rejects conflicting concurrent selections and rolls back partial bookings.
- **Temporary Expirable Seat Holds**:
  - Places temporary holds (default 180 seconds) while customers enter checkout details.
  - Automatically sweeps and releases expired holds back to `AVAILABLE` without blocking server threads.
- **Financial & Tax Engine**:
  - Dynamic seat pricing based on tier selection.
  - Promotional coupon discounts (`CINEMA20` for 20% off up to ₹150; `MOVIE50` for ₹50 off).
  - Fixed convenience fee (₹30.00 per ticket) and statutory 18% Entertainment GST.
- **Digital E-Tickets & Receipt Export**:
  - Formatted ASCII digital entry passes containing booking reference, screen details, seat codes, fee breakdowns, and simulated security barcodes.
  - Automatically exported to `exports/TICKET_BK-XXXXXX.txt`.
- **Cancellation & Seat Release Workflow**:
  - Cancellation portal allows users to enter booking references and instantly release reserved seats back to the public pool.
- **Dual Interface Modes**:
  - Modern responsive web portal (`http://localhost:8088/`) with Bootstrap styling.
  - Full-featured interactive terminal console (`--cli`) for headless servers.

---

## 4. System Architecture
1. **HTTP Controller & Web Presentation (`web/BookingWebServer.java`, `Main.java`)**: Manages routes for `/`, `/shows/view`, `/checkout`, `/ticket`, `/cancel`, `/bookings`, and terminal console.
2. **Business Service Layer (`service/BookingService.java`)**: Orchestrates seat matrix generation, hold expiry sweeps, double-booking prevention, coupon discounting, and e-ticket formatting.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: Encapsulates JDBC connections and schema bootstrap for SQLite and MySQL.
4. **Relational Persistence Layer (`model/`, SQLite / MySQL)**: Normalized tables for `movies`, `shows`, `seats`, and `bookings`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **Web Runtime**: Java Core HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **Frontend**: HTML5, CSS3, Modern Bootstrap 5, Vanilla JavaScript
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Bundled zero-dependency `sqlite-jdbc.jar`)
- **Production Database**: MySQL 8.x (Script in `sql/schema_mysql.sql`)
- **Build Tool**: Apache Maven (`pom.xml`) or standard JDK compiler

---

## 6. Directory Layout
```
JV-08-Movie-Ticket-Booking/
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── lib/
│   └── sqlite-jdbc.jar
├── sql/
│   ├── schema_sqlite.sql
│   └── schema_mysql.sql
├── src/
│   └── com/logiclegend/booking/
│       ├── Main.java             # Web launcher & CLI
│       ├── dao/
│       │   └── DatabaseManager.java # Connection & schema bootstrap
│       ├── model/
│       │   ├── Movie.java        # Movie catalog entity
│       │   ├── Show.java         # Showtime & screen entity
│       │   ├── Seat.java         # Seat matrix & status entity
│       │   └── Booking.java      # Reservation & e-ticket record
│       ├── service/
│       │   └── BookingService.java # Transactional ticketing logic
│       └── web/
│           └── BookingWebServer.java # Visual seat map & HTTP routes
└── tests/
    └── TestRunner.java           # Comprehensive 44-assertion test suite
```

---

## 7. Compilation and Setup Guide

### Direct Compilation with Standard JDK
```powershell
cd "Logic-Legend-Minor-Projects\Java-Projects\JV-08-Movie-Ticket-Booking"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\booking\model\*.java src\com\logiclegend\booking\dao\*.java src\com\logiclegend\booking\service\*.java src\com\logiclegend\booking\web\*.java src\com\logiclegend\booking\Main.java tests\TestRunner.java
```

---

## 8. Running the Application

### Mode 1: Web Portal Server (Recommended)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.booking.Main
```
Open your browser at:
- **Now Showing Catalog**: `http://localhost:8088/`
- **My Bookings & E-Tickets**: `http://localhost:8088/bookings`
- **Cancel Ticket Portal**: `http://localhost:8088/cancel`

### Mode 2: Interactive Terminal Console
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.booking.Main --cli
```

---

## 9. Automated Testing
Run the automated test suite executing 44 comprehensive assertions:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Sample test output:
```
=================================================================
  LOGIC LEGEND JV-08: AUTOMATED TEST SUITE EXECUTION
=================================================================
--- Test Suite 1: Movie & Show Catalog Creation ---
  [PASS] Movie created with valid ID
  [PASS] Movie title matches
  [PASS] Duration matches 180
  [PASS] Show created with valid ID
  [PASS] Venue matches
  [PASS] Total seat matrix contains 80 seats
...
=================================================================
  TOTAL ASSERTIONS: 44 | PASSED: 44 | FAILED: 0
=================================================================
```

---

## 10. Financial Ticketing Mathematical Engine
$$\text{Base Fare} = \sum_{s \in \text{Seats}} \text{Price}(s)$$
$$\text{Convenience Fee} = |\text{Seats}| \times 30.00$$
$$\text{Discount} = \begin{cases} \min(150, \text{Base Fare} \times 0.20), & \text{if Coupon} = \text{"CINEMA20"} \\ \min(\text{Base Fare}, 50), & \text{if Coupon} = \text{"MOVIE50"} \\ 0, & \text{otherwise} \end{cases}$$
$$\text{Taxable Amount} = \max(0, \text{Base Fare} - \text{Discount}) + \text{Convenience Fee}$$
$$\text{GST (18\%)} = \text{Taxable Amount} \times 0.18$$
$$\text{Grand Total} = \text{Taxable Amount} + \text{GST}$$

---

## 11. License
Educational Project Suite — Logic Legend Minor Projects. Open source under Apache 2.0 license.

