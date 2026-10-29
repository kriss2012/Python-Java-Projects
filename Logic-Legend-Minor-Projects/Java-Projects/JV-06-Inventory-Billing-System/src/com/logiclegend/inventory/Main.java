package com.logiclegend.inventory;

import com.logiclegend.inventory.dao.DatabaseManager;
import com.logiclegend.inventory.model.Customer;
import com.logiclegend.inventory.model.Invoice;
import com.logiclegend.inventory.model.InvoiceItem;
import com.logiclegend.inventory.model.Product;
import com.logiclegend.inventory.service.InventoryService;
import com.logiclegend.inventory.ui.InventoryFrame;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Entry point for JV-06 Inventory and Billing System.
 * Supports Swing POS desktop application and full interactive CLI terminal mode.
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        InventoryService service = new InventoryService(dbManager);

        try {
            service.seedDemoData();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding demo inventory data notice: " + e.getMessage());
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
                    InventoryFrame frame = new InventoryFrame(service);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable. Running in terminal CLI mode.");
                runCli(service);
            }
        }
    }

    public static void runCli(InventoryService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-06: INVENTORY & BILLING SYSTEM (CLI)");
        System.out.println("=================================================================");

        while (true) {
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("  POINT-OF-SALE & STOCK MANAGEMENT DASHBOARD");
            System.out.println("-------------------------------------------------------------");
            System.out.println("  1. List All Products in Inventory");
            System.out.println("  2. Search Product by Barcode / Keyword");
            System.out.println("  3. View Low Stock Alerts");
            System.out.println("  4. Process New POS Sale Checkout");
            System.out.println("  5. Restock / Replenish Product (+Quantity)");
            System.out.println("  6. Register New Customer");
            System.out.println("  7. Daily Sales Financial Summary");
            System.out.println("  8. View Recent Invoices");
            System.out.println("  0. Exit / Shutdown");
            System.out.print("Select an option (0-8): ");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (choice) {
                case "1":
                    listAllProductsCli(service);
                    break;
                case "2":
                    searchProductsCli(service, scanner);
                    break;
                case "3":
                    viewLowStockCli(service);
                    break;
                case "4":
                    processSaleCli(service, scanner);
                    break;
                case "5":
                    restockCli(service, scanner);
                    break;
                case "6":
                    registerCustomerCli(service, scanner);
                    break;
                case "7":
                    viewSalesSummaryCli(service);
                    break;
                case "8":
                    viewRecentInvoicesCli(service, scanner);
                    break;
                case "0":
                    System.out.println("[INFO] Exiting Inventory & Billing System. Goodbye!");
                    return;
                default:
                    System.out.println("[ERROR] Invalid choice. Please choose 0 to 8.");
            }
        }
    }

    private static void listAllProductsCli(InventoryService service) {
        try {
            List<Product> products = service.getAllProducts();
            printProductTable(products);
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch products: " + e.getMessage());
        }
    }

    private static void searchProductsCli(InventoryService service, Scanner scanner) {
        System.out.print("Enter barcode code or product name query: ");
        String query = scanner.nextLine().trim();
        try {
            List<Product> matches = service.searchProducts(query);
            printProductTable(matches);
        } catch (SQLException e) {
            System.out.println("[ERROR] Search error: " + e.getMessage());
        }
    }

    private static void viewLowStockCli(InventoryService service) {
        try {
            List<Product> lowStock = service.getLowStockProducts();
            System.out.println("\n[LOW STOCK REORDER WARNINGS]");
            if (lowStock.isEmpty()) {
                System.out.println("  All products have healthy inventory levels.");
            } else {
                printProductTable(lowStock);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch low stock alerts: " + e.getMessage());
        }
    }

    private static void processSaleCli(InventoryService service, Scanner scanner) {
        try {
            List<InvoiceItem> cart = new ArrayList<>();
            System.out.println("\n--- NEW POS SALE CHECKOUT ---");

            while (true) {
                System.out.print("Enter Barcode / Product Code (or 'DONE' to finish): ");
                String code = scanner.nextLine().trim();
                if ("DONE".equalsIgnoreCase(code) || code.isEmpty()) break;

                Product p = service.getProductByCode(code);
                if (p == null) {
                    System.out.println("[ERROR] Product code '" + code + "' not found.");
                    continue;
                }

                System.out.printf("Selected: %s (Available: %d, Price: ₹%,.2f)%n", p.getName(), p.getStockQuantity(), p.getUnitPrice());
                System.out.print("Enter Quantity to Buy: ");
                int qty = Integer.parseInt(scanner.nextLine().trim());
                if (qty <= 0) continue;

                if (p.getStockQuantity() < qty) {
                    System.out.println("[ERROR] Cannot add: requested quantity (" + qty + ") exceeds available stock (" + p.getStockQuantity() + ")");
                    continue;
                }

                cart.add(new InvoiceItem(p.getId(), p.getProductCode(), p.getName(), p.getUnitPrice(), qty));
                System.out.println("[ADDED] " + qty + " x " + p.getName() + " added to cart.");
            }

            if (cart.isEmpty()) {
                System.out.println("[INFO] Cart empty. Transaction cancelled.");
                return;
            }

            System.out.print("Customer Phone (or leave blank for Walk-in): ");
            String phone = scanner.nextLine().trim();
            Customer cust = !phone.isEmpty() ? service.getCustomerByPhone(phone) : null;
            Integer custId = cust != null ? cust.getId() : null;
            String custName = cust != null ? cust.getName() : "Walk-in Customer";

            System.out.print("Discount Percentage (0 to 50%): ");
            String discStr = scanner.nextLine().trim();
            double discPct = discStr.isEmpty() ? 0.0 : Double.parseDouble(discStr);

            int redeemPts = 0;
            if (cust != null && cust.getLoyaltyPoints() > 0) {
                System.out.printf("Customer has %d loyalty points. Redeem points? (0 to %d): ", cust.getLoyaltyPoints(), cust.getLoyaltyPoints());
                String ptsStr = scanner.nextLine().trim();
                if (!ptsStr.isEmpty()) redeemPts = Integer.parseInt(ptsStr);
            }

            System.out.print("Payment Method (CASH, CARD, UPI): ");
            String payMode = scanner.nextLine().trim();
            if (payMode.isEmpty()) payMode = "CASH";

            Invoice inv = service.processCheckout(custId, custName, cart, discPct, 0.18, payMode, redeemPts);
            File file = service.exportReceiptToFile(inv, "exports");

            System.out.println("\n" + service.generateReceiptText(inv));
            System.out.println("[SUCCESS] Receipt exported to: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("[ERROR] Checkout transaction failed: " + e.getMessage());
        }
    }

    private static void restockCli(InventoryService service, Scanner scanner) {
        try {
            System.out.print("Enter Barcode / Product Code to Restock: ");
            String code = scanner.nextLine().trim();
            Product p = service.getProductByCode(code);
            if (p == null) {
                System.out.println("[ERROR] Product code '" + code + "' not found.");
                return;
            }

            System.out.printf("Product: %s (Current Stock: %d)%n", p.getName(), p.getStockQuantity());
            System.out.print("Quantity to Add (+Qty): ");
            int addQty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Remarks / Shipment Batch: ");
            String remarks = scanner.nextLine().trim();

            service.restockProduct(p.getId(), addQty, remarks);
            Product updated = service.getProductById(p.getId());
            System.out.println("[SUCCESS] Stock updated. New Stock Level: " + updated.getStockQuantity());
        } catch (Exception e) {
            System.out.println("[ERROR] Restock failed: " + e.getMessage());
        }
    }

    private static void registerCustomerCli(InventoryService service, Scanner scanner) {
        try {
            System.out.print("Enter Customer Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Customer Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter Email Address: ");
            String email = scanner.nextLine().trim();

            Customer c = service.registerCustomer(name, phone, email);
            System.out.println("[SUCCESS] Registered Customer ID #" + c.getId() + " - " + c.getName());
        } catch (Exception e) {
            System.out.println("[ERROR] Registration failed: " + e.getMessage());
        }
    }

    private static void viewSalesSummaryCli(InventoryService service) {
        try {
            Map<String, Object> summary = service.getDailySalesSummary();
            System.out.println("\n=============================================================");
            System.out.println("  FINANCIAL & SALES SUMMARY");
            System.out.println("=============================================================");
            System.out.printf("  Total Invoices Issued: %d%n", summary.get("totalInvoices"));
            System.out.printf("  Total Gross Revenue:   ₹%,.2f%n", summary.get("totalRevenue"));
            System.out.printf("  Total Discounts Given: ₹%,.2f%n", summary.get("totalDiscounts"));
            System.out.println("=============================================================");
        } catch (SQLException e) {
            System.out.println("[ERROR] Summary failed: " + e.getMessage());
        }
    }

    private static void viewRecentInvoicesCli(InventoryService service, Scanner scanner) {
        try {
            List<Invoice> list = service.getAllInvoices(10);
            System.out.println("\n-------------------------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-20s | %-20s | %-12s | %-10s | %-16s%n",
                    "ID", "Invoice Number", "Customer Name", "Grand Total", "Payment", "Date");
            System.out.println("-------------------------------------------------------------------------------------------------");
            for (Invoice inv : list) {
                System.out.printf("%-4d | %-20s | %-20s | ₹%,10.2f | %-10s | %-16s%n",
                        inv.getId(), inv.getInvoiceNumber(), truncate(inv.getCustomerName(), 20),
                        inv.getGrandTotal(), inv.getPaymentMethod(), inv.getCreatedAt());
            }
            System.out.println("-------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch invoices: " + e.getMessage());
        }
    }

    private static void printProductTable(List<Product> products) {
        System.out.println("\n---------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-12s | %-32s | %-14s | %-10s | %-6s | %-8s | %-12s%n",
                "ID", "Barcode", "Product Name", "Category", "Price", "Stock", "Reorder", "Status");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        for (Product p : products) {
            System.out.printf("%-4d | %-12s | %-32s | %-14s | ₹%,8.2f | %-6d | %-8d | %-12s%n",
                    p.getId(), p.getProductCode(), truncate(p.getName(), 32),
                    truncate(p.getCategory(), 14), p.getUnitPrice(),
                    p.getStockQuantity(), p.getReorderLevel(),
                    p.isLowStock() ? "⚠️ LOW STOCK" : "IN STOCK");
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------");
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

