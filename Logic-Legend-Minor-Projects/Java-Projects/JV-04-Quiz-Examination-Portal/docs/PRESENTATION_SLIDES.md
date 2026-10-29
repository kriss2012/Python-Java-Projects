# Presentation Slides — Online Quiz and Examination Portal (JV-04)

---

### Slide 1: Title Slide
- **Project Title**: Online Quiz and Examination Portal
- **Project Code**: `JV-04`
- **Domain**: Web Engineering & Assessment Informatics
- **Technology Stack**: Java 21, Embedded HTTP Server, JDBC, SQLite/MySQL, Bootstrap HTML5/JS
- **Presenter**: Engineering Candidate

---

### Slide 2: Problem Statement & Motivation
- **Manual Assessment Challenges**:
  - Delays in manual objective paper evaluation and publishing results.
  - Inconsistency in manual score calculation and record transcription.
  - Lack of timely pedagogical explanations for incorrect answers.
  - Heavy infrastructure overheads of external enterprise servlet containers (Tomcat/WildFly) for light departmental tests.
- **Proposed Solution**: A lightweight, zero-dependency embedded Java HTTP examination microservice with real-time timers and instant evaluation.

---

### Slide 3: System Architecture
- **Layer 1: Controller & HTTP Server**: Native `com.sun.net.httpserver.HttpServer` listening on port 8084.
- **Layer 2: Service Layer**: `QuizService` enforcing grading mathematics, validation rules, and leaderboard rankings.
- **Layer 3: Data Access Layer**: `DatabaseManager` with automatic table creation and connection pooling.
- **Layer 4: Relational Database**: SQLite 3 (standalone portable) or MySQL 8.0 enterprise backend.

---

### Slide 4: Key Features & Capabilities
- **Candidate Interface**: Clean Bootstrap 5 responsive layout with live countdown timer and answering progress bar.
- **Security by Design**: Answer keys are never sent to the browser DOM; evaluation occurs strictly server-side.
- **Instant Automated Grading**: Real-time score, percentage calculation, and comparison against passing criteria.
- **Pedagogical Review**: Provides question-by-question post-test breakdown with detailed rationales.
- **Global Leaderboard**: Automatic sorting by score descending and elapsed time ascending.
- **Admin Dashboard**: Web forms for adding quizzes and populating question banks.

---

### Slide 5: Database Schema & Entity Relationships
- **3NF Relational Model**:
  - `quizzes`: Master test definitions (duration, passing percentage, category).
  - `questions`: 4-option multiple-choice questions linked via `quiz_id`.
  - `exam_submissions`: Candidate attempt records with score, percentage, elapsed time, and JSON answer history.

---

### Slide 6: Mathematical Grading & Tie-Breaking Logic
- **Percentage Formula**: $\text{Percentage} = \frac{\sum (w_i \cdot \delta(a_i, k_i))}{\sum w_i} \times 100$
- **Pass / Fail Logic**: $\text{Percentage} \ge \text{Pass Cutoff}$
- **Competitive Tie-Breaking**: Candidates with identical high scores are ranked by lowest elapsed test duration (`time_taken_seconds ASC`).

---

### Slide 7: Demonstration Walkthrough
1. **Explore Available Tests**: Browse active exams in Computer Science, Programming, and Web Dev.
2. **Attempt Timed Exam**: Enter student name and email; watch countdown timer and answer 4-choice questions.
3. **Instant Results**: Submit exam to view instant pass/fail badge, percentage, and detailed rationale breakdown.
4. **Leaderboard**: Inspect top candidate rankings across all departmental participants.
5. **Admin Management**: Create new quizzes and append questions via `/admin`.

---

### Slide 8: Testing & Quality Assurance
- **Automated Test Runner**: `tests.TestRunner` executing 33 unit assertions across 8 distinct suites.
- **Key Assertions Tested**:
  - Quiz and question CRUD operations.
  - 100% score calculation and passing evaluation.
  - Sub-threshold failure detection.
  - Skipped/unanswered question robustness.
  - Leaderboard multi-criteria sorting.
  - Input validation (empty fields, illegal percentages, invalid option indices).
- **Result**: 33 / 33 assertions passed (0 failures).

---

### Slide 9: Advantages & Innovations
- **Zero-Dependency Deployment**: Runs on any clean JDK 21 environment without external server configurations.
- **Dual Operating Modes**: High-efficiency Web UI alongside a full terminal CLI for headless servers.
- **Extensible Architecture**: Easy migration from SQLite to enterprise MySQL with one configuration change (`DB_TYPE=mysql`).

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: JV-04 delivers a production-grade, highly responsive online test portal adhering to rigorous academic and software engineering standards.
- **Future Enhancements**:
  - Randomized question shuffling per candidate.
  - Webcam proctoring and tab-switch cheating detection.
  - PDF certificate generation upon successful exam completion.

