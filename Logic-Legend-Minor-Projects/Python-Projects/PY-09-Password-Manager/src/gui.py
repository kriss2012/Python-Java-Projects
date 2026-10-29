"""
Logic Legend - PY-09 Secure Password Manager
Desktop Graphical User Interface (Tkinter)
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import threading
import time
from crypto_utils import CryptoUtils

CLIPBOARD_CLEAR_DELAY = 20  # seconds

class PasswordManagerGUI:
    def __init__(self, root, service):
        self.root = root
        self.service = service
        self.root.title("Logic Legend — PY-09 Secure Password Manager")
        self.root.geometry("1100x760")
        self.root.minsize(920, 640)

        self.selected_entry_id = None
        self.clipboard_timer = None
        self.auto_lock_after_id = None

        self._configure_styles()
        self._build_header()

        self.content_frame = tk.Frame(self.root, bg="#F1F5F9")
        self.content_frame.pack(fill="both", expand=True)

        if not self.service.is_master_configured():
            self._render_setup_screen()
        else:
            self._render_login_screen()

    def _configure_styles(self):
        self.style = ttk.Style()
        self.style.theme_use("clam")
        self.style.configure("Treeview.Heading", font=("Segoe UI", 9, "bold"), background="#E2E8F0")
        self.style.configure("Treeview", font=("Segoe UI", 9), rowheight=26)
        self.style.configure("Primary.TButton", font=("Segoe UI", 9, "bold"), background="#2563EB", foreground="white")
        self.style.configure("Success.TButton", font=("Segoe UI", 9, "bold"), background="#10B981", foreground="white")
        self.style.configure("Danger.TButton", font=("Segoe UI", 9, "bold"), background="#EF4444", foreground="white")

    def _build_header(self):
        self.hdr = tk.Frame(self.root, bg="#0F172A", height=60)
        self.hdr.pack(fill="x", side="top")

        title_lbl = tk.Label(
            self.hdr,
            text="LOGIC LEGEND  |  Secure AES-256 Password Vault",
            font=("Segoe UI", 14, "bold"),
            fg="#F8FAFC",
            bg="#0F172A"
        )
        title_lbl.pack(side="left", padx=20, pady=12)

        self.status_lbl = tk.Label(
            self.hdr,
            text="🔒 VAULT LOCKED",
            font=("Segoe UI", 10, "bold"),
            fg="#EF4444",
            bg="#0F172A"
        )
        self.status_lbl.pack(side="right", padx=20, pady=15)

    def _clear_content(self):
        for widget in self.content_frame.winfo_children():
            widget.destroy()

    def _render_setup_screen(self):
        self._clear_content()
        self.status_lbl.config(text="⚙️ INITIAL SETUP", fg="#F59E0B")

        card = tk.Frame(self.content_frame, bg="white", padx=40, pady=35, relief="solid", bd=1)
        card.place(relx=0.5, rely=0.45, anchor="center")

        tk.Label(card, text="Initialize Master Vault", font=("Segoe UI", 16, "bold"), bg="white", fg="#0F172A").pack(pady=(0, 10))
        tk.Label(
            card,
            text="Create a strong master password.\nThis key encrypts your database using PBKDF2-HMAC-SHA256 & AES-256.",
            font=("Segoe UI", 9),
            bg="white",
            fg="#64748B",
            justify="center"
        ).pack(pady=(0, 20))

        tk.Label(card, text="Master Password (Min 8 chars):", font=("Segoe UI", 9, "bold"), bg="white", anchor="w").pack(fill="x")
        pw_entry = ttk.Entry(card, show="●", width=36, font=("Segoe UI", 10))
        pw_entry.pack(pady=(4, 12))
        pw_entry.focus()

        tk.Label(card, text="Confirm Master Password:", font=("Segoe UI", 9, "bold"), bg="white", anchor="w").pack(fill="x")
        confirm_entry = ttk.Entry(card, show="●", width=36, font=("Segoe UI", 10))
        confirm_entry.pack(pady=(4, 20))

        def on_setup():
            p1 = pw_entry.get()
            p2 = confirm_entry.get()
            if p1 != p2:
                messagebox.showerror("Error", "Passwords do not match!")
                return
            ok, msg = self.service.setup_master_password(p1)
            if ok:
                messagebox.showinfo("Success", msg)
                self._render_dashboard()
            else:
                messagebox.showerror("Setup Failed", msg)

        ttk.Button(card, text="Create Vault & Encrypt", style="Primary.TButton", command=on_setup).pack(fill="x", ipady=4)

    def _render_login_screen(self):
        self._clear_content()
        self.status_lbl.config(text="🔒 VAULT LOCKED", fg="#EF4444")

        card = tk.Frame(self.content_frame, bg="white", padx=40, pady=35, relief="solid", bd=1)
        card.place(relx=0.5, rely=0.45, anchor="center")

        tk.Label(card, text="Unlock Secure Vault", font=("Segoe UI", 16, "bold"), bg="white", fg="#0F172A").pack(pady=(0, 10))
        tk.Label(
            card,
            text="Enter your master password to decrypt stored credentials.",
            font=("Segoe UI", 9),
            bg="white",
            fg="#64748B"
        ).pack(pady=(0, 20))

        tk.Label(card, text="Master Password:", font=("Segoe UI", 9, "bold"), bg="white", anchor="w").pack(fill="x")
        pw_entry = ttk.Entry(card, show="●", width=36, font=("Segoe UI", 10))
        pw_entry.pack(pady=(4, 20))
        pw_entry.focus()

        def on_unlock(event=None):
            pwd = pw_entry.get()
            ok, msg = self.service.unlock_vault(pwd)
            if ok:
                self._render_dashboard()
            else:
                messagebox.showerror("Access Denied", msg)
                pw_entry.delete(0, "end")

        pw_entry.bind("<Return>", on_unlock)
        ttk.Button(card, text="Unlock Vault", style="Primary.TButton", command=on_unlock).pack(fill="x", ipady=4)

    def _render_dashboard(self):
        self._clear_content()
        self.status_lbl.config(text="🔓 VAULT UNLOCKED (AES-256)", fg="#10B981")

        # Top Control Bar
        top_bar = tk.Frame(self.content_frame, bg="#E2E8F0", height=45)
        top_bar.pack(fill="x", side="top", padx=10, pady=5)

        ttk.Button(top_bar, text="🔒 Lock Vault", command=self._on_lock).pack(side="left", padx=5, pady=6)
        ttk.Button(top_bar, text="🛡️ Security Audit", command=self._on_audit).pack(side="left", padx=5, pady=6)
        ttk.Button(top_bar, text="💾 Backup Vault", command=self._on_export_backup).pack(side="left", padx=5, pady=6)
        ttk.Button(top_bar, text="📂 Restore Vault", command=self._on_import_backup).pack(side="left", padx=5, pady=6)

        # Main Split Layout
        paned = tk.PanedWindow(self.content_frame, orient="horizontal", bg="#CBD5E1", bd=0, sashwidth=4)
        paned.pack(fill="both", expand=True, padx=10, pady=(0, 10))

        # Left Pane: List & Search
        left_frame = tk.Frame(paned, bg="white")
        paned.add(left_frame, minsize=420)

        # Search bar
        search_box = tk.Frame(left_frame, bg="white", padx=10, pady=8)
        search_box.pack(fill="x")

        tk.Label(search_box, text="Search:", font=("Segoe UI", 9, "bold"), bg="white").pack(side="left")
        self.search_var = tk.StringVar()
        self.search_var.trace_add("write", lambda *args: self.refresh_list())
        s_entry = ttk.Entry(search_box, textvariable=self.search_var)
        s_entry.pack(side="left", fill="x", expand=True, padx=5)

        self.category_var = tk.StringVar(value="All")
        cat_combo = ttk.Combobox(search_box, textvariable=self.category_var, values=["All", "General", "Email", "Banking", "Social", "Work"], state="readonly", width=10)
        cat_combo.pack(side="right")
        cat_combo.bind("<<ComboboxSelected>>", lambda e: self.refresh_list())

        # Treeview
        tree_frame = tk.Frame(left_frame)
        tree_frame.pack(fill="both", expand=True, padx=10, pady=5)

        cols = ("id", "title", "username", "category")
        self.tree = ttk.Treeview(tree_frame, columns=cols, show="headings", selectmode="browse")
        self.tree.heading("id", text="ID")
        self.tree.heading("title", text="Title / Account")
        self.tree.heading("username", text="Username")
        self.tree.heading("category", text="Category")

        self.tree.column("id", width=35, anchor="center")
        self.tree.column("title", width=140)
        self.tree.column("username", width=130)
        self.tree.column("category", width=75, anchor="center")

        tree_scroll = ttk.Scrollbar(tree_frame, orient="vertical", command=self.tree.yview)
        self.tree.configure(yscrollcommand=tree_scroll.set)
        self.tree.pack(side="left", fill="both", expand=True)
        tree_scroll.pack(side="right", fill="y")

        self.tree.bind("<<TreeviewSelect>>", self._on_select_entry)

        # Right Pane: Entry Detail & Generator
        right_frame = tk.Frame(paned, bg="white", padx=15, pady=10)
        paned.add(right_frame, minsize=480)

        # Form fields
        form_box = ttk.LabelFrame(right_frame, text="Credential Details", padding=10)
        form_box.pack(fill="x", pady=(0, 10))

        tk.Label(form_box, text="Account / Website Title *:", font=("Segoe UI", 9, "bold")).grid(row=0, column=0, sticky="w", pady=3)
        self.ent_title = ttk.Entry(form_box, width=32)
        self.ent_title.grid(row=0, column=1, sticky="ew", pady=3)

        tk.Label(form_box, text="Username / Email *:", font=("Segoe UI", 9, "bold")).grid(row=1, column=0, sticky="w", pady=3)
        self.ent_user = ttk.Entry(form_box, width=32)
        self.ent_user.grid(row=1, column=1, sticky="ew", pady=3)

        tk.Label(form_box, text="Password *:", font=("Segoe UI", 9, "bold")).grid(row=2, column=0, sticky="w", pady=3)
        pw_box = tk.Frame(form_box)
        pw_box.grid(row=2, column=1, sticky="ew", pady=3)
        self.ent_pass = ttk.Entry(pw_box, show="●", width=22)
        self.ent_pass.pack(side="left", fill="x", expand=True)

        self.show_pass_var = tk.BooleanVar(value=False)
        self.btn_toggle_eye = ttk.Button(pw_box, text="👁 Show", width=7, command=self._toggle_show_pass)
        self.btn_toggle_eye.pack(side="left", padx=4)

        ttk.Button(pw_box, text="📋 Copy", width=7, command=self._copy_password).pack(side="left")

        # Live Password Strength Bar
        self.ent_pass.bind("<KeyRelease>", self._update_live_strength)
        self.strength_lbl = tk.Label(form_box, text="Strength: -", font=("Segoe UI", 8), fg="#64748B")
        self.strength_lbl.grid(row=3, column=1, sticky="w")

        tk.Label(form_box, text="Website URL:", font=("Segoe UI", 9)).grid(row=4, column=0, sticky="w", pady=3)
        self.ent_url = ttk.Entry(form_box, width=32)
        self.ent_url.grid(row=4, column=1, sticky="ew", pady=3)

        tk.Label(form_box, text="Category:", font=("Segoe UI", 9)).grid(row=5, column=0, sticky="w", pady=3)
        self.ent_cat = ttk.Combobox(form_box, values=["General", "Email", "Banking", "Social", "Work"], state="readonly", width=30)
        self.ent_cat.set("General")
        self.ent_cat.grid(row=5, column=1, sticky="ew", pady=3)

        tk.Label(form_box, text="Notes:", font=("Segoe UI", 9)).grid(row=6, column=0, sticky="nw", pady=3)
        self.ent_notes = tk.Text(form_box, height=3, width=30, font=("Segoe UI", 9))
        self.ent_notes.grid(row=6, column=1, sticky="ew", pady=3)

        # Form Buttons
        f_btn_box = tk.Frame(form_box)
        f_btn_box.grid(row=7, column=0, columnspan=2, pady=(10, 2))
        ttk.Button(f_btn_box, text="💾 Save / Update", style="Primary.TButton", command=self._save_entry).pack(side="left", padx=5)
        ttk.Button(f_btn_box, text="🗑 Delete", style="Danger.TButton", command=self._delete_entry).pack(side="left", padx=5)
        ttk.Button(f_btn_box, text="🧹 Clear Form", command=self._clear_form).pack(side="left", padx=5)

        # Password Generator Section
        gen_box = ttk.LabelFrame(right_frame, text="Integrated Password Generator", padding=10)
        gen_box.pack(fill="x", pady=5)

        len_box = tk.Frame(gen_box)
        len_box.pack(fill="x", pady=2)
        tk.Label(len_box, text="Length:", font=("Segoe UI", 9)).pack(side="left")
        self.gen_len_var = tk.IntVar(value=16)
        self.gen_len_slider = ttk.Scale(len_box, from_=8, to=36, variable=self.gen_len_var, orient="horizontal", command=lambda v: self.gen_len_lbl.config(text=str(int(float(v)))))
        self.gen_len_slider.pack(side="left", fill="x", expand=True, padx=8)
        self.gen_len_lbl = tk.Label(len_box, text="16", font=("Segoe UI", 9, "bold"), width=3)
        self.gen_len_lbl.pack(side="left")

        opt_box = tk.Frame(gen_box)
        opt_box.pack(fill="x", pady=4)
        self.gen_upper = tk.BooleanVar(value=True)
        self.gen_digits = tk.BooleanVar(value=True)
        self.gen_symbols = tk.BooleanVar(value=True)

        ttk.Checkbutton(opt_box, text="A-Z", variable=self.gen_upper).pack(side="left", padx=6)
        ttk.Checkbutton(opt_box, text="0-9", variable=self.gen_digits).pack(side="left", padx=6)
        ttk.Checkbutton(opt_box, text="#$%", variable=self.gen_symbols).pack(side="left", padx=6)

        ttk.Button(gen_box, text="⚡ Generate & Apply to Field", style="Success.TButton", command=self._generate_and_apply).pack(fill="x", pady=4)

        # Clipboard Auto-Clear Banner
        self.clip_banner = tk.Label(right_frame, text="", font=("Segoe UI", 9, "italic"), fg="#2563EB", bg="white")
        self.clip_banner.pack(pady=4)

        self.refresh_list()

    def refresh_list(self):
        for item in self.tree.get_children():
            self.tree.delete(item)

        entries = self.service.get_entries(
            search_query=self.search_var.get(),
            category_filter=self.category_var.get()
        )
        for e in entries:
            self.tree.insert("", "end", values=(e["id"], e["title"], e["username"], e["category"]))

    def _on_select_entry(self, event):
        sel = self.tree.selection()
        if not sel:
            return
        item = self.tree.item(sel[0])
        entry_id = item["values"][0]
        entry = self.service.get_entry_by_id(entry_id)
        if not entry:
            return

        self.selected_entry_id = entry["id"]
        self.ent_title.delete(0, "end")
        self.ent_title.insert(0, entry["title"])

        self.ent_user.delete(0, "end")
        self.ent_user.insert(0, entry["username"])

        self.ent_pass.delete(0, "end")
        self.ent_pass.insert(0, entry["decrypted_password"])

        self.ent_url.delete(0, "end")
        self.ent_url.insert(0, entry["url"] or "")

        self.ent_cat.set(entry["category"] or "General")

        self.ent_notes.delete("1.0", "end")
        self.ent_notes.insert("1.0", entry["notes"] or "")

        self._update_live_strength()

    def _clear_form(self):
        self.selected_entry_id = None
        self.tree.selection_remove(self.tree.selection())
        self.ent_title.delete(0, "end")
        self.ent_user.delete(0, "end")
        self.ent_pass.delete(0, "end")
        self.ent_url.delete(0, "end")
        self.ent_cat.set("General")
        self.ent_notes.delete("1.0", "end")
        self.strength_lbl.config(text="Strength: -", fg="#64748B")

    def _toggle_show_pass(self):
        if self.show_pass_var.get():
            self.ent_pass.config(show="●")
            self.btn_toggle_eye.config(text="👁 Show")
            self.show_pass_var.set(False)
        else:
            self.ent_pass.config(show="")
            self.btn_toggle_eye.config(text="🙈 Hide")
            self.show_pass_var.set(True)

    def _update_live_strength(self, event=None):
        pwd = self.ent_pass.get()
        if not pwd:
            self.strength_lbl.config(text="Strength: -", fg="#64748B")
            return
        eval_res = CryptoUtils.evaluate_strength(pwd)
        score = eval_res["score"]
        rating = eval_res["rating"]
        entropy = eval_res["entropy"]

        colors = {
            "VERY WEAK": "#DC2626",
            "WEAK": "#EA580C",
            "MODERATE": "#D97706",
            "STRONG": "#16A34A",
            "VERY STRONG": "#059669"
        }
        color = colors.get(rating, "#64748B")
        self.strength_lbl.config(text=f"Strength: {rating} ({score}/100) — Entropy: {entropy} bits", fg=color)

    def _generate_and_apply(self):
        pwd = CryptoUtils.generate_password(
            length=int(self.gen_len_var.get()),
            use_upper=self.gen_upper.get(),
            use_lower=True,
            use_digits=self.gen_digits.get(),
            use_symbols=self.gen_symbols.get()
        )
        self.ent_pass.delete(0, "end")
        self.ent_pass.insert(0, pwd)
        self._update_live_strength()

    def _copy_password(self):
        pwd = self.ent_pass.get()
        if not pwd:
            return
        try:
            import pyperclip
            pyperclip.copy(pwd)
        except Exception:
            self.root.clipboard_clear()
            self.root.clipboard_append(pwd)

        self.clip_banner.config(text=f"📋 Password copied! Clipboard will auto-clear in {CLIPBOARD_CLEAR_DELAY}s.")

        if self.clipboard_timer:
            self.root.after_cancel(self.clipboard_timer)

        self.clipboard_timer = self.root.after(CLIPBOARD_CLEAR_DELAY * 1000, self._auto_clear_clipboard)

    def _auto_clear_clipboard(self):
        try:
            import pyperclip
            pyperclip.copy("")
        except Exception:
            self.root.clipboard_clear()
        self.clip_banner.config(text="🔒 Clipboard auto-cleared for security.")

    def _save_entry(self):
        title = self.ent_title.get()
        user = self.ent_user.get()
        pwd = self.ent_pass.get()
        url = self.ent_url.get()
        cat = self.ent_cat.get()
        notes = self.ent_notes.get("1.0", "end").strip()

        if self.selected_entry_id:
            ok, msg = self.service.update_entry(self.selected_entry_id, title, user, pwd, url, cat, notes)
        else:
            ok, msg = self.service.add_entry(title, user, pwd, url, cat, notes)

        if ok:
            messagebox.showinfo("Saved", msg)
            self.refresh_list()
            self._clear_form()
        else:
            messagebox.showerror("Error", msg)

    def _delete_entry(self):
        if not self.selected_entry_id:
            messagebox.showwarning("Warning", "Select a credential to delete.")
            return

        confirm = messagebox.askyesno("Confirm Delete", "Are you sure you want to permanently delete this credential?")
        if confirm:
            ok, msg = self.service.delete_entry(self.selected_entry_id)
            if ok:
                messagebox.showinfo("Deleted", msg)
                self.refresh_list()
                self._clear_form()
            else:
                messagebox.showerror("Error", msg)

    def _on_lock(self):
        self.service.lock_vault()
        self._render_login_screen()

    def _on_audit(self):
        report = self.service.audit_vault()
        win = tk.Toplevel(self.root)
        win.title("Security Audit Report")
        win.geometry("560x420")
        win.configure(bg="#F8FAFC")

        tk.Label(win, text="🛡️ Vault Security Audit", font=("Segoe UI", 14, "bold"), bg="#F8FAFC", fg="#0F172A").pack(pady=12)

        summary_text = (
            f"Total Credentials Analyzed: {report['total_credentials']}\n"
            f"Weak Passwords (Score < 50): {report['weak_count']}\n"
            f"Reused / Duplicate Passwords: {report['duplicate_count']}"
        )
        tk.Label(win, text=summary_text, font=("Segoe UI", 10), justify="left", bg="#F8FAFC").pack(pady=5)

        t_box = tk.Text(win, font=("Segoe UI", 9), height=14)
        t_box.pack(fill="both", expand=True, padx=15, pady=10)

        if report["weak_entries"]:
            t_box.insert("end", "⚠️ WEAK CREDENTIALS IDENTIFIED:\n")
            for w in report["weak_entries"]:
                t_box.insert("end", f"  * [{w['title']}] ({w['username']}) - Rating: {w['rating']} (Score: {w['score']})\n")
                for fb in w["feedback"]:
                    t_box.insert("end", f"      - {fb}\n")
            t_box.insert("end", "\n")

        if report["duplicate_groups"]:
            t_box.insert("end", "⚠️ DUPLICATE PASSWORDS DETECTED ACROSS ACCOUNTS:\n")
            for pwd, titles in report["duplicate_groups"].items():
                t_box.insert("end", f"  * Same password shared across: {', '.join(titles)}\n")

        if not report["weak_entries"] and not report["duplicate_groups"]:
            t_box.insert("end", "✅ EXCELLENT! No weak or reused passwords detected in your vault.")

        t_box.config(state="disabled")

    def _on_export_backup(self):
        path = filedialog.asksaveasfilename(defaultextension=".enc", filetypes=[("Encrypted Vault Backup", "*.enc")])
        if path:
            ok, res = self.service.export_encrypted_backup(path)
            if ok:
                messagebox.showinfo("Export Successful", f"Encrypted backup saved to:\n{res}")
            else:
                messagebox.showerror("Export Failed", res)

    def _on_import_backup(self):
        path = filedialog.askopenfilename(filetypes=[("Encrypted Vault Backup", "*.enc")])
        if path:
            ok, msg = self.service.import_encrypted_backup(path)
            if ok:
                messagebox.showinfo("Restore Successful", msg)
                self.refresh_list()
            else:
                messagebox.showerror("Restore Failed", msg)

