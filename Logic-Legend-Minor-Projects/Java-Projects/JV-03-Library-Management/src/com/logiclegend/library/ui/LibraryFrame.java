package com.logiclegend.library.ui;

import com.logiclegend.library.model.*;
import com.logiclegend.library.service.LibraryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LibraryFrame extends JFrame {
    private final LibraryService service;

    private JTabbedPane tabbedPane;
    private DefaultTableModel modelBooks;
    private DefaultTableModel modelMembers;
    private DefaultTableModel modelIssues;
    private DefaultTableModel modelOverdue;
    private DefaultTableModel modelPopular;

    public LibraryFrame(LibraryService service) {
        this.service = service;
        setTitle("Logic Legend — JV-03: Library Management System");
        setSize(1120, 740);
        setMinimumSize(new Dimension(960, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 25));

        JLabel titleLbl = new JLabel("LOGIC LEGEND CENTRAL LIBRARY  |  Automated Circulation System");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JLabel subLbl = new JLabel("Fine Engine & Catalog Management");
        subLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subLbl.setForeground(new Color(148, 163, 184));
        header.add(subLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("📚 Book Catalog", buildBooksTab());
        tabbedPane.addTab("👤 Members Directory", buildMembersTab());
        tabbedPane.addTab("🔄 Book Circulation (Issue & Return)", buildCirculationTab());
        tabbedPane.addTab("📊 Overdue & Popularity Reports", buildReportsTab());

        add(tabbedPane, BorderLayout.CENTER);

        refreshBooks("");
        refreshMembers();
        refreshIssues();
        refreshReports();
    }

    private JPanel buildBooksTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JTextField txtSearch = new JTextField(22);
        JButton btnSearch = new JButton("Search Catalog");
        btnSearch.addActionListener(e -> refreshBooks(txtSearch.getText().trim()));

        JButton btnAddBook = new JButton("+ Add New Book");
        btnAddBook.setBackground(new Color(37, 99, 235));
        btnAddBook.setForeground(Color.WHITE);
        btnAddBook.addActionListener(e -> onAddBookDialog());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshBooks(""));

        topBar.add(new JLabel("Search:"));
        topBar.add(txtSearch);
        topBar.add(btnSearch);
        topBar.add(btnAddBook);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "ISBN", "Book Title", "Author", "Category", "Copies (Avail/Total)", "Shelf Rack"};
        modelBooks = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelBooks);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMembersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnRegMember = new JButton("+ Register Member");
        btnRegMember.setBackground(new Color(16, 185, 129));
        btnRegMember.setForeground(Color.WHITE);
        btnRegMember.addActionListener(e -> onRegisterMemberDialog());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshMembers());

        topBar.add(btnRegMember);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Member Code", "Full Name", "Type", "Email", "Phone", "Max Books", "Active Borrowed", "Status"};
        modelMembers = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelMembers);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildCirculationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnIssue = new JButton("📖 Issue Book");
        btnIssue.setBackground(new Color(124, 58, 237));
        btnIssue.setForeground(Color.WHITE);
        btnIssue.addActionListener(e -> onIssueBookDialog());

        JButton btnReturn = new JButton("📥 Return Selected Book");
        btnReturn.setBackground(new Color(245, 158, 11));
        btnReturn.setForeground(Color.WHITE);
        btnReturn.addActionListener(e -> onReturnBookDialog());

        JButton btnPayFine = new JButton("💰 Pay Fine");
        btnPayFine.addActionListener(e -> onPayFine());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshIssues());

        topBar.add(btnIssue);
        topBar.add(btnReturn);
        topBar.add(btnPayFine);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Ref", "Book Title", "ISBN", "Member Name", "Member Code", "Issue Date", "Due Date", "Return Date", "Fine (INR)", "Fine Status", "Status"};
        modelIssues = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelIssues);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: Overdue Books
        JPanel pnlOverdue = new JPanel(new BorderLayout(8, 8));
        pnlOverdue.setBorder(BorderFactory.createTitledBorder("🚨 Currently Overdue Borrowed Books"));
        String[] colsOver = {"Ref", "Book Title", "Member Name", "Due Date", "Status"};
        modelOverdue = new DefaultTableModel(colsOver, 0);
        JTable tblOverdue = new JTable(modelOverdue);
        pnlOverdue.add(new JScrollPane(tblOverdue), BorderLayout.CENTER);
        panel.add(pnlOverdue);

        // Bottom: Popular Books
        JPanel pnlPop = new JPanel(new BorderLayout(8, 8));
        pnlPop.setBorder(BorderFactory.createTitledBorder("⭐ Most Frequently Borrowed Books (Popularity Leaderboard)"));
        String[] colsPop = {"Book Title", "Author", "Category", "Times Borrowed"};
        modelPopular = new DefaultTableModel(colsPop, 0);
        JTable tblPop = new JTable(modelPopular);
        pnlPop.add(new JScrollPane(tblPop), BorderLayout.CENTER);
        panel.add(pnlPop);

        return panel;
    }

    private void refreshBooks(String query) {
        try {
            modelBooks.setRowCount(0);
            List<Book> list = (query == null || query.isEmpty()) ? service.getAllBooks() : service.searchBooks(query);
            for (Book b : list) {
                modelBooks.addRow(new Object[]{
                        b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getCategory(),
                        String.format("%d / %d", b.getAvailableCopies(), b.getTotalCopies()),
                        b.getShelfLocation()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Books", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMembers() {
        try {
            modelMembers.setRowCount(0);
            List<Member> list = service.getAllMembers();
            for (Member m : list) {
                int active = service.getActiveIssuedCount(m.getId());
                modelMembers.addRow(new Object[]{
                        m.getId(), m.getMemberCode(), m.getName(), m.getMemberType(),
                        m.getEmail(), m.getPhone(), m.getMaxBooks(), active, m.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Members", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshIssues() {
        try {
            modelIssues.setRowCount(0);
            List<BookIssue> list = service.getAllIssues();
            for (BookIssue bi : list) {
                modelIssues.addRow(new Object[]{
                        bi.getId(), bi.getIssueRef(), bi.getBookTitle(), bi.getBookIsbn(),
                        bi.getMemberName(), bi.getMemberCode(), bi.getIssueDate(),
                        bi.getDueDate(), bi.getReturnDate() != null ? bi.getReturnDate() : "BORROWED",
                        String.format("%,.2f", bi.getFineAmount()), bi.getFineStatus(), bi.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Circulation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshReports() {
        try {
            modelOverdue.setRowCount(0);
            List<BookIssue> overdue = service.getOverdueIssues(LocalDate.now().toString());
            for (BookIssue o : overdue) {
                modelOverdue.addRow(new Object[]{
                        o.getIssueRef(), o.getBookTitle(), o.getMemberName(), o.getDueDate(), o.getStatus()
                });
            }

            modelPopular.setRowCount(0);
            List<Map<String, Object>> pop = service.getPopularBooks(10);
            for (Map<String, Object> p : pop) {
                modelPopular.addRow(new Object[]{
                        p.get("title"), p.get("author"), p.get("category"), p.get("borrow_count")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading reports: " + e.getMessage());
        }
    }

    private void onAddBookDialog() {
        JTextField txtIsbn = new JTextField();
        JTextField txtTitle = new JTextField();
        JTextField txtAuthor = new JTextField();
        JTextField txtCategory = new JTextField("Computer Science");
        JTextField txtPublisher = new JTextField("Oxford Press");
        JTextField txtEdition = new JTextField("2nd Edition");
        JTextField txtCopies = new JTextField("5");
        JTextField txtShelf = new JTextField("Rack CS-105");

        Object[] fields = {
                "ISBN (13-digit) *:", txtIsbn,
                "Book Title *:", txtTitle,
                "Author *:", txtAuthor,
                "Category / Subject:", txtCategory,
                "Publisher:", txtPublisher,
                "Edition:", txtEdition,
                "Total Copies *:", txtCopies,
                "Shelf / Location:", txtShelf
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add New Book to Catalog", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int copies = Integer.parseInt(txtCopies.getText().trim());
                Book b = service.addBook(txtIsbn.getText().trim(), txtTitle.getText().trim(),
                        txtAuthor.getText().trim(), txtCategory.getText().trim(),
                        txtPublisher.getText().trim(), txtEdition.getText().trim(),
                        copies, txtShelf.getText().trim());
                JOptionPane.showMessageDialog(this, "Book Added to Catalog: " + b.getTitle(), "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshBooks("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRegisterMemberDialog() {
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});

        Object[] fields = {
                "Member Full Name *:", txtName,
                "Member Type (Student=3 books, Faculty=5 books):", cmbType,
                "Email Address:", txtEmail,
                "Phone Number:", txtPhone
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Register New Library Member", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Member m = service.registerMember(txtName.getText().trim(), txtEmail.getText().trim(),
                        txtPhone.getText().trim(), (String) cmbType.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Member Registered: " + m.getName() + " (" + m.getMemberCode() + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshMembers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onIssueBookDialog() {
        try {
            List<Book> books = service.getAllBooks();
            List<Member> members = service.getAllMembers();
            if (books.isEmpty() || members.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Catalog and member lists cannot be empty.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JComboBox<Book> cmbBook = new JComboBox<>(books.toArray(new Book[0]));
            JComboBox<Member> cmbMember = new JComboBox<>(members.toArray(new Member[0]));
            JTextField txtDate = new JTextField(LocalDate.now().toString());

            Object[] fields = {
                    "Select Book *:", cmbBook,
                    "Select Member *:", cmbMember,
                    "Issue Date (YYYY-MM-DD):", txtDate
            };

            int res = JOptionPane.showConfirmDialog(this, fields, "Issue Book to Member", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                Book b = (Book) cmbBook.getSelectedItem();
                Member m = (Member) cmbMember.getSelectedItem();
                BookIssue bi = service.issueBook(b.getId(), m.getId(), txtDate.getText().trim());
                String slip = service.generateIssueSlip(bi);
                JOptionPane.showMessageDialog(this, slip, "Book Issued — Borrowing Slip", JOptionPane.INFORMATION_MESSAGE);
                refreshBooks("");
                refreshMembers();
                refreshIssues();
                refreshReports();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Issue Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReturnBookDialog() {
        JPanel circPanel = (JPanel) tabbedPane.getComponentAt(2);
        JScrollPane sp = (JScrollPane) circPanel.getComponent(1);
        JTable tbl = (JTable) sp.getViewport().getView();
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an issued book record to return.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int issueId = (int) tbl.getValueAt(row, 0);
        String today = LocalDate.now().toString();
        String returnDate = JOptionPane.showInputDialog(this, "Enter Return Date (YYYY-MM-DD):", today);
        if (returnDate != null && !returnDate.trim().isEmpty()) {
            try {
                LibraryService.ReturnResult res = service.returnBook(issueId, returnDate.trim());
                JOptionPane.showMessageDialog(this, res.message, res.overdueDays > 0 ? "Late Return with Fine" : "Return Confirmed", JOptionPane.INFORMATION_MESSAGE);
                refreshBooks("");
                refreshMembers();
                refreshIssues();
                refreshReports();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onPayFine() {
        JPanel circPanel = (JPanel) tabbedPane.getComponentAt(2);
        JScrollPane sp = (JScrollPane) circPanel.getComponent(1);
        JTable tbl = (JTable) sp.getViewport().getView();
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a record with pending fine.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int issueId = (int) tbl.getValueAt(row, 0);
        String fineStatus = (String) tbl.getValueAt(row, 10);
        if (!"PENDING".equalsIgnoreCase(fineStatus)) {
            JOptionPane.showMessageDialog(this, "No pending fine for this record.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Collect fine payment and mark as PAID?", "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.payFine(issueId);
                JOptionPane.showMessageDialog(this, "Fine marked as PAID.", "Payment Recorded", JOptionPane.INFORMATION_MESSAGE);
                refreshIssues();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

