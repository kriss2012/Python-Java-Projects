package tests;

import com.logiclegend.banking.dao.DatabaseManager;
import com.logiclegend.banking.model.Account;
import com.logiclegend.banking.model.Transaction;
import com.logiclegend.banking.service.BankingService;

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
            System.out.printf("  [FAIL] %s - Assertion evaluated to FALSE\n", testName);
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
        System.out.println("  LOGIC LEGEND JV-01: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_bank_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        BankingService service = new BankingService(dbManager);

        try {
            testAccountCreation(service);
            testAuthenticationAnd3StrikeLockout(service);
            testAdminUnlock(service);
            testDeposit(service);
            testWithdrawalAndDenominations(service);
            testAtomicFundTransferSuccess(service);
            testAtomicFundTransferRollback(service);
            testMiniStatement(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            // Clean test database file
            File f = new File(testDbPath);
            if (f.exists()) {
                f.delete();
            }
        }

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed (Total: %d)\n", passed, failed, passed + failed);
        System.out.println("=================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testAccountCreation(BankingService service) throws SQLException {
        Account acc = service.createAccount("Rohan Verma", "5555", 5000.0, "rohan@test.com", "9900112233", "SAVINGS");
        assertTrue("Account creation returns non-null", acc != null);
        assertEquals("Initial balance stored correctly", 5000.0, acc.getBalance());
        assertEquals("Initial status is ACTIVE", "ACTIVE", acc.getStatus());
        assertTrue("PIN is hashed with salt (not plaintext)", !acc.getPinHash().equals("5555"));
    }

    private static void testAuthenticationAnd3StrikeLockout(BankingService service) throws SQLException {
        Account acc = service.createAccount("Vikram Singh", "8888", 10000.0, "vikram@test.com", "9900112244", "SAVINGS");
        String accNum = acc.getAccountNumber();

        // Correct PIN
        Account authOk = service.authenticate(accNum, "8888");
        assertTrue("Authenticate with correct PIN succeeds", authOk != null);

        // Strike 1
        try {
            service.authenticate(accNum, "0000");
            assertTrue("Strike 1 should throw SecurityException", false);
        } catch (SecurityException e) {
            assertTrue("Strike 1 recorded", e.getMessage().contains("Attempt 1 of 3"));
        }

        // Strike 2
        try {
            service.authenticate(accNum, "0000");
            assertTrue("Strike 2 should throw SecurityException", false);
        } catch (SecurityException e) {
            assertTrue("Strike 2 recorded", e.getMessage().contains("Attempt 2 of 3"));
        }

        // Strike 3 - Must lock
        try {
            service.authenticate(accNum, "0000");
            assertTrue("Strike 3 should throw IllegalStateException", false);
        } catch (IllegalStateException e) {
            assertTrue("Account locked on strike 3", e.getMessage().contains("LOCKED"));
        }

        Account locked = service.getAccount(accNum);
        assertEquals("Account status in database is LOCKED", "LOCKED", locked.getStatus());
    }

    private static void testAdminUnlock(BankingService service) throws SQLException {
        Account acc = service.createAccount("Sunita Rao", "9191", 8000.0, "sunita@test.com", "9900112255", "SAVINGS");
        String accNum = acc.getAccountNumber();

        // Lock it
        for (int i = 0; i < 3; i++) {
            try { service.authenticate(accNum, "0000"); } catch (Exception ignored) {}
        }
        assertTrue("Account is locked before admin unlock", service.getAccount(accNum).isLocked());

        boolean unlocked = service.unlockAccount(accNum);
        assertTrue("Admin unlock returned true", unlocked);
        assertTrue("Account status is ACTIVE after unlock", service.getAccount(accNum).isActive());
        assertEquals("Strike count reset to 0", 0, service.getAccount(accNum).getFailedPinAttempts());
    }

    private static void testDeposit(BankingService service) throws SQLException {
        Account acc = service.createAccount("Meera Nair", "1212", 2000.0, "meera@test.com", "9900112266", "SAVINGS");
        Transaction tx = service.deposit(acc.getAccountNumber(), 3500.0, "Test Cash Deposit");

        assertEquals("Transaction amount matches deposit", 3500.0, tx.getAmount());
        Account updated = service.getAccount(acc.getAccountNumber());
        assertEquals("Balance incremented to 5500.0", 5500.0, updated.getBalance());
    }

    private static void testWithdrawalAndDenominations(BankingService service) throws SQLException {
        Account acc = service.createAccount("Kavita Reddy", "3434", 20000.0, "kavita@test.com", "9900112277", "SAVINGS");

        // Test Denomination Calculator: 3800 = 2000x1, 500x3, 200x1, 100x1
        Map<Integer, Integer> denoms = service.calculateDenominations(3800);
        assertEquals("2000 notes count", Integer.valueOf(1), denoms.get(2000));
        assertEquals("500 notes count", Integer.valueOf(3), denoms.get(500));
        assertEquals("200 notes count", Integer.valueOf(1), denoms.get(200));
        assertEquals("100 notes count", Integer.valueOf(1), denoms.get(100));

        // Test actual withdrawal
        BankingService.WithdrawalResult res = service.withdraw(acc.getAccountNumber(), 3800.0);
        assertTrue("Withdrawal successful", res.success);
        Account afterW = service.getAccount(acc.getAccountNumber());
        assertEquals("Balance reduced by 3800", 16200.0, afterW.getBalance());

        // Test daily limit rejection (> 25000)
        BankingService.WithdrawalResult overLimit = service.withdraw(acc.getAccountNumber(), 22000.0);
        assertTrue("Exceeding daily limit of 25000 is rejected", !overLimit.success);
    }

    private static void testAtomicFundTransferSuccess(BankingService service) throws SQLException {
        Account sender = service.createAccount("Sender User", "1111", 10000.0, "sender@test.com", "9900112288", "SAVINGS");
        Account receiver = service.createAccount("Receiver User", "2222", 2000.0, "receiver@test.com", "9900112299", "SAVINGS");

        boolean ok = service.transferFunds(sender.getAccountNumber(), receiver.getAccountNumber(), 3000.0, "Project Payment");
        assertTrue("Fund transfer executed successfully", ok);

        Account sAfter = service.getAccount(sender.getAccountNumber());
        Account rAfter = service.getAccount(receiver.getAccountNumber());
        assertEquals("Sender balance deducted by 3000", 7000.0, sAfter.getBalance());
        assertEquals("Receiver balance credited by 3000", 5000.0, rAfter.getBalance());
    }

    private static void testAtomicFundTransferRollback(BankingService service) throws SQLException {
        Account sender = service.createAccount("Broke Sender", "1111", 1000.0, "broke@test.com", "9900112200", "SAVINGS");
        Account receiver = service.createAccount("Target Receiver", "2222", 500.0, "target@test.com", "9900112201", "SAVINGS");

        // Attempt to transfer 800 (which would leave sender with 200, violating minimum balance of 500)
        boolean failedAsExpected = false;
        try {
            service.transferFunds(sender.getAccountNumber(), receiver.getAccountNumber(), 800.0, "Excessive Transfer");
        } catch (Exception e) {
            failedAsExpected = true;
        }

        assertTrue("Excessive transfer failed with exception", failedAsExpected);

        // Verify ACID Rollback: neither balance changed
        Account sAfter = service.getAccount(sender.getAccountNumber());
        Account rAfter = service.getAccount(receiver.getAccountNumber());
        assertEquals("Sender balance rolled back to 1000.0", 1000.0, sAfter.getBalance());
        assertEquals("Receiver balance untouched at 500.0", 500.0, rAfter.getBalance());
    }

    private static void testMiniStatement(BankingService service) throws SQLException {
        Account acc = service.createAccount("Audit User", "7777", 6000.0, "audit@test.com", "9900112202", "SAVINGS");
        service.deposit(acc.getAccountNumber(), 1000.0, "Deposit 1");
        service.deposit(acc.getAccountNumber(), 2000.0, "Deposit 2");
        service.withdraw(acc.getAccountNumber(), 500.0);

        List<Transaction> list = service.getMiniStatement(acc.getAccountNumber(), 10);
        assertTrue("Mini-statement has recorded transactions", list.size() >= 3);
        assertEquals("Latest transaction was WITHDRAWAL", "WITHDRAWAL", list.get(0).getTxType());
    }
}

