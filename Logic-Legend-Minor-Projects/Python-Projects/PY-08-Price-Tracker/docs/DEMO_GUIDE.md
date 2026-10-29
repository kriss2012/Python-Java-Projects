# Evaluator Live Demonstration Guide: PY-08 Autonomous Price Tracker

This guide provides a rapid step-by-step walkthrough for project mentors, faculty evaluators, and external examiners to test and verify the PY-08 application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Run Unit Tests | 6 tests pass in < 0.2s (`Ran 6 tests ... OK`) | [ ] |
| 2 | Seed Demo Data via CLI | 4 realistic products inserted into SQLite | [ ] |
| 3 | Automated Price Check | Scrapes prices and triggers price drop alerts | [ ] |
| 4 | CSV Export | Valid CSV generated in `exports/` | [ ] |
| 5 | Launch GUI (Optional) | Tkinter dashboard opens with live table and charts | [ ] |

---

## 2. Command Line Demonstration (Fastest & 100% Deterministic)

### Step 1: Execute Automated Test Suite
```bash
python tests/test_tracker.py
```
**Verification Note**: Look for:
```
Ran 6 tests in 0.099s
OK
```

### Step 2: Seed Demo Data and Check Prices
```bash
python src/main.py --seed --check-all --export --cli
```
**Output Highlights to Observe**:
1. Demo products seeded into `data/price_tracker.db`:
   - Sony WH-1000XM5 Headphones (Target: ₹24,999.00)
   - Keychron K2 Mechanical Keyboard (Target: ₹6,999.00)
   - Dell 27-inch 4K UHD IPS Monitor (Target: ₹28,500.00)
   - Logitech G502 HERO Mouse (Target: ₹3,499.00)
2. Immediate Price Check output showing price comparisons:
   ```
   * Logitech G502 HERO | Old: INR 1,850.00 -> New: INR 1,850.00 (Target: INR 3,499.00)
     🚨 TRIGGERED: [SIMULATION] Email alert generated for 'Logitech G502 HERO' (Price: ₹1850 <= Target: ₹3499).
   ```
3. A clean ASCII table summarizing tracked products and their active status.
4. Export message: `[EXPORT] CSV generated: exports/price_history_export.csv (Success: True)`.

### Step 3: Add a Custom Product via CLI
```bash
python src/main.py --cli --add-url "https://example.com/products/earbuds" --title "ANC Earbuds" --target 2500.0
```
**Verification Note**: Product will be parsed, initialized with mock pricing, stored in SQLite, and printed in the table.

---

## 3. Graphical Interface Demonstration (Tkinter GUI)

### Step 1: Launch Application
```bash
python src/main.py
```

### Step 2: Test Dashboard Functionalities
1. **View Products**: Observe the products displayed in the Treeview with IDs, Titles, Current Prices, Target Prices, and Status (`ACTIVE`).
2. **Select Product**: Click any product row in the table:
   - The right-hand pane updates with the product title, current price, and historical data points.
   - The "Best Time to Buy" verdict (`BUY NOW` / `WAIT FOR DROP` / `FAIR VALUE`) is displayed.
3. **Trigger Manual Check**: Click the **Check Selected** or **Check All Now** button.
   - Observe the loading status and confirmation dialog showing scraped prices and alert triggers.
4. **Pause/Resume Tracking**: Click **Toggle Active/Pause** to pause a product without deleting it.
5. **Export CSV**: Click **Export History to CSV** and inspect the resulting spreadsheet.

---

## 4. Evaluator Viva Questions to Ask the Student
1. *"How does the system ensure it doesn't crash if an online store changes its HTML structure?"*
   - Student should explain the multi-selector fallback hierarchy and regex scanning in `src/scraper_engine.py`.
2. *"Can this system run without internet access?"*
   - Student should show the deterministic offline mock parser in `scraper_engine.py` using `example.com` fixture URLs.
3. *"How does the recommendation algorithm know if it is a good time to buy?"*
   - Student should explain the price average, minimum threshold, and moving historical calculations in `TrackerService.analyze_best_time_to_buy()`.

