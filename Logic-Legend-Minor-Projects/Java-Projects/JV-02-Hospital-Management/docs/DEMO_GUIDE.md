# Evaluator Live Demonstration Guide: JV-02 Hospital Management System

This guide provides a rapid step-by-step walkthrough for faculty evaluators, viva examiners, and project mentors to compile, run, and test the JV-02 hospital management application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Compile Java Sources | Clean build with `javac` into `bin/` | [ ] |
| 2 | Run Unit Tests | 29 assertions pass in < 0.2s (`TEST SUMMARY: 29 Passed, 0 Failed`) | [ ] |
| 3 | Login with Role | Authenticate as Admin (`admin`), Doctor, or Receptionist | [ ] |
| 4 | Patient Registration | Register a new patient and verify search | [ ] |
| 5 | Test Conflict Detection | Booking same doctor, date & time slot throws conflict warning | [ ] |
| 6 | Generate Medical Bill | Compute itemized fees, taxes, and view printable invoice | [ ] |

---

## 2. Compilation and Test Execution (Fastest & 100% Deterministic)

### Step 1: Compile Application and Test Suite
```bash
cd Logic-Legend-Minor-Projects/Java-Projects/JV-02-Hospital-Management
New-Item -ItemType Directory -Force -Path "bin"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\hospital\model\*.java src\com\logiclegend\hospital\dao\*.java src\com\logiclegend\hospital\service\*.java src\com\logiclegend\hospital\ui\*.java src\com\logiclegend\hospital\*.java tests\*.java
```

### Step 2: Run Automated Regression Test Suite
```bash
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
**Verification Note**: Look for:
```
-----------------------------------------------------------------
TEST SUMMARY: 29 Passed, 0 Failed (Total: 29)
=================================================================
```

---

## 3. Command Line Interface Demonstration (CLI Mode)

### Step 1: Launch Interactive Hospital Terminal
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.hospital.Main --cli
```

### Step 2: Test Hospital Workflows
1. **Login with Receptionist Credentials**:
   - Username: `rec_anita`
   - Password: `Rec@123`
   - Observe login welcome message: `Welcome, Anita Deshmukh (Role: RECEPTIONIST)!`
2. **View Patients (Option 1)**:
   - Observe pre-seeded patients: *Amit Verma*, *Sunita Pillai*, *Rohan Gupta*.
3. **Register New Patient (Option 2)**:
   - Name: `Manish Tiwari`
   - Age: `45`
   - Gender: `M`
   - Blood Group: `O+`
   - Phone: `9876543210`
   - Medical Notes: `Mild asthma`
   - Observe patient code generation (`PAT-XXXX`).
4. **View Doctors Directory (Option 3)**:
   - Observe Dr. Rajesh Sharma (Cardiology, ₹800), Dr. Kavita Menon (Neurology, ₹1,200), Dr. Suresh Rao (Orthopedics, ₹650).
5. **Test Appointment Conflict Detection (Option 5)**:
   - Enter Patient ID: `1`
   - Enter Doctor ID: `1` (Dr. Rajesh Sharma)
   - Date: `2026-11-05`
   - Time Slot: `10:00 AM` (Already booked during demo seed!)
   - Observe the **CONCURRENT BOOKING CONFLICT** rejection message!
   - Now try with time slot `02:00 PM` -> Appointment booked successfully!
6. **Generate Medical Bill (Option 7)**:
   - Patient ID: `1`
   - Doctor ID: `1`
   - Pharmacy Charges: `400`
   - Lab Charges: `1200`
   - Room Charges: `0`
   - Tax Rate: `5`
   - Payment Method: `CREDIT_CARD`
   - Observe formatted printable medical invoice output with total payable.
7. **Logout (Option 8)**.

---

## 4. Graphical Interface Demonstration (Swing GUI)

### Step 1: Launch Application
```bash
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.hospital.Main
```

### Step 2: Inspect GUI Elements
1. **Login Screen**: Enter username (`admin`) and password (`Admin@123`).
2. **Patients Tab**: Use search bar to filter patients by name or phone; click **+ Register New Patient** to open the registration dialog.
3. **Doctors Tab**: View doctor cards and OPD consultation fees.
4. **Appointments Tab**: Click **📅 Book New Appointment** to test doctor and time slot selection.
5. **Billing Tab**: Click **📄 View / Print Selected Invoice** to display the formatted patient discharge receipt.

---

## 5. Evaluator Viva Questions to Ask the Student
1. *"How does the system ensure a doctor cannot be scheduled for two patients at the same time?"*
   - Student should explain the conflict check query in `HospitalService.scheduleAppointment()` and the partial unique index on `appointments(doctor_id, appointment_date, time_slot)` where `status != 'CANCELLED'`.
2. *"Can a receptionist add a new doctor or modify doctor consultation fees?"*
   - Student should explain Role-Based Access Control (RBAC) and how `currentUser.isAdmin()` blocks non-admin users from creating doctors.
3. *"How are patient medical bills calculated?"*
   - Student should explain the consultation fee auto-lookup plus pharmacy, lab, room charges, and percentage GST addition in `HospitalService.createBill()`.

