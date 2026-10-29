# Logic Legend — JV-07: Student Management Portal

An enterprise student academic administration and transcript management web portal engineered using Java 21, Spring Boot design patterns, native embedded HTTP server, JDBC, SQLite, and MySQL. The system provides complete CRUD operations on student profiles, server-side regex validation, automated semester GPA (SGPA) and cumulative CGPA calculations, department filtering and search pagination, official academic transcript generation, department-level performance analytics, and a REST JSON API (`/api/students`).

---

## 1. Project Title and Identifier
- **Project Name**: Student Management Portal
- **Project Code**: `JV-07`
- **Application Category**: Higher Education Informatics, Academic ERP & Enterprise Web Applications
- **Target Audience**: University registrars, department chairs, academic mentors, students, and viva evaluators.

---

## 2. Executive Summary
University academic offices frequently face delays in manually compiling semester grade cards, managing student demographic updates, and computing weighted Cumulative Grade Point Averages across differing course credit schemes. The **Student Management Portal** delivers an automated, zero-configuration web application backed by relational databases. It enforces strict server-side validation for emails and phone numbers, calculates credit-weighted SGPA and CGPA in real time, generates downloadable monospaced transcripts, aggregates faculty analytics, and provides an open REST API.

---

## 3. Key Features
- **Student Profile Lifecycle (CRUD)**:
  - Capture unique registration numbers, full names, official emails, phone contacts, departments, and academic years.
  - Update profile details and manage status (`ACTIVE`, `PROBATION`, `GRADUATED`).
- **Automated Credit-Weighted GPA & CGPA Engine**:
  - Automatically converts letter grades (`O`, `A+`, `A`, `B+`, `B`, `C`, `P`, `F`) to standard 10-point scale grade points.
  - Computes credit-weighted Semester Grade Point Average (SGPA):
    $$\text{SGPA} = \frac{\sum_{\text{sem}} (\text{Credits} \times \text{Grade Points})}{\sum_{\text{sem}} \text{Credits}}$$
  - Automatically recalculates and updates the cumulative CGPA upon every course grade addition.
- **Search, Filtering & Pagination**:
  - Multi-parameter search matching registration numbers, names, or emails.
  - Departmental filtering (Computer Science, Electronics, Mechanical, Information Technology).
- **Official Academic Transcripts**:
  - Generates formatted university grade transcripts with institute headers, semester-by-semester course rosters, credits, letter grades, credit points, and academic standing classifications.
  - Exports transcripts directly to the filesystem (`exports/TRANSCRIPT_XXXX.txt`) and enables browser one-click printing.
- **Department Performance Analytics**:
  - Real-time aggregated statistics: student headcounts, departmental average CGPA, and top achiever benchmarks.
- **RESTful JSON API**:
  - `/api/students` delivers JSON payloads for external university integrations and microservice interoperability.
- **Dual Interface Modes**:
  - Modern responsive web portal (`http://localhost:8087/`) built with clean semantic Bootstrap styling.
  - Full-featured interactive terminal console (`--cli`) for headless and automation test environments.

---

## 4. System Architecture
1. **HTTP Controller & Presentation Layer (`web/StudentWebServer.java`, `Main.java`)**: Native HTTP routing, responsive Bootstrap HTML templates, and terminal interactive menu.
2. **Business Service Layer (`service/StudentService.java`)**: Validation rules, GPA/CGPA credit-weighting algorithms, transcript formatters, and seed data.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: JDBC connectivity and automated schema initialization for SQLite and MySQL.
4. **Relational Persistence Layer (`model/`, SQLite / MySQL)**: Normalized tables for `students` and `course_grades`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **Web Runtime**: Java Core HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **Frontend**: HTML5, CSS3, Modern Bootstrap 5 Design
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Bundled zero-dependency `sqlite-jdbc.jar`)
- **Production Database**: MySQL 8.x (Script in `sql/schema_mysql.sql`)
- **Build Tool**: Apache Maven (`pom.xml`) or standard JDK compilation

---

## 6. Directory Layout
```
JV-07-Student-Management-Portal/
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── lib/
│   └── sqlite-jdbc.jar
├── sql/
│   ├── schema_sqlite.sql
│   └── schema_mysql.sql
├── src/
│   └── com/logiclegend/student/
│       ├── Main.java             # Web server launcher & CLI
│       ├── dao/
│       │   └── DatabaseManager.java # Connection & schema bootstrap
│       ├── model/
│       │   ├── Student.java      # Student profile entity
│       │   └── CourseGrade.java  # Course grade entity
│       ├── service/
│       │   └── StudentService.java # Business logic & GPA calculation
│       └── web/
│           └── StudentWebServer.java # HTTP routes & UI templates
└── tests/
    └── TestRunner.java           # Comprehensive 40-assertion test suite
```

---

## 7. Compilation and Setup Guide

### Direct Compilation with Standard JDK
```powershell
cd "Logic-Legend-Minor-Projects\Java-Projects\JV-07-Student-Management-Portal"
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\student\model\*.java src\com\logiclegend\student\dao\*.java src\com\logiclegend\student\service\*.java src\com\logiclegend\student\web\*.java src\com\logiclegend\student\Main.java tests\TestRunner.java
```

---

## 8. Running the Application

### Mode 1: Web Portal Server (Recommended)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.student.Main
```
Open your browser at:
- **Student Directory**: `http://localhost:8087/`
- **New Enrollment**: `http://localhost:8087/students/new`
- **Department Analytics**: `http://localhost:8087/analytics`
- **REST API**: `http://localhost:8087/api/students`

### Mode 2: Interactive Terminal Console
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.student.Main --cli
```

---

## 9. Automated Testing
Run the automated test suite executing 40 comprehensive assertions:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Sample test output:
```
=================================================================
  LOGIC LEGEND JV-07: AUTOMATED TEST SUITE EXECUTION
=================================================================
--- Test Suite 1: Student Creation and Retrieval ---
  [PASS] Student created with non-zero ID
  [PASS] Registration number matches
  [PASS] Department matches
  [PASS] Academic year matches
  [PASS] Initial CGPA is 0.0
...
=================================================================
  TOTAL ASSERTIONS: 40 | PASSED: 40 | FAILED: 0
=================================================================
```

---

## 10. Grade Scale and Credit-Weighted Mathematical Engine
| Letter Grade | Performance Level | Grade Points |
| :--- | :--- | :--- |
| **O** / **A+** | Outstanding / Exemplary | 10.0 |
| **A** | Excellent | 9.0 |
| **B+** | Very Good | 8.0 |
| **B** | Good | 7.0 |
| **C** | Above Average | 6.0 |
| **P** | Pass | 5.0 |
| **F** | Fail | 0.0 |

$$\text{CGPA} = \frac{\sum_{i=1}^{K} (\text{credits}_i \times \text{grade\_points}_i)}{\sum_{i=1}^{K} \text{credits}_i}$$

---

## 11. License
Educational Project Suite — Logic Legend Minor Projects. Open source under Apache 2.0 license.

