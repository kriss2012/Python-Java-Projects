package com.logiclegend.banking.ui;

import com.logiclegend.banking.model.Account;
import com.logiclegend.banking.model.Transaction;
import com.logiclegend.banking.service.BankingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ATMFrame extends JFrame {
    private final BankingService service;
    private Account currentAccount = null;

    private CardLayout cardLayout;
    private JPanel screenPanel;

    // Login components
    private JTextField txtAccountNum;
    private JPasswordField txtPin;
    private JLabel lblStrikeWarning;

    // Dashboard components
    private JLabel lblWelcome;
    private JLabel lblBalance;

    public ATMFrame(BankingService service) {
        this.service = service;
        setTitle("Logic Legend — JV-01: ATM & Online Banking Simulator");
        setSize(980, 700);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(241, 245, 249));

        // Top Bank Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLbl = new JLabel("LOGIC LEGEND NATIONAL BANK  |  24x7 ATM Terminal");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(248, 250, 252));
        header.add(titleLbl, BorderLayout.WEST);

        JLabel subLbl = new JLabel("ACID Secure Transaction Simulation");
        subLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subLbl.setForeground(new Color(148, 163, 184));
        header.add(subLbl, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center: ATM Screen and Pinpad container
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        centerContainer.setBackground(new Color(241, 245, 249));

        // Left/Center: Digital ATM Screen
        cardLayout = new CardLayout();
        screenPanel = new JPanel(cardLayout);
        screenPanel.setBackground(Color.WHITE);
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        screenPanel.add(buildLoginScreen(), "LOGIN");
        screenPanel.add(buildMainMenuScreen(), "MENU");
        screenPanel.add(buildWithdrawScreen(), "WITHDRAW");
        screenPanel.add(buildDepositScreen(), "DEPOSIT");
        screenPanel.add(buildTransferScreen(), "TRANSFER");
        screenPanel.add(buildStatementScreen(), "STATEMENT");

        centerContainer.add(screenPanel, BorderLayout.CENTER);

        // Right: Physical PIN Pad Simulator
        JPanel keypadPanel = buildKeypad();
        centerContainer.add(keypadPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

        // Bottom: Status Banner
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(226, 232, 240));
        JLabel tipLbl = new JLabel("Demo Accounts: Arjun (Acc: 1001001001, PIN: 1234) | Priya (Acc: 1001002002, PIN: 4321) | 3-Strike Lockout Enforced");
        tipLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tipLbl.setForeground(new Color(71, 85, 105));
        footer.add(tipLbl);
        add(footer, BorderLayout.SOUTH);

        cardLayout.show(screenPanel, "LOGIN");
    }

    private JPanel buildLoginScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("WELCOME — INSERT YOUR CARD", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        JLabel lblSub = new JLabel("Please enter your 10-digit Account Number and 4-digit PIN", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Account Number:"), gbc);
        txtAccountNum = new JTextField("1001001001", 16);
        txtAccountNum.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(txtAccountNum, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Security PIN:"), gbc);
        txtPin = new JPasswordField("1234", 6);
        txtPin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(txtPin, gbc);

        lblStrikeWarning = new JLabel(" ");
        lblStrikeWarning.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStrikeWarning.setForeground(new Color(239, 68, 68));
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblStrikeWarning, gbc);

        JButton btnLogin = new JButton("Authenticate & Enter");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> onLogin());
        gbc.gridy = 5;
        panel.add(btnLogin, gbc);

        JButton btnUnlock = new JButton("Admin Unlock Account");
        btnUnlock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnUnlock.addActionListener(e -> onAdminUnlock());
        gbc.gridy = 6;
        panel.add(btnUnlock, gbc);

        return panel;
    }

    private JPanel buildMainMenuScreen() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);

        // Header info
        JPanel topInfo = new JPanel(new GridLayout(2, 1, 4, 4));
        topInfo.setBackground(new Color(248, 250, 252));
        topInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        lblWelcome = new JLabel("Welcome, Customer");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWelcome.setForeground(new Color(15, 23, 42));

        lblBalance = new JLabel("Available Balance: INR 0.00");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBalance.setForeground(new Color(16, 185, 129));

        topInfo.add(lblWelcome);
        topInfo.add(lblBalance);
        panel.add(topInfo, BorderLayout.NORTH);

        // Menu Buttons Grid
        JPanel btnGrid = new JPanel(new GridLayout(3, 2, 15, 15));
        btnGrid.setBackground(Color.WHITE);

        JButton btnWithdraw = createMenuButton("1. Cash Withdrawal", "#2563EB", e -> cardLayout.show(screenPanel, "WITHDRAW"));
        JButton btnDeposit = createMenuButton("2. Cash Deposit", "#10B981", e -> cardLayout.show(screenPanel, "DEPOSIT"));
        JButton btnBalanceCheck = createMenuButton("3. Balance Inquiry", "#0284C7", e -> onBalanceInquiry());
        JButton btnTransfer = createMenuButton("4. Fund Transfer", "#7C3AED", e -> cardLayout.show(screenPanel, "TRANSFER"));
        JButton btnStatement = createMenuButton("5. Mini-Statement", "#D97706", e -> onShowMiniStatement());
        JButton btnExit = createMenuButton("6. Exit / Eject Card", "#DC2626", e -> onLogout());

        btnGrid.add(btnWithdraw);
        btnGrid.add(btnDeposit);
        btnGrid.add(btnBalanceCheck);
        btnGrid.add(btnTransfer);
        btnGrid.add(btnStatement);
        btnGrid.add(btnExit);

        panel.add(btnGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildWithdrawScreen() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("ATM CASH WITHDRAWAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 2, 12, 12));
        center.setBackground(Color.WHITE);

        int[] fastAmounts = {500, 1000, 2000, 5000, 10000};
        for (int amt : fastAmounts) {
            JButton b = new JButton(String.format("Fast Cash INR %,d", amt));
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.addActionListener(e -> executeWithdrawal(amt));
            center.add(b);
        }

        JPanel customBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customBox.setBackground(Color.WHITE);
        JTextField txtCustom = new JTextField(7);
        JButton btnCustom = new JButton("Custom Amount");
        btnCustom.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(txtCustom.getText().trim());
                executeWithdrawal(amt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        customBox.add(new JLabel("Other: INR"));
        customBox.add(txtCustom);
        customBox.add(btnCustom);
        center.add(customBox);

        panel.add(center, BorderLayout.CENTER);

        JButton btnBack = new JButton("<< Back to Main Menu");
        btnBack.addActionListener(e -> cardLayout.show(screenPanel, "MENU"));
        panel.add(btnBack, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildDepositScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CASH DEPOSIT TERMINAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Deposit Amount (INR):"), gbc);
        JTextField txtDep = new JTextField(12);
        txtDep.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(txtDep, gbc);

        JButton btnConfirm = new JButton("Confirm Deposit");
        btnConfirm.setBackground(new Color(16, 185, 129));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirm.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(txtDep.getText().trim());
                Transaction tx = service.deposit(currentAccount.getAccountNumber(), amt, "ATM Deposit");
                currentAccount = service.getAccount(currentAccount.getAccountNumber());
                updateDashboardLabels();
                JOptionPane.showMessageDialog(this, String.format("Deposit Successful!\nAmount: INR %,.2f\nNew Balance: INR %,.2f\nRef: %s",
                        amt, currentAccount.getBalance(), tx.getTransactionRef()), "Deposit Confirmed", JOptionPane.INFORMATION_MESSAGE);
                txtDep.setText("");
                cardLayout.show(screenPanel, "MENU");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Deposit Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnConfirm, gbc);

        JButton btnBack = new JButton("<< Cancel & Back");
        btnBack.addActionListener(e -> cardLayout.show(screenPanel, "MENU"));
        gbc.gridy = 3;
        panel.add(btnBack, gbc);

        return panel;
    }

    private JPanel buildTransferScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("INTER-BANK / ACCOUNT FUND TRANSFER", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Recipient Account Number:"), gbc);
        JTextField txtRec = new JTextField(14);
        gbc.gridx = 1;
        panel.add(txtRec, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Transfer Amount (INR):"), gbc);
        JTextField txtAmt = new JTextField(14);
        gbc.gridx = 1;
        panel.add(txtAmt, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Remarks / Note:"), gbc);
        JTextField txtRemarks = new JTextField(14);
        gbc.gridx = 1;
        panel.add(txtRemarks, gbc);

        JButton btnTransfer = new JButton("Execute Atomic Transfer");
        btnTransfer.setBackground(new Color(124, 58, 237));
        btnTransfer.setForeground(Color.WHITE);
        btnTransfer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTransfer.addActionListener(e -> {
            try {
                String rec = txtRec.getText().trim();
                double amt = Double.parseDouble(txtAmt.getText().trim());
                String rem = txtRemarks.getText().trim();
                boolean ok = service.transferFunds(currentAccount.getAccountNumber(), rec, amt, rem);
                if (ok) {
                    currentAccount = service.getAccount(currentAccount.getAccountNumber());
                    updateDashboardLabels();
                    JOptionPane.showMessageDialog(this, String.format("Transfer Successful!\nTransferred INR %,.2f to Account %s\nUpdated Balance: INR %,.2f",
                            amt, rec, currentAccount.getBalance()), "Transfer Complete", JOptionPane.INFORMATION_MESSAGE);
                    txtRec.setText(""); txtAmt.setText(""); txtRemarks.setText("");
                    cardLayout.show(screenPanel, "MENU");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transfer Failed: " + ex.getMessage(), "Transfer Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnTransfer, gbc);

        JButton btnBack = new JButton("<< Cancel & Back");
        btnBack.addActionListener(e -> cardLayout.show(screenPanel, "MENU"));
        gbc.gridy = 5;
        panel.add(btnBack, gbc);

        return panel;
    }

    private JPanel buildStatementScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("RECENT TRANSACTIONS (MINI-STATEMENT)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Ref", "Type", "Amount (INR)", "Bal After", "Timestamp", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnBack = new JButton("<< Back to Main Menu");
        btnBack.addActionListener(e -> cardLayout.show(screenPanel, "MENU"));
        panel.add(btnBack, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildKeypad() {
        JPanel panel = new JPanel(new GridLayout(4, 3, 8, 8));
        panel.setPreferredSize(new Dimension(180, 220));
        panel.setBorder(BorderFactory.createTitledBorder("ATM Keypad"));
        panel.setBackground(new Color(241, 245, 249));

        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "CLR", "0", "OK"};
        for (String k : keys) {
            JButton b = new JButton(k);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            if ("CLR".equals(k)) {
                b.setBackground(new Color(245, 158, 11));
                b.setForeground(Color.WHITE);
                b.addActionListener(e -> {
                    if (txtPin.isFocusOwner()) txtPin.setText("");
                    else if (txtAccountNum.isFocusOwner()) txtAccountNum.setText("");
                });
            } else if ("OK".equals(k)) {
                b.setBackground(new Color(16, 185, 129));
                b.setForeground(Color.WHITE);
                b.addActionListener(e -> onLogin());
            } else {
                b.addActionListener(e -> {
                    if (txtPin.isFocusOwner()) {
                        txtPin.setText(new String(txtPin.getPassword()) + k);
                    } else if (txtAccountNum.isFocusOwner()) {
                        txtAccountNum.setText(txtAccountNum.getText() + k);
                    }
                });
            }
            panel.add(b);
        }
        return panel;
    }

    private JButton createMenuButton(String text, String hexColor, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(Color.decode(hexColor));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(l);
        return b;
    }

    private void onLogin() {
        String accNum = txtAccountNum.getText().trim();
        String pin = new String(txtPin.getPassword()).trim();
        try {
            currentAccount = service.authenticate(accNum, pin);
            lblStrikeWarning.setText(" ");
            updateDashboardLabels();
            cardLayout.show(screenPanel, "MENU");
        } catch (IllegalStateException ex) {
            lblStrikeWarning.setText("⛔ " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "ACCOUNT LOCKED", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException ex) {
            lblStrikeWarning.setText("⚠️ " + ex.getMessage());
            txtPin.setText("");
        } catch (Exception ex) {
            lblStrikeWarning.setText("❌ " + ex.getMessage());
        }
    }

    private void onAdminUnlock() {
        String acc = JOptionPane.showInputDialog(this, "Enter locked Account Number to unlock:", "Admin Unlock", JOptionPane.QUESTION_MESSAGE);
        if (acc != null && !acc.trim().isEmpty()) {
            try {
                boolean ok = service.unlockAccount(acc.trim());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Account " + acc + " unlocked successfully! Strike counter reset to 0.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    lblStrikeWarning.setText(" ");
                } else {
                    JOptionPane.showMessageDialog(this, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDashboardLabels() {
        if (currentAccount != null) {
            lblWelcome.setText(String.format("Welcome, %s (%s Account ••••%s)",
                    currentAccount.getHolderName(), currentAccount.getAccountType(),
                    currentAccount.getAccountNumber().substring(Math.max(0, currentAccount.getAccountNumber().length() - 4))));
            lblBalance.setText(String.format("Available Balance: INR %,.2f", currentAccount.getBalance()));
        }
    }

    private void executeWithdrawal(double amount) {
        try {
            BankingService.WithdrawalResult res = service.withdraw(currentAccount.getAccountNumber(), amount);
            if (res.success) {
                currentAccount = service.getAccount(currentAccount.getAccountNumber());
                updateDashboardLabels();

                String receiptText = service.generateReceipt(res.transaction, currentAccount, res.denominations);
                int choice = JOptionPane.showOptionDialog(this,
                        receiptText,
                        "Cash Dispensed — Take Cash & Receipt",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new Object[]{"Print / Save Receipt", "Done"},
                        "Done");

                cardLayout.show(screenPanel, "MENU");
            } else {
                JOptionPane.showMessageDialog(this, res.message, "Withdrawal Denied", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onBalanceInquiry() {
        try {
            currentAccount = service.getAccount(currentAccount.getAccountNumber());
            updateDashboardLabels();
            JOptionPane.showMessageDialog(this,
                    String.format("ACCOUNT BALANCE INQUIRY\nAccount Number : %s\nHolder Name    : %s\nAccount Type   : %s\nBalance        : INR %,.2f\nDaily Withdrawn: INR %,.2f / %,.2f",
                            currentAccount.getAccountNumber(), currentAccount.getHolderName(),
                            currentAccount.getAccountType(), currentAccount.getBalance(),
                            currentAccount.getDailyWithdrawn(), BankingService.DAILY_WITHDRAWAL_LIMIT),
                    "Balance Inquiry", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowMiniStatement() {
        try {
            List<Transaction> list = service.getMiniStatement(currentAccount.getAccountNumber(), 10);
            JPanel stmtPanel = (JPanel) screenPanel.getComponent(5);
            JScrollPane sp = (JScrollPane) stmtPanel.getComponent(1);
            JTable tbl = (JTable) sp.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) tbl.getModel();
            model.setRowCount(0);
            for (Transaction t : list) {
                model.addRow(new Object[]{
                        t.getTransactionRef(),
                        t.getTxType(),
                        String.format("%,.2f", t.getAmount()),
                        String.format("%,.2f", t.getBalanceAfter()),
                        t.getTimestamp(),
                        t.getDescription()
                });
            }
            cardLayout.show(screenPanel, "STATEMENT");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLogout() {
        currentAccount = null;
        txtPin.setText("");
        lblStrikeWarning.setText(" ");
        cardLayout.show(screenPanel, "LOGIN");
        JOptionPane.showMessageDialog(this, "Please take your card. Thank you for banking with us!", "Session Terminated", JOptionPane.INFORMATION_MESSAGE);
    }
}

