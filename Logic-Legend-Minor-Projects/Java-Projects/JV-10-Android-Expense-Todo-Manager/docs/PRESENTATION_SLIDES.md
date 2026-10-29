# Presentation Slides: Android Expense and To-Do Manager (JV-10)

---

## Slide 1: Title Slide
- **Project Title**: Android Expense and To-Do Manager
- **Project Code**: JV-10
- **Domain**: Mobile Computing / Personal Financial Management
- **Technologies**: Android SDK, Java 17, Room Database, SQLite, MPAndroidChart, Swing Desktop Companion
- **Presenter**: Engineering Candidate / Mentor

---

## Slide 2: Problem Statement
- Individuals frequently maintain disconnected apps for tracking expenditures and managing daily tasks.
- Personal budget overspending typically goes unnoticed until the end of the billing cycle.
- Many mobile projects require complex Android Studio environments, making academic evaluation and testing difficult.

---

## Slide 3: Objectives & Solution
- Build an integrated mobile application combining personal budgeting and prioritized task tracking.
- Implement Android Architecture Components: Room Database, DAOs, Repositories, ViewModels/Services.
- Provide automated budget threshold warnings (Safe, Warning $\ge$ 80%, Exceeded $\ge$ 100%).
- Visualize expenses using MPAndroidChart pie/bar representations.
- Bundle a Desktop Swing Companion & CLI for zero-configuration testing on standard JDK.

---

## Slide 4: System Architecture
- **Presentation Layer**:
  - Android: `MainActivity`, `ExpenseTrackerFragment`, `TodoListFragment`, `AnalyticsFragment`.
  - Desktop: `DesktopManagerFrame` (Swing), `Main` (Interactive CLI).
- **Service / ViewModel Layer**:
  - `ExpenseService`, `TaskService`.
- **Data Repository Layer**:
  - `ExpenseRepository`, `TaskRepository`.
- **Data Access & Storage**:
  - `ExpenseDao`, `TaskDao`, `BudgetDao`, `DatabaseManager` (SQLite).

---

## Slide 5: Room Database & Schema Design
- `expenses`: `(id, title, amount, category, expense_date, payment_method, notes, created_at)`
- `tasks`: `(id, title, description, priority, due_date, is_completed, linked_category, created_at)`
- `budgets`: `(id, month_year, category, budget_limit, created_at)` with `UNIQUE(month_year, category)`.

---

## Slide 6: Budget Threshold Warning Logic
- $\text{Remaining} = \text{Budget Limit} - \text{Total Category Spent}$
- $\text{Usage} = (\text{Total Category Spent} / \text{Budget Limit}) \times 100$
- **Three Alert Tiers**:
  1. **SAFE**: $\text{Usage} < 80\%$ (Green)
  2. **WARNING**: $80\% \le \text{Usage} < 100\%$ (Amber)
  3. **EXCEEDED**: $\text{Usage} \ge 100\%$ (Red)

---

## Slide 7: To-Do Task State Machine & Priorities
- Priority weighting: `HIGH` (Weight 1) $\rightarrow$ `MEDIUM` (Weight 2) $\rightarrow$ `LOW` (Weight 3).
- State transitions: `[PENDING]` $\leftrightarrow$ `[DONE]`.
- Dynamic overdue calculation: flags tasks where `due_date < current_date` and `is_completed == false`.

---

## Slide 8: Visual Analytics (MPAndroidChart)
- Aggregates category expenditure over monthly windows.
- Computes percentage contribution per spending category.
- Feeds data directly into `PieChart` and `BarChart` views with interactive slice legends.

---

## Slide 9: Verification & Automated Testing
- `TestRunner.java` executing 54 assertions:
  - Validation: empty titles, negative amounts, bad date formats.
  - Budget calculations: SAFE, WARNING, and EXCEEDED states.
  - Task workflows: completion toggle, overdue checks, priority sorting.
  - Analytics & CSV exports: total percentage sum, file generation.
- **Result**: 54 / 54 tests passing (100.0%).

---

## Slide 10: Conclusion & Future Scope
- **Conclusion**: A production-grade mobile architecture project delivering proactive budget alerts and task productivity with zero-emulator desktop portability.
- **Future Scope**:
  - SMS auto-parser for bank transaction notification scraping.
  - Cloud synchronization via Firebase Firestore.
  - Biometric fingerprint lock for personal financial privacy.

