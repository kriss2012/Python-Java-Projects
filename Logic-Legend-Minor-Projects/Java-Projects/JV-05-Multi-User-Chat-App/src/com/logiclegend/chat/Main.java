package com.logiclegend.chat;

import com.logiclegend.chat.client.ChatClient;
import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;
import com.logiclegend.chat.server.ChatServer;
import com.logiclegend.chat.ui.ChatClientFrame;
import com.logiclegend.chat.ui.ServerDashboardFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

/**
 * Unified entry point for JV-05 Multi-User Chat Application.
 * Supports:
 *  - java com.logiclegend.chat.Main --server [port]
 *  - java com.logiclegend.chat.Main --client [host] [port] [username]
 *  - Interactive selector menu in CLI / GUI
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            if (mode.equals("--server") || mode.equals("-s")) {
                int port = args.length > 1 ? parsePort(args[1], ChatServer.DEFAULT_PORT) : ChatServer.DEFAULT_PORT;
                startServer(port, false);
                return;
            } else if (mode.equals("--server-cli")) {
                int port = args.length > 1 ? parsePort(args[1], ChatServer.DEFAULT_PORT) : ChatServer.DEFAULT_PORT;
                startServer(port, true);
                return;
            } else if (mode.equals("--client") || mode.equals("-c")) {
                String host = args.length > 1 ? args[1] : "127.0.0.1";
                int port = args.length > 2 ? parsePort(args[2], ChatServer.DEFAULT_PORT) : ChatServer.DEFAULT_PORT;
                String user = args.length > 3 ? args[3] : "User" + (100 + (int) (Math.random() * 900));
                startClient(host, port, user, false);
                return;
            } else if (mode.equals("--client-cli")) {
                String host = args.length > 1 ? args[1] : "127.0.0.1";
                int port = args.length > 2 ? parsePort(args[2], ChatServer.DEFAULT_PORT) : ChatServer.DEFAULT_PORT;
                String user = args.length > 3 ? args[3] : "User" + (100 + (int) (Math.random() * 900));
                startClient(host, port, user, true);
                return;
            }
        }

        // Interactive mode selector
        runInteractiveSelector();
    }

    private static void runInteractiveSelector() {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-05: MULTI-USER CHAT APPLICATION");
        System.out.println("=================================================================");
        System.out.println("  1. Start Chat Server (GUI Dashboard + Socket Listener)");
        System.out.println("  2. Start Chat Server (Headless / CLI Terminal)");
        System.out.println("  3. Launch Chat Client (Swing GUI Window)");
        System.out.println("  4. Launch Chat Client (Terminal Interactive Chat)");
        System.out.println("  0. Exit");
        System.out.print("Select an option (0-4): ");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

        switch (choice) {
            case "1":
                startServer(ChatServer.DEFAULT_PORT, false);
                break;
            case "2":
                startServer(ChatServer.DEFAULT_PORT, true);
                break;
            case "3": {
                System.out.print("Enter Server Host [127.0.0.1]: ");
                String host = scanner.nextLine().trim();
                if (host.isEmpty()) host = "127.0.0.1";
                System.out.print("Enter Server Port [" + ChatServer.DEFAULT_PORT + "]: ");
                int port = parsePort(scanner.nextLine().trim(), ChatServer.DEFAULT_PORT);
                System.out.print("Choose Username (alphanumeric): ");
                String user = scanner.nextLine().trim();
                if (user.isEmpty()) user = "Student" + (100 + (int) (Math.random() * 900));
                startClient(host, port, user, false);
                break;
            }
            case "4": {
                System.out.print("Enter Server Host [127.0.0.1]: ");
                String host = scanner.nextLine().trim();
                if (host.isEmpty()) host = "127.0.0.1";
                System.out.print("Enter Server Port [" + ChatServer.DEFAULT_PORT + "]: ");
                int port = parsePort(scanner.nextLine().trim(), ChatServer.DEFAULT_PORT);
                System.out.print("Choose Username: ");
                String user = scanner.nextLine().trim();
                if (user.isEmpty()) user = "TerminalUser" + (100 + (int) (Math.random() * 900));
                startClient(host, port, user, true);
                break;
            }
            case "0":
            default:
                System.out.println("[INFO] Exiting chat system. Goodbye!");
        }
    }

    public static void startServer(int port, boolean forceCli) {
        ChatServer server = new ChatServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to start Chat Server on port " + port + ": " + e.getMessage());
            return;
        }

        if (!forceCli) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    ServerDashboardFrame frame = new ServerDashboardFrame(server);
                    frame.setVisible(true);
                });
                return;
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable. Running Server in terminal CLI mode.");
            }
        }

        // Terminal CLI administration loop
        Scanner scanner = new Scanner(System.in);
        System.out.println("[SERVER READY] Commands: /users, /kick <user>, /broadcast <msg>, /stop");
        while (server.isRunning() && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("/stop") || line.equalsIgnoreCase("/exit")) {
                server.stop();
                break;
            } else if (line.equalsIgnoreCase("/users")) {
                System.out.println("[ONLINE USERS] " + server.getOnlineUsernames());
            } else if (line.startsWith("/kick ")) {
                String target = line.substring(6).trim();
                boolean kicked = server.kickUser(target, "Kicked by console administrator.");
                System.out.println(kicked ? "[SUCCESS] Kicked " + target : "[ERROR] User not found.");
            } else if (line.startsWith("/broadcast ")) {
                String msg = line.substring(11).trim();
                server.broadcast(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", "*", "[ANNOUNCEMENT] " + msg));
            } else if (!line.isEmpty()) {
                server.broadcast(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", "*", "[ANNOUNCEMENT] " + line));
            }
        }
    }

    public static void startClient(String host, int port, String username, boolean forceCli) {
        ChatClient client = new ChatClient(host, port, username);
        try {
            client.connect();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to connect to chat server at " + host + ":" + port + " - " + e.getMessage());
            return;
        }

        if (!forceCli) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    ChatClientFrame frame = new ChatClientFrame(client);
                    frame.setVisible(true);
                });
                return;
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable. Running Client in terminal CLI mode.");
            }
        }

        // Terminal interactive chat client
        Scanner scanner = new Scanner(System.in);
        client.addMessageListener(msg -> System.out.println("\n" + msg.toDisplayString() + "\n> "));
        client.addErrorListener(err -> System.err.println("\n[ERROR] " + err + "\n> "));
        client.addDisconnectListener(() -> {
            System.out.println("\n[NOTICE] Disconnected from server.");
            System.exit(0);
        });

        System.out.println("[CONNECTED] Type messages below. Use /msg <user> <text> for DM, /users for active list, /quit to exit.");
        System.out.print("> ");
        while (client.isConnected() && scanner.hasNextLine()) {
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.print("> ");
                continue;
            }
            if (text.equalsIgnoreCase("/quit") || text.equalsIgnoreCase("/exit")) {
                client.disconnect();
                break;
            } else if (text.equalsIgnoreCase("/users")) {
                client.requestUserList();
            } else if (text.startsWith("/msg ")) {
                String[] parts = text.split(" ", 3);
                if (parts.length >= 3) {
                    client.sendPrivateMessage(parts[1], parts[2]);
                } else {
                    System.out.println("[USAGE] /msg <username> <message text>");
                }
            } else {
                client.sendPublicMessage(text);
            }
            System.out.print("> ");
        }
    }

    private static int parsePort(String str, int defaultPort) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultPort;
        }
    }
}

