package com.logiclegend.chat.client;

import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Socket client managing real-time bidirectional messaging with the ChatServer.
 */
public class ChatClient {
    private final String host;
    private final int port;
    private final String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean connected = false;

    private final List<Consumer<ChatMessage>> messageListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> errorListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> disconnectListeners = new CopyOnWriteArrayList<>();

    public ChatClient(String host, int port, String username) {
        this.host = host != null ? host : "127.0.0.1";
        this.port = port > 0 ? port : 8888;
        this.username = username != null ? username.trim() : "Guest";
    }

    public void addMessageListener(Consumer<ChatMessage> listener) {
        if (listener != null) messageListeners.add(listener);
    }

    public void addErrorListener(Consumer<String> listener) {
        if (listener != null) errorListeners.add(listener);
    }

    public void addDisconnectListener(Runnable listener) {
        if (listener != null) disconnectListeners.add(listener);
    }

    public synchronized void connect() throws IOException {
        if (connected) return;

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        // Send handshake
        ChatMessage handshake = new ChatMessage(MessageType.CONNECT, username, "*", "JOIN_REQUEST");
        out.println(handshake.serialize());

        connected = true;

        listenerThread = new Thread(this::listenLoop, "ChatClient-Listener-" + username);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenLoop() {
        try {
            String raw;
            while (connected && (raw = in.readLine()) != null) {
                ChatMessage msg = ChatMessage.deserialize(raw);
                if (msg != null) {
                    for (Consumer<ChatMessage> listener : messageListeners) {
                        try {
                            listener.accept(msg);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IOException e) {
            if (connected) {
                for (Consumer<String> err : errorListeners) {
                    err.accept("Connection lost to server: " + e.getMessage());
                }
            }
        } finally {
            disconnect();
        }
    }

    public synchronized void sendPublicMessage(String content) {
        if (!connected || content == null || content.trim().isEmpty()) return;
        ChatMessage msg = new ChatMessage(MessageType.PUBLIC_CHAT, username, "*", content.trim());
        out.println(msg.serialize());
    }

    public synchronized void sendPrivateMessage(String recipient, String content) {
        if (!connected || recipient == null || content == null || content.trim().isEmpty()) return;
        ChatMessage msg = new ChatMessage(MessageType.PRIVATE_CHAT, username, recipient.trim(), content.trim());
        out.println(msg.serialize());
    }

    public synchronized void requestUserList() {
        if (!connected) return;
        ChatMessage msg = new ChatMessage(MessageType.USER_LIST, username, "*", "");
        out.println(msg.serialize());
    }

    public synchronized void disconnect() {
        if (!connected) return;
        connected = false;

        try {
            if (out != null) {
                ChatMessage leave = new ChatMessage(MessageType.DISCONNECT, username, "*", "CLIENT_DEPARTURE");
                out.println(leave.serialize());
                out.flush();
            }
        } catch (Exception ignored) {}

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        if (out != null) out.close();

        for (Runnable r : disconnectListeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public boolean isConnected() { return connected; }
    public String getUsername() { return username; }
    public String getHost() { return host; }
    public int getPort() { return port; }
}
