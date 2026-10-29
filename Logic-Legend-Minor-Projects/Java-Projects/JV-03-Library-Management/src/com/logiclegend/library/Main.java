package com.logiclegend.library;

import com.logiclegend.library.dao.DatabaseManager;
import com.logiclegend.library.model.*;
import com.logiclegend.library.service.LibraryService;
import com.logiclegend.library.ui.LibraryFrame;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        LibraryService service = new LibraryService(dbManager);

        try {
            service.seedDemoData();
        } catch (Exception e) {
            System.err.println("[WARN] Could not seed demo data: " + e.getMessage());
        }

        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode) {
            runCli(service);
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    LibraryFrame frame = new LibraryFrame(service);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable (" + e.getMessage() + "). Launching CLI terminal.");
                runCli(service);
            }
        }
    }

    public static void runCli(LibraryService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-03: LIBRARY MANAGEMENT SYSTEM (CLI)");
        System.out.println("=================================================================");

        while (true) {
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("  CENTRAL LIBRARY DASHBOARD");
            System.out.println("-------------------------------------------------------------");
            System.out.println("  1. View All Books in Catalog");
            System.out.println("  2. Search Book by Keyword");
            System.out.println("  3. Add New Book to Catalog");
            System.out.println("  4. View Registered Members");
            System.out.println("  5. Register New Member");
            System.out.println("  6. Issue Book to Member");
            System.out.println("  7. Return Book & Calculate Fines");
            System.out.println("  8. View All Circulation History");
            System.out.println("  9. View Overdue Books Report");
            System.out.println("  10. View Popular Books Leaderboard");
            System.out.println("  11. Exit");
            System.out.print("Choose an option [1-11]: ");

            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1" -> {
                    try {
                        List<Book> list = service.getAllBooks();
                        System.out.println("\n--- BOOK CATALOG (" + list.size() + ") ---");
                        System.out.printf("%-4s %-15s %-32s %-22s %-10s %s\n", "ID", "ISBN", "Title", "Author", "Avail/Tot", "Shelf");
                        System.out.println("---------------------------------------------------------------------------------------------------");
                        for (Book b : list) {
                            System.out.printf("%-4d %-15s %-32s %-22s %-10s %s\n",
                                    b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                                    b.getAvailableCopies() + "/" + b.getTotalCopies(), b.getShelfLocation());
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "2" -> {
                    System.out.print("Enter search keyword (Title/Author/Category/ISBN): ");
                    String q = scanner.nextLine().trim();
                    try {
                        List<Book> list = service.searchBooks(q);
                        System.out.println("\n--- SEARCH RESULTS (" + list.size() + ") ---");
                        for (Book b : list) {
                            System.out.printf("  * [%s] '%s' by %s (%d/%d copies available) - Shelf: %s\n",
                                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getAvailableCopies(), b.getTotalCopies(), b.getShelfLocation());
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "3" -> {
                    try {
                        System.out.print("ISBN (13-digit): ");
                        String isbn = scanner.nextLine().trim();
                        System.out.print("Title: ");
                        String title = scanner.nextLine().trim();
                        System.out.print("Author: ");
                        String author = scanner.nextLine().trim();
                        System.out.print("Category: ");
                        String cat = scanner.nextLine().trim();
                        System.out.print("Total Copies: ");
                        int copies = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Shelf Rack Location: ");
                        String shelf = scanner.nextLine().trim();

                        Book b = service.addBook(isbn, title, author, cat, "", "", copies, shelf);
                        System.out.println("\n[SUCCESS] Book added: " + b.getTitle() + " (ID: " + b.getId() + ")");
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "4" -> {
                    try {
                        List<Member> list = service.getAllMembers();
                        System.out.println("\n--- REGISTERED MEMBERS (" + list.size() + ") ---");
                        System.out.printf("%-4s %-12s %-22s %-10s %-12s %-12s %s\n", "ID", "Code", "Name", "Type", "Max Books", "Borrowed", "Status");
                        System.out.println("-------------------------------------------------------------------------------------------");
                        for (Member m : list) {
                            int active = service.getActiveIssuedCount(m.getId());
                            System.out.printf("%-4d %-12s %-22s %-10s %-12d %-12d %s\n",
                                    m.getId(), m.getMemberCode(), m.getName(), m.getMemberType(), m.getMaxBooks(), active, m.getStatus());
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "5" -> {
                    try {
                        System.out.print("Member Full Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Type (STUDENT / FACULTY): ");
                        String type = scanner.nextLine().trim();
                        System.out.print("Email: ");
                        String email = scanner.nextLine().trim();
                        System.out.print("Phone: ");
                        String phone = scanner.nextLine().trim();

                        Member m = service.registerMember(name, email, phone, type);
                        System.out.println("\n[SUCCESS] Member registered: " + m.getName() + " (" + m.getMemberCode() + ", Max Books: " + m.getMaxBooks() + ")");
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "6" -> {
                    try {
                        System.out.print("Enter Book ID: ");
                        int bId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Member ID: ");
                        int mId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Issue Date (YYYY-MM-DD or press Enter for today): ");
                        String dt = scanner.nextLine().trim();
                        if (dt.isEmpty()) dt = LocalDate.now().toString();

                        BookIssue bi = service.issueBook(bId, mId, dt);
                        System.out.println("\n" + service.generateIssueSlip(bi));
                    } catch (Exception e) {
                        System.out.println("\n[ISSUE FAILED] " + e.getMessage());
                    }
                }
                case "7" -> {
                    try {
                        System.out.print("Enter Issue Record ID: ");
                        int iId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Return Date (YYYY-MM-DD or press Enter for today): ");
                        String rDate = scanner.nextLine().trim();
                        if (rDate.isEmpty()) rDate = LocalDate.now().toString();

                        LibraryService.ReturnResult res = service.returnBook(iId, rDate);
                        System.out.println("\n[RETURN RESULT] " + res.message);
                    } catch (Exception e) {
                        System.out.println("\n[RETURN FAILED] " + e.getMessage());
                    }
                }
                case "8" -> {
                    try {
                        List<BookIssue> list = service.getAllIssues();
                        System.out.println("\n--- CIRCULATION RECORDS (" + list.size() + ") ---");
                        System.out.printf("%-4s %-14s %-25s %-18s %-12s %-12s %-10s %s\n", "ID", "Ref", "Book Title", "Member Name", "Due Date", "Return Date", "Fine (INR)", "Status");
                        System.out.println("-----------------------------------------------------------------------------------------------------------------");
                        for (BookIssue bi : list) {
                            System.out.printf("%-4d %-14s %-25s %-18s %-12s %-12s INR %-7,.2f %s\n",
                                    bi.getId(), bi.getIssueRef(),
                                    bi.getBookTitle().length() > 24 ? bi.getBookTitle().substring(0, 24) : bi.getBookTitle(),
                                    bi.getMemberName(), bi.getDueDate(),
                                    bi.getReturnDate() != null ? bi.getReturnDate() : "BORROWED",
                                    bi.getFineAmount(), bi.getStatus());
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "9" -> {
                    try {
                        List<BookIssue> overdue = service.getOverdueIssues(LocalDate.now().toString());
                        System.out.println("\n--- OVERDUE BORROWED BOOKS (" + overdue.size() + ") ---");
                        for (BookIssue o : overdue) {
                            System.out.printf("  * [%s] '%s' borrowed by %s | Due: %s (Status: %s)\n",
                                    o.getIssueRef(), o.getBookTitle(), o.getMemberName(), o.getDueDate(), o.getStatus());
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "10" -> {
                    try {
                        List<Map<String, Object>> pop = service.getPopularBooks(10);
                        System.out.println("\n--- POPULAR BOOKS LEADERBOARD ---");
                        for (Map<String, Object> p : pop) {
                            System.out.printf("  * %-35s (Category: %-18s) - %d borrows\n",
                                    p.get("title"), p.get("category"), p.get("borrow_count"));
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "11" -> {
                    System.out.println("Exiting Library Management System. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Please choose between 1 and 11.");
            }
        }
    }
}

