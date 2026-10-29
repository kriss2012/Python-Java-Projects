# PRESENTATION SLIDES OUTLINE
## PY-02: AI Chatbot for College Queries
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: AI Chatbot for College Queries (PY-02)
- **Domain**: Conversational Natural Language Processing & EdTech
- **Technology Stack**: Python, Flask, scikit-learn (TF-IDF), SQLite, HTML5/CSS3/JS
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Thousands of student queries overwhelm academic helpdesks during admissions.
- Staff answer identical repetitive inquiries daily (fees, eligibility, timetables).
- Uncontrolled LLMs hallucinate inaccurate information regarding college deadlines.
- Students require 24/7 access to authoritative, verified institutional information.

---

### Slide 3: Objectives & Solution
- Provide an automated, high-precision conversational web chatbot.
- Vectorize student inputs using TF-IDF and classify intent with Cosine Similarity.
- Distinguish verified college answers from unverified topics via confidence scoring.
- Provide a direct "Talk to Admin" human escalation channel.
- Equip college staff with an administrative portal for FAQ updates and ticket resolution.

---

### Slide 4: System Architecture
- **Web Layer**: Flask serving responsive HTML5 chat and REST APIs.
- **NLP Engine**: Preprocessing, TF-IDF feature extraction, similarity scoring against JSON knowledge base.
- **Persistence Layer**: SQLite database logging conversations, timestamps, and escalation tickets.
- **Staff Panel**: Authenticated interface for intent CRUD and escalation audits.

---

### Slide 5: NLP Methodology & Confidence Thresholding
- **TF-IDF Transformation**: Weights distinctive terms across pattern documents.
- **Cosine Similarity**: Computes angular similarity between user input and trained intent patterns.
- **Threshold Rule**:
  - Score $\ge 0.30$: Displays verified institutional answer.
  - Score $< 0.30$: Returns confidence-aware fallback and renders escalation button.

---

### Slide 6: Database Schema & Entity Relationships
- `admin_users`: Secure SHA-256 hashed administrative credentials.
- `chat_logs`: Session ID, user input, detected intent, confidence score, timestamp.
- `escalations`: Full student contact records, query text, and PENDING/RESOLVED status.

---

### Slide 7: Human-in-the-Loop Escalation
- Prevents dead ends in conversational flows.
- One-click modal form captures student name, email, phone, and detailed problem.
- Admin dashboard notifies staff of pending requests and enables one-click resolution.

---

### Slide 8: Demonstration Highlights
- Answering standard queries (Admissions, Fees, Placements).
- Suggested quick-reply chips for common questions.
- Confidence badge display (`Official Verified` vs `Unverified/Fallback`).
- Dynamic addition of a new intent via the admin panel without server reboot.

---

### Slide 9: Testing & Verification
- Unit test coverage with `unittest`:
  - 100% intent classification accuracy on representative college queries.
  - Verification of low-confidence fallback behavior.
  - Escalation lifecycle state transitions.
  - Dynamic intent addition and deletion.

---

### Slide 10: Limitations & Future Enhancements
- **Limitations**: Inability to synthesize new knowledge beyond configured intents.
- **Future Scope**:
  - Integration with WhatsApp Business API and Telegram.
  - Multi-lingual voice dialog via TTS/STT pipelines.
  - Integration with College ERP for personalized grade/attendance lookups.

---

### Slide 11: Conclusion & Q&A
- Developed a robust, reliable, and deployable college information chatbot.
- Open for questions and evaluator inspection.

