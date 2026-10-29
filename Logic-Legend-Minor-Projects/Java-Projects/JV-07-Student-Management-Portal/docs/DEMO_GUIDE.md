# Evaluator Demonstration Guide — Student Management Portal (JV-07)

A step-by-step walkthrough for faculty evaluators and examiners to test all student profile lifecycle, GPA/CGPA computation, transcript generation, and analytics capabilities of the **Student Management Portal**.

---

## 1. Quick Launch Verification
1. Open PowerShell / Command Prompt.
2. Navigate to project root:
   ```powershell
   cd "Logic-Legend-Minor-Projects\Java-Projects\JV-07-Student-Management-Portal"
   ```
3. Run the automated test suite to confirm zero defects:
   ```powershell
   java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
   ```
   **Expected Outcome**: 40 / 40 assertions PASS in under 1 second.

---

## 2. Web Portal Demonstration (Primary Evaluation Path)

### Step 1: Launch the Embedded Web Server
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.student.Main
```
**Console Output Confirmation**:
```
=================================================================
  LOGIC LEGEND JV-07: STUDENT MANAGEMENT PORTAL
=================================================================
  [SUCCESS] Embedded Web Server running at:
  >>> Student Directory: http://localhost:8087/
  >>> New Enrollment:    http://localhost:8087/students/new
  >>> Dept Analytics:    http://localhost:8087/analytics
  >>> REST API:          http://localhost:8087/api/students
=================================================================
```

### Step 2: Explore the Student Directory
1. Open your browser and navigate to `http://localhost:8087/`.
2. Notice the modern, responsive student table with pre-seeded students:
   - *Aakash Banerjee* (Computer Science, Year 3, CGPA: 9.64) — Distinction
   - *Meera Krishnan* (Electronics & Communication, Year 3, CGPA: 8.64) — Distinction
   - *Sneha Roy* (Information Technology, Year 1, CGPA: 9.57) — Distinction
3. Note the color-coded CGPA badges and academic classification pills.

### Step 3: Test Search and Department Filtering
1. In the search box, type `Meera` and click **"Filter Records"**.
   - Notice that only Meera Krishnan's record appears.
2. Clear the search box, select `Computer Science` in the department dropdown, and click **"Filter Records"**.
   - Only Computer Science students are listed.
3. Select `All Departments` to reset the directory.

### Step 4: Enroll a New Student Profile
1. Click **"+ Enroll New Student"** (or visit `/students/new`).
2. Fill out the enrollment form:
   - **Registration Number**: `23CS501`
   - **Full Name**: `Prof. Evaluator`
   - **Official Email**: `evaluator@college.edu`
   - **Phone Number**: `+91 9876543299`
   - **Department**: `Computer Science`
   - **Academic Year**: `Year 3 (Junior)`
3. Click **"Enroll Student"**.
4. You will be redirected to the home directory. Locate `Prof. Evaluator` in the table (initial CGPA: 0.00).

### Step 5: Record Course Grades & Verify Automated CGPA Recalculation
1. Click **"Grades"** next to `Prof. Evaluator` (or visit `/students/view?id=...`).
2. Notice the Profile Overview card on the left displaying student metadata.
3. In the **"+ Add Course Grade"** card on the right:
   - **Course Code**: `CS301`
   - **Course Name**: `Cloud Computing Architecture`
   - **Credits**: `4 Cr`
   - **Letter Grade**: `O (10.0)`
   - **Semester**: `Semester 3`
4. Click **"Record Grade & Recalculate CGPA"**.
5. Observe:
   - The course appears in the course history table with 4 Credits, Grade O, 10.0 Grade Points, and 40.0 Credit Points.
   - The Cumulative GPA badge immediately updates to **10.00 / 10.0**.
6. Add a second course:
   - **Course Code**: `CS302`, **Course Name**: `Distributed Systems`, **Credits**: `4 Cr`, **Grade**: `A (9.0)`, **Semester**: `Semester 3`.
7. Click **"Record Grade & Recalculate CGPA"**.
8. Observe:
   - Total weighted points = (4*10 + 4*9) = 76. Total credits = 8.
   - The Cumulative GPA badge automatically recalculates to **9.50 / 10.0**!

### Step 6: View & Print Official Academic Transcript
1. On the student profile page, click **"📄 Official Transcript"** (or `/students/transcript?id=...`).
2. Inspect the official academic transcript:
   - Official university letterhead.
   - Student demographics, department, and academic standing.
   - Semester breakdown with individual Semester SGPAs.
   - Total credits completed and final CGPA.
3. Click **"🖨️ Print Transcript"** to preview the formatted hardcopy output.
4. Verify that the transcript text was saved to `exports/TRANSCRIPT_23CS501.txt`.

### Step 7: Explore Department Performance Analytics
1. In the navigation bar, click **"Analytics"** (or visit `/analytics`).
2. Observe the aggregated metrics cards:
   - Computer Science: Headcount, Average CGPA, Top CGPA.
   - Electronics, Mechanical, Information Technology stats.
3. Review the consolidated department summary table.

### Step 8: Test RESTful JSON API
1. In your browser or curl/Postman, visit:
   `http://localhost:8087/api/students`
2. Verify that a valid JSON array of all enrolled student profiles with IDs, registration numbers, names, departments, and CGPAs is returned with `Content-Type: application/json`.

---

## 3. Terminal CLI Mode Demonstration (Headless Environment)
Run the application with `--cli`:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.student.Main --cli
```
- Select Option `1` to list all registered students in ASCII tables.
- Select Option `2` to search students.
- Select Option `3` to enroll a student from the terminal.
- Select Option `5` to record course grades and witness instant CGPA recalculation.
- Select Option `6` to generate and export academic transcripts to the `exports/` directory.
- Select Option `7` to view department performance analytics tables.

