"""
Logic Legend - PY-09 Secure Password Manager
Vault Service & Credential Management Business Logic
"""

import json
import os
from datetime import datetime
from collections import defaultdict
from crypto_utils import CryptoUtils
from cryptography.fernet import InvalidToken

AUTH_CHECK_TOKEN_TEXT = "VAULT_ACCESS_GRANTED_SECURITY_TOKEN"

class VaultService:
    def __init__(self, db):
        self.db = db
        self.active_key = None

    def is_master_configured(self) -> bool:
        return self.db.is_master_initialized()

    def is_unlocked(self) -> bool:
        return self.active_key is not None

    def setup_master_password(self, master_password: str) -> tuple[bool, str]:
        """Initializes or resets the vault master password with a fresh random salt."""
        master_password = master_password.strip()
        if len(master_password) < 8:
            return False, "Master password must be at least 8 characters long."

        salt = CryptoUtils.generate_salt(16)
        key = CryptoUtils.derive_fernet_key(master_password, salt)
        check_token = CryptoUtils.encrypt_data(AUTH_CHECK_TOKEN_TEXT, key)

        self.db.store_master_auth(salt.hex(), check_token)
        self.active_key = key
        self.db.log_event("MASTER_SETUP", "Vault master password configured.")
        return True, "Master password configured successfully. Vault unlocked."

    def unlock_vault(self, master_password: str) -> tuple[bool, str]:
        """Verifies master password against stored check token and establishes session key."""
        auth_data = self.db.get_master_auth()
        if not auth_data:
            return False, "No master password configured. Please run setup first."

        salt = bytes.fromhex(auth_data["salt_hex"])
        candidate_key = CryptoUtils.derive_fernet_key(master_password, salt)

        try:
            decrypted = CryptoUtils.decrypt_data(auth_data["key_check_token"], candidate_key)
            if decrypted == AUTH_CHECK_TOKEN_TEXT:
                self.active_key = candidate_key
                self.db.log_event("VAULT_UNLOCKED", "Session unlocked successfully.")
                return True, "Vault unlocked successfully."
            else:
                self.db.log_event("LOGIN_FAILED", "Decryption mismatch during unlock attempt.")
                return False, "Invalid master password."
        except (InvalidToken, Exception):
            self.db.log_event("LOGIN_FAILED", "Invalid master password attempt.")
            return False, "Invalid master password."

    def lock_vault(self):
        """Terminates session and purges active cryptographic key from memory."""
        self.active_key = None
        self.db.log_event("VAULT_LOCKED", "Session locked.")

    def add_entry(self, title: str, username: str, password: str, url: str = "", category: str = "General", notes: str = "") -> tuple[bool, str]:
        if not self.is_unlocked():
            return False, "Vault is locked. Authenticate first."

        title = title.strip()
        username = username.strip()
        if not title or not username or not password:
            return False, "Title, username, and password are required fields."

        enc_pw = CryptoUtils.encrypt_data(password, self.active_key)

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO vault_entries (title, username, encrypted_password, url, category, notes)
                VALUES (?, ?, ?, ?, ?, ?)
            """, (title, username, enc_pw, url.strip(), category.strip(), notes.strip()))
            conn.commit()

        self.db.log_event("ENTRY_ADDED", f"Added credential: {title} ({username})")
        return True, f"Credential for '{title}' saved securely."

    def get_entries(self, search_query: str = "", category_filter: str = "") -> list[dict]:
        """Retrieves and decrypts stored credentials."""
        if not self.is_unlocked():
            return []

        query = "SELECT * FROM vault_entries WHERE 1=1"
        params = []

        if category_filter and category_filter != "All":
            query += " AND category = ?"
            params.append(category_filter)

        if search_query:
            query += " AND (title LIKE ? OR username LIKE ? OR url LIKE ? OR notes LIKE ?)"
            term = f"%{search_query.strip()}%"
            params.extend([term, term, term, term])

        query += " ORDER BY title ASC"

        results = []
        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, tuple(params))
            rows = cursor.fetchall()

        for r in rows:
            entry = dict(r)
            try:
                entry["decrypted_password"] = CryptoUtils.decrypt_data(entry["encrypted_password"], self.active_key)
            except Exception:
                entry["decrypted_password"] = "DECRYPTION_ERROR"
            results.append(entry)

        return results

    def get_entry_by_id(self, entry_id: int) -> dict | None:
        if not self.is_unlocked():
            return None

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM vault_entries WHERE id = ?", (entry_id,))
            row = cursor.fetchone()
            if not row:
                return None

            entry = dict(row)
            try:
                entry["decrypted_password"] = CryptoUtils.decrypt_data(entry["encrypted_password"], self.active_key)
            except Exception:
                entry["decrypted_password"] = "DECRYPTION_ERROR"
            return entry

    def update_entry(self, entry_id: int, title: str, username: str, password: str, url: str = "", category: str = "General", notes: str = "") -> tuple[bool, str]:
        if not self.is_unlocked():
            return False, "Vault is locked."

        title = title.strip()
        username = username.strip()
        if not title or not username or not password:
            return False, "Title, username, and password are required."

        enc_pw = CryptoUtils.encrypt_data(password, self.active_key)

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                UPDATE vault_entries
                SET title = ?, username = ?, encrypted_password = ?, url = ?, category = ?, notes = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """, (title, username, enc_pw, url.strip(), category.strip(), notes.strip(), entry_id))
            conn.commit()

        self.db.log_event("ENTRY_UPDATED", f"Updated credential ID {entry_id} ({title})")
        return True, f"Credential '{title}' updated successfully."

    def delete_entry(self, entry_id: int) -> tuple[bool, str]:
        if not self.is_unlocked():
            return False, "Vault is locked."

        with self.db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM vault_entries WHERE id = ?", (entry_id,))
            conn.commit()
            if cursor.rowcount > 0:
                self.db.log_event("ENTRY_DELETED", f"Deleted credential ID {entry_id}")
                return True, "Credential deleted successfully."
            return False, "Entry not found."

    def audit_vault(self) -> dict:
        """
        Scans all vault entries for security vulnerabilities:
        1. Weak passwords (evaluated score < 50)
        2. Duplicate / reused passwords across accounts
        """
        if not self.is_unlocked():
            return {"error": "Vault must be unlocked to run security audit."}

        entries = self.get_entries()
        total = len(entries)
        weak_list = []
        password_usage = defaultdict(list)

        for e in entries:
            pwd = e["decrypted_password"]
            strength = CryptoUtils.evaluate_strength(pwd)
            if strength["score"] < 50:
                weak_list.append({
                    "id": e["id"],
                    "title": e["title"],
                    "username": e["username"],
                    "score": strength["score"],
                    "rating": strength["rating"],
                    "feedback": strength["feedback"]
                })
            password_usage[pwd].append(e["title"])

        duplicate_groups = {pwd: titles for pwd, titles in password_usage.items() if len(titles) > 1}

        return {
            "total_credentials": total,
            "weak_count": len(weak_list),
            "weak_entries": weak_list,
            "duplicate_count": sum(len(titles) for titles in duplicate_groups.values()),
            "duplicate_groups": duplicate_groups
        }

    def export_encrypted_backup(self, filepath: str = "exports/vault_backup.enc") -> tuple[bool, str]:
        """Exports entire vault serialized as JSON and encrypted with active key."""
        if not self.is_unlocked():
            return False, "Vault must be unlocked to export backup."

        entries = self.get_entries()
        export_payload = {
            "exported_at": datetime.now().isoformat(),
            "count": len(entries),
            "credentials": [
                {
                    "title": e["title"],
                    "username": e["username"],
                    "password": e["decrypted_password"],
                    "url": e["url"],
                    "category": e["category"],
                    "notes": e["notes"]
                }
                for e in entries
            ]
        }

        raw_json = json.dumps(export_payload, indent=2)
        encrypted_blob = CryptoUtils.encrypt_data(raw_json, self.active_key)

        auth = self.db.get_master_auth()
        salt_hex = auth["salt_hex"] if auth else ""
        bundle = {
            "format": "LOGIC_LEGEND_ENCRYPTED_VAULT_V1",
            "salt_hex": salt_hex,
            "ciphertext": encrypted_blob
        }

        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(bundle, f, indent=2)

        self.db.log_event("VAULT_BACKUP_EXPORTED", f"Exported {len(entries)} items to {filepath}")
        return True, filepath

    def import_encrypted_backup(self, filepath: str, master_password: str = None) -> tuple[bool, str]:
        """Decrypts and imports credentials from an encrypted backup file."""
        if not self.is_unlocked():
            return False, "Vault must be unlocked to import backup."

        if not os.path.exists(filepath):
            return False, f"Backup file not found: {filepath}"

        try:
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()

            try:
                bundle = json.loads(content)
                if isinstance(bundle, dict) and "ciphertext" in bundle:
                    ciphertext = bundle["ciphertext"]
                    salt_hex = bundle.get("salt_hex")
                    if master_password and salt_hex:
                        key = CryptoUtils.derive_fernet_key(master_password, bytes.fromhex(salt_hex))
                    else:
                        key = self.active_key
                    raw_json = CryptoUtils.decrypt_data(ciphertext, key)
                else:
                    raw_json = CryptoUtils.decrypt_data(content, self.active_key)
            except Exception:
                raw_json = CryptoUtils.decrypt_data(content, self.active_key)

            payload = json.loads(raw_json)

            imported = 0
            for item in payload.get("credentials", []):
                self.add_entry(
                    title=item.get("title", "Imported"),
                    username=item.get("username", ""),
                    password=item.get("password", ""),
                    url=item.get("url", ""),
                    category=item.get("category", "General"),
                    notes=item.get("notes", "")
                )
                imported += 1

            self.db.log_event("VAULT_BACKUP_IMPORTED", f"Imported {imported} credentials from {filepath}")
            return True, f"Successfully restored {imported} credentials into vault."
        except InvalidToken:
            return False, "Decryption failed. Backup was created with a different master password."
        except Exception as e:
            return False, f"Failed to restore backup: {str(e)}"
