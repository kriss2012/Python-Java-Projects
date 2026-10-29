package com.logiclegend.booking.web;

import com.logiclegend.booking.model.Booking;
import com.logiclegend.booking.model.Movie;
import com.logiclegend.booking.model.Seat;
import com.logiclegend.booking.model.Show;
import com.logiclegend.booking.service.BookingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Embedded Web Server providing visual 2D interactive seating,
 * real-time checkout, digital e-tickets, and cancellation workflows.
 */
public class BookingWebServer {
    private final BookingService service;
    private final int port;
    private HttpServer server;

    public BookingWebServer(BookingService service, int port) {
        this.service = service;
        this.port = port > 0 ? port : 8088;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ShowsCatalogHandler());
        server.createContext("/shows/view", new SeatMatrixHandler());
        server.createContext("/checkout", new CheckoutHandler());
        server.createContext("/ticket", new TicketHandler());
        server.createContext("/cancel", new CancelHandler());
        server.createContext("/bookings", new BookingsListHandler());

        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // -------------------------------------------------------------------------
    // HANDLERS
    // -------------------------------------------------------------------------

    private class ShowsCatalogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Show> shows = service.getAllShows();
                StringBuilder html = new StringBuilder();
                appendHeader(html, "Now Showing — Cinema Ticket Portal");

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">Now Showing in Theatres</h2>
                            <p class="text-muted">Select a movie, theater venue, and showtime to pick your seats</p>
                        </div>
                        <div>
                            <a href="/cancel" class="btn btn-outline-danger me-2">Cancel Ticket</a>
                            <a href="/bookings" class="btn btn-outline-secondary">Search Bookings</a>
                        </div>
                    </div>

                    <div class="row g-4">
                """);

                for (Show s : shows) {
                    html.append("""
                        <div class="col-md-6 col-lg-4">
                            <div class="card shadow-sm h-100 border-0">
                                <div class="card-header bg-dark text-white py-3">
                                    <h5 class="card-title fw-bold mb-0">%s</h5>
                                </div>
                                <div class="card-body">
                                    <p class="text-muted mb-2"><i class="bi bi-geo-alt"></i> <strong>Venue:</strong> %s</p>
                                    <p class="text-muted mb-2"><strong>Screen:</strong> %s</p>
                                    <p class="text-muted mb-2"><strong>Date & Time:</strong> <span class="badge bg-primary">%s at %s</span></p>
                                    <div class="bg-light p-2 rounded mb-3">
                                        <small class="text-muted d-block">Platinum: ₹%,.2f | Gold: ₹%,.2f | Silver: ₹%,.2f</small>
                                    </div>
                                    <a href="/shows/view?id=%d" class="btn btn-primary w-100 fw-bold">Select Seats & Book &rarr;</a>
                                </div>
                            </div>
                        </div>
                    """.formatted(
                            escape(s.getMovieTitle()),
                            escape(s.getVenue()),
                            escape(s.getScreenName()),
                            escape(s.getShowDate()), escape(s.getShowTime()),
                            s.getPlatinumPrice(), s.getGoldPrice(), s.getSilverPrice(),
                            s.getId()
                    ));
                }

                html.append("</div>");
                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class SeatMatrixHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            int showId = Integer.parseInt(q.getOrDefault("id", "0"));

            try {
                Show show = service.getShowById(showId);
                if (show == null) {
                    sendError(exchange, 404, "Show not found.");
                    return;
                }

                List<Seat> seats = service.getSeatsForShow(showId);

                // Group seats by row
                Map<String, List<Seat>> rows = new LinkedHashMap<>();
                for (Seat seat : seats) {
                    rows.computeIfAbsent(seat.getRowName(), k -> new ArrayList<>()).add(seat);
                }

                StringBuilder html = new StringBuilder();
                appendHeader(html, "Select Seats — " + show.getMovieTitle());

                html.append("""
                    <div class="mb-3">
                        <a href="/" class="btn btn-sm btn-outline-secondary">&larr; Back to Shows</a>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-body">
                            <div class="row align-items-center">
                                <div class="col-md-8">
                                    <h3 class="fw-bold mb-1">%s</h3>
                                    <p class="text-muted mb-0">%s | %s | %s at %s</p>
                                </div>
                                <div class="col-md-4 text-md-end">
                                    <span class="badge bg-dark fs-6">Silver: ₹%,.0f</span>
                                    <span class="badge bg-warning text-dark fs-6">Gold: ₹%,.0f</span>
                                    <span class="badge bg-info text-dark fs-6">Platinum: ₹%,.0f</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <form method="POST" action="/checkout">
                        <input type="hidden" name="showId" value="%d">
                        <div class="row g-4">
                            <div class="col-lg-8">
                                <div class="card shadow-sm p-4 text-center">
                                    <div class="p-2 mb-4 bg-secondary text-white rounded fw-bold text-uppercase" style="letter-spacing: 2px;">
                                        🎬 CINEMA SCREEN THIS WAY 🎬
                                    </div>

                                    <div class="d-flex justify-content-center gap-4 mb-4">
                                        <div><span class="badge bg-light text-dark border p-2">⬜ Available</span></div>
                                        <div><span class="badge bg-danger p-2">🟥 Booked</span></div>
                                        <div><span class="badge bg-primary p-2">🟦 Selected</span></div>
                                    </div>

                                    <div class="seat-matrix text-center">
                """.formatted(
                        escape(show.getMovieTitle()),
                        escape(show.getVenue()), escape(show.getScreenName()),
                        escape(show.getShowDate()), escape(show.getShowTime()),
                        show.getSilverPrice(), show.getGoldPrice(), show.getPlatinumPrice(),
                        show.getId()
                ));

                for (Map.Entry<String, List<Seat>> entry : rows.entrySet()) {
                    String rowName = entry.getKey();
                    List<Seat> seatList = entry.getValue();
                    String tierName = seatList.get(0).getTier();

                    html.append("<div class='mb-2 d-flex justify-content-center align-items-center gap-1'>");
                    html.append("<span class='fw-bold text-muted me-2' style='width: 25px;'>").append(rowName).append("</span>");

                    for (Seat seat : seatList) {
                        if (!seat.isAvailable()) {
                            html.append("""
                                <button type="button" class="btn btn-sm btn-danger disabled" style="width: 40px; height: 35px; font-size: 11px;" title="%s (Booked)">%s</button>
                            """.formatted(seat.getSeatCode(), seat.getSeatCode()));
                        } else {
                            html.append("""
                                <label class="btn btn-sm btn-outline-dark p-0 d-flex align-items-center justify-content-center" style="width: 40px; height: 35px; font-size: 11px;">
                                    <input type="checkbox" name="seats" value="%s" class="btn-check" id="seat_%s" autocomplete="off">
                                    <label class="btn btn-outline-secondary w-100 h-100 d-flex align-items-center justify-content-center p-0" for="seat_%s">%s</label>
                                </label>
                            """.formatted(seat.getSeatCode(), seat.getSeatCode(), seat.getSeatCode(), seat.getSeatCode()));
                        }
                    }

                    html.append("<span class='badge bg-light text-dark ms-2' style='font-size: 10px;'>").append(tierName).append("</span>");
                    html.append("</div>");
                }

                html.append("""
                                    </div>
                                </div>
                            </div>

                            <div class="col-lg-4">
                                <div class="card shadow-sm">
                                    <div class="card-header bg-primary text-white fw-bold">Guest Checkout Information</div>
                                    <div class="card-body">
                                        <div class="mb-3">
                                            <label class="form-label fw-bold">Full Name *</label>
                                            <input type="text" name="customerName" class="form-control" placeholder="Your name" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label fw-bold">Email Address *</label>
                                            <input type="email" name="customerEmail" class="form-control" placeholder="ticket@example.com" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label fw-bold">Mobile Phone *</label>
                                            <input type="text" name="customerPhone" class="form-control" placeholder="+91 9876543210" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label fw-bold">Coupon Code</label>
                                            <input type="text" name="couponCode" class="form-control" placeholder="Try CINEMA20 or MOVIE50">
                                            <small class="text-muted">Use CINEMA20 for 20% off up to ₹150</small>
                                        </div>
                                        <div class="p-3 bg-light rounded mb-3">
                                            <small class="text-muted d-block">&bull; ₹30 Convenience Fee per ticket</small>
                                            <small class="text-muted d-block">&bull; 18% Entertainment GST added</small>
                                            <small class="text-muted d-block">&bull; Real-time double-booking protection</small>
                                        </div>
                                        <button type="submit" class="btn btn-success w-100 fw-bold py-2 fs-5">Confirm Booking & Pay</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                """);

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<String>> params = parseMultiFormData(body);

            try {
                int showId = Integer.parseInt(params.getOrDefault("showId", List.of("0")).get(0));
                String name = params.getOrDefault("customerName", List.of("")).get(0);
                String email = params.getOrDefault("customerEmail", List.of("")).get(0);
                String phone = params.getOrDefault("customerPhone", List.of("")).get(0);
                String coupon = params.getOrDefault("couponCode", List.of("")).get(0);
                List<String> seats = params.getOrDefault("seats", List.of());

                if (seats.isEmpty()) {
                    sendError(exchange, 400, "Please select at least one seat before confirming checkout.");
                    return;
                }

                Booking b = service.confirmBooking(showId, name, email, phone, seats, coupon);
                service.exportTicketToFile(b, "exports");
                redirect(exchange, "/ticket?ref=" + b.getBookingReference());
            } catch (Exception e) {
                sendError(exchange, 400, "Booking Failed: " + e.getMessage());
            }
        }
    }

    private class TicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String ref = q.get("ref");

            try {
                Booking b = service.getBookingByReference(ref);
                if (b == null) {
                    sendError(exchange, 404, "Booking reference not found.");
                    return;
                }

                String ticketText = service.generateDigitalTicket(b);

                StringBuilder html = new StringBuilder();
                appendHeader(html, "E-Ticket — " + b.getBookingReference());

                html.append("""
                    <div class="row justify-content-center">
                        <div class="col-md-9">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <a href="/" class="btn btn-outline-secondary">&larr; Back to Movies</a>
                                <div>
                                    <button onclick="window.print()" class="btn btn-primary fw-bold me-2">🖨️ Print Ticket</button>
                                    <a href="/cancel?ref=%s" class="btn btn-outline-danger">Cancel Booking</a>
                                </div>
                            </div>
                            <div class="card shadow-sm">
                                <div class="card-body p-4 bg-light">
                                    <pre class="bg-white p-4 border rounded font-monospace fs-6 text-dark mb-0" style="white-space: pre-wrap;">%s</pre>
                                </div>
                            </div>
                        </div>
                    </div>
                """.formatted(escape(b.getBookingReference()), escape(ticketText)));

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseQueryParams(body);
                String ref = params.get("bookingReference");

                try {
                    boolean cancelled = service.cancelBooking(ref);
                    if (cancelled) {
                        redirect(exchange, "/ticket?ref=" + ref);
                    } else {
                        sendError(exchange, 400, "Unable to cancel booking: Either not found or already cancelled.");
                    }
                } catch (SQLException e) {
                    sendError(exchange, 500, "Cancellation error: " + e.getMessage());
                }
                return;
            }

            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String prefill = q.getOrDefault("ref", "");

            StringBuilder html = new StringBuilder();
            appendHeader(html, "Cancel Booking — Cinema Portal");

            html.append("""
                <div class="row justify-content-center">
                    <div class="col-md-6">
                        <div class="card shadow-sm">
                            <div class="card-header bg-danger text-white py-3">
                                <h4 class="mb-0 fw-bold">Cancel Ticket Reservation</h4>
                            </div>
                            <div class="card-body p-4">
                                <p class="text-muted">Enter your booking reference to release your reserved seats.</p>
                                <form method="POST" action="/cancel">
                                    <div class="mb-3">
                                        <label class="form-label fw-bold">Booking Reference Number *</label>
                                        <input type="text" name="bookingReference" class="form-control" placeholder="e.g. BK-123456" value="%s" required>
                                    </div>
                                    <div class="d-flex justify-content-between mt-4">
                                        <a href="/" class="btn btn-outline-secondary">Go Back</a>
                                        <button type="submit" class="btn btn-danger fw-bold">Confirm Cancellation</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            """.formatted(escape(prefill)));

            appendFooter(html);
            sendHtml(exchange, html.toString());
        }
    }

    private class BookingsListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String query = q.get("q");

            try {
                List<Booking> list = service.searchBookings(query);

                StringBuilder html = new StringBuilder();
                appendHeader(html, "Search Bookings — Cinema Portal");

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2 class="fw-bold mb-0">Booking Records & E-Tickets</h2>
                        <a href="/" class="btn btn-outline-secondary">&larr; Back to Shows</a>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-body">
                            <form method="GET" action="/bookings" class="row g-2">
                                <div class="col-md-9">
                                    <input type="text" name="q" class="form-control" placeholder="Search by booking reference (e.g. BK-100234), guest name, or phone..." value="%s">
                                </div>
                                <div class="col-md-3">
                                    <button type="submit" class="btn btn-primary w-100">Search</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-body p-0">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Reference</th>
                                        <th>Movie Title</th>
                                        <th>Guest Name</th>
                                        <th>Seats</th>
                                        <th>Total Paid</th>
                                        <th>Status</th>
                                        <th class="text-end">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                """.formatted(escape(query != null ? query : "")));

                if (list.isEmpty()) {
                    html.append("<tr><td colspan='7' class='text-center py-4 text-muted'>No bookings found.</td></tr>");
                } else {
                    for (Booking b : list) {
                        String badge = "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) ? "bg-success" : "bg-danger";
                        html.append("""
                            <tr>
                                <td class="fw-bold"><a href="/ticket?ref=%s" class="text-decoration-none">%s</a></td>
                                <td>%s</td>
                                <td>%s<br><small class="text-muted">%s</small></td>
                                <td><span class="badge bg-light text-dark border">%s</span></td>
                                <td class="fw-bold">₹%,.2f</td>
                                <td><span class="badge %s">%s</span></td>
                                <td class="text-end"><a href="/ticket?ref=%s" class="btn btn-sm btn-outline-primary">View E-Ticket</a></td>
                            </tr>
                        """.formatted(
                                escape(b.getBookingReference()), escape(b.getBookingReference()),
                                escape(b.getMovieTitle()),
                                escape(b.getCustomerName()), escape(b.getCustomerPhone()),
                                escape(b.getSeatsCsv()),
                                b.getGrandTotal(),
                                badge, escape(b.getBookingStatus()),
                                escape(b.getBookingReference())
                        ));
                    }
                }

                html.append("""
                                </tbody>
                            </table>
                        </div>
                    </div>
                """);

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // TEMPLATE HELPERS
    // -------------------------------------------------------------------------

    private void appendHeader(StringBuilder html, String title) {
        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    body { background-color: #f8f9fa; font-family: 'Segoe UI', system-ui, sans-serif; }
                    .btn-check:checked + .btn-outline-secondary { background-color: #0d6efd; color: white; border-color: #0d6efd; }
                </style>
            </head>
            <body>
            <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4 shadow-sm">
                <div class="container">
                    <a class="navbar-brand text-warning fw-bold" href="/">🍿 LogicLegend Cineplex</a>
                    <div class="navbar-nav ms-auto">
                        <a class="nav-link" href="/">Now Showing</a>
                        <a class="nav-link" href="/bookings">My Bookings</a>
                        <a class="nav-link" href="/cancel">Cancel Ticket</a>
                    </div>
                </div>
            </nav>
            <div class="container mb-5">
        """.formatted(escape(title)));
    }

    private void appendFooter(StringBuilder html) {
        html.append("""
            </div>
            <footer class="footer mt-auto py-3 bg-white border-top text-center text-muted">
                <div class="container">
                    <small>LogicLegend Cineplex & Ticket Reservation System &copy; 2026.</small>
                </div>
            </footer>
            </body>
            </html>
        """);
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private void sendError(HttpExchange exchange, int code, String msg) throws IOException {
        byte[] bytes = ("<h1>" + code + " Error</h1><p>" + escape(msg) + "</p><a href='/'>&larr; Return Home</a>").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private Map<String, List<String>> parseMultiFormData(String body) {
        Map<String, List<String>> params = new HashMap<>();
        if (body == null) return params;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
            }
        }
        return params;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

