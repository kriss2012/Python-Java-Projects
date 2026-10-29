# Viva Presentation Slides: JV-02 Hospital Management System

---

### Slide 1: Title Slide
- **Project Title**: Hospital Management System with RBAC & Scheduling
- **Project Code**: JV-02 (Logic Legend Minor Project Suite)
- **Domain**: Healthcare Informatics, Enterprise Resource Planning (ERP) & Desktop GUI
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Hospitals deal with high daily volumes of outpatient consultations, emergency admissions, and billing workflows.
- Manual registers or disconnected software cause appointment double-bookings, misplaced medical histories, billing discrepancies, and unauthorized staff access.
- A centralized, role-secured system is essential for smooth clinical operations.

---

### Slide 3: Proposed Solution
- A complete Java 21 desktop application built using Swing, JDBC, SQLite, and MySQL.
- Role-Based Access Control (Admin, Doctor, Receptionist).
- Electronic patient records with complete medical and allergy history.
- Multi-specialty doctor directory with consultation fee tracking.
- Automated appointment conflict detection preventing overlapping doctor time slots.
- Itemized medical billing engine with automated tax calculation.

---

### Slide 4: System Architecture
- **Layer 1: User Interface**: Java Swing multi-tab clinical dashboard + Headless CLI.
- **Layer 2: Service Layer**: `HospitalService` managing RBAC, conflict detection algorithms, and invoicing logic.
- **Layer 3: Security Layer**: `SecurityUtils` handling salted SHA-256 password hashing.
- **Layer 4: Data Persistence**: `DatabaseManager` providing dual-engine JDBC support (embedded SQLite + MySQL).

---

### Slide 5: Role-Based Access Control (RBAC)
- **Administrator**:
  - Full system management.
  - Adding and configuring doctors, OPD rooms, and consultation fees.
  - Audit oversight of financial billing records.
- **Doctor**:
  - View assigned patient appointments.
  - Review medical history and symptoms.
  - Mark appointments as completed.
- **Receptionist**:
  - Patient onboarding and search.
  - Scheduling appointments.
  - Outpatient fee collection and invoice generation.

---

### Slide 6: Appointment Conflict Prevention
- Double-booking a doctor causes patient delays and clinical chaos.
- **Two-Tier Conflict Protection**:
  1. Service-Level Validation: `HospitalService.scheduleAppointment()` inspects existing active bookings for `(doctorId, date, timeSlot)`.
  2. Database Constraint: SQLite partial unique index:
     ```sql
     CREATE UNIQUE INDEX uq_active_slot 
     ON appointments(doctor_id, appointment_date, time_slot) 
     WHERE status != 'CANCELLED';
     ```
  3. Cancelling an appointment automatically frees up the time slot for new bookings.

---

### Slide 7: Medical Billing & Invoicing Engine
- Automated financial calculations:
  - Doctor Consultation Fee + Pharmacy + Lab Tests + Room Charges = Subtotal.
  - Subtotal $\times (\text{GST Rate} / 100) = \text{Tax}$.
  - Subtotal $+$ Tax $=$ Total Amount Payable.
- Generates clean, professional printable medical receipts with unique invoice numbers (`INV-XXXXXX`).

---

### Slide 8: Live Demonstration Highlights
- Logging in as Receptionist (`rec_anita` / `Rec@123`).
- Registering a new patient with medical notes.
- Booking an appointment with Dr. Rajesh Sharma (Cardiology) at 10:00 AM.
- Attempting to book a second patient for the exact same slot -> Demonstrating the **Instant Conflict Warning**.
- Generating an itemized medical bill and viewing the printed invoice receipt.

---

### Slide 9: Automated Verification & Testing
- 29/29 Automated unit test assertions passing:
  - User registration, salted SHA-256 hashing, and role checks.
  - Authentication rejection on incorrect passwords.
  - Doctor onboarding and fee tracking.
  - Patient registration and search by name/code.
  - Appointment booking and reference generation.
  - Conflict detection: duplicate slot throws `IllegalStateException`.
  - Re-booking after appointment cancellation.
  - Medical billing itemization and GST tax calculation.
  - Invoice text receipt format validation.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: A production-ready, reliable, and secure hospital management application.
- **Future Scope**:
  - Telemedicine WebRTC video consultation integration.
  - Automated SMS/WhatsApp appointment confirmation alerts.
  - Pharmacy inventory stock deduction upon invoice generation.

