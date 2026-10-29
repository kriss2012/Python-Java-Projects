package com.logiclegend.banking;

import com.logiclegend.banking.dao.DatabaseManager;
import com.logiclegend.banking.model.Account;
import com.logiclegend.banking.model.Transaction;
import com.logiclegend.banking.service.BankingService;
import com.logiclegend.banking.ui.ATMFrame;

import javax.swing.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        BankingService service = new BankingService(dbManager);

        try {
            service.seedDemoAccounts();
        } catch (Exception e) {
            System.err.println("[WARN] Could not seed demo accounts: " + e.getMessage());
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
                    ATMFrame frame = new ATMFrame(service);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Graphical desktop environment unavailable (" + e.getMessage() + "). Falling back to interactive CLI terminal.");
                runCli(service);
            }
        }
    }

    public static void runCli(BankingService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-01: ONLINE BANKING & ATM SIMULATOR (CLI)");
        System.out.println("=================================================================");
        System.out.println("Demo Accounts Available:");
        System.out.println("  * Arjun Sharma  | Acc: 1001001001 | PIN: 1234 (Savings, Bal: INR 15,000)");
        System.out.println("  * Priya Patel   | Acc: 1001002002 | PIN: 4321 (Savings, Bal: INR 45,000)");
        System.out.println("  * TechCorp      | Acc: 1001003003 | PIN: 9999 (Current, Bal: INR 125,000)");
        System.out.println("=================================================================");

        Account session = null;

        while (true) {
            if (session == null) {
                System.out.println("\n[ATM LOGIN]");
                System.out.print("Enter Account Number (or 'exit' to quit, 'admin' to unlock): ");
                String accNum = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(accNum)) {
                    System.out.println("Exiting ATM Simulator. Goodbye!");
                    break;
                }
                if ("admin".equalsIgnoreCase(accNum)) {
                    System.out.print("Enter locked account number to reset: ");
                    String toUnlock = scanner.nextLine().trim();
                    try {
                        boolean ok = service.unlockAccount(toUnlock);
                        System.out.println(ok ? "[SUCCESS] Account unlocked and strike count reset." : "[ERROR] Account not found.");
                    } catch (Exception ex) {
                        System.out.println("[ERROR] " + ex.getMessage());
                    }
                    continue;
                }

                System.out.print("Enter 4-digit Security PIN: ");
                String pin = scanner.nextLine().trim();

                try {
                    session = service.authenticate(accNum, pin);
                    System.out.printf("\n[AUTHENTICATION SUCCESS] Welcome, %s!\n", session.getHolderName());
                } catch (Exception ex) {
                    System.out.println("\n[AUTHENTICATION FAILED] " + ex.getMessage());
                }
            } else {
                System.out.println("\n-------------------------------------------------------------");
                System.out.printf("  LOGGED IN: %s (Acc: %s) | Balance: INR %,.2f\n",
                        session.getHolderName(), session.getAccountNumber(), session.getBalance());
                System.out.println("-------------------------------------------------------------");
                System.out.println("  1. Balance Inquiry");
                System.out.println("  2. Cash Withdrawal");
                System.out.println("  3. Cash Deposit");
                System.out.println("  4. Fund Transfer (Atomic ACID)");
                System.out.println("  5. Mini-Statement (Last 10 Transactions)");
                System.out.println("  6. Logout / Eject Card");
                System.out.print("Choose an option [1-6]: ");

                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1" -> {
                        try {
                            session = service.getAccount(session.getAccountNumber());
                            System.out.printf("\n[BALANCE INQUIRY] Current Available Balance: INR %,.2f (Daily Withdrawn: INR %,.2f / %,.2f)\n",
                                    session.getBalance(), session.getDailyWithdrawn(), BankingService.DAILY_WITHDRAWAL_LIMIT);
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "2" -> {
                        System.out.print("Enter withdrawal amount (multiples of 100): INR ");
                        try {
                            double amt = Double.parseDouble(scanner.nextLine().trim());
                            BankingService.WithdrawalResult res = service.withdraw(session.getAccountNumber(), amt);
                            if (res.success) {
                                session = service.getAccount(session.getAccountNumber());
                                System.out.println("\n" + service.generateReceipt(res.transaction, session, res.denominations));
                            } else {
                                System.out.println("\n[WITHDRAWAL FAILED] " + res.message);
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "3" -> {
                        System.out.print("Enter deposit amount: INR ");
                        try {
                            double amt = Double.parseDouble(scanner.nextLine().trim());
                            Transaction tx = service.deposit(session.getAccountNumber(), amt, "ATM Cash Deposit");
                            session = service.getAccount(session.getAccountNumber());
                            System.out.printf("\n[DEPOSIT SUCCESS] Deposited INR %,.2f. Updated Balance: INR %,.2f (Ref: %s)\n",
                                    amt, session.getBalance(), tx.getTransactionRef());
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "4" -> {
                        System.out.print("Enter Destination Account Number: ");
                        String dest = scanner.nextLine().trim();
                        System.out.print("Enter Transfer Amount: INR ");
                        try {
                            double amt = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Enter Remarks/Reason: ");
                            String rem = scanner.nextLine().trim();
                            boolean ok = service.transferFunds(session.getAccountNumber(), dest, amt, rem);
                            if (ok) {
                                session = service.getAccount(session.getAccountNumber());
                                System.out.printf("\n[TRANSFER SUCCESS] Transferred INR %,.2f to Account %s. Updated Balance: INR %,.2f\n",
                                        amt, dest, session.getBalance());
                            }
                        } catch (Exception e) {
                            System.out.println("\n[TRANSFER FAILED] " + e.getMessage());
                        }
                    }
                    case "5" -> {
                        try {
                            List<Transaction> list = service.getMiniStatement(session.getAccountNumber(), 10);
                            System.out.println("\n[MINI-STATEMENT] Last " + list.size() + " Transactions:");
                            System.out.printf("%-15s %-14s %-15s %-15s %s\n", "Ref", "Type", "Amount (INR)", "Balance (INR)", "Timestamp");
                            System.out.println("-----------------------------------------------------------------------------");
                            for (Transaction t : list) {
                                System.out.printf("%-15s %-14s %-15,.2f %-15,.2f %s\n",
                                        t.getTransactionRef(), t.getTxType(), t.getAmount(), t.getBalanceAfter(), t.getTimestamp());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "6" -> {
                        System.out.println("\n[SESSION TERMINATED] Card ejected. Thank you for banking with us!");
                        session = null;
                    }
                    default -> System.out.println("Invalid option. Please choose between 1 and 6.");
                }
            }
        }
    }
}
