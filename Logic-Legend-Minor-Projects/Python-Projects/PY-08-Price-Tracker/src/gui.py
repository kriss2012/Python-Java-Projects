"""
Logic Legend - PY-08 Price Tracker with Web Scraping
Desktop Graphical User Interface (Tkinter)
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from datetime import datetime

class PriceTrackerGUI:
    def __init__(self, root, service):
        self.root = root
        self.service = service
        self.root.title("Logic Legend — PY-08 Autonomous Price Tracker")
        self.root.geometry("1060x740")
        self.root.minsize(900, 620)

        self.selected_product_id = None
        self._configure_styles()
        self._build_header()
        self._build_main_layout()
        self.refresh_products()

    def _configure_styles(self):
        self.style = ttk.Style()
        self.style.theme_use("clam")
        self.style.configure("Treeview.Heading", font=("Segoe UI", 9, "bold"), background="#E2E8F0")
        self.style.configure("Treeview", font=("Segoe UI", 9), rowheight=26)
        self.style.configure("Primary.TButton", font=("Segoe UI", 9, "bold"), background="#2563EB", foreground="white")
        self.style.configure("Success.TButton", font=("Segoe UI", 9, "bold"), background="#10B981", foreground="white")
        self.style.configure("Danger.TButton", font=("Segoe UI", 9, "bold"), background="#EF4444", foreground="white")

    def _build_header(self):
        hdr = tk.Frame(self.root, bg="#0F172A", height=60)
        hdr.pack(fill="x", side="top")

        title_lbl = tk.Label(
            hdr,
            text="LOGIC LEGEND  |  Autonomous Price Tracker & Web Scraper",
            font=("Segoe UI", 14, "bold"),
            fg="#F8FAFC",
            bg="#0F172A"
        )
        title_lbl.pack(side="left", padx=20, pady=12)

        sub_lbl = tk.Label(
            hdr,
            text="Automated Scraper with Threshold SMTP Alerts & Trend Predictions",
            font=("Segoe UI", 9, "italic"),
            fg="#94A3B8",
            bg="#0F172A"
        )
        sub_lbl.pack(side="right", padx=20, pady=15)

    def _build_main_layout(self):
        # 1. Top Add Box
        top_box = ttk.LabelFrame(self.root, text="Add New Product to Monitor", padding=12)
        top_box.pack(fill="x", padx=15, pady=(10, 5))

        f_row = ttk.Frame(top_box)
        f_row.pack(fill="x")

        ttk.Label(f_row, text="Product Title:").pack(side="left", padx=4)
        self.ent_title = ttk.Entry(f_row, width=24)
        self.ent_title.pack(side="left", padx=4)

        ttk.Label(f_row, text="Product URL:").pack(side="left", padx=4)
        self.ent_url = ttk.Entry(f_row, width=32)
        self.ent_url.pack(side="left", padx=4)

        ttk.Label(f_row, text="Target Price (₹):").pack(side="left", padx=4)
        self.ent_target = ttk.Entry(f_row, width=10)
        self.ent_target.pack(side="left", padx=4)

        btn_add = ttk.Button(f_row, text="➕ Start Tracking", style="Primary.TButton", command=self._action_add_product)
        btn_add.pack(side="left", padx=10)

        # 2. Main Paned Window
        paned = tk.PanedWindow(self.root, orient="vertical", bg="#F1F5F9")
        paned.pack(fill="both", expand=True, padx=15, pady=5)

        # Table Box (Upper Pane)
        tbl_frame = ttk.LabelFrame(paned, text="Tracked Products & Live Prices", padding=8)
        paned.add(tbl_frame, height=260)

        tbl_ctrl = ttk.Frame(tbl_frame)
        tbl_ctrl.pack(fill="x", pady=(0, 6))

        btn_check_all = ttk.Button(tbl_ctrl, text="🔄 Check All Active Prices", style="Success.TButton", command=self._action_check_all)
        btn_check_all.pack(side="left", padx=4)

        btn_toggle = ttk.Button(tbl_ctrl, text="⏸ Pause / Resume Selected", command=self._action_toggle_status)
        btn_toggle.pack(side="left", padx=4)

        btn_del = ttk.Button(tbl_ctrl, text="🗑️ Delete Product", style="Danger.TButton", command=self._action_delete_product)
        btn_del.pack(side="left", padx=4)

        btn_export = ttk.Button(tbl_ctrl, text="📥 Export CSV", command=self._action_export_csv)
        btn_export.pack(side="right", padx=4)

        # Products Treeview
        cols = ("id", "title", "current", "target", "savings", "status", "rating")
        self.tree_prods = ttk.Treeview(tbl_frame, columns=cols, show="headings", selectmode="browse")
        self.tree_prods.heading("id", text="ID")
        self.tree_prods.heading("title", text="Product Title")
        self.tree_prods.heading("current", text="Current Price (₹)")
        self.tree_prods.heading("target", text="Target Price (₹)")
        self.tree_prods.heading("savings", text="Alert Trigger Status")
        self.tree_prods.heading("status", text="Tracking Status")
        self.tree_prods.heading("rating", text="Rating")

        self.tree_prods.column("id", width=35, anchor="center")
        self.tree_prods.column("title", width=280, anchor="w")
        self.tree_prods.column("current", width=120, anchor="e")
        self.tree_prods.column("target", width=120, anchor="e")
        self.tree_prods.column("savings", width=160, anchor="center")
        self.tree_prods.column("status", width=100, anchor="center")
        self.tree_prods.column("rating", width=60, anchor="center")

        self.tree_prods.bind("<<TreeviewSelect>>", self._on_product_selected)

        scr = ttk.Scrollbar(tbl_frame, orient="vertical", command=self.tree_prods.yview)
        self.tree_prods.configure(yscrollcommand=scr.set)
        self.tree_prods.pack(side="left", fill="both", expand=True)
        scr.pack(side="right", fill="y")

        # Lower Pane: Chart & Best Time to Buy
        lower_box = ttk.LabelFrame(paned, text="Price Trend Visualization & Purchase Advisor", padding=10)
        paned.add(lower_box, height=280)

        # Advisor banner
        self.lbl_advisor = tk.Label(
            lower_box,
            text="Select a tracked product to view historical price trajectory and purchase advice.",
            font=("Segoe UI", 10, "bold"),
            bg="#E2E8F0",
            fg="#1E293B",
            pady=6
        )
        self.lbl_advisor.pack(fill="x", pady=(0, 8))

        # Canvas for line chart
        self.chart_canvas = tk.Canvas(lower_box, bg="white", highlightthickness=0)
        self.chart_canvas.pack(fill="both", expand=True)

    def _action_add_product(self):
        title = self.ent_title.get().strip()
        url = self.ent_url.get().strip()
        target = self.ent_target.get().strip()

        if not title or not url or not target:
            messagebox.showerror("Validation Error", "All fields are required.")
            return

        ok, msg = self.service.add_product(url, title, target)
        if ok:
            messagebox.showinfo("Success", msg)
            self.ent_title.delete(0, tk.END)
            self.ent_url.delete(0, tk.END)
            self.ent_target.delete(0, tk.END)
            self.refresh_products()
        else:
            messagebox.showerror("Error", msg)

    def _action_check_all(self):
        results = self.service.check_all_active()
        alert_count = sum(1 for r in results if r.get("alert_triggered"))
        self.refresh_products()
        if alert_count > 0:
            messagebox.showinfo("Price Drop Alerts", f"Scrape complete! {alert_count} product(s) dropped below target price and triggered email alerts!")
        else:
            messagebox.showinfo("Scrape Complete", f"Checked {len(results)} products. All prices updated.")

    def _action_toggle_status(self):
        sel = self.tree_prods.selection()
        if not sel:
            messagebox.showwarning("Select Product", "Please select a product.")
            return
        pid = self.tree_prods.item(sel[0])["values"][0]
        ok, new_st = self.service.toggle_product_status(pid)
        if ok:
            self.refresh_products()

    def _action_delete_product(self):
        sel = self.tree_prods.selection()
        if not sel:
            messagebox.showwarning("Select Product", "Please select a product.")
            return
        pid = self.tree_prods.item(sel[0])["values"][0]
        if messagebox.askyesno("Confirm Delete", f"Are you sure you want to delete product #{pid}?"):
            self.service.delete_product(pid)
            self.refresh_products()
            self.chart_canvas.delete("all")
            self.lbl_advisor.config(text="Select a product to view price trend.")

    def _action_export_csv(self):
        path = filedialog.asksaveasfilename(defaultextension=".csv", filetypes=[("CSV files", "*.csv")])
        if path:
            ok, res = self.service.export_csv(path)
            if ok:
                messagebox.showinfo("Export Success", f"Saved to:\n{res}")
            else:
                messagebox.showerror("Export Failed", res)

    def _on_product_selected(self, event):
        sel = self.tree_prods.selection()
        if not sel:
            return
        item = self.tree_prods.item(sel[0])
        pid = item["values"][0]
        self.selected_product_id = pid

        # Update Advisor
        analysis = self.service.analyze_best_time_to_buy(pid)
        verdict = analysis.get("verdict", "WAIT")
        color_map = {"BUY NOW": ("#DCFCE7", "#166534"), "WAIT FOR DROP": ("#FEE2E2", "#991B1B"), "FAIR VALUE": ("#FEF3C7", "#92400E")}
        bg_col, fg_col = color_map.get(verdict, ("#E2E8F0", "#1E293B"))

        self.lbl_advisor.config(
            text=f"[{verdict}] {analysis.get('advice', '')}",
            bg=bg_col,
            fg=fg_col
        )

        # Draw Line Chart
        self._draw_price_chart(pid, item["values"][3])

    def _draw_price_chart(self, product_id, target_price_str):
        self.chart_canvas.delete("all")
        history = self.service.get_price_history(product_id)
        w = self.chart_canvas.winfo_width()
        h = self.chart_canvas.winfo_height()
        if w < 100 or h < 100:
            w, h = 800, 220

        if not history or len(history) < 2:
            self.chart_canvas.create_text(w/2, h/2, text="Need at least 2 historical price records to plot trajectory.", font=("Segoe UI", 10), fill="#64748B")
            return

        try:
            target_val = float(str(target_price_str).replace(',', ''))
        except ValueError:
            target_val = 0.0

        prices = [p["price"] for p in history]
        all_vals = prices + ([target_val] if target_val > 0 else [])
        min_p = min(all_vals) * 0.95
        max_p = max(all_vals) * 1.05
        span = max_p - min_p if (max_p - min_p) > 0 else 1.0

        pad_x = 60
        pad_y = 30
        plot_w = w - (2 * pad_x)
        plot_h = h - (2 * pad_y)

        # Draw Axes
        self.chart_canvas.create_line(pad_x, pad_y + plot_h, pad_x + plot_w, pad_y + plot_h, fill="#CBD5E1", width=2)
        self.chart_canvas.create_line(pad_x, pad_y, pad_x, pad_y + plot_h, fill="#CBD5E1", width=2)

        # Draw Target Price Threshold Line (Dashed Red)
        if target_val > 0:
            target_y = pad_y + plot_h - ((target_val - min_p) / span * plot_h)
            self.chart_canvas.create_line(pad_x, target_y, pad_x + plot_w, target_y, fill="#EF4444", dash=(4, 4), width=2)
            self.chart_canvas.create_text(pad_x + plot_w - 60, target_y - 10, text=f"Target: ₹{target_val:,.0f}", font=("Segoe UI", 8, "bold"), fill="#EF4444")

        # Plot Price Trajectory Points
        step_x = plot_w / (len(prices) - 1)
        coords = []
        for idx, p in enumerate(prices):
            cx = pad_x + (idx * step_x)
            cy = pad_y + plot_h - ((p - min_p) / span * plot_h)
            coords.append((cx, cy))

        for i in range(len(coords) - 1):
            self.chart_canvas.create_line(coords[i][0], coords[i][1], coords[i+1][0], coords[i+1][1], fill="#2563EB", width=3)

        for cx, cy in coords:
            self.chart_canvas.create_oval(cx - 4, cy - 4, cx + 4, cy + 4, fill="#1D4ED8", outline="white", width=2)

    def refresh_products(self):
        for row in self.tree_prods.get_children():
            self.tree_prods.delete(row)

        products = self.service.get_all_products()
        for p in products:
            curr = p["current_price"]
            target = p["target_price"]
            alert_status = "🚨 ALERT TRIGGERED" if curr <= target else "Above Target"

            self.tree_prods.insert("", "end", values=(
                p["id"], p["title"], f"{curr:,.2f}", f"{target:,.2f}",
                alert_status, p["status"], f"★ {p['rating']}"
            ))

