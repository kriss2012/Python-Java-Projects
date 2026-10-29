"""
Logic Legend - PY-09 Secure Password Manager
Automated Unit Test Suite
"""

import unittest
import os
import sys
import tempfile
import gc
from cryptography.fernet import InvalidToken

# Add src to path
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(BASE_DIR, "src")
if SRC_DIR not in sys.path:
    sys.path.insert(0, SRC_DIR)

from crypto_utils import CryptoUtils
from database import VaultDB
from vault_service import VaultService

class TestPasswordManager(unittest.TestCase):
    def setUp(self):
        self.temp_fd, self.temp_db_path = tempfile.mkstemp(suffix=".db")
        os.close(self.temp_fd)

        self.db = VaultDB(db_path=self.temp_db_path)
        self.service = VaultService(self.db)

    def tearDown(self):
        del self.service
        del self.db
        gc.collect()
        if os.path.exists(self.temp_db_path):
            try:
                os.remove(self.temp_db_path)
            except OSError:
                pass

    def test_crypto_key_derivation_and_encryption(self):
        """Verify PBKDF2 key derivation and Fernet AES encryption roundtrip."""
        salt = CryptoUtils.generate_salt(16)
        key = CryptoUtils.derive_fernet_key("MasterSecretKey!2026", salt)
        self.assertIsNotNone(key)
        self.assertEqual(len(key), 44)  # 32 bytes base64-encoded is 44 chars

        plaintext = "SuperSecretBankPassword#99"
        ciphertext = CryptoUtils.encrypt_data(plaintext, key)
        self.assertNotEqual(plaintext, ciphertext)

        decrypted = CryptoUtils.decrypt_data(ciphertext, key)
        self.assertEqual(plaintext, decrypted)

    def test_crypto_invalid_key_decryption(self):
        """Verify decrypting with an incorrect key raises InvalidToken."""
        salt1 = CryptoUtils.generate_salt(16)
        salt2 = CryptoUtils.generate_salt(16)
        key1 = CryptoUtils.derive_fernet_key("PasswordOne!123", salt1)
        key2 = CryptoUtils.derive_fernet_key("PasswordTwo!456", salt2)

        ciphertext = CryptoUtils.encrypt_data("SensitiveData", key1)
        with self.assertRaises(InvalidToken):
            CryptoUtils.decrypt_data(ciphertext, key2)

    def test_password_generator(self):
        """Verify password generator produces expected length and requested character types."""
        pwd = CryptoUtils.generate_password(length=20, use_upper=True, use_lower=True, use_digits=True, use_symbols=True)
        self.assertEqual(len(pwd), 20)
        self.assertTrue(any(c.isupper() for c in pwd))
        self.assertTrue(any(c.islower() for c in pwd))
        self.assertTrue(any(c.isdigit() for c in pwd))
        self.assertTrue(any(not c.isalnum() for c in pwd))

    def test_password_strength_and_entropy(self):
        """Verify entropy calculation and strength evaluation categories."""
        weak = CryptoUtils.evaluate_strength("123456")
        self.assertIn(weak["rating"], ["VERY WEAK", "WEAK"])
        self.assertLess(weak["score"], 40)

        strong = CryptoUtils.evaluate_strength("X9#mK$2vL@qP!8zW")
        self.assertIn(strong["rating"], ["STRONG", "VERY STRONG"])
        self.assertGreaterEqual(strong["score"], 80)
        self.assertGreater(strong["entropy"], 60.0)

    def test_vault_master_setup_and_unlock(self):
        """Verify master password initialization and authentication rejection."""
        self.assertFalse(self.service.is_master_configured())

        # Setup with valid password
        ok, msg = self.service.setup_master_password("MasterVaultPass#2026")
        self.assertTrue(ok)
        self.assertTrue(self.service.is_master_configured())
        self.assertTrue(self.service.is_unlocked())

        # Lock and unlock with wrong password
        self.service.lock_vault()
        self.assertFalse(self.service.is_unlocked())

        fail_ok, _ = self.service.unlock_vault("WrongPassword123")
        self.assertFalse(fail_ok)
        self.assertFalse(self.service.is_unlocked())

        # Unlock with correct password
        success_ok, _ = self.service.unlock_vault("MasterVaultPass#2026")
        self.assertTrue(success_ok)
        self.assertTrue(self.service.is_unlocked())

    def test_vault_crud_operations(self):
        """Verify adding, searching, updating, and deleting credentials."""
        self.service.setup_master_password("MasterPassword!1")

        # Add entry
        ok, msg = self.service.add_entry("GitHub", "octocat", "GitPass#99", "https://github.com", "Work", "Dev token")
        self.assertTrue(ok)

        entries = self.service.get_entries()
        self.assertEqual(len(entries), 1)
        self.assertEqual(entries[0]["title"], "GitHub")
        self.assertEqual(entries[0]["decrypted_password"], "GitPass#99")

        entry_id = entries[0]["id"]

        # Update entry
        up_ok, _ = self.service.update_entry(entry_id, "GitHub Enterprise", "octocat", "NewSecret#2026", "https://github.com", "Work", "Updated")
        self.assertTrue(up_ok)

        updated = self.service.get_entry_by_id(entry_id)
        self.assertEqual(updated["title"], "GitHub Enterprise")
        self.assertEqual(updated["decrypted_password"], "NewSecret#2026")

        # Delete entry
        del_ok, _ = self.service.delete_entry(entry_id)
        self.assertTrue(del_ok)
        self.assertEqual(len(self.service.get_entries()), 0)

    def test_vault_security_audit(self):
        """Verify detection of weak and duplicate passwords across vault entries."""
        self.service.setup_master_password("MasterPass#2026")
        self.service.add_entry("Account A", "userA", "123456", category="General")  # Weak
        self.service.add_entry("Account B", "userB", "SharedSafe#123", category="General")  # Shared
        self.service.add_entry("Account C", "userC", "SharedSafe#123", category="General")  # Shared

        audit = self.service.audit_vault()
        self.assertEqual(audit["total_credentials"], 3)
        self.assertGreaterEqual(audit["weak_count"], 1)
        self.assertEqual(audit["duplicate_count"], 2)
        self.assertIn("SharedSafe#123", audit["duplicate_groups"])

    def test_vault_encrypted_backup_and_restore(self):
        """Verify encrypted JSON export and restore into another vault."""
        self.service.setup_master_password("MasterPass#2026")
        self.service.add_entry("Netflix", "streamer@mail.com", "Stream#992", category="Entertainment")

        temp_dir = tempfile.mkdtemp()
        backup_path = os.path.join(temp_dir, "test_vault_backup.enc")

        # Export backup
        ok, res = self.service.export_encrypted_backup(backup_path)
        self.assertTrue(ok)
        self.assertTrue(os.path.exists(backup_path))

        # Create a fresh database and service with the same master password
        fresh_fd, fresh_db_path = tempfile.mkstemp(suffix=".db")
        os.close(fresh_fd)
        fresh_db = VaultDB(db_path=fresh_db_path)
        fresh_service = VaultService(fresh_db)
        fresh_service.setup_master_password("MasterPass#2026")

        # Restore into fresh vault using same master password
        res_ok, msg = fresh_service.import_encrypted_backup(backup_path, master_password="MasterPass#2026")
        self.assertTrue(res_ok)

        restored_entries = fresh_service.get_entries()
        self.assertEqual(len(restored_entries), 1)
        self.assertEqual(restored_entries[0]["title"], "Netflix")
        self.assertEqual(restored_entries[0]["decrypted_password"], "Stream#992")

        # Cleanup
        del fresh_service
        del fresh_db
        gc.collect()
        try:
            os.remove(backup_path)
            os.rmdir(temp_dir)
            os.remove(fresh_db_path)
        except OSError:
            pass

if __name__ == "__main__":
    unittest.main()
