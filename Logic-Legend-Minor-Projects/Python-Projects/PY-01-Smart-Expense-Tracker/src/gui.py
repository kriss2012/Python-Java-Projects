"""
Logic Legend - PY-01 Smart Expense Tracker with Analytics
Desktop Graphical User Interface (Tkinter & Canvas)
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from datetime import datetime

class ExpenseTrackerGUI:
    def __init__(self, root, service):
        self.root = root
        self.service = service
        self.root.title("Logic Legend — PY-01 Smart Expense Tracker")
        self.root.geometry("1020x720")
        self.root.minsize(850, 600)

        # Apply Modern Styling
        self.style = ttk.Style()
        self.style.theme_use("clam")
        self._configure_styles()

        # Build Main Layout
        self._build_header()
        self._build_notebook()

        # Initial Data Refresh
        self.refresh_all_views()

    def _configure_styles(self):
        self.style.configure("TNotebook", background="#F4F6F9")
        self.style.configure("TNotebook.Tab", font=("Segoe UI", 10, "bold"), padding=[15, 6])
        self.style.configure("Treeview.Heading", font=("Segoe UI", 9, "bold"), background="#E2E8F0")
        self.style.configure("Treeview", font=("Segoe UI", 9), rowheight=26)
        self.style.configure("Primary.TButton", font=("Segoe UI", 9, "bold"), background="#2563EB", foreground="white")
        self.style.map("Primary.TButton", background=[("active", "#1D4ED8")])
        self.style.configure("Success.TButton", font=("Segoe UI", 9, "bold"), background="#10B981", foreground="white")
        self.style.configure("Danger.TButton", font=("Segoe UI", 9, "bold"), background="#EF4444", foreground="white")

    def _build_header(self):
        header_frame = tk.Frame(self.root, bg="#1E293B", height=60)
        header_frame.pack(fill="x", side="top")

        title_label = tk.Label(
            header_frame,
            text="LOGIC LEGEND  |  Smart Expense Tracker & Analytics",
            font=("Segoe UI", 14, "bold"),
            fg="#F8FAFC",
            bg="#1E293B"
        )
        title_label.pack(side="left", padx=20, pady=12)

        subtitle = tk.Label(
            header_frame,
            text="Personal Finance Decision Support System",
            font=("Segoe UI", 9, "italic"),
            fg="#94A3B8",
            bg="#1E293B"
        )
        subtitle.pack(side="right", padx=20, pady=15)

    def _build_notebook(self):
        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(fill="both", expand=True, padx=10, pady=10)

        # Tabs
        self.tab_dashboard = ttk.Frame(self.notebook)
        self.tab_transactions = ttk.Frame(self.notebook)
        self.tab_budgets = ttk.Frame(self.notebook)
        self.tab_ocr = ttk.Frame(self.notebook)

        self.notebook.add(self.tab_dashboard, text="📊 Dashboard & Analytics")
        self.notebook.add(self.tab_transactions, text="💳 Transactions")
        self.notebook.add(self.tab_budgets, text="🎯 Budgets & Alerts")
        self.notebook.add(self.tab_ocr, text="🧾 Receipt Scanner (OCR)")

        self._build_dashboard_tab()
        self._build_transactions_tab()
        self._build_budgets_tab()
        self._build_ocr_tab()

    # ------------------ TAB 1: DASHBOARD & ANALYTICS ------------------
    def _build_dashboard_tab(self):
        # KPI Cards container
        kpi_frame = tk.Frame(self.tab_dashboard, bg="#F4F6F9")
        kpi_frame.pack(fill="x", padx=15, pady=15)

        self.card_income = self._create_kpi_card(kpi_frame, "Total Income", "₹ 0.00", "#10B981")
        self.card_income.pack(side="left", fill="both", expand=True, padx=5)

        self.card_expense = self._create_kpi_card(kpi_frame, "Total Expenses", "₹ 0.00", "#EF4444")
        self.card_expense.pack(side="left", fill="both", expand=True, padx=5)

        self.card_balance = self._create_kpi_card(kpi_frame, "Net Balance", "₹ 0.00", "#3B82F6")
        self.card_balance.pack(side="left", fill="both", expand=True, padx=5)

        self.card_savings = self._create_kpi_card(kpi_frame, "Savings Rate", "0.0 %", "#8B5CF6")
        self.card_savings.pack(side="left", fill="both", expand=True, padx=5)

        # Budget Alert Banner
        self.alert_banner = tk.Label(
            self.tab_dashboard,
            text="Checking budget limits...",
            font=("Segoe UI", 10, "bold"),
            bg="#FEF3C7",
            fg="#92400E",
            relief="solid",
            bd=1,
            pady=8
        )
        self.alert_banner.pack(fill="x", padx=20, pady=(0, 10))

        # Chart Visualizer Frame
        chart_container = ttk.LabelFrame(self.tab_dashboard, text="Category-Wise Spending Breakdown", padding=10)
        chart_container.pack(fill="both", expand=True, padx=20, pady=(0, 15))

        self.chart_canvas = tk.Canvas(chart_container, bg="white", highlightthickness=0)
        self.chart_canvas.pack(fill="both", expand=True)

    def _create_kpi_card(self, parent, title, initial_val, color):
        frame = tk.Frame(parent, bg="white", relief="ridge", bd=1, padx=15, pady=12)
        lbl_title = tk.Label(frame, text=title, font=("Segoe UI", 9), fg="#64748B", bg="white")
        lbl_title.pack(anchor="w")
        lbl_val = tk.Label(frame, text=initial_val, font=("Segoe UI", 15, "bold"), fg=color, bg="white")
        lbl_val.pack(anchor="w", pady=(5, 0))
        frame.val_label = lbl_val
        return frame

    def _draw_category_bars(self):
        self.chart_canvas.delete("all")
        breakdown = self.service.get_category_breakdown("expense")
        w = self.chart_canvas.winfo_width()
        h = self.chart_canvas.winfo_height()

        if w < 100 or h < 100:
            w, h = 750, 320

        if not breakdown:
            self.chart_canvas.create_text(w / 2, h / 2, text="No expense records available for visualization.", font=("Segoe UI", 11), fill="#94A3B8")
            return

        max_val = max(item["total"] for item in breakdown)
        if max_val == 0:
            max_val = 1

        y_offset = 25
        bar_height = 24
        gap = 14
        colors = ["#EF4444", "#F59E0B", "#10B981", "#3B82F6", "#8B5CF6", "#EC4899", "#6366F1", "#14B8A6"]

        for idx, item in enumerate(breakdown[:8]):
            cat = item["category"]
            total = item["total"]
            bar_w = (total / max_val) * (w - 300)
            color = colors[idx % len(colors)]

            # Category text
            self.chart_canvas.create_text(20, y_offset + 12, text=cat[:22], anchor="w", font=("Segoe UI", 9, "bold"), fill="#1E293B")
            # Bar rectangle
            self.chart_canvas.create_rectangle(180, y_offset, 180 + bar_w, y_offset + bar_height, fill=color, outline="")
            # Amount label
            self.chart_canvas.create_text(190 + bar_w, y_offset + 12, text=f"₹ {total:,.2f}", anchor="w", font=("Segoe UI", 9), fill="#475569")

            y_offset += bar_height + gap

    # ------------------ TAB 2: TRANSACTIONS ------------------
    def _build_transactions_tab(self):
        # Top Action Bar (Add Form + Filters)
        action_box = ttk.LabelFrame(self.tab_transactions, text="Transaction Operations & Filters", padding=10)
        action_box.pack(fill="x", padx=15, pady=10)

        # Row 1: Quick Add
        row1 = ttk.Frame(action_box)
        row1.pack(fill="x", pady=2)

        ttk.Label(row1, text="Type:").pack(side="left", padx=5)
        self.var_type = tk.StringVar(value="expense")
        self.combo_type = ttk.Combobox(row1, textvariable=self.var_type, values=["expense", "income"], state="readonly", width=8)
        self.combo_type.pack(side="left", padx=5)

        ttk.Label(row1, text="Amount:").pack(side="left", padx=5)
        self.entry_amount = ttk.Entry(row1, width=10)
        self.entry_amount.pack(side="left", padx=5)

        ttk.Label(row1, text="Category:").pack(side="left", padx=5)
        self.var_category = tk.StringVar()
        self.combo_category = ttk.Combobox(row1, textvariable=self.var_category, width=18)
        self.combo_category.pack(side="left", padx=5)

        ttk.Label(row1, text="Date (YYYY-MM-DD):").pack(side="left", padx=5)
        self.entry_date = ttk.Entry(row1, width=12)
        self.entry_date.insert(0, datetime.now().strftime("%Y-%m-%d"))
        self.entry_date.pack(side="left", padx=5)

        ttk.Label(row1, text="Mode:").pack(side="left", padx=5)
        self.var_method = tk.StringVar(value="UPI")
        self.combo_method = ttk.Combobox(row1, textvariable=self.var_method, values=["UPI", "Cash", "Card", "Bank Transfer"], width=12)
        self.combo_method.pack(side="left", padx=5)

        # Row 2: Description and Actions
        row2 = ttk.Frame(action_box)
        row2.pack(fill="x", pady=(8, 2))

        ttk.Label(row2, text="Description:").pack(side="left", padx=5)
        self.entry_desc = ttk.Entry(row2, width=32)
        self.entry_desc.pack(side="left", padx=5)

        btn_add = ttk.Button(row2, text="➕ Save Entry", style="Success.TButton", command=self._action_add_transaction)
        btn_add.pack(side="left", padx=10)

        btn_export_csv = ttk.Button(row2, text="📥 Export CSV", command=self._action_export_csv)
        btn_export_csv.pack(side="left", padx=5)

        btn_export_rpt = ttk.Button(row2, text="📄 Export Report", command=self._action_export_report)
        btn_export_rpt.pack(side="left", padx=5)

        btn_delete = ttk.Button(row2, text="🗑️ Delete Selected", style="Danger.TButton", command=self._action_delete_transaction)
        btn_delete.pack(side="right", padx=5)

        # Transactions Table
        table_frame = ttk.Frame(self.tab_transactions)
        table_frame.pack(fill="both", expand=True, padx=15, pady=(0, 10))

        columns = ("id", "date", "type", "category", "amount", "payment_method", "description")
        self.tree_trans = ttk.Treeview(table_frame, columns=columns, show="headings", selectmode="browse")

        self.tree_trans.heading("id", text="ID")
        self.tree_trans.heading("date", text="Date")
        self.tree_trans.heading("type", text="Type")
        self.tree_trans.heading("category", text="Category")
        self.tree_trans.heading("amount", text="Amount (₹)")
        self.tree_trans.heading("payment_method", text="Payment Method")
        self.tree_trans.heading("description", text="Description")

        self.tree_trans.column("id", width=45, anchor="center")
        self.tree_trans.column("date", width=95, anchor="center")
        self.tree_trans.column("type", width=75, anchor="center")
        self.tree_trans.column("category", width=180, anchor="w")
        self.tree_trans.column("amount", width=100, anchor="e")
        self.tree_trans.column("payment_method", width=110, anchor="center")
        self.tree_trans.column("description", width=260, anchor="w")

        scrollbar = ttk.Scrollbar(table_frame, orient="vertical", command=self.tree_trans.yview)
        self.tree_trans.configure(yscrollcommand=scrollbar.set)
        self.tree_trans.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

    # ------------------ TAB 3: BUDGETS & ALERTS ------------------
    def _build_budgets_tab(self):
        top_box = ttk.LabelFrame(self.tab_budgets, text="Set Monthly Category Budget Limit", padding=10)
        top_box.pack(fill="x", padx=15, pady=10)

        f = ttk.Frame(top_box)
        f.pack(fill="x")

        ttk.Label(f, text="Category:").pack(side="left", padx=5)
        self.budget_cat = ttk.Combobox(f, width=20)
        self.budget_cat.pack(side="left", padx=5)

        ttk.Label(f, text="Month (YYYY-MM):").pack(side="left", padx=5)
        self.budget_month = ttk.Entry(f, width=10)
        self.budget_month.insert(0, datetime.now().strftime("%Y-%m"))
        self.budget_month.pack(side="left", padx=5)

        ttk.Label(f, text="Limit (₹):").pack(side="left", padx=5)
        self.budget_limit = ttk.Entry(f, width=10)
        self.budget_limit.pack(side="left", padx=5)

        ttk.Label(f, text="Alert Threshold (%):").pack(side="left", padx=5)
        self.budget_threshold = ttk.Entry(f, width=8)
        self.budget_threshold.insert(0, "80")
        self.budget_threshold.pack(side="left", padx=5)

        btn_save_b = ttk.Button(f, text="💾 Save Budget Limit", style="Primary.TButton", command=self._action_save_budget)
        btn_save_b.pack(side="left", padx=15)

        # Budget Monitoring Table
        b_table_frame = ttk.Frame(self.tab_budgets)
        b_table_frame.pack(fill="both", expand=True, padx=15, pady=(0, 15))

        cols = ("category", "month", "limit", "spent", "remaining", "percent", "status")
        self.tree_budgets = ttk.Treeview(b_table_frame, columns=cols, show="headings", selectmode="browse")

        self.tree_budgets.heading("category", text="Category")
        self.tree_budgets.heading("month", text="Month")
        self.tree_budgets.heading("limit", text="Limit (₹)")
        self.tree_budgets.heading("spent", text="Spent (₹)")
        self.tree_budgets.heading("remaining", text="Remaining (₹)")
        self.tree_budgets.heading("percent", text="Usage (%)")
        self.tree_budgets.heading("status", text="Alert Status")

        for col in cols:
            self.tree_budgets.column(col, anchor="center")
        self.tree_budgets.column("category", anchor="w", width=180)

        b_scroll = ttk.Scrollbar(b_table_frame, orient="vertical", command=self.tree_budgets.yview)
        self.tree_budgets.configure(yscrollcommand=b_scroll.set)
        self.tree_budgets.pack(side="left", fill="both", expand=True)
        b_scroll.pack(side="right", fill="y")

    # ------------------ TAB 4: OCR RECEIPT SCANNER ------------------
    def _build_ocr_tab(self):
        info_frame = ttk.LabelFrame(self.tab_ocr, text="Optical Character Recognition (OCR) Scanner Extension", padding=15)
        info_frame.pack(fill="both", expand=True, padx=15, pady=15)

        lbl = tk.Label(
            info_frame,
            text="Load receipt image or paste raw OCR text below. The smart parser automatically extracts merchant, date, and total.",
            font=("Segoe UI", 9),
            fg="#475569"
        )
        lbl.pack(anchor="w", pady=(0, 10))

        btn_bar = ttk.Frame(info_frame)
        btn_bar.pack(fill="x", pady=(0, 10))

        btn_sample = ttk.Button(btn_bar, text="📄 Load Sample Receipt Text", command=self._load_sample_receipt)
        btn_sample.pack(side="left", padx=5)

        btn_parse = ttk.Button(btn_bar, text="🔍 Extract & Review Transaction", style="Primary.TButton", command=self._parse_ocr_text)
        btn_parse.pack(side="left", padx=10)

        self.txt_ocr = tk.Text(info_frame, font=("Consolas", 10), height=14)
        self.txt_ocr.pack(fill="both", expand=True)

    def _load_sample_receipt(self):
        sample = """CAMPUS TECH SUPERSTORE
Tax Invoice / Bill of Supply
Date: 2026-10-25
Invoice No: CTS-88492
Item 1: Sandisk USB 3.2 64GB      Rs. 550.00
Item 2: Logitech Wireless Mouse   Rs. 899.00
Subtotal: Rs. 1449.00
GST 18%: Rs. 260.82
TOTAL AMOUNT: Rs. 1709.82
Payment Mode: UPI Confirmed
Thank you for visiting!"""
        self.txt_ocr.delete("1.0", tk.END)
        self.txt_ocr.insert(tk.END, sample)

    def _parse_ocr_text(self):
        text = self.txt_ocr.get("1.0", tk.END).strip()
        if not text:
            messagebox.showwarning("Empty Content", "Please enter or load receipt text.")
            return

        extracted = self.service.parse_receipt_text(text)
        
        # Confirmation Dialog before saving
        confirm_msg = (
            f"Review Extracted Receipt Details:\n\n"
            f"Merchant: {extracted['merchant']}\n"
            f"Date: {extracted['date']}\n"
            f"Extracted Total: ₹ {extracted['amount']:.2f}\n"
            f"Assigned Category: {extracted['category']}\n\n"
            f"Do you want to confirm and add this transaction to your records?"
        )
        if messagebox.askyesno("Confirm OCR Extraction", confirm_msg):
            success, msg = self.service.add_transaction(
                t_type="expense",
                amount=extracted["amount"],
                category=extracted["category"],
                description=f"Receipt: {extracted['merchant']}",
                date_str=extracted["date"],
                payment_method="UPI"
            )
            if success:
                messagebox.showinfo("Success", "Transaction confirmed and saved!")
                self.refresh_all_views()
            else:
                messagebox.showerror("Error", msg)

    # ------------------ EVENT HANDLERS & REFRESH ------------------
    def _action_add_transaction(self):
        t_type = self.var_type.get()
        amount = self.entry_amount.get().strip()
        category = self.var_category.get().strip()
        date_str = self.entry_date.get().strip()
        method = self.var_method.get().strip()
        desc = self.entry_desc.get().strip()

        if not category:
            messagebox.showerror("Validation Error", "Please select or enter a category.")
            return

        success, msg = self.service.add_transaction(t_type, amount, category, desc, date_str, method)
        if success:
            messagebox.showinfo("Success", msg)
            self.entry_amount.delete(0, tk.END)
            self.entry_desc.delete(0, tk.END)
            self.refresh_all_views()
        else:
            messagebox.showerror("Error", msg)

    def _action_delete_transaction(self):
        selected = self.tree_trans.selection()
        if not selected:
            messagebox.showwarning("Select Item", "Please select a transaction to delete.")
            return

        item = self.tree_trans.item(selected[0])
        t_id = item["values"][0]
        if messagebox.askyesno("Confirm Delete", f"Are you sure you want to delete transaction ID {t_id}?"):
            if self.service.delete_transaction(t_id):
                messagebox.showinfo("Deleted", "Transaction removed.")
                self.refresh_all_views()
            else:
                messagebox.showerror("Error", "Could not delete transaction.")

    def _action_save_budget(self):
        cat = self.budget_cat.get().strip()
        month = self.budget_month.get().strip()
        limit = self.budget_limit.get().strip()
        threshold = self.budget_threshold.get().strip()

        if not cat or not month or not limit:
            messagebox.showerror("Input Error", "Please complete all budget fields.")
            return

        success, msg = self.service.set_budget(cat, month, limit, threshold)
        if success:
            messagebox.showinfo("Success", msg)
            self.refresh_all_views()
        else:
            messagebox.showerror("Error", msg)

    def _action_export_csv(self):
        path = filedialog.asksaveasfilename(defaultextension=".csv", filetypes=[("CSV files", "*.csv")])
        if path:
            success, res = self.service.export_csv(path)
            if success:
                messagebox.showinfo("Export Successful", f"Saved to:\n{res}")
            else:
                messagebox.showerror("Export Failed", res)

    def _action_export_report(self):
        path = filedialog.asksaveasfilename(defaultextension=".txt", filetypes=[("Text Report", "*.txt")])
        if path:
            success, res = self.service.export_summary_report(path)
            if success:
                messagebox.showinfo("Export Successful", f"Report saved to:\n{res}")
            else:
                messagebox.showerror("Export Failed", res)

    def refresh_all_views(self):
        # 1. Categories Comboboxes
        categories = self.service.get_categories()
        cat_names = [c["name"] for c in categories]
        self.combo_category["values"] = cat_names
        self.budget_cat["values"] = [c["name"] for c in categories if c["type"] == "expense"]
        if cat_names and not self.var_category.get():
            self.var_category.set(cat_names[0])
        if self.budget_cat["values"] and not self.budget_cat.get():
            self.budget_cat.set(self.budget_cat["values"][0])

        # 2. KPI Cards
        summary = self.service.get_financial_summary()
        self.card_income.val_label.config(text=f"₹ {summary['income']:,.2f}")
        self.card_expense.val_label.config(text=f"₹ {summary['expense']:,.2f}")
        self.card_balance.val_label.config(text=f"₹ {summary['balance']:,.2f}")
        self.card_savings.val_label.config(text=f"{summary['savings_rate']} %")

        # 3. Budget Alerts Banner
        alerts = self.service.check_budgets()
        warning_count = sum(1 for b in alerts if b["status"] == "WARNING")
        exceeded_count = sum(1 for b in alerts if b["status"] == "EXCEEDED")

        if exceeded_count > 0:
            self.alert_banner.config(
                text=f"🚨 ALERT: {exceeded_count} Category Budget(s) EXCEEDED! Immediate review required.",
                bg="#FEE2E2",
                fg="#991B1B"
            )
        elif warning_count > 0:
            self.alert_banner.config(
                text=f"⚠️ NOTICE: {warning_count} Category Budget(s) approaching limit threshold (>= 80%).",
                bg="#FEF3C7",
                fg="#92400E"
            )
        else:
            self.alert_banner.config(
                text="✅ All category budgets are currently within healthy spending limits.",
                bg="#DCFCE7",
                fg="#166534"
            )

        # 4. Transactions Treeview
        for row in self.tree_trans.get_children():
            self.tree_trans.delete(row)
        for t in self.service.get_transactions():
            self.tree_trans.insert("", "end", values=(
                t["id"], t["date"], t["type"].upper(), t["category"],
                f"{t['amount']:,.2f}", t["payment_method"], t["description"]
            ))

        # 5. Budgets Treeview
        for row in self.tree_budgets.get_children():
            self.tree_budgets.delete(row)
        for b in alerts:
            self.tree_budgets.insert("", "end", values=(
                b["category"], b["month_year"], f"{b['limit']:,.2f}",
                f"{b['spent']:,.2f}", f"{b['remaining']:,.2f}",
                f"{b['percent_used']} %", b["status"]
            ))

        # 6. Chart Visualizer
        self.root.after(100, self._draw_category_bars)

