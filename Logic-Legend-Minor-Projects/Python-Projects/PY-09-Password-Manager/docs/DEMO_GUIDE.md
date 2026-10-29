# Evaluator Live Demonstration Guide: PY-09 Secure Password Manager

This guide provides a rapid step-by-step walkthrough for faculty evaluators, viva examiners, and project mentors to inspect and test the PY-09 application.

---

## 1. Quick Verification Checklist
| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Run Unit Tests | 8 tests pass in < 0.6s (`Ran 8 tests ... OK`) | [ ] |
| 2 | Seed Demo Data via CLI | 5 credentials saved with AES-256 encryption | [ ] |
| 3 | Run Security Audit | Identifies weak and reused duplicate passwords | [ ] |
| 4 | Export & Restore Backup | Encrypted backup created and verified | [ ] |
| 5 | Launch GUI (Optional) | Tkinter dashboard opens with full vault features | [ ] |

---

## 2. Command Line Demonstration (Fastest & 100% Deterministic)

### Step 1: Execute Automated Test Suite
```bash
python tests/test_password_manager.py
```
**Verification Note**: Look for:
```
Ran 8 tests in 0.499s
OK
```

### Step 2: Initialize Master Password, Seed Credentials & Run Security Audit
```bash
python src/main.py --setup-master "MasterSecret!2026" --seed --list --show-passwords --audit --export-backup --cli
```
**Output Highlights to Observe**:
1. `[MASTER SETUP] Master password configured successfully. Vault unlocked.`
2. 5 seeded credentials across Categories (Education, Work, Banking, Social, Email).
3. Plaintext passwords decrypted cleanly using the in-memory master key:
   - `College Student Portal`: `Col!ege#2026Pass`
   - `GitHub Developer Account`: `GhP_78vT$xM9!kL2`
   - `HDFC NetBanking`: `Hdfc@Bank#9912`
   - `LinkedIn Professional`: `Col!ege#2026Pass` (Reused password)
   - `ProtonMail Secure`: `weakpassword` (Weak password)
4. Audit Report identifies:
   - `Weak Passwords (Score < 50): 1`
   - `Reused / Duplicate Passwords: 2` (Reused in College Student Portal, LinkedIn Professional).
5. Encrypted backup generated in `exports/vault_backup.enc`.

### Step 3: Test Password Generator from CLI
```bash
python src/main.py --generate --length 24 --cli
```
**Verification Note**: Output demonstrates cryptographic randomness, character pool diversity, mathematical entropy (e.g. > 150 bits), and a `VERY STRONG (Score: 90/100)` grade.

---

## 3. Graphical Interface Demonstration (Tkinter GUI)

### Step 1: Launch Application
```bash
python src/main.py
```

### Step 2: Test Authentication & Dashboard
1. **Unlock Screen**: Enter master password (`MasterSecret!2026`) and click **Unlock Vault**.
2. **Browse Credentials**:
   - Click items in the Treeview (e.g., *GitHub Developer Account*).
   - Observe account details populate in the right pane with masked password (`●●●●●●●●`).
3. **Show / Hide Password**: Click the **👁 Show** toggle to reveal the plaintext password; click **🙈 Hide** to re-mask.
4. **Copy Password**: Click **📋 Copy**. Observe the blue notification banner:
   `📋 Password copied! Clipboard will auto-clear in 20s.`
5. **Interactive Password Generator**:
   - Drag the Length slider to 20.
   - Click **⚡ Generate & Apply to Field**.
   - Watch the password field populate and the real-time strength meter indicate `VERY STRONG` in green.
6. **Security Audit**: Click the **🛡️ Security Audit** button in the top toolbar to view the vulnerability modal highlighting weak and duplicated credentials.
7. **Lock Vault**: Click **🔒 Lock Vault** to test instant key purging and lock screen return.

---

## 4. Evaluator Viva Questions to Ask the Student
1. *"What happens if an attacker steals the SQLite database file?"*
   - Student should explain that the `encrypted_password` column contains AES-128-CBC + HMAC-SHA256 ciphertexts, and the master password is never stored anywhere in the database.
2. *"Why is PBKDF2 with 200,000 rounds used instead of a standard MD5 or SHA-256 hash?"*
   - Student should explain computational work stretching, salt randomization, and mitigation of GPU/ASIC brute-force and rainbow table attacks.
3. *"How does the clipboard auto-clearing protect the user?"*
   - Student should explain background timers wiping the clipboard buffer to prevent other applications from sniffing exposed plaintext credentials.

