# Viva Voce Examination Questions & Answers — Online Quiz and Examination Portal (JV-04)

---

### Q1: Why did you choose Java's built-in `HttpServer` instead of Apache Tomcat or Spring Boot for this project?
**Answer**: Java SE provides the lightweight `com.sun.net.httpserver.HttpServer` directly in the runtime (`jdk.httpserver` module). For an examination portal microservice, this eliminates heavy external servlet container dependencies (such as downloading a 100MB Tomcat distribution or managing WAR deployments). It starts in under 200 milliseconds, has a negligible memory footprint (~35MB RAM), and makes the application 100% portable on any standard JDK installation.

---

### Q2: How does the system prevent candidates from viewing correct answers using browser Developer Tools / Inspect Element?
**Answer**: The correct answer key (`correct_option`) and explanation strings are strictly preserved on the server side and never injected into the client HTML or JavaScript payloads sent to `/exam`. When `/exam` is rendered, the server only outputs the question texts and options A, B, C, D. Only upon POST submission to `/submit-exam` does the server retrieve the answer keys from the database, compute the score, and return the rationale in the post-test evaluation response.

---

### Q3: What happens if a student runs out of time during the test?
**Answer**: The candidate examination interface includes a JavaScript client timer that tracks the elapsed and remaining duration in seconds. When the remaining seconds reach zero (`remainingSeconds <= 0`), the timer triggers `document.getElementById('examForm').submit()`, automatically dispatching the candidate's currently selected answers to the server without requiring manual intervention.

---

### Q4: How is candidate ranking decided on the leaderboard in the event of a tie in scores?
**Answer**: The system applies a multi-column tie-breaker in SQL:
```sql
ORDER BY score_obtained DESC, time_taken_seconds ASC, submitted_at ASC
```
If two candidates obtain the exact same score (e.g., 20/20 marks), the student who completed the test in fewer seconds ranks higher. If the completion times are also identical, the earlier submission timestamp is preferred.

---

### Q5: How does the system handle skipped or unanswered questions?
**Answer**: When a candidate skips a question, the form parameter for that question is omitted or mapped to null/0. During server-side evaluation in `QuizService.submitExam()`, the algorithm checks whether `answers.get(questionId)` matches `q.getCorrectOption()`. If the key is absent or non-matching, zero points are credited for that question without causing a `NullPointerException` or arithmetic crash.

---

### Q6: How is ACID compliance maintained during exam submission?
**Answer**: Exam submission is recorded via a parameterized `PreparedStatement` within an atomic transaction. Database write operations verify that the referenced `quiz_id` foreign key is valid. The submission record contains both the derived quantitative scores (`score_obtained`, `percentage`, `result_status`) and an immutable JSON payload representing the exact candidate responses for forensic auditability.

---

### Q7: How does the system validate administrative question additions?
**Answer**: `QuizService.addQuestion()` enforces several structural validation rules:
1. Question text and all 4 options must not be null or whitespace.
2. `correct_option` must be an integer between 1 and 4 inclusive (representing Option A, B, C, or D).
3. `marks` must be strictly greater than 0.
Violations trigger an `IllegalArgumentException` prior to executing any SQL query.

---

### Q8: How can this application be deployed with MySQL instead of SQLite in production?
**Answer**: The `DatabaseManager` dynamically checks the `DB_TYPE` environment variable. If set to `mysql`, it reads `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, and `MYSQL_PASSWORD`, connecting via `jdbc:mysql://...`. The project includes `sql/schema_mysql.sql` with equivalent table definitions, primary keys, foreign keys, and indexes.

---

### Q9: What is the time complexity of the exam grading algorithm?
**Answer**: Let $Q$ be the number of questions in a quiz. Fetching questions from the database takes $O(Q)$ time. Populating the candidate answer lookup map takes $O(A)$ where $A \le Q$. Iterating through questions to match keys and sum scores takes $O(Q)$ comparisons. Thus, the grading algorithm runs in $O(Q)$ linear time with $O(Q)$ auxiliary space, executing in less than 2 milliseconds for typical tests.

---

### Q10: How do you format and calculate the final passing status?
**Answer**:
$$\text{Percentage} = \frac{\text{Score Obtained}}{\text{Total Marks}} \times 100$$
If $\text{Percentage} \ge \text{Pass Percentage Threshold}$, the status is set to `"PASS"`; otherwise, it is assigned `"FAIL"`. The status is displayed with clear visual badges in the web portal.

---

### Q11: How does the application support headless Linux servers or environments without graphical browsers?
**Answer**: The application includes a full Command-Line Interface invoked via `java -cp "bin;lib/sqlite-jdbc.jar" com.logiclegend.quiz.Main --cli`. This interactive console allows users to list quizzes, take exams directly inside the terminal with keyboard input, review questions, create quizzes, and view leaderboards.

---

### Q12: Why are SQL PreparedStatements crucial in the exam submission pipeline?
**Answer**: Candidate names, email addresses, and form values are external user inputs. Concatenating user strings directly into SQL queries would expose the database to SQL Injection (e.g., `' OR '1'='1`). Parameterized `PreparedStatement` objects separate SQL syntax from untrusted data parameters, completely neutralizing SQL injection risks.

---

### Q13: Can a candidate modify their score by editing HTTP POST parameters in transit?
**Answer**: No. The client browser only submits the question IDs and selected option numbers (e.g., `q_1=2&q_2=3`). The marks per question, the correct keys, the total possible score, the percentage, and the pass/fail determination are entirely calculated and verified on the server side in `QuizService`.

---

### Q14: How does `QuizWebServer` handle multiple students taking exams simultaneously?
**Answer**: Java's `HttpServer` uses an internal thread executor (or thread pool) to dispatch incoming HTTP requests asynchronously across worker threads. Since each HTTP request executes in its own thread, concurrent test attempts are handled independently without blocking other students.

---

### Q15: How are test results archived for long-term college audits?
**Answer**: All submissions are persisted in the `exam_submissions` table with a unique submission reference code (`submission_ref`, e.g., `EXAM-6421`), student email, score breakdown, timestamp, and a JSON dump of all selected answers. Evaluators and auditors can query historical exam performances at any time.

