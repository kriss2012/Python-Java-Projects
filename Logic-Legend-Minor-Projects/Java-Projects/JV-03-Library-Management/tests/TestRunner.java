package tests;

import com.logiclegend.library.dao.DatabaseManager;
import com.logiclegend.library.model.*;
import com.logiclegend.library.service.LibraryService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
        if (expected == null && actual == null || expected != null && expected.equals(actual)) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-03: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_lib_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        LibraryService service = new LibraryService(dbManager);

        try {
            testAddBookAndInventory(service);
            testMemberRegistrationAndLimits(service);
            testBookIssueWorkflow(service);
            testOutOfStockRejection(service);
            testMemberBorrowLimitRejection(service);
            testOnTimeReturnZeroFine(service);
            testOverdueReturnFineCalculation(service);
            testFinePayment(service);
            testSearchAndPopularityRanking(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) f.delete();
        }

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed (Total: %d)\n", passed, failed, passed + failed);
        System.out.println("=================================================================");

        if (failed > 0) System.exit(1);
    }

    private static void testAddBookAndInventory(LibraryService service) throws SQLException {
        Book b = service.addBook("9780134685991", "Effective Java", "Joshua Bloch", "Computer Science", "Addison-Wesley", "3rd", 4, "Rack CS-1");
        assertTrue("Book created with non-null ID", b != null && b.getId() > 0);
        assertEquals("Total copies is 4", 4, b.getTotalCopies());
        assertEquals("Available copies is 4", 4, b.getAvailableCopies());
        assertTrue("Book isAvailable() returns true", b.isAvailable());
    }

    private static void testMemberRegistrationAndLimits(LibraryService service) throws SQLException {
        Member stu = service.registerMember("Rahul Roy", "rahul@test.edu", "9900112233", "STUDENT");
        assertEquals("Student max books limit is 3", 3, stu.getMaxBooks());
        assertTrue("Student code starts with STU-", stu.getMemberCode().startsWith("STU-"));

        Member fac = service.registerMember("Dr. S. K. Bose", "bose@test.edu", "9900112244", "FACULTY");
        assertEquals("Faculty max books limit is 5", 5, fac.getMaxBooks());
        assertTrue("Faculty code starts with FAC-", fac.getMemberCode().startsWith("FAC-"));
    }

    private static void testBookIssueWorkflow(LibraryService service) throws SQLException {
        Book b = service.addBook("9780132350884", "Clean Code", "Robert C. Martin", "SE", "PH", "1st", 2, "Rack CS-2");
        Member m = service.registerMember("Neha Sen", "neha@test.edu", "9900112255", "STUDENT");

        BookIssue issue = service.issueBook(b.getId(), m.getId(), "2026-11-01");
        assertTrue("Issue record generated", issue != null && issue.getIssueRef().startsWith("ISSUE-"));
        assertEquals("Due date is 14 days later for student", "2026-11-15", issue.getDueDate());

        Book afterIssue = service.getBookById(b.getId());
        assertEquals("Available copies decremented from 2 to 1", 1, afterIssue.getAvailableCopies());
    }

    private static void testOutOfStockRejection(LibraryService service) throws SQLException {
        Book single = service.addBook("9780262033848", "Single Copy Book", "Author X", "CS", "Press", "1st", 1, "Rack CS-3");
        Member m1 = service.registerMember("Borrower 1", "b1@test.edu", "9900112266", "STUDENT");
        Member m2 = service.registerMember("Borrower 2", "b2@test.edu", "9900112277", "STUDENT");

        service.issueBook(single.getId(), m1.getId(), "2026-11-01");

        boolean rejected = false;
        try {
            service.issueBook(single.getId(), m2.getId(), "2026-11-01");
        } catch (IllegalStateException e) {
            rejected = true;
            assertTrue("Rejection mentions OUT OF STOCK", e.getMessage().contains("OUT OF STOCK"));
        }
        assertTrue("Issuing out-of-stock book threw IllegalStateException", rejected);
    }

    private static void testMemberBorrowLimitRejection(LibraryService service) throws SQLException {
        Book b1 = service.addBook("ISBN-1", "Book One", "Author A", "CS", "", "", 5, "Rack A");
        Book b2 = service.addBook("ISBN-2", "Book Two", "Author B", "CS", "", "", 5, "Rack A");
        Book b3 = service.addBook("ISBN-3", "Book Three", "Author C", "CS", "", "", 5, "Rack A");
        Book b4 = service.addBook("ISBN-4", "Book Four", "Author D", "CS", "", "", 5, "Rack A");

        Member stu = service.registerMember("Avid Reader", "reader@test.edu", "9900112288", "STUDENT"); // limit 3

        service.issueBook(b1.getId(), stu.getId(), "2026-11-01");
        service.issueBook(b2.getId(), stu.getId(), "2026-11-01");
        service.issueBook(b3.getId(), stu.getId(), "2026-11-01");

        boolean limitCaught = false;
        try {
            service.issueBook(b4.getId(), stu.getId(), "2026-11-01");
        } catch (IllegalStateException e) {
            limitCaught = true;
            assertTrue("Limit rejection message mentions BORROWING LIMIT", e.getMessage().contains("BORROWING LIMIT REACHED"));
        }
        assertTrue("Exceeding maxBooks threw IllegalStateException", limitCaught);
    }

    private static void testOnTimeReturnZeroFine(LibraryService service) throws SQLException {
        Book b = service.addBook("ISBN-5", "On-Time Book", "Author E", "Lit", "", "", 3, "Rack B");
        Member m = service.registerMember("Punctual User", "punc@test.edu", "9900112299", "STUDENT");

        BookIssue issue = service.issueBook(b.getId(), m.getId(), "2026-11-01"); // due 2026-11-15
        LibraryService.ReturnResult res = service.returnBook(issue.getId(), "2026-11-10"); // returned early

        assertTrue("Return on time succeeds", res.success);
        assertEquals("Overdue days is 0", 0L, res.overdueDays);
        assertEquals("Fine amount is 0.0", 0.0, res.fineAmount);
        assertEquals("Fine status is NONE", "NONE", res.issue.getFineStatus());

        Book afterReturn = service.getBookById(b.getId());
        assertEquals("Available copies incremented back to 3", 3, afterReturn.getAvailableCopies());
    }

    private static void testOverdueReturnFineCalculation(LibraryService service) throws SQLException {
        Book b = service.addBook("ISBN-6", "Overdue Book", "Author F", "Sci", "", "", 2, "Rack C");
        Member m = service.registerMember("Late User", "late@test.edu", "9900112200", "STUDENT");

        BookIssue issue = service.issueBook(b.getId(), m.getId(), "2026-11-01"); // due 2026-11-15
        // Returned 5 days late on 2026-11-20
        LibraryService.ReturnResult res = service.returnBook(issue.getId(), "2026-11-20");

        assertTrue("Overdue return processed", res.success);
        assertEquals("Overdue days calculated as 5", 5L, res.overdueDays);
        assertEquals("Fine calculated as 5 * 5.0 = 25.0", 25.0, res.fineAmount);
        assertEquals("Fine status is PENDING", "PENDING", res.issue.getFineStatus());
    }

    private static void testFinePayment(LibraryService service) throws SQLException {
        Book b = service.addBook("ISBN-7", "Fine Payment Book", "Author G", "Sci", "", "", 2, "Rack D");
        Member m = service.registerMember("Settling User", "settle@test.edu", "9900112201", "STUDENT");

        BookIssue issue = service.issueBook(b.getId(), m.getId(), "2026-10-01");
        LibraryService.ReturnResult res = service.returnBook(issue.getId(), "2026-10-25");
        assertTrue("Overdue fine exists", res.fineAmount > 0);

        boolean paid = service.payFine(issue.getId());
        assertTrue("Fine payment returned true", paid);

        BookIssue updated = service.getIssueById(issue.getId());
        assertEquals("Fine status updated to PAID", "PAID", updated.getFineStatus());
    }

    private static void testSearchAndPopularityRanking(LibraryService service) throws SQLException {
        List<Book> searchRes = service.searchBooks("Clean Code");
        assertTrue("Search found Clean Code", !searchRes.isEmpty());

        List<Map<String, Object>> pop = service.getPopularBooks(5);
        assertTrue("Popular books list returned", pop != null && !pop.isEmpty());
        assertTrue("Top book has borrow_count field", pop.get(0).containsKey("borrow_count"));
    }
}

