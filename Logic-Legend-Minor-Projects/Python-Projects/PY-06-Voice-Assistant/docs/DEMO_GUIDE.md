# EVALUATOR DEMONSTRATION GUIDE
## PY-06: Personal Voice Assistant
**Step-by-step demonstration manual for lab examiners and viva evaluators.**

---

## 1. Quick Startup
Open your system terminal in the project directory:
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-06-Voice-Assistant
python src/main.py
```
*The desktop GUI opens with pre-seeded task reminders and assistant ready status.*

---

## 2. Recommended Demonstration Flow

### Step 1: Query Time and Date
- In the text entry box at the bottom, type: `what is the current time` and press **Enter** (or click **🎤 Speak** if microphone is connected).
- Notice:
  - Dialogue feed displays: `[Time] You: what is the current time`.
  - Assistant responds with formatted current time (and speaks aloud via TTS if audio is enabled).
- Type: `today's date` -> Assistant responds with current day, month, date, and year.

### Step 2: Query Wikipedia Knowledge Base
- Type: `wikipedia artificial intelligence`.
- Point out:
  - The assistant retrieves a clean 2-sentence summary from Wikipedia and speaks it aloud.
  - The summary is displayed in the conversation history log.

### Step 3: Demonstrate Security Allowlist & Injection Blocking
- Attempt an arbitrary shell command:
  Type: `rm -rf /` or `del *.* /s /q`
- Point out:
  - **IMMEDIATE SECURITY REJECTION**:
    `Security Alert: Command blocked. Execution of arbitrary shell scripts or destructive system commands is strictly prohibited by security allowlist.`
  - The command is flagged with status `BLOCKED` in the SQLite audit log.
- Attempt to open an unauthorized application:
  Type: `open malware_tool.exe`
- Point out:
  - Rejection notice: `Security Policy Notice: 'malware_tool.exe' is not in the authorized application allowlist.`
- Launch an authorized application:
  Type: `open notepad`
- Point out:
  - Notepad immediately opens on the desktop safely!

### Step 4: Manage To-Do Reminders
- Look at the right panel under **To-Do & Reminders Manager**.
- In the assistant chat, type:
  `add task Submit Final Capstone Report`
- Notice:
  - Assistant confirms: `Task #4 'Submit Final Capstone Report' has been added to your reminders.`
  - The task immediately appears in the right table.
- Select a task in the table and click **✔ Mark Done**:
  - The status updates to `COMPLETED`.
- In the chat box, type:
  `clear all tasks`
- Notice:
  - A modal confirmation dialog pops up: *"Are you sure you want to delete all tasks and reminders?"*
  - This proves safety against accidental speech destruction.

### Step 5: Demonstrate Smart Home IoT Control (Extension)
- In the chat box, type:
  `smart home light on`
- Notice the right panel updates: **Living Room Light: ON**.
- Type: `smart home fan on` -> **Study Room Fan: ON**.
- Click the toggle buttons in the Smart Home panel to demonstrate real-time bidirectional synchronization.

### Step 6: Test Voice Mute Toggle
- Uncheck the **Voice Output (TTS)** checkbox.
- Issue a command: `what time is it`.
- Notice the assistant executes and prints the response silently, proving silent operational mode.

---

## 3. Automated Unit Testing
Run the unit test suite in terminal:
```powershell
python tests/test_assistant.py
```
*All 7 unit tests pass, verifying security blocking, allowlist enforcement, to-do lifecycle, destructive confirmations, and SQLite audit trails.*

