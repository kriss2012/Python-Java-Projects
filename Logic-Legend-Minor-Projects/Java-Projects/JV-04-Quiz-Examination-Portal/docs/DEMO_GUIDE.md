# Evaluator Demonstration Guide — Online Quiz and Examination Portal (JV-04)

This step-by-step walkthrough is designed for college faculty evaluators and examiners to test all core functionalities of the **Online Quiz and Examination Portal**.

---

## 1. Quick Launch Checklist
1. Open PowerShell / Command Prompt.
2. Navigate to the project root:
   ```powershell
   cd "Logic-Legend-Minor-Projects\Java-Projects\JV-04-Quiz-Examination-Portal"
   ```
3. Run the automated test suite to confirm zero defects:
   ```powershell
   java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
   ```
   **Expected Outcome**: 33 / 33 assertions PASS.

---

## 2. Web Portal Demonstration (Primary Evaluation Path)

### Step 1: Launch the Embedded Web Server
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.quiz.Main
```
**Console Output Confirmation**:
```
=================================================================
  LOGIC LEGEND JV-04: ONLINE QUIZ & EXAMINATION PORTAL
=================================================================
  [SUCCESS] Embedded Web Server running at:
  >>> http://localhost:8084/
  >>> Admin Dashboard: http://localhost:8084/admin
  >>> Leaderboard:     http://localhost:8084/leaderboard
=================================================================
```

### Step 2: Explore Available Quizzes
1. Open your browser and navigate to `http://localhost:8084/`.
2. Notice the clean, modern responsive dashboard displaying pre-seeded quizzes:
   - *Java Core Fundamentals Assessment*
   - *Object-Oriented Programming (OOP) in Java*
   - *Data Structures & Algorithms in Java*
3. Note the displayed metadata: Category, duration (in minutes), passing percentage requirement, and question count.

### Step 3: Attempt a Timed Examination
1. Click **"Start Exam"** on *Java Core Fundamentals Assessment*.
2. On the registration screen, enter:
   - **Candidate Name**: `Prof. Evaluator`
   - **Candidate Email**: `evaluator@university.edu`
3. Click **"Begin Examination"**.
4. Observe the candidate exam interface:
   - The live JavaScript countdown timer ticking down in the header.
   - The answering progress bar advancing as options are chosen.
   - 4-choice radio buttons for each question.
5. Answer questions:
   - Select correct answers for some, and deliberately pick an incorrect answer for another.
6. Click **"Submit Examination"**.

### Step 4: Verify Instant Evaluation & Pedagogical Review
1. Observe the immediate Results Dashboard:
   - **Status Badge**: Green `PASSED` or Red `FAILED` based on whether the score exceeds the cutoff percentage.
   - Quantitative breakdown: Score obtained, total marks, percentage, and test duration in minutes and seconds.
2. Scroll to the **Detailed Answer Review**:
   - Check that each question displays your chosen answer alongside the verified correct answer key.
   - Notice the green badge for correct responses and the red badge for incorrect responses.
   - Review the detailed educational explanation explaining why the correct choice is right.

### Step 5: Verify Global Leaderboard & Tie-Breaking
1. In the navigation bar, click **"Leaderboard"** or visit `http://localhost:8084/leaderboard`.
2. Select the *Java Core Fundamentals* quiz.
3. Observe the sorted ranks:
   - Highest scores appear at Rank #1.
   - Candidates with identical scores are ordered by fastest completion time.

### Step 6: Test Administrator Authoring Dashboard
1. Click **"Admin Dashboard"** or navigate to `http://localhost:8084/admin`.
2. Fill out the **"Create New Quiz"** form:
   - **Title**: `Advanced Concurrent Java`
   - **Category**: `Systems Programming`
   - **Duration**: `10` minutes
   - **Passing %**: `70`
3. Click **"Create Quiz"** and verify confirmation.
4. Fill out the **"Add Question"** form for your new quiz:
   - Enter question text, Option A, B, C, D, mark option index (1-4), marks, and rationale.
5. Click **"Add Question to Bank"**.
6. Return to Home (`/`) to verify your new quiz appears and is ready for candidates!

---

## 3. Terminal CLI Mode Demonstration (Headless Servers)

If testing on an environment without a browser, run:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.quiz.Main --cli
```
- Select Option `1` to list all quizzes in ASCII tables.
- Select Option `2` to take a simulated terminal exam with real-time question prompts and instant grade printouts.
- Select Option `3` to inspect the leaderboard ranking table.

