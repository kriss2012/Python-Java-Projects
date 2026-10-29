package com.logiclegend.payroll.ui;

import com.logiclegend.payroll.model.Employee;
import com.logiclegend.payroll.model.PayrollRecord;
import com.logiclegend.payroll.service.PayrollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Modern Swing HR & Payroll Desktop Terminal.
 */
public class PayrollFrame extends JFrame {
    private final PayrollService service;

    // Components - Tab 1
    private DefaultTableModel employeeTableModel;
    private JTable employeeTable;

    // Components - Tab 2 (Processing)
    private JComboBox<String> employeeSelectorCombo;
    private JTextField monthField;
    private JSpinner totalDaysSpinner;
    private JSpinner unpaidLeavesSpinner;
    private JTextField bonusField;
    private JLabel basicEarnedLabel;
    private JLabel hraLabel;
    private JLabel daLabel;
    private JLabel grossLabel;
    private JLabel epfLabel;
    private JLabel deductionsLabel;
    private JLabel netSalaryLabel;

    // Components - Tab 3 (Payslips)
    private DefaultTableModel payslipTableModel;
    private JTable payslipTable;
    private DefaultTableModel deptSummaryTableModel;
    private JTable deptSummaryTable;

    public PayrollFrame(PayrollService service) {
        this.service = service;

        setTitle("LogicLegend — Enterprise HR & Employee Payroll System");
        setSize(1080, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("  LOGIC LEGEND HR & PAYROLL CONTROLLER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel statusLabel = new JLabel("Statutory EPF (12%) | HRA (40%) | DA (20%) | PT ₹200  ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(236, 240, 241));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("👥 Employee Master Directory", createEmployeePanel());
        tabbedPane.addTab("💰 Process Monthly Payroll", createPayrollProcessingPanel());
        tabbedPane.addTab("📄 Payslip Archive & Dept Analytics", createReportsPanel());

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);

        refreshEmployeeTable();
        refreshEmployeeSelectorCombo();
        refreshReportsTab();
    }

    // -------------------------------------------------------------------------
    // TAB 1: EMPLOYEE MASTER DIRECTORY
    // -------------------------------------------------------------------------
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton addEmpBtn = new JButton("+ Onboard New Employee");
        addEmpBtn.setBackground(new Color(41, 128, 185));
        addEmpBtn.setForeground(Color.WHITE);
        addEmpBtn.addActionListener(e -> promptAddEmployee());
        topBar.add(addEmpBtn);

        JButton reviseSalaryBtn = new JButton("Revise Salary (Appraisal)");
        reviseSalaryBtn.setBackground(new Color(39, 174, 96));
        reviseSalaryBtn.setForeground(Color.WHITE);
        reviseSalaryBtn.addActionListener(e -> promptReviseSalary());
        topBar.add(reviseSalaryBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshEmployeeTable());
        topBar.add(refreshBtn);

        panel.add(topBar, BorderLayout.NORTH);

        employeeTableModel = new DefaultTableModel(new String[]{
                "ID", "Emp Code", "Full Name", "Designation", "Department", "Email", "Phone", "Bank Account", "PAN", "Basic Salary", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        employeeTable = new JTable(employeeTableModel);
        employeeTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(employeeTable);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void promptAddEmployee() {
        JTextField codeField = new JTextField("EMP-" + (1000 + (int)(Math.random() * 9000)));
        JTextField nameField = new JTextField();
        JTextField desigField = new JTextField();
        JTextField deptField = new JTextField("Engineering");
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField("+91 ");
        JTextField bankField = new JTextField("HDFC000");
        JTextField panField = new JTextField();
        JTextField basicField = new JTextField("50000");

        JPanel form = new JPanel(new GridLayout(9, 2, 6, 6));
        form.add(new JLabel("Employee Code:")); form.add(codeField);
        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Designation:")); form.add(desigField);
        form.add(new JLabel("Department:")); form.add(deptField);
        form.add(new JLabel("Email Address:")); form.add(emailField);
        form.add(new JLabel("Phone Contact:")); form.add(phoneField);
        form.add(new JLabel("Bank Account / IFSC:")); form.add(bankField);
        form.add(new JLabel("PAN Number:")); form.add(panField);
        form.add(new JLabel("Basic Salary (₹):")); form.add(basicField);

        int res = JOptionPane.showConfirmDialog(this, form, "Onboard New Employee", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                service.createEmployee(
                        codeField.getText().trim(),
                        nameField.getText().trim(),
                        desigField.getText().trim(),
                        deptField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        bankField.getText().trim(),
                        panField.getText().trim(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Double.parseDouble(basicField.getText().trim())
                );
                refreshEmployeeTable();
                refreshEmployeeSelectorCombo();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to create employee: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void promptReviseSalary() {
        int row = employeeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee row to revise salary.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int empId = (int) employeeTableModel.getValueAt(row, 0);
        String name = (String) employeeTableModel.getValueAt(row, 2);

        JTextField newBasicField = new JTextField();
        JTextField reasonField = new JTextField("Annual Merit Appraisal");

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("New Basic Salary (₹):")); form.add(newBasicField);
        form.add(new JLabel("Revision Reason:")); form.add(reasonField);

        int res = JOptionPane.showConfirmDialog(this, form, "Salary Revision for " + name, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double newBasic = Double.parseDouble(newBasicField.getText().trim());
                service.reviseSalary(empId, newBasic, null, reasonField.getText().trim());
                refreshEmployeeTable();
                refreshEmployeeSelectorCombo();
                JOptionPane.showMessageDialog(this, "Salary successfully revised for " + name, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to revise salary: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshEmployeeTable() {
        employeeTableModel.setRowCount(0);
        try {
            List<Employee> list = service.getAllEmployees();
            for (Employee e : list) {
                employeeTableModel.addRow(new Object[]{
                        e.getId(), e.getEmpCode(), e.getName(), e.getDesignation(),
                        e.getDepartment(), e.getEmail(), e.getPhone(), e.getBankAccount(),
                        e.getPanNumber(), String.format("₹%,.2f", e.getBasicSalary()), e.getStatus()
                });
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load employees: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // TAB 2: MONTHLY PAYROLL PROCESSING
    // -------------------------------------------------------------------------
    private JPanel createPayrollProcessingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Payroll Calculation Parameters"));

        formPanel.add(new JLabel("Select Employee:"));
        employeeSelectorCombo = new JComboBox<>();
        employeeSelectorCombo.addActionListener(e -> updateCalculationsPreview());
        formPanel.add(employeeSelectorCombo);

        formPanel.add(new JLabel("Pay Period Month (YYYY-MM):"));
        monthField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        formPanel.add(monthField);

        formPanel.add(new JLabel("Total Working Days:"));
        totalDaysSpinner = new JSpinner(new SpinnerNumberModel(30, 20, 31, 1));
        totalDaysSpinner.addChangeListener(e -> updateCalculationsPreview());
        formPanel.add(totalDaysSpinner);

        formPanel.add(new JLabel("Unpaid Leaves (Loss of Pay):"));
        unpaidLeavesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 31, 1));
        unpaidLeavesSpinner.addChangeListener(e -> updateCalculationsPreview());
        formPanel.add(unpaidLeavesSpinner);

        formPanel.add(new JLabel("Special Bonus / Incentive (₹):"));
        bonusField = new JTextField("0");
        bonusField.addActionListener(e -> updateCalculationsPreview());
        formPanel.add(bonusField);

        panel.add(formPanel, BorderLayout.NORTH);

        // Center Live Calculation Cards
        JPanel previewPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Prorated Real-Time Salary Breakdown"));

        basicEarnedLabel = createStatCard(previewPanel, "Basic Earned", "₹0.00", new Color(41, 128, 185));
        hraLabel = createStatCard(previewPanel, "HRA Allowance (40%)", "₹0.00", new Color(52, 152, 219));
        daLabel = createStatCard(previewPanel, "DA Allowance (20%)", "₹0.00", new Color(26, 188, 156));
        grossLabel = createStatCard(previewPanel, "GROSS EARNINGS", "₹0.00", new Color(39, 174, 96));

        epfLabel = createStatCard(previewPanel, "EPF Deduction (12%)", "₹0.00", new Color(230, 126, 34));
        deductionsLabel = createStatCard(previewPanel, "TOTAL DEDUCTIONS", "₹0.00", new Color(231, 76, 60));
        netSalaryLabel = createStatCard(previewPanel, "NET SALARY PAYABLE", "₹0.00", new Color(46, 204, 113));

        panel.add(previewPanel, BorderLayout.CENTER);

        JButton processBtn = new JButton("Process Monthly Payroll & Generate Official Payslip");
        processBtn.setBackground(new Color(44, 62, 80));
        processBtn.setForeground(Color.WHITE);
        processBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processBtn.setPreferredSize(new Dimension(0, 45));
        processBtn.addActionListener(e -> finalizePayroll());
        panel.add(processBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createStatCard(JPanel parent, String title, String val, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(Color.GRAY);
        card.add(titleLbl, BorderLayout.NORTH);

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valLbl.setForeground(color);
        card.add(valLbl, BorderLayout.CENTER);

        parent.add(card);
        return valLbl;
    }

    private void refreshEmployeeSelectorCombo() {
        employeeSelectorCombo.removeAllItems();
        try {
            List<Employee> list = service.getAllEmployees();
            for (Employee e : list) {
                employeeSelectorCombo.addItem(e.getId() + " - " + e.getName() + " (" + e.getEmpCode() + ")");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load selector combo: " + e.getMessage());
        }
        updateCalculationsPreview();
    }

    private void updateCalculationsPreview() {
        String selected = (String) employeeSelectorCombo.getSelectedItem();
        if (selected == null) return;

        int empId = Integer.parseInt(selected.split(" - ")[0]);
        try {
            Employee e = service.getEmployeeById(empId);
            if (e == null) return;

            int totalDays = (int) totalDaysSpinner.getValue();
            int unpaidLeaves = (int) unpaidLeavesSpinner.getValue();
            int daysWorked = Math.max(0, totalDays - unpaidLeaves);
            double ratio = (double) daysWorked / (double) totalDays;

            double basicEarned = Math.round(e.getBasicSalary() * ratio * 100.0) / 100.0;
            double hra = Math.round(basicEarned * 0.40 * 100.0) / 100.0;
            double da = Math.round(basicEarned * 0.20 * 100.0) / 100.0;
            double conveyance = 2000.0;
            double bonus = 0.0;
            try { bonus = Double.parseDouble(bonusField.getText().trim()); } catch (Exception ignored) {}
            double gross = Math.round((basicEarned + hra + da + conveyance + bonus) * 100.0) / 100.0;

            double epf = Math.round(basicEarned * 0.12 * 100.0) / 100.0;
            double esi = gross <= 21000.0 ? Math.round(gross * 0.0075 * 100.0) / 100.0 : 0.0;
            double pt = 200.0;
            double tds = Math.round((gross > 50000.0 ? gross * 0.05 : 0.0) * 100.0) / 100.0;
            double deductions = epf + esi + pt + tds;
            double net = gross - deductions;

            basicEarnedLabel.setText(String.format("₹%,.2f", basicEarned));
            hraLabel.setText(String.format("₹%,.2f", hra));
            daLabel.setText(String.format("₹%,.2f", da));
            grossLabel.setText(String.format("₹%,.2f", gross));
            epfLabel.setText(String.format("₹%,.2f", epf));
            deductionsLabel.setText(String.format("₹%,.2f", deductions));
            netSalaryLabel.setText(String.format("₹%,.2f", net));
        } catch (SQLException ex) {
            System.err.println("[ERROR] Preview failed: " + ex.getMessage());
        }
    }

    private void finalizePayroll() {
        String selected = (String) employeeSelectorCombo.getSelectedItem();
        if (selected == null) return;

        int empId = Integer.parseInt(selected.split(" - ")[0]);
        String month = monthField.getText().trim();
        int totalDays = (int) totalDaysSpinner.getValue();
        int unpaidLeaves = (int) unpaidLeavesSpinner.getValue();
        double bonus = 0.0;
        try { bonus = Double.parseDouble(bonusField.getText().trim()); } catch (Exception ignored) {}

        try {
            Employee emp = service.getEmployeeById(empId);
            PayrollRecord rec = service.processPayroll(empId, month, totalDays, unpaidLeaves, bonus);
            File file = service.exportPayslipToFile(rec, emp, "exports");

            // Display payslip modal
            JTextArea area = new JTextArea(service.generatePayslipText(rec, emp));
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(680, 480));

            JOptionPane.showMessageDialog(this, scroll, "Official Payslip — " + rec.getSlipNumber(), JOptionPane.INFORMATION_MESSAGE);

            refreshReportsTab();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Payroll processing failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // TAB 3: PAYSLIP REGISTRY & REPORTS
    // -------------------------------------------------------------------------
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Top: Payslips table
        JPanel payslipsPanel = new JPanel(new BorderLayout(4, 4));
        payslipsPanel.setBorder(BorderFactory.createTitledBorder("Generated Monthly Payslips Archive"));

        JButton viewPayslipBtn = new JButton("View Selected Payslip");
        viewPayslipBtn.setBackground(new Color(142, 68, 173));
        viewPayslipBtn.setForeground(Color.WHITE);
        viewPayslipBtn.addActionListener(e -> viewSelectedPayslip());
        JPanel payslipActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        payslipActions.add(viewPayslipBtn);
        payslipsPanel.add(payslipActions, BorderLayout.NORTH);

        payslipTableModel = new DefaultTableModel(new String[]{
                "ID", "Slip Number", "Emp Code", "Employee Name", "Department", "Month", "Gross Pay", "Deductions", "Net Salary"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        payslipTable = new JTable(payslipTableModel);
        payslipTable.setRowHeight(24);
        payslipsPanel.add(new JScrollPane(payslipTable), BorderLayout.CENTER);
        panel.add(payslipsPanel);

        // Bottom: Dept summary table
        JPanel deptPanel = new JPanel(new BorderLayout(4, 4));
        deptPanel.setBorder(BorderFactory.createTitledBorder("Department-Wise Payroll Distribution"));

        deptSummaryTableModel = new DefaultTableModel(new String[]{
                "Department", "Employee Count", "Total Gross Paid", "Total Deductions", "Total Net Disbursed", "Average Net Salary"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        deptSummaryTable = new JTable(deptSummaryTableModel);
        deptSummaryTable.setRowHeight(24);
        deptPanel.add(new JScrollPane(deptSummaryTable), BorderLayout.CENTER);
        panel.add(deptPanel);

        return panel;
    }

    private void viewSelectedPayslip() {
        int row = payslipTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payslip row from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String slipNumber = (String) payslipTableModel.getValueAt(row, 1);
        try {
            PayrollRecord rec = service.getPayrollRecordBySlipNumber(slipNumber);
            if (rec != null) {
                Employee emp = service.getEmployeeById(rec.getEmployeeId());
                JTextArea area = new JTextArea(service.generatePayslipText(rec, emp));
                area.setFont(new Font("Consolas", Font.PLAIN, 12));
                area.setEditable(false);
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(680, 480));
                JOptionPane.showMessageDialog(this, scroll, "Official Payslip — " + slipNumber, JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load payslip: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshReportsTab() {
        payslipTableModel.setRowCount(0);
        deptSummaryTableModel.setRowCount(0);

        try {
            List<PayrollRecord> slips = service.getAllPayrollRecords(100);
            for (PayrollRecord p : slips) {
                payslipTableModel.addRow(new Object[]{
                        p.getId(), p.getSlipNumber(), p.getEmpCode(), p.getEmpName(),
                        p.getDepartment(), p.getMonthYear(),
                        String.format("₹%,.2f", p.getGrossEarnings()),
                        String.format("₹%,.2f", p.getTotalDeductions()),
                        String.format("₹%,.2f", p.getNetSalary())
                });
            }

            List<Map<String, Object>> deptStats = service.getDepartmentSalarySummary();
            for (Map<String, Object> map : deptStats) {
                deptSummaryTableModel.addRow(new Object[]{
                        map.get("department"),
                        map.get("employeeCount"),
                        String.format("₹%,.2f", map.get("totalGross")),
                        String.format("₹%,.2f", map.get("totalDeductions")),
                        String.format("₹%,.2f", map.get("totalNet")),
                        String.format("₹%,.2f", map.get("avgNet"))
                });
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load reports: " + e.getMessage());
        }
    }
}

