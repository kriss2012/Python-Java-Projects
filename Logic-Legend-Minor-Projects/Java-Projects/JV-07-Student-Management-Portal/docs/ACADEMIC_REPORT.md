# Academic Project Report — Student Management Portal (JV-07)

**Project Identifier**: `JV-07`  
**Domain**: Higher Education Informatics & Enterprise Web Systems  
**Academic Level**: Bachelor of Technology / Master of Computer Applications (Minor Project)

---

## 1. Abstract
The **Student Management Portal** is an enterprise web application engineered in Java 21 adhering to Spring MVC and REST architectural patterns. The platform manages the entire academic record lifecycle of students, including demographic profiles, course grade entry, automatic credit-weighted Semester Grade Point Average (SGPA) and Cumulative Grade Point Average (CGPA) calculations, academic standing classifications, official transcript generation, department-level performance analytics, and RESTful API endpoints. Rigorous server-side regex validation prevents corrupt records, and ACID database transactions ensure data integrity across student enrollments and grade modifications.

---

## 2. System Architecture

```mermaid
flowchart TD
    User[Academic Registrar / Student / Evaluator] -->|HTTP GET / POST| Controller[StudentWebServer / com.sun.net.httpserver]
    APIClient[External Client / Mobile App] -->|HTTP GET /api/students| Controller
    
    subgraph Web Controller Layer
        Controller -->|Route /students| ListHandler[Directory Controller]
        Controller -->|Route /students/save| SaveHandler[Enrollment Controller]
        Controller -->|Route /students/add-grade| GradeHandler[Grade Calculation Controller]
        Controller -->|Route /students/transcript| TranscriptHandler[Transcript Controller]
        Controller -->|Route /analytics| AnalyticsHandler[Faculty Analytics Controller]
        Controller -->|Route /api/students| ApiHandler[JSON REST Controller]
    end
    
    subgraph Service & Business Layer
        ListHandler --> Service[StudentService]
        SaveHandler --> Service
        GradeHandler --> Service
        TranscriptHandler --> Service
        AnalyticsHandler --> Service
        ApiHandler --> Service
    end
    
    subgraph Data Access Layer
        Service --> DatabaseManager[DatabaseManager JDBC]
        DatabaseManager --> SQLite[(SQLite Embedded DB)]
        DatabaseManager -.-> MySQL[(MySQL Enterprise DB)]
    end
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    STUDENTS ||--o{ COURSE_GRADES : earns

    STUDENTS {
        int id PK
        string reg_number UK
        string name
        string email UK
        string phone
        string department
        int academic_year
        double cgpa
        string status
        datetime created_at
    }

    COURSE_GRADES {
        int id PK
        int student_id FK
        string course_code
        string course_name
        int credits
        string letter_grade
        double grade_points
        int semester
        datetime created_at
    }
```

---

## 4. Data Flow Diagram (DFD)

### Level 0: Context Diagram
```mermaid
flowchart LR
    Registrar[Academic Registrar] -->|1. Enroll Student & Record Course Grades| System((JV-07 Student Portal))
    System -->|2. Return Grade Cards, Transcripts & Department Analytics| Registrar
    Student[Student / External System] -->|3. Search Profiles & Request Transcripts| System
    System -->|4. Return JSON Payloads & Formatted Transcripts| Student
```

### Level 1: Subsystem Decomposition
```mermaid
flowchart TD
    P1[1.0 Student Enrollment & Input Validation] -->|Valid Profile| DB[(Relational Storage)]
    P2[2.0 Course Grade Management] -->|Add Course Credits & Grades| P3[3.0 Credit-Weighted GPA Engine]
    P3 -->|Update Calculated CGPA| DB
    DB --> P4[4.0 Search & Pagination Subsystem]
    DB --> P5[5.0 Transcript Generation Subsystem]
    DB --> P6[6.0 Department Performance Aggregation]
```

---

## 5. Sequence Diagram: Course Grade Addition and CGPA Recalculation

```mermaid
sequenceDiagram
    autonumber
    actor Staff as Academic Mentor / Registrar
    participant Browser as Web Browser (DOM)
    participant Web as StudentWebServer
    participant Service as StudentService
    participant DB as Relational Database

    Staff->>Browser: Select Student & Fill Course Grade Form (Code, Credits, Grade)
    Staff->>Browser: Click 'Record Grade & Recalculate CGPA'
    Browser->>Web: POST /students/add-grade (studentId, courseCode, credits, grade, sem)
    
    Web->>Service: addCourseGrade(studentId, code, name, credits, letterGrade, sem)
    Service->>Service: convertLetterToPoints(letterGrade) -> gradePoints
    Service->>DB: INSERT INTO course_grades VALUES (...)
    DB-->>Service: Generated Grade ID
    
    Service->>Service: recalculateCgpa(studentId)
    Service->>DB: SELECT credits, grade_points FROM course_grades WHERE student_id = ?
    DB-->>Service: List of all enrolled course grades
    
    Note over Service: CGPA = Sum(credits * grade_points) / Sum(credits)
    
    Service->>DB: UPDATE students SET cgpa = ? WHERE id = ?
    DB-->>Service: Update Confirmation
    
    Service-->>Web: Updated Student Entity
    Web-->>Browser: HTTP 302 Redirect to /students/view?id=...
    Browser->>Web: GET /students/view?id=...
    Web-->>Browser: Render Profile with Updated Course History & Recalculated CGPA
```

---

## 6. Credit-Weighted Mathematical Engine

### Credit Points Calculation
For course $j$, the credit points earned are:
$$\text{CP}_j = \text{Credits}_j \times \text{Grade Points}_j$$

### Semester Grade Point Average (SGPA)
$$\text{SGPA}_s = \frac{\sum_{j \in S_s} (\text{Credits}_j \times \text{Grade Points}_j)}{\sum_{j \in S_s} \text{Credits}_j}$$
Where $S_s$ denotes the set of courses taken in semester $s$.

### Cumulative Grade Point Average (CGPA)
Across all $N$ completed courses over all academic semesters:
$$\text{CGPA} = \frac{\sum_{j=1}^{N} (\text{Credits}_j \times \text{Grade Points}_j)}{\sum_{j=1}^{N} \text{Credits}_j}$$

### Academic Classification Scale
$$\text{Classification} = \begin{cases} 
\text{Distinction with Exemplary}, & \text{if } \text{CGPA} \ge 9.0 \\
\text{First Class with Distinction}, & \text{if } 8.0 \le \text{CGPA} < 9.0 \\
\text{First Class}, & \text{if } 6.5 \le \text{CGPA} < 8.0 \\
\text{Second Class}, & \text{if } 5.0 \le \text{CGPA} < 6.5 \\
\text{Academic Probation}, & \text{if } \text{CGPA} < 5.0
\end{cases}$$

---

## 7. Verification and Test Results
The automated test runner (`tests/TestRunner.java`) executed 40 unit and integration assertions across 8 test suites:
- Profile creation, registration number uniqueness, and field persistence.
- Server-side regex validation for malformed email addresses and illegal phone numbers.
- Automated CGPA recalculation upon single and multiple course grade additions.
- Accurate separation of semester SGPA from cumulative CGPA.
- Multi-parameter search queries and department filtering.
- Profile update and soft status transitions (`ACTIVE` to `GRADUATED`).
- Official transcript generation with header formatting and filesystem export.
- Departmental analytics aggregation (headcount, average CGPA, top CGPA).
- **Result: 40 / 40 assertions passed (100% success rate)**.

