package com.logiclegend.chat.model;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Encapsulates an atomic chat transmission over the network.
 * Uses robust Base64 encoding for payload contents to prevent delimiter collisions.
 */
public class ChatMessage {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DELIMITER = "|";

    private final MessageType type;
    private final String sender;
    private final String recipient;
    private final String timestamp;
    private final String content;

    public ChatMessage(MessageType type, String sender, String recipient, String content) {
        this(type, sender, recipient, LocalDateTime.now().format(TIME_FORMAT), content);
    }

    public ChatMessage(MessageType type, String sender, String recipient, String timestamp, String content) {
        this.type = type != null ? type : MessageType.PUBLIC_CHAT;
        this.sender = sender != null ? sender.trim() : "ANONYMOUS";
        this.recipient = recipient != null && !recipient.trim().isEmpty() ? recipient.trim() : "*";
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(TIME_FORMAT);
        this.content = content != null ? content : "";
    }

    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getTimestamp() { return timestamp; }
    public String getContent() { return content; }

    public boolean isPrivate() {
        return type == MessageType.PRIVATE_CHAT;
    }

    /**
     * Serializes this message to wire transmission string:
     * TYPE|TIMESTAMP|SENDER|RECIPIENT|BASE64_CONTENT
     */
    public String serialize() {
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        return type.name() + DELIMITER +
               timestamp + DELIMITER +
               sender + DELIMITER +
               recipient + DELIMITER +
               encodedContent;
    }

    /**
     * Deserializes a raw network string back into a ChatMessage instance.
     */
    public static ChatMessage deserialize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String[] parts = raw.split("\\|", 5);
        if (parts.length < 5) {
            // Fallback for raw legacy or telnet strings
            return new ChatMessage(MessageType.PUBLIC_CHAT, "UNKNOWN", "*", raw);
        }

        try {
            MessageType type = MessageType.valueOf(parts[0].trim().toUpperCase());
            String timestamp = parts[1].trim();
            String sender = parts[2].trim();
            String recipient = parts[3].trim();
            String content = new String(Base64.getDecoder().decode(parts[4].trim()), StandardCharsets.UTF_8);

            return new ChatMessage(type, sender, recipient, timestamp, content);
        } catch (Exception e) {
            return new ChatMessage(MessageType.ERROR, "SYSTEM", "*", "Malformed message: " + raw);
        }
    }

    /**
     * Human-friendly formatted rendering for UI / CLI logs.
     */
    public String toDisplayString() {
        return switch (type) {
            case CONNECT -> String.format("[%s] --> %s joined the chat room.", timestamp, sender);
            case DISCONNECT -> String.format("[%s] <-- %s left the chat room.", timestamp, sender);
            case PUBLIC_CHAT -> String.format("[%s] %s: %s", timestamp, sender, content);
            case PRIVATE_CHAT -> String.format("[%s] [DM %s -> %s]: %s", timestamp, sender, recipient, content);
            case SYSTEM_NOTICE -> String.format("[%s] [SYSTEM]: %s", timestamp, content);
            case ERROR -> String.format("[%s] [ERROR]: %s", timestamp, content);
            case USER_LIST -> String.format("[%s] Active Users: %s", timestamp, content);
            case FILE_TRANSFER -> String.format("[%s] [FILE from %s]: %s", timestamp, sender, content);
            case PING -> String.format("[%s] PING", timestamp);
        };
    }

    @Override
    public String toString() {
        return serialize();
    }
}

