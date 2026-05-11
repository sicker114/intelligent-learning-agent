package com.backend.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatMessage 单元测试")
class ChatMessageTest {

    @Test
    @DisplayName("创建用户消息")
    void testCreateUserMessage() {
        ChatMessage message = new ChatMessage("user", "你好");

        assertNotNull(message);
        assertEquals("user", message.getRole());
        assertEquals("你好", message.getContent());
        assertTrue(message.getTimestamp() > 0);
    }

    @Test
    @DisplayName("创建AI助手消息")
    void testCreateAssistantMessage() {
        ChatMessage message = new ChatMessage("assistant", "你好！有什么可以帮助你的吗？");

        assertNotNull(message);
        assertEquals("assistant", message.getRole());
        assertEquals("你好！有什么可以帮助你的吗？", message.getContent());
        assertTrue(message.getTimestamp() > 0);
    }

    @Test
    @DisplayName("使用无参构造函数")
    void testNoArgsConstructor() {
        ChatMessage message = new ChatMessage();

        assertNotNull(message);
        assertNull(message.getRole());
        assertNull(message.getContent());
        assertEquals(0, message.getTimestamp());
    }

    @Test
    @DisplayName("使用全参构造函数")
    void testAllArgsConstructor() {
        long timestamp = System.currentTimeMillis();
        ChatMessage message = new ChatMessage("user", "测试消息", timestamp);

        assertNotNull(message);
        assertEquals("user", message.getRole());
        assertEquals("测试消息", message.getContent());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    @DisplayName("设置角色")
    void testSetRole() {
        ChatMessage message = new ChatMessage();
        message.setRole("user");

        assertEquals("user", message.getRole());
    }

    @Test
    @DisplayName("设置内容")
    void testSetContent() {
        ChatMessage message = new ChatMessage();
        message.setContent("新消息");

        assertEquals("新消息", message.getContent());
    }

    @Test
    @DisplayName("设置时间戳")
    void testSetTimestamp() {
        ChatMessage message = new ChatMessage();
        long timestamp = 1234567890L;
        message.setTimestamp(timestamp);

        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    @DisplayName("空内容消息")
    void testEmptyContent() {
        ChatMessage message = new ChatMessage("user", "");

        assertNotNull(message);
        assertEquals("", message.getContent());
    }

    @Test
    @DisplayName("null内容消息")
    void testNullContent() {
        ChatMessage message = new ChatMessage("user", null);

        assertNotNull(message);
        assertNull(message.getContent());
    }

    @Test
    @DisplayName("长内容消息")
    void testLongContent() {
        String longContent = "这是一条很长的消息 ".repeat(100);
        ChatMessage message = new ChatMessage("user", longContent);

        assertNotNull(message);
        assertEquals(longContent, message.getContent());
    }

    @Test
    @DisplayName("特殊字符内容")
    void testSpecialCharacters() {
        String specialContent = "@#$%^&*()!@#￥%……&*（）";
        ChatMessage message = new ChatMessage("user", specialContent);

        assertNotNull(message);
        assertEquals(specialContent, message.getContent());
    }

    @Test
    @DisplayName("多行内容")
    void testMultilineContent() {
        String multilineContent = "第一行\n第二行\n第三行";
        ChatMessage message = new ChatMessage("user", multilineContent);

        assertNotNull(message);
        assertEquals(multilineContent, message.getContent());
    }

    @Test
    @DisplayName("时间戳自动设置为当前时间")
    void testAutoTimestamp() {
        long before = System.currentTimeMillis();
        ChatMessage message = new ChatMessage("user", "测试");
        long after = System.currentTimeMillis();

        assertTrue(message.getTimestamp() >= before);
        assertTrue(message.getTimestamp() <= after);
    }

    @Test
    @DisplayName("序列化兼容性")
    void testSerializable() {
        ChatMessage message = new ChatMessage("user", "测试消息");

        assertNotNull(message);
        assertTrue(message instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("不同角色类型")
    void testDifferentRoles() {
        ChatMessage userMessage = new ChatMessage("user", "用户消息");
        ChatMessage assistantMessage = new ChatMessage("assistant", "助手消息");
        ChatMessage systemMessage = new ChatMessage("system", "系统消息");

        assertEquals("user", userMessage.getRole());
        assertEquals("assistant", assistantMessage.getRole());
        assertEquals("system", systemMessage.getRole());
    }

    @Test
    @DisplayName("Lombok Data注解功能")
    void testDataAnnotation() {
        ChatMessage message1 = new ChatMessage("user", "测试");
        ChatMessage message2 = new ChatMessage("user", "测试");

        assertNotNull(message1.toString());
        assertNotNull(message1.hashCode());
    }

    @Test
    @DisplayName("中文内容")
    void testChineseContent() {
        String chineseContent = "你好，世界！这是一个中文测试消息。";
        ChatMessage message = new ChatMessage("user", chineseContent);

        assertEquals(chineseContent, message.getContent());
    }

    @Test
    @DisplayName("英文内容")
    void testEnglishContent() {
        String englishContent = "Hello, World! This is a test message.";
        ChatMessage message = new ChatMessage("user", englishContent);

        assertEquals(englishContent, message.getContent());
    }

    @Test
    @DisplayName("混合语言内容")
    void testMixedLanguageContent() {
        String mixedContent = "Hello 你好 World 世界";
        ChatMessage message = new ChatMessage("user", mixedContent);

        assertEquals(mixedContent, message.getContent());
    }

    @Test
    @DisplayName("Emoji内容")
    void testEmojiContent() {
        String emojiContent = "你好 👋 世界 🌍";
        ChatMessage message = new ChatMessage("user", emojiContent);

        assertEquals(emojiContent, message.getContent());
    }
}
