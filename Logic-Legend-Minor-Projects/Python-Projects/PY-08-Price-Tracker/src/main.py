"""
Logic Legend - PY-08 Price Tracker with Web Scraping
Main Application Entry Point (CLI & GUI)
"""

import sys
import os
import argparse

# Configure UTF-8 stdout for Windows console safety
if sys.platform.startswith("win"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

# Ensure src package is importable
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from database import PriceTrackerDB
from scraper_engine import ScraperEngine
from notifier import Notifier
from tracker_service import TrackerService

def seed_sample_products(service, db):
    """Populates tracker with realistic demo products if database is empty."""
    existing = service.get_all_products()
    if len(existing) > 0:
        print("[INFO] Database already contains tracked products.")
        return

    print("[INFO] Seeding initial demo products...")
    samples = [
        ("https://example.com/products/wireless-headphones", "Sony WH-1000XM5 Wireless Headphones", 24999.0),
        ("https://example.com/products/mechanical-keyboard", "Keychron K2 Mechanical Keyboard", 6999.0),
        ("https://example.com/products/4k-monitor", "Dell 27-inch 4K UHD IPS Monitor", 28500.0),
        ("https://example.com/products/gaming-mouse", "Logitech G502 HERO High Performance Mouse", 3499.0)
    ]

    for url, title, target in samples:
        success, msg = service.add_product(url, title, target)
        print(f"  + {title}: {msg}")

def run_cli(service, db, args):
    print("=" * 65)
    print("  LOGIC LEGEND PY-08: AUTONOMOUS PRICE TRACKER (CLI MODE)")
    print("=" * 65)

    if args.seed:
        seed_sample_products(service, db)

    if args.add_url and args.title and args.target:
        success, msg = service.add_product(args.add_url, args.title, float(args.target))
        print(f"\n[ADD RESULT] Success: {success} | {msg}")

    if args.check_all:
        print("\n[CHECKING PRICES] Running automated scraper across active items...")
        results = service.check_all_active()
        for r in results:
            print(f"  * {r['title'][:32]} | Old: INR {r['old_price']:,.2f} -> New: INR {r['new_price']:,.2f} (Target: INR {r['target_price']:,.2f})")
            if r['alert_triggered']:
                print(f"    🚨 TRIGGERED: {r['alert_details']['message']}")

    products = service.get_all_products()
    print(f"\nCurrently Tracked Products ({len(products)} items):")
    print(f"{'ID':<4} {'Title':<35} {'Current (INR)':<15} {'Target (INR)':<15} {'Status':<10}")
    print("-" * 82)
    for p in products:
        print(f"{p['id']:<4} {p['title'][:34]:<35} {p['current_price']:<15,.2f} {p['target_price']:<15,.2f} {p['status']:<10}")

    if args.export:
        ok, path = service.export_csv()
        print(f"\n[EXPORT] CSV generated: {path} (Success: {ok})")

    print("\n" + "=" * 65)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-08 Price Tracker")
    parser.add_argument("--cli", action="store_true", help="Run in command-line interface mode")
    parser.add_argument("--seed", action="store_true", help="Seed default demo products")
    parser.add_argument("--check-all", action="store_true", help="Scrape prices for all active tracked items")
    parser.add_argument("--add-url", type=str, help="Add new product URL")
    parser.add_argument("--title", type=str, help="Product title for adding")
    parser.add_argument("--target", type=float, help="Target price threshold for adding")
    parser.add_argument("--export", action="store_true", help="Export price history to CSV")

    args = parser.parse_args()

    db = PriceTrackerDB()
    scraper = ScraperEngine()
    notifier = Notifier(db)
    service = TrackerService(db, scraper, notifier)

    if args.cli or args.seed or args.check_all or args.add_url or args.export:
        run_cli(service, db, args)
    else:
        try:
            import tkinter as tk
            from gui import PriceTrackerGUI
            root = tk.Tk()
            app = PriceTrackerGUI(root, service)
            root.mainloop()
        except Exception as e:
            print(f"[GUI ERROR] Tkinter display unavailable ({e}). Falling back to CLI mode.")
            run_cli(service, db, args)

if __name__ == "__main__":
    main()

