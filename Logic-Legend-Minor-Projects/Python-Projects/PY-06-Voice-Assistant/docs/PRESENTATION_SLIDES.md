# PRESENTATION SLIDES OUTLINE
## PY-06: Personal Voice Assistant
**Logic Legend — Minor Project Viva Presentation**

---

### Slide 1: Title & Overview
- **Project**: Personal Voice Assistant (PY-06)
- **Domain**: Human-Computer Interaction, Voice Processing & Desktop Automation
- **Technologies**: Python, SpeechRecognition, pyttsx3, SQLite, Wikipedia, Tkinter
- **Theme**: *Logic Legend — Ideas Today, Impact Tomorrow*

---

### Slide 2: Problem Statement & Motivation
- Cloud-based voice assistants (Alexa, Siri) continuously upload ambient audio to remote servers.
- Open-source hobbyist assistants frequently execute arbitrary shell commands without security boundaries.
- Assistants crash in classrooms or exam halls when microphones are absent or loud background noise occurs.

---

### Slide 3: Objectives & Solution Highlights
- A local-first desktop voice assistant with zero cloud audio dependency.
- **Standout Feature**: Strict security allowlist preventing arbitrary code execution.
- Dual input modality: Seamlessly transition between voice dictation and typed keyboard input.
- Persistent SQLite database for to-do items, reminders, and command history audit logs.
- Confirmation gates for destructive commands (e.g., clearing task databases).

---

### Slide 4: System Architecture & Workflow
- **Input Layer**: SpeechRecognition (Microphone) / Tkinter Entry (Keyboard).
- **Security & Parsing Layer**: Regex injection filter -> Security allowlist check -> Intent router.
- **Execution Modules**: Time/Date, Wikipedia Summarizer, Web Search, To-Do Database, Smart Home Hub.
- **Output Layer**: pyttsx3 Text-to-Speech audio synthesizer & Tkinter conversational log.

---

### Slide 5: Security Architecture & Injection Prevention
- Explicitly rejects dangerous system binaries:
  - `rm`, `del`, `format`, `shutdown`, `powershell`, `eval()`.
- Whitelist-only application execution:
  - Permitted: `notepad`, `calculator`, `paint`, `browser`.
  - Non-whitelisted applications are systematically rejected with an audit log.

---

### Slide 6: Database Persistence & Reminders
- SQLite schema:
  - `todos`: Task title, due date, status (`PENDING` / `COMPLETED`), creation timestamp.
  - `command_history`: Timestamped record of every spoken/typed instruction and assistant reply.

---

### Slide 7: Smart Home Simulation (Extension)
- Built-in virtual IoT device state manager:
  - Living room light (`ON` / `OFF`).
  - Study room fan (`ON` / `OFF`).
  - Air conditioner temperature adjustments.

---

### Slide 8: Demonstration & Visual Highlights
- Speaking/typing "what time is it" -> Synthesized speech and chat response.
- Querying "wikipedia artificial intelligence" -> 2-sentence summary.
- Adding and completing to-dos in real time.
- Attempting a dangerous command (`rm -rf /`) -> Immediate visual security alert.

---

### Slide 9: Testing & Quality Assurance
- Automated test suite validating:
  - Allowlist rejection of malicious shell arguments.
  - Rejection of unauthorized executables.
  - Time/date formatting.
  - Confirmation prompt on destructive actions.
  - SQLite persistence and state transitions.

---

### Slide 10: Limitations & Future Enhancements
- **Limitations**: Speech recognition accuracy decreases in noisy cafeteria or classroom environments.
- **Future Scope**:
  - Offline acoustic wake-word engine (e.g. Porcupine).
  - Microcontroller serial interface (Arduino / ESP32) for physical appliance switching.

---

### Slide 11: Conclusion & Q&A
- Delivered a secure, versatile, and privacy-respecting desktop voice assistant.
- Open for questions and live demonstration.

