# Logic Legend — JV-04: Online Quiz and Examination Portal

A comprehensive, full-featured web and terminal-based Online Quiz and Examination Management System built using Java 21, standard embedded `com.sun.net.httpserver.HttpServer`, JDBC, SQLite, MySQL, and responsive Bootstrap HTML5/CSS3. The platform provides automated timed examinations, client-side auto-submission countdown timers, instant automated grading and scoring algorithms, detailed question-by-question review with pedagogical rationale, competitive global leaderboards, and an administrative question bank authoring dashboard.

---

## 1. Project Title and Identifier
- **Project Name**: Online Quiz and Examination Portal
- **Project Code**: `JV-04`
- **Application Category**: Educational Technology (EdTech), Assessment Informatics & Web Applications
- **Target Audience**: Educational institutions, university test conductors, professors, competitive programming mentors, students, and viva evaluators.

---

## 2. Executive Summary
Traditional pen-and-paper examinations and disconnected quiz portals suffer from manual paper correction delays, lack of immediate feedback, administrative grading discrepancies, and vulnerability to server downtimes during high concurrency. The **Online Quiz and Examination Portal** offers a resilient, zero-dependency Java web service backed by ACID-compliant relational databases. It delivers real-time time-locked exam sessions, automated objective score calculation, percentage evaluation against configurable cutoff criteria, instant post-test rationale reviews, and competitive rank leaderboards sorted by score and completion duration.

---

## 3. Key Features
- **Embedded Zero-Dependency Web Server**: Built using Java's native high-performance `HttpServer` (`com.sun.net.httpserver`), requiring zero external Tomcat/Jetty containers.
- **Interactive Candidate Examination Interface**:
  - Live JavaScript exam countdown timer synchronized with backend duration limits.
  - Progress bar tracking answering completion percentage.
  - Clean multiple-choice question layout (Option A through D) with instant option selection.
- **Instant Automated Evaluation Engine**:
  - Compares submitted candidate answers against encrypted/relational correct answers.
  - Dynamically computes score obtained, total marks, percentage score, and Pass/Fail status.
  - Enforces configurable passing cutoffs (e.g., 50%, 60%, 75%).
- **Pedagogical Answer Review**: Generates itemized question breakdowns displaying candidate choices, correct keys, awarded points, and educational explanations.
- **Competitive Real-Time Leaderboard**: Ranks candidate performances per quiz ordered by highest score descending and shortest test completion duration ascending.
- **Administrative Question Bank Authoring**:
  - Create new subject-specific quizzes with customized duration and pass thresholds.
  - Add multiple-choice questions with customized options, answer keys, marks, and pedagogical explanations.
- **Dual Interface Modes**: Modern responsive Web Portal (`http://localhost:8084/`) combined with an interactive Command-Line Interface (`--cli`) for headless and automation test environments.
- **Zero-Configuration Portability**: Embedded SQLite database (`sqlite-jdbc.jar`) works out of the box on JDK 21 without requiring MySQL server installation, with production MySQL schema provided.

---

## 4. System Architecture
The application follows a clean 4-tier web architecture:
1. **HTTP Controller & Presentation Layer (`web/QuizWebServer.java`, `Main.java`)**: Handles incoming HTTP requests, serves semantic Bootstrap HTML templates, parses form submissions, and provides interactive terminal console.
2. **Business Service Layer (`service/QuizService.java`)**: Manages business rules, exam validation, score evaluation, pass/fail thresholds, leaderboard sorting, and demo seed data.
3. **Data Access Layer (`dao/DatabaseManager.java`)**: Encapsulates JDBC connections, connection pooling, and automatic schema bootstrap for SQLite and MySQL.
4. **Relational Persistence Layer (`model/`, SQLite / MySQL)**: Normalized database tables for `quizzes`, `questions`, and `exam_submissions`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **Web Runtime**: Java Core HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **Frontend**: HTML5, CSS3, Modern Bootstrap Design, Vanilla JavaScript
- **Database Connectivity**: JDBC (`java.sql.*`)
- **Default Database**: SQLite 3 (Bundled zero-dependency `sqlite-jdbc.jar`)
- **Production Database**: MySQL 8.x (Script in `sql/schema_mysql.sql`)
- **Build & Dependency Tool**: Apache Maven (`pom.xml`) or direct JDK CLI compilation

---

## 6. Directory and Package Layout
```
JV-04-Quiz-Examination-Portal/
├── .env.example                  # Environment configuration template
├── .gitignore                    # Git ignore file
├── pom.xml                       # Maven build descriptor
├── README.md                     # Comprehensive 19-section documentation
├── docs/
│   ├── ACADEMIC_REPORT.md        # Complete academic thesis with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md    # 10-slide viva presentation deck
│   ├── VIVA_QUESTIONS.md         # 15+ deep technical questions and answers
│   └── DEMO_GUIDE.md             # Step-by-step evaluator testing checklist
├── lib/
│   └── sqlite-jdbc.jar           # Embedded zero-dependency SQLite driver
├── sql/
│   ├── schema_sqlite.sql         # Relational schema for SQLite
│   └── schema_mysql.sql          # Relational schema for MySQL
├── src/
│   └── com/logiclegend/quiz/
│       ├── Main.java             # Dual-mode server and CLI launcher
│       ├── dao/
│       │   └── DatabaseManager.java # JDBC connection & migration manager
│       ├── model/
│       │   ├── Quiz.java         # Quiz entity
│       │   ├── Question.java     # Question entity
│       │   └── ExamSubmission.java # Exam attempt & score record
│       ├── service/
│       │   └── QuizService.java  # Grading logic, validation, leaderboard
│       └── web/
│           └── QuizWebServer.java # Native HTTP endpoints & HTML templates
└── tests/
    └── TestRunner.java           # Comprehensive 33-assertion automated test suite
```

---

## 7. Database Design and Schemas
Normalized 3NF relational schema:
- **`quizzes`**: `id` (PK), `quiz_code` (UNIQUE), `title`, `description`, `category`, `duration_minutes`, `pass_percentage`, `status`, `created_at`.
- **`questions`**: `id` (PK), `quiz_id` (FK referencing `quizzes`), `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_option` (1-4), `explanation`, `marks`, `created_at`.
- **`exam_submissions`**: `id` (PK), `submission_ref` (UNIQUE), `quiz_id` (FK), `student_name`, `student_email`, `total_questions`, `correct_answers`, `score_obtained`, `max_score`, `percentage`, `result_status`, `time_taken_seconds`, `answers_json`, `submitted_at`.

---

## 8. Installation and Compilation Guide

### Direct Compilation with Standard JDK
```powershell
# Navigate to project folder
cd "Logic-Legend-Minor-Projects\Java-Projects\JV-04-Quiz-Examination-Portal"

# Compile all source and test classes
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\quiz\model\*.java src\com\logiclegend\quiz\dao\*.java src\com\logiclegend\quiz\service\*.java src\com\logiclegend\quiz\web\*.java src\com\logiclegend\quiz\Main.java tests\TestRunner.java
```

---

## 9. Running the Application

### Mode 1: Web Portal Server (Recommended)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.quiz.Main
```
Then open your web browser at:
- **Portal Home**: `http://localhost:8084/`
- **Admin Dashboard**: `http://localhost:8084/admin`
- **Global Leaderboard**: `http://localhost:8084/leaderboard`

### Mode 2: Interactive Terminal Console
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.quiz.Main --cli
```

---

## 10. Automated Testing
Run the comprehensive test suite verifying 33 functional assertions:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Sample test output:
```
=================================================================
  LOGIC LEGEND JV-04: AUTOMATED TEST SUITE EXECUTION
=================================================================
--- Test Suite 1: Quiz Creation and Retrieval ---
  [PASS] Quiz created with non-zero ID
  [PASS] Quiz title matches
  [PASS] Quiz category matches
  [PASS] Quiz duration matches 30
  [PASS] Quiz is active by default
  [PASS] Retrieved quiz is non-null
  [PASS] Retrieved title matches
  [PASS] Quiz list contains at least 1 quiz
...
=================================================================
  TOTAL ASSERTIONS: 33 | PASSED: 33 | FAILED: 0
=================================================================
```

---

## 11. Security and Fraud Prevention
- **Answer Key Protection**: Correct answers and detailed pedagogical explanations are never exposed to the client in the exam HTML/JS payload; evaluation is performed exclusively on the server.
- **SQL Injection Prevention**: All queries use parameterized JDBC `PreparedStatement` interfaces.
- **Deterministic Evaluation**: Test submissions are timestamped and assigned unique tracking UUIDs (`EXAM-XXXX`).

---

## 12. Evaluation & Grading Mathematical Formula
$$\text{Percentage} = \left(\frac{\text{Score Obtained}}{\text{Maximum Score}}\right) \times 100$$
$$\text{Status} = \begin{cases} \text{PASSED}, & \text{if } \text{Percentage} \ge \text{Pass Percentage} \\ \text{FAILED}, & \text{otherwise} \end{cases}$$

---

## 13. License
Educational Project Suite — Logic Legend Minor Projects. Open source under Apache 2.0 license.

