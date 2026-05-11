package com.backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SessionHistoryService 单元测试")
class SessionHistoryServiceTest {



    private RedisTemplate<String, Object> mockRedisTemplate;
    private ValueOperations<String, Object> mockValueOps;
    private SessionHistoryService sessionHistoryService;

    @BeforeEach
    void setUp() throws Exception {
        mockRedisTemplate = mock(RedisTemplate.class);
        mockValueOps = mock(ValueOperations.class);

        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOps);

        sessionHistoryService = new SessionHistoryService();

        Field redisTemplateField = SessionHistoryService.class.getDeclaredField("redisTemplate");
        redisTemplateField.setAccessible(true);
        redisTemplateField.set(sessionHistoryService, mockRedisTemplate);
    }

    @Test
    @DisplayName("保存用户消息到新会话")
    void testSaveUserMessage_NewSession() {
        String sessionId = "session-001";
        String message = "你好，我想学习Java";

        when(mockValueOps.get(anyString())).thenReturn(null);

        sessionHistoryService.saveUserMessage(sessionId, message);

        verify(mockValueOps, times(1)).set(
            eq("chat:session:" + sessionId),
            any(ChatSession.class),
            eq(720L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("保存用户消息到现有会话")
    void testSaveUserMessage_ExistingSession() {
        String sessionId = "session-001";
        String message = "第二个问题";

        ChatSession existingSession = new ChatSession(sessionId);
        existingSession.addMessage(new ChatMessage("user", "第一个问题"));

        when(mockValueOps.get("chat:session:" + sessionId)).thenReturn(existingSession);

        sessionHistoryService.saveUserMessage(sessionId, message);

        verify(mockValueOps, times(1)).set(
            eq("chat:session:" + sessionId),
            any(ChatSession.class),
            eq(720L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("保存AI回复到会话")
    void testSaveAssistantMessage() {
        String sessionId = "session-001";
        String message = "这是AI的回复";

        when(mockValueOps.get(anyString())).thenReturn(null);

        sessionHistoryService.saveAssistantMessage(sessionId, message);

        verify(mockValueOps, times(1)).set(
            eq("chat:session:" + sessionId),
            any(ChatSession.class),
            eq(720L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("获取存在的会话")
    void testGetSession_Exists() {
        String sessionId = "session-001";
        ChatSession expectedSession = new ChatSession(sessionId);
        expectedSession.addMessage(new ChatMessage("user", "测试消息"));

        when(mockValueOps.get("chat:session:" + sessionId)).thenReturn(expectedSession);

        ChatSession actualSession = sessionHistoryService.getSession(sessionId);

        assertNotNull(actualSession);
        assertEquals(sessionId, actualSession.getSessionId());
        assertEquals(1, actualSession.getMessages().size());
    }

    @Test
    @DisplayName("获取不存在的会话返回null")
    void testGetSession_NotExists() {
        String sessionId = "non-existent-session";

        when(mockValueOps.get("chat:session:" + sessionId)).thenReturn(null);

        ChatSession session = sessionHistoryService.getSession(sessionId);

        assertNull(session);
    }

    @Test
    @DisplayName("获取所有会话ID列表")
    void testGetAllSessionIds() {
        Set<String> keys = new HashSet<>();
        keys.add("chat:session:session-001");
        keys.add("chat:session:session-002");
        keys.add("chat:session:session-003");

        when(mockRedisTemplate.keys("chat:session:*")).thenReturn(keys);

        List<String> sessionIds = sessionHistoryService.getAllSessionIds();

        assertNotNull(sessionIds);
        assertEquals(3, sessionIds.size());
        assertTrue(sessionIds.contains("session-001"));
        assertTrue(sessionIds.contains("session-002"));
        assertTrue(sessionIds.contains("session-003"));
    }

    @Test
    @DisplayName("没有会话时返回空列表")
    void testGetAllSessionIds_Empty() {
        when(mockRedisTemplate.keys("chat:session:*")).thenReturn(new HashSet<>());

        List<String> sessionIds = sessionHistoryService.getAllSessionIds();

        assertNotNull(sessionIds);
        assertTrue(sessionIds.isEmpty());
    }

    @Test
    @DisplayName("删除指定会话")
    void testDeleteSession() {
        String sessionId = "session-001";

        sessionHistoryService.deleteSession(sessionId);

        verify(mockRedisTemplate, times(1)).delete("chat:session:" + sessionId);
    }

    @Test
    @DisplayName("清空所有会话")
    void testClearAllSessions() {
        Set<String> keys = new HashSet<>();
        keys.add("chat:session:session-001");
        keys.add("chat:session:session-002");

        when(mockRedisTemplate.keys("chat:session:*")).thenReturn(keys);

        sessionHistoryService.clearAllSessions();

        verify(mockRedisTemplate, times(1)).delete(keys);
    }

    @Test
    @DisplayName("清空会话时没有会话")
    void testClearAllSessions_NoSessions() {
        when(mockRedisTemplate.keys("chat:session:*")).thenReturn(null);

        sessionHistoryService.clearAllSessions();

        verify(mockRedisTemplate, never()).delete(anySet());
    }

    @Test
    @DisplayName("会话过期时间设置正确")
    void testSessionExpireTime() {
        String sessionId = "session-001";
        String message = "测试消息";

        when(mockValueOps.get(anyString())).thenReturn(null);

        sessionHistoryService.saveUserMessage(sessionId, message);

        verify(mockValueOps).set(
            anyString(),
            any(),
            eq(720L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("消息时间戳自动设置")
    void testMessageTimestamp() {
        String sessionId = "session-001";

        when(mockValueOps.get(anyString())).thenReturn(null);

        long beforeSave = System.currentTimeMillis();
        sessionHistoryService.saveUserMessage(sessionId, "测试");
        long afterSave = System.currentTimeMillis();

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(mockValueOps).set(anyString(), captor.capture(), anyLong(), any(TimeUnit.class));

        ChatSession savedSession = captor.getValue();
        assertNotNull(savedSession.getMessages());
        assertFalse(savedSession.getMessages().isEmpty());

        long messageTimestamp = savedSession.getMessages().get(0).getTimestamp();
        assertTrue(messageTimestamp >= beforeSave && messageTimestamp <= afterSave);
    }
}
