package com.logiclegend.chat.ui;

import com.logiclegend.chat.client.ChatClient;
import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * Modern Swing graphical chat client interface.
 */
public class ChatClientFrame extends JFrame {
    private final ChatClient client;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final DefaultListModel<String> userListModel;
    private final JList<String> userList;
    private final JComboBox<String> recipientCombo;
    private final JLabel statusLabel;

    public ChatClientFrame(ChatClient client) {
        this.client = client;

        setTitle("LogicLegend Chat — " + client.getUsername() + " (" + client.getHost() + ":" + client.getPort() + ")");
        setSize(850, 580);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
                dispose();
            }
        });

        // Main layout container
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(245, 247, 250));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel("  LOGIC LEGEND CHAT ROOM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Logged in as: " + client.getUsername() + " | Connected to " + client.getHost() + ":" + client.getPort() + "  ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(236, 240, 241));

        JButton disconnectBtn = new JButton("Disconnect");
        disconnectBtn.setFocusPainted(false);
        disconnectBtn.setBackground(new Color(231, 76, 60));
        disconnectBtn.setForeground(Color.WHITE);
        disconnectBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        disconnectBtn.addActionListener(e -> {
            client.disconnect();
            dispose();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(statusLabel);
        rightHeader.add(disconnectBtn);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Center Content Split Pane
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        chatArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("Live Conversation"));

        // Recipients combo box
        recipientCombo = new JComboBox<>();
        recipientCombo.addItem("Public Room (*)");
        recipientCombo.setPreferredSize(new Dimension(160, 26));

        // User list panel
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null && !selected.equalsIgnoreCase(client.getUsername())) {
                    recipientCombo.setSelectedItem(selected);
                }
            }
        });

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(180, 0));
        userScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, userScroll);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerSize(5);
        root.add(splitPane, BorderLayout.CENTER);

        // Bottom Input Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);

        JPanel sendToPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sendToPanel.setOpaque(false);
        sendToPanel.add(new JLabel("Send To:"));
        sendToPanel.add(recipientCombo);
        bottomPanel.add(sendToPanel, BorderLayout.WEST);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputField.addActionListener(e -> sendMessage());
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBackground(new Color(46, 204, 113));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendBtn.setFocusPainted(false);
        sendBtn.addActionListener(e -> sendMessage());
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);

        // Register client event hooks
        client.addMessageListener(this::onMessageReceived);
        client.addErrorListener(err -> SwingUtilities.invokeLater(() -> appendMessage("[SYSTEM ERROR] " + err)));
        client.addDisconnectListener(() -> SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Disconnected from server.");
            inputField.setEnabled(false);
            sendBtn.setEnabled(false);
            appendMessage("[SESSION] Disconnected from server.");
        }));

        appendMessage("[SYSTEM] Connected as " + client.getUsername() + ". Start typing below!");
        appendMessage("[TIP] Type /msg <username> <text> for private messages or /quit to leave.");
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        if (text.startsWith("/quit") || text.startsWith("/exit")) {
            client.disconnect();
            dispose();
            return;
        }

        if (text.startsWith("/msg ")) {
            String[] parts = text.split(" ", 3);
            if (parts.length >= 3) {
                client.sendPrivateMessage(parts[1], parts[2]);
            } else {
                appendMessage("[USAGE] /msg <username> <message text>");
            }
        } else {
            String selectedRecipient = (String) recipientCombo.getSelectedItem();
            if (selectedRecipient != null && !selectedRecipient.startsWith("Public Room")) {
                client.sendPrivateMessage(selectedRecipient, text);
            } else {
                client.sendPublicMessage(text);
            }
        }

        inputField.setText("");
        inputField.requestFocus();
    }

    private void onMessageReceived(ChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getType() == MessageType.USER_LIST) {
                updateOnlineUsers(msg.getContent());
            } else {
                appendMessage(msg.toDisplayString());
            }
        });
    }

    private void updateOnlineUsers(String rawUserList) {
        userListModel.clear();
        String currentSelected = (String) recipientCombo.getSelectedItem();
        recipientCombo.removeAllItems();
        recipientCombo.addItem("Public Room (*)");

        if (rawUserList != null && !rawUserList.trim().isEmpty()) {
            String[] users = rawUserList.split(",");
            Arrays.sort(users, String.CASE_INSENSITIVE_ORDER);
            for (String u : users) {
                String name = u.trim();
                if (!name.isEmpty()) {
                    userListModel.addElement(name);
                    if (!name.equalsIgnoreCase(client.getUsername())) {
                        recipientCombo.addItem(name);
                    }
                }
            }
        }

        if (currentSelected != null) {
            recipientCombo.setSelectedItem(currentSelected);
        }
    }

    private void appendMessage(String line) {
        chatArea.append(line + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
