# Academic Project Report: PY-09 Secure Password Manager

**Department of Computer Science and Engineering**  
*Undergraduate Minor Project Suite — Logic Legend*

---

## Abstract
In an increasingly digitized society, the proliferation of online accounts has exacerbated the risks of credential reuse, weak passwords, and unauthorized account access. The **Secure Password Manager** is an autonomous desktop application built using Python, Tkinter, SQLite, and the Python `cryptography` library. It implements a zero-knowledge local security architecture wherein sensitive credentials are protected using PBKDF2-HMAC-SHA256 key derivation with 200,000 hashing rounds and Fernet authenticated symmetric encryption (AES-128-CBC + HMAC-SHA256). The application incorporates a cryptographically secure random password generator, a Shannon entropy and strength evaluator, an automated clipboard wiper, a security vulnerability auditor, and encrypted backup portability.

---

## System Architecture

```mermaid
flowchart TD
    User["User / Evaluator"] --> UI["Tkinter GUI / Headless CLI"]
    UI --> Service["VaultService (Session & CRUD Logic)"]
    Service --> Crypto["CryptoUtils (PBKDF2, Fernet, CSPRNG)"]
    Service --> DB[(SQLite Database: data/password_manager.db)]
    Service --> Clipboard["OS Clipboard Engine (with Auto-Clear Timer)"]
    Service --> Backup["Encrypted Backup File (.enc)"]
```

---

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    MASTER_AUTH {
        int id PK "Must be 1"
        string salt_hex
        string key_check_token
        timestamp created_at
    }

    VAULT_ENTRIES {
        int id PK
        string title
        string username
        string encrypted_password
        string url
        string category
        string notes
        timestamp created_at
        timestamp updated_at
    }

    SECURITY_AUDIT_LOGS {
        int id PK
        string event_type
        string details
        timestamp timestamp
    }
```

---

## Data Flow Diagram (DFD Level 1)

```mermaid
flowchart LR
    A["User Submits Master Password"] --> B["CryptoUtils: PBKDF2-HMAC-SHA256 Derivation"]
    B --> C["Derived Fernet 256-Bit Symmetric Key"]
    C --> D{"Attempt Decryption of Canary Token"}
    D -- Mismatch / Error --> E["Access Denied: Log to security_audit_logs"]
    D -- Valid --> F["Unlock Vault: Cache Key in Memory"]
    G["User Adds / Modifies Credential"] --> H["Encrypt Password using AES-128-CBC + HMAC"]
    H --> I[("Persist Ciphertext into vault_entries Table")]
    J["User Requests View / Copy"] --> K["Decrypt Ciphertext using In-Memory Key"]
    K --> L["Display Decrypted / Copy to Clipboard"]
```

---

## Sequence Diagram: Credential Encryption and Storage

```mermaid
sequenceDiagram
    autonumber
    actor Evaluator as Evaluator / User
    participant GUI as Desktop GUI / CLI
    participant Service as VaultService
    participant Crypto as CryptoUtils
    participant DB as SQLite DB

    Evaluator->>GUI: Enter Master Password & Unlock
    GUI->>Service: unlock_vault(master_password)
    Service->>DB: Query salt_hex and key_check_token
    DB-->>Service: Return auth credentials
    Service->>Crypto: derive_fernet_key(master_password, salt)
    Crypto-->>Service: Session Fernet Key
    Service->>Crypto: decrypt_data(key_check_token, key)
    Crypto-->>Service: 'VAULT_ACCESS_GRANTED_SECURITY_TOKEN'
    Service-->>GUI: Unlock Success Confirmed

    Evaluator->>GUI: Enter New Account Details (Title, User, Password)
    GUI->>Service: add_entry(title, user, password, ...)
    Service->>Crypto: encrypt_data(password, session_key)
    Crypto-->>Service: Return AES-HMAC Ciphertext
    Service->>DB: INSERT INTO vault_entries (title, user, ciphertext, ...)
    Service->>DB: INSERT INTO security_audit_logs ('ENTRY_ADDED', ...)
    Service-->>GUI: Credential Encrypted & Stored
    GUI-->>Evaluator: Confirmation Message & Refreshed Table
```

---

## Mathematical Formulation
1. **Key Derivation Function (PBKDF2)**:
   $$K = \text{PBKDF2}(\text{PRF}=\text{HMAC-SHA256}, \text{Password}=P, \text{Salt}=S, c=200000, dklen=32)$$
2. **Shannon Entropy for Password Space**:
   Given password length $L$ and alphabet pool size $R$:
   $$E = L \log_2(R) \quad \text{bits}$$
   Where:
   $$R = 26 \cdot \mathbb{I}_{\text{lower}} + 26 \cdot \mathbb{I}_{\text{upper}} + 10 \cdot \mathbb{I}_{\text{digits}} + 33 \cdot \mathbb{I}_{\text{symbols}}$$
   A 16-character password drawn from all four character pools ($R = 95$) delivers:
   $$E = 16 \log_2(95) \approx 16 \times 6.5698 = 105.12 \text{ bits of entropy}$$
   Surpassing the recommended NIST threshold for high-security credentials.

---

## Conclusion
The PY-09 Secure Password Manager provides a robust, zero-knowledge, mathematically grounded credential storage solution. By combining industry-standard authenticated encryption with proactive vulnerability audits and defensive interface mechanisms (clipboard auto-clear, masked inputs), it provides a complete, professional security application for college submission and technical evaluation.

