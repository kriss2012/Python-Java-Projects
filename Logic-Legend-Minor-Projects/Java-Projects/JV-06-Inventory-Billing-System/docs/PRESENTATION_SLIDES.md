# Presentation Slides — Inventory and Billing System (JV-06)

---

### Slide 1: Title Slide
- **Project Title**: Point-of-Sale (POS) Inventory and Billing System
- **Project Code**: `JV-06`
- **Domain**: Enterprise Resource Planning (ERP) & Retail Informatics
- **Technology Stack**: Java 21 LTS, Java Swing, JDBC, SQLite, MySQL
- **Presenter**: Engineering Candidate

---

### Slide 2: Problem Statement & Motivation
- **Retail Challenges**:
  - Manual billing leads to pricing errors, inventory discrepancies, and slow checkout lines.
  - Risk of "over-selling" stock across multiple sales registers when stock deduction is non-atomic.
  - Lack of timely reorder notifications resulting in out-of-stock items.
  - Disconnected customer loyalty systems.
- **Proposed Solution**: A robust, ACID-compliant POS checkout and stock management system with automated low-stock warnings, multi-tender payments, and itemized receipt printing.

---

### Slide 3: System Architecture
- **Layer 1: Presentation & POS UI**: Modern Swing terminal (`InventoryFrame`) with barcode search, cart table, and quick checkout controls.
- **Layer 2: Service & Business Logic**: `InventoryService` orchestrating ACID checkout transactions, tax/discount math, and stock logging.
- **Layer 3: Data Access Layer**: `DatabaseManager` providing dual SQLite/MySQL support.
- **Layer 4: Relational Persistence**: 3NF normalized schema for products, customers, invoices, invoice items, and stock logs.

---

### Slide 4: Key Features & Functional Modules
- **Point-of-Sale Checkout**: Rapid barcode item scanning, quantity spin controls, and real-time total computation.
- **Atomic Stock Reservation**: Every item sale locks and updates stock inside a single database transaction.
- **Zero Negative Stock Guarantee**: Sales exceeding available inventory are instantly rejected and rolled back.
- **Statutory GST & Discounts**: Automated 18% GST calculation and percentage/loyalty discount deductions.
- **Customer Loyalty Program**: Automatically earns 1 point per ₹100; redeemable at ₹1 per point.
- **Low-Stock Alerts**: Instant visual badges for products at or below `reorder_level`.
- **Receipt Exporter**: Prints thermal-formatted ASCII receipts with export to disk.

---

### Slide 5: Database Design & Entity Relationships
- **`products`**: `product_code` (UNIQUE), `name`, `cost_price`, `unit_price`, `stock_quantity`, `reorder_level`.
- **`customers`**: `phone` (UNIQUE), `name`, `loyalty_points`.
- **`invoices`**: `invoice_number` (UNIQUE), `subtotal`, `discount_amount`, `tax_amount`, `grand_total`, `payment_method`.
- **`invoice_items`**: Foreign keys to `invoices` and `products`, capturing sold quantity and price.
- **`stock_logs`**: Historical audit log recording every inventory increase, decrease, and final balance.

---

### Slide 6: Financial Calculation Mathematical Engine
- **Subtotal**: $\sum (\text{unit\_price} \times \text{quantity})$
- **Discounts**: $\text{Subtotal} \times \frac{D_{\text{pct}}}{100} + (\text{Redeemed Pts} \times 1.0)$
- **Taxable Base**: $\text{Subtotal} - \text{Discount}$
- **GST (18%)**: $\text{Taxable Base} \times 0.18$
- **Grand Total**: $\text{Taxable Base} + \text{GST}$
- **Points Accrual**: $\lfloor \text{Grand Total} / 100 \rfloor$

---

### Slide 7: Demonstration Walkthrough
1. **Barcode Scan**: Enter `BAR-1001` or search `Mouse`; select quantity and click "Add to Cart".
2. **Customer Loyalty**: Enter customer phone `9876543210` to retrieve name and loyalty points.
3. **Redeem Points**: Check "Redeem Loyalty Points" to apply instant bill reduction.
4. **Checkout**: Select payment method (UPI / Card / Cash) and click "Complete Sale".
5. **Receipt**: View the formatted invoice and verify saved copy in `exports/`.
6. **Stock Replenishment**: Switch to Inventory tab, select low-stock product, and add +20 units.

---

### Slide 8: Automated Verification & Testing
- **Test Runner (`tests.TestRunner`)**: 41 comprehensive assertions across 9 distinct test suites:
  - Product CRUD and barcode retrieval.
  - Stock replenishment and audit logging.
  - Low-stock threshold detection.
  - Transactional checkout and stock deduction.
  - Insufficient stock rejection and rollback.
  - GST (18%) and discount mathematics.
  - Customer loyalty accrual and redemption.
  - Receipt text formatting and file export.
  - Financial turnover summaries.
- **Result**: 41 / 41 assertions passed (0 failures).

---

### Slide 9: Architectural Strengths
- **ACID Compliance**: Full rollback guarantees zero orphaned invoice records or corrupt inventory balances.
- **Zero-Configuration Portability**: Embedded SQLite JDBC driver ensures out-of-the-box execution on standard JDK 21.
- **Production Scalability**: Seamless migration to enterprise MySQL via single environment variable change.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: JV-06 delivers an enterprise-grade POS and inventory system ready for commercial deployment and college evaluation.
- **Future Enhancements**:
  - Integration with hardware USB barcode scanners and receipt ESC/POS printers.
  - Multi-store central warehouse synchronization.
  - UPI QR code dynamic generation on checkout screen.

