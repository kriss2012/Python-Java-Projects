"""
Logic Legend - PY-03 Face Recognition Attendance System
Desktop Graphical User Interface (Tkinter)
"""

import os
import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from datetime import datetime
import cv2
from PIL import Image, ImageTk

class AttendanceGUI:
    def __init__(self, root, service):
        self.root = root
        self.service = service
        self.root.title("Logic Legend — PY-03 Face Recognition Attendance System")
        self.root.geometry("1040x740")
        self.root.minsize(880, 620)

        self.cap = None
        self.camera_running = False
        self.current_frame = None

        self._configure_styles()
        self._build_header()
        self._build_notebook()
        self.refresh_all_views()

    def _configure_styles(self):
        self.style = ttk.Style()
        self.style.theme_use("clam")
        self.style.configure("TNotebook", background="#F1F5F9")
        self.style.configure("TNotebook.Tab", font=("Segoe UI", 10, "bold"), padding=[16, 6])
        self.style.configure("Treeview.Heading", font=("Segoe UI", 9, "bold"), background="#E2E8F0")
        self.style.configure("Treeview", font=("Segoe UI", 9), rowheight=26)
        self.style.configure("Primary.TButton", font=("Segoe UI", 9, "bold"), background="#2563EB", foreground="white")
        self.style.configure("Success.TButton", font=("Segoe UI", 9, "bold"), background="#10B981", foreground="white")
        self.style.configure("Danger.TButton", font=("Segoe UI", 9, "bold"), background="#EF4444", foreground="white")

    def _build_header(self):
        header_frame = tk.Frame(self.root, bg="#0F172A", height=60)
        header_frame.pack(fill="x", side="top")

        title_label = tk.Label(
            header_frame,
            text="LOGIC LEGEND  |  Face Recognition Attendance System",
            font=("Segoe UI", 14, "bold"),
            fg="#F8FAFC",
            bg="#0F172A"
        )
        title_label.pack(side="left", padx=20, pady=12)

        sub_label = tk.Label(
            header_frame,
            text="Biometric Verification with Consent & Duplicate Suppression",
            font=("Segoe UI", 9, "italic"),
            fg="#94A3B8",
            bg="#0F172A"
        )
        sub_label.pack(side="right", padx=20, pady=15)

    def _build_notebook(self):
        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(fill="both", expand=True, padx=10, pady=10)

        self.tab_scanner = ttk.Frame(self.notebook)
        self.tab_enrollment = ttk.Frame(self.notebook)
        self.tab_reports = ttk.Frame(self.notebook)
        self.tab_low_attendance = ttk.Frame(self.notebook)

        self.notebook.add(self.tab_scanner, text="📷 Live Attendance Scanner")
        self.notebook.add(self.tab_enrollment, text="👤 Student Enrollment")
        self.notebook.add(self.tab_reports, text="📋 Attendance Records & CSV")
        self.notebook.add(self.tab_low_attendance, text="⚠️ Low Attendance Alerts (<75%)")

        self._build_scanner_tab()
        self._build_enrollment_tab()
        self._build_reports_tab()
        self._build_low_attendance_tab()

    # ---------------- TAB 1: LIVE ATTENDANCE SCANNER ----------------
    def _build_scanner_tab(self):
        main_pane = tk.PanedWindow(self.tab_scanner, orient="horizontal", bg="#F1F5F9")
        main_pane.pack(fill="both", expand=True, padx=10, pady=10)

        # Left: Camera Feed Viewport
        left_frame = ttk.LabelFrame(main_pane, text="Webcam Live Video Feed", padding=10)
        main_pane.add(left_frame, width=540)

        self.canvas_cam = tk.Canvas(left_frame, bg="#1E293B", width=500, height=360)
        self.canvas_cam.pack(fill="both", expand=True)

        ctrl_bar = ttk.Frame(left_frame)
        ctrl_bar.pack(fill="x", pady=(10, 0))

        self.btn_cam_toggle = ttk.Button(ctrl_bar, text="▶ Start Webcam", style="Success.TButton", command=self._toggle_webcam)
        self.btn_cam_toggle.pack(side="left", padx=5)

        btn_recognize = ttk.Button(ctrl_bar, text="🔍 Capture & Mark", style="Primary.TButton", command=self._capture_and_mark)
        btn_recognize.pack(side="left", padx=5)

        btn_sim = ttk.Button(ctrl_bar, text="🖼️ Load Image / Demo Scan", command=self._simulate_scan)
        btn_sim.pack(side="left", padx=5)

        # Right: Recognition Card & Manual Fallback
        right_frame = ttk.LabelFrame(main_pane, text="Attendance Verification Status", padding=15)
        main_pane.add(right_frame, width=420)

        self.lbl_status_banner = tk.Label(
            right_frame,
            text="Ready to scan student face.",
            font=("Segoe UI", 10, "bold"),
            bg="#E2E8F0",
            fg="#1E293B",
            pady=8
        )
        self.lbl_status_banner.pack(fill="x", pady=(0, 15))

        # Student Details Card
        self.card_frame = tk.Frame(right_frame, bg="white", relief="ridge", bd=1, padx=15, pady=15)
        self.card_frame.pack(fill="x", pady=(0, 15))

        self.lbl_rec_name = tk.Label(self.card_frame, text="Name: --", font=("Segoe UI", 11, "bold"), bg="white", anchor="w")
        self.lbl_rec_name.pack(fill="x", pady=2)
        self.lbl_rec_roll = tk.Label(self.card_frame, text="Roll No: --", font=("Segoe UI", 10), fg="#475569", bg="white", anchor="w")
        self.lbl_rec_roll.pack(fill="x", pady=2)
        self.lbl_rec_dept = tk.Label(self.card_frame, text="Department: --", font=("Segoe UI", 10), fg="#475569", bg="white", anchor="w")
        self.lbl_rec_dept.pack(fill="x", pady=2)
        self.lbl_rec_conf = tk.Label(self.card_frame, text="Match Confidence: --", font=("Segoe UI", 10), fg="#059669", bg="white", anchor="w")
        self.lbl_rec_conf.pack(fill="x", pady=2)
        self.lbl_rec_time = tk.Label(self.card_frame, text="Timestamp: --", font=("Segoe UI", 9, "italic"), fg="#64748B", bg="white", anchor="w")
        self.lbl_rec_time.pack(fill="x", pady=2)

        # Manual Fallback Section (Privacy & Camera Failure alternative)
        manual_box = ttk.LabelFrame(right_frame, text="Manual Attendance Alternative (Privacy / Camera Error)", padding=10)
        manual_box.pack(fill="x")

        ttk.Label(manual_box, text="Select Student:").pack(anchor="w", pady=2)
        self.combo_manual_student = ttk.Combobox(manual_box, state="readonly")
        self.combo_manual_student.pack(fill="x", pady=(0, 8))

        btn_manual_mark = ttk.Button(manual_box, text="✍️ Mark Manual Attendance", command=self._action_manual_mark)
        btn_manual_mark.pack(fill="x")

    # ---------------- TAB 2: STUDENT ENROLLMENT ----------------
    def _build_enrollment_tab(self):
        box = ttk.LabelFrame(self.tab_enrollment, text="Enroll Authorized Student Face Profile", padding=20)
        box.pack(fill="both", expand=True, padx=20, pady=20)

        f = ttk.Frame(box)
        f.pack(fill="x", pady=5)
        ttk.Label(f, text="Roll Number *:", width=20).pack(side="left")
        self.ent_roll = ttk.Entry(f, width=28)
        self.ent_roll.pack(side="left", padx=5)

        f2 = ttk.Frame(box)
        f2.pack(fill="x", pady=5)
        ttk.Label(f2, text="Full Student Name *:", width=20).pack(side="left")
        self.ent_name = ttk.Entry(f2, width=28)
        self.ent_name.pack(side="left", padx=5)

        f3 = ttk.Frame(box)
        f3.pack(fill="x", pady=5)
        ttk.Label(f3, text="Department *:", width=20).pack(side="left")
        self.ent_dept = ttk.Combobox(f3, values=["Computer Science", "Information Technology", "Electronics & Comm", "Mechanical Eng", "Civil Eng"], width=26)
        self.ent_dept.pack(side="left", padx=5)

        f4 = ttk.Frame(box)
        f4.pack(fill="x", pady=5)
        ttk.Label(f4, text="Semester:", width=20).pack(side="left")
        self.ent_sem = ttk.Combobox(f4, values=[str(i) for i in range(1, 9)], width=10)
        self.ent_sem.set("6")
        self.ent_sem.pack(side="left", padx=5)

        # Mandatory Privacy Consent
        f_consent = ttk.Frame(box)
        f_consent.pack(fill="x", pady=(15, 10))
        self.var_consent = tk.BooleanVar(value=True)
        chk = ttk.Checkbutton(
            f_consent,
            text="Informed Consent Obtained: The student has consented to photographic biometric facial template storage.",
            variable=self.var_consent
        )
        chk.pack(anchor="w")

        f_actions = ttk.Frame(box)
        f_actions.pack(fill="x", pady=15)
        btn_capture = ttk.Button(f_actions, text="📸 Capture Face from Webcam / File", command=self._action_choose_face_photo)
        btn_capture.pack(side="left", padx=5)

        btn_save_student = ttk.Button(f_actions, text="💾 Complete Enrollment", style="Primary.TButton", command=self._action_enroll)
        btn_save_student.pack(side="left", padx=15)

        self.lbl_enrolled_preview = tk.Label(box, text="No face sample loaded yet.", fg="#64748B", bg="#F8FAFC", height=8, relief="solid", bd=1)
        self.lbl_enrolled_preview.pack(fill="x", pady=10)
        self.enrolled_face_bgr = None

    # ---------------- TAB 3: ATTENDANCE RECORDS & CSV ----------------
    def _build_reports_tab(self):
        filter_bar = ttk.Frame(self.tab_reports)
        filter_bar.pack(fill="x", padx=15, pady=10)

        ttk.Label(filter_bar, text="Filter Date (YYYY-MM-DD):").pack(side="left", padx=5)
        self.filter_date = ttk.Entry(filter_bar, width=12)
        self.filter_date.pack(side="left", padx=5)

        ttk.Label(filter_bar, text="Search:").pack(side="left", padx=5)
        self.filter_search = ttk.Entry(filter_bar, width=16)
        self.filter_search.pack(side="left", padx=5)

        btn_search = ttk.Button(filter_bar, text="Filter", command=self.refresh_attendance_table)
        btn_search.pack(side="left", padx=5)

        btn_clear = ttk.Button(filter_bar, text="Reset", command=self._reset_filters)
        btn_clear.pack(side="left", padx=5)

        btn_export = ttk.Button(filter_bar, text="📥 Export CSV", style="Primary.TButton", command=self._action_export_csv)
        btn_export.pack(side="right", padx=5)

        # Table
        cols = ("id", "date", "time", "session", "roll", "name", "dept", "conf", "mode")
        self.tree_att = ttk.Treeview(self.tab_reports, columns=cols, show="headings", selectmode="browse")

        self.tree_att.heading("id", text="ID")
        self.tree_att.heading("date", text="Date")
        self.tree_att.heading("time", text="Time")
        self.tree_att.heading("session", text="Session")
        self.tree_att.heading("roll", text="Roll Number")
        self.tree_att.heading("name", text="Student Name")
        self.tree_att.heading("dept", text="Department")
        self.tree_att.heading("conf", text="Confidence")
        self.tree_att.heading("mode", text="Verification Mode")

        self.tree_att.column("id", width=45, anchor="center")
        self.tree_att.column("date", width=95, anchor="center")
        self.tree_att.column("time", width=85, anchor="center")
        self.tree_att.column("session", width=120, anchor="center")
        self.tree_att.column("roll", width=120, anchor="center")
        self.tree_att.column("name", width=160, anchor="w")
        self.tree_att.column("dept", width=160, anchor="w")
        self.tree_att.column("conf", width=85, anchor="center")
        self.tree_att.column("mode", width=130, anchor="center")

        scroll = ttk.Scrollbar(self.tab_reports, orient="vertical", command=self.tree_att.yview)
        self.tree_att.configure(yscrollcommand=scroll.set)
        self.tree_att.pack(fill="both", expand=True, padx=15, pady=(0, 10))
        scroll.pack(side="right", fill="y")

    # ---------------- TAB 4: LOW ATTENDANCE ALERTS ----------------
    def _build_low_attendance_tab(self):
        banner = tk.Label(
            self.tab_low_attendance,
            text="Institutional Academic Compliance: Enforces minimum 75% attendance threshold.",
            font=("Segoe UI", 9, "bold"),
            bg="#FEF3C7",
            fg="#92400E",
            pady=8
        )
        banner.pack(fill="x", padx=15, pady=(10, 5))

        cols = ("roll", "name", "dept", "attended", "total", "pct", "status")
        self.tree_low = ttk.Treeview(self.tab_low_attendance, columns=cols, show="headings")

        self.tree_low.heading("roll", text="Roll Number")
        self.tree_low.heading("name", text="Student Name")
        self.tree_low.heading("dept", text="Department")
        self.tree_low.heading("attended", text="Attended")
        self.tree_low.heading("total", text="Total Sessions")
        self.tree_low.heading("pct", text="Attendance %")
        self.tree_low.heading("status", text="Compliance Status")

        for col in cols:
            self.tree_low.column(col, anchor="center")
        self.tree_low.column("name", anchor="w", width=180)

        self.tree_low.pack(fill="both", expand=True, padx=15, pady=10)

    # ---------------- ACTIONS & EVENT HANDLERS ----------------
    def _toggle_webcam(self):
        if not self.camera_running:
            self.cap = cv2.VideoCapture(0)
            if not self.cap.isOpened():
                messagebox.showerror(
                    "Camera Unavailable",
                    "No physical webcam detected or access permission was denied.\n\n"
                    "Please use 'Load Image / Demo Scan' or the 'Manual Attendance Alternative' to proceed."
                )
                self.cap = None
                return
            self.camera_running = True
            self.btn_cam_toggle.config(text="⏹ Stop Webcam", style="Danger.TButton")
            self._update_webcam_loop()
        else:
            self.camera_running = False
            if self.cap:
                self.cap.release()
                self.cap = None
            self.btn_cam_toggle.config(text="▶ Start Webcam", style="Success.TButton")
            self.canvas_cam.delete("all")

    def _update_webcam_loop(self):
        if self.camera_running and self.cap:
            ret, frame = self.cap.read()
            if ret:
                self.current_frame = frame
                # Draw face rectangles on display
                display_frame = frame.copy()
                faces = self.service.face_engine.detect_faces(frame)
                for (x, y, w, h) in faces:
                    cv2.rectangle(display_frame, (x, y), (x+w, y+h), (0, 255, 0), 2)

                # Render to Tkinter canvas
                rgb = cv2.cvtColor(display_frame, cv2.COLOR_BGR2RGB)
                img = Image.fromarray(rgb)
                img = img.resize((500, 360), Image.Resampling.LANCZOS)
                self.photo_tk = ImageTk.PhotoImage(image=img)
                self.canvas_cam.create_image(0, 0, image=self.photo_tk, anchor="nw")

            self.root.after(30, self._update_webcam_loop)

    def _capture_and_mark(self):
        if self.current_frame is None:
            messagebox.showwarning("No Camera Feed", "Webcam is not running or no frame captured.")
            return

        res = self.service.process_frame_for_attendance(self.current_frame)
        self._display_scan_result(res)

    def _simulate_scan(self):
        """Allows testing facial recognition without hardware webcam."""
        # Use synthetic enrolled student face or load from disk
        path = filedialog.askopenfilename(filetypes=[("Image files", "*.jpg;*.png;*.jpeg")])
        if path:
            frame = cv2.imread(path)
        else:
            # Generate synthetic face
            frame = self.service.face_engine.generate_synthetic_face()

        self.current_frame = frame
        # Display image
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        img = Image.fromarray(rgb)
        img = img.resize((500, 360), Image.Resampling.LANCZOS)
        self.photo_tk = ImageTk.PhotoImage(image=img)
        self.canvas_cam.create_image(0, 0, image=self.photo_tk, anchor="nw")

        res = self.service.process_frame_for_attendance(frame)
        self._display_scan_result(res)

    def _display_scan_result(self, res):
        status = res.get("status")
        msg = res.get("message", "")

        if status == "SUCCESS":
            st = res.get("student", {})
            self.lbl_status_banner.config(text=f"✅ {msg}", bg="#DCFCE7", fg="#166534")
            self.lbl_rec_name.config(text=f"Name: {st.get('name')}")
            self.lbl_rec_roll.config(text=f"Roll No: {st.get('roll_number')}")
            self.lbl_rec_dept.config(text=f"Department: {st.get('department')}")
            self.lbl_rec_conf.config(text=f"Match Confidence: {int(res.get('confidence', 1.0)*100)} %")
            self.lbl_rec_time.config(text=f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
            self.refresh_attendance_table()
            self.refresh_low_attendance_table()
        elif status == "DUPLICATE":
            self.lbl_status_banner.config(text=f"⚠️ {msg}", bg="#FEF3C7", fg="#92400E")
        else:
            self.lbl_status_banner.config(text=f"❌ {msg}", bg="#FEE2E2", fg="#991B1B")

    def _action_manual_mark(self):
        selected_text = self.combo_manual_student.get()
        if not selected_text:
            messagebox.showwarning("Select Student", "Please select a student from the list.")
            return

        student_id = int(selected_text.split(" - ")[0])
        success, msg = self.service.mark_attendance(student_id, mode="MANUAL", confidence=1.0)
        if success:
            messagebox.showinfo("Success", msg)
            self.refresh_attendance_table()
            self.refresh_low_attendance_table()
        else:
            messagebox.showwarning("Attendance Notice", msg)

    def _action_choose_face_photo(self):
        path = filedialog.askopenfilename(filetypes=[("Image files", "*.jpg;*.png;*.jpeg")])
        if path:
            self.enrolled_face_bgr = cv2.imread(path)
            self.lbl_enrolled_preview.config(text=f"Face photo loaded from: {os.path.basename(path)}")
        else:
            self.enrolled_face_bgr = self.service.face_engine.generate_synthetic_face()
            self.lbl_enrolled_preview.config(text="Generated synthetic facial template.")

    def _action_enroll(self):
        roll = self.ent_roll.get().strip()
        name = self.ent_name.get().strip()
        dept = self.ent_dept.get().strip()
        sem = self.ent_sem.get().strip()
        consent = self.var_consent.get()

        if self.enrolled_face_bgr is None:
            self.enrolled_face_bgr = self.service.face_engine.generate_synthetic_face()

        success, msg = self.service.enroll_student(roll, name, dept, sem, consent, self.enrolled_face_bgr)
        if success:
            messagebox.showinfo("Enrollment Success", msg)
            self.ent_roll.delete(0, tk.END)
            self.ent_name.delete(0, tk.END)
            self.enrolled_face_bgr = None
            self.lbl_enrolled_preview.config(text="No face sample loaded yet.")
            self.refresh_all_views()
        else:
            messagebox.showerror("Enrollment Error", msg)

    def _action_export_csv(self):
        path = filedialog.asksaveasfilename(defaultextension=".csv", filetypes=[("CSV file", "*.csv")])
        if path:
            success, res = self.service.export_csv(path)
            if success:
                messagebox.showinfo("Export Successful", f"Saved to:\n{res}")
            else:
                messagebox.showerror("Export Failed", res)

    def _reset_filters(self):
        self.filter_date.delete(0, tk.END)
        self.filter_search.delete(0, tk.END)
        self.refresh_attendance_table()

    def refresh_all_views(self):
        # Refresh manual dropdown
        students = self.service.get_all_students()
        student_choices = [f"{s['id']} - {s['roll_number']} ({s['name']})" for s in students]
        self.combo_manual_student["values"] = student_choices
        if student_choices:
            self.combo_manual_student.set(student_choices[0])

        self.refresh_attendance_table()
        self.refresh_low_attendance_table()

    def refresh_attendance_table(self):
        for row in self.tree_att.get_children():
            self.tree_att.delete(row)

        d_str = self.filter_date.get().strip() or None
        kw = self.filter_search.get().strip() or None

        records = self.service.get_attendance_records(date_str=d_str, search_keyword=kw)
        for r in records:
            conf_str = f"{int(r['confidence'] * 100)}%" if r['confidence'] else "100%"
            self.tree_att.insert("", "end", values=(
                r["id"], r["date"], r["time"], r["session_name"],
                r["roll_number"], r["name"], r["department"], conf_str, r["mode"]
            ))

    def refresh_low_attendance_table(self):
        for row in self.tree_low.get_children():
            self.tree_low.delete(row)

        stats = self.service.calculate_attendance_percentages()
        for s in stats:
            status = "⚠️ LOW ATTENDANCE" if s["low_attendance"] else "✅ COMPLIANT"
            self.tree_low.insert("", "end", values=(
                s["roll_number"], s["name"], s["department"],
                s["attended"], s["total_sessions"], f"{s['percentage']} %", status
            ))

