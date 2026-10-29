# PRESENTATION SLIDES OUTLINE
## PY-03: Face Recognition Attendance System
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: Face Recognition Attendance System (PY-03)
- **Domain**: Computer Vision, Campus Automation & Data Ethics
- **Technologies**: Python, OpenCV (`cv2`), Tkinter, SQLite, NumPy, Pillow
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Conventional roll calling consumes 10–15 minutes per lecture.
- Manual paper sheets permit proxy attendance ("buddy punching").
- Existing automated systems neglect student biometric privacy consent.
- Camera hardware errors often leave proctors stranded with no alternative.

---

### Slide 3: Objectives & Solution Highlights
- Autonomous, contactless attendance via webcam.
- Haar Cascade face detection with illumination-equalized template matching.
- **Standout Feature**: Strict informed biometric consent check and manual attendance fallback.
- Database-level duplicate check-in prevention.
- Automatic defaulter identification for students below 75% attendance.

---

### Slide 4: System Architecture & Data Flow
- **Video Capture Pipeline**: Frame grabbing -> Color normalization -> Face ROI detection.
- **Biometric Matching**: Intensity histogram correlation + Normalized cross-correlation.
- **Relational Persistence**: SQLite enforces uniqueness on `(student_id, date, session_name)`.
- **Administrative Layer**: Student enrollment, CSV reporting, and defaulter tracking.

---

### Slide 5: Privacy by Design & Ethics
- **Informed Consent**: Biometric templates cannot be saved without an explicit signed consent record.
- **Manual Attendance Alternative**: Non-consenting students or students with temporary physical occlusions can be marked manually without academic penalty.
- **Local Storage**: Biometric signatures remain strictly on premises (no cloud exposure).

---

### Slide 6: Duplicate Suppression Mechanism
- Prevents students from scanning multiple times during the same class session.
- System detects existing check-ins and displays a yellow warning banner without crashing or writing duplicate rows.

---

### Slide 7: Low Attendance Monitoring (< 75%)
- Calculates cumulative percentage: $\frac{\text{Sessions Attended}}{\text{Total Conducted}} \times 100$.
- Instantly identifies defaulters in an interactive dashboard for academic intervention.

---

### Slide 8: Demonstration & Visual Highlights
- Live webcam streaming with real-time green bounding boxes.
- "Load Image / Demo Scan" button for environments lacking hardware cameras.
- Real-time result card rendering (Student Name, Roll Number, Department, Confidence %).
- One-click CSV export of complete attendance registers.

---

### Slide 9: Testing & Verification
- 6 automated unit tests verifying:
  - Consent validation gates.
  - Duplicate check-in rejection.
  - Manual marking pipeline.
  - Mathematical accuracy of attendance percentages.
  - Feature extraction and CSV generation.

---

### Slide 10: Limitations & Future Enhancements
- **Limitations**: Susceptible to 2D photo spoofing without depth sensors; reduced confidence in extreme dark lighting.
- **Future Scope**:
  - Infrared/RGB liveness detection (blink detection, micro-movements).
  - Parent notification webhooks via WhatsApp Business API.
  - Cloud synchronization with University ERP systems.

---

### Slide 11: Conclusion & Q&A
- Delivered an ethical, robust, and verified biometric attendance application.
- Ready for viva evaluation and live demonstration.
