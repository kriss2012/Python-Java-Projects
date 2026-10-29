# Logic Legend — JV-06: Inventory and Billing System

A retail and wholesale Point-of-Sale (POS) and inventory replenishment management system engineered in Java 21, Swing, JDBC, SQLite, and MySQL. The platform delivers instant barcode search and cart management, atomic inventory decrementing with negative stock prevention, automated GST and discount calculation, customer loyalty point accrual and redemption, low-stock threshold monitoring, printable and exportable itemized receipts, and daily sales summaries.

---

## 1. Project Title and Identifier
- **Project Name**: Inventory and Billing System
- **Project Code**: `JV-06`
- **Application Category**: Enterprise Resource Planning (ERP), Retail Informatics & Point-of-Sale (POS)
- **Target Audience**: Retail store operators, supermarket cashiers, inventory managers, computer science students, and faculty evaluators.

---

## 2. Executive Summary
Retail outlets face daily stock-out risks, billing calculation discrepancies, inventory shrinkages, and lost revenue caused by delayed checkout terminals. The **Inventory and Billing System** provides a responsive point-of-sale terminal backed by ACID-compliant relational databases. It prevents over-selling through atomic inventory reservation, calculates tiered discounts and statutory GST, tracks customer loyalty rewards, triggers automated low-stock reorder alerts, and exports formatted invoice receipts.

---

## 3. Key Features
- **Point of Sale (POS) Checkout Terminal**:
  - Rapid item lookup by barcode code or name with keyboard scanner emulation.
  - Interactive shopping cart with quantity adjustment, item removal, and live subtotaling.
  - Multi-payment tender recording (CASH, CARD, UPI, NET_BANKING).
- **Atomic Stock Reservation & Prevention of Over-Selling**:
  - Enforces database transactions (`conn.setAutoCommit(false)`): checks available inventory prior to deduction.
  - Completely prevents negative stock levels; transactions are aborted and rolled back if requested quantity exceeds availability.
- **Statutory GST & Promotional Discount Computation**:
  - Configurable goods and services tax (default 18% GST).
  - Store-wide percentage discounts and loyalty point redemption discounts applied transparently.
- **Customer Loyalty Rewards Program**:
  - Tracks customer phone numbers and historical loyalty balances.
  - Awards 1 reward point per ₹100 of gross invoice total.
  - Supports instant point redemption (₹1.00 per point) with automatic ledger updates.
- **Automated Low-Stock Threshold Alerts**:
  - Evaluates real-time inventory against configured `reorder_level`.
  - Visual warning badges for products requiring immediate purchase requisition.
- **Formatted Itemized Receipts & File Export**:
  - Formatted ASCII receipts suitable for thermal POS receipt printers.
  - Automatic export to `exports/INV-XXXXXXXX.txt` with invoice number, itemized totals, GST, and loyalty balances.
- **Daily Sales Financial Analytics**:
  - Aggregates daily invoices count, gross sales revenue, and total discounts surrendered.
- **Dual Interface Modes**: Modern multi-tab Swing GUI alongside an interactive terminal CLI mode (`--cli`).

---

## 4. System Architecture
1. **Presentation Layer (`ui/InventoryFrame.java`, `Main.java`)**: POS terminal, inventory master editor, and terminal console.
2. **Business Service Layer (`service/InventoryService.java`)**: Transactional checkout coordinator, discount/tax calculations, and stock log recorder.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: JDBC connectivity and auto-migration for SQLite and MySQL.
4. **Relational Persistence Layer (`model/`, SQLite / MySQL)**: Tables for `products`, `customers`, `invoices`, `invoice_items`, and `stock_logs`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **GUI Framework**: Java Swing & AWT (Standard JDK SE)
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Bundled zero-dependency `sqlite-jdbc.jar`)
- **Production Database**: MySQL 8.x (Script in `sql/schema_mysql.sql`)
- **Build Tool**: Apache Maven (`pom.xml`) or standard JDK compiler

---

## 6. Directory Layout
```
JV-06-Inventory-Billing-System/
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── lib/
│   └── sqlite-jdbc.jar
├── sql/
│   ├── schema_sqlite.sql
│   └── schema_mysql.sql
├── src/
│   └── com/logiclegend/inventory/
│       ├── Main.java             # Dual-mode desktop and CLI runner
│       ├── dao/
│       │   └── DatabaseManager.java # Connection & schema bootstrap
│       ├── model/
│       │   ├── Product.java      # Product & stock entity
│       │   ├── Customer.java     # Customer & loyalty balance
│       │   ├── Invoice.java      # Sales invoice header
│       │   └── InvoiceItem.java  # Itemized line detail
│       ├── service/
│       │   └── InventoryService.java # Transactional POS logic
│       └── ui/
│           └── InventoryFrame.java # Swing POS & Inventory GUI
└── tests/
    └── TestRunner.java           # Comprehensive 41-assertion test suite
```

---

## 7. Compilation and Setup Guide

### Direct Compilation with Standard JDK
```powershell
cd "Logic-Legend-Minor-Projects\Java-Projects\JV-06-Inventory-Billing-System"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\inventory\model\*.java src\com\logiclegend\inventory\dao\*.java src\com\logiclegend\inventory\service\*.java src\com\logiclegend\inventory\ui\*.java src\com\logiclegend\inventory\Main.java tests\TestRunner.java
```

---

## 8. Running the Application

### Mode 1: Graphical Desktop Application (Default)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.inventory.Main
```

### Mode 2: Interactive Terminal Console
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.inventory.Main --cli
```

---

## 9. Automated Testing
Run the automated test suite executing 41 comprehensive assertions:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Sample test output:
```
=================================================================
  LOGIC LEGEND JV-06: AUTOMATED TEST SUITE EXECUTION
=================================================================
--- Test Suite 1: Product Creation and Retrieval ---
  [PASS] Product created with non-zero ID
  [PASS] Barcode code matches
  [PASS] Selling price matches
  [PASS] Stock quantity matches
...
=================================================================
  TOTAL ASSERTIONS: 41 | PASSED: 41 | FAILED: 0
=================================================================
```

---

## 10. Financial Billing Mathematics
$$\text{Subtotal} = \sum_{i=1}^{M} (\text{unit\_price}_i \times \text{quantity}_i)$$
$$\text{Discount} = \text{Subtotal} \times \left(\frac{\text{Discount \%}}{100}\right) + (\text{Redeemed Points} \times 1.0)$$
$$\text{Taxable Amount} = \max(0, \text{Subtotal} - \text{Discount})$$
$$\text{GST Tax} = \text{Taxable Amount} \times 0.18$$
$$\text{Grand Total} = \text{Taxable Amount} + \text{GST Tax}$$
$$\text{Points Earned} = \left\lfloor \frac{\text{Grand Total}}{100} \right\rfloor$$

---

## 11. License
Educational Project Suite — Logic Legend Minor Projects. Open source under Apache 2.0 license.

