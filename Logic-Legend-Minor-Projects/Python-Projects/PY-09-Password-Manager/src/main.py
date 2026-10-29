"""
Logic Legend - PY-09 Secure Password Manager
Application Entry Point (CLI & GUI)
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

# Ensure src is in sys.path
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from database import VaultDB
from vault_service import VaultService
from crypto_utils import CryptoUtils

def seed_demo_vault(service):
    """Populates realistic credentials for viva evaluation."""
    print("[INFO] Seeding demo credentials into vault...")
    samples = [
        ("College Student Portal", "krishna@college.edu", "Col!ege#2026Pass", "https://portal.college.edu", "Education", "Semester registration portal"),
        ("GitHub Developer Account", "krish-coder", "GhP_78vT$xM9!kL2", "https://github.com", "Work", "Personal code repositories"),
        ("HDFC NetBanking", "hdfc_user_449", "Hdfc@Bank#9912", "https://netbanking.hdfcbank.com", "Banking", "Savings account netbanking"),
        ("ProtonMail Secure", "krish.logic@pm.me", "weakpassword", "https://proton.me", "Email", "Encrypted email box - demo weak pass"),
        ("LinkedIn Professional", "krishna@college.edu", "Col!ege#2026Pass", "https://linkedin.com", "Social", "Demo duplicate password reused from college portal")
    ]
    for title, user, pwd, url, cat, notes in samples:
        ok, msg = service.add_entry(title, user, pwd, url, cat, notes)
        print(f"  + Added: {title} ({user})")

def run_cli(service, db, args):
    print("=" * 65)
    print("  LOGIC LEGEND PY-09: SECURE PASSWORD MANAGER (CLI MODE)")
    print("=" * 65)

    if args.generate:
        length = args.length or 16
        pwd = CryptoUtils.generate_password(length=length)
        eval_res = CryptoUtils.evaluate_strength(pwd)
        print(f"\n[GENERATED PASSWORD] : {pwd}")
        print(f"  Length   : {length}")
        print(f"  Entropy  : {eval_res['entropy']} bits")
        print(f"  Strength : {eval_res['rating']} (Score: {eval_res['score']}/100)")
        return

    # Master setup
    if args.setup_master:
        ok, msg = service.setup_master_password(args.setup_master)
        print(f"[MASTER SETUP] {msg}")

    # Unlock vault
    master_key = args.unlock or args.setup_master
    if master_key:
        ok, msg = service.unlock_vault(master_key)
        if not ok:
            print(f"[ERROR] Failed to unlock vault: {msg}")
            return
        print(f"[UNLOCKED] {msg}")
    elif not service.is_master_configured():
        print("[NOTICE] Vault is uninitialized. Run with: --setup-master <password>")
        return
    else:
        print("[NOTICE] Vault is locked. Provide master password via: --unlock <password>")
        return

    if args.seed:
        seed_demo_vault(service)

    if args.add and args.title and args.username and args.password:
        ok, msg = service.add_entry(
            title=args.title,
            username=args.username,
            password=args.password,
            url=args.url or "",
            category=args.category or "General",
            notes=args.notes or ""
        )
        print(f"\n[ADD RESULT] Success: {ok} | {msg}")

    if args.list:
        entries = service.get_entries()
        print(f"\nStored Credentials ({len(entries)} items):")
        print(f"{'ID':<4} {'Title':<25} {'Username':<22} {'Category':<12} {'Password':<20}")
        print("-" * 88)
        for e in entries:
            pwd_disp = e['decrypted_password'] if args.show_passwords else "••••••••••••"
            print(f"{e['id']:<4} {e['title'][:24]:<25} {e['username'][:21]:<22} {e['category'][:11]:<12} {pwd_disp:<20}")

    if args.audit:
        report = service.audit_vault()
        print("\n" + "=" * 45)
        print("  VAULT SECURITY AUDIT REPORT")
        print("=" * 45)
        print(f"Total Credentials Analyzed : {report['total_credentials']}")
        print(f"Weak Passwords (Score < 50): {report['weak_count']}")
        print(f"Reused / Duplicate Passwords: {report['duplicate_count']}")
        if report['weak_entries']:
            print("\n[!] Weak Passwords Found:")
            for w in report['weak_entries']:
                print(f"  * {w['title']} ({w['username']}) - Rating: {w['rating']}")
        if report['duplicate_groups']:
            print("\n[!] Duplicate Passwords Reused Across:")
            for pwd, titles in report['duplicate_groups'].items():
                print(f"  * Reused in: {', '.join(titles)}")

    if args.export_backup:
        path = args.export_backup if isinstance(args.export_backup, str) and args.export_backup != "True" else "exports/vault_backup.enc"
        ok, path_out = service.export_encrypted_backup(path)
        print(f"\n[BACKUP] Exported encrypted vault to: {path_out} (Success: {ok})")

    print("\n" + "=" * 65)

def main():
    parser = argparse.ArgumentParser(description="Logic Legend PY-09 Secure Password Manager")
    parser.add_argument("--cli", action="store_true", help="Run in command-line mode")
    parser.add_argument("--setup-master", type=str, help="Setup master password")
    parser.add_argument("--unlock", type=str, help="Unlock vault with master password")
    parser.add_argument("--seed", action="store_true", help="Seed demo credentials")
    parser.add_argument("--list", action="store_true", help="List stored credentials")
    parser.add_argument("--show-passwords", action="store_true", help="Reveal passwords in list mode")
    parser.add_argument("--add", action="store_true", help="Add credential flag")
    parser.add_argument("--title", type=str, help="Credential title")
    parser.add_argument("--username", type=str, help="Username/email")
    parser.add_argument("--password", type=str, help="Password")
    parser.add_argument("--url", type=str, help="Website URL")
    parser.add_argument("--category", type=str, help="Category")
    parser.add_argument("--notes", type=str, help="Notes")
    parser.add_argument("--generate", action="store_true", help="Generate secure password")
    parser.add_argument("--length", type=int, default=16, help="Length for generator")
    parser.add_argument("--audit", action="store_true", help="Perform vault security audit")
    parser.add_argument("--export-backup", nargs="?", const="exports/vault_backup.enc", help="Export encrypted backup")

    args = parser.parse_args()

    db = VaultDB()
    service = VaultService(db)

    # If any CLI arguments are provided
    if (args.cli or args.setup_master or args.unlock or args.seed or args.list or 
        args.add or args.generate or args.audit or args.export_backup):
        run_cli(service, db, args)
    else:
        try:
            import tkinter as tk
            from gui import PasswordManagerGUI
            root = tk.Tk()
            app = PasswordManagerGUI(root, service)
            root.mainloop()
        except Exception as e:
            print(f"[GUI ERROR] Tkinter display unavailable ({e}). Falling back to CLI mode.")
            run_cli(service, db, args)

if __name__ == "__main__":
    main()

