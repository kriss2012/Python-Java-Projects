"""
Logic Legend - PY-06 Personal Voice Assistant
Command Parsing & Security Allowlist Engine
"""

import os
import re
import webbrowser
import subprocess
from datetime import datetime
import wikipedia

# Strict Security Allowlist for Application Launching
PERMITTED_APPS = {
    "notepad": ["notepad.exe"],
    "calculator": ["calc.exe"],
    "calc": ["calc.exe"],
    "paint": ["mspaint.exe"],
    "mspaint": ["mspaint.exe"],
    "file explorer": ["explorer.exe"],
    "explorer": ["explorer.exe"],
    "cmd": ["cmd.exe"],
    "terminal": ["cmd.exe"]
}

# Strict Security Allowlist for Permitted Web Destinations
PERMITTED_DOMAINS = {
    "google": "https://www.google.com",
    "wikipedia": "https://www.wikipedia.org",
    "github": "https://www.github.com",
    "youtube": "https://www.youtube.com",
    "python": "https://www.python.org",
    "stackoverflow": "https://www.stackoverflow.com"
}

# Dangerous Keywords Strictly Blocked
DANGEROUS_PATTERNS = [
    r'\b(rm|del|rmdir|erase|format|mkfs|dd)\b',
    r'\b(shutdown|reboot|halt|init 0)\b',
    r'\b(powershell|bash|sh|cmd\.exe /c)\b',
    r'(\||;|&&|`|\$\()',
    r'\b(exec|eval|os\.system)\b'
]

class CommandParser:
    def __init__(self, db, voice_engine):
        self.db = db
        self.voice = voice_engine
        self.smart_home_state = {
            "living_room_light": "OFF",
            "bedroom_light": "OFF",
            "air_conditioner": "OFF (24°C)",
            "study_fan": "OFF"
        }

    def is_dangerous(self, text):
        for pat in DANGEROUS_PATTERNS:
            if re.search(pat, text, re.IGNORECASE):
                return True
        return False

    def execute_command(self, raw_command, confirm_destructive=False):
        text = raw_command.strip()
        cmd_lower = text.lower()

        if not text:
            return {"response": "I didn't receive any command. Please speak or type.", "status": "EMPTY"}

        # 1. Security Check against shell injection & arbitrary code
        if self.is_dangerous(cmd_lower):
            msg = "Security Alert: Command blocked. Execution of arbitrary shell scripts or destructive system commands is strictly prohibited by security allowlist."
            self.db.log_command(text, msg, status="BLOCKED")
            self.voice.speak(msg)
            return {"response": msg, "status": "BLOCKED"}

        # 2. Time & Date
        if re.search(r'\b(time|what time)\b', cmd_lower):
            now_time = datetime.now().strftime("%I:%M %p")
            resp = f"The current time is {now_time}."
            self._respond(text, resp)
            return {"response": resp, "status": "SUCCESS"}

        if re.search(r'\b(date|today\'s date|what date)\b', cmd_lower):
            now_date = datetime.now().strftime("%A, %B %d, %Y")
            resp = f"Today is {now_date}."
            self._respond(text, resp)
            return {"response": resp, "status": "SUCCESS"}

        # 3. Wikipedia Search
        if cmd_lower.startswith("wikipedia") or "who is" in cmd_lower or "what is" in cmd_lower:
            topic = re.sub(r'^(wikipedia|who is|what is|search wikipedia for)\s+', '', cmd_lower).strip()
            if topic:
                try:
                    summary = wikipedia.summary(topic, sentences=2)
                    resp = f"According to Wikipedia: {summary}"
                except wikipedia.exceptions.DisambiguationError as e:
                    resp = f"Topic is ambiguous. Did you mean: {', '.join(e.options[:3])}?"
                except Exception:
                    resp = f"Could not locate Wikipedia summary for '{topic}'."
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS"}

        # 4. Open Application (Strict Allowlist Check)
        if cmd_lower.startswith("open app") or cmd_lower.startswith("launch") or cmd_lower.startswith("open"):
            target = re.sub(r'^(open app|launch|open)\s+', '', cmd_lower).strip()

            # Check websites first
            if target in PERMITTED_DOMAINS:
                url = PERMITTED_DOMAINS[target]
                webbrowser.open(url)
                resp = f"Opening {target.title()} in your default browser."
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS"}

            # Check apps allowlist
            if target in PERMITTED_APPS:
                app_cmd = PERMITTED_APPS[target]
                try:
                    subprocess.Popen(app_cmd, shell=False)
                    resp = f"Launching {target.title()}."
                except Exception as e:
                    resp = f"Failed to launch {target}: {str(e)}"
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS"}

            # If not in allowlist
            resp = f"Security Policy Notice: '{target}' is not in the authorized application allowlist."
            self._respond(text, resp, status="DENIED")
            return {"response": resp, "status": "DENIED"}

        # 5. Web Search
        if cmd_lower.startswith("search") or cmd_lower.startswith("google"):
            query = re.sub(r'^(search for|search|google)\s+', '', cmd_lower).strip()
            if query:
                webbrowser.open(f"https://www.google.com/search?q={query}")
                resp = f"Searching the web for '{query}'."
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS"}

        # 6. To-Do & Reminder Management
        if cmd_lower.startswith("add task") or cmd_lower.startswith("add reminder"):
            title = re.sub(r'^(add task|add reminder|create task)\s+', '', text, flags=re.I).strip()
            if title:
                ok, tid = self.db.add_todo(title)
                resp = f"Task #{tid} '{title}' has been added to your reminders."
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS", "action": "TODO_UPDATED"}

        if re.search(r'\b(list tasks|show tasks|my tasks|reminders)\b', cmd_lower):
            todos = self.db.get_todos(status="PENDING")
            if not todos:
                resp = "You have no active pending tasks or reminders."
            else:
                items = [f"#{t['id']} {t['title']}" for t in todos[:5]]
                resp = f"Active reminders ({len(todos)} total): " + "; ".join(items)
            self._respond(text, resp)
            return {"response": resp, "status": "SUCCESS"}

        if cmd_lower.startswith("complete task") or cmd_lower.startswith("finish task"):
            match = re.search(r'\d+', cmd_lower)
            if match:
                tid = int(match.group())
                if self.db.complete_todo(tid):
                    resp = f"Task #{tid} marked as completed."
                else:
                    resp = f"Task #{tid} not found."
            else:
                resp = "Please specify task ID (e.g., 'complete task 1')."
            self._respond(text, resp)
            return {"response": resp, "status": "SUCCESS", "action": "TODO_UPDATED"}

        if "clear all tasks" in cmd_lower:
            if not confirm_destructive:
                resp = "CONFIRMATION_REQUIRED: Are you sure you want to delete all tasks and reminders? Say or click confirm."
                return {"response": resp, "status": "NEEDS_CONFIRMATION"}
            else:
                self.db.clear_all_todos()
                resp = "All tasks have been cleared from your records."
                self._respond(text, resp)
                return {"response": resp, "status": "SUCCESS", "action": "TODO_UPDATED"}

        # 7. Smart Home Control Simulation (Extension)
        if "smart home" in cmd_lower or "light" in cmd_lower or "fan" in cmd_lower or "ac" in cmd_lower:
            if "light on" in cmd_lower:
                self.smart_home_state["living_room_light"] = "ON"
                resp = "Smart Home: Living room lights turned ON."
            elif "light off" in cmd_lower:
                self.smart_home_state["living_room_light"] = "OFF"
                resp = "Smart Home: Living room lights turned OFF."
            elif "fan on" in cmd_lower:
                self.smart_home_state["study_fan"] = "ON"
                resp = "Smart Home: Study fan turned ON."
            elif "fan off" in cmd_lower:
                self.smart_home_state["study_fan"] = "OFF"
                resp = "Smart Home: Study fan turned OFF."
            elif "ac on" in cmd_lower or "ac set" in cmd_lower:
                self.smart_home_state["air_conditioner"] = "ON (22°C)"
                resp = "Smart Home: Air conditioning set to 22°C."
            else:
                resp = f"Smart Home Devices: Lights: {self.smart_home_state['living_room_light']}, Fan: {self.smart_home_state['study_fan']}, AC: {self.smart_home_state['air_conditioner']}."
            self._respond(text, resp)
            return {"response": resp, "status": "SUCCESS", "smart_home": self.smart_home_state}

        # Unknown Command Fallback
        resp = f"I didn't recognize that command: '{text}'. Try 'time', 'date', 'wikipedia <topic>', 'add task <title>', or 'open notepad'."
        self._respond(text, resp, status="UNKNOWN")
        return {"response": resp, "status": "UNKNOWN"}

    def _respond(self, cmd_text, resp_text, status="SUCCESS"):
        self.db.log_command(cmd_text, resp_text, status=status)
        self.voice.speak(resp_text)
