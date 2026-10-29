package com.logiclegend.chat.ui;

import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;
import com.logiclegend.chat.server.ChatServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Server administrator GUI console.
 */
public class ServerDashboardFrame extends JFrame {
    private final ChatServer server;
    private final JTextArea logArea;
    private final JLabel clientCountLabel;
    private final DefaultTableModel clientsTableModel;
    private final JTable clientsTable;

    public ServerDashboardFrame(ChatServer server) {
        this.server = server;

        setTitle("LogicLegend Chat Server — Admin Management Dashboard");
        setSize(850, 520);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stop();
                dispose();
            }
        });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(245, 247, 250));

        // Top Status Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = new JLabel("  CHAT SERVER CONTROLLER (PORT " + server.getPort() + ")");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        clientCountLabel = new JLabel("Active Clients: 0  ");
        clientCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clientCountLabel.setForeground(new Color(46, 204, 113));
        headerPanel.add(clientCountLabel, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Center Split: Logs (Left) & Active Clients Table (Right)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(220, 220, 220));
        logArea.setMargin(new Insets(6, 6, 6, 6));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Server System & Communication Logs"));

        clientsTableModel = new DefaultTableModel(new String[]{"#", "Online Username"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        clientsTable = new JTable(clientsTableModel);
        clientsTable.setRowHeight(24);
        clientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane clientsScroll = new JScrollPane(clientsTable);
        clientsScroll.setPreferredSize(new Dimension(240, 0));
        clientsScroll.setBorder(BorderFactory.createTitledBorder("Connected Users"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, clientsScroll);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(5);
        root.add(splitPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottomBar.setOpaque(false);

        JButton broadcastBtn = new JButton("Broadcast Announcement");
        broadcastBtn.setBackground(new Color(52, 152, 219));
        broadcastBtn.setForeground(Color.WHITE);
        broadcastBtn.setFocusPainted(false);
        broadcastBtn.addActionListener(e -> promptBroadcast());

        JButton kickBtn = new JButton("Kick Selected User");
        kickBtn.setBackground(new Color(230, 126, 34));
        kickBtn.setForeground(Color.WHITE);
        kickBtn.setFocusPainted(false);
        kickBtn.addActionListener(e -> kickSelectedUser());

        JButton shutdownBtn = new JButton("Shutdown Server");
        shutdownBtn.setBackground(new Color(231, 76, 60));
        shutdownBtn.setForeground(Color.WHITE);
        shutdownBtn.setFocusPainted(false);
        shutdownBtn.addActionListener(e -> {
            server.stop();
            dispose();
        });

        bottomBar.add(broadcastBtn);
        bottomBar.add(kickBtn);
        bottomBar.add(shutdownBtn);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);

        // Hook log listener and periodic table refresher
        server.addLogListener(msg -> SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            refreshUserTable();
        }));

        refreshUserTable();
    }

    private void promptBroadcast() {
        String msg = JOptionPane.showInputDialog(this, "Enter system announcement to broadcast to all clients:", "Broadcast", JOptionPane.QUESTION_MESSAGE);
        if (msg != null && !msg.trim().isEmpty()) {
            server.broadcast(new ChatMessage(MessageType.SYSTEM_NOTICE, "SERVER", "*", "[ADMIN ANNOUNCEMENT] " + msg.trim()));
            server.log("[ADMIN BROADCAST] " + msg.trim());
        }
    }

    private void kickSelectedUser() {
        int row = clientsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the Connected Users table to kick.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) clientsTableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to kick '" + username + "' from the chat server?", "Confirm Kick", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            server.kickUser(username, "Disconnected by server administrator.");
            refreshUserTable();
        }
    }

    private void refreshUserTable() {
        clientsTableModel.setRowCount(0);
        List<String> users = server.getOnlineUsernames();
        int idx = 1;
        for (String u : users) {
            clientsTableModel.addRow(new Object[]{idx++, u});
        }
        clientCountLabel.setText("Active Clients: " + users.size() + "  ");
    }
}

