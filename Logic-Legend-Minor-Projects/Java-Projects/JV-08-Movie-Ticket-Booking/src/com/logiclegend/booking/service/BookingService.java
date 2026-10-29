package com.logiclegend.booking.service;

import com.logiclegend.booking.dao.DatabaseManager;
import com.logiclegend.booking.model.Booking;
import com.logiclegend.booking.model.Movie;
import com.logiclegend.booking.model.Seat;
import com.logiclegend.booking.model.Show;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core business engine for cinema showtimes, visual 2D seating,
 * concurrent double-booking prevention, temporary expirable holds,
 * dynamic pricing tiers, promotional coupons, and digital e-tickets.
 */
public class BookingService {
    public static final double CONVENIENCE_FEE_PER_SEAT = 30.00;
    public static final double GST_RATE = 0.18; // 18% GST
    public static final int DEFAULT_HOLD_SECONDS = 180; // 3-minute hold

    private final DatabaseManager dbManager;

    public BookingService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------------------
    // MOVIES & SHOWS MANAGEMENT
    // -------------------------------------------------------------------------

    public Movie addMovie(String title, String genre, int durationMinutes,
                          String language, String rating, String synopsis) throws SQLException {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Movie title is required.");
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be at least 1 minute.");

        String sql = """
            INSERT INTO movies (title, genre, duration_minutes, language, rating, synopsis)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        int movieId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title.trim());
            ps.setString(2, genre != null ? genre.trim() : "Action");
            ps.setInt(3, durationMinutes);
            ps.setString(4, language != null ? language.trim() : "English");
            ps.setString(5, rating != null ? rating.trim() : "UA");
            ps.setString(6, synopsis != null ? synopsis.trim() : "");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                movieId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getMovieById(movieId);
    }

    public Movie getMovieById(int id) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMovie(rs);
            }
        }
        return null;
    }

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title ASC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapMovie(rs));
        }
        return list;
    }

    public Show createShow(int movieId, String venue, String screenName,
                           String showDate, String showTime,
                           double silverPrice, double goldPrice, double platinumPrice) throws SQLException {
        Movie movie = getMovieById(movieId);
        if (movie == null) throw new IllegalArgumentException("Movie ID " + movieId + " does not exist.");

        String sql = """
            INSERT INTO shows (movie_id, venue, screen_name, show_date, show_time, silver_price, gold_price, platinum_price)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int showId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, movieId);
            ps.setString(2, venue.trim());
            ps.setString(3, screenName.trim());
            ps.setString(4, showDate.trim());
            ps.setString(5, showTime.trim());
            ps.setDouble(6, silverPrice > 0 ? silverPrice : 150.0);
            ps.setDouble(7, goldPrice > 0 ? goldPrice : 220.0);
            ps.setDouble(8, platinumPrice > 0 ? platinumPrice : 350.0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                showId = rs.next() ? rs.getInt(1) : -1;
            }
        }

        // Initialize 80-seat matrix for this show
        generateSeatMatrixForShow(showId, silverPrice, goldPrice, platinumPrice);

        return getShowById(showId);
    }

    public Show getShowById(int id) throws SQLException {
        String sql = """
            SELECT s.*, m.title AS movie_title
            FROM shows s
            JOIN movies m ON s.movie_id = m.id
            WHERE s.id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapShow(rs);
            }
        }
        return null;
    }

    public List<Show> getAllShows() throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = """
            SELECT s.*, m.title AS movie_title
            FROM shows s
            JOIN movies m ON s.movie_id = m.id
            ORDER BY s.show_date ASC, s.show_time ASC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapShow(rs));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 2D SEAT MATRIX & DYNAMIC EXPOSURE / EXPIRING HOLDS
    // -------------------------------------------------------------------------

    private void generateSeatMatrixForShow(int showId, double silverPrice, double goldPrice, double platinumPrice) throws SQLException {
        String sql = "INSERT INTO seats (show_id, seat_code, row_name, seat_number, tier, price, status) VALUES (?, ?, ?, ?, ?, ?, 'AVAILABLE')";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Rows A, B: PLATINUM (10 seats each)
            for (String row : List.of("A", "B")) {
                for (int i = 1; i <= 10; i++) {
                    addSeatBatch(ps, showId, row + i, row, i, "PLATINUM", platinumPrice);
                }
            }
            // Rows C, D, E, F: GOLD (10 seats each)
            for (String row : List.of("C", "D", "E", "F")) {
                for (int i = 1; i <= 10; i++) {
                    addSeatBatch(ps, showId, row + i, row, i, "GOLD", goldPrice);
                }
            }
            // Rows G, H: SILVER (10 seats each)
            for (String row : List.of("G", "H")) {
                for (int i = 1; i <= 10; i++) {
                    addSeatBatch(ps, showId, row + i, row, i, "SILVER", silverPrice);
                }
            }
            ps.executeBatch();
        }
    }

    private void addSeatBatch(PreparedStatement ps, int showId, String code, String row, int num, String tier, double price) throws SQLException {
        ps.setInt(1, showId);
        ps.setString(2, code);
        ps.setString(3, row);
        ps.setInt(4, num);
        ps.setString(5, tier);
        ps.setDouble(6, price);
        ps.addBatch();
    }

    public List<Seat> getSeatsForShow(int showId) throws SQLException {
        // Automatically release expired holds
        releaseExpiredHolds(showId);

        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE show_id = ? ORDER BY row_name ASC, seat_number ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSeat(rs));
            }
        }
        return list;
    }

    public void releaseExpiredHolds(int showId) throws SQLException {
        long now = System.currentTimeMillis();
        String sql = "UPDATE seats SET status = 'AVAILABLE', hold_expiry_ms = 0, hold_user = NULL WHERE show_id = ? AND status = 'HOLD' AND hold_expiry_ms < ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setLong(2, now);
            ps.executeUpdate();
        }
    }

    public synchronized boolean holdSeats(int showId, List<String> seatCodes, int holdDurationSeconds, String customerId) throws SQLException {
        if (seatCodes == null || seatCodes.isEmpty()) return false;
        releaseExpiredHolds(showId);

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Verify every seat is available
                for (String code : seatCodes) {
                    Seat seat = getSeat(conn, showId, code.trim().toUpperCase());
                    if (seat == null || !seat.isAvailable()) {
                        conn.rollback();
                        throw new IllegalStateException("Seat " + code + " is already occupied or held by another guest.");
                    }
                }

                // Place hold
                long expiry = System.currentTimeMillis() + ((long) (holdDurationSeconds > 0 ? holdDurationSeconds : DEFAULT_HOLD_SECONDS) * 1000L);
                String sql = "UPDATE seats SET status = 'HOLD', hold_expiry_ms = ?, hold_user = ? WHERE show_id = ? AND seat_code = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (String code : seatCodes) {
                        ps.setLong(1, expiry);
                        ps.setString(2, customerId != null ? customerId : "guest");
                        ps.setInt(3, showId);
                        ps.setString(4, code.trim().toUpperCase());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw new SQLException("Hold failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // BOOKING CHECKOUT & DOUBLE-BOOKING PREVENTION (ACID TRANSACTION)
    // -------------------------------------------------------------------------

    public Booking confirmBooking(int showId, String customerName, String customerEmail, String customerPhone,
                                  List<String> seatCodes, String couponCode) throws SQLException {
        if (seatCodes == null || seatCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected.");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }

        Show show = getShowById(showId);
        if (show == null) throw new IllegalArgumentException("Show not found.");

        releaseExpiredHolds(showId);

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double subtotal = 0.0;
                List<String> validCodes = new ArrayList<>();

                // Verify seats are either AVAILABLE or currently held by this customer
                for (String code : seatCodes) {
                    Seat seat = getSeat(conn, showId, code.trim().toUpperCase());
                    if (seat == null) {
                        throw new IllegalStateException("Seat " + code + " does not exist for this show.");
                    }
                    if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
                        throw new IllegalStateException("Seat " + code + " has already been booked by another customer.");
                    }

                    subtotal += seat.getPrice();
                    validCodes.add(seat.getSeatCode());
                }

                // Promotional Coupon Calculation
                double discount = 0.0;
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    String cleanCoupon = couponCode.trim().toUpperCase();
                    if ("CINEMA20".equals(cleanCoupon)) {
                        discount = Math.min(150.0, subtotal * 0.20); // 20% off up to ₹150
                    } else if ("MOVIE50".equals(cleanCoupon)) {
                        discount = Math.min(subtotal, 50.0);
                    }
                }

                double convenienceFee = validCodes.size() * CONVENIENCE_FEE_PER_SEAT;
                double taxableAmount = Math.max(0, subtotal - discount) + convenienceFee;
                double gst = Math.round((taxableAmount * GST_RATE) * 100.0) / 100.0;
                double grandTotal = Math.round((taxableAmount + gst) * 100.0) / 100.0;

                String bookingRef = "BK-" + (100000 + new Random().nextInt(900000));
                String seatsCsv = String.join(", ", validCodes);

                String insertSql = """
                    INSERT INTO bookings (booking_reference, show_id, movie_title, customer_name, customer_email,
                                          customer_phone, seats_csv, subtotal, convenience_fee, gst_tax,
                                          discount_amount, grand_total, booking_status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')
                """;

                int bookingId;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, bookingRef);
                    ps.setInt(2, showId);
                    ps.setString(3, show.getMovieTitle());
                    ps.setString(4, customerName.trim());
                    ps.setString(5, customerEmail != null ? customerEmail.trim() : "");
                    ps.setString(6, customerPhone != null ? customerPhone.trim() : "");
                    ps.setString(7, seatsCsv);
                    ps.setDouble(8, subtotal);
                    ps.setDouble(9, convenienceFee);
                    ps.setDouble(10, gst);
                    ps.setDouble(11, discount);
                    ps.setDouble(12, grandTotal);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        bookingId = rs.next() ? rs.getInt(1) : -1;
                    }
                }

                // Update seats to BOOKED
                String updateSeatSql = "UPDATE seats SET status = 'BOOKED', hold_expiry_ms = 0, hold_user = NULL WHERE show_id = ? AND seat_code = ?";
                try (PreparedStatement psSeat = conn.prepareStatement(updateSeatSql)) {
                    for (String code : validCodes) {
                        psSeat.setInt(1, showId);
                        psSeat.setString(2, code);
                        psSeat.addBatch();
                    }
                    psSeat.executeBatch();
                }

                conn.commit();
                return getBookingById(bookingId);
            } catch (Exception e) {
                conn.rollback();
                throw new SQLException("Booking failed and was rolled back: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean cancelBooking(String bookingReference) throws SQLException {
        if (bookingReference == null) return false;
        Booking b = getBookingByReference(bookingReference);
        if (b == null || "CANCELLED".equalsIgnoreCase(b.getBookingStatus())) return false;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update booking status
                try (PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_reference = ?")) {
                    ps.setString(1, bookingReference.trim().toUpperCase());
                    ps.executeUpdate();
                }

                // Free up seats
                String releaseSql = "UPDATE seats SET status = 'AVAILABLE', hold_expiry_ms = 0, hold_user = NULL WHERE show_id = ? AND seat_code = ?";
                try (PreparedStatement psSeat = conn.prepareStatement(releaseSql)) {
                    for (String code : b.getSeatList()) {
                        psSeat.setInt(1, b.getShowId());
                        psSeat.setString(2, code);
                        psSeat.addBatch();
                    }
                    psSeat.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Booking getBookingById(int id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }
        }
        return null;
    }

    public Booking getBookingByReference(String ref) throws SQLException {
        if (ref == null) return null;
        String sql = "SELECT * FROM bookings WHERE UPPER(booking_reference) = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ref.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }
        }
        return null;
    }

    public List<Booking> searchBookings(String query) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String q = "%" + (query != null ? query.trim().toUpperCase() : "") + "%";
        String sql = """
            SELECT * FROM bookings
            WHERE UPPER(booking_reference) LIKE ? OR UPPER(customer_name) LIKE ? OR customer_phone LIKE ?
            ORDER BY id DESC LIMIT 50
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // DIGITAL E-TICKET GENERATOR & EXPORTS
    // -------------------------------------------------------------------------

    public String generateDigitalTicket(Booking b) throws SQLException {
        if (b == null) return "";
        Show show = getShowById(b.getShowId());

        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                 LOGIC LEGEND CINEPLEX MULTIPLEX\n");
        sb.append("               OFFICIAL DIGITAL CINEMA ENTRY E-TICKET\n");
        sb.append("======================================================================\n");
        sb.append(String.format("Booking Ref:   %-20s Status: %s\n", b.getBookingReference(), b.getBookingStatus()));
        sb.append(String.format("Movie Title:   %-35s\n", b.getMovieTitle()));
        sb.append(String.format("Venue/Screen:  %-35s\n", (show != null ? show.getVenue() + " — " + show.getScreenName() : "Screen 1")));
        sb.append(String.format("Show Timing:   %s at %s\n", (show != null ? show.getShowDate() : "Today"), (show != null ? show.getShowTime() : "07:00 PM")));
        sb.append(String.format("Guest Name:    %-25s Phone: %s\n", b.getCustomerName(), b.getCustomerPhone()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("CONFIRMED SEATS:  [ %s ] (Total: %d Tickets)\n", b.getSeatsCsv(), b.getSeatList().size()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("%50s: %,10.2f\n", "Ticket Base Fare", b.getSubtotal()));
        if (b.getDiscountAmount() > 0) {
            sb.append(String.format("%50s: -%,9.2f\n", "Promotional Discount", b.getDiscountAmount()));
        }
        sb.append(String.format("%50s: %,10.2f\n", "Convenience Fees", b.getConvenienceFee()));
        sb.append(String.format("%50s: %,10.2f\n", "Entertainment GST (18%)", b.getGstTax()));
        sb.append("======================================================================\n");
        sb.append(String.format("%50s: %,10.2f\n", "TOTAL AMOUNT PAID (INR)", b.getGrandTotal()));
        sb.append("======================================================================\n");
        sb.append(" [BARCODE] ||| | |||| || ||| ||||| | || |||| ||| ||||| | ||| || ||||\n");
        sb.append("    Please display this digital ticket or SMS on your phone at entry.\n");
        sb.append("======================================================================\n");
        return sb.toString();
    }

    public File exportTicketToFile(Booking b, String targetDirectory) throws SQLException, IOException {
        File dir = new File(targetDirectory != null ? targetDirectory : "exports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "TICKET_" + b.getBookingReference() + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateDigitalTicket(b));
        }
        return file;
    }

    // -------------------------------------------------------------------------
    // HELPERS & SEED DATA
    // -------------------------------------------------------------------------

    private Seat getSeat(Connection conn, int showId, String seatCode) throws SQLException {
        String sql = "SELECT * FROM seats WHERE show_id = ? AND UPPER(seat_code) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setString(2, seatCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSeat(rs);
            }
        }
        return null;
    }

    private Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setGenre(rs.getString("genre"));
        m.setDurationMinutes(rs.getInt("duration_minutes"));
        m.setLanguage(rs.getString("language"));
        m.setRating(rs.getString("rating"));
        m.setSynopsis(rs.getString("synopsis"));
        m.setCreatedAt(rs.getString("created_at"));
        return m;
    }

    private Show mapShow(ResultSet rs) throws SQLException {
        Show s = new Show();
        s.setId(rs.getInt("id"));
        s.setMovieId(rs.getInt("movie_id"));
        try {
            s.setMovieTitle(rs.getString("movie_title"));
        } catch (SQLException ignored) {}
        s.setVenue(rs.getString("venue"));
        s.setScreenName(rs.getString("screen_name"));
        s.setShowDate(rs.getString("show_date"));
        s.setShowTime(rs.getString("show_time"));
        s.setSilverPrice(rs.getDouble("silver_price"));
        s.setGoldPrice(rs.getDouble("gold_price"));
        s.setPlatinumPrice(rs.getDouble("platinum_price"));
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }

    private Seat mapSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setId(rs.getInt("id"));
        seat.setShowId(rs.getInt("show_id"));
        seat.setSeatCode(rs.getString("seat_code"));
        seat.setRowName(rs.getString("row_name"));
        seat.setSeatNumber(rs.getInt("seat_number"));
        seat.setTier(rs.getString("tier"));
        seat.setPrice(rs.getDouble("price"));
        seat.setStatus(rs.getString("status"));
        seat.setHoldExpiryMs(rs.getLong("hold_expiry_ms"));
        seat.setHoldUser(rs.getString("hold_user"));
        return seat;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setShowId(rs.getInt("show_id"));
        b.setMovieTitle(rs.getString("movie_title"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerEmail(rs.getString("customer_email"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setSeatsCsv(rs.getString("seats_csv"));
        b.setSubtotal(rs.getDouble("subtotal"));
        b.setConvenienceFee(rs.getDouble("convenience_fee"));
        b.setGstTax(rs.getDouble("gst_tax"));
        b.setDiscountAmount(rs.getDouble("discount_amount"));
        b.setGrandTotal(rs.getDouble("grand_total"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }

    public void seedDemoData() throws SQLException {
        if (!getAllMovies().isEmpty()) return;

        Movie m1 = addMovie("Inception: Dream Horizon", "Sci-Fi / Thriller", 148, "English", "UA", "Dom Cobb extracts secrets through corporate subconscious invasion.");
        Movie m2 = addMovie("Kalki 2898 AD", "Mythology / Sci-Fi", 180, "Telugu / Hindi", "UA", "A modern avatar arrives in a dystopian future to protect life.");
        Movie m3 = addMovie("Interstellar: Beyond Time", "Adventure / Sci-Fi", 169, "English", "U", "Astronauts travel through a wormhole in search of a new home for mankind.");

        String today = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        createShow(m1.getId(), "PVR IMAX Forum Mall", "Audi 1 (IMAX)", today, "07:00 PM", 180.0, 250.0, 400.0);
        createShow(m2.getId(), "INOX Nexus City Center", "Screen 3 (Atmos)", today, "09:30 PM", 150.0, 220.0, 350.0);
        createShow(m3.getId(), "PVR Gold Class Central", "Gold Class 2", today, "04:15 PM", 200.0, 300.0, 500.0);
    }
}

