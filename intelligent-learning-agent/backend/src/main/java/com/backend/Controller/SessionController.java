package com.backend.Controller;

import com.backend.Service.ChatSession;
import com.backend.Service.SessionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "*")
public class SessionController {

    @Autowired
    private SessionHistoryService sessionHistoryService;

    /**
     * 获取指定会话的历史记录
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionHistory(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ChatSession session = sessionHistoryService.getSession(sessionId);
            if (session != null) {
                response.put("success", true);
                response.put("data", session);
            } else {
                response.put("success", false);
                response.put("message", "会话不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取会话失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有会话ID列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> sessionIds = sessionHistoryService.getAllSessionIds();
            response.put("success", true);
            response.put("data", sessionIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取会话列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除指定会话
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            sessionHistoryService.deleteSession(sessionId);
            response.put("success", true);
            response.put("message", "会话已删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除会话失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 清空所有会话
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllSessions() {
        Map<String, Object> response = new HashMap<>();
        try {
            sessionHistoryService.clearAllSessions();
            response.put("success", true);
            response.put("message", "所有会话已清空");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清空会话失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}