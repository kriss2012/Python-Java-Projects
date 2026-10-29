# Viva Voce Examination Questions & Answers — Inventory and Billing System (JV-06)

---

### Q1: How does the system prevent negative stock when two cashiers attempt to sell the last unit of an item simultaneously?
**Answer**: Negative stock is prevented through ACID database transactions and serializable updates:
1. `conn.setAutoCommit(false)` is set at the start of `processCheckout()`.
2. When querying each item in the cart, the system checks whether `product.getStockQuantity() < requestedQuantity`.
3. If insufficient, an `IllegalStateException` is thrown immediately, and `conn.rollback()` restores all table states.
4. When executed against relational databases like MySQL or SQLite with row/table locking, concurrent update queries are serialized, ensuring only the first cashier's transaction commits while the second is rejected with an "Insufficient stock" message.

---

### Q2: Why are discounts applied before calculating GST (18%)?
**Answer**: Under standard Indian Goods and Services Tax (GST) statutory regulations, trade and promotional discounts given to customers at the time of sale reduce the transaction value of the supply. GST is charged only on the net taxable amount ($Value - Discount$):
$$\text{Taxable Value} = \text{Subtotal} - \text{Discount}$$
$$\text{GST} = \text{Taxable Value} \times 0.18$$
Charging tax on the gross amount before discount would unfairly inflate the tax liability of the customer.

---

### Q3: How does the Customer Loyalty Rewards mechanism work mathematically?
**Answer**:
- **Accrual**: For every ₹100 of grand total spent, the customer earns 1 loyalty point:
  $$\text{Points Earned} = \left\lfloor \frac{\text{Grand Total}}{100} \right\rfloor$$
- **Redemption**: Each loyalty point has a cash value of ₹1.00. When redeeming $P$ points, the bill is reduced by ₹$P$, and the customer's loyalty ledger balance is updated:
  $$\text{Balance}_{\text{new}} = \text{Balance}_{\text{old}} - P_{\text{redeemed}} + P_{\text{earned}}$$
All point updates occur inside the atomic checkout transaction.

---

### Q4: What is the purpose of the `stock_logs` audit table?
**Answer**: In retail and inventory systems, tracking discrepancies and shrinkage (loss, theft, damage) requires a tamper-evident audit trail. The `stock_logs` table records every single change to product inventory:
- `change_type`: `"INITIAL_STOCK"`, `"RESTOCK"`, `"SALE"`, `"ADJUSTMENT"`.
- `quantity_changed`: Positive for restock, negative for sales.
- `final_stock`: The exact on-hand inventory immediately following the operation.
- `remarks`: Human or batch notes (e.g. shipment invoices, batch numbers).
This allows auditors to reconstruct inventory levels at any historical date.

---

### Q5: How does the system trigger Low-Stock alerts?
**Answer**: Each product defines a configurable `reorder_level` (default 5 units). The system executes:
```sql
SELECT * FROM products WHERE stock_quantity <= reorder_level ORDER BY stock_quantity ASC;
```
Products matching this condition receive a visual warning badge (`⚠️ LOW STOCK`) in the inventory table, and can be filtered with a single click in the GUI to expedite purchase replenishment.

---

### Q6: How does the application format receipts for standard thermal POS printers?
**Answer**: Thermal printers use fixed monospaced character widths (typically 48 or 72 columns). The method `InventoryService.generateReceiptText()` formats output using fixed-width string format specifiers (e.g., `%-10s | %-26s | %-6d | %,10.2f`), aligning columns, dividing lines, totals, and GST breakdowns into neat ASCII layouts suitable for both raw text export and thermal printer ESC/POS streams.

---

### Q7: Why are batch inserts used for `invoice_items`?
**Answer**: A checkout transaction may contain dozens of line items. Executing individual `PreparedStatement.executeUpdate()` calls incurs repeated network round-trips to the database engine. By calling `ps.addBatch()` in a loop and executing `ps.executeBatch()` once, all item lines are transmitted and inserted in a single round-trip, significantly reducing disk I/O and checkout latency.

---

### Q8: How does the system handle Walk-in customers who do not wish to provide their phone number?
**Answer**: Customer phone registration is completely optional. If the cashier leaves the phone field empty, the system sets `customer_id = NULL` and records `customer_name = "Walk-in Customer"`. The invoice is generated normally, but loyalty points are neither deducted nor accrued.

---

### Q9: What happens if a customer attempts to redeem more loyalty points than they currently possess?
**Answer**: The system applies clamping:
$$\text{Redeemed Points} = \min(\text{Customer Points}, \text{Requested Redemption})$$
Furthermore, the redemption discount cannot exceed the subtotal of the cart. This guarantees that total discounts never exceed the bill value or create negative balances.

---

### Q10: How can this application be deployed with enterprise MySQL instead of SQLite?
**Answer**: The `DatabaseManager` dynamically checks the `DB_TYPE` environment variable. If set to `mysql`, it reads host, port, database, user, and password from environment variables and connects via `jdbc:mysql://...`. The project includes `sql/schema_mysql.sql` featuring InnoDB tables with foreign key constraints (`ON DELETE CASCADE` and `ON DELETE SET NULL`).

---

### Q11: How does the system validate product additions?
**Answer**: `InventoryService.addProduct()` validates that:
1. Product code / barcode is non-null and non-empty.
2. Product name is non-empty.
3. Unit price is $\ge 0$.
4. Initial stock quantity is $\ge 0$.
Violations throw an `IllegalArgumentException` before touching the database.

---

### Q12: Why is the `product_code` column indexed with a UNIQUE constraint?
**Answer**: In retail environments, cashiers scan barcodes rapidly using hardware laser or CCD barcode scanners. A UNIQUE B-Tree index on `product_code` guarantees that lookups execute in $O(\log N)$ logarithmic time (virtually instantaneous even with 100,000 SKUs) and prevents accidental duplicate barcode assignments.

---

### Q13: How does the application support headless Linux servers or command-line testing?
**Answer**: Passing `--cli` or `-c` launches `Main.runCli()`. This full interactive terminal interface allows cashiers and administrators to view products, search barcodes, execute sales checkouts, restock items, register customers, and print daily sales summaries without requiring any graphical display.

---

### Q14: What is the time complexity of the checkout transaction?
**Answer**: Let $M$ be the number of items in the cart.
1. Looking up and locking $M$ items in the database takes $O(M \log N)$ time with indexed primary keys.
2. Calculating totals and discounts takes $O(M)$ time.
3. Batch inserting line items takes $O(M)$ time.
4. Total transaction time complexity is $O(M \log N)$, completing in less than 5 milliseconds on modern SSDs.

---

### Q15: How are rounding errors avoided in decimal currency calculations?
**Answer**: Financial calculations in `InventoryService` round monetary totals to two decimal places:
```java
double grandTotal = Math.round((taxableAmount + taxAmount) * 100.0) / 100.0;
```
In SQL schemas, MySQL uses `DECIMAL(10,2)` to store exact currency values without IEEE-754 floating-point precision drift.

