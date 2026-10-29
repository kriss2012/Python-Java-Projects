package com.logiclegend.chat.model;

/**
 * Protocol message types for client-server communication.
 */
public enum MessageType {
    CONNECT,          // Initial handshake with chosen username
    DISCONNECT,       // Graceful client exit (/quit)
    PUBLIC_CHAT,      // Message broadcast to all connected users
    PRIVATE_CHAT,     // One-to-one direct message (/msg username message)
    USER_LIST,        // Synchronized list of online usernames
    SYSTEM_NOTICE,    // Informational server announcement or alert
    ERROR,            // Failure notification (e.g., username taken, user not found)
    FILE_TRANSFER,    // File payload transfer notice
    PING              // Keep-alive heartbeat
}

