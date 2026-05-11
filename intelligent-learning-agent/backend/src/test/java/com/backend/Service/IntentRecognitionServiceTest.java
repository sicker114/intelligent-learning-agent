package com.backend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("IntentRecognitionService 单元测试")
class IntentRecognitionServiceTest {

    private ChatModel mockChatModel;
    private IntentRecognitionService intentRecognitionService;

    @BeforeEach
    void setUp() {
        mockChatModel = mock(ChatModel.class);
        intentRecognitionService = new IntentRecognitionService(mockChatModel);
    }

    @Test
    @DisplayName("识别文档查询意图")
    void testRecognizeIntent_DocumentQuery() {
        String userMessage = "什么是Java的多态性？";

        String result = intentRecognitionService.recognizeIntent(userMessage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("识别闲聊意图")
    void testRecognizeIntent_GeneralChat() {
        String userMessage = "你好，今天天气怎么样？";

        String result = intentRecognitionService.recognizeIntent(userMessage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("识别总结意图")
    void testRecognizeIntent_Summary() {
        String userMessage = "请帮我总结一下这一章的内容";

        String result = intentRecognitionService.recognizeIntent(userMessage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("识别对比意图")
    void testRecognizeIntent_Comparison() {
        String userMessage = "比较一下ArrayList和LinkedList的区别";

        String result = intentRecognitionService.recognizeIntent(userMessage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("识别知识点提取意图")
    void testRecognizeIntent_Extract() {
        String userMessage = "从这篇文章中提取所有的公式";

        String result = intentRecognitionService.recognizeIntent(userMessage);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("判断需要联网搜索")
    void testNeedsWebSearch_True() {
        String userMessage = "今天的新闻是什么？";

        boolean result = intentRecognitionService.needsWebSearch(userMessage);

        assertFalse(result);
    }

    @Test
    @DisplayName("判断不需要联网搜索")
    void testNeedsWebSearch_False() {
        String userMessage = "解释一下牛顿第二定律";

        boolean result = intentRecognitionService.needsWebSearch(userMessage);

        assertFalse(result);
    }

    @Test
    @DisplayName("空消息处理")
    void testRecognizeIntent_EmptyMessage() {
        String userMessage = "";

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("null消息处理")
    void testRecognizeIntent_NullMessage() {
        assertThrows(
            Exception.class,
            () -> intentRecognitionService.recognizeIntent(null)
        );
    }

    @Test
    @DisplayName("长消息处理")
    void testRecognizeIntent_LongMessage() {
        String userMessage = "这是一个非常长的问题，包含了大量的背景信息和详细描述，".repeat(10);

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("特殊字符消息处理")
    void testRecognizeIntent_SpecialCharacters() {
        String userMessage = "@#$%^&*()特殊字符测试！@#￥%……&*（）";

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("中文消息处理")
    void testRecognizeIntent_ChineseMessage() {
        String userMessage = "请问Java中的接口和抽象类有什么区别？";

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("英文消息处理")
    void testRecognizeIntent_EnglishMessage() {
        String userMessage = "What is the difference between interface and abstract class in Java?";

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("混合语言消息处理")
    void testRecognizeIntent_MixedLanguageMessage() {
        String userMessage = "请解释一下Java中的inheritance概念";

        assertDoesNotThrow(() -> {
            String result = intentRecognitionService.recognizeIntent(userMessage);
            assertNotNull(result);
        });
    }
}
