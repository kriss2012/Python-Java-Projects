# Viva Voce Examination Questions & Answers — Multi-User Chat Application (JV-05)

---

### Q1: Why did you choose TCP over UDP for this multi-user chat application?
**Answer**: TCP (Transmission Control Protocol) is a connection-oriented, reliable byte-stream protocol. In a chat application, guaranteeing in-order, lossless message delivery is vital: messages must not be dropped, duplicated, or received out of order. UDP is connectionless and does not guarantee packet delivery or sequencing, which would lead to missing messages, broken user join/leave sequences, or corrupted conversation threads.

---

### Q2: How does the server handle multiple simultaneous client connections?
**Answer**: The server creates a `ServerSocket` bound to port 8888. A background thread runs an `accept()` loop. Each incoming `Socket` connection is delegated to an instance of `ClientHandler` (which implements `Runnable`) and dispatched to an `ExecutorService` cached thread pool (`Executors.newCachedThreadPool()`). Each client runs on its own independent worker thread, allowing asynchronous reading and writing without blocking the main listener or other clients.

---

### Q3: What happens when a user types delimiter characters like pipe `|` or newlines into their message?
**Answer**: To eliminate delimiter collisions and protocol injection attacks, our custom wire protocol encodes the entire message content using RFC 4648 Base64:
$$\text{TYPE} \mid \text{TIMESTAMP} \mid \text{SENDER} \mid \text{RECIPIENT} \mid \text{BASE64}(\text{CONTENT})$$
Because Base64 output only contains characters `[A-Za-z0-9+/=]`, pipes, newlines, commas, and emojis can never disrupt the 5-field pipe-delimited framing.

---

### Q4: How is thread safety ensured when multiple clients join, message, and leave concurrently?
**Answer**:
1. Active clients are stored in a `ConcurrentHashMap<String, ClientHandler>`, which allows lock-free concurrent reads and atomic modifications (`putIfAbsent()`, `remove()`).
2. Event listeners for UI updates use `CopyOnWriteArrayList` to prevent `ConcurrentModificationException` during broadcast iterations.
3. Outbound message writes in `ClientHandler.sendMessage()` are synchronized on the handler to ensure that concurrent public broadcasts and private messages do not interleave byte buffers on the client's `PrintWriter`.

---

### Q5: How does the server prevent two users from claiming the same username?
**Answer**: During the initial connection handshake, the server evaluates `clients.putIfAbsent(username.toLowerCase(), handler)`. If an existing entry already exists for that lowercased key, the method returns the existing handler, signalling a collision. The server immediately returns an `ERROR` message ("Username already taken") and closes the duplicate client socket, preserving identity uniqueness.

---

### Q6: How does the server ensure that private messages are truly confidential?
**Answer**: When a client sends a message with type `PRIVATE_CHAT` specifying a recipient:
```java
server.sendPrivateMessage(sender, recipient, content);
```
The server retrieves only the specific `ClientHandler` associated with the recipient from the internal map and invokes `sendMessage()` solely on that handler (plus echoing a confirmation to the sender). The message is never pushed to the broadcast loop, ensuring that third-party clients connected to the server have zero visibility of the private message payload.

---

### Q7: What was the deadlock issue encountered when closing client sockets on Windows, and how was it solved?
**Answer**: On Windows, calling `in.close()` on a `BufferedReader` while another thread is blocked in `readLine()` causes `in.close()` to block waiting for `BufferedReader`'s internal monitor lock. If `socket.close()` is placed after `in.close()`, the code deadlocks because `readLine()` never releases the lock. The solution is to close the native socket first (`socket.close()`). This immediately interrupts the underlying OS TCP socket, causing `readLine()` to throw a `SocketException`, release its monitor lock, and allow the reader thread to exit cleanly.

---

### Q8: How does the server handle an abrupt client crash (e.g., killed process or pulled network cable)?
**Answer**: When an active socket connection is abruptly broken, `in.readLine()` throws an `IOException` (Connection reset). The `ClientHandler` catches this exception in its `try-catch` block, marks `running = false`, and invokes `close()` in its `finally` clause. `close()` removes the client from `ConcurrentHashMap`, decrements the active count, and broadcasts a `DISCONNECT` event and updated `USER_LIST` to all remaining peers.

---

### Q9: What is the time complexity of broadcasting a public message to $N$ connected clients?
**Answer**: Iterating through the values of `ConcurrentHashMap` takes $O(N)$ time where $N$ is the number of active clients. Writing a line to each client's non-blocking socket buffer is $O(1)$. Thus, public broadcasting is an optimal $O(N)$ operation.

---

### Q10: What is the time complexity of routing a private message?
**Answer**: Looking up the recipient's handler in `ConcurrentHashMap` via their lowercased username hash takes $O(1)$ expected time. Delivering the message to the recipient's output stream is $O(1)$. Thus, private message routing runs in optimal $O(1)$ constant time regardless of the total number of connected clients.

---

### Q11: How does the online user roster remain synchronized on all client GUIs?
**Answer**: Whenever a client successfully joins or departs (or is kicked), the server triggers `broadcastUserList()`. It extracts all registered usernames from `displayNames`, joins them into a comma-delimited string, and transmits a `USER_LIST` message to all clients. Client GUIs intercept this message and invoke `SwingUtilities.invokeLater()` to update the `JList` and direct message target dropdowns.

---

### Q12: Why did you provide both GUI and CLI modes?
**Answer**: In real-world enterprise architectures and automated evaluation pipelines (such as CI/CD environments or headless Linux servers), graphical displays (X11/Wayland) may be unavailable. Dual modes ensure that the application can run in high-concurrency headless terminal mode (`--server-cli`, `--client-cli`) while also providing an intuitive, polished Swing GUI for desktop users and viva demonstrations.

---

### Q13: How does the Administrator Kick feature work?
**Answer**: When the administrator selects a user in `ServerDashboardFrame` and confirms a kick:
1. The server fetches the target's `ClientHandler`.
2. Sends a high-priority `SYSTEM_NOTICE` alerting the user that they were disconnected by the administrator.
3. Closes the client's socket connection, triggering automated removal and user list broadcast.
4. Broadcasts a public announcement informing the chat room that the user was kicked.

---

### Q14: How does the client GUI prevent freezing during network operations?
**Answer**: Network reading occurs in a dedicated background daemon thread (`ChatClient-Listener`). When a message arrives, the listener dispatches UI update tasks to Swing's Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater()`. Outbound network writes are lightweight non-blocking socket flushes, keeping the GUI responsive at 60 FPS.

---

### Q15: How could end-to-end encryption (E2EE) be added to this architecture?
**Answer**: Clients could exchange Diffie-Hellman public keys upon establishing a private chat session. Messages would then be encrypted locally using AES-GCM before serialization into the `content` field. The central server would only see and route ciphertext payloads, ensuring zero-knowledge privacy where only the intended recipient holding the shared secret key can decrypt the message.

