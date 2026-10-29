# Presentation Slides — Multi-User Chat Application (JV-05)

---

### Slide 1: Title Slide
- **Project Title**: Multi-User Chat Application
- **Project Code**: `JV-05`
- **Domain**: Computer Networks & Concurrent Distributed Systems
- **Technology Stack**: Java 21 LTS, Java TCP Sockets, Multithreading, Non-blocking Collections, Swing GUI
- **Presenter**: Engineering Candidate

---

### Slide 2: Problem Statement & Objectives
- **Communication Inefficiencies**:
  - Legacy socket chats often crash when clients disconnect abruptly.
  - Naive text protocols fail when users type delimiter characters (`|`, `,`, newlines, emojis).
  - Lack of direct messaging privacy in simplistic broadcast-only chat rooms.
  - Difficulty in managing concurrent thread lifecycles and race conditions.
- **Objectives**: Engineer a resilient, thread-safe, multi-user socket chat system with public/private routing and an administrator console.

---

### Slide 3: Network Architecture & Topologies
- **Client-Server Star Topology**:
  - Central `ChatServer` listening on configurable TCP port (default 8888).
  - Dedicated worker thread (`ClientHandler`) per connected client.
  - `ConcurrentHashMap` for $O(1)$ client lookups and non-blocking reads.
- **Message Dispatching**:
  - Broadcast engine for global room chat.
  - Point-to-point router for private 1-to-1 direct messaging (`/msg`).

---

### Slide 4: Custom Wire Protocol Design
- **Framing Specification**:
  `TYPE|TIMESTAMP|SENDER|RECIPIENT|BASE64(PAYLOAD)`
- **Key Advantages**:
  - **Collision-Free**: Base64 encoding guarantees that newlines, commas, JSON strings, or pipes inside user messages never break message boundaries.
  - **Inspectable & Lightweight**: Easy to trace in server console without bulky XML/binary parsers.

---

### Slide 5: Thread Lifecycle & Concurrency Engineering
- **Cached Thread Pool**: `Executors.newCachedThreadPool()` adapts elastically to incoming connections.
- **Graceful Teardown Pattern**:
  - Closing `socket` before `in.close()` to unblock reader threads without thread lock contention on Windows.
  - Synchronized output streams (`PrintWriter`) preventing interleaved packet corruption.

---

### Slide 6: Key Functional Features
- **Unique Username Enforcement**: Case-insensitive check prevents identity spoofing.
- **Dynamic Presence Directory**: Automatically pushes updated `USER_LIST` upon every join or departure.
- **Private Direct Messaging**: Recipient isolation ensures non-participants cannot read private messages.
- **Admin Dashboard**: Live client counter, server system logs, global broadcast alerts, and user kick actions.

---

### Slide 7: Graphical & Terminal Interfaces
- **Swing Client (`ChatClientFrame`)**:
  - Live conversation panel with auto-scrolling and timestamps.
  - Online user roster with double-click DM target selection.
  - Dropdown to toggle between Public Room and specific online users.
- **Server Dashboard (`ServerDashboardFrame`)**:
  - Real-time connection metrics, activity logs, and administrative controls.
- **Headless CLI**: Full terminal support (`--server-cli`, `--client-cli`) for headless Linux/servers.

---

### Slide 8: Automated Verification & Testing
- **Test Harness (`tests.TestRunner`)**: 29 unit and integration assertions across 8 suites:
  - Multi-client handshakes and duplicate name rejection.
  - Public broadcast distribution.
  - Private message routing and strict privacy isolation.
  - Active user list synchronization.
  - Abrupt disconnect and graceful departure cleanup.
  - Administrator kick mechanism.
  - Base64 payload integrity under complex inputs.
- **Result**: 29 / 29 assertions passed (100% success).

---

### Slide 9: Technical Highlights & Edge Cases Solved
- **Solved ReadLine Deadlock**: Overcame Windows JVM blocking on `BufferedReader.close()` by prioritizing native socket closure.
- **Case-Insensitive Uniqueness**: `alice` cannot impersonate `Alice`.
- **Zero Third-Party Dependencies**: Runs directly on standard JDK with zero external JAR requirements.

---

### Slide 10: Conclusion & Future Extensions
- **Conclusion**: JV-05 provides a robust, production-grade distributed chat system meeting rigorous academic and networking standards.
- **Future Roadmap**:
  - TLS / SSL socket encryption (`SSLSocketFactory`).
  - Binary chunked file transfer over dedicated data streams.
  - Persistent SQLite chat history archival.

