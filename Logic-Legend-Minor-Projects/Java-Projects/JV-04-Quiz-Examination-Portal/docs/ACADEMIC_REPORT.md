# Academic Project Report — Online Quiz and Examination Portal (JV-04)

**Project Identifier**: `JV-04`  
**Domain**: Computer Science & Engineering / Educational Informatics  
**Academic Level**: Bachelor of Technology / Master of Computer Applications (Minor Project)

---

## 1. Abstract
The **Online Quiz and Examination Portal** is an automated, web-based assessment evaluation and management system engineered in Java 21. By eliminating manual grading bottlenecks and paper examination overheads, the platform implements real-time synchronized candidate countdown timers, zero-dependency embedded HTTP micro-server architecture, atomic relational answer verification, and automated Pass/Fail ranking. The system incorporates pedagogical rationale feedback, dynamic question bank management, and tie-breaking leaderboard sorting based on dual criteria (highest score and minimum time taken).

---

## 2. System Architecture

```mermaid
flowchart TD
    Candidate[Student Candidate / Web Browser] -->|HTTP GET / POST| Controller[QuizWebServer / com.sun.net.httpserver]
    Admin[Test Administrator / Examiner] -->|HTTP GET / POST| Controller
    
    subgraph Web Application Layer
        Controller -->|Route /exam| ExamHandler[Exam Form Handler]
        Controller -->|Route /submit-exam| SubmitHandler[Evaluation & Grading Engine]
        Controller -->|Route /leaderboard| LeaderboardHandler[Leaderboard Controller]
        Controller -->|Route /admin| AdminHandler[Question Bank Dashboard]
    end
    
    subgraph Business Logic Layer
        ExamHandler --> QuizService[QuizService]
        SubmitHandler --> QuizService
        LeaderboardHandler --> QuizService
        AdminHandler --> QuizService
    end
    
    subgraph Persistence Layer
        QuizService --> DatabaseManager[DatabaseManager JDBC]
        DatabaseManager --> SQLite[(SQLite Embedded DB)]
        DatabaseManager -.-> MySQL[(MySQL Enterprise DB)]
    end
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    QUIZZES ||--o{ QUESTIONS : contains
    QUIZZES ||--o{ EXAM_SUBMISSIONS : evaluates

    QUIZZES {
        int id PK
        string quiz_code UK
        string title
        string description
        string category
        int duration_minutes
        double pass_percentage
        string status
        datetime created_at
    }

    QUESTIONS {
        int id PK
        int quiz_id FK
        string question_text
        string option_a
        string option_b
        string option_c
        string option_d
        int correct_option
        string explanation
        double marks
        datetime created_at
    }

    EXAM_SUBMISSIONS {
        int id PK
        string submission_ref UK
        int quiz_id FK
        string student_name
        string student_email
        int total_questions
        int correct_answers
        double score_obtained
        double max_score
        double percentage
        string result_status
        int time_taken_seconds
        text answers_json
        datetime submitted_at
    }
```

---

## 4. Data Flow Diagram (DFD)

### Level 0: Context Diagram
```mermaid
flowchart LR
    Candidate[Candidate] -->|1. Request Quiz & Submit Answers| System((JV-04 Quiz Examination System))
    System -->|2. Return Instant Evaluation & Detailed Review| Candidate
    Admin[Administrator] -->|3. Author Quizzes & Questions| System
    System -->|4. View Analytics & Leaderboards| Admin
```

### Level 1: Subsystem Decomposition
```mermaid
flowchart TD
    P1[1.0 Authenticate & Launch Exam] --> P2[2.0 Exam Session & Timer Control]
    P2 -->|Candidate Answers Form| P3[3.0 Answer Verification & Scoring]
    P3 -->|Write Result| DB[(Relational Storage)]
    P3 --> P4[4.0 Result & Review Generation]
    DB --> P5[5.0 Leaderboard & Analytics Ranking]
```

---

## 5. Sequence Diagram: Exam Submission and Grading

```mermaid
sequenceDiagram
    autonumber
    actor Student as Student Candidate
    participant Browser as Web Browser (DOM & JS Timer)
    participant Web as QuizWebServer
    participant Service as QuizService
    participant DB as Relational Database

    Student->>Browser: Select Quiz & Enter Credentials
    Browser->>Web: GET /exam?quizId=1
    Web->>Service: getQuestionsForQuiz(1)
    Service->>DB: SELECT * FROM questions WHERE quiz_id = 1
    DB-->>Service: List<Question> (without exposing keys to DOM)
    Service-->>Web: Question List
    Web-->>Browser: Render HTML Form + JavaScript Countdown Timer
    
    Note over Browser: Timer ticks down... Student selects answers
    Student->>Browser: Click 'Submit Exam'
    Browser->>Web: POST /submit-exam (quizId, candidate info, chosen options, elapsed seconds)
    
    Web->>Service: submitExam(quizId, name, email, answersMap, timeTaken)
    Service->>DB: SELECT id, correct_option, marks FROM questions WHERE quiz_id = 1
    DB-->>Service: Answer Keys
    
    Note over Service: Evaluate: correctCount, scoreObtained, percentage, pass/fail status
    Service->>DB: INSERT INTO exam_submissions VALUES (...)
    DB-->>Service: Return generated submission ID
    Service-->>Web: ExamSubmission Object
    Web-->>Browser: Render Result Page + Detailed Question Rationale + Rank
    Browser-->>Student: Display Score, Percentage, Review, and Certificate Status
```

---

## 6. Mathematical Evaluation Formulations

### Objective Percentage Calculation
$$\text{Percentage} = \left(\frac{\sum_{i=1}^{N} \delta(a_i, k_i) \cdot w_i}{\sum_{i=1}^{N} w_i}\right) \times 100$$
Where:
- $N$ is total number of questions in the exam.
- $a_i$ is the student's selected option for question $i$.
- $k_i$ is the correct answer key for question $i$.
- $\delta(a_i, k_i) = 1$ if $a_i = k_i$, else $0$.
- $w_i$ is the weight (marks) allocated to question $i$.

### Leaderboard Sorting Tuple
Leaderboard entries are sorted according to strict priority tuple:
$$\mathcal{O} = (\text{Score Obtained} \downarrow, \text{Time Taken Seconds} \uparrow, \text{Submission Timestamp} \uparrow)$$

---

## 7. Verification and Test Results
The automated unit test suite (`tests/TestRunner.java`) executed 33 rigorous assertions:
- **Quiz Management**: Validation of title constraints, duration boundaries, and default active state.
- **Question Bank**: Multi-option indexing (1 to 4), weight validation, and retrieval by quiz foreign key.
- **Grading Engine**: Perfect score verification (100%), failing score below threshold (50% < 75%), and handling of skipped/unanswered questions.
- **Leaderboard Tie-Breaking**: Proper ranking of candidates with identical scores based on completion speed.
- **Result**: **33 / 33 assertions passed (100% success rate)**.

