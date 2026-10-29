package tests;

import com.logiclegend.booking.dao.DatabaseManager;
import com.logiclegend.booking.model.Booking;
import com.logiclegend.booking.model.Movie;
import com.logiclegend.booking.model.Seat;
import com.logiclegend.booking.model.Show;
import com.logiclegend.booking.service.BookingService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Condition was FALSE\n", testName);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-08: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_booking_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        BookingService service = new BookingService(dbManager);

        try {
            testMovieAndShowCreation(service);
            testSeatMatrixTiersAndPricing(service);
            testSuccessfulBookingAndSeatLock(service);
            testDoubleBookingRejectionAndRollback(service);
            testTemporarySeatHoldAndExpiration(service);
            testDiscountCouponAndFinancialMath(service);
            testBookingCancellationAndSeatRelease(service);
            testDigitalTicketFormattingAndFileExport(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) f.delete();
        }

        System.out.println("=================================================================");
        System.out.printf("  TOTAL ASSERTIONS: %d | PASSED: %d | FAILED: %d\n", passed + failed, passed, failed);
        System.out.println("=================================================================");

        System.exit(failed > 0 ? 1 : 0);
    }

    private static void testMovieAndShowCreation(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 1: Movie & Show Catalog Creation ---");
        Movie movie = service.addMovie(
                "Oppenheimer: The Atomic Dawn",
                "Biography / Drama",
                180,
                "English",
                "A",
                "The story of American scientist J. Robert Oppenheimer."
        );

        assertTrue("Movie created with valid ID", movie.getId() > 0);
        assertEquals("Movie title matches", "Oppenheimer: The Atomic Dawn", movie.getTitle());
        assertEquals("Duration matches 180", 180, movie.getDurationMinutes());

        Show show = service.createShow(
                movie.getId(),
                "PVR IMAX Orion Mall",
                "Screen 1",
                "2026-11-01",
                "07:30 PM",
                150.0,
                220.0,
                350.0
        );

        assertTrue("Show created with valid ID", show.getId() > 0);
        assertEquals("Venue matches", "PVR IMAX Orion Mall", show.getVenue());

        List<Seat> seats = service.getSeatsForShow(show.getId());
        assertEquals("Total seat matrix contains 80 seats", 80, seats.size());
    }

    private static void testSeatMatrixTiersAndPricing(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 2: Seat Tier Distribution & Dynamic Pricing ---");
        Movie m = service.addMovie("Gladiator II", "Action", 150, "English", "UA", "Rome re-enters conflict.");
        Show show = service.createShow(m.getId(), "INOX Forum", "Audi 2", "2026-11-01", "06:00 PM", 120.0, 200.0, 300.0);

        List<Seat> seats = service.getSeatsForShow(show.getId());

        Seat seatA1 = seats.stream().filter(s -> "A1".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Row A is PLATINUM tier", "PLATINUM", seatA1.getTier());
        assertEquals("Platinum seat price is 300.0", 300.0, seatA1.getPrice());

        Seat seatC5 = seats.stream().filter(s -> "C5".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Row C is GOLD tier", "GOLD", seatC5.getTier());
        assertEquals("Gold seat price is 200.0", 200.0, seatC5.getPrice());

        Seat seatG8 = seats.stream().filter(s -> "G8".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Row G is SILVER tier", "SILVER", seatG8.getTier());
        assertEquals("Silver seat price is 120.0", 120.0, seatG8.getPrice());
    }

    private static void testSuccessfulBookingAndSeatLock(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 3: Successful Booking & Seat Status Lock ---");
        Movie m = service.addMovie("Dune: Part Two", "Sci-Fi", 166, "English", "UA", "Paul Atreides unites Fremen.");
        Show show = service.createShow(m.getId(), "PVR Gold", "Screen 1", "2026-11-02", "08:00 PM", 150.0, 220.0, 350.0);

        List<String> bookedSeats = List.of("A1", "A2");
        Booking b = service.confirmBooking(show.getId(), "Rajesh Khanna", "rajesh@test.com", "+91 9876543210", bookedSeats, null);

        assertTrue("Booking generated with valid ID", b.getId() > 0);
        assertTrue("Booking reference has BK- prefix", b.getBookingReference().startsWith("BK-"));
        assertEquals("Subtotal for 2 Platinum seats (2 x 350)", 700.0, b.getSubtotal());
        assertEquals("Convenience fee for 2 seats (2 x 30)", 60.0, b.getConvenienceFee());
        assertEquals("Booking status is CONFIRMED", "CONFIRMED", b.getBookingStatus());

        List<Seat> refreshedSeats = service.getSeatsForShow(show.getId());
        Seat a1 = refreshedSeats.stream().filter(s -> "A1".equals(s.getSeatCode())).findFirst().get();
        Seat a2 = refreshedSeats.stream().filter(s -> "A2".equals(s.getSeatCode())).findFirst().get();

        assertEquals("Seat A1 status is BOOKED", "BOOKED", a1.getStatus());
        assertEquals("Seat A2 status is BOOKED", "BOOKED", a2.getStatus());
        assertTrue("Seat A1 isAvailable() returns false", !a1.isAvailable());
    }

    private static void testDoubleBookingRejectionAndRollback(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 4: Double-Booking Prevention & Rollback ---");
        Movie m = service.addMovie("Avatar 3", "Sci-Fi", 190, "English", "UA", "Pandora fires.");
        Show show = service.createShow(m.getId(), "PVR IMAX", "Audi 1", "2026-11-03", "09:00 PM", 150.0, 220.0, 350.0);

        // Guest 1 books B1
        service.confirmBooking(show.getId(), "First Guest", "first@test.com", "9876543201", List.of("B1"), null);

        // Guest 2 attempts to book B1 and B2
        try {
            service.confirmBooking(show.getId(), "Concurrent Guest", "second@test.com", "9876543202", List.of("B1", "B2"), null);
            assertTrue("Expected exception for already booked seat B1", false);
        } catch (SQLException e) {
            assertTrue("Double-booking attempt rejected", e.getMessage().contains("already been booked"));
        }

        // Verify B2 remained available and was not locked (atomicity / rollback)
        List<Seat> seats = service.getSeatsForShow(show.getId());
        Seat b2 = seats.stream().filter(s -> "B2".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Seat B2 remained AVAILABLE after failed transaction", "AVAILABLE", b2.getStatus());
    }

    private static void testTemporarySeatHoldAndExpiration(BookingService service) throws Exception {
        System.out.println("\n--- Test Suite 5: Temporary Expirable Seat Holds ---");
        Movie m = service.addMovie("The Dark Knight", "Action", 152, "English", "UA", "Joker in Gotham.");
        Show show = service.createShow(m.getId(), "INOX Cinema", "Screen 2", "2026-11-04", "05:00 PM", 100.0, 150.0, 250.0);

        // Hold C1 for 1 second
        boolean holdSuccess = service.holdSeats(show.getId(), List.of("C1"), 1, "test-user-123");
        assertTrue("Seat hold returned true", holdSuccess);

        List<Seat> seatsDuringHold = service.getSeatsForShow(show.getId());
        Seat c1Hold = seatsDuringHold.stream().filter(s -> "C1".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Status is HOLD during window", "HOLD", c1Hold.getStatus());
        assertTrue("Seat C1 isAvailable() returns false while on active hold", !c1Hold.isAvailable());

        // Wait 1.2 seconds for hold to expire
        Thread.sleep(1200);

        // Query seats again (auto-triggers releaseExpiredHolds)
        List<Seat> seatsAfterHold = service.getSeatsForShow(show.getId());
        Seat c1Expired = seatsAfterHold.stream().filter(s -> "C1".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Status reverted back to AVAILABLE after timer expiration", "AVAILABLE", c1Expired.getStatus());
        assertTrue("Seat C1 isAvailable() returns true after expiration", c1Expired.isAvailable());
    }

    private static void testDiscountCouponAndFinancialMath(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 6: Promotional Coupon & Tax Mathematics ---");
        Movie m = service.addMovie("Spider-Man", "Action", 140, "English", "U", "Peter Parker.");
        Show show = service.createShow(m.getId(), "Cinepolis", "Audi 3", "2026-11-05", "03:00 PM", 100.0, 200.0, 300.0);

        // 2 Gold seats = 2 x ₹200 = ₹400
        // Coupon CINEMA20 = 20% off ₹400 = ₹80 discount
        // Convenience fee = 2 x ₹30 = ₹60
        // Taxable amount = (400 - 80) + 60 = 320 + 60 = ₹380
        // 18% GST = 380 * 0.18 = ₹68.40
        // Grand Total = 380 + 68.40 = ₹448.40
        Booking b = service.confirmBooking(show.getId(), "Anil Deshmukh", "anil@test.com", "9876543210", List.of("D1", "D2"), "CINEMA20");

        assertEquals("Subtotal is 400.0", 400.0, b.getSubtotal());
        assertEquals("Discount is 80.0", 80.0, b.getDiscountAmount());
        assertEquals("Convenience fee is 60.0", 60.0, b.getConvenienceFee());
        assertEquals("GST Tax is 68.40", 68.40, b.getGstTax());
        assertEquals("Grand total is 448.40", 448.40, b.getGrandTotal());
    }

    private static void testBookingCancellationAndSeatRelease(BookingService service) throws SQLException {
        System.out.println("\n--- Test Suite 7: Booking Cancellation & Seat Release ---");
        Movie m = service.addMovie("Avengers: Endgame", "Action", 181, "English", "UA", "Thanos battle.");
        Show show = service.createShow(m.getId(), "PVR Central", "Audi 1", "2026-11-06", "10:00 PM", 150.0, 220.0, 350.0);

        Booking b = service.confirmBooking(show.getId(), "Sanjay Dutt", "sanjay@test.com", "9876543299", List.of("E1", "E2"), null);
        assertEquals("Initial status is CONFIRMED", "CONFIRMED", b.getBookingStatus());

        boolean cancelled = service.cancelBooking(b.getBookingReference());
        assertTrue("Cancellation method returned true", cancelled);

        Booking afterCancel = service.getBookingByReference(b.getBookingReference());
        assertEquals("Updated booking status is CANCELLED", "CANCELLED", afterCancel.getBookingStatus());

        List<Seat> seats = service.getSeatsForShow(show.getId());
        Seat e1 = seats.stream().filter(s -> "E1".equals(s.getSeatCode())).findFirst().get();
        assertEquals("Seat E1 released back to AVAILABLE", "AVAILABLE", e1.getStatus());
        assertTrue("Seat E1 isAvailable() returns true", e1.isAvailable());
    }

    private static void testDigitalTicketFormattingAndFileExport(BookingService service) throws Exception {
        System.out.println("\n--- Test Suite 8: Digital E-Ticket Formatting & Export ---");
        Movie m = service.addMovie("Oppenheimer", "Biography", 180, "English", "A", "Nuclear physicist.");
        Show show = service.createShow(m.getId(), "PVR Forum", "Screen 2", "2026-11-07", "08:15 PM", 150.0, 220.0, 350.0);

        Booking b = service.confirmBooking(show.getId(), "Sunil Gavaskar", "sunil@cricket.org", "9876543200", List.of("A5", "A6"), null);
        String ticketText = service.generateDigitalTicket(b);

        assertTrue("Ticket contains cinema header", ticketText.contains("LOGIC LEGEND CINEPLEX"));
        assertTrue("Ticket contains booking reference", ticketText.contains(b.getBookingReference()));
        assertTrue("Ticket contains movie title", ticketText.contains("Oppenheimer"));
        assertTrue("Ticket contains seat codes A5, A6", ticketText.contains("A5, A6"));
        assertTrue("Ticket contains barcode graphic", ticketText.contains("[BARCODE]"));

        File exported = service.exportTicketToFile(b, "exports");
        assertTrue("Exported ticket file exists on disk", exported.exists());
        assertTrue("Exported file is non-empty", exported.length() > 50);
    }
}

