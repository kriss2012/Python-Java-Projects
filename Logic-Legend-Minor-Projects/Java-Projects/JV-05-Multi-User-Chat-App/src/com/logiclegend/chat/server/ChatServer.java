package com.logiclegend.chat.server;

import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * High-concurrency multi-threaded central chat server.
 */
public class ChatServer {
    public static final int DEFAULT_PORT = 8888;

    private final int port;
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> displayNames = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final List<Consumer<String>> logListeners = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    public void addLogListener(Consumer<String> listener) {
        if (listener != null) {
            logListeners.add(listener);
        }
    }

    public void log(String message) {
        System.out.println(message);
        for (Consumer<String> listener : logListeners) {
            try {
                listener.accept(message);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void start() throws IOException {
        if (running) return;
        serverSocket = new ServerSocket(port);
        running = true;
        log("[SERVER STARTED] Listening on TCP port " + port + ". Ready for client connections.");

        threadPool.execute(this::acceptConnections);
    }

    private void acceptConnections() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.execute(handler);
            } catch (IOException e) {
                if (running) {
                    log("[SERVER EXCEPTION] Accept failure: " + e.getMessage());
                }
            }
        }
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        log("[SERVER SHUTTING DOWN] Disconnecting all clients...");

        for (ClientHandler handler : clients.values()) {
            try {
                handler.sendMessage(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", "*", "Server is shutting down. Connection terminated."));
                handler.close();
            } catch (Exception ignored) {}
        }
        clients.clear();
        displayNames.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        threadPool.shutdownNow();
        log("[SERVER STOPPED] Socket released.");
    }

    public boolean registerClient(String username, ClientHandler handler) {
        String key = username.toLowerCase();
        ClientHandler existing = clients.putIfAbsent(key, handler);
        if (existing == null) {
            displayNames.put(key, username);
            return true;
        }
        return false;
    }

    public boolean unregisterClient(String username) {
        if (username == null) return false;
        String key = username.toLowerCase();
        displayNames.remove(key);
        return clients.remove(key) != null;
    }

    public void broadcast(ChatMessage message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public boolean sendPrivateMessage(String sender, String recipient, String content) {
        if (recipient == null) return false;
        String recKey = recipient.toLowerCase();
        ClientHandler recHandler = clients.get(recKey);
        ClientHandler senderHandler = clients.get(sender.toLowerCase());

        if (recHandler == null) {
            if (senderHandler != null) {
                senderHandler.sendMessage(new ChatMessage(
                        MessageType.ERROR,
                        "SERVER",
                        sender,
                        "User '" + recipient + "' is currently offline or does not exist."
                ));
            }
            return false;
        }

        String actualRecipientName = displayNames.getOrDefault(recKey, recipient);
        ChatMessage directMsg = new ChatMessage(MessageType.PRIVATE_CHAT, sender, actualRecipientName, content);

        recHandler.sendMessage(directMsg);
        if (senderHandler != null && senderHandler != recHandler) {
            // Echo back to sender so they see their own sent DM
            senderHandler.sendMessage(directMsg);
        }
        return true;
    }

    public void broadcastUserList() {
        List<String> users = getOnlineUsernames();
        String payload = String.join(",", users);
        broadcast(new ChatMessage(MessageType.USER_LIST, "SERVER", "*", payload));
    }

    public List<String> getOnlineUsernames() {
        List<String> list = new ArrayList<>(displayNames.values());
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public int getClientCount() {
        return clients.size();
    }

    public boolean kickUser(String username, String reason) {
        String key = username.toLowerCase();
        ClientHandler handler = clients.get(key);
        if (handler != null) {
            handler.sendMessage(new ChatMessage(
                    MessageType.SYSTEM_NOTICE,
                    "SERVER",
                    username,
                    "You have been disconnected by the server administrator: " + (reason != null ? reason : "No reason specified.")
            ));
            handler.close();
            broadcast(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", "*", username + " was kicked by administrator."));
            return true;
        }
        return false;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return running;
    }
}

