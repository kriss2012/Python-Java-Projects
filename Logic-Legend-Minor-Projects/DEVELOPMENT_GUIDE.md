# Logic Legend Minor Projects — Master Development & Setup Guide

This guide details how to install dependencies, initialize databases, run applications, and execute test suites across the complete suite of 20 projects.

---

## 1. Prerequisites and System Environment

- **Operating System**: Windows 10/11, Linux (Ubuntu/Debian), or macOS.
- **Python**: Python 3.10 or higher (Python 3.12 recommended).
- **Java**: Java Development Kit (JDK) 17 or JDK 21 LTS (`javac` and `java` available in PATH).
- **Database**:
  - SQLite (built into Python; embedded SQLite JDBC included/configured for Java desktop projects).
  - MySQL 8.x (optional for Java projects; fallback embedded SQLite / H2 configurations provided so every project runs immediately without requiring a running local MySQL daemon).
- **Tools**: Git, modern web browser (Chrome, Firefox, or Edge).

---

## 2. Setting Up Python Projects

Each Python project has its own virtual environment and `requirements.txt`.
You can set up any Python project in its respective directory:

```powershell
# Navigate to the project directory
cd Logic-Legend-Minor-Projects/Python-Projects/PY-01-Smart-Expense-Tracker

# Create a virtual environment
python -m venv venv

# Activate the virtual environment (Windows PowerShell)
.\venv\Scripts\Activate.ps1
# (Or on Linux/macOS: source venv/bin/activate)

# Install required dependencies
pip install -r requirements.txt

# Run the test suite
pytest tests/ -v

# Launch the application
python src/main.py
```

---

## 3. Setting Up Java Projects

Java projects are organized cleanly into Maven-compatible directory structures (`src/main/java`, `src/test/java`).
For desktop Swing applications, standard compilation scripts (`build.bat` / `run.bat` or `javac`) are provided for instantaneous zero-friction execution without needing complicated IDE setups:

```powershell
# Navigate to the project directory
cd Logic-Legend-Minor-Projects/Java-Projects/JV-01-Banking-ATM-Simulation

# Compile the project
javac -d bin src/main/java/com/logiclegend/banking/**/*.java src/main/java/com/logiclegend/banking/*.java

# Run the application
java -cp bin com.logiclegend.banking.Main
```

For Spring Boot projects (JV-07, JV-08):
```powershell
cd Logic-Legend-Minor-Projects/Java-Projects/JV-07-Student-Management-Portal
mvn clean spring-boot:run
```

---

## 4. Documentation & Deliverables

Every single project contains a dedicated `docs/` folder with:
- **`ACADEMIC_REPORT.md`**: Complete report conforming to university project submission standards (Abstract, Introduction, DFD, ER diagram, Architecture, Implementation, Test Cases, Results, Limitations, References).
- **`PRESENTATION_SLIDES.md`**: 8–12 slide viva presentation outline.
- **`VIVA_QUESTIONS.md`**: 15+ university viva questions with concise technical explanations.
- **`DEMO_GUIDE.md`**: Step-by-step evaluator demonstration flow.

