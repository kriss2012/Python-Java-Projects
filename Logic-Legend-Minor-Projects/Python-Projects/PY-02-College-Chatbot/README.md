# PY-02: AI Chatbot for College Queries
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-02`
- **Project Title**: AI Chatbot for College Queries
- **Domain**: Conversational Natural Language Processing & EdTech Web Applications
- **Platform**: Web (Flask, HTML5, CSS3, JavaScript) & Embedded SQLite

---

## 2. Project Overview
The AI Chatbot for College Queries is an intelligent conversational agent engineered to answer prospective and enrolled student questions regarding admissions, fees, timetables, examinations, placements, departments, and campus infrastructure. Featuring an administrative escalation workflow and dynamic knowledge base management, it bridges the gap between campus administration and students.

---

## 3. Problem Statement
Academic institutions receive thousands of repetitive inquiries every admissions cycle. Phone lines and front desks become overwhelmed, resulting in long response times, frustrated students, and staff burnout. Existing generic chatbots frequently hallucinate or fail to distinguish verified institutional policies from missing information.

---

## 4. Objectives
- Classify student natural language inquiries accurately using TF-IDF and Cosine Similarity.
- Present verified institutional answers with clear confidence scoring.
- Enforce confidence-aware fallback responses when queries are unrecognized.
- Provide a "Talk to Admin" human escalation workflow.
- Equip administrative personnel with a secure dashboard to manage FAQs and inspect student query logs.

---

## 5. Key Features
- **Conversational Interface**: Sleek chat UI with suggested prompts and timestamps.
- **NLP Intent Engine**: Preprocessing, TF-IDF vectorization, and similarity scoring.
- **Confidence-Aware Fallback**: Flags uncertain replies and invites human escalation.
- **Admin Dashboard**: Full CRUD over intents, real-time query logs, and escalation ticket resolution.
- **Persistence**: SQLite database logging all student interactions and escalation requests.
- **Voice Input (Extension)**: Web Speech API microphone dictation support.

---

## 6. Technology Stack
- **Backend**: Python 3.10+, Flask
- **Machine Learning & NLP**: scikit-learn (TfidfVectorizer, Cosine Similarity)
- **Database**: SQLite3
- **Frontend**: HTML5, CSS3, JavaScript (Fetch API, Web Speech API)
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Disk Space**: 150 MB free space.
- **Web Browser**: Chrome, Edge, Firefox, or Safari.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-02-College-Chatbot

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Database Setup
The SQLite database (`data/chatbot.db`) is automatically initialized upon launching the application. Default administrative credentials (`admin` / `admin123`) are pre-seeded.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
FLASK_APP=src/app.py
SECRET_KEY=logic_legend_chatbot_secret_2026
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
CONFIDENCE_THRESHOLD=0.30
```

---

## 11. Commands to Run the Application
```powershell
# Start the Flask web server
python src/app.py
```
*Access the student chat portal at `http://127.0.0.1:5000` and the staff administration dashboard at `http://127.0.0.1:5000/admin`.*

---

## 12. Demo Credentials
- **Student Portal**: No authentication required (open access).
- **Staff Administration Panel**:
  - **URL**: `http://127.0.0.1:5000/admin`
  - **Username**: `admin`
  - **Password**: `admin123`

---

## 13. Screenshots and Interface Description
1. **Student Chat Interface**: Interactive chat window displaying suggested topic chips, message history, timestamps, and confidence verification badges.
2. **Escalation Modal**: Form capturing Student Name, Email, Phone, and Query.
3. **Staff Admin Dashboard**: Metrics table displaying pending escalations, active knowledge base intents, and audit logs.

---

## 14. API Documentation
- `POST /api/chat`: Ingests `{ message, session_id }`, returns `{ response, tag, confidence, verified, suggest_escalation, timestamp }`.
- `POST /api/escalate`: Ingests student contact info and escalation details.
- `GET /api/suggestions`: Returns dynamic list of popular inquiries.

---

## 15. Test Instructions
```powershell
python tests/test_chatbot.py
```
*Executes 6 comprehensive unit tests covering intent matching, fallback handling, chat persistence, escalation lifecycle, admin auth, and dynamic intent CRUD.*

---

## 16. Known Limitations
- Model relies on pattern matching; extremely convoluted multi-topic queries may require rephrasing or escalation.
- Offline speech recognition depends on modern browser Web Speech API availability.

---

## 17. Future Enhancements
- Fine-tuned small language model (SLM) for generative question answering.
- WhatsApp Business and Telegram bot connectors.
- Multi-lingual language support (Hindi, Spanish, etc.).

---

## 18. Troubleshooting
- **Port 5000 in use**: Run with `python src/app.py --port 5050` or terminate existing process.
- **Microphone not working**: Ensure browser site permissions permit microphone access.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines. Free for academic presentation and college viva submission.
