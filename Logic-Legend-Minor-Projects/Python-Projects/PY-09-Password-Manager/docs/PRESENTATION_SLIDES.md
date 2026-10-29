# Viva Presentation Slides: PY-09 Secure Password Manager

---

### Slide 1: Title Slide
- **Project Title**: Secure Password Manager with AES Encryption
- **Project Code**: PY-09 (Logic Legend Minor Project Suite)
- **Domain**: Cybersecurity, Cryptography & Desktop GUI
- **Presenter**: Engineering Candidate
- **Advisor/Mentor**: Project Evaluation Committee

---

### Slide 2: Problem Statement
- Humans cannot safely remember dozens of unique, 16+ character high-entropy passwords.
- Common habits: reusing passwords across multiple sites, using weak dictionary phrases, or storing plaintext in unprotected files.
- Credential stuffing and database leaks compromise millions of interconnected accounts every year.

---

### Slide 3: Proposed Solution
- A standalone, zero-knowledge, cryptographically fortified desktop vault.
- Strong key derivation: PBKDF2-HMAC-SHA256 with 200,000 iterations and 16-byte random salt.
- Authenticated symmetric encryption: Fernet (AES-128-CBC + HMAC-SHA256).
- Proactive security: cryptographic password generator, real-time entropy evaluation, clipboard auto-clearing, and vulnerability auditing.

---

### Slide 4: System Architecture
- **Layer 1: User Interface**: Tkinter modern split-view dashboard + Headless CLI.
- **Layer 2: Service / Logic**: `VaultService` handling authentication, in-memory key lifecycle, CRUD, and audit routines.
- **Layer 3: Cryptography**: `CryptoUtils` implementing key derivation, Fernet encryption, secrets-based generation, and Shannon entropy.
- **Layer 4: Data Storage**: SQLite database with encrypted records and audit trails.

---

### Slide 5: Cryptographic Specifications
- **Master Password Derivation**:
  - Algorithm: PBKDF2 with HMAC-SHA256.
  - Iterations: 200,000 rounds (mitigates GPU/ASIC brute-force attacks).
  - Salt: 16 bytes generated via system CSPRNG (`os.urandom`).
- **Data Encryption**:
  - Fernet specification: AES-128 in CBC mode with PKCS7 padding.
  - Authentication: HMAC-SHA256 signature ensures tamper-proof verification (Encrypt-then-MAC).

---

### Slide 6: Zero-Knowledge Verification
- How is the master password verified without storing it?
- We encrypt a known constant string (`VAULT_ACCESS_GRANTED_SECURITY_TOKEN`) during setup using the derived key.
- Upon login, the candidate key attempts to decrypt this canary token.
- If decryption succeeds and matches, the key is valid and held in memory for the session.
- If decryption fails (`InvalidToken`), authentication is denied immediately.

---

### Slide 7: Password Generation & Strength Evaluation
- **Generator**: Uses Python's `secrets` module to pick cryptographically unbiased characters from chosen pools (upper, lower, digits, symbols), avoiding ambiguous symbols (`l, 1, I, O, 0`).
- **Strength Evaluation**:
  - Calculates Shannon entropy: $E = L \times \log_2(R)$.
  - Grades passwords into: `VERY WEAK`, `WEAK`, `MODERATE`, `STRONG`, `VERY STRONG`.
  - Provides constructive feedback (e.g., "Add uppercase letters", "Dictionary word detected").

---

### Slide 8: Proactive Security Features
- **Auto-Clear Clipboard**: Automatically wipes copied passwords from the OS clipboard after 20 seconds.
- **Vulnerability Audit**: Scans all vault entries for weak passwords (score < 50) and dangerous password reuse across accounts.
- **Encrypted Portability**: Full JSON vault backup encrypted with salt header for secure transport.

---

### Slide 9: Automated Verification & Testing
- 8/8 Automated unit tests passing:
  - PBKDF2 derivation and Fernet round-trip encryption.
  - Rejection of invalid decryption keys.
  - CSPRNG generator length and diversity.
  - Mathematical entropy and strength scoring.
  - Master password setup and unlock validation.
  - Vault CRUD operations in SQLite.
  - Security audit detection of weak and duplicate credentials.
  - Encrypted backup export and restoration.

---

### Slide 10: Conclusion & Future Scope
- **Conclusion**: A production-grade, impenetrable local password manager adhering to NIST digital identity guidelines.
- **Future Scope**:
  - Hardware token integration (YubiKey / FIDO2).
  - Time-based One-Time Password (TOTP) two-factor authentication generator.
  - Biometric integration via Windows Hello.

