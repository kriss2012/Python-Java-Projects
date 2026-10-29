package com.logiclegend.banking.service;

import com.logiclegend.banking.dao.DatabaseManager;
import com.logiclegend.banking.model.Account;
import com.logiclegend.banking.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BankingService {
    private final DatabaseManager dbManager;
    public static final double DAILY_WITHDRAWAL_LIMIT = 25000.0;
    public static final double SAVINGS_MINIMUM_BALANCE = 500.0;

    public static class WithdrawalResult {
        public final boolean success;
        public final String message;
        public final Transaction transaction;
        public final Map<Integer, Integer> denominations;

        public WithdrawalResult(boolean success, String message, Transaction transaction, Map<Integer, Integer> denominations) {
            this.success = success;
            this.message = message;
            this.transaction = transaction;
            this.denominations = denominations;
        }
    }

    public BankingService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Account createAccount(String holderName, String pin, double initialDeposit,
                                 String email, String phone, String accountType) throws SQLException {
        if (holderName == null || holderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must be 4 to 6 numeric digits.");
        }
        if ("SAVINGS".equalsIgnoreCase(accountType) && initialDeposit < SAVINGS_MINIMUM_BALANCE) {
            throw new IllegalArgumentException("Initial deposit for Savings account must be at least INR " + SAVINGS_MINIMUM_BALANCE);
        }

        String accNum = generateUniqueAccountNumber();
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPin(pin, salt);

        String insertAccSql = """
            INSERT INTO accounts (account_number, holder_name, email, phone, account_type, pin_hash, pin_salt, balance)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertAccSql)) {
                ps.setString(1, accNum);
                ps.setString(2, holderName.trim());
                ps.setString(3, email != null ? email.trim() : "");
                ps.setString(4, phone != null ? phone.trim() : "");
                ps.setString(5, accountType != null ? accountType.toUpperCase() : "SAVINGS");
                ps.setString(6, hash);
                ps.setString(7, salt);
                ps.setDouble(8, initialDeposit);
                ps.executeUpdate();
            }

            if (initialDeposit > 0) {
                String txRef = "TX" + System.currentTimeMillis() + (int)(Math.random() * 1000);
                String insertTxSql = """
                    INSERT INTO transactions (transaction_ref, account_number, tx_type, amount, balance_after, description)
                    VALUES (?, ?, 'DEPOSIT', ?, ?, 'Initial Account Opening Deposit')
                """;
                try (PreparedStatement psTx = conn.prepareStatement(insertTxSql)) {
                    psTx.setString(1, txRef);
                    psTx.setString(2, accNum);
                    psTx.setDouble(3, initialDeposit);
                    psTx.setDouble(4, initialDeposit);
                    psTx.executeUpdate();
                }
            }
            conn.commit();
        }

        return getAccount(accNum);
    }

    public Account getAccount(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setId(rs.getInt("id"));
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setHolderName(rs.getString("holder_name"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPhone(rs.getString("phone"));
                    acc.setAccountType(rs.getString("account_type"));
                    acc.setPinHash(rs.getString("pin_hash"));
                    acc.setPinSalt(rs.getString("pin_salt"));
                    acc.setBalance(rs.getDouble("balance"));
                    acc.setDailyWithdrawn(rs.getDouble("daily_withdrawn"));
                    acc.setFailedPinAttempts(rs.getInt("failed_pin_attempts"));
                    acc.setStatus(rs.getString("status"));
                    acc.setCreatedAt(rs.getString("created_at"));
                    return acc;
                }
            }
        }
        return null;
    }

    public Account authenticate(String accountNumber, String pin) throws SQLException {
        Account acc = getAccount(accountNumber);
        if (acc == null) {
            throw new IllegalArgumentException("Account number not found: " + accountNumber);
        }
        if (acc.isLocked()) {
            throw new IllegalStateException("ACCOUNT LOCKED: 3 consecutive failed PIN attempts. Please contact bank administration for unlocking.");
        }

        boolean valid = SecurityUtils.verifyPin(pin, acc.getPinSalt(), acc.getPinHash());
        if (valid) {
            // Reset failed counter
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET failed_pin_attempts = 0 WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ps.executeUpdate();
            }
            acc.setFailedPinAttempts(0);
            return acc;
        } else {
            int newAttempts = acc.getFailedPinAttempts() + 1;
            String newStatus = newAttempts >= 3 ? "LOCKED" : "ACTIVE";

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET failed_pin_attempts = ?, status = ? WHERE account_number = ?")) {
                ps.setInt(1, newAttempts);
                ps.setString(2, newStatus);
                ps.setString(3, accountNumber);
                ps.executeUpdate();
            }

            if (newAttempts >= 3) {
                throw new IllegalStateException("SECURITY LOCKOUT: Incorrect PIN entered 3 times. Account has been LOCKED!");
            } else {
                throw new SecurityException(String.format("Invalid PIN! Attempt %d of 3. Account will lock after 3 strikes.", newAttempts));
            }
        }
    }

    public boolean unlockAccount(String accountNumber) throws SQLException {
        String sql = "UPDATE accounts SET failed_pin_attempts = 0, status = 'ACTIVE' WHERE account_number = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    public Transaction deposit(String accountNumber, double amount, String description) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero.");
        }

        Account acc = getAccount(accountNumber);
        if (acc == null || !acc.isActive()) {
            throw new IllegalStateException("Account is inactive or does not exist.");
        }

        double newBalance = acc.getBalance() + amount;
        String txRef = "TX" + System.currentTimeMillis() + (int)(Math.random() * 1000);

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psAcc = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                    psAcc.setDouble(1, newBalance);
                    psAcc.setString(2, accountNumber);
                    psAcc.executeUpdate();
                }

                try (PreparedStatement psTx = conn.prepareStatement("""
                    INSERT INTO transactions (transaction_ref, account_number, tx_type, amount, balance_after, description)
                    VALUES (?, ?, 'DEPOSIT', ?, ?, ?)
                """)) {
                    psTx.setString(1, txRef);
                    psTx.setString(2, accountNumber);
                    psTx.setDouble(3, amount);
                    psTx.setDouble(4, newBalance);
                    psTx.setString(5, description != null ? description : "Cash Deposit at ATM");
                    psTx.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        return new Transaction(txRef, accountNumber, "DEPOSIT", amount, null, newBalance, description);
    }

    public WithdrawalResult withdraw(String accountNumber, double amount) throws SQLException {
        if (amount <= 0) {
            return new WithdrawalResult(false, "Withdrawal amount must be greater than zero.", null, null);
        }
        if (amount % 100 != 0) {
            return new WithdrawalResult(false, "Withdrawal amount must be a multiple of INR 100.", null, null);
        }

        Account acc = getAccount(accountNumber);
        if (acc == null || !acc.isActive()) {
            return new WithdrawalResult(false, "Account is not active or does not exist.", null, null);
        }

        if (acc.getDailyWithdrawn() + amount > DAILY_WITHDRAWAL_LIMIT) {
            return new WithdrawalResult(false, String.format("Daily limit exceeded! Max daily withdrawal is INR %,.2f (Already used: INR %,.2f)",
                    DAILY_WITHDRAWAL_LIMIT, acc.getDailyWithdrawn()), null, null);
        }

        double minRequired = "SAVINGS".equalsIgnoreCase(acc.getAccountType()) ? SAVINGS_MINIMUM_BALANCE : 0.0;
        if (acc.getBalance() - amount < minRequired) {
            return new WithdrawalResult(false, String.format("Insufficient funds! Account must maintain minimum balance of INR %,.2f. Available to withdraw: INR %,.2f",
                    minRequired, Math.max(0.0, acc.getBalance() - minRequired)), null, null);
        }

        Map<Integer, Integer> notes = calculateDenominations((int) amount);
        double newBalance = acc.getBalance() - amount;
        double newDaily = acc.getDailyWithdrawn() + amount;
        String txRef = "TX" + System.currentTimeMillis() + (int)(Math.random() * 1000);

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psAcc = conn.prepareStatement("UPDATE accounts SET balance = ?, daily_withdrawn = ? WHERE account_number = ?")) {
                    psAcc.setDouble(1, newBalance);
                    psAcc.setDouble(2, newDaily);
                    psAcc.setString(3, accountNumber);
                    psAcc.executeUpdate();
                }

                try (PreparedStatement psTx = conn.prepareStatement("""
                    INSERT INTO transactions (transaction_ref, account_number, tx_type, amount, balance_after, description)
                    VALUES (?, ?, 'WITHDRAWAL', ?, ?, ?)
                """)) {
                    psTx.setString(1, txRef);
                    psTx.setString(2, accountNumber);
                    psTx.setDouble(3, amount);
                    psTx.setDouble(4, newBalance);
                    psTx.setString(5, "Cash Withdrawal at ATM terminal");
                    psTx.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        Transaction tx = new Transaction(txRef, accountNumber, "WITHDRAWAL", amount, null, newBalance, "Cash Withdrawal");
        return new WithdrawalResult(true, "Cash dispensed successfully.", tx, notes);
    }

    public boolean transferFunds(String senderAccNum, String receiverAccNum, double amount, String remarks) throws SQLException {
        if (senderAccNum.equals(receiverAccNum)) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account sender = getAccountWithConn(conn, senderAccNum);
                Account receiver = getAccountWithConn(conn, receiverAccNum);

                if (sender == null || !sender.isActive()) {
                    throw new IllegalStateException("Sender account is invalid or locked.");
                }
                if (receiver == null || !receiver.isActive()) {
                    throw new IllegalStateException("Receiver account is invalid or locked.");
                }

                double minRequired = "SAVINGS".equalsIgnoreCase(sender.getAccountType()) ? SAVINGS_MINIMUM_BALANCE : 0.0;
                if (sender.getBalance() - amount < minRequired) {
                    throw new IllegalStateException(String.format("Transfer failed: Insufficient funds. Sender must maintain INR %,.2f balance.", minRequired));
                }

                double senderNewBal = sender.getBalance() - amount;
                double receiverNewBal = receiver.getBalance() + amount;
                String txRefOut = "TX" + System.currentTimeMillis() + "A";
                String txRefIn = "TX" + System.currentTimeMillis() + "B";

                // Update sender
                try (PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                    ps.setDouble(1, senderNewBal);
                    ps.setString(2, senderAccNum);
                    ps.executeUpdate();
                }

                // Update receiver
                try (PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                    ps.setDouble(1, receiverNewBal);
                    ps.setString(2, receiverAccNum);
                    ps.executeUpdate();
                }

                // Log debit
                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO transactions (transaction_ref, account_number, tx_type, amount, source_dest_account, balance_after, description)
                    VALUES (?, ?, 'TRANSFER_OUT', ?, ?, ?, ?)
                """)) {
                    ps.setString(1, txRefOut);
                    ps.setString(2, senderAccNum);
                    ps.setDouble(3, amount);
                    ps.setString(4, receiverAccNum);
                    ps.setDouble(5, senderNewBal);
                    ps.setString(6, "Transfer to Acc: " + receiverAccNum + " (" + (remarks != null ? remarks : "") + ")");
                    ps.executeUpdate();
                }

                // Log credit
                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO transactions (transaction_ref, account_number, tx_type, amount, source_dest_account, balance_after, description)
                    VALUES (?, ?, 'TRANSFER_IN', ?, ?, ?, ?)
                """)) {
                    ps.setString(1, txRefIn);
                    ps.setString(2, receiverAccNum);
                    ps.setDouble(3, amount);
                    ps.setString(4, senderAccNum);
                    ps.setDouble(5, receiverNewBal);
                    ps.setString(6, "Transfer received from Acc: " + senderAccNum + " (" + (remarks != null ? remarks : "") + ")");
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Transaction> getMiniStatement(String accountNumber, int limit) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setTransactionRef(rs.getString("transaction_ref"));
                    tx.setAccountNumber(rs.getString("account_number"));
                    tx.setTxType(rs.getString("tx_type"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setSourceDestAccount(rs.getString("source_dest_account"));
                    tx.setBalanceAfter(rs.getDouble("balance_after"));
                    tx.setDescription(rs.getString("description"));
                    tx.setTimestamp(rs.getString("timestamp"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    public Map<Integer, Integer> calculateDenominations(int amount) {
        int[] denoms = {2000, 500, 200, 100};
        Map<Integer, Integer> result = new LinkedHashMap<>();
        int remaining = amount;
        for (int d : denoms) {
            if (remaining >= d) {
                int count = remaining / d;
                result.put(d, count);
                remaining %= d;
            } else {
                result.put(d, 0);
            }
        }
        return result;
    }

    public String generateReceipt(Transaction tx, Account acc, Map<Integer, Integer> denominations) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         LOGIC LEGEND NATIONAL BANK       \n");
        sb.append("              ATM RECEIPT                \n");
        sb.append("=========================================\n");
        sb.append(String.format("Date / Time     : %s\n", tx.getTimestamp() != null ? tx.getTimestamp() : new Date().toString()));
        sb.append(String.format("ATM Terminal ID : ATM-LL-0042\n"));
        sb.append(String.format("Transaction Ref : %s\n", tx.getTransactionRef()));
        sb.append(String.format("Account Number  : ••••••••%s\n", acc.getAccountNumber().substring(Math.max(0, acc.getAccountNumber().length() - 4))));
        sb.append(String.format("Account Holder  : %s\n", acc.getHolderName()));
        sb.append(String.format("Transaction Type: %s\n", tx.getTxType()));
        sb.append(String.format("Amount (INR)    : %,.2f\n", tx.getAmount()));
        sb.append(String.format("Available Bal   : INR %,.2f\n", tx.getBalanceAfter()));

        if (denominations != null && !denominations.isEmpty()) {
            sb.append("-----------------------------------------\n");
            sb.append("Currency Notes Dispensed:\n");
            for (Map.Entry<Integer, Integer> e : denominations.entrySet()) {
                if (e.getValue() > 0) {
                    sb.append(String.format("  INR %-4d x %-2d = INR %,d\n", e.getKey(), e.getValue(), e.getKey() * e.getValue()));
                }
            }
        }
        sb.append("=========================================\n");
        sb.append("    Thank you for banking with us!       \n");
        sb.append("      Please take your cash & card       \n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    public void seedDemoAccounts() throws SQLException {
        if (getAccount("1001001001") == null) {
            createAccountExplicit("1001001001", "Arjun Sharma", "1234", 15000.0, "arjun@example.com", "9876543210", "SAVINGS");
            createAccountExplicit("1001002002", "Priya Patel", "4321", 45000.0, "priya@example.com", "9876543211", "SAVINGS");
            createAccountExplicit("1001003003", "TechCorp Enterprises", "9999", 125000.0, "billing@techcorp.com", "9876543212", "CURRENT");
        }
    }

    private void createAccountExplicit(String accNum, String name, String pin, double bal, String email, String phone, String type) throws SQLException {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPin(pin, salt);
        String sql = """
            INSERT OR IGNORE INTO accounts (account_number, holder_name, email, phone, account_type, pin_hash, pin_salt, balance)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accNum);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, type);
            ps.setString(6, hash);
            ps.setString(7, salt);
            ps.setDouble(8, bal);
            ps.executeUpdate();
        }
    }

    private String generateUniqueAccountNumber() throws SQLException {
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            String cand = "100" + (1000000 + rand.nextInt(9000000));
            if (getAccount(cand) == null) {
                return cand;
            }
        }
        return "100" + System.currentTimeMillis() % 10000000;
    }

    private Account getAccountWithConn(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setId(rs.getInt("id"));
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setHolderName(rs.getString("holder_name"));
                    acc.setAccountType(rs.getString("account_type"));
                    acc.setBalance(rs.getDouble("balance"));
                    acc.setStatus(rs.getString("status"));
                    return acc;
                }
            }
        }
        return null;
    }
}
