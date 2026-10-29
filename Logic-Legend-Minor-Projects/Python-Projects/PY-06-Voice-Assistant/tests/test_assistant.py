"""
Logic Legend - PY-06 Personal Voice Assistant
Automated Unit and Security Allowlist Test Suite
"""

import os
import sys
import tempfile
import unittest
import gc

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src"))

from database import AssistantDatabase
from voice_engine import VoiceEngine
from command_parser import CommandParser

class TestVoiceAssistant(unittest.TestCase):
    def setUp(self):
        self.temp_db_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        os.close(self.temp_db_fd)
        self.db = AssistantDatabase(db_path=self.temp_db_path)
        # Initialize voice engine with voice output disabled during tests
        self.voice = VoiceEngine(voice_enabled=False)
        self.parser = CommandParser(db=self.db, voice_engine=self.voice)

    def tearDown(self):
        del self.parser
        del self.voice
        del self.db
        gc.collect()
        try:
            if os.path.exists(self.temp_db_path):
                os.remove(self.temp_db_path)
        except OSError:
            pass

    def test_01_security_blocks_dangerous_commands(self):
        """Test that destructive shell commands and injections are strictly blocked."""
        dangerous_inputs = [
            "rm -rf /",
            "format C: /y",
            "del *.* /s /q",
            "powershell -enc JABzAD0ATgBlAHcALQBPAGIAag...",
            "shutdown -s -t 0",
            "python -c 'import os; os.system(\"rmdir\")'"
        ]
        for cmd in dangerous_inputs:
            res = self.parser.execute_command(cmd)
            self.assertEqual(res["status"], "BLOCKED")
            self.assertIn("Security Alert", res["response"])

    def test_02_security_app_allowlist_enforcement(self):
        """Test that unauthorized executables are denied while allowlisted apps succeed."""
        # Unauthorized app
        res_denied = self.parser.execute_command("open keylogger.exe")
        self.assertEqual(res_denied["status"], "DENIED")
        self.assertIn("Security Policy Notice", res_denied["response"])

        # Allowlisted web target
        res_web = self.parser.execute_command("open github")
        self.assertEqual(res_web["status"], "SUCCESS")
        self.assertIn("Github", res_web["response"])

    def test_03_time_and_date_queries(self):
        """Test returning accurate time and date strings."""
        res_time = self.parser.execute_command("what is the current time")
        self.assertEqual(res_time["status"], "SUCCESS")
        self.assertIn("current time is", res_time["response"].lower())

        res_date = self.parser.execute_command("today's date please")
        self.assertEqual(res_date["status"], "SUCCESS")
        self.assertIn("today is", res_date["response"].lower())

    def test_04_todo_reminder_crud(self):
        """Test adding, listing, and completing to-dos via command interface."""
        # Add task
        res_add = self.parser.execute_command("add task Study Operating Systems Viva")
        self.assertEqual(res_add["status"], "SUCCESS")
        todos = self.db.get_todos()
        self.assertTrue(any("Study Operating Systems" in t["title"] for t in todos))

        # List tasks
        res_list = self.parser.execute_command("show my reminders")
        self.assertEqual(res_list["status"], "SUCCESS")
        self.assertIn("Study Operating Systems", res_list["response"])

        # Complete task
        tid = todos[0]["id"]
        res_done = self.parser.execute_command(f"complete task {tid}")
        self.assertEqual(res_done["status"], "SUCCESS")

    def test_05_destructive_action_confirmation(self):
        """Test that clearing all tasks requires explicit user confirmation."""
        # Without confirmation: should ask for confirmation
        res_unconfirmed = self.parser.execute_command("clear all tasks", confirm_destructive=False)
        self.assertEqual(res_unconfirmed["status"], "NEEDS_CONFIRMATION")

        # With confirmation: executes destruction
        res_confirmed = self.parser.execute_command("clear all tasks", confirm_destructive=True)
        self.assertEqual(res_confirmed["status"], "SUCCESS")
        self.assertEqual(len(self.db.get_todos()), 0)

    def test_06_smart_home_simulation(self):
        """Test smart-home automation extension state toggling."""
        res_on = self.parser.execute_command("smart home light on")
        self.assertEqual(res_on["status"], "SUCCESS")
        self.assertEqual(self.parser.smart_home_state["living_room_light"], "ON")

        res_off = self.parser.execute_command("smart home light off")
        self.assertEqual(res_off["status"], "SUCCESS")
        self.assertEqual(self.parser.smart_home_state["living_room_light"], "OFF")

    def test_07_command_history_logging(self):
        """Test that every command is persistently logged into SQLite audit trail."""
        self.parser.execute_command("what time is it")
        history = self.db.get_command_history()
        self.assertGreaterEqual(len(history), 1)
        self.assertEqual(history[0]["command_text"], "what time is it")

if __name__ == "__main__":
    unittest.main()

