"""
Logic Legend - PY-06 Personal Voice Assistant
Main Application Entrypoint
"""

import sys
import os
import argparse
import tkinter as tk

# Ensure current dir is in Python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

from database import AssistantDatabase
from voice_engine import VoiceEngine
from command_parser import CommandParser
from gui import AssistantGUI

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-06 Personal Voice Assistant")
    parser.add_argument("--cli", action="store_true", help="Run in command-line text mode")
    parser.add_argument("--cmd", type=str, help="Single command to execute via CLI")
    parser.add_argument("--no-voice", action="store_true", help="Mute text-to-speech engine")
    args = parser.parse_args()

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    db_path = os.path.join(base_dir, "data", "assistant.db")

    db = AssistantDatabase(db_path=db_path)
    voice = VoiceEngine(voice_enabled=not args.no_voice)
    cmd_parser = CommandParser(db=db, voice_engine=voice)

    if args.cmd:
        res = cmd_parser.execute_command(args.cmd)
        print(f"Command : {args.cmd}")
        print(f"Status  : {res.get('status')}")
        print(f"Response: {res.get('response')}")
        return

    if args.cli:
        print("=" * 60)
        print("      LOGIC LEGEND PY-06 VOICE ASSISTANT (CLI MODE)")
        print("Type 'exit' to quit. Commands: time, date, add task <title>, etc.")
        print("=" * 60)
        while True:
            try:
                inp = input("\nYou > ").strip()
                if inp.lower() in ("exit", "quit"):
                    break
                res = cmd_parser.execute_command(inp)
                print(f"Assistant > {res['response']}")
            except (KeyboardInterrupt, EOFError):
                break
        return

    # Launch Tkinter Desktop Application
    root = tk.Tk()
    app = AssistantGUI(root, cmd_parser, voice, db)
    root.mainloop()

if __name__ == "__main__":
    main()

