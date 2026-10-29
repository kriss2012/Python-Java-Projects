# PY-06: Personal Voice Assistant
**Logic Legend Minor Project Series — Ideas Today, Impact Tomorrow**

---

## 1. Project Title and Identifier
- **Project Identifier**: `PY-06`
- **Project Title**: Personal Voice Assistant
- **Domain**: Desktop Automation, Human-Computer Interaction & Voice Processing
- **Platform**: Desktop GUI (Python Tkinter) & Embedded SQLite

---

## 2. Project Overview
The Personal Voice Assistant is an intelligent desktop automation agent capable of understanding spoken and typed commands to report the time/date, query Wikipedia, execute web searches, manage persistent to-do tasks and reminders, control simulated smart-home appliances, and open authorized applications through a strict security allowlist.

---

## 3. Problem Statement
Commercial voice assistants (e.g. Siri, Alexa) require persistent internet connections and external cloud servers, raising serious privacy concerns regarding continuous acoustic eavesdropping. Conversely, simplistic open-source voice scripts frequently suffer from arbitrary command injection vulnerabilities where untrusted speech can execute destructive shell commands.

---

## 4. Objectives
- Implement dual speech-to-text (STT) and text-to-speech (TTS) interfaces.
- Support typed text commands when hardware microphones are unavailable.
- Enforce a strict security allowlist preventing arbitrary shell code execution.
- Maintain persistent task and reminder management using SQLite.
- Require explicit user confirmation before executing destructive operations.
- Provide a mute toggle for privacy and silent operation.

---

## 5. Key Features
- **Speech & Text Dual Input**: Seamlessly alternate between microphone audio and typed input.
- **Security Allowlist**: Only pre-authorized applications (Notepad, Calculator, Paint, Browser) can be launched; shell injection (`rm`, `del`, `format`, `eval`) is blocked.
- **Wikipedia Knowledge Retrieval**: Fetches concise 2-sentence summaries for factual queries.
- **To-Do & Reminder Engine**: Add, list, complete, and delete tasks with SQLite persistence.
- **Smart Home Simulation (Extension)**: Virtual control of lights, fans, and air conditioning.
- **Command History Audit**: Real-time logging of all incoming queries and responses.

---

## 6. Technology Stack
- **Language**: Python 3.10+
- **Speech Recognition**: `SpeechRecognition`
- **Text to Speech**: `pyttsx3`
- **Knowledge API**: `wikipedia`
- **Automation**: `pyautogui`, `webbrowser`, `subprocess`
- **Database**: SQLite3
- **GUI**: Tkinter & TTK
- **Testing**: Python `unittest` framework

---

## 7. System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Microphone**: Standard input microphone (optional; typed fallback provided).
- **Audio Output**: Speakers or headphones for synthesized speech.
- **RAM**: Minimum 2 GB.
- **Disk Space**: 100 MB free space.

---

## 8. Prerequisites and Installation
```powershell
cd Logic-Legend-Minor-Projects/Python-Projects/PY-06-Voice-Assistant

# Install dependencies
python -m pip install -r requirements.txt
```

---

## 9. Database Setup
The SQLite database (`data/assistant.db`) initializes automatically with pre-seeded task reminders and command audit logs.

---

## 10. Environment Variable Configuration
Refer to `.env.example`:
```env
VOICE_RATE=175
VOICE_VOLUME=0.9
DEFAULT_SEARCH_ENGINE=duckduckgo
DB_PATH=data/assistant.db
ENABLE_VOICE_OUTPUT=true
```

---

## 11. Commands to Run the Application
```powershell
# Launch Desktop GUI Application
python src/main.py

# Run in Interactive CLI Terminal Mode
python src/main.py --cli

# Execute a Single Command via CLI
python src/main.py --cmd "what time is it" --no-voice
```

---

## 12. Demo Credentials
No login required. Local desktop assistant.

---

## 13. Screenshots and Interface Description
1. **Left Dialogue Viewport**: Real-time conversation stream displaying user queries, assistant replies, and timestamps.
2. **Controls Bar**: Mute TTS checkbox, mic status indicator, and security allowlist badge.
3. **To-Do Manager**: Searchable task list with "Mark Done", "Delete", and "Clear All" buttons.
4. **Smart Home Panel**: Status labels and toggle buttons for virtual lights, fans, and AC.

---

## 14. API Documentation
Key methods in `CommandParser`:
- `execute_command(raw_command, confirm_destructive=False)`
- `is_dangerous(text)`
Key methods in `VoiceEngine`:
- `speak(text)`
- `listen()`

---

## 15. Test Instructions
```powershell
python tests/test_assistant.py
```
*All 7 unit tests verify security blocking of dangerous commands, allowlist enforcement, time/date queries, to-do lifecycle, destructive action confirmation, smart home toggling, and SQLite audit logging.*

---

## 16. Known Limitations
- Background environmental noise may degrade speech recognition accuracy in uncalibrated microphone environments.

---

## 17. Future Enhancements
- Offline acoustic wake-word engine using Porcupine or Snowboy.
- Microcontroller hardware integration via Arduino / ESP32 serial bridge for physical relay control.

---

## 18. Troubleshooting
- **No speech output heard**: Verify speakers are connected and ensure the "Voice Output (TTS)" checkbox is enabled.
- **Microphone not detected**: Use the typed input box at the bottom of the dialogue window to execute any command.

---

## 19. License & Third-Party Notes
Built under Logic Legend educational guidelines for academic viva evaluations.

