package com.backend.Controller;

import com.backend.Service.AgentOrchestratorService;
import com.backend.Service.SessionHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentOrchestratorService agentOrchestratorService;

    @MockBean
    private SessionHistoryService sessionHistoryService;


    private String testSessionId;
    private String testMessage;

    @BeforeEach
    void setUp() {
        testSessionId = UUID.randomUUID().toString();
        testMessage = "什么是机器学习？";
    }

    @Test
    void testStreamChat_WithValidMessage_ShouldReturnSseEmitter() throws Exception {
        // Arrange
        String mockResponse = "机器学习是人工智能的一个分支...";
        when(agentOrchestratorService.processRequest(eq(testMessage), anyString())).thenReturn(mockResponse);
        doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());
        doNothing().when(sessionHistoryService).saveAssistantMessage(anyString(), anyString());

        Map<String, String> payload = Map.of(
            "message", testMessage,
            "sessionId", testSessionId
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + testMessage + "\",\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify
        verify(sessionHistoryService, times(1)).saveUserMessage(eq(testSessionId), eq(testMessage));
        verify(agentOrchestratorService, times(1)).processRequest(eq(testMessage), eq(testSessionId));
    }

    @Test
    void testStreamChat_WithEmptyMessage_ShouldReturnError() throws Exception {
        // Arrange
        Map<String, String> payload = Map.of(
            "message", "",
            "sessionId", testSessionId
        );

        // Act & Assert
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"\",\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));

        verify(sessionHistoryService, never()).saveUserMessage(anyString(), anyString());
        verify(agentOrchestratorService, never()).processRequest(anyString(), anyString());
    }

    @Test
    void testStreamChat_WithNullMessage_ShouldReturnError() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));

        verify(sessionHistoryService, never()).saveUserMessage(anyString(), anyString());
    }

    @Test
    void testStreamChat_WithoutSessionId_ShouldGenerateNewSessionId() throws Exception {
        // Arrange
        String mockResponse = "这是AI的回答";
        when(agentOrchestratorService.processRequest(eq(testMessage), anyString())).thenReturn(mockResponse);
        doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());
        doNothing().when(sessionHistoryService).saveAssistantMessage(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + testMessage + "\"}"))
                .andExpect(status().isOk());

        verify(sessionHistoryService, times(1)).saveUserMessage(anyString(), eq(testMessage));
        verify(agentOrchestratorService, times(1)).processRequest(eq(testMessage), anyString());
    }

    @Test
    void testStreamChat_AgentProcessing_ShouldSaveMessages() throws Exception {
        // Arrange
        String mockResponse = "详细的学习资料内容...";
        when(agentOrchestratorService.processRequest(eq(testMessage), eq(testSessionId))).thenReturn(mockResponse);
        doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());
        doNothing().when(sessionHistoryService).saveAssistantMessage(anyString(), anyString());

        // Act
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + testMessage + "\",\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(status().isOk());

        // Assert
        verify(sessionHistoryService, times(1)).saveUserMessage(eq(testSessionId), eq(testMessage));
        verify(sessionHistoryService, times(1)).saveAssistantMessage(eq(testSessionId), eq(mockResponse));
    }

    @Test
    void testStreamChat_WithDifferentIntents_ShouldHandleCorrectly() throws Exception {
        // Test different types of questions
        String[] questions = {
            "今天几号？",
            "帮我总结一下学习资料",
            "对比一下机器学习和深度学习的区别",
            "你好"
        };

        for (String question : questions) {
            // Arrange
            when(agentOrchestratorService.processRequest(eq(question), anyString()))
                .thenReturn("回答：" + question);
            doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());
            doNothing().when(sessionHistoryService).saveAssistantMessage(anyString(), anyString());

            // Act & Assert
            mockMvc.perform(post("/api/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\":\"" + question + "\"}"))
                    .andExpect(status().isOk());

            verify(agentOrchestratorService, atLeastOnce()).processRequest(eq(question), anyString());
        }
    }

    @Test
    void testStreamChat_WithLongResponse_ShouldStreamInChunks() throws Exception {
        // Arrange
        StringBuilder longResponse = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longResponse.append("这是第").append(i).append("段内容。");
        }
        String response = longResponse.toString();

        when(agentOrchestratorService.processRequest(eq(testMessage), eq(testSessionId))).thenReturn(response);
        doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());
        doNothing().when(sessionHistoryService).saveAssistantMessage(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + testMessage + "\",\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(status().isOk());

        verify(sessionHistoryService, times(1)).saveAssistantMessage(eq(testSessionId), eq(response));
    }

    @Test
    void testStreamChat_WhenAgentThrowsException_ShouldHandleGracefully() throws Exception {
        // Arrange
        String errorMessage = "服务异常";
        when(agentOrchestratorService.processRequest(eq(testMessage), anyString()))
            .thenThrow(new RuntimeException(errorMessage));
        doNothing().when(sessionHistoryService).saveUserMessage(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + testMessage + "\",\"sessionId\":\"" + testSessionId + "\"}"))
                .andExpect(result -> assertNotNull(result.getResolvedException()));

        verify(sessionHistoryService, times(1)).saveUserMessage(eq(testSessionId), eq(testMessage));
        verify(sessionHistoryService, never()).saveAssistantMessage(anyString(), anyString());
    }
}
