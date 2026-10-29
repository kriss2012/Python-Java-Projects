"""
Logic Legend - PY-01 Smart Expense Tracker with Analytics
Main Application Entrypoint
"""

import sys
import os
import argparse
import tkinter as tk

# Ensure current dir is in Python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from database import DatabaseManager
from tracker_service import TrackerService
from gui import ExpenseTrackerGUI

def run_cli_summary(service):
    summary = service.get_financial_summary()
    print("=" * 60)
    print("      LOGIC LEGEND PY-01 EXPENSE TRACKER - CLI SUMMARY")
    print("=" * 60)
    print(f"Total Income   : ₹ {summary['income']:,.2f}")
    print(f"Total Expenses : ₹ {summary['expense']:,.2f}")
    print(f"Net Balance    : ₹ {summary['balance']:,.2f}")
    print(f"Savings Rate   : {summary['savings_rate']} %")
    print("-" * 60)
    print("Active Budgets:")
    for b in service.check_budgets():
        print(f"  [{b['status']:^8}] {b['category']}: Spent ₹{b['spent']} of ₹{b['limit']} ({b['percent_used']}%)")
    print("=" * 60)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-01 Smart Expense Tracker")
    parser.add_argument("--cli", action="store_true", help="Run in command-line summary mode")
    parser.add_argument("--seed", action="store_true", help="Seed demo transaction data")
    parser.add_argument("--export", action="store_true", help="Export summary report to file")
    args = parser.parse_args()

    # Determine database path relative to project root
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    db_path = os.path.join(base_dir, "data", "expenses.db")

    db_mgr = DatabaseManager(db_path=db_path)
    service = TrackerService(db_mgr)

    # Seed demo data if database is fresh or explicitly requested
    if args.seed:
        db_mgr.seed_demo_data()
        print("Demo data seeded successfully.")

    if args.export:
        out_path = os.path.join(base_dir, "exports", "financial_report.txt")
        service.export_summary_report(out_path)
        print(f"Exported report to: {out_path}")

    if args.cli:
        run_cli_summary(service)
        return

    # Automatically seed sample data if starting fresh
    db_mgr.seed_demo_data()

    # Launch GUI
    root = tk.Tk()
    app = ExpenseTrackerGUI(root, service)
    root.mainloop()

if __name__ == "__main__":
    main()
