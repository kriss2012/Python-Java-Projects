# Presentation Slides — Student Management Portal (JV-07)

---

### Slide 1: Title Slide
- **Project Title**: Student Academic Management & Transcript Portal
- **Project Code**: `JV-07`
- **Domain**: Higher Education Informatics & Enterprise Web Systems
- **Technology Stack**: Java 21 LTS, Spring MVC / Embedded HTTP Server, JDBC, SQLite/MySQL, Bootstrap 5
- **Presenter**: Engineering Candidate

---

### Slide 2: Problem Statement & Motivation
- **Academic Office Bottlenecks**:
  - Manual computation of credit-weighted GPA and CGPA is prone to calculation errors.
  - Scattered records result in misplaced student histories and transcript delays.
  - Absence of real-time departmental performance analytics for academic accreditation (NAAC/NBA).
  - Lack of standardized server-side data validation leading to corrupt email/phone records.
- **Proposed Solution**: An enterprise web portal automating student lifecycle management, credit-weighted CGPA computation, official transcript publishing, and departmental benchmarking.

---

### Slide 3: System Architecture
- **Layer 1: Web Controller**: Embedded HTTP server listening on port 8087, mapping endpoints for directory, enrollment, grade recording, transcripts, analytics, and REST API.
- **Layer 2: Service Layer**: `StudentService` managing regex validations, credit-weighted GPA/CGPA calculations, and transcript formatters.
- **Layer 3: Data Access Layer**: `DatabaseManager` providing dual-database portability for SQLite and MySQL.
- **Layer 4: Relational Database**: 3NF schema for `students` and `course_grades`.

---

### Slide 4: Key Functional Modules
- **Student Profile Management**: Full CRUD operations with regex validation on official email and phone numbers.
- **Credit-Weighted GPA / CGPA Engine**:
  - Automatically converts letter grades (`O` to `F`) into standard 10-point scale grade points.
  - Automatically recalculates cumulative CGPA upon every grade entry.
  - Computes individual Semester GPAs (SGPA).
- **Official Academic Transcripts**:
  - Formatted university transcript with course details, credits, earned points, and classification.
  - Exportable to text files and one-click printable from browser.
- **Faculty Analytics Dashboard**:
  - Department-wise breakdown of student enrollment counts, average CGPA, and top achievers.
- **RESTful API**:
  - Endpoint `/api/students` returning structured JSON for third-party systems.

---

### Slide 5: Database Schema & Relationships
- **`students`**: `reg_number` (UNIQUE), `name`, `email` (UNIQUE), `phone`, `department`, `academic_year`, `cgpa`, `status`.
- **`course_grades`**: Linked via `student_id` foreign key (`ON DELETE CASCADE`), storing `course_code`, `course_name`, `credits`, `letter_grade`, `grade_points`, and `semester`.

---

### Slide 6: Mathematical Formulations
- **Credit Points**: $\text{Credits}_i \times \text{Grade Points}_i$
- **Semester SGPA**: $\frac{\sum_{\text{sem}} (\text{Credits} \times \text{Points})}{\sum_{\text{sem}} \text{Credits}}$
- **Cumulative CGPA**: $\frac{\sum_{\text{all}} (\text{Credits} \times \text{Points})}{\sum_{\text{all}} \text{Credits}}$
- **Academic Standing**:
  - $\text{CGPA} \ge 9.0$: Exemplary Distinction
  - $8.0 \le \text{CGPA} < 9.0$: First Class with Distinction
  - $6.5 \le \text{CGPA} < 8.0$: First Class
  - $\text{CGPA} < 5.0$: Academic Probation

---

### Slide 7: Demonstration Walkthrough
1. **Explore Directory**: Open `http://localhost:8087/` to view enrolled students, CGPA badges, and academic classifications.
2. **Filter & Search**: Filter by department (e.g. *Computer Science*) or search student name.
3. **Enroll New Student**: Visit `/students/new` to enroll a student with server-side validated email/phone.
4. **Record Grades**: Open student profile, enter course code (`CS401`), credits (`4`), and letter grade (`A+`).
5. **View Transcript**: Inspect the generated official academic transcript with formatted university seals.
6. **Analytics**: Visit `/analytics` to review department-wise student counts and average CGPA metrics.

---

### Slide 8: Automated Verification & Testing
- **Test Runner (`tests.TestRunner`)**: 40 unit and integration assertions across 8 distinct suites:
  - Student creation and registration uniqueness.
  - Server-side regex validation (email, phone, reg numbers).
  - Progressive CGPA recalculation across multiple courses.
  - Semester SGPA isolation vs overall CGPA.
  - Multi-parameter search queries and department filtering.
  - Profile update and graduation status transitions.
  - Academic transcript formatting and filesystem export.
  - Department performance analytics.
- **Result**: 40 / 40 assertions passed (0 failures).

---

### Slide 9: Architectural Strengths
- **Zero Configuration**: Bundled SQLite JDBC driver guarantees that the application runs immediately without external software.
- **Dual Operating Modes**: High-efficiency Web UI alongside a full terminal CLI (`--cli`) for headless servers.
- **Open REST API**: Enables seamless integration with mobile apps, parent portals, or enterprise ERP systems.

---

### Slide 10: Conclusion & Future Roadmap
- **Conclusion**: JV-07 delivers an enterprise-grade academic management system meeting all college submission and viva evaluation requirements.
- **Future Roadmap**:
  - Student self-service portal with role-based authentication.
  - PDF transcript generation with digital signature verification.
  - Automated low-GPA warning emails to academic advisors.

