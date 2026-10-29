# Logic Legend — JV-02: Hospital Management System

A comprehensive, role-based desktop healthcare administration and clinical management system built using Java 21, Swing, JDBC, SQLite, and MySQL. The system provides Role-Based Access Control (Admin, Doctor, Receptionist), electronic patient registration and medical history records, doctor scheduling directories, appointment booking with concurrent time-slot conflict prevention, and itemized medical billing with tax calculations and invoice receipts.

---

## 1. Project Title and Identifier
- **Project Name**: Hospital Management System
- **Project Code**: `JV-02`
- **Application Category**: Healthcare Informatics, Enterprise Resource Planning (ERP) & Desktop GUI
- **Target Audience**: Hospital administrators, medical practitioners, computer science students, and faculty evaluators.

---

## 2. Executive Summary
Modern hospitals handle hundreds of patient admissions, multi-specialty clinical consultations, and pharmacy/diagnostic bills every day. Manual record-keeping or loosely coupled spreadsheets cause double-booked doctor appointments, billing discrepancies, misplaced patient histories, and unauthorized staff access. The **Hospital Management System** provides an integrated, automated software solution with Role-Based Access Control (RBAC), atomic database storage, automated conflict detection on doctor scheduling slots, and automated medical invoicing with GST/tax computation.

---

## 3. Key Features
- **Role-Based Access Control (RBAC)**:
  - **Administrator**: Full administrative oversight, doctor onboarding, staff accounts, system-wide billing audits.
  - **Doctor**: View specialized clinical appointments, review patient medical histories, record symptoms.
  - **Receptionist**: Patient registration, scheduling appointments, outpatient fee collection, and invoice printing.
- **Patient Profile Management**: Stores full demographics (patient code, age, gender, blood group, address, contact) and persistent medical history.
- **Doctor Directory & OPD Scheduling**: Tracks qualifications, medical specializations (Cardiology, Neurology, Orthopedics, Pediatrics), room/OPD numbers, and consultation fees.
- **Intelligent Appointment Conflict Prevention**: Enforces database-level partial unique index constraints (`doctor_id`, `appointment_date`, `time_slot`) preventing overlapping bookings for the same doctor, while allowing re-booking if an earlier appointment was cancelled.
- **Automated Medical Billing & Invoicing**: Computes consolidated medical invoices incorporating doctor consultation fees, pharmacy/prescription charges, laboratory test fees, room/bed charges, and configurable percentage-based GST/tax.
- **Printable Medical Invoices**: Formats clean, itemized hospital invoices suitable for patient discharge and insurance claims.
- **Dual Interface Modes**: Modern multi-tab Java Swing GUI with split dialogs alongside an interactive command-line terminal interface for headless servers.
- **Zero-Configuration Portability**: Embedded SQLite database (`sqlite-jdbc.jar`) works instantly out of the box with zero external configuration, while also providing complete MySQL production deployment scripts.

---

## 4. System Architecture
The application adheres to a modular four-layer architecture:
1. **Presentation Layer (`ui/HospitalFrame.java`, `Main.java`)**: Java Swing GUI with tabbed navigation and interactive CLI.
2. **Business Service Layer (`service/HospitalService.java`, `service/SecurityUtils.java`)**: Authentication, appointment conflict logic, billing mathematics, and seed generators.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: Manages JDBC connections and automated schema initialization.
4. **Data Persistence Layer (`model/`, SQLite / MySQL)**: Relational schema covering `users`, `doctors`, `patients`, `appointments`, and `billings`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **GUI Framework**: Java Swing & AWT (Standard JDK)
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Embedded via self-contained `sqlite-jdbc.jar`)
- **Production Database**: MySQL 8.x (Supported via `sql/schema_mysql.sql`)
- **Security**: SHA-256 password hashing with CSPRNG salt generation

---

## 6. Directory Structure
```
JV-02-Hospital-Management/
├── .env.example              # Configuration template for database settings
├── .gitignore                # Git exclusions for compiled classes and databases
├── pom.xml                   # Maven project descriptor
├── README.md                 # 19-section comprehensive documentation
├── lib/
│   └── sqlite-jdbc.jar       # Self-contained zero-dependency SQLite JDBC driver
├── data/                     # Local SQLite database storage directory
├── exports/                  # Exported medical invoices and reports
├── sql/
│   ├── schema_sqlite.sql     # SQLite relational table definitions
│   └── schema_mysql.sql      # MySQL production database schema
├── docs/                     # Academic deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   └── com/logiclegend/hospital/
│       ├── model/
│       │   ├── User.java        # User authentication entity
│       │   ├── Doctor.java      # Doctor directory entity
│       │   ├── Patient.java     # Patient medical record entity
│       │   ├── Appointment.java # Appointment entity with conflict tracking
│       │   └── Billing.java     # Invoicing entity
│       ├── dao/
│       │   └── DatabaseManager.java # Dual SQLite / MySQL connection manager
│       ├── service/
│       │   ├── SecurityUtils.java  # CSPRNG salt & SHA-256 hashing
│       │   └── HospitalService.java# Clinical rules, appointments & billing
│       ├── ui/
│       │   └── HospitalFrame.java  # Java Swing GUI with multi-tab dashboard
│       └── Main.java               # Application entry point (GUI + CLI)
└── tests/
    └── TestRunner.java       # Automated standalone unit test suite (29 assertions)
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Java-Projects/JV-02-Hospital-Management
   ```
2. Compile all source and test files using JDK 21:
   ```bash
   New-Item -ItemType Directory -Force -Path "bin"
   javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\hospital\model\*.java src\com\logiclegend\hospital\dao\*.java src\com\logiclegend\hospital\service\*.java src\com\logiclegend\hospital\ui\*.java src\com\logiclegend\hospital\*.java tests\*.java
   ```

---

## 8. Usage Instructions
### Running the Graphical Application (Swing GUI)
Launch the hospital portal:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.hospital.Main
```
- Pre-seeded Demo Logins:
  - **Administrator**: Username: `admin`, Password: `Admin@123`
  - **Doctor**: Username: `dr_sharma`, Password: `Doc@123`
  - **Receptionist**: Username: `rec_anita`, Password: `Rec@123`

### Running in Headless / CLI Mode
Launch the interactive terminal interface:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.hospital.Main --cli
```

---

## 9. Database Design and Schema
- **`users`**: `id`, `username` (UNIQUE), `password_hash`, `password_salt`, `full_name`, `role` (`ADMIN`/`DOCTOR`/`RECEPTIONIST`), `created_at`.
- **`doctors`**: `id`, `doctor_code` (UNIQUE), `name`, `specialization`, `qualification`, `consultation_fee`, `phone`, `email`, `room_number`, `status`.
- **`patients`**: `id`, `patient_code` (UNIQUE), `name`, `age`, `gender`, `blood_group`, `phone`, `email`, `address`, `medical_history`, `created_at`.
- **`appointments`**: `id`, `appointment_ref` (UNIQUE), `patient_id` (FK), `doctor_id` (FK), `appointment_date`, `time_slot`, `status` (`SCHEDULED`/`COMPLETED`/`CANCELLED`), `symptoms`.
  - Enforced index: `UNIQUE(doctor_id, appointment_date, time_slot) WHERE status != 'CANCELLED'`.
- **`billings`**: `id`, `invoice_number` (UNIQUE), `patient_id` (FK), `doctor_id` (FK), `consultation_fee`, `pharmacy_charges`, `lab_charges`, `room_charges`, `tax_amount`, `total_amount`, `payment_status`, `payment_method`.

---

## 10. Appointment Conflict Prevention Architecture
To ensure zero double-booking:
1. `HospitalService.scheduleAppointment()` executes a query for existing active appointments:
   ```sql
   SELECT id FROM appointments 
   WHERE doctor_id = ? AND appointment_date = ? AND time_slot = ? AND status != 'CANCELLED'
   ```
2. If an active record exists, an `IllegalStateException` is thrown immediately, preventing the UI from submitting overlapping bookings.
3. If an appointment is marked `CANCELLED`, the partial unique index frees the slot for other patients.

---

## 11. Testing and Verification
Run the automated unit test suite:
```bash
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
**29/29 Unit Test Assertions Passing**:
- ✅ User registration, SHA-256 password hashing with salt, and role assignment
- ✅ Rejection of invalid credentials with `SecurityException`
- ✅ Doctor directory onboarding, code generation, and fee validation
- ✅ Patient profile registration and search by name/code
- ✅ Appointment booking and reference code generation
- ✅ Double-booking prevention: conflicting appointment slot throws `IllegalStateException`
- ✅ Appointment cancellation allowing slot re-booking
- ✅ Itemized medical billing calculation (Consultation + Pharmacy + Lab + Room + Tax)
- ✅ Printable invoice text format integrity

---

## 12. Security and Error Handling
- **Password Salting**: Every user account has a distinct 16-byte random salt to prevent pre-computed dictionary attacks.
- **SQL Injection Prevention**: All queries use `PreparedStatement` with parameterized placeholders.
- **Role Isolation**: Doctor creation is restricted to users with `ADMIN` privileges.
- **Database Validation**: Check constraints prevent negative fees, invalid age, and illegal gender strings.

---

## 13. Academic Relevance and College Submission
This project satisfies curriculum criteria for:
- Enterprise Software Architecture in Java
- Relational Database Management Systems (Constraints, Foreign Keys, Partial Indexes)
- Role-Based Access Control (RBAC) & Information Security
- User Interface Design with Java Swing (Tabs, Modals, Tables)

---

## 14. Known Limitations
- PACS (Picture Archiving and Communication System) for high-resolution DICOM X-ray image storage is omitted in favor of text notes.
- Integrated payment gateway (Razorpay/Stripe) is simulated via payment method selection.

---

## 15. Future Enhancements
- Integration of SMS gateway for automated appointment reminder alerts to patients.
- Telemedicine video consultation link generator via WebRTC.
- Barcode/QR code scanner integration for fast patient check-in at reception.

---

## 16. Evaluator Quick Start
```bash
# 1. Compile
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\hospital\model\*.java src\com\logiclegend\hospital\dao\*.java src\com\logiclegend\hospital\service\*.java src\com\logiclegend\hospital\ui\*.java src\com\logiclegend\hospital\*.java tests\*.java

# 2. Run automated tests
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner

# 3. Launch Swing GUI
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.hospital.Main
```

---

## 17. References
- Oracle Java 21 Documentation: `docs.oracle.com/en/java/javase/21/`
- SQLite JDBC Documentation: `github.com/xerial/sqlite-jdbc`
- Health Level Seven (HL7) Healthcare Standards Overview

---

## 18. License
Educational Open-Source License — Free for academic, learning, and college submission use.

