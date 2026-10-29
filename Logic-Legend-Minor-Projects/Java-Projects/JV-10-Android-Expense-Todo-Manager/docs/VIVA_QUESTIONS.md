# Faculty Viva Questions & Answers: Android Expense and To-Do Manager (JV-10)

### Q1: What is the primary purpose and scope of the Android Expense and To-Do Manager?
**Answer**: The project integrates personal financial expense tracking and task management into a single mobile and desktop solution. It allows users to monitor itemized expenses, establish monthly category budgets, receive early warning threshold alerts (at 80% and 100% capacity), manage prioritized to-dos with due dates, and visualize spending patterns.

### Q2: What are Android Architecture Components, and how are they used in this project?
**Answer**: Android Architecture Components are a collection of libraries that guide robust, testable, and maintainable app design. In this project:
- **Room Database / SQLite**: Provides object-relational mapping and database abstraction.
- **DAOs (`ExpenseDao`, `TaskDao`, `BudgetDao`)**: Define SQL operations and queries.
- **Repositories (`ExpenseRepository`, `TaskRepository`)**: Provide a single source of truth mediating between database sources and the application.
- **Service / ViewModel Layer**: Encapsulates business logic, budget alerts, and state management.

### Q3: How does the budget threshold alert engine work?
**Answer**: Whenever an expense is recorded or the user checks their budget status, the system calculates:
$$\text{Usage Percentage} = \left(\frac{\text{Total Category Spent}}{\text{Monthly Budget Limit}}\right) \times 100$$
- If usage is below 80%, the status is `SAFE`.
- If usage is between 80% and 99.9%, the status is `WARNING` (alerting the user before money runs out).
- If usage reaches or exceeds 100%, the status is `EXCEEDED` (signaling overspending).

### Q4: How is the application able to run on desktop without an Android emulator?
**Answer**: The architecture strictly separates core business models, DAOs, repositories, and services from the Android framework UI. The persistence layer operates over SQLite using standard JDBC (`sqlite-jdbc.jar`). A companion Swing GUI (`DesktopManagerFrame.java`) and a console CLI (`Main.java`) expose the identical service layer, allowing faculty and mentors to run and evaluate the application immediately on any desktop with standard JDK 17+.

### Q5: How are To-Do tasks prioritized and ordered?
**Answer**: Tasks are assigned a priority rating (`HIGH`, `MEDIUM`, or `LOW`). In SQL queries, tasks are ordered using a `CASE` statement:
```sql
ORDER BY is_completed ASC,
         CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC,
         due_date ASC
```
This guarantees pending tasks appear first, ordered by priority urgency, followed by the earliest due date.

### Q6: How does the system detect overdue tasks?
**Answer**: Overdue tasks are identified by querying for tasks that are not yet completed (`is_completed = 0`) and whose due date precedes the current date (`due_date < ?`, bound to today's date formatted as `YYYY-MM-DD`).

### Q7: How does the database prevent duplicate budget entries for the same category and month?
**Answer**: The `budgets` table defines a unique composite constraint: `UNIQUE(month_year, category)`. When updating a budget, an `ON CONFLICT(month_year, category) DO UPDATE SET budget_limit = excluded.budget_limit` clause is used, performing an upsert operation.

### Q8: What library is utilized for visual charts in Android?
**Answer**: **MPAndroidChart** (v3.1.0), an open-source Android chart and graph view library. It is integrated into `AnalyticsFragment.java` to render interactive pie and bar charts showing category spending breakdowns.

### Q9: How is data exported, and in what format?
**Answer**: The `ExpenseService.exportExpensesToCsv` method queries all expenses or month-filtered expenses and serializes them into Comma-Separated Values (CSV) format with sanitized quotes (`"..."`), saving the file into the `exports/` folder.

### Q10: How does the application prevent SQL injection?
**Answer**: Every query across all DAOs uses `PreparedStatement` with parameterized placeholders (`?`). Values are bound using strongly-typed methods (`setString`, `setDouble`, `setInt`), preventing SQL manipulation through user input.

### Q11: What happens if an expense has a negative or zero amount?
**Answer**: The service layer validates input before querying the database and throws an `IllegalArgumentException("Expense amount must be positive.")`. Furthermore, the SQLite schema enforces a check constraint: `amount REAL NOT NULL CHECK(amount > 0)`.

### Q12: How are dates standardized across the application?
**Answer**: Dates are formatted using the ISO-8601 standard `YYYY-MM-DD` (e.g., `2026-10-29`), and months are represented as `YYYY-MM`. This standard format allows direct lexicographical comparisons and SQL string filtering (`WHERE expense_date LIKE '2026-10%'`).

### Q13: Can a task be linked to a budget category?
**Answer**: Yes. The `TaskEntity` contains an optional `linked_category` field (such as "Bills & Utilities" or "Food & Dining"). This allows tasks like "Pay Electricity Bill" to link directly with the corresponding expense budget category.

### Q14: What design pattern is used between the UI and database?
**Answer**: The **Repository Pattern**. The UI does not query DAOs directly; instead, it communicates through `ExpenseRepository` and `TaskRepository`. This decouples the presentation from the storage mechanism, making it easy to swap local SQLite with remote cloud APIs without changing the UI code.

### Q15: How were the automated tests implemented?
**Answer**: `TestRunner.java` contains 54 comprehensive unit and integration tests executing against an isolated SQLite test database (`data/test_expensetodo.db`). It validates expense persistence, budget threshold states (`SAFE`, `WARNING`, `EXCEEDED`), task state toggling, overdue detection, analytical percentages, and CSV file exports.

