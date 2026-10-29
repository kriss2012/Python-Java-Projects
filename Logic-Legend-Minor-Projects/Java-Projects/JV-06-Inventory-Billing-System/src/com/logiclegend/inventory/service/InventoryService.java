package com.logiclegend.inventory.service;

import com.logiclegend.inventory.dao.DatabaseManager;
import com.logiclegend.inventory.model.Customer;
import com.logiclegend.inventory.model.Invoice;
import com.logiclegend.inventory.model.InvoiceItem;
import com.logiclegend.inventory.model.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core business service for POS checkout, inventory replenishment,
 * stock reservation, low-stock notifications, and financial receipt generation.
 */
public class InventoryService {
    private final DatabaseManager dbManager;
    private static final double DEFAULT_TAX_RATE = 0.18; // 18% GST

    public InventoryService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------------------
    // PRODUCT & INVENTORY MANAGEMENT
    // -------------------------------------------------------------------------

    public Product addProduct(String code, String name, String category,
                              double costPrice, double unitPrice,
                              int initialStock, int reorderLevel) throws SQLException {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("Product code / barcode is required.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name is required.");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative.");
        if (initialStock < 0) throw new IllegalArgumentException("Initial stock cannot be negative.");

        String sql = """
            INSERT INTO products (product_code, name, category, cost_price, unit_price, stock_quantity, reorder_level)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        int prodId;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.setString(3, category != null ? category.trim() : "General");
            ps.setDouble(4, costPrice >= 0 ? costPrice : 0.0);
            ps.setDouble(5, unitPrice);
            ps.setInt(6, initialStock);
            ps.setInt(7, reorderLevel >= 0 ? reorderLevel : 5);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                prodId = rs.next() ? rs.getInt(1) : -1;
            }

            if (initialStock > 0 && prodId > 0) {
                logStockChange(conn, prodId, "INITIAL_STOCK", initialStock, initialStock, "Initial stock ingestion");
            }
        }
        return getProductById(prodId);
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    public Product getProductByCode(String code) throws SQLException {
        if (code == null) return null;
        String sql = "SELECT * FROM products WHERE UPPER(product_code) = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity <= reorder_level ORDER BY stock_quantity ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String query = "%" + (keyword != null ? keyword.trim().toUpperCase() : "") + "%";
        String sql = "SELECT * FROM products WHERE UPPER(product_code) LIKE ? OR UPPER(name) LIKE ? OR UPPER(category) LIKE ? ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query);
            ps.setString(2, query);
            ps.setString(3, query);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        }
        return list;
    }

    public boolean restockProduct(int productId, int additionalQuantity, String remarks) throws SQLException {
        if (additionalQuantity <= 0) throw new IllegalArgumentException("Restock quantity must be positive.");

        String updateSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, additionalQuantity);
                    ps.setInt(2, productId);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                Product p = getProductById(productId);
                int finalStock = p != null ? p.getStockQuantity() : additionalQuantity;
                logStockChange(conn, productId, "RESTOCK", additionalQuantity, finalStock, remarks != null ? remarks : "Manual restock replenishment");

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // CUSTOMER & LOYALTY MANAGEMENT
    // -------------------------------------------------------------------------

    public Customer registerCustomer(String name, String phone, String email) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Customer name is required.");
        if (phone == null || phone.trim().isEmpty()) throw new IllegalArgumentException("Customer phone is required.");

        String sql = "INSERT INTO customers (name, phone, email, loyalty_points) VALUES (?, ?, ?, 0)";
        int custId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, phone.trim());
            ps.setString(3, email != null ? email.trim() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                custId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getCustomerById(custId);
    }

    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        }
        return null;
    }

    public Customer getCustomerByPhone(String phone) throws SQLException {
        if (phone == null) return null;
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        }
        return null;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // POINT-OF-SALE CHECKOUT & INVOICING (ACID TRANSACTION)
    // -------------------------------------------------------------------------

    public Invoice processCheckout(Integer customerId, String customerName,
                                   List<InvoiceItem> cartItems,
                                   double discountPercentage, double taxRate,
                                   String paymentMethod, int redeemPoints) throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout.");
        }

        taxRate = taxRate >= 0 ? taxRate : DEFAULT_TAX_RATE;
        discountPercentage = Math.max(0, Math.min(100, discountPercentage));
        paymentMethod = paymentMethod != null ? paymentMethod.toUpperCase() : "CASH";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Step 1: Verify inventory availability and deduct stock atomically
                double subtotal = 0.0;
                for (InvoiceItem item : cartItems) {
                    Product p = getProductForUpdate(conn, item.getProductId());
                    if (p == null) {
                        throw new IllegalStateException("Product ID " + item.getProductId() + " not found in database.");
                    }
                    if (p.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalStateException(String.format(
                                "Insufficient stock for '%s' (Code: %s). Requested: %d, Available: %d",
                                p.getName(), p.getProductCode(), item.getQuantity(), p.getStockQuantity()
                        ));
                    }

                    // Populate correct metadata
                    item.setProductCode(p.getProductCode());
                    item.setProductName(p.getName());
                    item.setUnitPrice(p.getUnitPrice());
                    item.setTotalPrice(p.getUnitPrice() * item.getQuantity());
                    subtotal += item.getTotalPrice();

                    // Deduct stock
                    int newStock = p.getStockQuantity() - item.getQuantity();
                    try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE products SET stock_quantity = ? WHERE id = ?")) {
                        psUpdate.setInt(1, newStock);
                        psUpdate.setInt(2, p.getId());
                        psUpdate.executeUpdate();
                    }

                    logStockChange(conn, p.getId(), "SALE", -item.getQuantity(), newStock, "Sale item in cart");
                }

                // Step 2: Customer Loyalty verification & calculation
                Customer customer = null;
                if (customerId != null && customerId > 0) {
                    customer = getCustomerById(customerId);
                }

                int pointsToRedeem = 0;
                double loyaltyDiscount = 0.0;
                if (customer != null && redeemPoints > 0) {
                    pointsToRedeem = Math.min(customer.getLoyaltyPoints(), redeemPoints);
                    loyaltyDiscount = pointsToRedeem * 1.0; // ₹1 per loyalty point
                }

                double percentageDiscount = subtotal * (discountPercentage / 100.0);
                double totalDiscount = percentageDiscount + loyaltyDiscount;
                if (totalDiscount > subtotal) totalDiscount = subtotal;

                double taxableAmount = subtotal - totalDiscount;
                double taxAmount = taxableAmount * taxRate;
                double grandTotal = Math.round((taxableAmount + taxAmount) * 100.0) / 100.0;

                int pointsEarned = (int) (grandTotal / 100.0); // 1 point per ₹100 spent

                // Step 3: Insert Invoice record
                String invNumber = "INV-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()) +
                                   "-" + (1000 + new Random().nextInt(9000));

                String invSql = """
                    INSERT INTO invoices (invoice_number, customer_id, customer_name, subtotal, discount_amount, tax_amount,
                                          grand_total, payment_method, loyalty_points_earned, loyalty_points_redeemed)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

                int invoiceId;
                try (PreparedStatement psInv = conn.prepareStatement(invSql, Statement.RETURN_GENERATED_KEYS)) {
                    psInv.setString(1, invNumber);
                    if (customerId != null && customerId > 0) psInv.setInt(2, customerId);
                    else psInv.setNull(2, Types.INTEGER);
                    psInv.setString(3, customerName != null && !customerName.trim().isEmpty() ? customerName.trim() : "Walk-in Customer");
                    psInv.setDouble(4, subtotal);
                    psInv.setDouble(5, totalDiscount);
                    psInv.setDouble(6, taxAmount);
                    psInv.setDouble(7, grandTotal);
                    psInv.setString(8, paymentMethod);
                    psInv.setInt(9, pointsEarned);
                    psInv.setInt(10, pointsToRedeem);
                    psInv.executeUpdate();

                    try (ResultSet rs = psInv.getGeneratedKeys()) {
                        invoiceId = rs.next() ? rs.getInt(1) : -1;
                    }
                }

                // Step 4: Insert Invoice Items
                String itemSql = """
                    INSERT INTO invoice_items (invoice_id, product_id, product_code, product_name, unit_price, quantity, total_price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                    for (InvoiceItem item : cartItems) {
                        psItem.setInt(1, invoiceId);
                        psItem.setInt(2, item.getProductId());
                        psItem.setString(3, item.getProductCode());
                        psItem.setString(4, item.getProductName());
                        psItem.setDouble(5, item.getUnitPrice());
                        psItem.setInt(6, item.getQuantity());
                        psItem.setDouble(7, item.getTotalPrice());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                // Step 5: Update customer loyalty points
                if (customer != null) {
                    int updatedPoints = customer.getLoyaltyPoints() - pointsToRedeem + pointsEarned;
                    try (PreparedStatement psCust = conn.prepareStatement("UPDATE customers SET loyalty_points = ? WHERE id = ?")) {
                        psCust.setInt(1, updatedPoints);
                        psCust.setInt(2, customer.getId());
                        psCust.executeUpdate();
                    }
                }

                conn.commit();
                return getInvoiceById(invoiceId);
            } catch (Exception e) {
                conn.rollback();
                throw new SQLException("Checkout transaction aborted and rolled back: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Invoice getInvoiceById(int id) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapInvoice(rs);
                    inv.setItems(getInvoiceItems(inv.getId()));
                    return inv;
                }
            }
        }
        return null;
    }

    public List<Invoice> getAllInvoices(int limit) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices ORDER BY id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = mapInvoice(rs);
                    inv.setItems(getInvoiceItems(inv.getId()));
                    list.add(inv);
                }
            }
        }
        return list;
    }

    public List<InvoiceItem> getInvoiceItems(int invoiceId) throws SQLException {
        List<InvoiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getInt("id"));
                    item.setInvoiceId(rs.getInt("invoice_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductCode(rs.getString("product_code"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setTotalPrice(rs.getDouble("total_price"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // RECEIPT GENERATION & EXPORTS
    // -------------------------------------------------------------------------

    public String generateReceiptText(Invoice inv) {
        if (inv == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                   LOGIC LEGEND RETAIL & WHOLESALE HUB\n");
        sb.append("                 GSTIN: 29AAAAA0000A1Z5 | Phone: +91 98765 43210\n");
        sb.append("======================================================================\n");
        String dateStr = (inv.getCreatedAt() != null) ? inv.getCreatedAt() : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        sb.append(String.format("Invoice No:   %-20s Date: %s\n", inv.getInvoiceNumber(), dateStr));
        sb.append(String.format("Customer:     %-20s Payment Mode: %s\n", inv.getCustomerName(), inv.getPaymentMethod()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("%-10s | %-26s | %-6s | %-10s | %-10s\n", "Code", "Item Description", "Qty", "Unit Price", "Total (INR)"));
        sb.append("----------------------------------------------------------------------\n");

        for (InvoiceItem item : inv.getItems()) {
            sb.append(String.format("%-10s | %-26s | %-6d | %,10.2f | %,10.2f\n",
                    item.getProductCode(),
                    truncate(item.getProductName(), 26),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));
        }

        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("%55s: %,10.2f\n", "Subtotal", inv.getSubtotal()));
        sb.append(String.format("%55s: -%,9.2f\n", "Discount Applied", inv.getDiscountAmount()));
        sb.append(String.format("%55s: %,10.2f\n", "GST Tax (18%)", inv.getTaxAmount()));
        sb.append("======================================================================\n");
        sb.append(String.format("%55s: %,10.2f\n", "GRAND TOTAL (INR)", inv.getGrandTotal()));
        sb.append("======================================================================\n");
        sb.append(String.format("Loyalty Points Redeemed: %d | Loyalty Points Earned: +%d\n", inv.getLoyaltyPointsRedeemed(), inv.getLoyaltyPointsEarned()));
        sb.append("       Thank you for shopping with us! Please retain this invoice.\n");
        sb.append("======================================================================\n");
        return sb.toString();
    }

    public File exportReceiptToFile(Invoice inv, String targetDirectory) throws IOException {
        File dir = new File(targetDirectory != null ? targetDirectory : "exports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, inv.getInvoiceNumber() + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateReceiptText(inv));
        }
        return file;
    }

    public Map<String, Object> getDailySalesSummary() throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();
        String sql = """
            SELECT COUNT(*) AS total_invoices,
                   COALESCE(SUM(grand_total), 0.0) AS total_revenue,
                   COALESCE(SUM(discount_amount), 0.0) AS total_discounts
            FROM invoices
        """;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                summary.put("totalInvoices", rs.getInt("total_invoices"));
                summary.put("totalRevenue", rs.getDouble("total_revenue"));
                summary.put("totalDiscounts", rs.getDouble("total_discounts"));
            }
        }
        return summary;
    }

    // -------------------------------------------------------------------------
    // HELPER & SEED METHODS
    // -------------------------------------------------------------------------

    private Product getProductForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    private void logStockChange(Connection conn, int prodId, String type, int qty, int finalStock, String remarks) throws SQLException {
        String sql = "INSERT INTO stock_logs (product_id, change_type, quantity_changed, final_stock, remarks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prodId);
            ps.setString(2, type);
            ps.setInt(3, qty);
            ps.setInt(4, finalStock);
            ps.setString(5, remarks);
            ps.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setProductCode(rs.getString("product_code"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setUnitPrice(rs.getDouble("unit_price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setCustomerId((Integer) rs.getObject("customer_id"));
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setSubtotal(rs.getDouble("subtotal"));
        inv.setDiscountAmount(rs.getDouble("discount_amount"));
        inv.setTaxAmount(rs.getDouble("tax_amount"));
        inv.setGrandTotal(rs.getDouble("grand_total"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setLoyaltyPointsEarned(rs.getInt("loyalty_points_earned"));
        inv.setLoyaltyPointsRedeemed(rs.getInt("loyalty_points_redeemed"));
        inv.setCreatedAt(rs.getString("created_at"));
        return inv;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    public void seedDemoData() throws SQLException {
        if (!getAllProducts().isEmpty()) return;

        // Seed sample products
        addProduct("BAR-1001", "Logitech Wireless Mouse M185", "Electronics", 450.0, 799.0, 45, 10);
        addProduct("BAR-1002", "SanDisk 64GB Ultra USB 3.0", "Electronics", 320.0, 549.0, 30, 8);
        addProduct("BAR-1003", "Dell Professional Backpack 15", "Accessories", 850.0, 1499.0, 18, 5);
        addProduct("BAR-1004", "Classmate Octane Gel Pen Pack", "Stationery", 60.0, 100.0, 120, 25);
        addProduct("BAR-1005", "Boat BassHeads 100 Earphones", "Audio", 280.0, 499.0, 4, 10); // Low stock trigger
        addProduct("BAR-1006", "HP 108W Laser Printer Toner", "Supplies", 1200.0, 2199.0, 3, 5);  // Low stock trigger

        // Seed sample customers
        registerCustomer("Rohan Deshmukh", "9876543210", "rohan@example.com");
        registerCustomer("Priya Sundaram", "9123456780", "priya@example.com");
    }
}
