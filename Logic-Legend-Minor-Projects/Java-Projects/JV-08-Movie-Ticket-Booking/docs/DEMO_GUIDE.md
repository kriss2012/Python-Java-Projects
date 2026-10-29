# Evaluator Demonstration Guide — Movie or Event Ticket Booking System (JV-08)

A step-by-step walkthrough for faculty evaluators and examiners to test all cinema reservation, 2D visual seating, concurrent double-booking prevention, and e-ticketing features of the **Movie or Event Ticket Booking System**.

---

## 1. Quick Launch Verification
1. Open PowerShell / Command Prompt.
2. Navigate to project root:
   ```powershell
   cd "Logic-Legend-Minor-Projects\Java-Projects\JV-08-Movie-Ticket-Booking"
   ```
3. Run the automated test suite to confirm zero defects:
   ```powershell
   java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
   ```
   **Expected Outcome**: 44 / 44 assertions PASS in ~3.6 seconds.

---

## 2. Web Portal Demonstration (Primary Evaluation Path)

### Step 1: Launch the Embedded Web Server
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.booking.Main
```
**Console Output Confirmation**:
```
=================================================================
  LOGIC LEGEND JV-08: MOVIE TICKET BOOKING SYSTEM
=================================================================
  [SUCCESS] Embedded Web Server running at:
  >>> Browse Shows:    http://localhost:8088/
  >>> My Bookings:     http://localhost:8088/bookings
  >>> Cancel Ticket:   http://localhost:8088/cancel
=================================================================
```

### Step 2: Browse Now Showing Movies
1. Open your browser and navigate to `http://localhost:8088/`.
2. Observe the movie catalog cards displaying pre-seeded shows:
   - *Inception: Dream Horizon* at PVR IMAX Forum Mall (7:00 PM)
   - *Kalki 2898 AD* at INOX Nexus City Center (9:30 PM)
   - *Interstellar: Beyond Time* at PVR Gold Class Central (4:15 PM)
3. Note the displayed tier prices (Platinum, Gold, Silver).

### Step 3: Explore the Visual 2D Seating Matrix
1. Click **"Select Seats & Book &rarr;"** on *Inception*.
2. Review the visual 2D auditorium map:
   - Screen direction banner: `🎬 CINEMA SCREEN THIS WAY 🎬`.
   - Rows A & B: **Platinum VIP** tier (₹400/seat).
   - Rows C, D, E, F: **Gold Club** tier (₹250/seat).
   - Rows G & H: **Silver Classic** tier (₹180/seat).
   - Status legend: ⬜ Available, 🟥 Booked, 🟦 Selected.
3. Select seats `A1` and `A2` (Platinum VIP).
   - Notice the buttons highlight in blue.

### Step 4: Test Coupon Discount & Guest Checkout
1. In the Guest Checkout Information card on the right, enter:
   - **Full Name**: `Prof. Evaluator`
   - **Email Address**: `evaluator@university.edu`
   - **Mobile Phone**: `+91 9876543210`
   - **Coupon Code**: `CINEMA20` (awards 20% discount up to ₹150).
2. Click **"Confirm Booking & Pay"**.

### Step 5: Verify Digital E-Ticket & Receipt Export
1. You will be redirected to `/ticket?ref=BK-XXXXXX`.
2. Inspect the digital e-ticket:
   - Official letterhead: *LOGIC LEGEND CINEPLEX MULTIPLEX*.
   - Booking reference: `BK-XXXXXX`.
   - Confirmed seats: `A1, A2`.
   - Financial breakdown: Base Fare (₹800.00), Promotional Discount (-₹150.00), Convenience Fees (₹60.00), Entertainment GST (₹127.80), Grand Total (₹837.80).
   - Simulated ASCII barcode.
3. Click **"🖨️ Print Ticket"** to preview hardcopy output.
4. Verify that the file was exported to `exports/TICKET_BK-XXXXXX.txt`.

### Step 6: Test Double-Booking Prevention
1. Return to the home page (`http://localhost:8088/`) and click "Select Seats" on the same *Inception* show.
2. Look at seats `A1` and `A2` on the 2D grid:
   - Notice they are now **red, disabled buttons marked `A1` and `A2`**!
   - They cannot be clicked or selected by any other user!

### Step 7: Test Cancellation & Immediate Seat Release
1. In the navigation bar, click **"Cancel Ticket"** (or visit `/cancel`).
2. Enter your booking reference (e.g., `BK-XXXXXX`) and click **"Confirm Cancellation"**.
3. The booking status updates to `CANCELLED`.
4. Now return to the seat selection page for *Inception*:
   - Observe that `A1` and `A2` have **instantly turned back to white (Available)**!
   - Another customer can now book them immediately!

### Step 8: Search Bookings
1. In the navigation bar, click **"My Bookings"** (or visit `/bookings`).
2. Search by your name `Evaluator` or reference `BK-`.
3. View historical booking records and click "View E-Ticket" to re-inspect any confirmed or cancelled ticket.

---

## 3. Terminal CLI Mode Demonstration (Headless Environment)
Run the application with `--cli`:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.booking.Main --cli
```
- Select Option `1` to view all shows in ASCII tables.
- Select Option `2` to display the 2D seat matrix directly inside the terminal console.
- Select Option `3` to book tickets with promotional coupons and print digital e-tickets.
- Select Option `4` to cancel a booking by reference.
- Select Option `5` to lookup bookings and view e-tickets.

