"""
Logic Legend - PY-09 Secure Password Manager
Cryptographic Primitives, Key Derivation, & Password Utilities
"""

import os
import base64
import math
import secrets
import string
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
from cryptography.fernet import Fernet, InvalidToken

PBKDF2_ITERATIONS = 200_000

class CryptoUtils:
    @staticmethod
    def generate_salt(length: int = 16) -> bytes:
        """Generates a cryptographically secure random salt."""
        return os.urandom(length)

    @staticmethod
    def derive_fernet_key(master_password: str, salt: bytes, iterations: int = PBKDF2_ITERATIONS) -> bytes:
        """
        Derives a URL-safe base64-encoded 32-byte key from master password and salt
        using PBKDF2-HMAC-SHA256 suitable for Fernet symmetric encryption.
        """
        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=iterations,
        )
        key = kdf.derive(master_password.encode("utf-8"))
        return base64.urlsafe_b64encode(key)

    @classmethod
    def encrypt_data(cls, plaintext: str, fernet_key: bytes) -> str:
        """Encrypts plaintext string with Fernet (AES-128-CBC + HMAC-SHA256)."""
        f = Fernet(fernet_key)
        encrypted_bytes = f.encrypt(plaintext.encode("utf-8"))
        return encrypted_bytes.decode("utf-8")

    @classmethod
    def decrypt_data(cls, ciphertext: str, fernet_key: bytes) -> str:
        """Decrypts ciphertext string with Fernet. Raises InvalidToken on failure."""
        f = Fernet(fernet_key)
        decrypted_bytes = f.decrypt(ciphertext.encode("utf-8"))
        return decrypted_bytes.decode("utf-8")

    @staticmethod
    def generate_password(
        length: int = 16,
        use_upper: bool = True,
        use_lower: bool = True,
        use_digits: bool = True,
        use_symbols: bool = True,
        avoid_ambiguous: bool = True
    ) -> str:
        """
        Generates a cryptographically secure random password using secrets module.
        Ensures representation of each selected character pool.
        """
        if length < 6:
            length = 6
        if length > 128:
            length = 128

        chars = ""
        mandatory_chars = []

        ambiguous = "l1IO0o"

        if use_lower:
            pool = string.ascii_lowercase
            if avoid_ambiguous:
                pool = "".join(c for c in pool if c not in ambiguous)
            chars += pool
            mandatory_chars.append(secrets.choice(pool))

        if use_upper:
            pool = string.ascii_uppercase
            if avoid_ambiguous:
                pool = "".join(c for c in pool if c not in ambiguous)
            chars += pool
            mandatory_chars.append(secrets.choice(pool))

        if use_digits:
            pool = string.digits
            if avoid_ambiguous:
                pool = "".join(c for c in pool if c not in ambiguous)
            chars += pool
            mandatory_chars.append(secrets.choice(pool))

        if use_symbols:
            pool = "!@#$%^&*()_+-=[]{}|;:,.<>?"
            chars += pool
            mandatory_chars.append(secrets.choice(pool))

        if not chars:
            chars = string.ascii_letters + string.digits
            mandatory_chars.append(secrets.choice(chars))

        # Fill remaining length
        remaining = length - len(mandatory_chars)
        password_chars = mandatory_chars + [secrets.choice(chars) for _ in range(max(0, remaining))]

        # Shuffle cryptographically
        shuffled = list(password_chars)
        for i in range(len(shuffled) - 1, 0, -1):
            j = secrets.randbelow(i + 1)
            shuffled[i], shuffled[j] = shuffled[j], shuffled[i]

        return "".join(shuffled)

    @staticmethod
    def calculate_entropy(password: str) -> float:
        """
        Calculates information entropy (bits) using pool size R: E = L * log2(R).
        """
        if not password:
            return 0.0

        pool_size = 0
        has_lower = any(c.islower() for c in password)
        has_upper = any(c.isupper() for c in password)
        has_digit = any(c.isdigit() for c in password)
        has_symbol = any(not c.isalnum() for c in password)

        if has_lower:
            pool_size += 26
        if has_upper:
            pool_size += 26
        if has_digit:
            pool_size += 10
        if has_symbol:
            pool_size += 33

        if pool_size == 0:
            return 0.0

        return round(len(password) * math.log2(pool_size), 2)

    @classmethod
    def evaluate_strength(cls, password: str) -> dict:
        """
        Evaluates password strength across length, character pools, and patterns.
        Returns a score (0-100), rating category, entropy, and actionable advice.
        """
        if not password:
            return {
                "score": 0,
                "rating": "EMPTY",
                "entropy": 0.0,
                "feedback": ["Password cannot be empty."]
            }

        length = len(password)
        entropy = cls.calculate_entropy(password)
        feedback = []
        score = 0

        # Length evaluation
        if length < 8:
            feedback.append("Password is too short (minimum 8 characters).")
            score += 10
        elif length < 12:
            score += 25
        elif length < 16:
            score += 40
        else:
            score += 50

        # Diversity evaluation
        has_lower = any(c.islower() for c in password)
        has_upper = any(c.isupper() for c in password)
        has_digit = any(c.isdigit() for c in password)
        has_symbol = any(not c.isalnum() for c in password)

        variety = sum([has_lower, has_upper, has_digit, has_symbol])
        score += variety * 10

        if not has_upper:
            feedback.append("Add uppercase letters.")
        if not has_lower:
            feedback.append("Add lowercase letters.")
        if not has_digit:
            feedback.append("Add numerical digits.")
        if not has_symbol:
            feedback.append("Add special symbols (!@#$%).")

        # Check repetitive / trivial patterns
        if password.lower() in ["password", "123456", "admin123", "qwerty", "welcome"]:
            score = 10
            feedback.append("Common dictionary word or trivial sequence detected!")

        score = min(100, max(0, score))

        if score < 30:
            rating = "VERY WEAK"
        elif score < 50:
            rating = "WEAK"
        elif score < 70:
            rating = "MODERATE"
        elif score < 90:
            rating = "STRONG"
        else:
            rating = "VERY STRONG"

        return {
            "score": score,
            "rating": rating,
            "entropy": entropy,
            "feedback": feedback if feedback else ["Password meets high security standards."]
        }

