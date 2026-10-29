# EVALUATOR DEMONSTRATION GUIDE
## PY-03: Face Recognition Attendance System
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-03-Face-Attendance
python src/main.py
```
*The graphical application opens initialized with pre-seeded student profiles and attendance records.*

---

## 2. Recommended Demonstration Flow

### Step 1: Demonstrate Facial Attendance Scanning
- Stay on the **Live Attendance Scanner** tab.
- If a webcam is connected: Click **▶ Start Webcam**, face the camera, and click **🔍 Capture & Mark**.
- If a webcam is **not** connected (or in an evaluation lab):
  - Click **🖼️ Load Image / Demo Scan**.
  - A synthetic biometric sample is rendered on the canvas.
  - Notice the result:
    - Status Banner: Green confirmation banner appears (`✅ Attendance marked for Aarav Sharma`).
    - Recognition Card: Displays student name (`Aarav Sharma`), roll number (`CSE-2026-001`), department (`Computer Science`), confidence (`92%`), and timestamp.

### Step 2: Demonstrate Duplicate Check-in Suppression
- Immediately click **🔍 Capture & Mark** or **🖼️ Load Image / Demo Scan** a second time.
- Point out:
  - The system detects that Aarav Sharma has **already** checked in today for the `Morning Lecture`.
  - The status banner switches to **YELLOW**:
    `⚠️ Duplicate: Attendance already recorded today at HH:MM:SS (FACE_RECOGNITION)`.
  - No duplicate rows are written to the database.

### Step 3: Demonstrate Manual Attendance Alternative (Privacy & Camera Failure)
- Look at the right panel under **Manual Attendance Alternative**.
- Select student: `4 - CSE-2026-004 (Ananya Iyer)`.
- Click **✍️ Mark Manual Attendance**.
- A confirmation pops up: `Attendance marked for Ananya Iyer at HH:MM:SS`.
- Point out that this satisfies institutional privacy guidelines for students who opt out of facial scanning or when camera hardware malfunctions.

### Step 4: Inspect Attendance Records & CSV Export
- Switch to the **Attendance Records & CSV** tab.
- Observe the table:
  - Both `FACE_RECOGNITION` and `MANUAL` records appear chronologically with timestamps.
  - Point out search and date filtering controls.
- Click **📥 Export CSV**. Save the file and inspect it to demonstrate clean spreadsheet integration.

### Step 5: Demonstrate Low Attendance Alerts (< 75%)
- Switch to the **Low Attendance Alerts (<75%)** tab.
- Highlight the compliance table:
  - `Aarav Sharma`, `Diya Patel`, `Rohan Mehta`: Display `100.0%` with `✅ COMPLIANT`.
  - `Ananya Iyer`: Displays `40.0%` with `⚠️ LOW ATTENDANCE`.
  - `Vikram Singh`: Displays `20.0%` with `⚠️ LOW ATTENDANCE`.
- Explain how this automated calculation allows course coordinators to immediately identify students requiring academic warnings.

### Step 6: Demonstrate Informed Consent Enforcement
- Switch to the **Student Enrollment** tab.
- Enter:
  - Roll Number: `CSE-2026-099`
  - Name: `Test Student`
  - Department: `Computer Science`
- **Uncheck** the informed consent checkbox.
- Click **Complete Enrollment**.
- A dialog pops up: `Enrollment error: Informed biometric consent is strictly mandatory before enrollment.`
- Check the box, click again, and demonstrate successful enrollment!

---

## 3. Automated Unit Testing
Run the automated test suite in your terminal:
```powershell
python tests/test_attendance.py
```
*All 6 unit tests pass, verifying consent validation, duplicate suppression, manual fallback, low-attendance threshold calculation, and CSV generation.*
