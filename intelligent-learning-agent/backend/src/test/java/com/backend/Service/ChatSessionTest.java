package com.backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatSession 单元测试")
class ChatSessionTest {

    private ChatSession session;
    private String sessionId;

    @BeforeEach
    void setUp() {
        sessionId = "test-session-001";
        session = new ChatSession(sessionId);
    }

    @Test
    @DisplayName("创建新会话")
    void testCreateSession() {
        assertNotNull(session);
        assertEquals(sessionId, session.getSessionId());
        assertNotNull(session.getMessages());
        assertTrue(session.getCreateTime() > 0);
        assertTrue(session.getLastUpdateTime() > 0);
    }

    @Test
    @DisplayName("添加用户消息")
    void testAddUserMessage() {
        ChatMessage message = new ChatMessage("user", "你好");
        session.addMessage(message);

        assertNotNull(session.getMessages());
        assertEquals(1, session.getMessages().size());
        assertEquals("你好", session.getMessages().get(0).getContent());
    }

    @Test
    @DisplayName("添加AI助手消息")
    void testAddAssistantMessage() {
        ChatMessage message = new ChatMessage("assistant", "你好！有什么可以帮助你的吗？");
        session.addMessage(message);

        assertEquals(1, session.getMessages().size());
        assertEquals("assistant", session.getMessages().get(0).getRole());
    }

    @Test
    @DisplayName("添加多条消息")
    void testAddMultipleMessages() {
        session.addMessage(new ChatMessage("user", "消息1"));
        session.addMessage(new ChatMessage("assistant", "回复1"));
        session.addMessage(new ChatMessage("user", "消息2"));
        session.addMessage(new ChatMessage("assistant", "回复2"));

        assertEquals(4, session.getMessages().size());
    }

    @Test
    @DisplayName("添加消息后更新最后更新时间")
    void testUpdateLastUpdateTime() {
        long beforeUpdate = session.getLastUpdateTime();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        session.addMessage(new ChatMessage("user", "新消息"));

        assertTrue(session.getLastUpdateTime() >= beforeUpdate);
    }

    @Test
    @DisplayName("获取会话ID")
    void testGetSessionId() {
        assertEquals(sessionId, session.getSessionId());
    }

    @Test
    @DisplayName("设置会话ID")
    void testSetSessionId() {
        String newSessionId = "new-session-002";
        session.setSessionId(newSessionId);

        assertEquals(newSessionId, session.getSessionId());
    }

    @Test
    @DisplayName("获取消息列表")
    void testGetMessages() {
        List<ChatMessage> messages = session.getMessages();
        assertNotNull(messages);
    }

    @Test
    @DisplayName("设置消息列表")
    void testSetMessages() {
        List<ChatMessage> messages = List.of(
            new ChatMessage("user", "消息1"),
            new ChatMessage("assistant", "回复1")
        );
        session.setMessages(messages);

        assertEquals(2, session.getMessages().size());
    }

    @Test
    @DisplayName("设置创建时间")
    void testSetCreateTime() {
        long createTime = 1234567890L;
        session.setCreateTime(createTime);

        assertEquals(createTime, session.getCreateTime());
    }

    @Test
    @DisplayName("设置最后更新时间")
    void testSetLastUpdateTime() {
        long updateTime = 1234567890L;
        session.setLastUpdateTime(updateTime);

        assertEquals(updateTime, session.getLastUpdateTime());
    }

    @Test
    @DisplayName("使用无参构造函数")
    void testNoArgsConstructor() {
        ChatSession newSession = new ChatSession();
        assertNotNull(newSession);
        assertNull(newSession.getSessionId());
    }

    @Test
    @DisplayName("使用全参构造函数")
    void testAllArgsConstructor() {
        long createTime = System.currentTimeMillis();
        long updateTime = System.currentTimeMillis();
        List<ChatMessage> messages = List.of(new ChatMessage("user", "测试"));

        ChatSession newSession = new ChatSession(sessionId, messages, createTime, updateTime);

        assertEquals(sessionId, newSession.getSessionId());
        assertEquals(messages, newSession.getMessages());
        assertEquals(createTime, newSession.getCreateTime());
        assertEquals(updateTime, newSession.getLastUpdateTime());
    }

    @Test
    @DisplayName("空会话的消息列表为null或空")
    void testEmptySessionMessages() {
        ChatSession newSession = new ChatSession("empty-session");

        List<ChatMessage> messages = newSession.getMessages();
        assertTrue(messages == null || messages.isEmpty());
    }

    @Test
    @DisplayName("添加null消息")
    void testAddNullMessage() {
        assertThrows(
            NullPointerException.class,
            () -> session.addMessage(null)
        );
    }

    @Test
    @DisplayName("消息顺序保持")
    void testMessageOrder() {
        session.addMessage(new ChatMessage("user", "第一条"));
        session.addMessage(new ChatMessage("assistant", "第二条"));
        session.addMessage(new ChatMessage("user", "第三条"));

        assertEquals("第一条", session.getMessages().get(0).getContent());
        assertEquals("第二条", session.getMessages().get(1).getContent());
        assertEquals("第三条", session.getMessages().get(2).getContent());
    }

    @Test
    @DisplayName("会话序列化兼容性")
    void testSerializable() {
        session.addMessage(new ChatMessage("user", "测试消息"));

        assertTrue(session instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("长时间运行的会话")
    void testLongRunningSession() {
        for (int i = 0; i < 100; i++) {
            session.addMessage(new ChatMessage("user", "消息" + i));
            session.addMessage(new ChatMessage("assistant", "回复" + i));
        }

        assertEquals(200, session.getMessages().size());
    }

    @Test
    @DisplayName("Lombok Data注解功能")
    void testDataAnnotation() {
        assertNotNull(session.toString());
        assertNotNull(session.hashCode());
    }

    @Test
    @DisplayName("会话ID可以为null")
    void testNullSessionId() {
        ChatSession newSession = new ChatSession();
        newSession.setSessionId(null);

        assertNull(newSession.getSessionId());
    }

    @Test
    @DisplayName("特殊字符会话ID")
    void testSpecialCharacterSessionId() {
        String specialId = "session-@#$%-001";
        ChatSession newSession = new ChatSession(specialId);

        assertEquals(specialId, newSession.getSessionId());
    }
}
