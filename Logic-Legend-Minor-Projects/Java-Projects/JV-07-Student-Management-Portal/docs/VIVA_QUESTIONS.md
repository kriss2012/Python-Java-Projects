# Viva Voce Examination Questions & Answers — Student Management Portal (JV-07)

---

### Q1: How does the system calculate credit-weighted CGPA, and why is simple arithmetic mean insufficient?
**Answer**: In university curricula, courses carry differing credit weights depending on lecture and laboratory hours (e.g., a 4-credit Core Systems course vs a 1-credit Seminar). A simple arithmetic average of grade points treats all courses equally, distorting academic performance. The credit-weighted Cumulative Grade Point Average (CGPA) formula is:
$$\text{CGPA} = \frac{\sum_{i=1}^{N} (\text{Credits}_i \times \text{Grade Points}_i)}{\sum_{i=1}^{N} \text{Credits}_i}$$
This ensures that performance in heavier credit subjects exerts proportionally greater influence on the student's cumulative ranking.

---

### Q2: What happens when a new course grade is added to a student's record?
**Answer**: When `StudentService.addCourseGrade()` executes:
1. It validates the course code, course name, credits ($\ge 1$), and converts the letter grade to numeric grade points (e.g. `O` $\to 10.0$, `A` $\to 9.0$).
2. Inserts the record into the `course_grades` table.
3. Automatically triggers `recalculateCgpa(studentId)`, which aggregates all course grades currently earned by the student, computes the new weighted average, and updates the `cgpa` column in the `students` master table.
This guarantees that student list queries and transcripts always reflect the most up-to-date academic metrics without expensive runtime recalculations.

---

### Q3: How is server-side input validation implemented for emails and phone numbers?
**Answer**: Validation is enforced using compiled regular expressions (`java.util.regex.Pattern`):
- **Email**: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`
- **Phone**: `^[0-9+ -]{10,15}$`
If input parameters do not match these specifications, `StudentService` throws an `IllegalArgumentException` prior to executing any SQL statement, preventing corrupt records from entering the database.

---

### Q4: How does the system separate Semester GPA (SGPA) from Cumulative CGPA?
**Answer**:
- **SGPA**: Computed by filtering course grades matching a specific semester number:
  $$\text{SGPA}_s = \frac{\sum_{\text{sem}=s} (\text{Credits} \times \text{Points})}{\sum_{\text{sem}=s} \text{Credits}}$$
- **CGPA**: Aggregates all courses across all semesters completed by the student to date.
In the official transcript, each semester section concludes with its specific SGPA, while the bottom footer displays the final overall CGPA.

---

### Q5: How does the portal handle student pagination and multi-parameter filtering?
**Answer**: In `StudentService.searchStudents()`:
1. Dynamic SQL is built with conditional `WHERE` clauses for search keyword (matching `reg_number`, `name`, or `email`), `department`, and `academic_year`.
2. Pagination is handled at the database level using SQL `LIMIT ? OFFSET ?`, where:
   $$\text{OFFSET} = (\text{Page Number} - 1) \times \text{Page Size}$$
This ensures high query efficiency even with thousands of student records without loading the entire table into JVM memory.

---

### Q6: How does the system implement cascading deletions when a student profile is removed?
**Answer**: The database schema defines foreign key constraints on `course_grades`:
```sql
FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
```
When a student record is deleted from the `students` table, the relational database automatically deletes all associated course grade records, maintaining referential integrity and eliminating orphaned rows.

---

### Q7: Why are registration numbers and emails enforced with UNIQUE constraints?
**Answer**: A university registration number (e.g., `21CS101`) and official institutional email address represent unique identifiers for each student. A `UNIQUE` B-tree index in the database prevents administrative double-enrollments and ensures $O(\log N)$ fast lookup times when searching or authenticating students.

---

### Q8: How can this application be deployed with enterprise MySQL instead of SQLite in production?
**Answer**: The `DatabaseManager` dynamically evaluates the `DB_TYPE` environment variable. If set to `mysql`, it establishes a connection using `com.mysql.cj.jdbc.Driver` to the configured MySQL host and database. The project includes `sql/schema_mysql.sql` containing equivalent InnoDB table definitions.

---

### Q9: What is the purpose of the `/api/students` REST endpoint?
**Answer**: Modern educational institutions require microservice interoperability. The `/api/students` endpoint serializes the active student directory into standard JSON format (`application/json`), allowing external consumers (such as mobile attendance apps, parent notification systems, or placement portals) to consume student academic records programmatically.

---

### Q10: How does the application format academic transcripts for printing?
**Answer**: `StudentService.generateTranscriptText()` formats the transcript using fixed-width monospaced columns (`%-4s | %-12s | %-34s | %-6d | %-6s | %,10.2f`). In the web interface, the transcript is wrapped in a monospaced `<pre>` element with a CSS print stylesheet and a `window.print()` button, enabling clean, professional hardcopy printouts on standard A4 paper.

---

### Q11: How does the application support headless Linux servers or command-line testing?
**Answer**: The application includes a full Command-Line Interface (`--cli`). In this mode, users can list students in ASCII tables, enroll new profiles, search records, record course grades, view department analytics, and export official transcripts to text files directly from the command line.

---

### Q12: What is the time complexity of the department analytics aggregation query?
**Answer**: The department analytics query uses SQL `GROUP BY department`:
```sql
SELECT department, COUNT(*), AVG(cgpa), MAX(cgpa) FROM students GROUP BY department;
```
Because the number of distinct departments in a university is small (typically 5 to 20), the aggregation executes in $O(N)$ linear time where $N$ is the total student count, completing in less than 2 milliseconds.

---

### Q13: How does the system handle students who have not yet completed any courses?
**Answer**: When a student is enrolled, their initial `cgpa` is set to `0.0`. If `recalculateCgpa()` is called when no course grades exist, it safely updates `cgpa` to `0.0` without dividing by zero, assigning the academic status to `"ACTIVE"` or `"Second Class"`.

---

### Q14: Why is PreparedStatement parameterization critical in the student portal?
**Answer**: Student names, search queries, and registration numbers are supplied by end users. Parameterized `PreparedStatement` interfaces ensure that user inputs are treated strictly as data literals and never compiled as SQL commands, completely neutralizing SQL Injection vulnerabilities.

---

### Q15: How are academic standing classifications assigned?
**Answer**: The method `Student.getAcademicStanding()` evaluates the student's cumulative CGPA:
- $\text{CGPA} \ge 9.0$: Distinction / First Class with Exemplary
- $8.0 \le \text{CGPA} < 9.0$: First Class with Distinction
- $6.5 \le \text{CGPA} < 8.0$: First Class
- $5.0 \le \text{CGPA} < 6.5$: Second Class
- $\text{CGPA} < 5.0$: Academic Probation
This provides immediate academic standing visibility on profile cards and official transcripts.

