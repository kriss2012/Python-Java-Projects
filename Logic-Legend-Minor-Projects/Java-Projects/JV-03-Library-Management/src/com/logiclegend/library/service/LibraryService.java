package com.logiclegend.library.service;

import com.logiclegend.library.dao.DatabaseManager;
import com.logiclegend.library.model.Book;
import com.logiclegend.library.model.BookIssue;
import com.logiclegend.library.model.Member;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryService {
    private final DatabaseManager dbManager;
    public static final double DAILY_FINE_RATE = 5.0; // INR per day overdue
    public static final int STUDENT_LOAN_DAYS = 14;
    public static final int FACULTY_LOAN_DAYS = 30;

    public static class ReturnResult {
        public final boolean success;
        public final String message;
        public final BookIssue issue;
        public final long overdueDays;
        public final double fineAmount;

        public ReturnResult(boolean success, String message, BookIssue issue, long overdueDays, double fineAmount) {
            this.success = success;
            this.message = message;
            this.issue = issue;
            this.overdueDays = overdueDays;
            this.fineAmount = fineAmount;
        }
    }

    public LibraryService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------
    // BOOK CATALOG
    // -------------------------------------------------------------
    public Book addBook(String isbn, String title, String author, String category,
                        String publisher, String edition, int totalCopies, String shelfLocation) throws SQLException {
        if (isbn == null || isbn.trim().isEmpty()) throw new IllegalArgumentException("ISBN is required.");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
        if (author == null || author.trim().isEmpty()) throw new IllegalArgumentException("Author is required.");
        if (totalCopies <= 0) throw new IllegalArgumentException("Total copies must be at least 1.");

        String sql = """
            INSERT INTO books (isbn, title, author, category, publisher, edition, total_copies, available_copies, shelf_location)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int bookId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, isbn.trim());
            ps.setString(2, title.trim());
            ps.setString(3, author.trim());
            ps.setString(4, category != null ? category.trim() : "General");
            ps.setString(5, publisher != null ? publisher.trim() : "Academic Press");
            ps.setString(6, edition != null ? edition.trim() : "1st Edition");
            ps.setInt(7, totalCopies);
            ps.setInt(8, totalCopies);
            ps.setString(9, shelfLocation != null ? shelfLocation.trim() : "Rack A-1");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                bookId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getBookById(bookId);
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractBook(rs);
            }
        }
        return null;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title ASC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractBook(rs));
        }
        return list;
    }

    public List<Book> searchBooks(String query) throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? ORDER BY title ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query.trim() + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractBook(rs));
            }
        }
        return list;
    }

    private Book extractBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setPublisher(rs.getString("publisher"));
        b.setEdition(rs.getString("edition"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setShelfLocation(rs.getString("shelf_location"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }

    // -------------------------------------------------------------
    // MEMBER MANAGEMENT
    // -------------------------------------------------------------
    public Member registerMember(String name, String email, String phone, String memberType) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Member name is required.");

        String type = (memberType != null && memberType.equalsIgnoreCase("FACULTY")) ? "FACULTY" : "STUDENT";
        int maxAllowed = "FACULTY".equals(type) ? 5 : 3;
        String mCode = ("FACULTY".equals(type) ? "FAC-" : "STU-") + (1000 + new Random().nextInt(9000));

        String sql = """
            INSERT INTO members (member_code, name, email, phone, member_type, max_books, status)
            VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
        """;
        int mId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mCode);
            ps.setString(2, name.trim());
            ps.setString(3, email != null ? email.trim() : "");
            ps.setString(4, phone != null ? phone.trim() : "");
            ps.setString(5, type);
            ps.setInt(6, maxAllowed);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                mId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getMemberById(mId);
    }

    public Member getMemberById(int memberId) throws SQLException {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractMember(rs);
            }
        }
        return null;
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractMember(rs));
        }
        return list;
    }

    public int getActiveIssuedCount(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_issues WHERE member_id = ? AND status = 'ISSUED'";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Member extractMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getInt("id"));
        m.setMemberCode(rs.getString("member_code"));
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setPhone(rs.getString("phone"));
        m.setMemberType(rs.getString("member_type"));
        m.setMaxBooks(rs.getInt("max_books"));
        m.setStatus(rs.getString("status"));
        m.setCreatedAt(rs.getString("created_at"));
        return m;
    }

    // -------------------------------------------------------------
    // BOOK ISSUE & RETURN WORKFLOWS
    // -------------------------------------------------------------
    public BookIssue issueBook(int bookId, int memberId, String issueDateStr) throws SQLException {
        Book book = getBookById(bookId);
        if (book == null) throw new IllegalArgumentException("Book ID " + bookId + " does not exist.");
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' is currently OUT OF STOCK (0 available copies).");
        }

        Member member = getMemberById(memberId);
        if (member == null || !member.isActive()) {
            throw new IllegalStateException("Member is inactive or does not exist.");
        }

        int activeCount = getActiveIssuedCount(memberId);
        if (activeCount >= member.getMaxBooks()) {
            throw new IllegalStateException(String.format("BORROWING LIMIT REACHED! %s already has %d books issued (Limit: %d).",
                    member.getName(), activeCount, member.getMaxBooks()));
        }

        LocalDate issueDate = (issueDateStr != null && !issueDateStr.trim().isEmpty())
                ? LocalDate.parse(issueDateStr.trim()) : LocalDate.now();
        int loanPeriod = "FACULTY".equalsIgnoreCase(member.getMemberType()) ? FACULTY_LOAN_DAYS : STUDENT_LOAN_DAYS;
        LocalDate dueDate = issueDate.plusDays(loanPeriod);

        String ref = "ISSUE-" + System.currentTimeMillis() % 1000000;
        int issueId;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Decrement copy
                try (PreparedStatement psBook = conn.prepareStatement("UPDATE books SET available_copies = available_copies - 1 WHERE id = ?")) {
                    psBook.setInt(1, bookId);
                    psBook.executeUpdate();
                }

                // Insert issue
                String sqlIssue = """
                    INSERT INTO book_issues (issue_ref, book_id, member_id, issue_date, due_date, status)
                    VALUES (?, ?, ?, ?, ?, 'ISSUED')
                """;
                try (PreparedStatement psIssue = conn.prepareStatement(sqlIssue, Statement.RETURN_GENERATED_KEYS)) {
                    psIssue.setString(1, ref);
                    psIssue.setInt(2, bookId);
                    psIssue.setInt(3, memberId);
                    psIssue.setString(4, issueDate.toString());
                    psIssue.setString(5, dueDate.toString());
                    psIssue.executeUpdate();
                    try (ResultSet rs = psIssue.getGeneratedKeys()) {
                        issueId = rs.next() ? rs.getInt(1) : -1;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        BookIssue issue = new BookIssue(ref, bookId, memberId, issueDate.toString(), dueDate.toString());
        issue.setId(issueId);
        issue.setBookTitle(book.getTitle());
        issue.setBookIsbn(book.getIsbn());
        issue.setMemberName(member.getName());
        issue.setMemberCode(member.getMemberCode());
        return issue;
    }

    public ReturnResult returnBook(int issueId, String returnDateStr) throws SQLException {
        BookIssue issue = getIssueById(issueId);
        if (issue == null) throw new IllegalArgumentException("Issue record ID " + issueId + " not found.");
        if ("RETURNED".equalsIgnoreCase(issue.getStatus())) {
            return new ReturnResult(false, "Book has already been marked as returned.", issue, 0, 0.0);
        }

        LocalDate returnDate = (returnDateStr != null && !returnDateStr.trim().isEmpty())
                ? LocalDate.parse(returnDateStr.trim()) : LocalDate.now();
        LocalDate dueDate = LocalDate.parse(issue.getDueDate());

        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        double fine = overdueDays * DAILY_FINE_RATE;
        String fineStatus = fine > 0 ? "PENDING" : "NONE";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Increment available copy
                try (PreparedStatement psBook = conn.prepareStatement("UPDATE books SET available_copies = available_copies + 1 WHERE id = ?")) {
                    psBook.setInt(1, issue.getBookId());
                    psBook.executeUpdate();
                }

                // Update issue record
                String sqlUp = """
                    UPDATE book_issues 
                    SET return_date = ?, fine_amount = ?, fine_status = ?, status = 'RETURNED'
                    WHERE id = ?
                """;
                try (PreparedStatement psUp = conn.prepareStatement(sqlUp)) {
                    psUp.setString(1, returnDate.toString());
                    psUp.setDouble(2, fine);
                    psUp.setString(3, fineStatus);
                    psUp.setInt(4, issueId);
                    psUp.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        issue.setReturnDate(returnDate.toString());
        issue.setFineAmount(fine);
        issue.setFineStatus(fineStatus);
        issue.setStatus("RETURNED");

        String msg = fine > 0
                ? String.format("Book returned successfully. Overdue by %d days. Fine Payable: INR %,.2f", overdueDays, fine)
                : "Book returned on time with zero fine.";
        return new ReturnResult(true, msg, issue, overdueDays, fine);
    }

    public boolean payFine(int issueId) throws SQLException {
        String sql = "UPDATE book_issues SET fine_status = 'PAID' WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            return ps.executeUpdate() > 0;
        }
    }

    public BookIssue getIssueById(int issueId) throws SQLException {
        String sql = """
            SELECT bi.*, b.title AS book_title, b.isbn AS book_isbn, m.name AS member_name, m.member_code
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.id
            JOIN members m ON bi.member_id = m.id
            WHERE bi.id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractBookIssue(rs);
            }
        }
        return null;
    }

    public List<BookIssue> getAllIssues() throws SQLException {
        List<BookIssue> list = new ArrayList<>();
        String sql = """
            SELECT bi.*, b.title AS book_title, b.isbn AS book_isbn, m.name AS member_name, m.member_code
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.id
            JOIN members m ON bi.member_id = m.id
            ORDER BY bi.id DESC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractBookIssue(rs));
        }
        return list;
    }

    public List<BookIssue> getOverdueIssues(String currentDateStr) throws SQLException {
        String today = (currentDateStr != null) ? currentDateStr : LocalDate.now().toString();
        List<BookIssue> list = new ArrayList<>();
        String sql = """
            SELECT bi.*, b.title AS book_title, b.isbn AS book_isbn, m.name AS member_name, m.member_code
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.id
            JOIN members m ON bi.member_id = m.id
            WHERE bi.status = 'ISSUED' AND bi.due_date < ?
            ORDER BY bi.due_date ASC
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, today);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractBookIssue(rs));
            }
        }
        return list;
    }

    public List<Map<String, Object>> getPopularBooks(int limit) throws SQLException {
        String sql = """
            SELECT b.id, b.title, b.author, b.category, COUNT(bi.id) AS borrow_count
            FROM books b
            LEFT JOIN book_issues bi ON b.id = bi.book_id
            GROUP BY b.id
            ORDER BY borrow_count DESC, b.title ASC
            LIMIT ?
        """;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("title", rs.getString("title"));
                    map.put("author", rs.getString("author"));
                    map.put("category", rs.getString("category"));
                    map.put("borrow_count", rs.getInt("borrow_count"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    private BookIssue extractBookIssue(ResultSet rs) throws SQLException {
        BookIssue bi = new BookIssue();
        bi.setId(rs.getInt("id"));
        bi.setIssueRef(rs.getString("issue_ref"));
        bi.setBookId(rs.getInt("book_id"));
        bi.setMemberId(rs.getInt("member_id"));
        bi.setIssueDate(rs.getString("issue_date"));
        bi.setDueDate(rs.getString("due_date"));
        bi.setReturnDate(rs.getString("return_date"));
        bi.setFineAmount(rs.getDouble("fine_amount"));
        bi.setFineStatus(rs.getString("fine_status"));
        bi.setStatus(rs.getString("status"));
        bi.setCreatedAt(rs.getString("created_at"));
        bi.setBookTitle(rs.getString("book_title"));
        bi.setBookIsbn(rs.getString("book_isbn"));
        bi.setMemberName(rs.getString("member_name"));
        bi.setMemberCode(rs.getString("member_code"));
        return bi;
    }

    public String generateIssueSlip(BookIssue bi) {
        StringBuilder sb = new StringBuilder();
        sb.append("===================================================\n");
        sb.append("         LOGIC LEGEND CENTRAL LIBRARY              \n");
        sb.append("               BOOK BORROWING SLIP                 \n");
        sb.append("===================================================\n");
        sb.append(String.format("Issue Ref       : %s\n", bi.getIssueRef()));
        sb.append(String.format("Book Title      : %s\n", bi.getBookTitle()));
        sb.append(String.format("Book ISBN       : %s\n", bi.getBookIsbn()));
        sb.append(String.format("Member Name     : %s (%s)\n", bi.getMemberName(), bi.getMemberCode()));
        sb.append(String.format("Issue Date      : %s\n", bi.getIssueDate()));
        sb.append(String.format("RETURN DUE DATE : %s\n", bi.getDueDate()));
        sb.append("---------------------------------------------------\n");
        sb.append(String.format("Late Return Fine: INR %,.2f per overdue day\n", DAILY_FINE_RATE));
        sb.append("Please handle books with care and return on time.\n");
        sb.append("===================================================\n");
        return sb.toString();
    }

    // -------------------------------------------------------------
    // SEED DEMO DATA
    // -------------------------------------------------------------
    public void seedDemoData() throws SQLException {
        if (getAllBooks().isEmpty()) {
            Book b1 = addBook("9780134685991", "Effective Java", "Joshua Bloch", "Computer Science", "Addison-Wesley", "3rd Edition", 5, "Rack CS-101");
            Book b2 = addBook("9780262033848", "Introduction to Algorithms", "CLRS", "Computer Science", "MIT Press", "3rd Edition", 4, "Rack CS-102");
            Book b3 = addBook("9780132350884", "Clean Code", "Robert C. Martin", "Software Engineering", "Prentice Hall", "1st Edition", 3, "Rack SE-201");
            Book b4 = addBook("9780198520115", "The Feynman Lectures on Physics", "Richard Feynman", "Physics", "Addison-Wesley", "Commemorative", 2, "Rack SCI-301");
            Book b5 = addBook("9780062316097", "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History", "Harper", "1st Edition", 6, "Rack GEN-401");

            Member m1 = registerMember("Aakash Nair", "aakash@college.edu", "9812345670", "STUDENT");
            Member m2 = registerMember("Sneha Roy", "sneha@college.edu", "9812345671", "STUDENT");
            Member m3 = registerMember("Prof. D. K. Saraf", "saraf@college.edu", "9812345672", "FACULTY");

            // Issue b1 to m1 (due in future)
            issueBook(b1.getId(), m1.getId(), "2026-10-25");
            // Issue b2 to m2 with an old issue date (simulating overdue)
            issueBook(b2.getId(), m2.getId(), "2026-09-15");
        }
    }
}

