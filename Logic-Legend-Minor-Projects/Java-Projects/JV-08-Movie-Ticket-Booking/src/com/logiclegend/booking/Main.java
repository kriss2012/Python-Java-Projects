package com.logiclegend.booking;

import com.logiclegend.booking.dao.DatabaseManager;
import com.logiclegend.booking.model.Booking;
import com.logiclegend.booking.model.Movie;
import com.logiclegend.booking.model.Seat;
import com.logiclegend.booking.model.Show;
import com.logiclegend.booking.service.BookingService;
import com.logiclegend.booking.web.BookingWebServer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Main application launcher for JV-08 Movie or Event Ticket Booking System.
 * Supports embedded web server (http://localhost:8088/) and interactive terminal CLI.
 */
public class Main {
    private static final int DEFAULT_PORT = 8088;

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        BookingService service = new BookingService(dbManager);

        try {
            service.seedDemoData();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding demo booking data notice: " + e.getMessage());
        }

        boolean cliOnly = false;
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            if ("--cli".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i])) {
                cliOnly = true;
            } else if ("--web".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (cliOnly) {
            runCli(service);
        } else {
            try {
                BookingWebServer webServer = new BookingWebServer(service, port);
                webServer.start();
                System.out.println("=================================================================");
                System.out.println("  LOGIC LEGEND JV-08: MOVIE TICKET BOOKING SYSTEM");
                System.out.println("=================================================================");
                System.out.println("  [SUCCESS] Embedded Web Server running at:");
                System.out.println("  >>> Browse Shows:    http://localhost:" + port + "/");
                System.out.println("  >>> My Bookings:     http://localhost:" + port + "/bookings");
                System.out.println("  >>> Cancel Ticket:   http://localhost:" + port + "/cancel");
                System.out.println("=================================================================");
                System.out.println("  Starting concurrent CLI manager (press Ctrl+C or enter 0 to exit)...");
                runCli(service);
                webServer.stop();
            } catch (IOException e) {
                System.err.println("[ERROR] Unable to bind Web Server to port " + port + ": " + e.getMessage());
                System.out.println("[INFO] Falling back to terminal CLI mode...");
                runCli(service);
            }
        }
    }

    public static void runCli(BookingService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("  CINEPLEX TICKET RESERVATION CONSOLE (CLI)");
        System.out.println("-------------------------------------------------------------");

        while (true) {
            System.out.println("\n[MENU]");
            System.out.println("  1. List Now Showing Movies & Showtimes");
            System.out.println("  2. View 2D Seat Matrix for a Show");
            System.out.println("  3. Book Tickets (CLI Checkout)");
            System.out.println("  4. Cancel Booking by Reference");
            System.out.println("  5. Lookup Booking Reference & View E-Ticket");
            System.out.println("  6. Add New Movie to Catalog (Admin)");
            System.out.println("  0. Exit / Shutdown");
            System.out.print("Select an option (0-6): ");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (choice) {
                case "1":
                    listShowsCli(service);
                    break;
                case "2":
                    viewSeatMatrixCli(service, scanner);
                    break;
                case "3":
                    bookTicketsCli(service, scanner);
                    break;
                case "4":
                    cancelBookingCli(service, scanner);
                    break;
                case "5":
                    lookupBookingCli(service, scanner);
                    break;
                case "6":
                    addMovieCli(service, scanner);
                    break;
                case "0":
                    System.out.println("[INFO] Exiting Booking System. Goodbye!");
                    return;
                default:
                    System.out.println("[ERROR] Invalid choice. Choose 0 to 6.");
            }
        }
    }

    private static void listShowsCli(BookingService service) {
        try {
            List<Show> shows = service.getAllShows();
            System.out.println("\n------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-28s | %-25s | %-16s | %-12s | %-10s%n",
                    "ID", "Movie Title", "Venue & Screen", "Date & Time", "Tiers (S/G/P)", "Status");
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            for (Show s : shows) {
                System.out.printf("%-4d | %-28s | %-25s | %-16s | ₹%.0f/%.0f/%.0f | %-10s%n",
                        s.getId(), truncate(s.getMovieTitle(), 28),
                        truncate(s.getVenue() + " (" + s.getScreenName() + ")", 25),
                        s.getShowDate() + " " + s.getShowTime(),
                        s.getSilverPrice(), s.getGoldPrice(), s.getPlatinumPrice(),
                        "ACTIVE");
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch shows: " + e.getMessage());
        }
    }

    private static void viewSeatMatrixCli(BookingService service, Scanner scanner) {
        try {
            listShowsCli(service);
            System.out.print("\nEnter Show ID: ");
            int showId = Integer.parseInt(scanner.nextLine().trim());
            List<Seat> seats = service.getSeatsForShow(showId);
            if (seats.isEmpty()) {
                System.out.println("[ERROR] No seats found for Show ID " + showId);
                return;
            }

            Map<String, List<Seat>> rows = new LinkedHashMap<>();
            for (Seat seat : seats) {
                rows.computeIfAbsent(seat.getRowName(), k -> new ArrayList<>()).add(seat);
            }

            System.out.println("\n=========================================================================");
            System.out.println("                   CINEMA SCREEN THIS WAY");
            System.out.println("=========================================================================");
            for (Map.Entry<String, List<Seat>> entry : rows.entrySet()) {
                System.out.printf("Row %s [%-8s]:  ", entry.getKey(), entry.getValue().get(0).getTier());
                for (Seat s : entry.getValue()) {
                    if (!s.isAvailable()) {
                        System.out.printf("[XX] ");
                    } else {
                        System.out.printf("[%2s] ", s.getSeatCode());
                    }
                }
                System.out.println();
            }
            System.out.println("=========================================================================");
            System.out.println("  Legend: [A1] = Available | [XX] = Booked/Occupied");
            System.out.println("=========================================================================");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to display seat matrix: " + e.getMessage());
        }
    }

    private static void bookTicketsCli(BookingService service, Scanner scanner) {
        try {
            listShowsCli(service);
            System.out.print("\nEnter Show ID to book: ");
            int showId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Seat Codes comma-separated (e.g. A1, A2, B5): ");
            String seatStr = scanner.nextLine().trim();
            List<String> seats = Arrays.stream(seatStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (seats.isEmpty()) {
                System.out.println("[ERROR] No seats entered.");
                return;
            }

            System.out.print("Guest Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Guest Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Guest Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Coupon Code (or press Enter to skip, e.g. CINEMA20): ");
            String coupon = scanner.nextLine().trim();

            Booking b = service.confirmBooking(showId, name, email, phone, seats, coupon);
            File file = service.exportTicketToFile(b, "exports");

            System.out.println("\n" + service.generateDigitalTicket(b));
            System.out.println("[SUCCESS] Digital e-ticket exported to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[ERROR] Booking failed: " + e.getMessage());
        }
    }

    private static void cancelBookingCli(BookingService service, Scanner scanner) {
        try {
            System.out.print("Enter Booking Reference to Cancel (e.g. BK-123456): ");
            String ref = scanner.nextLine().trim();
            boolean cancelled = service.cancelBooking(ref);
            if (cancelled) {
                System.out.println("[SUCCESS] Booking " + ref + " has been successfully CANCELLED. Seats released.");
            } else {
                System.out.println("[ERROR] Booking reference not found or already cancelled.");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Cancellation failed: " + e.getMessage());
        }
    }

    private static void lookupBookingCli(BookingService service, Scanner scanner) {
        try {
            System.out.print("Enter Booking Reference: ");
            String ref = scanner.nextLine().trim();
            Booking b = service.getBookingByReference(ref);
            if (b == null) {
                System.out.println("[ERROR] Booking not found.");
                return;
            }
            System.out.println("\n" + service.generateDigitalTicket(b));
        } catch (SQLException e) {
            System.out.println("[ERROR] Lookup failed: " + e.getMessage());
        }
    }

    private static void addMovieCli(BookingService service, Scanner scanner) {
        try {
            System.out.print("Enter Movie Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Genre: ");
            String genre = scanner.nextLine().trim();
            System.out.print("Duration in Minutes (e.g. 150): ");
            int dur = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Language: ");
            String lang = scanner.nextLine().trim();
            System.out.print("Rating (U, UA, A): ");
            String rating = scanner.nextLine().trim();
            System.out.print("Synopsis: ");
            String synopsis = scanner.nextLine().trim();

            Movie m = service.addMovie(title, genre, dur, lang, rating, synopsis);
            System.out.println("[SUCCESS] Movie added ID #" + m.getId() + " - " + m.getTitle());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to add movie: " + e.getMessage());
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

