package tests;

import com.logiclegend.chat.client.ChatClient;
import com.logiclegend.chat.model.ChatMessage;
import com.logiclegend.chat.model.MessageType;
import com.logiclegend.chat.server.ChatServer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Condition was FALSE\n", testName);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-05: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        int testPort = 9123;
        ChatServer server = new ChatServer(testPort);

        try {
            server.start();
            assertTrue("ChatServer started and isRunning() is true", server.isRunning());
            assertEquals("ChatServer bound to port 9123", testPort, server.getPort());

            testClientHandshakeAndConnection(server, testPort);
            testDuplicateUsernameRejection(server, testPort);
            testPublicMessageBroadcast(server, testPort);
            testPrivateMessageRoutingAndIsolation(server, testPort);
            testOnlineUserDirectorySynchronization(server, testPort);
            testGracefulDisconnectionAndStateCleanup(server, testPort);
            testAdministratorKickWorkflow(server, testPort);
            testProtocolSerializationAndBoundaryIntegrity();

        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            server.stop();
        }

        System.out.println("=================================================================");
        System.out.printf("  TOTAL ASSERTIONS: %d | PASSED: %d | FAILED: %d\n", passed + failed, passed, failed);
        System.out.println("=================================================================");

        System.exit(failed > 0 ? 1 : 0);
    }

    private static void testClientHandshakeAndConnection(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 1: Handshake and Client Registration ---");
        ChatClient clientAlice = new ChatClient("127.0.0.1", port, "Alice");
        List<ChatMessage> received = new CopyOnWriteArrayList<>();
        clientAlice.addMessageListener(received::add);

        clientAlice.connect();
        Thread.sleep(200);

        assertTrue("Alice is connected", clientAlice.isConnected());
        assertEquals("Server tracks 1 client", 1, server.getClientCount());
        assertTrue("Online users contains Alice", server.getOnlineUsernames().contains("Alice"));
        assertTrue("Alice received welcome or system notice", !received.isEmpty());

        clientAlice.disconnect();
        Thread.sleep(150);
    }

    private static void testDuplicateUsernameRejection(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 2: Duplicate Username Rejection ---");
        ChatClient clientOriginal = new ChatClient("127.0.0.1", port, "David");
        clientOriginal.connect();
        Thread.sleep(150);

        ChatClient clientDuplicate = new ChatClient("127.0.0.1", port, "david");
        List<ChatMessage> dupMessages = new CopyOnWriteArrayList<>();
        clientDuplicate.addMessageListener(dupMessages::add);

        clientDuplicate.connect();
        Thread.sleep(250);

        boolean errorReceived = dupMessages.stream()
                .anyMatch(m -> m.getType() == MessageType.ERROR && m.getContent().toLowerCase().contains("already taken"));
        assertTrue("Duplicate username 'david' received ERROR response", errorReceived);
        assertTrue("Duplicate client was closed/disconnected by server", !clientDuplicate.isConnected());

        clientOriginal.disconnect();
        Thread.sleep(150);
    }

    private static void testPublicMessageBroadcast(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 3: Public Broadcast Messaging ---");
        ChatClient alice = new ChatClient("127.0.0.1", port, "Alice");
        ChatClient bob = new ChatClient("127.0.0.1", port, "Bob");

        List<ChatMessage> bobMessages = new CopyOnWriteArrayList<>();
        bob.addMessageListener(bobMessages::add);

        alice.connect();
        bob.connect();
        Thread.sleep(200);

        alice.sendPublicMessage("Greetings Bob from Alice!");
        Thread.sleep(200);

        boolean receivedPublic = bobMessages.stream()
                .anyMatch(m -> m.getType() == MessageType.PUBLIC_CHAT &&
                               "Alice".equals(m.getSender()) &&
                               m.getContent().contains("Greetings Bob from Alice!"));

        assertTrue("Bob received public broadcast from Alice", receivedPublic);

        alice.disconnect();
        bob.disconnect();
        Thread.sleep(150);
    }

    private static void testPrivateMessageRoutingAndIsolation(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 4: Private Direct Messaging Isolation ---");
        ChatClient alice = new ChatClient("127.0.0.1", port, "Alice");
        ChatClient bob = new ChatClient("127.0.0.1", port, "Bob");
        ChatClient eve = new ChatClient("127.0.0.1", port, "Eve");

        List<ChatMessage> bobMessages = new CopyOnWriteArrayList<>();
        List<ChatMessage> eveMessages = new CopyOnWriteArrayList<>();

        bob.addMessageListener(bobMessages::add);
        eve.addMessageListener(eveMessages::add);

        alice.connect();
        bob.connect();
        eve.connect();
        Thread.sleep(200);

        String secretText = "Classified-Token-789";
        alice.sendPrivateMessage("Bob", secretText);
        Thread.sleep(200);

        boolean bobGotSecret = bobMessages.stream()
                .anyMatch(m -> m.getType() == MessageType.PRIVATE_CHAT &&
                               m.getContent().contains(secretText));

        boolean eveGotSecret = eveMessages.stream()
                .anyMatch(m -> m.getContent().contains(secretText));

        assertTrue("Bob received private message directly from Alice", bobGotSecret);
        assertTrue("Eve did NOT receive private message between Alice and Bob", !eveGotSecret);

        alice.disconnect();
        bob.disconnect();
        eve.disconnect();
        Thread.sleep(150);
    }

    private static void testOnlineUserDirectorySynchronization(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 5: Online User Directory Synchronization ---");
        ChatClient user1 = new ChatClient("127.0.0.1", port, "Alpha");
        ChatClient user2 = new ChatClient("127.0.0.1", port, "Beta");

        List<ChatMessage> betaMessages = new CopyOnWriteArrayList<>();
        user2.addMessageListener(betaMessages::add);

        user1.connect();
        Thread.sleep(100);
        user2.connect();
        Thread.sleep(200);

        List<String> online = server.getOnlineUsernames();
        assertEquals("Server reports 2 online users", 2, online.size());
        assertTrue("Alpha is listed in directory", online.contains("Alpha"));
        assertTrue("Beta is listed in directory", online.contains("Beta"));

        boolean userListReceived = betaMessages.stream()
                .anyMatch(m -> m.getType() == MessageType.USER_LIST && m.getContent().contains("Alpha"));
        assertTrue("Beta received synchronized USER_LIST containing Alpha", userListReceived);

        user1.disconnect();
        user2.disconnect();
        Thread.sleep(150);
    }

    private static void testGracefulDisconnectionAndStateCleanup(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 6: Graceful Disconnect and State Cleanup ---");
        ChatClient tempClient = new ChatClient("127.0.0.1", port, "TempUser");
        tempClient.connect();
        Thread.sleep(150);

        assertEquals("Server registers TempUser", 1, server.getClientCount());
        tempClient.disconnect();
        Thread.sleep(200);

        assertEquals("Server decrements client count to 0 after disconnect", 0, server.getClientCount());
        assertTrue("TempUser is removed from online directory", !server.getOnlineUsernames().contains("TempUser"));
    }

    private static void testAdministratorKickWorkflow(ChatServer server, int port) throws Exception {
        System.out.println("\n--- Test Suite 7: Administrator Kick Workflow ---");
        ChatClient troubleMaker = new ChatClient("127.0.0.1", port, "Spammer");
        List<ChatMessage> spammerMessages = new CopyOnWriteArrayList<>();
        troubleMaker.addMessageListener(spammerMessages::add);

        troubleMaker.connect();
        Thread.sleep(150);

        assertEquals("Spammer is registered", 1, server.getClientCount());

        boolean kickResult = server.kickUser("Spammer", "Spamming the chat room.");
        assertTrue("Admin kick method returned true", kickResult);
        Thread.sleep(200);

        boolean receivedKickNotice = spammerMessages.stream()
                .anyMatch(m -> m.getType() == MessageType.SYSTEM_NOTICE && m.getContent().contains("administrator"));
        assertTrue("Spammer received kick notice from server", receivedKickNotice);
        assertEquals("Client count is 0 after kick", 0, server.getClientCount());
    }

    private static void testProtocolSerializationAndBoundaryIntegrity() {
        System.out.println("\n--- Test Suite 8: Wire Protocol Serialization & Boundary Tests ---");

        String complexText = "Hello! Check pipe | characters, newlines \n\n, emojis \uD83D\uDE80 & JSON {\"status\":\"ok\"}";
        ChatMessage original = new ChatMessage(MessageType.PUBLIC_CHAT, "Alice", "*", complexText);

        String serialized = original.serialize();
        assertTrue("Serialized wire format contains message type", serialized.startsWith("PUBLIC_CHAT|"));

        ChatMessage parsed = ChatMessage.deserialize(serialized);
        assertTrue("Deserialized message is non-null", parsed != null);
        assertEquals("Parsed message type matches", MessageType.PUBLIC_CHAT, parsed.getType());
        assertEquals("Parsed sender matches", "Alice", parsed.getSender());
        assertEquals("Parsed recipient matches", "*", parsed.getRecipient());
        assertEquals("Parsed complex content matches exactly across base64", complexText, parsed.getContent());

        ChatMessage malformed = ChatMessage.deserialize("NOT_ENOUGH_FIELDS");
        assertTrue("Malformed input handled safely without null", malformed != null);
    }
}
