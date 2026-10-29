# Presentation Slides — Movie or Event Ticket Booking System (JV-08)

---

### Slide 1: Title Slide
- **Project Title**: Cinema & Event Ticket Reservation System
- **Project Code**: `JV-08`
- **Domain**: Entertainment Informatics & High-Concurrency Reservation Systems
- **Technology Stack**: Java 21 LTS, Embedded HTTP Server, JDBC, SQLite/MySQL, Bootstrap 5
- **Presenter**: Engineering Candidate

---

### Slide 2: Problem Statement & Motivation
- **Online Ticketing Challenges**:
  - High risk of concurrent double-booking collisions when thousands of users target the same front-row seats.
  - Abandoned carts leave seats locked permanently without expiration timers.
  - Complicated tiered pricing models (Silver, Gold, Platinum VIP).
  - Lack of seamless self-service cancellation that releases inventory immediately.
- **Proposed Solution**: A lightweight, zero-dependency Java web reservation portal featuring an interactive 2D seating matrix, auto-expiring holds, ACID double-booking prevention, and instant digital e-tickets.

---

### Slide 3: System Architecture
- **Layer 1: Web Controller**: Embedded HTTP micro-server on port 8088 serving shows catalog, 2D seat map, checkout, e-tickets, and cancellation forms.
- **Layer 2: Service Layer**: `BookingService` orchestrating expirable holds, double-booking prevention, dynamic pricing, and coupons.
- **Layer 3: Data Access Layer**: `DatabaseManager` providing dual SQLite and MySQL compatibility.
- **Layer 4: Relational Persistence**: 3NF schema for `movies`, `shows`, `seats`, and `bookings`.

---

### Slide 4: 2D Visual Seating Matrix & Tier Structure
- **Interactive Theater Layout**:
  - Screen direction banner at the top of the grid.
  - 8 Rows (A through H) with 10 Columns (1 through 10) = 80 Seats per auditorium.
- **Dynamic Pricing Categories**:
  - **Rows A & B**: *Platinum VIP Recliners* (₹350 - ₹500)
  - **Rows C, D, E, F**: *Gold Club* (₹200 - ₹300)
  - **Rows G & H**: *Silver Classic* (₹120 - ₹180)
- **Status Badges**:
  - Available (White clickable checkboxes), Booked (Red disabled badges).

---

### Slide 5: Concurrency Protection & Hold Timers
- **ACID Double-Booking Guard**: Check-and-reserve executed inside `conn.setAutoCommit(false)`. If any chosen seat was claimed in the interim, the transaction is rolled back.
- **Passive Hold Expiration Sweeper**: Stale holds automatically expire after 180 seconds on the next query, eliminating thread leaks.

---

### Slide 6: Promotional Coupons & Financial Math
- **Base Fare**: Sum of chosen seat tier prices.
- **Coupons**: `CINEMA20` (20% off up to ₹150) or `MOVIE50` (₹50 off).
- **Fees & Taxes**: ₹30 convenience fee per ticket + 18% Entertainment GST.
- **Grand Total**: $\text{Taxable Amount} \times 1.18$.

---

### Slide 7: Demonstration Walkthrough
1. **Browse Shows**: Visit `http://localhost:8088/` to view showtimes for *Inception*, *Kalki*, and *Interstellar*.
2. **Select Seats**: Click "Select Seats" to view the 2D visual seating layout with cinema screen orientation.
3. **Pick Seats**: Select Platinum seats `A1` and `A2`.
4. **Apply Coupon**: Enter `CINEMA20` to claim discount.
5. **Confirm Booking**: Fill guest contact details and submit.
6. **Digital E-Ticket**: View the generated entry pass with barcode and click "Print Ticket".
7. **Cancellation**: Visit `/cancel`, input booking reference, and witness `A1` and `A2` instantly turn green for re-booking!

---

### Slide 8: Automated Verification & Testing
- **Test Runner (`tests.TestRunner`)**: 44 assertions across 8 test suites:
  - Catalog CRUD and automated 80-seat matrix generation.
  - Multi-tier seat pricing verification.
  - Successful reservation and status locking.
  - Concurrent double-booking rejection and transaction rollback.
  - Expirable seat hold timer and post-expiration release.
  - Promotional coupon and 18% GST computations.
  - Cancellation workflow and seat restitution.
  - E-Ticket formatting and filesystem export.
- **Result**: 44 / 44 assertions passed (0 failures).

---

### Slide 9: Key Technical Innovations
- **Zero External Dependencies**: Zero Tomcat, Spring, or Node.js runtime requirements.
- **Dual Operating Modes**: High-efficiency Web UI alongside full interactive terminal CLI (`--cli`).
- **Complete ACID Integrity**: Guarantees zero orphaned records or overbooked auditoriums.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: JV-08 delivers a production-grade cinema reservation platform ready for college submission, viva evaluation, and commercial deployment.
- **Future Enhancements**:
  - Payment gateway webhooks (Stripe / Razorpay).
  - Food & Beverage (F&B) combo add-ons during seat checkout.
  - Push notifications via WhatsApp / SMS gateways.

