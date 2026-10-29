# Logic Legend — JV-05: Multi-User Chat Application

A high-concurrency client-server chat application engineered using Java 21, Java TCP Sockets, multithreading, non-blocking collections, and Java Swing. The application provides public chat rooms, private direct messaging (`/msg <recipient> <text>`), synchronized active user directories, graceful disconnection handlers, duplicate username rejection, an administrative server dashboard with live client metrics, and interactive terminal console modes.

---

## 1. Project Title and Identifier
- **Project Name**: Multi-User Chat Application
- **Project Code**: `JV-05`
- **Application Category**: Distributed Systems, Network Programming & Real-Time Communication
- **Target Audience**: Computer networks students, software engineering evaluators, distributed systems educators, and network administrators.

---

## 2. Executive Summary
Real-time distributed messaging requires thread-safe socket lifecycle handling, deterministic message framing, robust exception recovery upon sudden client drops, and strict isolation between public broadcasts and private conversations. The **Multi-User Chat Application** implements a concurrent client-server architecture where each client socket is handled by a dedicated worker thread communicating over a structured text protocol with Base64 payload encoding. The server manages connections using thread-safe non-blocking maps (`ConcurrentHashMap`), synchronizes live presence across all participants, and provides an administrative management console.

---

## 3. Key Features
- **Concurrent Multi-Client Server**:
  - Handles dozens of simultaneous client socket connections via `ExecutorService` cached thread pool.
  - Enforces case-insensitive unique display names across active sessions.
  - Gracefully recovers from abrupt client drops without crashing or locking server threads.
- **Public & Private Chat Channels**:
  - **Public Room (`*`)**: Broadcasts messages to all connected participants.
  - **Private Messaging (`/msg <user> <message>`)**: Delivers confidential direct messages exclusively to the intended recipient and echoes a confirmation to the sender.
- **Real-Time Online User Directory**:
  - Automatically broadcasts synchronized participant lists upon every join or departure.
  - Client GUI features an interactive online user roster with one-click direct message target selection.
- **Base64 Payload Framing**:
  - Wire protocol uses `TYPE|TIMESTAMP|SENDER|RECIPIENT|BASE64(CONTENT)`.
  - Guarantees that raw user inputs (newlines, emojis, pipes, JSON) never break transmission framing.
- **Server Administrator Dashboard**:
  - Live client connection counter and interactive table of connected usernames.
  - Real-time communication and connection logging stream.
  - System-wide administrative announcements.
  - One-click user kick functionality with custom reason alerts.
- **Dual Interface Modes**:
  - Full graphical Swing interfaces for both Server and Client (`ChatClientFrame`, `ServerDashboardFrame`).
  - Terminal interactive CLI modes for headless servers and automation scripts (`--server-cli`, `--client-cli`).

---

## 4. System Architecture
The application employs a star-topology client-server architecture:
1. **Network Wire Protocol Layer (`model/ChatMessage.java`, `model/MessageType.java`)**: Delimited message framing with Base64 payload encoding.
2. **Server Runtime Layer (`server/ChatServer.java`, `server/ClientHandler.java`)**: Manages `ServerSocket` listeners, thread pool execution, client registry (`ConcurrentHashMap`), and routing.
3. **Client Networking Layer (`client/ChatClient.java`)**: Background socket reader thread with event listeners for incoming broadcasts, private messages, and user lists.
4. **Presentation Layer (`ui/`, `Main.java`)**: Dual Swing GUI and terminal interactive CLI consoles.

---

## 5. Technology Stack and Prerequisites
- **Language**: Java 17+ / Java 21 LTS
- **Networking**: Java TCP Sockets (`java.net.ServerSocket`, `java.net.Socket`)
- **Concurrency**: `java.util.concurrent` (`ExecutorService`, `ConcurrentHashMap`, `CopyOnWriteArrayList`)
- **GUI Framework**: Java Swing & AWT (Standard JDK SE)
- **Wire Encoding**: UTF-8 Base64 (`java.util.Base64`)
- **Build Tool**: Apache Maven (`pom.xml`) or standard JDK compilation

---

## 6. Directory Layout
```
JV-05-Multi-User-Chat-App/
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── docs/
│   ├── ACADEMIC_REPORT.md
│   ├── PRESENTATION_SLIDES.md
│   ├── VIVA_QUESTIONS.md
│   └── DEMO_GUIDE.md
├── src/
│   └── com/logiclegend/chat/
│       ├── Main.java             # Unified server/client CLI launcher
│       ├── model/
│       │   ├── MessageType.java  # Protocol message enumerations
│       │   └── ChatMessage.java  # Message entity and serialization
│       ├── server/
│       │   ├── ChatServer.java   # Central socket listener and router
│       │   └── ClientHandler.java# Per-client worker thread
│       ├── client/
│       │   └── ChatClient.java   # Client socket lifecycle and reader
│       └── ui/
│           ├── ChatClientFrame.java     # Swing client UI
│           └── ServerDashboardFrame.java# Swing server admin console
└── tests/
    └── TestRunner.java           # Comprehensive 29-assertion test suite
```

---

## 7. Compilation and Setup Guide

### Direct Compilation with Standard JDK
```powershell
cd "Logic-Legend-Minor-Projects\Java-Projects\JV-05-Multi-User-Chat-App"
javac -encoding UTF-8 -cp "src" -d bin src\com\logiclegend\chat\model\*.java src\com\logiclegend\chat\server\*.java src\com\logiclegend\chat\client\*.java src\com\logiclegend\chat\ui\*.java src\com\logiclegend\chat\Main.java tests\TestRunner.java
```

---

## 8. Running the Application

### 1. Launch Server (GUI Mode)
```powershell
java -cp "bin" com.logiclegend.chat.Main --server 8888
```

### 2. Launch Client (GUI Mode)
```powershell
# Open separate terminals for each client:
java -cp "bin" com.logiclegend.chat.Main --client 127.0.0.1 8888 Alice
java -cp "bin" com.logiclegend.chat.Main --client 127.0.0.1 8888 Bob
```

### 3. Headless / Terminal CLI Mode
```powershell
# Run server in terminal:
java -cp "bin" com.logiclegend.chat.Main --server-cli

# Run client in terminal:
java -cp "bin" com.logiclegend.chat.Main --client-cli 127.0.0.1 8888 Charlie
```

---

## 9. Automated Testing
Run the automated test suite executing 29 comprehensive socket assertions:
```powershell
java -cp "bin" tests.TestRunner
```
Sample test output:
```
=================================================================
  LOGIC LEGEND JV-05: AUTOMATED TEST SUITE EXECUTION
=================================================================
[SERVER STARTED] Listening on TCP port 9123. Ready for client connections.
  [PASS] ChatServer started and isRunning() is true
  [PASS] ChatServer bound to port 9123
--- Test Suite 1: Handshake and Client Registration ---
  [PASS] Alice is connected
  [PASS] Server tracks 1 client
  [PASS] Online users contains Alice
  [PASS] Alice received welcome or system notice
...
=================================================================
  TOTAL ASSERTIONS: 29 | PASSED: 29 | FAILED: 0
=================================================================
```

---

## 10. Protocol Wire Specification
Messages are exchanged over TCP streams as single UTF-8 lines formatted as:
$$\text{MESSAGE} = \text{TYPE} \parallel \text{"|"} \parallel \text{TIMESTAMP} \parallel \text{"|"} \parallel \text{SENDER} \parallel \text{"|"} \parallel \text{RECIPIENT} \parallel \text{"|"} \parallel \text{BASE64(CONTENT)}$$

---

## 11. License
Educational Project Suite — Logic Legend Minor Projects. Open source under Apache 2.0 license.

