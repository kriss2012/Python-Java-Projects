# Viva Voce Examination Questions & Answers — Movie or Event Ticket Booking System (JV-08)

---

### Q1: How does the system prevent double-booking when multiple users click the same seats at the same second?
**Answer**: Double-booking prevention is enforced via atomic database transactions:
1. `conn.setAutoCommit(false)` is initiated at the start of `confirmBooking()`.
2. For each requested seat code, the server executes a query inside the transaction to check its current status.
3. If any seat is marked as `'BOOKED'` (or held by another user whose hold hasn't expired), the method immediately throws an `IllegalStateException`.
4. The exception triggers `conn.rollback()`, ensuring that none of the other seats in the cart are locked, and the transaction is aborted.
5. In enterprise databases like MySQL, `SELECT ... FOR UPDATE` or unique constraints on `(show_id, seat_code)` guarantee serializability and eliminate race conditions.

---

### Q2: How does the expirable seat hold timer function without consuming thread pool resources?
**Answer**: Rather than allocating a sleeping timer thread or heavy quartz job for every temporary hold (which would exhaust thread pools under high user traffic), the system uses a passive time-check algorithm:
- When a hold is placed, `hold_expiry_ms = System.currentTimeMillis() + (holdDuration * 1000)` is stored in the `seats` table.
- Whenever `getSeatsForShow()` or `confirmBooking()` is queried, a single fast indexed SQL statement runs:
  ```sql
  UPDATE seats SET status = 'AVAILABLE', hold_expiry_ms = 0, hold_user = NULL
  WHERE show_id = ? AND status = 'HOLD' AND hold_expiry_ms < ?;
  ```
- Any hold older than the current timestamp is automatically restored to `AVAILABLE` in $O(1)$ time, eliminating background polling overheads.

---

### Q3: How is dynamic tier pricing structured across the visual 2D seating matrix?
**Answer**: Each auditorium generates an 80-seat grid organized into 8 rows (A through H) with 10 columns:
- **Rows A and B**: `PLATINUM` (e.g. ₹350 - ₹500, VIP recliners in prime viewing location).
- **Rows C, D, E, F**: `GOLD` (e.g. ₹200 - ₹300, premium central seating).
- **Rows G and H**: `SILVER` (e.g. ₹120 - ₹180, standard value rows).
The total base fare is dynamically calculated by summing the individual tier prices of the selected seats:
$$\text{Base Fare} = \sum_{s \in \text{Selected Seats}} \text{Seat.getPrice}()$$

---

### Q4: How does the promotional coupon engine compute discounts?
**Answer**: The coupon engine validates the coupon code string against active discount rules:
- `CINEMA20`: Awards a 20% discount on the base ticket fare, capped at a maximum deduction of ₹150.00:
  $$\text{Discount} = \min(150.00, \text{Base Fare} \times 0.20)$$
- `MOVIE50`: Deducts a flat ₹50.00 off the base fare (clamped to the base fare to prevent negative values):
  $$\text{Discount} = \min(\text{Base Fare}, 50.00)$$
Discounts apply exclusively to base ticket fares before convenience fees and GST are applied.

---

### Q5: How are statutory entertainment taxes and convenience fees computed?
**Answer**:
1. Convenience fees are computed per booked ticket:
   $$\text{Convenience Fee} = \text{Seat Count} \times 30.00$$
2. The taxable amount is the net base fare plus the convenience fee:
   $$\text{Taxable Amount} = (\text{Base Fare} - \text{Discount}) + \text{Convenience Fee}$$
3. Entertainment GST is calculated at 18%:
   $$\text{GST} = \text{Taxable Amount} \times 0.18$$
4. Grand total is rounded to two decimal places:
   $$\text{Grand Total} = \text{Taxable Amount} + \text{GST}$$

---

### Q6: What happens during a booking cancellation?
**Answer**: When `BookingService.cancelBooking(bookingReference)` executes:
1. It validates that the reference exists and is not already cancelled.
2. In an atomic transaction, it updates `bookings.booking_status = 'CANCELLED'`.
3. It parses the comma-separated seat codes (e.g., `"A1, A2"`) and executes:
   ```sql
   UPDATE seats SET status = 'AVAILABLE', hold_expiry_ms = 0, hold_user = NULL
   WHERE show_id = ? AND seat_code = ?;
   ```
4. Commits the transaction. The seats immediately reappear as green/available on the 2D visual seat matrix for other guests to reserve.

---

### Q7: Why are seat codes stored with a composite UNIQUE constraint?
**Answer**: The schema defines:
```sql
UNIQUE(show_id, seat_code)
```
While multiple shows will have a seat named "A1", a single show must NEVER have duplicate rows for "A1". The database composite unique index enforces physical integrity and ensures fast indexed lookups when updating seat states.

---

### Q8: How can this application be deployed with enterprise MySQL instead of SQLite in production?
**Answer**: `DatabaseManager` evaluates the `DB_TYPE` environment variable. If set to `mysql`, it connects to MySQL via `com.mysql.cj.jdbc.Driver`. The project provides `sql/schema_mysql.sql` containing InnoDB tables with foreign keys and `ON DELETE CASCADE` constraints.

---

### Q9: How is the digital e-ticket formatted for mobile and desktop display?
**Answer**: `BookingService.generateDigitalTicket()` formats the ticket into a monospaced ASCII document with cinema letterhead, booking reference, movie title, venue, date/time, confirmed seat numbers, financial breakdown, and an ASCII barcode representation. In the web UI, it is displayed inside a styled card with print and cancellation shortcuts.

---

### Q10: How does the application support headless Linux servers or command-line testing?
**Answer**: Passing `--cli` or `-c` executes `Main.runCli()`. This full terminal interface allows users to list movies, view ASCII representations of the 2D seat matrix (e.g. `Row A [PLATINUM]: [A1] [A2] [XX]`), execute ticket bookings, cancel reservations, and view digital tickets directly in the terminal console.

---

### Q11: What is the time complexity of the seat booking transaction?
**Answer**: Let $K$ be the number of seats requested (typically 1 to 10).
1. Checking seat availability via indexed lookups takes $O(K \log N)$ where $N$ is the number of seats (80).
2. Updating seat statuses in batch takes $O(K)$ time.
3. Inserting the booking record takes $O(1)$ time.
Total transaction time complexity is $O(K \log N)$, completing in under 3 milliseconds.

---

### Q12: Why are batch updates utilized for seat status modifications?
**Answer**: When a customer books 6 seats, issuing 6 separate `PreparedStatement.executeUpdate()` calls incurs repeated database round-trips. By calling `ps.addBatch()` and executing `ps.executeBatch()` once, all seat updates execute atomically in a single round-trip, significantly reducing lock contention on the database engine.

---

### Q13: Can a customer reserve seats without providing their contact information?
**Answer**: No. `BookingService.confirmBooking()` enforces that `customerName` must not be null or whitespace. Contact details (email and mobile number) are required to transmit digital e-tickets and enable booking reference lookups in case of cancellations.

---

### Q14: How does the 2D visual seat matrix render occupied seats in HTML?
**Answer**: In `BookingWebServer.SeatMatrixHandler`, each seat is evaluated:
- If `seat.isAvailable()` is false, the server renders a disabled red button:
  `<button type="button" class="btn btn-sm btn-danger disabled">A1</button>`
- If available, it renders an interactive checkbox styled as a Bootstrap button:
  `<input type="checkbox" name="seats" value="A1" class="btn-check" id="seat_A1">`
This prevents client-side tampering with booked seats.

---

### Q15: How are booking reference codes generated?
**Answer**: Booking references are formatted as:
$$\text{Ref} = \text{"BK-"} + \text{CSPRNG Random Integer in } [100000, 999999]$$
The `booking_reference` column in the database has a `UNIQUE` constraint, guaranteeing that every customer receives a distinct ticket identifier.

