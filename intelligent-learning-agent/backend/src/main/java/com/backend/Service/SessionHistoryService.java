package com.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 负责管理所有的聊天记录
 */
@Service
public class SessionHistoryService {

    private static final String SESSION_KEY_PREFIX = "chat:session:";//   Redis 键的前缀
    private static final long SESSION_EXPIRE_HOURS = 720; // 过期时间

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户消息到会话
     */
    public void saveUserMessage(String sessionId, String message) {
        String key = SESSION_KEY_PREFIX + sessionId;
        ChatSession session = getSession(sessionId);// 获取旧对话
        if (session == null) {
            session = new ChatSession(sessionId);
        }
        session.addMessage(new ChatMessage("user", message));
        redisTemplate.opsForValue().set(key, session, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 保存AI回复到会话
     */
    public void saveAssistantMessage(String sessionId, String message) {
        String key = SESSION_KEY_PREFIX + sessionId;
        ChatSession session = getSession(sessionId);
        if (session == null) {
            session = new ChatSession(sessionId);
        }
        session.addMessage(new ChatMessage("assistant", message));
        redisTemplate.opsForValue().set(key, session, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 获取完整会话历史
     */
    public ChatSession getSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof ChatSession) {
            return (ChatSession) obj;
        }
        return null;
    }

    /**
     * 获取所有会话ID列表
     */
    public List<String> getAllSessionIds() {
        Set<String> keys = redisTemplate.keys(SESSION_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        return keys.stream()
                .map(key -> key.replace(SESSION_KEY_PREFIX, ""))
                .collect(Collectors.toList());
    }

    /**
     * 删除指定会话
     */
    public void deleteSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    /**
     * 清空所有会话
     */
    public void clearAllSessions() {
        Set<String> keys = redisTemplate.keys(SESSION_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}