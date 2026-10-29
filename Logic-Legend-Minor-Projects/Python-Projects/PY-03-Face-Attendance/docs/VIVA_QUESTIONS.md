# VIVA VOCE PREPARATION & QUESTIONS
## PY-03: Face Recognition Attendance System
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the primary operational workflow of this attendance system?
**Answer**: The system ingests frames from a video stream, converts them to grayscale, runs an OpenCV Haar Cascade classifier to isolate bounding boxes of human faces, extracts equalized template/histogram signatures, and matches them against pre-enrolled student signatures. Upon a match exceeding the confidence threshold, it records attendance if no duplicate entry exists for the active session.

### Q2: What algorithm is used for face detection, and how does it work?
**Answer**: We use the Viola-Jones Haar Cascade classifier (`haarcascade_frontalface_default.xml`). It operates by evaluating rectangular Haar-like features (edge, line, and four-rectangle features) across an integral image, using an AdaBoost ensemble cascade to rapidly reject non-face background windows in early stages.

### Q3: How does the matching engine evaluate similarity between face samples?
**Answer**: We implement a hybrid metric combining:
1. **Histogram Correlation**: Evaluates intensity distribution across 32 normalized bins using Pearson correlation.
2. **Normalized Cross-Correlation (NCC)**: Evaluates structural spatial pixel alignment over a contrast-equalized $100 \times 100$ grayscale thumbnail.
The combined score ranges from 0.0 to 1.0.

### Q4: How does the application prevent duplicate attendance entries?
**Answer**: At the database level, the `attendance` table enforces a composite `UNIQUE(student_id, date, session_name)` constraint. Before attempting an insertion, the service queries existing records for the current date and session. If a record exists, the system returns a descriptive duplicate notification with the original check-in timestamp.

### Q5: Why is informed consent mandatory during student enrollment?
**Answer**: Biometric data (facial geometry) constitutes sensitive personal information under global privacy regulations (GDPR, India DPDP Act). Capturing biometric samples without consent violates privacy ethics. In our system, enrollment fails if the consent flag is not explicitly verified.

### Q6: What happens if a student refuses biometric enrollment or the camera breaks?
**Answer**: The application provides a "Manual Attendance Alternative" tab and button. Instructors can select the student by name and roll number to record attendance directly with mode set to `MANUAL`. This prevents biometric refusal or camera failure from penalizing students.

### Q7: How does the system handle students with low attendance?
**Answer**: The `calculate_attendance_percentages` service calculates the ratio of attended sessions to total distinct sessions conducted. Any student whose attendance falls below $75.0\%$ is flagged with `LOW ATTENDANCE WARNING` in the administrative monitoring tab.

### Q8: What database schema is utilized?
**Answer**: An SQLite database (`attendance.db`) containing:
- `students`: Roll number, name, department, semester, consent status, photo path.
- `attendance`: Foreign key to students, ISO date, timestamp, session name, confidence score, mode (`FACE_RECOGNITION` vs `MANUAL`).
- `admin_users`: Cryptographically hashed administrator credentials.

### Q9: How do you handle environments without physical webcam hardware?
**Answer**: The GUI incorporates a "Load Image / Demo Scan" button that enables loading an image file or generating a synthetic facial frame. This allows full evaluation, grading, and testing on virtual machines or servers without hardware cameras.

### Q10: What are the primary failure modes of 2D facial recognition?
**Answer**:
1. **Illumination Extremes**: Deep shadows or strong backlighting degrade contrast.
2. **Pose Variations**: Severe head rotations (> 30 degrees) disrupt frontal face geometry.
3. **Occlusions**: Masks, heavy sunglasses, or scarves conceal key facial features.
4. **Spoofing**: Static photographs held before the lens without liveness detection.

### Q11: How do you mitigate false positives in matching?
**Answer**: We enforce a configurable confidence threshold (default 0.60). Any detected face whose similarity score falls below this threshold is rejected as `UNKNOWN_FACE`, preventing incorrect student attribution.

### Q12: Why did you choose SQLite over CSV for primary persistence?
**Answer**: While reports can be exported to CSV, storing operational state in SQLite ensures ACID transactions, composite unique key constraints (preventing duplicates), relational foreign key cascades, and instantaneous indexed lookups.

### Q13: What automated tests were implemented?
**Answer**: Using Python's `unittest` framework:
- Rejection of enrollment when consent is False.
- Successful enrollment when consent is True.
- Duplicate attendance suppression for the same session.
- Proper execution of manual marking.
- Accurate identification of $< 75\%$ attendance defaulters.
- CSV export file integrity.

### Q14: How does the UI maintain responsiveness during webcam streaming?
**Answer**: The webcam read loop uses Tkinter's non-blocking `root.after(30, ...)` event loop scheduling rather than a blocking `while True: time.sleep()` loop, preserving user interface interactivity for button clicks and tab switching.

### Q15: What architectural enhancements would you propose for large-scale university deployment?
**Answer**:
1. Deep learning embeddings using ResNet/Inception (FaceNet or ArcFace) with a Vector Database (Milvus or Chroma) for 10,000+ student rosters.
2. IR/Blink liveness verification to defeat photo spoofing.
3. Edge deployment on Raspberry Pi or Jetson Nano units mounted at classroom entrances.
