# PY-03: Face Recognition Attendance System
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-03`
- **Project Title**: Face Recognition Attendance System
- **Domain**: Biometric Computer Vision, Campus Automation & Data Ethics
- **Platform**: Desktop GUI (Python Tkinter) & Embedded SQLite

---

## 2. Project Overview
The Face Recognition Attendance System is an autonomous computer vision desktop application that identifies registered students through a camera feed and automatically records timestamped attendance while preventing duplicate check-ins, enforcing informed consent, and calculating academic threshold warnings (< 75%).

---

## 3. Problem Statement
Manual attendance rolls waste between 10 to 15 minutes of instructional lecture time per session, are vulnerable to buddy punching (proxy attendance), and create significant paperwork. Conversely, poorly implemented facial recognition tools lack data privacy safeguards and fail to provide manual fallbacks when camera hardware fails.

---

## 4. Objectives
- Perform real-time face detection using OpenCV Haar Cascades.
- Extract robust, illumination-normalized facial signatures for enrollment and matching.
- Prevent duplicate attendance entries for the same student within the same lecture session.
- Enforce mandatory informed consent before biometric registration.
- Provide a full manual attendance alternative for privacy or hardware failure scenarios.
- Generate low-attendance alert reports (< 75% attendance) for academic monitoring.

---

## 5. Key Features
- **Live Biometric Capture**: OpenCV webcam feed with face bounding-box overlays.
- **Duplicate Suppression**: Database constraints prevent multiple submissions per session.
- **Privacy First**: Explicit biometric consent tracking; manual alternative for non-consenting students.
- **Camera Fallback / Simulation**: Offline demo mode allowing image loading when webcams are absent.
- **Defaulter Alerts**: Automatic calculation of cumulative attendance and flagging of at-risk students (< 75%).
- **Audit Reports**: Filterable attendance logs with one-click CSV export.

---

## 6. Technology Stack
- **Programming Language**: Python 3.10+
- **Computer Vision**: OpenCV (`cv2`) & Pillow (`PIL`)
- **GUI**: Tkinter & TTK
- **Database**: SQLite3
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Webcam**: Standard USB or integrated 720p webcam (or use built-in image simulation mode).
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 150 MB free space.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-03-Face-Attendance

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Database Setup
The SQLite database (`data/attendance.db`) is automatically initialized and seeded with sample student rosters and attendance histories upon launch.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
DB_PATH=data/attendance.db
CONFIDENCE_THRESHOLD=0.60
ATTENDANCE_WARNING_THRESHOLD=75.0
DEFAULT_SESSION=Morning Lecture
CAMERA_INDEX=0
```

---

## 11. Commands to Run the Application
```powershell
# Launch Desktop GUI Application
python src/main.py

# Run in Headless CLI Summary Mode
python src/main.py --cli

# Re-seed Initial Demonstration Data
python src/main.py --seed

# Export Attendance Logs to CSV
python src/main.py --export
```

---

## 12. Demo Credentials
Local administrative rights are enabled by default for authorized classroom proctors. Default administrative account: `admin` / `admin123`.

---

## 13. Screenshots and Interface Description
1. **Live Attendance Scanner**: Dual pane featuring live video feed with bounding boxes, verification card, duplicate warning banner, and manual mark fallback.
2. **Student Enrollment**: Registration form with informed consent checkbox and photo capture.
3. **Attendance Records & CSV**: Searchable log table with export controls.
4. **Low Attendance Alerts**: Real-time table calculating attended vs conducted sessions, percentage, and compliance status.

---

## 14. API Documentation
Key methods in `AttendanceService`:
- `enroll_student(roll_number, name, department, semester, consent_given, face_bgr)`
- `mark_attendance(student_id, session_name, mode, confidence)`
- `process_frame_for_attendance(frame_bgr, session_name)`
- `calculate_attendance_percentages(warning_threshold=75.0)`
- `export_csv(filepath, date_str)`

---

## 15. Test Instructions
```powershell
python tests/test_attendance.py
```
*All 6 unit tests verify mandatory biometric consent rejection, duplicate submission suppression, manual marking, low-attendance thresholds, and CSV export.*

---

## 16. Known Limitations
- Extreme lighting variations (backlight, complete darkness) reduce matching confidence.
- Deep occlusions (scarves, medical masks) require manual verification.

---

## 17. Future Enhancements
- Infrared liveness detection to prevent 2D printed photograph spoofing.
- Cloud ERP integration for SMS alerts to parents regarding low attendance.

---

## 18. Troubleshooting
- **Camera Unavailable Error**: Displayed when no camera hardware is present. Use the **Load Image / Demo Scan** button or the **Manual Attendance Alternative** to test without physical hardware.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines for academic project evaluation.
