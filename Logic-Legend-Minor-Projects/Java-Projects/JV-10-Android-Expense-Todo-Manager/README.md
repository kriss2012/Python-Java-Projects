# JV-10 — Android Expense and To-Do Manager

## 1. Project Overview
**Android Expense and To-Do Manager** is a cross-platform mobile and desktop productivity application built using modern **Android Architecture Components** (Java 17, Room Database, Repositories, ViewModels/Services, and MPAndroidChart). It unifies personal financial budgeting with daily task productivity into a single cohesive system. Users can record expenditures by category, establish monthly budget limits, receive real-time threshold alert warnings (Safe, Warning $\ge$ 80%, Exceeded $\ge$ 100%), manage prioritize To-Do reminders with due dates, inspect visual spending breakdown charts, and export ledgers to CSV.

To guarantee effortless evaluation without requiring Android Studio, Gradle mobile downloads, or an Android Virtual Device (AVD), the application includes a **Desktop Swing Companion GUI** and an **Autonomous Terminal CLI** that execute the exact same Room-compatible SQLite database and business layer out of the box.

---

## 2. Key Features & Business Logic
- **Dual Expense & Task Management**: Centralized hub bridging personal finance and task tracking with optional category linking.
- **Room Database Architecture**: Entity-DAO-Repository pattern enforcing strict data types, auto-increment keys, and foreign relational schemas.
- **Monthly Budget Threshold Alert Engine**:
  - $\text{Usage} < 80\% \rightarrow \textbf{SAFE}$ (Normal state, green indicator)
  - $80\% \le \text{Usage} < 100\% \rightarrow \textbf{WARNING}$ (Threshold warning alert, amber indicator)
  - $\text{Usage} \ge 100\% \rightarrow \textbf{EXCEEDED}$ (Over-budget alarm, red indicator)
- **To-Do Priority State Machine**: Tasks categorized into `HIGH`, `MEDIUM`, and `LOW` priorities, with completion state toggling (`[DONE]` vs `[PENDING]`) and automated overdue detection against current date.
- **Visual Analytics (MPAndroidChart)**: Category breakdown aggregation computing total outlay, transaction frequencies, and percentage share.
- **CSV Data Exporter**: One-click filesystem export of monthly or all-time expenses to `exports/EXPENSES_<MONTH>.csv`.
- **Zero-Emulator Portability**: Instant desktop evaluation through Swing GUI or terminal CLI.

---

## 3. Technology Stack & Prerequisites
- **Language**: Java 17+ (LTS)
- **Mobile Architecture**: Android Architecture Components (Room Database, Lifecycle, ViewModel, FragmentStateAdapter)
- **Visualization**: MPAndroidChart (v3.1.0) for Android / Java2D Swing Charts
- **Embedded Database**: SQLite 3.36+ (Zero-dependency embedded JDBC driver bundled in `lib/`)
- **Production Database**: MySQL 8.0+ compatible (`sql/schema_mysql.sql` included)
- **Build Systems**: Gradle Kotlin DSL (`build.gradle.kts`) for Android + Maven (`pom.xml`) / `javac` for desktop
- **Prerequisites**: Standard JDK 17 or JDK 21 installed and added to `PATH`.

---

## 4. Directory Structure
```
JV-10-Android-Expense-Todo-Manager/
├── build.gradle.kts
├── settings.gradle.kts
├── pom.xml
├── .env.example
├── .gitignore
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/ (strings.xml, colors.xml, themes.xml)
│       │   └── layout/ (activity_main.xml)
│       └── java/com/logiclegend/expensetodo/ui/
│           ├── MainActivity.java
│           ├── ExpenseTrackerFragment.java
│           ├── TodoListFragment.java
│           └── AnalyticsFragment.java
├── lib/
│   └── sqlite-jdbc.jar
├── sql/
│   ├── schema_sqlite.sql
│   └── schema_mysql.sql
├── data/
│   └── expensetodo.db
├── exports/
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── src/
│   └── com/logiclegend/expensetodo/
│       ├── Main.java
│       ├── model/
│       │   ├── ExpenseEntity.java
│       │   ├── TaskEntity.java
│       │   ├── BudgetEntity.java
│       │   ├── BudgetStatus.java
│       │   └── CategorySummary.java
│       ├── dao/
│       │   ├── DatabaseManager.java
│       │   ├── ExpenseDao.java
│       │   ├── TaskDao.java
│       │   └── BudgetDao.java
│       ├── repository/
│       │   ├── ExpenseRepository.java
│       │   └── TaskRepository.java
│       ├── service/
│       │   ├── ExpenseService.java
│       │   └── TaskService.java
│       └── ui/
│           └── DesktopManagerFrame.java
├── tests/
│   └── TestRunner.java
└── README.md
```

---

## 5. Database Architecture & Schema
The relational database mirrors the Android Room SQLite entities:
- `expenses`: Stores transactions with `amount`, `category`, `expense_date`, `payment_method`, and `notes`.
- `tasks`: Stores to-dos with `priority`, `due_date`, `is_completed`, and optional `linked_category`.
- `budgets`: Stores monthly limits per category with a unique constraint on `(month_year, category)`.

---

## 6. Installation & Quick Start
### 6.1 Clone & Navigate
```bash
cd "Logic-Legend-Minor-Projects/Java-Projects/JV-10-Android-Expense-Todo-Manager"
```

### 6.2 Compilation (Standard Java 17/21)
```powershell
if (-not (Test-Path bin)) { New-Item -ItemType Directory -Path bin }
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;src" -d bin src\com\logiclegend\expensetodo\model\*.java src\com\logiclegend\expensetodo\dao\*.java src\com\logiclegend\expensetodo\repository\*.java src\com\logiclegend\expensetodo\service\*.java src\com\logiclegend\expensetodo\ui\DesktopManagerFrame.java src\com\logiclegend\expensetodo\Main.java tests\TestRunner.java
```

### 6.3 Run Graphical Desktop GUI (Zero Emulator Needed)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.expensetodo.Main
```

### 6.4 Run Interactive Command-Line Interface (CLI Mode)
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" com.logiclegend.expensetodo.Main --cli
```

---

## 7. Running Automated Tests
The automated test runner executes 54 assertions verifying Room entities, budgets, calculations, task lifecycles, and analytical summaries:
```powershell
java -cp "bin;lib\sqlite-jdbc.jar" tests.TestRunner
```
Expected output:
```
=================================================================
  TESTS FINISHED: 54 / 54 PASSED (100.0%)
=================================================================
>>> ALL JV-10 EXPENSE & TO-DO MANAGER TESTS PASSED! <<<
```

---

## 8. Application Walkthrough & Tabs
1. **KPI Dashboard Cards**: Top banner displays real-time `Total Month's Expenses`, `Budget Health`, and count of `Pending Tasks`.
2. **Tab 1: Expenses & Ledger**: View, filter by category, add new expenses, and export CSV.
3. **Tab 2: To-Do Task Manager**: Sort tasks by priority (`HIGH`, `MEDIUM`, `LOW`), filter by completion or overdue state, and toggle status.
4. **Tab 3: Monthly Budgets & Alerts**: Inspect category limits, actual expenditure, remaining balance, usage percentages, and color-coded alert badges (`SAFE`, `WARNING`, `EXCEEDED`).
5. **Tab 4: Category Analytics**: High-level aggregated breakdown representing the MPAndroidChart data source.

---

## 9. Budget Alert Logic Matrix
| Usage Percentage | Alert Status | Indicator Color | Recommended User Action |
| :--- | :--- | :--- | :--- |
| **0.0% – 79.9%** | `SAFE` | Green | Normal spending within limit |
| **80.0% – 99.9%** | `WARNING` | Amber / Orange | Caution: Approaching budget ceiling |
| **$\ge$ 100.0%** | `EXCEEDED` | Red | Alert: Spending ceiling breached |

---

## 10. Security & Data Integrity
- Parameterized SQL queries prevent SQL injection across all operations.
- Input validation rejects non-positive expense amounts, empty titles, and invalid date formats.
- Budget conflict updates (`ON CONFLICT(month_year, category) DO UPDATE`) prevent duplicate monthly entries while enabling seamless budget adjustments.
- Offline-first storage ensures complete personal privacy with zero remote server data harvesting.

---

## 11. Troubleshooting & FAQs
- **Q: How to build in Android Studio?**
  - Open the project folder in Android Studio. Gradle will sync dependencies (`Room`, `MPAndroidChart`, `Material`). Run on an Android emulator or physical device via USB debugging.
- **Q: How to run without Android Studio on Windows/Mac/Linux?**
  - Run `Main.java` using the bundled `sqlite-jdbc.jar`. The companion GUI renders identical state and business logic without any mobile SDK requirement.
- **Q: Where are exported CSV files stored?**
  - Files are written to the `exports/` folder named `EXPENSES_<MONTH>.csv`.

---

## 12. Evaluation & Viva Guide
Refer to [docs/VIVA_QUESTIONS.md](file:///c:/D%20Drive/Python+Java=Projects/Logic-Legend-Minor-Projects/Java-Projects/JV-10-Android-Expense-Todo-Manager/docs/VIVA_QUESTIONS.md) for 15+ faculty viva questions and [docs/DEMO_GUIDE.md](file:///c:/D%20Drive/Python+Java=Projects/Logic-Legend-Minor-Projects/Java-Projects/JV-10-Android-Expense-Todo-Manager/docs/DEMO_GUIDE.md) for step-by-step evaluator instructions.

