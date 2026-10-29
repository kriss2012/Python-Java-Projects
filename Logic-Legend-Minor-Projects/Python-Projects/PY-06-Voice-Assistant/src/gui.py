"""
Logic Legend - PY-06 Personal Voice Assistant
Desktop Graphical User Interface (Tkinter)
"""

import tkinter as tk
from tkinter import ttk, messagebox
import threading
from datetime import datetime

class AssistantGUI:
    def __init__(self, root, parser, voice, db):
        self.root = root
        self.parser = parser
        self.voice = voice
        self.db = db

        self.root.title("Logic Legend — PY-06 Personal Voice Assistant")
        self.root.geometry("1040x720")
        self.root.minsize(880, 600)

        self._configure_styles()
        self._build_header()
        self._build_main_layout()
        self.refresh_todos()
        self.refresh_history()

    def _configure_styles(self):
        self.style = ttk.Style()
        self.style.theme_use("clam")
        self.style.configure("Primary.TButton", font=("Segoe UI", 9, "bold"), background="#2563EB", foreground="white")
        self.style.configure("Success.TButton", font=("Segoe UI", 9, "bold"), background="#10B981", foreground="white")
        self.style.configure("Danger.TButton", font=("Segoe UI", 9, "bold"), background="#EF4444", foreground="white")

    def _build_header(self):
        hdr = tk.Frame(self.root, bg="#0F172A", height=60)
        hdr.pack(fill="x", side="top")

        lbl_title = tk.Label(
            hdr,
            text="LOGIC LEGEND  |  Personal Voice Assistant",
            font=("Segoe UI", 14, "bold"),
            fg="#F8FAFC",
            bg="#0F172A"
        )
        lbl_title.pack(side="left", padx=20, pady=12)

        self.lbl_mic_status = tk.Label(
            hdr,
            text="● Assistant Ready (Voice & Text)",
            font=("Segoe UI", 9, "bold"),
            fg="#10B981",
            bg="#0F172A"
        )
        self.lbl_mic_status.pack(side="right", padx=20, pady=15)

    def _build_main_layout(self):
        paned = tk.PanedWindow(self.root, orient="horizontal", bg="#F1F5F9")
        paned.pack(fill="both", expand=True, padx=10, pady=10)

        # ----------------- LEFT PANE: CHAT & COMMAND LOGS -----------------
        left_box = ttk.LabelFrame(paned, text="Assistant Dialogue & Command History", padding=12)
        paned.add(left_box, width=620)

        # Controls Bar
        ctrl_bar = ttk.Frame(left_box)
        ctrl_bar.pack(fill="x", pady=(0, 8))

        self.var_tts = tk.BooleanVar(value=True)
        chk_tts = ttk.Checkbutton(ctrl_bar, text="Voice Output (TTS)", variable=self.var_tts, command=self._toggle_tts)
        chk_tts.pack(side="left", padx=5)

        lbl_shield = tk.Label(ctrl_bar, text="🛡️ Security Allowlist: Enforced", font=("Segoe UI", 8, "bold"), fg="#166534", bg="#DCFCE7", padx=6, pady=2)
        lbl_shield.pack(side="right", padx=5)

        # Chat Log Box
        self.txt_log = tk.Text(left_box, wrap="word", font=("Segoe UI", 10), state="disabled", bg="#FFFFFF", relief="solid", bd=1)
        self.txt_log.pack(fill="both", expand=True, pady=(0, 10))

        # Input Controls Bar
        input_bar = ttk.Frame(left_box)
        input_bar.pack(fill="x")

        self.btn_listen = ttk.Button(input_bar, text="🎤 Speak", style="Success.TButton", command=self._action_listen)
        self.btn_listen.pack(side="left", padx=(0, 8))

        self.entry_cmd = ttk.Entry(input_bar, font=("Segoe UI", 10))
        self.entry_cmd.pack(side="left", fill="x", expand=True, padx=(0, 8))
        self.entry_cmd.bind("<Return>", lambda e: self._action_send_typed())

        btn_send = ttk.Button(input_bar, text="Execute", style="Primary.TButton", command=self._action_send_typed)
        btn_send.pack(side="right")

        # ----------------- RIGHT PANE: TO-DOS & SMART HOME -----------------
        right_box = ttk.Frame(paned)
        paned.add(right_box, width=380)

        # To-Do Management Box
        todo_box = ttk.LabelFrame(right_box, text="To-Do & Reminders Manager", padding=10)
        todo_box.pack(fill="both", expand=True, pady=(0, 10))

        add_bar = ttk.Frame(todo_box)
        add_bar.pack(fill="x", pady=(0, 8))
        self.entry_quick_task = ttk.Entry(add_bar, width=22)
        self.entry_quick_task.pack(side="left", fill="x", expand=True, padx=(0, 5))
        btn_quick_add = ttk.Button(add_bar, text="➕ Add", command=self._action_quick_add_todo)
        btn_quick_add.pack(side="right")

        # Tasks Table
        cols = ("id", "title", "status")
        self.tree_todo = ttk.Treeview(todo_box, columns=cols, show="headings", height=8, selectmode="browse")
        self.tree_todo.heading("id", text="#")
        self.tree_todo.heading("title", text="Task Description")
        self.tree_todo.heading("status", text="Status")
        self.tree_todo.column("id", width=35, anchor="center")
        self.tree_todo.column("title", width=220, anchor="w")
        self.tree_todo.column("status", width=70, anchor="center")
        self.tree_todo.pack(fill="both", expand=True, pady=(0, 8))

        todo_action_bar = ttk.Frame(todo_box)
        todo_action_bar.pack(fill="x")
        btn_done = ttk.Button(todo_action_bar, text="✔ Mark Done", command=self._action_complete_todo)
        btn_done.pack(side="left", padx=2)
        btn_del = ttk.Button(todo_action_bar, text="🗑️ Delete", command=self._action_delete_todo)
        btn_del.pack(side="left", padx=2)
        btn_clear = ttk.Button(todo_action_bar, text="Clear All", style="Danger.TButton", command=self._action_clear_todos)
        btn_clear.pack(side="right", padx=2)

        # Smart Home Simulation Box (Extension)
        sh_box = ttk.LabelFrame(right_box, text="Smart Home Control Hub (Extension)", padding=10)
        sh_box.pack(fill="x")

        self.lbl_sh_light = tk.Label(sh_box, text="Living Room Light: OFF", font=("Segoe UI", 9), anchor="w")
        self.lbl_sh_light.pack(fill="x", pady=2)
        self.lbl_sh_fan = tk.Label(sh_box, text="Study Room Fan: OFF", font=("Segoe UI", 9), anchor="w")
        self.lbl_sh_fan.pack(fill="x", pady=2)
        self.lbl_sh_ac = tk.Label(sh_box, text="Air Conditioner: OFF", font=("Segoe UI", 9), anchor="w")
        self.lbl_sh_ac.pack(fill="x", pady=2)

        sh_btn_bar = ttk.Frame(sh_box)
        sh_btn_bar.pack(fill="x", pady=(6, 0))
        btn_tog_light = ttk.Button(sh_btn_bar, text="Toggle Light", command=lambda: self._execute_text_command("smart home light on" if "OFF" in self.lbl_sh_light["text"] else "smart home light off"))
        btn_tog_light.pack(side="left", padx=2)
        btn_tog_fan = ttk.Button(sh_btn_bar, text="Toggle Fan", command=lambda: self._execute_text_command("smart home fan on" if "OFF" in self.lbl_sh_fan["text"] else "smart home fan off"))
        btn_tog_fan.pack(side="left", padx=2)

    def _toggle_tts(self):
        self.voice.voice_enabled = self.var_tts.get()

    def _append_chat(self, speaker, text, color="#1E293B"):
        self.txt_log.config(state="normal")
        time_str = datetime.now().strftime("%I:%M %p")
        self.txt_log.insert(tk.END, f"[{time_str}] {speaker}: ", ("bold",))
        self.txt_log.insert(tk.END, f"{text}\n\n")
        self.txt_log.tag_config("bold", font=("Segoe UI", 9, "bold"), foreground=color)
        self.txt_log.see(tk.END)
        self.txt_log.config(state="disabled")

    def _execute_text_command(self, cmd_text):
        cmd = cmd_text.strip()
        if not cmd:
            return
        self._append_chat("You", cmd, color="#2563EB")
        res = self.parser.execute_command(cmd)

        if res.get("status") == "NEEDS_CONFIRMATION":
            if messagebox.askyesno("Confirm Dangerous Action", res["response"]):
                res = self.parser.execute_command(cmd, confirm_destructive=True)

        self._append_chat("Assistant", res["response"], color="#059669")
        if res.get("action") == "TODO_UPDATED":
            self.refresh_todos()
        if "smart_home" in res:
            self._update_smart_home_ui(res["smart_home"])

    def _action_send_typed(self):
        cmd = self.entry_cmd.get().strip()
        self.entry_cmd.delete(0, tk.END)
        self._execute_text_command(cmd)

    def _action_listen(self):
        self.lbl_mic_status.config(text="● Listening for voice command...", fg="#EF4444")
        self.btn_listen.config(state="disabled")

        def _listen_worker():
            res = self.voice.listen()
            self.root.after(0, lambda: self._handle_listen_result(res))

        threading.Thread(target=_listen_worker, daemon=True).start()

    def _handle_listen_result(self, res):
        self.btn_listen.config(state="normal")
        self.lbl_mic_status.config(text="● Assistant Ready (Voice & Text)", fg="#10B981")
        if "text" in res:
            self._execute_text_command(res["text"])
        else:
            self._append_chat("System", res.get("error", "Speech recognition failed."), color="#DC2626")

    def _action_quick_add_todo(self):
        t = self.entry_quick_task.get().strip()
        if t:
            self.db.add_todo(t)
            self.entry_quick_task.delete(0, tk.END)
            self.refresh_todos()

    def _action_complete_todo(self):
        sel = self.tree_todo.selection()
        if sel:
            tid = self.tree_todo.item(sel[0])["values"][0]
            self.db.complete_todo(tid)
            self.refresh_todos()

    def _action_delete_todo(self):
        sel = self.tree_todo.selection()
        if sel:
            tid = self.tree_todo.item(sel[0])["values"][0]
            self.db.delete_todo(tid)
            self.refresh_todos()

    def _action_clear_todos(self):
        if messagebox.askyesno("Confirm Clear", "Are you sure you want to clear all tasks?"):
            self.db.clear_all_todos()
            self.refresh_todos()

    def _update_smart_home_ui(self, sh):
        self.lbl_sh_light.config(text=f"Living Room Light: {sh.get('living_room_light', 'OFF')}")
        self.lbl_sh_fan.config(text=f"Study Room Fan: {sh.get('study_fan', 'OFF')}")
        self.lbl_sh_ac.config(text=f"Air Conditioner: {sh.get('air_conditioner', 'OFF')}")

    def refresh_todos(self):
        for row in self.tree_todo.get_children():
            self.tree_todo.delete(row)
        for t in self.db.get_todos():
            self.tree_todo.insert("", "end", values=(t["id"], t["title"], t["status"]))

    def refresh_history(self):
        self._append_chat("Assistant", "Hello! I am your Personal Voice Assistant. Speak or type 'time', 'wikipedia <topic>', 'add task <text>', or 'open notepad'.", color="#059669")

