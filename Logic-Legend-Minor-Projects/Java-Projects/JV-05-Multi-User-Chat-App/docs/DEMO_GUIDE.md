# Evaluator Demonstration Guide — Multi-User Chat Application (JV-05)

A step-by-step walkthrough for evaluators and examiners to test all networking and functional capabilities of the **Multi-User Chat Application**.

---

## 1. Quick Launch Verification
1. Open PowerShell / Command Prompt.
2. Navigate to project root:
   ```powershell
   cd "Logic-Legend-Minor-Projects\Java-Projects\JV-05-Multi-User-Chat-App"
   ```
3. Run the automated test suite to confirm zero defects:
   ```powershell
   java -cp "bin" tests.TestRunner
   ```
   **Expected Outcome**: 29 / 29 assertions PASS in under 5 seconds.

---

## 2. Live Desktop Demonstration (Multi-Window Setup)

### Step 1: Start the Central Chat Server
Open Terminal 1:
```powershell
java -cp "bin" com.logiclegend.chat.Main --server 8888
```
- A modern dark-themed **Server Dashboard Frame** will appear.
- Note the top status bar: `CHAT SERVER CONTROLLER (PORT 8888) | Active Clients: 0`.
- The server logs pane will display: `[SERVER STARTED] Listening on TCP port 8888`.

### Step 2: Connect First Client (Alice)
Open Terminal 2:
```powershell
java -cp "bin" com.logiclegend.chat.Main --client 127.0.0.1 8888 Alice
```
- A client chat window will launch with title `LogicLegend Chat — Alice (127.0.0.1:8888)`.
- The Online Users panel on the right will display `Alice`.
- On the Server Dashboard, watch the `Active Clients` label increment to `1`, and see `Alice` appear in the connected users table.

### Step 3: Connect Second Client (Bob)
Open Terminal 3:
```powershell
java -cp "bin" com.logiclegend.chat.Main --client 127.0.0.1 8888 Bob
```
- Bob's chat window appears.
- Notice that in both Alice's and Bob's windows, the Online Users roster updates automatically to show `Alice` and `Bob`.
- A green system notice announces: `[HH:mm:ss] --> Bob joined the chat room.`

### Step 4: Test Public Broadcast
1. In Alice's window, type: `Hello Bob, welcome to the test room!` and press **Enter** or click **Send**.
2. Immediately observe that Bob's chat window displays:
   `[HH:mm:ss] Alice: Hello Bob, welcome to the test room!`
3. In Bob's window, reply: `Thanks Alice! Real-time sockets are working!`
4. Verify Alice's window receives Bob's reply instantaneously.

### Step 5: Test Private Direct Messaging (`/msg`)
1. Connect a 3rd client (Charlie) in Terminal 4:
   ```powershell
   java -cp "bin" com.logiclegend.chat.Main --client 127.0.0.1 8888 Charlie
   ```
2. Verify all 3 clients now show `Alice`, `Bob`, `Charlie` in their online lists.
3. In Alice's window, select **Bob** from the "Send To" dropdown (or type `/msg Bob Project secret passcode is 9876`).
4. Click **Send**.
5. Observe:
   - Alice sees: `[HH:mm:ss] [DM Alice -> Bob]: Project secret passcode is 9876`
   - Bob sees: `[HH:mm:ss] [DM Alice -> Bob]: Project secret passcode is 9876`
   - **Charlie's window sees NOTHING** — confirming complete private message confidentiality!

### Step 6: Test Server Administrator Features
1. Switch to the **Server Dashboard Window**.
2. Click **"Broadcast Announcement"**. Enter: `Server scheduled maintenance in 15 minutes.`
3. Verify that all 3 clients (Alice, Bob, Charlie) receive:
   `[HH:mm:ss] [SYSTEM]: [ADMIN ANNOUNCEMENT] Server scheduled maintenance in 15 minutes.`
4. In the connected users table, select **Charlie**.
5. Click **"Kick Selected User"** and confirm.
6. Observe:
   - Charlie's window receives a disconnection alert and disables input controls.
   - Alice and Bob receive a system notice: `Charlie was kicked by administrator.`
   - Charlie disappears from online rosters in Alice's and Bob's windows.
   - The Server Dashboard active client count decrements to 2.

### Step 7: Test Duplicate Username Rejection
In Terminal 4, try launching a client with Alice's username:
```powershell
java -cp "bin" com.logiclegend.chat.Main --client-cli 127.0.0.1 8888 alice
```
- Observe error message in terminal:
  `[ERROR] Username 'alice' is already taken. Please reconnect with another name.`
- The duplicate client is terminated immediately, preserving the original Alice's identity.

---

## 3. Terminal CLI Mode Demonstration

To run in completely headless environments:
- Server: `java -cp "bin" com.logiclegend.chat.Main --server-cli`
  - Enter `/users` to inspect active users.
  - Enter `/broadcast <text>` to broadcast announcements.
  - Enter `/kick <username>` to remove clients.
  - Enter `/stop` to shut down the server.
- Client: `java -cp "bin" com.logiclegend.chat.Main --client-cli 127.0.0.1 8888 Student1`

