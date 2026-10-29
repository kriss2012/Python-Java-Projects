# VIVA VOCE PREPARATION & QUESTIONS
## PY-02: AI Chatbot for College Queries
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the core problem addressed by this college chatbot?
**Answer**: Academic institutions experience massive query volumes during admissions and exam cycles. Front desks and telephone operators face extreme workload, leading to delayed responses. The chatbot provides 24/7 automated responses for recurring questions while escalating complex queries to staff.

### Q2: What NLP approach is used for intent classification, and why?
**Answer**: We use TF-IDF (Term Frequency-Inverse Document Frequency) vectorization combined with Cosine Similarity against pattern vectors. This approach is lightweight, computationally efficient, runs locally without expensive GPU hardware or third-party cloud APIs, and avoids hallucinations.

### Q3: How does the chatbot decide when to trigger a fallback response?
**Answer**: After vectorizing the user's input, the system computes cosine similarity against all trained intent patterns. If the maximum similarity score falls below a predetermined threshold (default 0.30), the engine routes the inquiry to a `fallback` intent and flags `suggest_escalation: True`.

### Q4: Why is confidence-aware thresholding important in an academic application?
**Answer**: Hallucinating or returning incorrect answers for academic deadlines, tuition fees, or admission eligibility can have serious legal and financial consequences for students. Explicitly flagging uncertain queries prevents misinformation.

### Q5: How does the "Talk to Admin" escalation workflow function?
**Answer**: When a student's question cannot be answered with high confidence, or when they click "Talk to Admin", a modal captures their name, email, phone number, and query. This is stored in the `escalations` table with status `PENDING`, allowing college staff to review and resolve it via the admin dashboard.

### Q6: How does the admin dashboard allow dynamic knowledge base updates without restarting the server?
**Answer**: When staff add, update, or delete an intent via the admin portal, the system updates `knowledge_base.json` on disk and immediately calls `self.load_knowledge_base()`, which refits the `TfidfVectorizer` and re-indexes pattern vectors in memory in milliseconds.

### Q7: What database schema is used for interaction logging?
**Answer**: An SQLite database (`chatbot.db`) contains:
- `chat_logs`: Logs session ID, user message, bot response, detected intent tag, confidence score, and timestamp.
- `escalations`: Stores escalated student tickets and resolution timestamps.
- `admin_users`: Stores admin credentials with SHA-256 hashed passwords.

### Q8: How is administrative authentication secured?
**Answer**: Admin passwords are stored as cryptographic SHA-256 hashes rather than plaintext. When an admin logs in, the input password is encrypted and compared with the stored hash using parameterized SQL queries to prevent SQL injection.

### Q9: What role do the "suggested prompt chips" play in user experience?
**Answer**: They guide first-time users by showing common topics (Admissions, Fees, Placements), reducing typing effort and helping users formulate queries that match institutional terminology.

### Q10: How does the Web Speech API voice input work?
**Answer**: The browser's native `webkitSpeechRecognition` or `SpeechRecognition` API listens to microphone input, transcribes speech to text in the browser, and populates the chat input field, triggering automated message dispatch.

### Q11: What testing framework was used, and what test cases were verified?
**Answer**: Python's `unittest` framework was used to test:
- Accurate intent classification for admissions, fees, and placements.
- Fallback triggering on random out-of-domain sentences.
- Persistence of chat logs in SQLite.
- Creation and resolution of escalation tickets.
- Admin password verification.
- Dynamic addition and deletion of intents in the knowledge base.

### Q12: Why did you choose Flask over Django or FastAPI?
**Answer**: Flask is a lightweight micro-framework ideal for minor academic projects. It offers minimal overhead, straightforward template rendering with Jinja2, easy REST API structuring, and zero unnecessary boilerplate.

### Q13: How does the system prevent cross-site request forgery or injection attacks?
**Answer**: Flask's Jinja2 templating automatically escapes HTML output to prevent XSS. All SQLite operations use parameterized queries (`?` placeholders) preventing SQL injection, and admin endpoints require an active authenticated session.

### Q14: What are the primary limitations of rule/pattern-based intent classification?
**Answer**:
1. Grammatically complex or highly misspelled sentences can cause lower similarity scores.
2. It cannot generate novel, conversational synthesis beyond pre-configured response templates.

### Q15: How can this chatbot be extended in future development?
**Answer**:
1. Incorporating a locally hosted Small Language Model (SLM) such as LLaMA-3-8B or Mistral for conversational depth.
2. Integrating with messaging channels like Telegram or WhatsApp via webhooks.
3. Adding multi-lingual query understanding using mBERT or Indic-NLP models.

