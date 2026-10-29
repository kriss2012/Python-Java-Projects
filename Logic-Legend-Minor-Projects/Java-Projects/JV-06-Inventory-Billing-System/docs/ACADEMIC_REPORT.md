# Academic Project Report — Inventory and Billing System (JV-06)

**Project Identifier**: `JV-06`  
**Domain**: Enterprise Resource Planning (ERP) & Retail Informatics  
**Academic Level**: Bachelor of Technology / Master of Computer Applications (Minor Project)

---

## 1. Abstract
The **Inventory and Billing System** is an integrated Point-of-Sale (POS) and inventory replenishment platform engineered in Java 21. By combining barcode search, atomic transactional stock reservation, tiered discount and Goods and Services Tax (GST) computation, and customer loyalty rewards, the system guarantees that sales cannot exceed on-hand inventory levels. The platform enforces strict ACID guarantees across concurrent sales, generates itemized ASCII receipts, tracks stock history logs, and produces real-time financial turnover metrics.

---

## 2. System Architecture

```mermaid
flowchart TD
    Cashier[Cashier / Store Manager] -->|Barcode Scan & Input| UI[InventoryFrame / Swing GUI]
    CLIUser[Terminal / Headless Operator] -->|CLI Menu Options| Main[Main Controller]
    
    subgraph Presentation & Controller Layer
        UI --> Service[InventoryService]
        Main --> Service
    end
    
    subgraph Business Service Layer
        Service --> TransactionCoordinator[Transactional POS Engine]
        Service --> TaxDiscountEngine[Tax & Discount Engine]
        Service --> LoyaltyEngine[Loyalty Points Calculator]
        Service --> ReceiptGenerator[Thermal Receipt Formatter]
    end
    
    subgraph Data Access Layer
        TransactionCoordinator --> DatabaseManager[DatabaseManager JDBC]
        DatabaseManager --> SQLite[(SQLite Embedded DB)]
        DatabaseManager -.-> MySQL[(MySQL Enterprise DB)]
    end
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    CUSTOMERS ||--o{ INVOICES : places
    INVOICES ||--|{ INVOICE_ITEMS : contains
    PRODUCTS ||--o{ INVOICE_ITEMS : referenced_in
    PRODUCTS ||--o{ STOCK_LOGS : tracked_in

    PRODUCTS {
        int id PK
        string product_code UK
        string name
        string category
        double cost_price
        double unit_price
        int stock_quantity
        int reorder_level
        datetime created_at
    }

    CUSTOMERS {
        int id PK
        string name
        string phone UK
        string email
        int loyalty_points
        datetime created_at
    }

    INVOICES {
        int id PK
        string invoice_number UK
        int customer_id FK
        string customer_name
        double subtotal
        double discount_amount
        double tax_amount
        double grand_total
        string payment_method
        int loyalty_points_earned
        int loyalty_points_redeemed
        datetime created_at
    }

    INVOICE_ITEMS {
        int id PK
        int invoice_id FK
        int product_id FK
        string product_code
        string product_name
        double unit_price
        int quantity
        double total_price
    }

    STOCK_LOGS {
        int id PK
        int product_id FK
        string change_type
        int quantity_changed
        int final_stock
        string remarks
        datetime created_at
    }
```

---

## 4. Data Flow Diagram (DFD)

### Level 0: Context Diagram
```mermaid
flowchart LR
    Cashier[Cashier / Operator] -->|1. Scan Barcode & Submit Cart| System((JV-06 Inventory & Billing System))
    System -->|2. Return Itemized Receipt & Low Stock Alerts| Cashier
    Customer[Customer] -->|3. Provide Phone Number & Payment| System
    System -->|4. Update Loyalty Ledger & Issue Invoice| Customer
```

### Level 1: Subsystem Decomposition
```mermaid
flowchart TD
    P1[1.0 Product Catalog & Barcode Lookup] -->|Stock Availability| P2[2.0 Cart Management]
    P2 -->|Cart Items| P3[3.0 Transactional Checkout & Stock Deduction]
    P3 -->|Write Changes| DB[(Relational Persistence)]
    P3 --> P4[4.0 Tax & Discount Accounting]
    P4 --> P5[5.0 Receipt Formatter & Export]
    DB --> P6[6.0 Low Stock Alert & Reporting Engine]
```

---

## 5. Sequence Diagram: POS Transactional Checkout

```mermaid
sequenceDiagram
    autonumber
    actor Cashier
    participant UI as InventoryFrame GUI
    participant Service as InventoryService
    participant DB as Relational Database

    Cashier->>UI: Enter Barcode & Quantity (Add to Cart)
    UI->>Service: getProductByCode(code)
    Service->>DB: SELECT * FROM products WHERE code = ?
    DB-->>Service: Product Entity
    Service-->>UI: Display in Cart Table & Recalculate Subtotal
    
    Cashier->>UI: Select Payment Mode & Click 'Complete Sale'
    UI->>Service: processCheckout(customerId, cartItems, discPct, taxRate, payMode, redeemPts)
    
    Service->>DB: BEGIN TRANSACTION (conn.setAutoCommit(false))
    
    loop For each item in cart
        Service->>DB: SELECT stock_quantity FROM products WHERE id = ?
        DB-->>Service: Current Stock
        alt stock_quantity < requested_quantity
            Service-->>DB: ROLLBACK TRANSACTION
            Service-->>UI: Throw InsufficientStockException
        else stock sufficient
            Service-->>DB: UPDATE products SET stock_quantity = stock_quantity - ?
            Service-->>DB: INSERT INTO stock_logs VALUES (...)
        end
    end
    
    Note over Service: Calculate Subtotal, Discounts, GST (18%), Grand Total, Loyalty Points
    
    Service->>DB: INSERT INTO invoices VALUES (...)
    Service->>DB: INSERT INTO invoice_items VALUES (...) (Batch)
    Service->>DB: UPDATE customers SET loyalty_points = loyalty_points - redeemed + earned
    
    Service->>DB: COMMIT TRANSACTION
    DB-->>Service: Success Confirmation
    Service->>Service: Generate Receipt Text & Export to exports/INV-XXXX.txt
    Service-->>UI: Return Invoice Entity
    UI-->>Cashier: Display Formatted Receipt Modal & Refresh Tables
```

---

## 6. Financial Formulations & Business Rules

### Itemized Calculations
$$\text{Line Total}_i = \text{unit\_price}_i \times \text{quantity}_i$$
$$\text{Subtotal} = \sum_{i=1}^{M} \text{Line Total}_i$$

### Discount Compounding
$$\text{Promotional Discount} = \text{Subtotal} \times \left(\frac{D_{\text{pct}}}{100}\right)$$
$$\text{Loyalty Discount} = P_{\text{redeem}} \times 1.0$$
$$\text{Total Discount} = \min(\text{Subtotal}, \text{Promotional Discount} + \text{Loyalty Discount})$$

### Tax & Final Payable
$$\text{Taxable Amount} = \text{Subtotal} - \text{Total Discount}$$
$$\text{GST Amount} = \text{Taxable Amount} \times 0.18$$
$$\text{Grand Total} = \text{Taxable Amount} + \text{GST Amount}$$

---

## 7. Verification and Test Results
The automated test runner (`tests/TestRunner.java`) executed 41 assertions across 9 test suites:
- Product creation, barcode retrieval, and pricing accuracy.
- Stock replenishment and auditing of `stock_logs`.
- Real-time low stock threshold alerts (`stock_quantity <= reorder_level`).
- Atomic stock reservation and deduction upon checkout.
- Abort and rollback of sales attempting to exceed available inventory.
- Mathematical precision of discounts and 18% GST computations.
- Loyalty point accrual (+1 pt per ₹100) and point redemption (₹1/pt).
- Receipt formatting and filesystem export verification.
- Daily financial summaries (turnover, discounts, invoice count).
- **Result: 41 / 41 assertions passed (100% success rate)**.

