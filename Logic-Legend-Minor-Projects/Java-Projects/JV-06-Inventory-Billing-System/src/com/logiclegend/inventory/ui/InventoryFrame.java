package com.logiclegend.inventory.ui;

import com.logiclegend.inventory.model.Customer;
import com.logiclegend.inventory.model.Invoice;
import com.logiclegend.inventory.model.InvoiceItem;
import com.logiclegend.inventory.model.Product;
import com.logiclegend.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern Swing Point-of-Sale (POS) and Inventory Administration Terminal.
 */
public class InventoryFrame extends JFrame {
    private final InventoryService service;

    // POS State
    private final List<InvoiceItem> activeCart = new ArrayList<>();
    private Customer activeCustomer = null;

    // POS Components
    private JTextField barcodeSearchField;
    private JSpinner quantitySpinner;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel grandTotalLabel;
    private JTextField discountField;
    private JComboBox<String> paymentMethodCombo;
    private JTextField customerPhoneField;
    private JLabel customerInfoLabel;
    private JCheckBox redeemPointsCheckBox;

    // Inventory Tab Components
    private DefaultTableModel inventoryTableModel;
    private JTable inventoryTable;
    private JCheckBox lowStockFilterCheck;

    // Sales Tab Components
    private DefaultTableModel salesTableModel;
    private JTable salesTable;
    private JLabel summaryLabel;

    public InventoryFrame(InventoryService service) {
        this.service = service;

        setTitle("LogicLegend — Point of Sale (POS) & Inventory Billing System");
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 44, 97));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("  LOGIC LEGEND POINT-OF-SALE & INVENTORY TERMINAL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel statusBadge = new JLabel("System Ready | GSTIN: 29AAAAA0000A1Z5");
        statusBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBadge.setForeground(new Color(223, 228, 234));
        headerPanel.add(statusBadge, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Main Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("🛒 POS Billing Checkout", createPosPanel());
        tabbedPane.addTab("📦 Inventory & Stock Control", createInventoryPanel());
        tabbedPane.addTab("📊 Sales Invoices & Reports", createSalesPanel());

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);

        refreshInventoryTable();
        refreshSalesTable();
    }

    // -------------------------------------------------------------------------
    // TAB 1: POS BILLING CHECKOUT
    // -------------------------------------------------------------------------
    private JPanel createPosPanel() {
        JPanel posPanel = new JPanel(new BorderLayout(8, 8));
        posPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Top Search / Barcode Ingestion Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setBorder(BorderFactory.createTitledBorder("Product Barcode Scanner & Lookup"));

        topBar.add(new JLabel("Barcode / Item Code:"));
        barcodeSearchField = new JTextField(16);
        barcodeSearchField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        barcodeSearchField.addActionListener(e -> addItemToCart());
        topBar.add(barcodeSearchField);

        topBar.add(new JLabel("Qty:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        quantitySpinner.setPreferredSize(new Dimension(60, 26));
        topBar.add(quantitySpinner);

        JButton addBtn = new JButton("Add to Cart");
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addItemToCart());
        topBar.add(addBtn);

        JButton clearCartBtn = new JButton("Clear Cart");
        clearCartBtn.setBackground(new Color(231, 76, 60));
        clearCartBtn.setForeground(Color.WHITE);
        clearCartBtn.addActionListener(e -> clearCart());
        topBar.add(clearCartBtn);

        posPanel.add(topBar, BorderLayout.NORTH);

        // Center Cart Table
        cartTableModel = new DefaultTableModel(new String[]{"#", "Barcode", "Product Name", "Unit Price", "Qty", "Total (INR)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(24);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder("Active Customer Shopping Cart"));
        posPanel.add(cartScroll, BorderLayout.CENTER);

        // Right Checkout Panel
        JPanel checkoutPanel = new JPanel(new BorderLayout(8, 8));
        checkoutPanel.setPreferredSize(new Dimension(320, 0));
        checkoutPanel.setBorder(BorderFactory.createTitledBorder("Checkout & Settlement"));

        JPanel custPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        custPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel phoneRow = new JPanel(new BorderLayout(4, 4));
        phoneRow.add(new JLabel("Customer Phone:"), BorderLayout.WEST);
        customerPhoneField = new JTextField();
        customerPhoneField.addActionListener(e -> lookupCustomer());
        phoneRow.add(customerPhoneField, BorderLayout.CENTER);
        JButton searchCustBtn = new JButton("Find");
        searchCustBtn.addActionListener(e -> lookupCustomer());
        phoneRow.add(searchCustBtn, BorderLayout.EAST);
        custPanel.add(phoneRow);

        customerInfoLabel = new JLabel("Customer: Walk-in Customer (0 pts)");
        customerInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        custPanel.add(customerInfoLabel);

        redeemPointsCheckBox = new JCheckBox("Redeem Loyalty Points (₹1/pt)");
        redeemPointsCheckBox.setEnabled(false);
        redeemPointsCheckBox.addActionListener(e -> recalculateTotals());
        custPanel.add(redeemPointsCheckBox);

        JPanel discRow = new JPanel(new BorderLayout(4, 4));
        discRow.add(new JLabel("Store Discount %:"), BorderLayout.WEST);
        discountField = new JTextField("0");
        discountField.addActionListener(e -> recalculateTotals());
        discRow.add(discountField, BorderLayout.CENTER);
        custPanel.add(discRow);

        checkoutPanel.add(custPanel, BorderLayout.NORTH);

        // Calculations Display
        JPanel calcPanel = new JPanel(new GridLayout(6, 1, 4, 4));
        calcPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        calcPanel.setBackground(new Color(245, 246, 250));

        subtotalLabel = new JLabel("Subtotal:  ₹0.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calcPanel.add(subtotalLabel);

        discountLabel = new JLabel("Discount: -₹0.00");
        discountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        discountLabel.setForeground(new Color(192, 57, 43));
        calcPanel.add(discountLabel);

        taxLabel = new JLabel("GST (18%):  ₹0.00");
        taxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calcPanel.add(taxLabel);

        grandTotalLabel = new JLabel("GRAND TOTAL:  ₹0.00");
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        grandTotalLabel.setForeground(new Color(39, 174, 96));
        calcPanel.add(grandTotalLabel);

        JPanel payRow = new JPanel(new BorderLayout(4, 4));
        payRow.setOpaque(false);
        payRow.add(new JLabel("Payment Mode:"), BorderLayout.WEST);
        paymentMethodCombo = new JComboBox<>(new String[]{"CASH", "CARD", "UPI", "NET_BANKING"});
        payRow.add(paymentMethodCombo, BorderLayout.CENTER);
        calcPanel.add(payRow);

        JButton checkoutBtn = new JButton("Complete Sale & Print Receipt");
        checkoutBtn.setBackground(new Color(41, 128, 185));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.addActionListener(e -> finalizeCheckout());
        calcPanel.add(checkoutBtn);

        checkoutPanel.add(calcPanel, BorderLayout.CENTER);
        posPanel.add(checkoutPanel, BorderLayout.EAST);

        return posPanel;
    }

    private void addItemToCart() {
        String query = barcodeSearchField.getText().trim();
        if (query.isEmpty()) return;

        int qty = (int) quantitySpinner.getValue();
        try {
            Product p = service.getProductByCode(query);
            if (p == null) {
                List<Product> matches = service.searchProducts(query);
                if (!matches.isEmpty()) p = matches.get(0);
            }

            if (p == null) {
                JOptionPane.showMessageDialog(this, "Product not found for: '" + query + "'", "Product Lookup", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check existing cart quantity
            int existingInCart = 0;
            for (InvoiceItem item : activeCart) {
                if (item.getProductId() == p.getId()) existingInCart += item.getQuantity();
            }

            if (p.getStockQuantity() < existingInCart + qty) {
                JOptionPane.showMessageDialog(this,
                        String.format("Insufficient stock for '%s'!\nAvailable: %d\nAlready in Cart: %d\nRequested: %d",
                                p.getName(), p.getStockQuantity(), existingInCart, qty),
                        "Stock Limit", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add or merge into cart
            boolean merged = false;
            for (InvoiceItem item : activeCart) {
                if (item.getProductId() == p.getId()) {
                    item.setQuantity(item.getQuantity() + qty);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                activeCart.add(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), qty));
            }

            updateCartDisplay();
            barcodeSearchField.setText("");
            quantitySpinner.setValue(1);
            barcodeSearchField.requestFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        int idx = 1;
        for (InvoiceItem item : activeCart) {
            cartTableModel.addRow(new Object[]{
                    idx++,
                    item.getProductCode(),
                    item.getProductName(),
                    String.format("₹%,.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("₹%,.2f", item.getTotalPrice())
            });
        }
        recalculateTotals();
    }

    private void recalculateTotals() {
        double subtotal = 0.0;
        for (InvoiceItem item : activeCart) subtotal += item.getTotalPrice();

        double discountPct = 0.0;
        try {
            discountPct = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException ignored) {}

        double loyaltyDiscount = 0.0;
        if (redeemPointsCheckBox.isSelected() && activeCustomer != null) {
            loyaltyDiscount = Math.min(subtotal, activeCustomer.getLoyaltyPoints() * 1.0);
        }

        double pctDiscount = subtotal * (discountPct / 100.0);
        double totalDiscount = Math.min(subtotal, pctDiscount + loyaltyDiscount);
        double taxable = subtotal - totalDiscount;
        double tax = taxable * 0.18;
        double grand = taxable + tax;

        subtotalLabel.setText(String.format("Subtotal:  ₹%,.2f", subtotal));
        discountLabel.setText(String.format("Discount: -₹%,.2f", totalDiscount));
        taxLabel.setText(String.format("GST (18%):  ₹%,.2f", tax));
        grandTotalLabel.setText(String.format("GRAND TOTAL:  ₹%,.2f", grand));
    }

    private void clearCart() {
        activeCart.clear();
        updateCartDisplay();
    }

    private void lookupCustomer() {
        String phone = customerPhoneField.getText().trim();
        if (phone.isEmpty()) {
            activeCustomer = null;
            customerInfoLabel.setText("Customer: Walk-in Customer (0 pts)");
            redeemPointsCheckBox.setEnabled(false);
            redeemPointsCheckBox.setSelected(false);
            recalculateTotals();
            return;
        }

        try {
            Customer c = service.getCustomerByPhone(phone);
            if (c != null) {
                activeCustomer = c;
                customerInfoLabel.setText(String.format("Customer: %s (%d pts)", c.getName(), c.getLoyaltyPoints()));
                redeemPointsCheckBox.setEnabled(c.getLoyaltyPoints() > 0);
                redeemPointsCheckBox.setText("Redeem " + c.getLoyaltyPoints() + " Pts (₹" + c.getLoyaltyPoints() + ")");
            } else {
                int res = JOptionPane.showConfirmDialog(this, "Customer phone not found. Would you like to register new customer?", "New Customer", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    String name = JOptionPane.showInputDialog(this, "Enter Customer Name:");
                    if (name != null && !name.trim().isEmpty()) {
                        activeCustomer = service.registerCustomer(name.trim(), phone, null);
                        customerInfoLabel.setText(String.format("Customer: %s (0 pts)", activeCustomer.getName()));
                    }
                }
            }
            recalculateTotals();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Customer lookup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizeCheckout() {
        if (activeCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Please add items before checking out.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double discountPct = 0.0;
        try {
            discountPct = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException ignored) {}

        int redeemPts = (redeemPointsCheckBox.isSelected() && activeCustomer != null) ? activeCustomer.getLoyaltyPoints() : 0;
        String payMethod = (String) paymentMethodCombo.getSelectedItem();
        Integer custId = activeCustomer != null ? activeCustomer.getId() : null;
        String custName = activeCustomer != null ? activeCustomer.getName() : "Walk-in Customer";

        try {
            Invoice inv = service.processCheckout(custId, custName, activeCart, discountPct, 0.18, payMethod, redeemPts);
            File receiptFile = service.exportReceiptToFile(inv, "exports");

            // Display receipt dialog
            JTextArea receiptArea = new JTextArea(service.generateReceiptText(inv));
            receiptArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            receiptArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(receiptArea);
            scroll.setPreferredSize(new Dimension(600, 450));

            JOptionPane.showMessageDialog(this, scroll, "Sale Complete — Invoice #" + inv.getInvoiceNumber(), JOptionPane.INFORMATION_MESSAGE);

            // Reset state
            clearCart();
            customerPhoneField.setText("");
            lookupCustomer();
            refreshInventoryTable();
            refreshSalesTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // TAB 2: INVENTORY & STOCK MANAGEMENT
    // -------------------------------------------------------------------------
    private JPanel createInventoryPanel() {
        JPanel invPanel = new JPanel(new BorderLayout(8, 8));
        invPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        lowStockFilterCheck = new JCheckBox("Show Low-Stock Alerts Only");
        lowStockFilterCheck.addActionListener(e -> refreshInventoryTable());
        topBar.add(lowStockFilterCheck);

        JButton addProductBtn = new JButton("Add New Product");
        addProductBtn.setBackground(new Color(52, 152, 219));
        addProductBtn.setForeground(Color.WHITE);
        addProductBtn.addActionListener(e -> promptAddProduct());
        topBar.add(addProductBtn);

        JButton restockBtn = new JButton("Replenish Stock (+Qty)");
        restockBtn.setBackground(new Color(39, 174, 96));
        restockBtn.setForeground(Color.WHITE);
        restockBtn.addActionListener(e -> promptRestock());
        topBar.add(restockBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshInventoryTable());
        topBar.add(refreshBtn);

        invPanel.add(topBar, BorderLayout.NORTH);

        inventoryTableModel = new DefaultTableModel(new String[]{"ID", "Barcode", "Product Name", "Category", "Cost Price", "Selling Price", "Stock Qty", "Reorder Level", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(inventoryTable);
        invPanel.add(scroll, BorderLayout.CENTER);

        return invPanel;
    }

    private void promptAddProduct() {
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField catField = new JTextField("General");
        JTextField costField = new JTextField("0");
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField("10");
        JTextField reorderField = new JTextField("5");

        JPanel form = new JPanel(new GridLayout(7, 2, 6, 6));
        form.add(new JLabel("Barcode / Product Code:"));
        form.add(codeField);
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(catField);
        form.add(new JLabel("Cost Price (₹):"));
        form.add(costField);
        form.add(new JLabel("Selling Unit Price (₹):"));
        form.add(priceField);
        form.add(new JLabel("Initial Stock:"));
        form.add(stockField);
        form.add(new JLabel("Reorder Alert Level:"));
        form.add(reorderField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Inventory Product", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                service.addProduct(
                        codeField.getText().trim(),
                        nameField.getText().trim(),
                        catField.getText().trim(),
                        Double.parseDouble(costField.getText().trim()),
                        Double.parseDouble(priceField.getText().trim()),
                        Integer.parseInt(stockField.getText().trim()),
                        Integer.parseInt(reorderField.getText().trim())
                );
                refreshInventoryTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to add product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void promptRestock() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product row to restock.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int prodId = (int) inventoryTableModel.getValueAt(row, 0);
        String name = (String) inventoryTableModel.getValueAt(row, 2);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter additional quantity to add for '" + name + "':", "Restock", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr != null && !qtyStr.trim().isEmpty()) {
            try {
                int addQty = Integer.parseInt(qtyStr.trim());
                service.restockProduct(prodId, addQty, "Manual POS replenishment");
                refreshInventoryTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Restock failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshInventoryTable() {
        inventoryTableModel.setRowCount(0);
        try {
            List<Product> products = lowStockFilterCheck.isSelected() ? service.getLowStockProducts() : service.getAllProducts();
            for (Product p : products) {
                inventoryTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getProductCode(),
                        p.getName(),
                        p.getCategory(),
                        String.format("₹%,.2f", p.getCostPrice()),
                        String.format("₹%,.2f", p.getUnitPrice()),
                        p.getStockQuantity(),
                        p.getReorderLevel(),
                        p.isLowStock() ? "⚠️ LOW STOCK" : "IN STOCK"
                });
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load inventory: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // TAB 3: SALES INVOICES & REPORTS
    // -------------------------------------------------------------------------
    private JPanel createSalesPanel() {
        JPanel salesPanel = new JPanel(new BorderLayout(8, 8));
        salesPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new BorderLayout());
        summaryLabel = new JLabel("Sales Analytics Loading...");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryLabel.setForeground(new Color(41, 128, 185));
        topBar.add(summaryLabel, BorderLayout.WEST);

        JButton viewReceiptBtn = new JButton("View Selected Invoice Receipt");
        viewReceiptBtn.setBackground(new Color(142, 68, 173));
        viewReceiptBtn.setForeground(Color.WHITE);
        viewReceiptBtn.addActionListener(e -> viewSelectedInvoiceReceipt());
        topBar.add(viewReceiptBtn, BorderLayout.EAST);
        salesPanel.add(topBar, BorderLayout.NORTH);

        salesTableModel = new DefaultTableModel(new String[]{"ID", "Invoice Number", "Customer Name", "Subtotal", "Discount", "GST Tax", "Grand Total", "Payment Mode", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(salesTable);
        salesPanel.add(scroll, BorderLayout.CENTER);

        return salesPanel;
    }

    private void viewSelectedInvoiceReceipt() {
        int row = salesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table to view its receipt.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int invId = (int) salesTableModel.getValueAt(row, 0);
        try {
            Invoice inv = service.getInvoiceById(invId);
            if (inv != null) {
                JTextArea area = new JTextArea(service.generateReceiptText(inv));
                area.setFont(new Font("Consolas", Font.PLAIN, 12));
                area.setEditable(false);
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(600, 450));
                JOptionPane.showMessageDialog(this, scroll, "Invoice Receipt #" + inv.getInvoiceNumber(), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to retrieve invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSalesTable() {
        salesTableModel.setRowCount(0);
        try {
            Map<String, Object> summary = service.getDailySalesSummary();
            summaryLabel.setText(String.format("Sales Summary: %d Invoices | Revenue: ₹%,.2f | Discounts: ₹%,.2f",
                    (Integer) summary.getOrDefault("totalInvoices", 0),
                    (Double) summary.getOrDefault("totalRevenue", 0.0),
                    (Double) summary.getOrDefault("totalDiscounts", 0.0)));

            List<Invoice> invoices = service.getAllInvoices(100);
            for (Invoice inv : invoices) {
                salesTableModel.addRow(new Object[]{
                        inv.getId(),
                        inv.getInvoiceNumber(),
                        inv.getCustomerName(),
                        String.format("₹%,.2f", inv.getSubtotal()),
                        String.format("₹%,.2f", inv.getDiscountAmount()),
                        String.format("₹%,.2f", inv.getTaxAmount()),
                        String.format("₹%,.2f", inv.getGrandTotal()),
                        inv.getPaymentMethod(),
                        inv.getCreatedAt()
                });
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load sales table: " + e.getMessage());
        }
    }
}

