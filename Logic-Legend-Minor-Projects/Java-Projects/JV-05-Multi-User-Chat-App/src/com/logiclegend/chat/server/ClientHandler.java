package com.logiclegend.chat.server;

import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Worker thread for handling client socket communication.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            // Handshake: Expect CONNECT message
            String rawHandshake = in.readLine();
            if (rawHandshake == null) {
                close();
                return;
            }

            ChatMessage handshake = ChatMessage.deserialize(rawHandshake);
            if (handshake.getType() != MessageType.CONNECT) {
                sendMessage(new ChatMessage(MessageType.ERROR, "SERVER", "*", "Handshake failed. Expected CONNECT."));
                close();
                return;
            }

            String requestedName = handshake.getSender().trim();
            if (!isValidUsername(requestedName)) {
                sendMessage(new ChatMessage(MessageType.ERROR, "SERVER", "*", "Invalid username. Must be 2-20 alphanumeric characters or underscores."));
                close();
                return;
            }

            boolean registered = server.registerClient(requestedName, this);
            if (!registered) {
                sendMessage(new ChatMessage(MessageType.ERROR, "SERVER", "*", "Username '" + requestedName + "' is already taken. Please reconnect with another name."));
                close();
                return;
            }

            this.username = requestedName;
            server.log("[CONNECTED] Client registered: " + username + " (" + socket.getRemoteSocketAddress() + ")");

            // Send welcome notice and broadcast join
            sendMessage(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", username, "Welcome to LogicLegend Chat Room, " + username + "! Type /help for available commands."));
            server.broadcast(new ChatMessage(MessageType.CONNECT, username, "*", username + " has entered the chat."));
            server.broadcastUserList();

            // Main message processing loop
            String rawMessage;
            while (running && (rawMessage = in.readLine()) != null) {
                ChatMessage msg = ChatMessage.deserialize(rawMessage);
                if (msg == null) continue;

                switch (msg.getType()) {
                    case PUBLIC_CHAT:
                        server.broadcast(new ChatMessage(MessageType.PUBLIC_CHAT, username, "*", msg.getContent()));
                        break;

                    case PRIVATE_CHAT:
                        server.sendPrivateMessage(username, msg.getRecipient(), msg.getContent());
                        break;

                    case DISCONNECT:
                        running = false;
                        break;

                    case USER_LIST:
                        sendMessage(new ChatMessage(MessageType.USER_LIST, "SERVER", username, String.join(",", server.getOnlineUsernames())));
                        break;

                    case FILE_TRANSFER:
                        server.broadcast(new ChatMessage(MessageType.FILE_TRANSFER, username, msg.getRecipient(), msg.getContent()));
                        break;

                    case PING:
                        sendMessage(new ChatMessage(MessageType.PING, "SERVER", username, "PONG"));
                        break;

                    default:
                        server.log("[WARN] Unhandled message type from " + username + ": " + msg.getType());
                }
            }
        } catch (IOException e) {
            if (running) {
                server.log("[DISCONNECT] Connection dropped for user: " + (username != null ? username : "unauthenticated client"));
            }
        } finally {
            close();
        }
    }

    public synchronized void sendMessage(ChatMessage message) {
        if (out != null && message != null) {
            out.println(message.serialize());
        }
    }

    public void close() {
        running = false;
        if (username != null) {
            boolean removed = server.unregisterClient(username);
            if (removed) {
                server.log("[DISCONNECT] User departed: " + username);
                server.broadcast(new ChatMessage(MessageType.DISCONNECT, username, "*", username + " has left the chat."));
                server.broadcastUserList();
            }
            username = null;
        }

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}

        if (out != null) out.close();
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    private static boolean isValidUsername(String name) {
        return name != null && name.matches("^[a-zA-Z0-9_]{2,20}$");
    }
}
