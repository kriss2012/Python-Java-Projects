# VIVA VOCE PREPARATION & QUESTIONS
## PY-06: Personal Voice Assistant
**15+ University & Technical Viva Questions with Model Answers**

---

### Q1: What is the primary purpose and scope of this Voice Assistant?
**Answer**: The assistant provides an accessible desktop agent that listens to voice and typed commands to automate daily tasks: querying the time/date, searching Wikipedia, opening authorized desktop tools, managing persistent to-dos, and controlling virtual smart-home devices while enforcing strict security safeguards.

### Q2: What libraries are used for Speech-to-Text and Text-to-Speech?
**Answer**:
- **Speech-to-Text (STT)**: Python's `SpeechRecognition` library connecting to Google's acoustic API (with fallback handling for offline and typed modes).
- **Text-to-Speech (TTS)**: `pyttsx3`, a cross-platform Python text-to-speech engine that operates 100% offline using native OS speech synthesizers (SAPI5 on Windows, NSSpeechSynthesizer on macOS, espeak on Linux).

### Q3: How did you implement the Security Allowlist for application launching?
**Answer**: We maintain a static dictionary of permitted application identifiers (`notepad`, `calculator`, `paint`, `browser`, `terminal`) mapped to explicit safe executable binaries. When a launch request arrives, the parser strictly checks if the target is in the allowlist. Arbitrary executables and shell scripts are rejected, and `subprocess.Popen` is invoked with `shell=False` to prevent command chaining.

### Q4: Why is running subprocesses with `shell=True` dangerous in a voice assistant?
**Answer**: If an assistant passes raw user speech directly to a shell with `shell=True`, an attacker (or malicious audio broadcast) could speak: `"open notepad && rm -rf /"` or `"echo harmless; shutdown"`. The shell interprets `;` or `&&` as command separators, executing catastrophic commands on the host machine.

### Q5: How does the assistant handle environments where no hardware microphone is available?
**Answer**: The GUI and CLI provide a dual-modality architecture. The user can type any command in the text entry field and hit Enter. The command parser executes the exact same business logic, preventing hardware limitations from interrupting evaluator demonstrations.

### Q6: How are to-do tasks and reminders stored persistently?
**Answer**: Tasks are persisted in an embedded SQLite database (`assistant.db`) inside the `todos` table with fields `id`, `title`, `due_date`, and `status` (`PENDING` or `COMPLETED`). They survive application restarts.

### Q7: How does the application safeguard against accidental destruction of all tasks?
**Answer**: When a command like `"clear all tasks"` is received, the parser does not delete records immediately. Instead, it returns a status of `NEEDS_CONFIRMATION` and prompts the user with an explicit confirmation dialog before executing the deletion.

### Q8: How does the Wikipedia summarization pipeline work?
**Answer**: The assistant extracts the search query, calls `wikipedia.summary(topic, sentences=2)`, catches potential disambiguation errors (`wikipedia.exceptions.DisambiguationError`), and returns a concise 2-sentence summary that can be read aloud by the TTS engine.

### Q9: How is the assistant dialogue prevented from freezing during TTS speech output?
**Answer**: In `VoiceEngine.speak()`, speech synthesis is offloaded to a background daemon `threading.Thread` with a threading mutex lock. This ensures the Tkinter GUI event loop remains responsive and does not stutter while audio is playing.

### Q10: What database schema is used for audit trails?
**Answer**: The `command_history` table records every incoming instruction (`command_text`), assistant response (`response_text`), execution outcome (`SUCCESS`, `BLOCKED`, `DENIED`, `UNKNOWN`), and ISO timestamp.

### Q11: What smart home simulation features are supported?
**Answer**: A simulated IoT device state hub tracks the status of living room lights, study room fans, and air conditioning temperatures. Users can issue natural language commands (e.g. "turn on lights", "fan off") to manipulate device states.

### Q12: How can the user mute voice output during sensitive or quiet environments?
**Answer**: A "Voice Output (TTS)" checkbox in the GUI toggles the `voice_enabled` flag. When unchecked, the assistant continues to process commands and display text responses without generating audible sound.

### Q13: What automated tests were implemented?
**Answer**: Using Python's `unittest` framework:
- Blocking of dangerous commands (`rm -rf`, `format`, `powershell`).
- Rejection of non-allowlisted applications.
- Accuracy of time and date queries.
- Complete CRUD lifecycle of to-do items.
- Confirmation requirement for clearing all tasks.
- Smart home state transitions.
- Persistent audit logging in SQLite.

### Q14: What are the primary limitations of rule-based command parsing?
**Answer**:
1. Grammatical variations outside configured regular expressions can trigger unknown command fallbacks.
2. It lacks semantic understanding of multi-step or deeply ambiguous compound tasks.

### Q15: How would you extend this project for a smart campus or production environment?
**Answer**:
1. Incorporating a local Large Language Model (e.g., Llama 3 via Ollama) for flexible intent parsing.
2. Integrating hardware IoT relays via MQTT / Zigbee protocols.
3. Adding voice biometric speaker recognition to verify user identity before executing administrative commands.

