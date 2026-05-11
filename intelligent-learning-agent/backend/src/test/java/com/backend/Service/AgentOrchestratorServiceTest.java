package com.backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AgentOrchestratorService 单元测试")
class AgentOrchestratorServiceTest {

    private ChatModel mockChatModel;
    private VectorStore mockVectorStore;
    private IntentRecognitionService mockIntentService;
    private SessionHistoryService mockSessionHistoryService;
    private ToolService mockToolService;
    private AgentOrchestratorService agentOrchestratorService;

    @BeforeEach
    void setUp() {
        mockChatModel = mock(ChatModel.class);
        mockVectorStore = mock(VectorStore.class);
        mockIntentService = mock(IntentRecognitionService.class);
        mockSessionHistoryService = mock(SessionHistoryService.class);
        mockToolService = mock(ToolService.class);

        agentOrchestratorService = new AgentOrchestratorService(
            mockChatModel,
            mockVectorStore,
            mockIntentService,
            mockSessionHistoryService,
            mockToolService
        );
    }

    @Test
    @DisplayName("处理文档查询请求")
    void testProcessRequest_DocumentQuery() {
        String userMessage = "什么是Java的多态？";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("DOCUMENT_QUERY");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockIntentService, times(1)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理闲聊请求")
    void testProcessRequest_GeneralChat() {
        String userMessage = "你好";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("GENERAL_CHAT");

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockIntentService, times(1)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理总结请求")
    void testProcessRequest_Summary() {
        String userMessage = "总结一下学习内容";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("SUMMARY");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockIntentService, times(1)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理对比请求")
    void testProcessRequest_Comparison() {
        String userMessage = "比较ArrayList和LinkedList";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("COMPARISON");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockIntentService, times(1)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理知识点提取请求")
    void testProcessRequest_Extract() {
        String userMessage = "提取所有公式";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("EXTRACT");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockIntentService, times(1)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理日期时间工具调用")
    void testProcessRequest_DateTimeTool() {
        String userMessage = "今天是几号？";
        String sessionId = "session-001";

        when(mockToolService.getCurrentDateTime()).thenReturn("2026年05月11日 星期一");

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        assertTrue(result.contains("当前日期"));
        verify(mockToolService, times(1)).getCurrentDateTime();
    }

    @Test
    @DisplayName("处理网页抓取工具调用")
    void testProcessRequest_WebFetchTool() {
        String userMessage = "帮我看看这个网页 https://example.com";
        String sessionId = "session-001";

        when(mockToolService.fetchWebContent(anyString())).thenReturn("网页内容");

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockToolService, times(1)).fetchWebContent(anyString());
    }

    @Test
    @DisplayName("处理搜索工具调用")
    void testProcessRequest_SearchTool() {
        String userMessage = "搜索Java教程";
        String sessionId = "session-001";

        when(mockToolService.webSearch(anyString())).thenReturn("搜索结果");

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockToolService, times(1)).webSearch(anyString());
    }

    @Test
    @DisplayName("处理空消息抛出异常")
    void testProcessRequest_EmptyMessage() {
        String userMessage = "";
        String sessionId = "session-001";

        assertThrows(
            Exception.class,
            () -> agentOrchestratorService.processRequest(userMessage, sessionId)
        );
    }

    @Test
    @DisplayName("处理null消息抛出异常")
    void testProcessRequest_NullMessage() {
        String sessionId = "session-001";

        assertThrows(
            Exception.class,
            () -> agentOrchestratorService.processRequest(null, sessionId)
        );
    }

    @Test
    @DisplayName("未知意图使用默认处理")
    void testProcessRequest_UnknownIntent() {
        String userMessage = "测试消息";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("UNKNOWN");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("向量库返回文档时正确处理")
    void testProcessRequest_WithDocuments() {
        String userMessage = "Java是什么？";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("DOCUMENT_QUERY");

        Document doc = new Document("Java是一种面向对象的编程语言");
        when(mockVectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(List.of(doc));

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockVectorStore, times(1)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("sessionId为null时使用默认值")
    void testProcessRequest_NullSessionId() {
        String userMessage = "测试消息";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("GENERAL_CHAT");

        assertDoesNotThrow(() -> {
            String result = agentOrchestratorService.processRequest(userMessage, null);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("多次处理相同请求")
    void testProcessRequest_MultipleCalls() {
        String userMessage = "你好";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage)).thenReturn("GENERAL_CHAT");

        String result1 = agentOrchestratorService.processRequest(userMessage, sessionId);
        String result2 = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result1);
        assertNotNull(result2);
        verify(mockIntentService, times(2)).recognizeIntent(userMessage);
    }

    @Test
    @DisplayName("处理包含URL的消息")
    void testProcessRequest_MessageWithUrl() {
        String userMessage = "查看 https://www.example.com 的内容";
        String sessionId = "session-001";

        when(mockToolService.fetchWebContent(anyString())).thenReturn("网页内容摘要");

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        verify(mockToolService, times(1)).fetchWebContent("https://www.example.com");
    }

    @Test
    @DisplayName("异常情况下返回错误信息")
    void testProcessRequest_ExceptionHandling() {
        String userMessage = "测试消息";
        String sessionId = "session-001";

        when(mockIntentService.recognizeIntent(userMessage))
            .thenThrow(new RuntimeException("模拟异常"));

        String result = agentOrchestratorService.processRequest(userMessage, sessionId);

        assertNotNull(result);
        assertTrue(result.contains("错误") || result.contains("异常"));
    }
}
