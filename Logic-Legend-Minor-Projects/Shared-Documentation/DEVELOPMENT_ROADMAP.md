# Logic Legend — Development Roadmap & Standards

## 1. Architectural Philosophy

Every project in the **Logic Legend Minor Project Suite** adheres to professional software engineering standards adapted for college evaluation:
1. **Separation of Concerns (SoC)**: Strict division between presentation/UI layer, domain/business logic, data access (DAO / Repository), and configuration.
2. **Persistence First**: Every project uses real persistent databases (SQLite for embedded desktop/lightweight web; MySQL / H2 for enterprise Java projects; Room for Android). No volatile, in-memory-only mockups.
3. **Robust Input Validation & Error Handling**: Comprehensive defensive programming prevents unexpected crashes from dirty input, missing records, or external network failures.
4. **Offline Capability & Graceful Degradation**: Where external hardware (webcam, microphone) or external cloud services (API keys, SMTP servers) are optional or unavailable, clean fallback / demo modes are provided.
5. **No Hardcoded Secrets**: Configuration is managed via `.env` templates, system environment variables, or external configuration properties.

---

## 2. Universal Directory Hierarchy

Each project follows an isolated, self-contained structure:
```text
<PROJECT-ID>-<Project-Name>/
├── src/                  # Production application source code
│   ├── models/           # Domain models and entities
│   ├── controllers/      # Routing / UI event handling
│   ├── services/         # Core business logic
│   ├── database/         # Schemas, migrations, seed data, DB helpers
│   └── ui/               # Templates, static assets, or GUI classes
├── tests/                # Automated unit and integration tests
├── data/                 # Sample datasets, SQLite databases, exports
├── docs/                 # Academic report, slides, viva Q&A, demo guide
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── requirements.txt      # (For Python projects) Pinned dependencies
├── pom.xml / build.gradle# (For Java / Android projects)
├── .env.example          # Environment variable template
├── .gitignore            # Clean VCS exclusions
└── README.md             # 19-section comprehensive project README
```

---

## 3. Technology Stack Reference

### Python Stack
- **Runtime**: Python 3.10+ (tested on Python 3.12)
- **GUI & Web Frameworks**: Tkinter (standard library), Flask, Streamlit
- **Data & Analytics**: Pandas, NumPy, Matplotlib, Seaborn, Plotly
- **Machine Learning & NLP**: scikit-learn, NLTK, VADER, joblib
- **Automation & Security**: Requests, BeautifulSoup4, cryptography (Fernet/PBKDF2), pyttsx3, SpeechRecognition, OpenCV

### Java Stack
- **Runtime**: OpenJDK 17 / 21 LTS
- **UI Frameworks**: Java Swing, JavaFX, Thymeleaf HTML5/CSS3
- **Database & Persistence**: JDBC, SQLite JDBC Driver, MySQL Connector/J, Spring Data JPA, Hibernate, Android Room
- **Enterprise & Web**: Spring Boot 3, Spring Web, Spring Security, OpenAPI/Swagger
- **Mobile**: Android SDK, Java 17, MPAndroidChart, Room ORM
- **Testing**: JUnit 5, Mockito
