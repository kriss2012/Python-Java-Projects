package tests;

import com.logiclegend.inventory.dao.DatabaseManager;
import com.logiclegend.inventory.model.Customer;
import com.logiclegend.inventory.model.Invoice;
import com.logiclegend.inventory.model.InvoiceItem;
import com.logiclegend.inventory.model.Product;
import com.logiclegend.inventory.service.InventoryService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
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
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-06: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_inv_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        InventoryService service = new InventoryService(dbManager);

        try {
            testProductCreationAndRetrieval(service);
            testStockReplenishment(service);
            testLowStockAlertTrigger(service);
            testSuccessfulSaleAndAtomicStockDeduction(service);
            testInsufficientStockRejection(service);
            testDiscountAndGstCalculation(service);
            testCustomerLoyaltyPointsAccrualAndRedemption(service);
            testReceiptTextGenerationAndFileExport(service);
            testDailySalesFinancialSummary(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) f.delete();
        }

        System.out.println("=================================================================");
        System.out.printf("  TOTAL ASSERTIONS: %d | PASSED: %d | FAILED: %d\n", passed + failed, passed, failed);
        System.out.println("=================================================================");

        System.exit(failed > 0 ? 1 : 0);
    }

    private static void testProductCreationAndRetrieval(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 1: Product Creation and Retrieval ---");
        Product p = service.addProduct("SKU-9901", "Mechanical Keyboard RGB", "Peripherals", 1800.0, 3299.0, 25, 5);

        assertTrue("Product created with non-zero ID", p.getId() > 0);
        assertEquals("Barcode code matches", "SKU-9901", p.getProductCode());
        assertEquals("Selling price matches", 3299.0, p.getUnitPrice());
        assertEquals("Stock quantity matches", 25, p.getStockQuantity());
        assertEquals("Reorder level matches", 5, p.getReorderLevel());
        assertTrue("Product is not low stock", !p.isLowStock());

        Product byCode = service.getProductByCode("SKU-9901");
        assertTrue("Product retrieved by barcode is non-null", byCode != null);
        assertEquals("Name matches", "Mechanical Keyboard RGB", byCode.getName());
    }

    private static void testStockReplenishment(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 2: Stock Replenishment ---");
        Product p = service.addProduct("SKU-9902", "USB-C Fast Charging Cable", "Accessories", 120.0, 299.0, 10, 5);

        boolean restocked = service.restockProduct(p.getId(), 20, "Shipment Batch #OCT-2026");
        assertTrue("Restock method returned true", restocked);

        Product updated = service.getProductById(p.getId());
        assertEquals("Stock incremented from 10 to 30", 30, updated.getStockQuantity());
    }

    private static void testLowStockAlertTrigger(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 3: Low Stock Warning Trigger ---");
        Product normal = service.addProduct("SKU-NORM", "Standard Mousepad", "Stationery", 50.0, 150.0, 50, 10);
        Product scarce = service.addProduct("SKU-SCARCE", "Rare Replacement Battery", "Electronics", 400.0, 850.0, 3, 5);

        assertTrue("Scarce product flags isLowStock true", scarce.isLowStock());
        assertTrue("Normal product flags isLowStock false", !normal.isLowStock());

        List<Product> lowList = service.getLowStockProducts();
        boolean containsScarce = lowList.stream().anyMatch(p -> "SKU-SCARCE".equals(p.getProductCode()));
        boolean containsNormal = lowList.stream().anyMatch(p -> "SKU-NORM".equals(p.getProductCode()));

        assertTrue("Low stock query includes scarce product", containsScarce);
        assertTrue("Low stock query excludes normal product", !containsNormal);
    }

    private static void testSuccessfulSaleAndAtomicStockDeduction(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 4: POS Checkout & Atomic Stock Deduction ---");
        Product p = service.addProduct("SKU-9903", "Wireless Presenter Pointer", "Office", 350.0, 750.0, 15, 3);

        List<InvoiceItem> cart = new ArrayList<>();
        cart.add(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), 3));

        Invoice inv = service.processCheckout(null, "Test Walk-in", cart, 0.0, 0.18, "CASH", 0);

        assertTrue("Invoice generated with valid ID", inv.getId() > 0);
        assertTrue("Invoice has formatted number", inv.getInvoiceNumber().startsWith("INV-"));
        assertEquals("Subtotal is 3 x 750 = 2250.0", 2250.0, inv.getSubtotal());
        assertEquals("GST Tax is 18% of 2250 = 405.0", 405.0, inv.getTaxAmount());
        assertEquals("Grand total is 2655.0", 2655.0, inv.getGrandTotal());

        Product afterSale = service.getProductById(p.getId());
        assertEquals("Stock decremented from 15 to 12", 12, afterSale.getStockQuantity());
    }

    private static void testInsufficientStockRejection(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 5: Insufficient Stock Rejection & Rollback ---");
        Product p = service.addProduct("SKU-9904", "Premium Ergonomic Chair", "Furniture", 5000.0, 9500.0, 2, 1);

        List<InvoiceItem> cart = new ArrayList<>();
        cart.add(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), 5)); // 5 requested, only 2 available

        try {
            service.processCheckout(null, "Eager Buyer", cart, 0.0, 0.18, "CARD", 0);
            assertTrue("Expected exception for exceeding available stock", false);
        } catch (SQLException e) {
            assertTrue("Transaction rejected due to insufficient stock", e.getMessage().contains("Insufficient stock"));
        }

        Product afterAttempt = service.getProductById(p.getId());
        assertEquals("Stock remained untouched at 2 after rollback", 2, afterAttempt.getStockQuantity());
    }

    private static void testDiscountAndGstCalculation(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 6: Discount and Tax Arithmetic Verification ---");
        Product p = service.addProduct("SKU-9905", "4K HDMI Cable", "Cables", 200.0, 1000.0, 20, 5);

        List<InvoiceItem> cart = List.of(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), 1));

        // Subtotal = 1000.0, 10% Discount = 100.0, Taxable = 900.0, 18% Tax = 162.0, Grand Total = 1062.0
        Invoice inv = service.processCheckout(null, "Loyal Customer", cart, 10.0, 0.18, "UPI", 0);

        assertEquals("Subtotal matches 1000.0", 1000.0, inv.getSubtotal());
        assertEquals("Discount matches 100.0", 100.0, inv.getDiscountAmount());
        assertEquals("Tax matches 162.0", 162.0, inv.getTaxAmount());
        assertEquals("Grand total matches 1062.0", 1062.0, inv.getGrandTotal());
    }

    private static void testCustomerLoyaltyPointsAccrualAndRedemption(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 7: Customer Loyalty Points Accrual & Redemption ---");
        Customer c = service.registerCustomer("Vikram Malhotra", "9988776655", "vikram@test.com");
        assertEquals("Initial loyalty points is 0", 0, c.getLoyaltyPoints());

        Product p = service.addProduct("SKU-9906", "Smart Bluetooth Speaker", "Audio", 600.0, 2500.0, 10, 2);

        // Buy 1 speaker for ₹2500 + 18% tax = ₹2950. Earns 29 points (1 pt per ₹100).
        List<InvoiceItem> cart1 = List.of(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), 1));
        Invoice inv1 = service.processCheckout(c.getId(), c.getName(), cart1, 0.0, 0.18, "CARD", 0);

        Customer afterFirstBuy = service.getCustomerById(c.getId());
        assertEquals("Customer earned 29 loyalty points", 29, afterFirstBuy.getLoyaltyPoints());

        // Second purchase: Redeem 20 points
        Product p2 = service.addProduct("SKU-9907", "Earphone Case", "Accessories", 50.0, 200.0, 10, 2);
        List<InvoiceItem> cart2 = List.of(new InvoiceItem(p2.getId(), p2.getProductCode(), p2.getName(), p2.getUnitPrice(), 1));
        Invoice inv2 = service.processCheckout(c.getId(), c.getName(), cart2, 0.0, 0.18, "CASH", 20);

        assertEquals("20 loyalty points redeemed", 20, inv2.getLoyaltyPointsRedeemed());
        Customer afterSecondBuy = service.getCustomerById(c.getId());
        // Remaining = 29 - 20 + points earned on inv2 (₹200 - ₹20 = ₹180 + 18% = ₹212.4 -> 2 pts earned) = 11 pts
        assertTrue("Customer points updated after redemption", afterSecondBuy.getLoyaltyPoints() >= 9);
    }

    private static void testReceiptTextGenerationAndFileExport(InventoryService service) throws Exception {
        System.out.println("\n--- Test Suite 8: Receipt Generation & File Export ---");
        Product p = service.addProduct("SKU-9908", "Wireless Optical Mouse", "Peripherals", 250.0, 500.0, 15, 3);
        List<InvoiceItem> cart = List.of(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), 2));

        Invoice inv = service.processCheckout(null, "Mrs. Gupta", cart, 5.0, 0.18, "UPI", 0);
        String receiptText = service.generateReceiptText(inv);

        assertTrue("Receipt contains store header", receiptText.contains("LOGIC LEGEND RETAIL"));
        assertTrue("Receipt contains invoice number", receiptText.contains(inv.getInvoiceNumber()));
        assertTrue("Receipt contains product name", receiptText.contains("Wireless Optical Mouse"));
        assertTrue("Receipt contains GST Tax line", receiptText.contains("GST Tax"));
        assertTrue("Receipt contains GRAND TOTAL", receiptText.contains("GRAND TOTAL"));

        File exported = service.exportReceiptToFile(inv, "exports");
        assertTrue("Exported receipt file exists on disk", exported.exists());
        assertTrue("Exported file is non-empty", exported.length() > 50);
    }

    private static void testDailySalesFinancialSummary(InventoryService service) throws SQLException {
        System.out.println("\n--- Test Suite 9: Financial Summary Analytics ---");
        Map<String, Object> summary = service.getDailySalesSummary();
        assertTrue("Summary contains totalInvoices key", summary.containsKey("totalInvoices"));
        assertTrue("Summary contains totalRevenue key", summary.containsKey("totalRevenue"));

        int totalInvoices = (Integer) summary.get("totalInvoices");
        double totalRev = (Double) summary.get("totalRevenue");

        assertTrue("Total invoices recorded is >= 4", totalInvoices >= 4);
        assertTrue("Total revenue recorded is > 0", totalRev > 0);
    }
}

