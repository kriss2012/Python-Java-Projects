package com.logiclegend.expensetodo.ui;

import com.logiclegend.expensetodo.model.*;
import com.logiclegend.expensetodo.service.ExpenseService;
import com.logiclegend.expensetodo.service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Desktop Companion GUI for JV-10 Android Expense and To-Do Manager.
 * Enables full graphical interaction and live evaluation on standard desktop environments.
 */
public class DesktopManagerFrame extends JFrame {
    private final ExpenseService expenseService;
    private final TaskService taskService;

    // Components
    private JLabel lblTotalSpent;
    private JLabel lblPendingTasks;
    private JLabel lblBudgetStatus;

    // Tables
    private JTable expenseTable;
    private DefaultTableModel expenseTableModel;
    private JTable taskTable;
    private DefaultTableModel taskTableModel;
    private JTable budgetTable;
    private DefaultTableModel budgetTableModel;
    private JTable analyticsTable;
    private DefaultTableModel analyticsTableModel;

    private JComboBox<String> cmbExpenseCategoryFilter;
    private JComboBox<String> cmbTaskFilter;

    public DesktopManagerFrame(ExpenseService expenseService, TaskService taskService) {
        this.expenseService = expenseService;
        this.taskService = taskService;

        setTitle("Logic Legend JV-10: Expense and To-Do Manager (Desktop View)");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(248, 250, 252));

        // 1. Header Banner & KPI Cards
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Expense & To-Do Personal Management Suite");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel("Android Architecture Components (Room Database, Repositories, Services, Threshold Alerts)");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        JPanel titlesBox = new JPanel(new GridLayout(2, 1, 2, 2));
        titlesBox.setOpaque(false);
        titlesBox.add(titleLabel);
        titlesBox.add(subtitleLabel);

        // KPI Card Box
        JPanel kpiBox = new JPanel(new GridLayout(1, 3, 12, 0));
        kpiBox.setOpaque(false);
        lblTotalSpent = createKpiCard(kpiBox, "THIS MONTH'S EXPENSES", "INR 0.00", new Color(30, 58, 138));
        lblBudgetStatus = createKpiCard(kpiBox, "BUDGET HEALTH", "SAFE", new Color(13, 148, 136));
        lblPendingTasks = createKpiCard(kpiBox, "PENDING TASKS", "0", new Color(217, 119, 6));

        headerPanel.add(titlesBox, BorderLayout.NORTH);
        headerPanel.add(kpiBox, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Tabbed Center Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Expenses & Ledger", createExpensesPanel());
        tabbedPane.addTab("To-Do Task Manager", createTasksPanel());
        tabbedPane.addTab("Monthly Budgets & Alerts", createBudgetsPanel());
        tabbedPane.addTab("Category Analytics", createAnalyticsPanel());

        root.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JLabel createKpiCard(JPanel parent, String title, String initialVal, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLbl.setForeground(accent);

        JLabel vLbl = new JLabel(initialVal);
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        vLbl.setForeground(new Color(15, 23, 42));

        card.add(tLbl, BorderLayout.NORTH);
        card.add(vLbl, BorderLayout.CENTER);
        parent.add(card);
        return vLbl;
    }

    // -------------------------------------------------------------------------
    // TAB 1: EXPENSES
    // -------------------------------------------------------------------------
    private JPanel createExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter & Action Toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setOpaque(false);

        topBar.add(new JLabel("Category:"));
        cmbExpenseCategoryFilter = new JComboBox<>();
        cmbExpenseCategoryFilter.addItem("ALL");
        for (String cat : ExpenseService.DEFAULT_CATEGORIES) cmbExpenseCategoryFilter.addItem(cat);
        cmbExpenseCategoryFilter.addActionListener(e -> refreshExpensesTable());
        topBar.add(cmbExpenseCategoryFilter);

        JButton btnAddExpense = new JButton("+ Add Expense");
        btnAddExpense.setBackground(new Color(30, 58, 138));
        btnAddExpense.setForeground(Color.WHITE);
        btnAddExpense.addActionListener(e -> showAddExpenseDialog());
        topBar.add(btnAddExpense);

        JButton btnDeleteExpense = new JButton("Delete Expense");
        btnDeleteExpense.addActionListener(e -> deleteSelectedExpense());
        topBar.add(btnDeleteExpense);

        JButton btnExportCsv = new JButton("Export CSV");
        btnExportCsv.addActionListener(e -> exportExpensesCsv());
        topBar.add(btnExportCsv);

        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Date", "Title", "Amount (INR)", "Category", "Payment Method", "Notes"};
        expenseTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        expenseTable = new JTable(expenseTableModel);
        expenseTable.setRowHeight(24);
        panel.add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------------------
    // TAB 2: TO-DO TASKS
    // -------------------------------------------------------------------------
    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setOpaque(false);

        topBar.add(new JLabel("Filter:"));
        cmbTaskFilter = new JComboBox<>(new String[]{"ALL", "PENDING ONLY", "COMPLETED", "OVERDUE"});
        cmbTaskFilter.addActionListener(e -> refreshTasksTable());
        topBar.add(cmbTaskFilter);

        JButton btnAddTask = new JButton("+ Add Task");
        btnAddTask.setBackground(new Color(13, 148, 136));
        btnAddTask.setForeground(Color.WHITE);
        btnAddTask.addActionListener(e -> showAddTaskDialog());
        topBar.add(btnAddTask);

        JButton btnToggle = new JButton("Toggle Complete");
        btnToggle.addActionListener(e -> toggleTaskCompletion());
        topBar.add(btnToggle);

        JButton btnDelete = new JButton("Delete Task");
        btnDelete.addActionListener(e -> deleteSelectedTask());
        topBar.add(btnDelete);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Status", "Title", "Priority", "Due Date", "Category", "Description"};
        taskTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(taskTableModel);
        taskTable.setRowHeight(24);
        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------------------
    // TAB 3: BUDGETS & ALERTS
    // -------------------------------------------------------------------------
    private JPanel createBudgetsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setOpaque(false);

        JButton btnSetBudget = new JButton("Set / Update Category Budget");
        btnSetBudget.setBackground(new Color(30, 58, 138));
        btnSetBudget.setForeground(Color.WHITE);
        btnSetBudget.addActionListener(e -> showSetBudgetDialog());
        topBar.add(btnSetBudget);

        JLabel info = new JLabel("(Alert Rule: >= 80% Warning | >= 100% Exceeded)");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(new Color(100, 116, 139));
        topBar.add(info);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Month", "Category", "Budget Limit (INR)", "Spent (INR)", "Remaining (INR)", "Usage %", "Health Status"};
        budgetTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        budgetTable = new JTable(budgetTableModel);
        budgetTable.setRowHeight(26);

        // Highlight Exceeded & Warning
        budgetTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String status = (String) table.getValueAt(row, 6);
                if ("EXCEEDED".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(220, 38, 38));
                } else if ("WARNING".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(217, 119, 6));
                } else {
                    c.setForeground(new Color(22, 163, 74));
                }
                if (isSelected) c.setBackground(new Color(226, 232, 240));
                else c.setBackground(Color.WHITE);
                return c;
            }
        });

        panel.add(new JScrollPane(budgetTable), BorderLayout.CENTER);
        return panel;
    }

    // -------------------------------------------------------------------------
    // TAB 4: ANALYTICS BREAKDOWN
    // -------------------------------------------------------------------------
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setOpaque(false);
        JLabel lblTitle = new JLabel("Category Spending Breakdown (MPAndroidChart Data Source)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topBar.add(lblTitle);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Category", "Total Outlay (INR)", "Transaction Count", "Share (%)"};
        analyticsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        analyticsTable = new JTable(analyticsTableModel);
        analyticsTable.setRowHeight(24);
        panel.add(new JScrollPane(analyticsTable), BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------------------
    // DATA REFRESH LOGIC
    // -------------------------------------------------------------------------
    public void refreshAllData() {
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            // Update KPIs
            double totalMonth = expenseService.getTotalSpentInMonth(currentMonth);
            lblTotalSpent.setText(String.format("INR %,.2f", totalMonth));

            List<TaskEntity> pending = taskService.getPendingTasks();
            lblPendingTasks.setText(String.valueOf(pending.size()));

            List<BudgetStatus> statuses = expenseService.getAllBudgetStatuses(currentMonth);
            boolean hasExceeded = statuses.stream().anyMatch(BudgetStatus::isExceeded);
            boolean hasWarning = statuses.stream().anyMatch(BudgetStatus::isWarning);

            if (hasExceeded) {
                lblBudgetStatus.setText("OVER BUDGET");
                lblBudgetStatus.setForeground(new Color(220, 38, 38));
            } else if (hasWarning) {
                lblBudgetStatus.setText("THRESHOLD ALERT");
                lblBudgetStatus.setForeground(new Color(217, 119, 6));
            } else {
                lblBudgetStatus.setText("HEALTHY");
                lblBudgetStatus.setForeground(new Color(22, 163, 74));
            }

            refreshExpensesTable();
            refreshTasksTable();
            refreshBudgetsTable();
            refreshAnalyticsTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshExpensesTable() {
        expenseTableModel.setRowCount(0);
        String filterCat = (String) cmbExpenseCategoryFilter.getSelectedItem();
        try {
            List<ExpenseEntity> list = expenseService.getAllExpenses();
            for (ExpenseEntity e : list) {
                if (filterCat != null && !"ALL".equalsIgnoreCase(filterCat) && !filterCat.equalsIgnoreCase(e.getCategory())) {
                    continue;
                }
                expenseTableModel.addRow(new Object[]{
                        e.getId(),
                        e.getExpenseDate(),
                        e.getTitle(),
                        String.format("%,.2f", e.getAmount()),
                        e.getCategory(),
                        e.getPaymentMethod(),
                        e.getNotes() != null ? e.getNotes() : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshTasksTable() {
        taskTableModel.setRowCount(0);
        String filter = (String) cmbTaskFilter.getSelectedItem();
        try {
            List<TaskEntity> list;
            if ("PENDING ONLY".equalsIgnoreCase(filter)) {
                list = taskService.getPendingTasks();
            } else if ("OVERDUE".equalsIgnoreCase(filter)) {
                list = taskService.getOverdueTasks();
            } else {
                list = taskService.getAllTasks();
            }

            for (TaskEntity t : list) {
                if ("COMPLETED".equalsIgnoreCase(filter) && !t.isCompleted()) continue;
                taskTableModel.addRow(new Object[]{
                        t.getId(),
                        t.isCompleted() ? "[DONE]" : "[PENDING]",
                        t.getTitle(),
                        t.getPriority(),
                        t.getDueDate(),
                        t.getLinkedCategory() != null ? t.getLinkedCategory() : "-",
                        t.getDescription() != null ? t.getDescription() : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshBudgetsTable() {
        budgetTableModel.setRowCount(0);
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        try {
            List<BudgetStatus> list = expenseService.getAllBudgetStatuses(currentMonth);
            for (BudgetStatus b : list) {
                budgetTableModel.addRow(new Object[]{
                        b.getMonthYear(),
                        b.getCategory(),
                        String.format("%,.2f", b.getBudgetLimit()),
                        String.format("%,.2f", b.getTotalSpent()),
                        String.format("%,.2f", b.getRemaining()),
                        String.format("%.1f%%", b.getPercentageUsed()),
                        b.getAlertStatus()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshAnalyticsTable() {
        analyticsTableModel.setRowCount(0);
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        try {
            List<CategorySummary> summaries = expenseService.getCategoryBreakdown(currentMonth);
            for (CategorySummary s : summaries) {
                analyticsTableModel.addRow(new Object[]{
                        s.getCategory(),
                        String.format("%,.2f", s.getTotalAmount()),
                        s.getCount(),
                        String.format("%.1f%%", s.getPercentage())
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // DIALOGS & ACTIONS
    // -------------------------------------------------------------------------

    private void showAddExpenseDialog() {
        JTextField txtTitle = new JTextField(20);
        JTextField txtAmount = new JTextField(10);
        JComboBox<String> cmbCat = new JComboBox<>(ExpenseService.DEFAULT_CATEGORIES);
        JTextField txtDate = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 10);
        JComboBox<String> cmbPay = new JComboBox<>(ExpenseService.PAYMENT_METHODS);
        JTextField txtNotes = new JTextField(20);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("Title:")); p.add(txtTitle);
        p.add(new JLabel("Amount (INR):")); p.add(txtAmount);
        p.add(new JLabel("Category:")); p.add(cmbCat);
        p.add(new JLabel("Date (YYYY-MM-DD):")); p.add(txtDate);
        p.add(new JLabel("Payment Method:")); p.add(cmbPay);
        p.add(new JLabel("Notes:")); p.add(txtNotes);

        int res = JOptionPane.showConfirmDialog(this, p, "Add New Expense", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double amt = Double.parseDouble(txtAmount.getText().trim());
                expenseService.createExpense(txtTitle.getText(), amt, (String) cmbCat.getSelectedItem(),
                        txtDate.getText().trim(), (String) cmbPay.getSelectedItem(), txtNotes.getText().trim());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Expense recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row to delete.");
            return;
        }
        int id = (int) expenseTableModel.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete expense ID " + id + "?");
        if (conf == JOptionPane.YES_OPTION) {
            try {
                expenseService.deleteExpense(id);
                refreshAllData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void exportExpensesCsv() {
        try {
            File f = expenseService.exportExpensesToCsv(null, "exports");
            JOptionPane.showMessageDialog(this, "Expenses successfully exported to:\n" + f.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddTaskDialog() {
        JTextField txtTitle = new JTextField(20);
        JTextField txtDesc = new JTextField(20);
        JComboBox<String> cmbPri = new JComboBox<>(TaskService.PRIORITIES);
        JTextField txtDue = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 10);
        JComboBox<String> cmbCat = new JComboBox<>(ExpenseService.DEFAULT_CATEGORIES);

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.add(new JLabel("Task Title:")); p.add(txtTitle);
        p.add(new JLabel("Description:")); p.add(txtDesc);
        p.add(new JLabel("Priority:")); p.add(cmbPri);
        p.add(new JLabel("Due Date (YYYY-MM-DD):")); p.add(txtDue);
        p.add(new JLabel("Linked Category:")); p.add(cmbCat);

        int res = JOptionPane.showConfirmDialog(this, p, "Create To-Do Task", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                taskService.createTask(txtTitle.getText(), txtDesc.getText(), (String) cmbPri.getSelectedItem(),
                        txtDue.getText().trim(), (String) cmbCat.getSelectedItem());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Task created successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleTaskCompletion() {
        int row = taskTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task from the list.");
            return;
        }
        int id = (int) taskTableModel.getValueAt(row, 0);
        String status = (String) taskTableModel.getValueAt(row, 1);
        boolean newStatus = !status.contains("DONE");
        try {
            taskService.toggleTaskCompletion(id, newStatus);
            refreshAllData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating task: " + ex.getMessage());
        }
    }

    private void deleteSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.");
            return;
        }
        int id = (int) taskTableModel.getValueAt(row, 0);
        try {
            taskService.deleteTask(id);
            refreshAllData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showSetBudgetDialog() {
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        JTextField txtMonth = new JTextField(currentMonth, 10);
        JComboBox<String> cmbCat = new JComboBox<>(ExpenseService.DEFAULT_CATEGORIES);
        JTextField txtLimit = new JTextField(10);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Month (YYYY-MM):")); p.add(txtMonth);
        p.add(new JLabel("Category:")); p.add(cmbCat);
        p.add(new JLabel("Budget Limit (INR):")); p.add(txtLimit);

        int res = JOptionPane.showConfirmDialog(this, p, "Configure Category Monthly Budget", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double limit = Double.parseDouble(txtLimit.getText().trim());
                expenseService.setBudget(txtMonth.getText().trim(), (String) cmbCat.getSelectedItem(), limit);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Budget saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

