# Viva Examination Questions and Answers: PY-09 Secure Password Manager

### Q1: What encryption standard does your password manager use, and what is its key length?
**Answer**: The system uses Fernet symmetric encryption from Python's standard `cryptography` library. Under the hood, Fernet uses 128-bit AES in CBC mode with PKCS7 padding and 128-bit HMAC-SHA256 for integrity verification, utilizing a 256-bit (32-byte) key derived from the master password.

### Q2: Why is PBKDF2 used instead of simple SHA-256 for hashing the master password?
**Answer**: Simple SHA-256 is designed to be extremely fast for data integrity, which makes it vulnerable to GPU/ASIC-accelerated brute-force attacks (billions of hashes per second). PBKDF2 (Password-Based Key Derivation Function 2) introduces computational work stretching by running 200,000 iterations of HMAC-SHA256 with a unique 16-byte random salt, making brute-force attacks computationally infeasible.

### Q3: What is a cryptographic salt, and why is it essential?
**Answer**: A salt is a sequence of cryptographically random bytes generated via `os.urandom(16)` and combined with the password before key derivation. It ensures that even if two users choose identical master passwords, their derived keys and ciphertexts will be completely different, defeating pre-computed rainbow table attacks.

### Q4: How does the application verify the master password without storing it on disk?
**Answer**: We employ a canary verification token. During setup, the application derives the symmetric key and encrypts a constant string (`VAULT_ACCESS_GRANTED_SECURITY_TOKEN`), storing this ciphertext in the database. During login, the candidate master password derives a trial key to decrypt the canary token. If decryption succeeds and yields the exact token, authentication succeeds; otherwise, an `InvalidToken` exception is raised. The master password is never written to disk.

### Q5: What is Encrypt-then-MAC, and why is it implemented in Fernet?
**Answer**: Encrypt-then-MAC is an authenticated encryption approach where the ciphertext is computed first using AES, and then an HMAC signature is generated over the ciphertext, IV, and metadata. Upon decryption, the HMAC is verified *before* passing the ciphertext to the AES decryptor. This prevents chosen-ciphertext attacks, padding oracle attacks, and bit-flipping tampering.

### Q6: How does the password generator guarantee cryptographic randomness?
**Answer**: It uses Python's `secrets` module instead of the standard `random` module. The `secrets` module accesses the underlying operating system's Cryptographically Secure Pseudorandom Number Generator (CSPRNG, such as `/dev/urandom` on Unix or `CryptGenRandom` on Windows), ensuring non-reproducible, high-entropy output.

### Q7: Explain the mathematical calculation of password entropy.
**Answer**: Information entropy is calculated as $E = L \times \log_2(R)$, where $L$ is the password length and $R$ is the size of the character pool. For example, a 16-character password incorporating lowercase (26), uppercase (26), numbers (10), and symbols (33) has $R = 95$. Its entropy is $16 \times \log_2(95) \approx 105.12$ bits, representing $2^{105}$ possible combinations.

### Q8: Why is clipboard auto-clearing important in a password manager?
**Answer**: When a user copies a password to paste into a website, that sensitive plaintext resides in the OS system clipboard. Any background application or browser tab with clipboard read permissions could harvest that password. Our application automatically clears the clipboard after 20 seconds using a background timer to eliminate this vulnerability window.

### Q9: How does the security audit feature protect the user?
**Answer**: The security audit feature iterates through all decrypted passwords in the unlocked vault and analyzes two major security hazards:
1. Weak passwords (evaluated strength score < 50 or common dictionary patterns).
2. Reused / duplicate passwords shared across multiple accounts, which expose the user to credential-stuffing attacks if any single service is breached.

### Q10: What happens when the user locks the vault or exits the application?
**Answer**: The in-memory variable holding the active symmetric key (`self.active_key`) is immediately set to `None`, and garbage collection is triggered. The UI switches back to the login screen, ensuring that passwords cannot be decrypted by inspecting memory after the user steps away.

### Q11: How does the encrypted backup and restore feature work?
**Answer**: The entire database of credentials is serialized into JSON and encrypted with the active symmetric key. The resulting file contains the salt and ciphertext bundle. When restoring, the user's active key (or master password) decrypts the bundle and bulk-inserts the credentials into the target database.

### Q12: Why did you choose SQLite over JSON flat-files for credential storage?
**Answer**: SQLite offers ACID transactional guarantees, atomic write operations that prevent database corruption during sudden crashes or power loss, indexed querying for instant search across thousands of records, and referential constraints like unique primary keys and check constraints.

### Q13: Can someone with direct access to `data/password_manager.db` read the stored passwords?
**Answer**: No. All values in the `encrypted_password` column are stored as Fernet-encrypted ciphertexts. Without the master password to derive the specific PBKDF2 key, the ciphertext is indistinguishable from random noise.

### Q14: How does the system handle SQL injection?
**Answer**: All SQL statements throughout `VaultDB` and `VaultService` use parameterized queries with `?` placeholders (e.g., `cursor.execute("SELECT * FROM vault_entries WHERE id = ?", (entry_id,))`). User input is never concatenated into raw SQL strings.

### Q15: What is the purpose of excluding ambiguous characters from password generation?
**Answer**: Certain characters like lowercase `l`, uppercase `I`, digit `1`, uppercase `O`, and digit `0` look visually identical in many fonts. Omitting them prevents user confusion and transcription errors when manually re-typing credentials onto mobile or secondary devices.

