# EVALUATOR DEMONSTRATION GUIDE
## PY-02: AI Chatbot for College Queries
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open a terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-02-College-Chatbot
python src/app.py
```
*The Flask web server will start at `http://127.0.0.1:5000`.*
Open your web browser and navigate to `http://127.0.0.1:5000`.

---

## 2. Recommended Demonstration Flow

### Step 1: Demonstrate Conversational Queries
- Click on the suggested chip: **"How to apply for admission?"**
- Point out:
  - Immediate response with admission criteria (10+2, 60% aggregate).
  - Green verification badge: **"Official Verified (Confidence: 85%)"**.
  - Message timestamp and clean avatar formatting.
- Type a fee-related inquiry: **"What is the fee structure for B.Tech?"**
  - Verify the bot answers with tuition fees (₹ 1,25,000) and late fee policy.
- Type a placement inquiry: **"What is the highest package?"**
  - Verify the bot provides placement data (₹ 44 LPA, Google, Microsoft).

### Step 2: Demonstrate Confidence-Aware Fallback
- Type an unrelated or out-of-domain query:
  `"How do rockets launch into Mars orbit?"`
- Point out:
  - The bot does **not** hallucinate or invent false college answers.
  - Returns fallback message: *"I do not have verified official records matching that specific question."*
  - An inline button **"Connect with Administrator"** appears automatically.

### Step 3: Demonstrate "Talk to Admin" Escalation
- Click **"Talk to Admin"** (either on the message or top right header).
- A clean modal pops up. Enter:
  - Full Name: `Aditya Sharma`
  - Email: `aditya@example.com`
  - Phone: `+91 98765 43210`
  - Query: `Need info on scholarship for state sports medalists.`
- Click **Submit Escalation Ticket**.
- An alert confirms ticket submission with an assigned ticket ID.

### Step 4: Demonstrate Staff Administration Portal
- Navigate to `http://127.0.0.1:5000/admin`.
- Log in with staff credentials:
  - Username: `admin`
  - Password: `admin123`
- Inspect the **Student Escalation Tickets** table:
  - Point out Aditya Sharma's ticket with status `PENDING`.
  - Click **Mark Resolved** to demonstrate ticket lifecycle management.
- Inspect the **Recent Student Conversation Audit Logs**:
  - Show live audit entries with user message, detected intent, and confidence score.
- Demonstrate **Dynamic Knowledge Base Editing**:
  - Add a new FAQ tag: `transport_routes`
  - Pattern: `bus routes and stops`
  - Response: `Campus buses cover 12 routes across the metropolitan area.`
  - Submit the form. Notice the intent is saved immediately.
- Return to the chat window (`http://127.0.0.1:5000`), ask: `"bus routes and stops"`, and show that the newly added answer is recognized without needing a server restart!

---

## 3. Verification of Unit Tests
Run the automated test suite in a separate terminal:
```powershell
python tests/test_chatbot.py
```
*All 6 unit tests verify intent classification, fallback behavior, SQLite logging, escalation ticket creation and resolution, and admin authentication.*

