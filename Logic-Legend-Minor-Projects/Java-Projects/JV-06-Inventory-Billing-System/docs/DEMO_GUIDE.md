# Evaluator Demonstration Guide — Inventory and Billing System (JV-06)

This step-by-step walkthrough is designed for evaluators and examiners to test all retail checkout, inventory replenishment, and accounting capabilities of the **Inventory and Billing System**.

---

## 1. Quick Launch Verification
1. Open PowerShell / Command Prompt.
2. Navigate to project root:
   ```powershell
   cd "Logic-Legend-Minor-Projects\Java-Projects\JV-06-Inventory-Billing-System"
   ```
3. Run the automated test suite to confirm zero defects:
   ```powershell
   java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
   ```
   **Expected Outcome**: 41 / 41 assertions PASS in under 1 second.

---

## 2. Desktop POS Demonstration (Graphical Evaluation Path)

### Step 1: Launch the Application
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.inventory.Main
```
- The application will launch with title `LogicLegend — Point of Sale (POS) & Inventory Billing System`.
- A 3-tabbed interface will appear:
  - `🛒 POS Billing Checkout`
  - `📦 Inventory & Stock Control`
  - `📊 Sales Invoices & Reports`

### Step 2: Test Low-Stock Reorder Alerts
1. Click the **"📦 Inventory & Stock Control"** tab.
2. Notice the pre-seeded products table:
   - `BAR-1005` (*Boat BassHeads 100 Earphones*, Stock: 4, Reorder: 10) shows `⚠️ LOW STOCK`.
   - `BAR-1006` (*HP 108W Laser Printer Toner*, Stock: 3, Reorder: 5) shows `⚠️ LOW STOCK`.
3. Check **"Show Low-Stock Alerts Only"** and verify that only low-stock items remain visible.
4. Uncheck the box to return to the full catalog.

### Step 3: Test Point of Sale (POS) Checkout
1. Switch back to the **"🛒 POS Billing Checkout"** tab.
2. In the **"Barcode / Item Code"** field, type `BAR-1001` (or click in the box and type `Logitech`).
3. Set Quantity to `2`. Click **"Add to Cart"** (or press Enter).
   - Observe the item added to the shopping cart table:
     - 2 x Logitech Mouse @ ₹799.00 = ₹1,598.00.
4. Add another product: type `BAR-1004`, set Quantity to `3`, and click **"Add to Cart"**.
   - 3 x Gel Pen Pack @ ₹100.00 = ₹300.00.
5. Review the live calculations in the right panel:
   - **Subtotal**: `₹1,898.00`
   - **GST (18%)**: `₹341.64`
   - **GRAND TOTAL**: `₹2,239.64`

### Step 4: Test Customer Loyalty Program
1. In the **"Customer Phone"** field, enter `9876543210` and click **"Find"** (or press Enter).
2. The label updates to: `Customer: Rohan Deshmukh (0 pts)`.
3. Enter `5` in the **"Store Discount %"** field and press Enter.
   - Observe discount of ₹94.90 applied, taxable reduced to ₹1,803.10, and grand total recalculated to `₹2,127.66`.

### Step 5: Complete Sale & Print Receipt
1. In **"Payment Mode"**, select `UPI` (or `CARD`).
2. Click **"Complete Sale & Print Receipt"**.
3. A formatted receipt modal dialog will appear displaying:
   - Header: *LOGIC LEGEND RETAIL & WHOLESALE HUB*
   - Invoice Number: `INV-2026XXXX-XXXX`
   - Customer: `Rohan Deshmukh`
   - Itemized lines with quantities and totals.
   - Subtotal, Discount, 18% GST, and Grand Total.
   - Loyalty points earned: `+21` points.
4. Click **OK** to close the receipt modal.
5. Verify that the file was exported to `exports/INV-2026XXXX-XXXX.txt`.

### Step 6: Verify Stock Deduction
1. Switch to the **"📦 Inventory & Stock Control"** tab.
2. Check the row for `BAR-1001` (Logitech Mouse):
   - Notice that stock has automatically decremented from `45` to `43`!
3. Check `BAR-1004` (Gel Pen Pack):
   - Decremented from `120` to `117`!

### Step 7: Test Negative Stock Prevention
1. Return to the **"🛒 POS Billing Checkout"** tab.
2. Enter `BAR-1006` (Printer Toner, only 3 in stock).
3. Set Quantity to `10`.
4. Click **"Add to Cart"**.
5. Observe the error message dialog:
   `Insufficient stock for 'HP 108W Laser Printer Toner'! Available: 3, Requested: 10`
   The system blocks the addition, preserving stock integrity!

### Step 8: Test Stock Replenishment
1. Switch to the **"📦 Inventory & Stock Control"** tab.
2. Select the `BAR-1006` row.
3. Click **"Replenish Stock (+Qty)"**.
4. Enter `25` in the dialog and click OK.
5. Verify the stock quantity immediately updates from `3` to `28`, and the low stock warning disappears!

### Step 9: View Sales Invoices & Daily Analytics
1. Switch to the **"📊 Sales Invoices & Reports"** tab.
2. Check the top summary banner:
   - `Sales Summary: X Invoices | Revenue: ₹X,XXX.XX | Discounts: ₹XX.XX`.
3. Select the invoice row you just created and click **"View Selected Invoice Receipt"**.
4. The complete itemized receipt is re-displayed from the persistent database!

---

## 3. Terminal CLI Mode Demonstration (Headless Environment)
Run the application with `--cli`:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.inventory.Main --cli
```
- Select Option `1` to view all products in ASCII tables.
- Select Option `3` to inspect low-stock warnings.
- Select Option `4` to run an interactive terminal checkout sale and generate printed receipts.
- Select Option `7` to view financial revenue summaries.

