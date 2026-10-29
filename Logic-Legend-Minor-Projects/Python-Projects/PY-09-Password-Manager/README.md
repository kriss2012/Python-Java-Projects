# Logic Legend — PY-09: Secure Password Manager

A zero-knowledge, cryptographically fortified desktop credential vault and password security management system built using Python, Tkinter, SQLite, and the standard `cryptography` primitives (PBKDF2-HMAC-SHA256 and AES-128/256-CBC with HMAC). The application provides end-to-end credential encryption, entropy-driven password generation, live strength evaluation, automatic clipboard clearing, vulnerability audits (weak & reused passwords), and encrypted vault backups.

---

## 1. Project Title and Identifier
- **Project Name**: Secure Password Manager
- **Project Code**: `PY-09`
- **Application Category**: Cybersecurity, Applied Cryptography & Desktop GUI
- **Target Audience**: College students, developers, security researchers, and faculty evaluators.

---

## 2. Executive Summary
Credential reuse, weak passwords, and unencrypted credential storage are among the leading causes of security breaches today. Most users struggle to memorize dozens of distinct, high-entropy passwords, leading to dangerous patterns like writing credentials in text files or reusing identical passwords across multiple services. The **Secure Password Manager** solves this problem by establishing a zero-knowledge local vault: a single high-strength master password derives an AES-256 encryption key via PBKDF2 with 200,000 iterations and a unique cryptographic salt. All credentials are encrypted prior to being written to an ACID-compliant SQLite database, ensuring that even if the physical database file is stolen, credentials remain impenetrable without the master key.

---

## 3. Key Features
- **Zero-Knowledge Architecture**: The master password is never stored on disk in plaintext or as an unsalted hash. Authentication uses an encrypted canary token (`VAULT_ACCESS_GRANTED_SECURITY_TOKEN`).
- **Military-Grade Cryptography**: Employs PBKDF2-HMAC-SHA256 key derivation with 200,000 hashing rounds and Fernet symmetric authenticated encryption (AES-128-CBC + HMAC-SHA256).
- **Cryptographic Password Generator**: Uses Python's `secrets` module to generate high-entropy passwords with configurable length, character sets (upper, lower, digits, symbols), and exclusion of ambiguous characters (`l, 1, I, O, 0`).
- **Information Entropy & Strength Meter**: Calculates mathematical Shannon entropy ($E = L \times \log_2(R)$) and provides real-time visual strength scoring (Very Weak, Weak, Moderate, Strong, Very Strong) with actionable feedback.
- **Clipboard Auto-Clear**: Automatically wipes copied passwords from the OS clipboard after 20 seconds to prevent clipboard sniffing attacks.
- **Security Audit Engine**: Analyzes stored vault credentials to detect weak passwords (score < 50) and dangerous password reuse across accounts.
- **Encrypted Backup & Portability**: Exports and restores full vault backups as encrypted JSON bundles with cryptographically tied salts.
- **Desktop Tkinter Dashboard & Dual CLI**: Provides an intuitive graphical interface alongside a comprehensive command-line interface for headless environments and automation.

---

## 4. System Architecture
The application is structured into four cohesive layers:
1. **Presentation Layer (`src/gui.py`, `src/main.py`)**: Tkinter graphical UI with split master/detail view, generator modal, live strength meters, and CLI runner.
2. **Business Logic Layer (`src/vault_service.py`)**: Manages session state, handles encryption/decryption workflows, coordinates security audits, and manages backups.
3. **Cryptographic Primitives Layer (`src/crypto_utils.py`)**: Implements PBKDF2-HMAC key derivation, Fernet encryption, entropy calculations, and password generation.
4. **Data Persistence Layer (`src/database.py`)**: SQLite database (`data/password_manager.db`) maintaining `master_auth`, `vault_entries`, and `security_audit_logs`.

---

## 5. Technology Stack and Prerequisites
- **Language**: Python 3.10+
- **Cryptographic Engine**: `cryptography >= 42.0.0`
- **Clipboard Management**: `pyperclip >= 1.8.2`
- **Database**: SQLite 3 (Standard Library)
- **GUI Framework**: Tkinter / ttk (Python Standard Library)

---

## 6. Directory Structure
```
PY-09-Password-Manager/
├── .env.example              # Configuration template for vault parameters
├── .gitignore                # Git exclusion rules for databases and caches
├── requirements.txt          # Python dependency specifications
├── README.md                 # 19-section comprehensive documentation
├── data/                     # SQLite database storage directory
├── exports/                  # Encrypted vault backups (.enc)
├── docs/                     # Academic deliverables
│   ├── ACADEMIC_REPORT.md    # Detailed college report with Mermaid diagrams
│   ├── PRESENTATION_SLIDES.md# Viva presentation slide deck
│   ├── VIVA_QUESTIONS.md     # 15+ viva examination Q&As
│   └── DEMO_GUIDE.md         # Evaluator test procedure
├── src/
│   ├── __init__.py           # Package marker
│   ├── database.py           # SQLite schema and audit logging
│   ├── crypto_utils.py       # Key derivation, Fernet, generator, and entropy
│   ├── vault_service.py      # Session management, CRUD, audit, and backups
│   ├── gui.py                # Tkinter visual application
│   └── main.py               # Application entry point (GUI + CLI)
└── tests/
    ├── __init__.py           # Test package marker
    └── test_password_manager.py # Automated unit test suite
```

---

## 7. Installation and Setup
1. Clone or navigate to the project directory:
   ```bash
   cd Logic-Legend-Minor-Projects/Python-Projects/PY-09-Password-Manager
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv venv
   # On Windows:
   venv\Scripts\activate
   # On Linux/macOS:
   source venv/bin/activate
   ```
3. Install required packages:
   ```bash
   python -m pip install -r requirements.txt
   ```

---

## 8. Usage Instructions
### Running the Graphical Application
Launch the Tkinter dashboard:
```bash
python src/main.py
```
- If running for the first time, the application prompts you to initialize your Master Password.
- If already initialized, enter your Master Password to unlock the vault.

### Running in Headless / CLI Mode
Setup master password, seed demo items, run security audit, and export encrypted backup:
```bash
python src/main.py --setup-master "MasterSecret!2026" --seed --list --show-passwords --audit --export-backup --cli
```

Generate a secure 24-character random password from the CLI:
```bash
python src/main.py --generate --length 24 --cli
```

Unlock vault and list credentials:
```bash
python src/main.py --unlock "MasterSecret!2026" --list --cli
```

---

## 9. Database Schema and Security Architecture
The SQLite database (`data/password_manager.db`) contains three structured tables:
- **`master_auth`**:
  - `id`: Fixed to 1 (enforces single-master constraint).
  - `salt_hex`: 16-byte random salt in hexadecimal.
  - `key_check_token`: Fernet-encrypted canary string used to verify master key validity without saving the password.
  - `created_at`: Initialization timestamp.
- **`vault_entries`**:
  - `id`: Autoincrementing primary key.
  - `title`, `username`, `category`, `url`, `notes`.
  - `encrypted_password`: AES-128-CBC + HMAC-SHA256 ciphertext (base64 string).
  - `created_at`, `updated_at`.
- **`security_audit_logs`**:
  - `id`, `event_type` (`MASTER_SETUP`, `VAULT_UNLOCKED`, `ENTRY_ADDED`, `LOGIN_FAILED`), `details`, `timestamp`.

---

## 10. Cryptographic Workflow
1. **Key Derivation (PBKDF2)**:
   $$\text{Key} = \text{PBKDF2-HMAC-SHA256}(\text{Password}, \text{Salt}, \text{rounds}=200000, \text{dklen}=32)$$
2. **Encryption (Fernet)**:
   - Derives IV using system CSPRNG (`os.urandom(16)`).
   - Encrypts plaintext using AES-128 in CBC mode.
   - Computes HMAC-SHA256 signature over version, timestamp, IV, and ciphertext.
   - Concatenates and encodes as URL-safe Base64.
3. **Decryption & Verification**:
   - Verifies HMAC signature before attempting AES decryption (Encrypt-then-MAC).
   - Raises `InvalidToken` immediately if ciphertext is modified or if incorrect master key is used.

---

## 11. Password Strength and Entropy Formulation
Information entropy measures unpredictability in bits:
$$E = L \times \log_2(R)$$
Where:
- $L$ is the length of the password.
- $R$ is the size of the character pool:
  - Lowercase letters ($a-z$): 26
  - Uppercase letters ($A-Z$): 26
  - Numerical digits ($0-9$): 10
  - Special symbols: 33
  - Max pool $R = 95$

**Strength Categories**:
- $< 30$ bits / score: **VERY WEAK**
- $30 - 49$ bits / score: **WEAK**
- $50 - 69$ bits / score: **MODERATE**
- $70 - 89$ bits / score: **STRONG**
- $\ge 90$ bits / score: **VERY STRONG**

---

## 12. Testing and Verification
Run the automated test suite:
```bash
python tests/test_password_manager.py
```
Test results verify:
- ✅ PBKDF2 key derivation and Fernet encryption roundtrip
- ✅ Authentication rejection on invalid master passwords (`InvalidToken`)
- ✅ Cryptographic password generator length and diversity
- ✅ Shannon entropy and strength grading accuracy
- ✅ Master password setup, locking, and unlocking cycles
- ✅ Full vault CRUD operations
- ✅ Security audit detection of weak and duplicate passwords
- ✅ Encrypted backup export and cross-vault restoration

---

## 13. Threat Model and Defense Mechanisms
| Threat | Mitigation Strategy |
|---|---|
| Database File Theft | All credentials encrypted at rest with AES-256; master password never stored |
| Brute-Force Password Cracking | 200,000 PBKDF2 hashing rounds with unique 16-byte random salt |
| Memory Scraping | Encryption keys purged from memory upon vault lock; passwords decrypted only on-demand |
| Clipboard Snooping | Clipboard is automatically wiped clean after 20 seconds |
| Shoulder Surfing | Password fields masked with `●` by default; eye toggle required to reveal |
| SQL Injection | 100% parameterized SQL queries via SQLite `?` placeholders |

---

## 14. Academic Relevance and College Submission
This project satisfies university curriculum criteria for:
- Applied Cryptography & Information Security
- Operating Systems (Entropy, Process Memory, Secure File Storage)
- Object-Oriented Software Engineering (OOD/OOP)
- Database Management Systems (ACID Transactions, Foreign Keys)
- Human-Computer Interaction (HCI / UX Security Patterns)

---

## 15. Known Limitations
- Hardware security module (HSM) or TPM integration is not used; master key security relies on OS-level memory isolation.
- Cloud sync is intentionally omitted to preserve zero-knowledge local autonomy.

---

## 16. Future Enhancements
- Biometric authentication (Windows Hello / Touch ID / Fingerprint sensor).
- Two-factor authentication (TOTP authenticator generator) integrated per account.
- Browser extension integration for auto-filling web login forms.

---

## 17. Evaluator Quick Start
```bash
# 1. Run automated test suite
python tests/test_password_manager.py

# 2. Run CLI demo with seeded credentials and security audit
python src/main.py --setup-master "MasterSecret!2026" --seed --list --show-passwords --audit --export-backup --cli

# 3. Launch GUI
python src/main.py
```

---

## 18. References
- RFC 2898: PKCS #5: Password-Based Cryptography Specification Version 2.0
- NIST Special Publication 800-63B: Digital Identity Guidelines
- Cryptography Python Library: `cryptography.io`

---

## 19. License
Educational Open-Source License — Free for academic, learning, and college submission use.

