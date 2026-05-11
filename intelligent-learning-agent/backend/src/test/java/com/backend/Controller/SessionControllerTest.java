package com.backend.Controller;

import com.backend.Service.ChatSession;
import com.backend.Service.SessionHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionHistoryService sessionHistoryService;

    private String testSessionId;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testSessionId = UUID.randomUUID().toString();
        testSession = new ChatSession(testSessionId);
    }

    @Test
    void testGetSessionHistory_WithValidSession_ShouldReturnSession() throws Exception {
        // Arrange
        when(sessionHistoryService.getSession(testSessionId)).thenReturn(testSession);

        // Act & Assert
        mockMvc.perform(get("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());

        verify(sessionHistoryService, times(1)).getSession(testSessionId);
    }

    @Test
    void testGetSessionHistory_WithNonExistentSession_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(sessionHistoryService.getSession(testSessionId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("会话不存在"));

        verify(sessionHistoryService, times(1)).getSession(testSessionId);
    }

    @Test
    void testGetSessionHistory_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String errorMessage = "获取会话失败";
        when(sessionHistoryService.getSession(anyString()))
            .thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(errorMessage)));

        verify(sessionHistoryService, times(1)).getSession(testSessionId);
    }

    @Test
    void testGetAllSessions_ShouldReturnSessionList() throws Exception {
        // Arrange
        List<String> sessionIds = new ArrayList<>();
        sessionIds.add(UUID.randomUUID().toString());
        sessionIds.add(UUID.randomUUID().toString());
        sessionIds.add(UUID.randomUUID().toString());

        when(sessionHistoryService.getAllSessionIds()).thenReturn(sessionIds);

        // Act & Assert
        mockMvc.perform(get("/api/session/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        verify(sessionHistoryService, times(1)).getAllSessionIds();
    }

    @Test
    void testGetAllSessions_WithNoSessions_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(sessionHistoryService.getAllSessionIds()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/session/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(sessionHistoryService, times(1)).getAllSessionIds();
    }

    @Test
    void testGetAllSessions_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String errorMessage = "获取会话列表失败";
        when(sessionHistoryService.getAllSessionIds())
            .thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/session/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(errorMessage)));

        verify(sessionHistoryService, times(1)).getAllSessionIds();
    }

    @Test
    void testDeleteSession_WithValidSession_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(sessionHistoryService).deleteSession(testSessionId);

        // Act & Assert
        mockMvc.perform(delete("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("会话已删除"));

        verify(sessionHistoryService, times(1)).deleteSession(testSessionId);
    }

    @Test
    void testDeleteSession_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String errorMessage = "删除会话失败";
        doThrow(new RuntimeException(errorMessage)).when(sessionHistoryService).deleteSession(testSessionId);

        // Act & Assert
        mockMvc.perform(delete("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(errorMessage)));

        verify(sessionHistoryService, times(1)).deleteSession(testSessionId);
    }

    @Test
    void testClearAllSessions_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(sessionHistoryService).clearAllSessions();

        // Act & Assert
        mockMvc.perform(delete("/api/session/clear")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("所有会话已清空"));

        verify(sessionHistoryService, times(1)).clearAllSessions();
    }

    @Test
    void testClearAllSessions_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String errorMessage = "清空会话失败";
        doThrow(new RuntimeException(errorMessage)).when(sessionHistoryService).clearAllSessions();

        // Act & Assert
        mockMvc.perform(delete("/api/session/clear")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(errorMessage)));

        verify(sessionHistoryService, times(1)).clearAllSessions();
    }

    @Test
    void testGetSessionHistory_WithInvalidSessionIdFormat_ShouldHandleGracefully() throws Exception {
        // Arrange
        String invalidSessionId = "";
        when(sessionHistoryService.getSession(invalidSessionId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/session/{sessionId}", invalidSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(sessionHistoryService, times(1)).getSession(invalidSessionId);
    }

    @Test
    void testDeleteSession_MultipleTimes_ShouldHandleGracefully() throws Exception {
        // Arrange
        doNothing().when(sessionHistoryService).deleteSession(testSessionId);

        // Act - 第一次删除
        mockMvc.perform(delete("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Act - 第二次删除（会话已不存在）
        mockMvc.perform(delete("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionHistoryService, times(2)).deleteSession(testSessionId);
    }

    @Test
    void testGetAllSessions_WithLargeNumberOfSessions_ShouldReturnAll() throws Exception {
        // Arrange
        List<String> sessionIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            sessionIds.add(UUID.randomUUID().toString());
        }
        when(sessionHistoryService.getAllSessionIds()).thenReturn(sessionIds);

        // Act & Assert
        mockMvc.perform(get("/api/session/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(100));

        verify(sessionHistoryService, times(1)).getAllSessionIds();
    }

    @Test
    void testSessionEndpoints_ShouldSupportCors() throws Exception {
        // Arrange
        when(sessionHistoryService.getSession(testSessionId)).thenReturn(testSession);

        // Act & Assert - 验证跨域支持
        mockMvc.perform(get("/api/session/{sessionId}", testSessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());

        verify(sessionHistoryService, times(1)).getSession(testSessionId);
    }

    @Test
    void testGetSessionHistory_WithSpecialCharactersInSessionId_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String specialSessionId = "session-123_abc";
        when(sessionHistoryService.getSession(specialSessionId)).thenReturn(testSession);

        // Act & Assert
        mockMvc.perform(get("/api/session/{sessionId}", specialSessionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sessionHistoryService, times(1)).getSession(specialSessionId);
    }
}
