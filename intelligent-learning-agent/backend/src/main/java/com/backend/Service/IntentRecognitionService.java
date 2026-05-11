package com.backend.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * 意图识别服务
 */
@Service
public class IntentRecognitionService {

    private final ChatClient chatClient;

    public IntentRecognitionService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 识别用户意图类型
     * @return: DOCUMENT_QUERY(学习答疑), GENERAL_CHAT(闲聊), SUMMARY(总结), COMPARISON(对比), EXTRACT(知识点提取)
     */
    public String recognizeIntent(String userMessage) {
        String prompt = """
                你是一个学习助手的意图识别模块。请分析学生问题的意图，返回以下类别之一：
                - DOCUMENT_QUERY: 关于学习内容的具体问题、概念解释、题目解答
                - GENERAL_CHAT: 日常聊天、问候、学习方法咨询等
                - SUMMARY: 要求总结、概括学习内容，生成复习提纲
                - COMPARISON: 对比不同概念、方法、理论的异同
                - EXTRACT: 提取知识点、公式、定义、关键信息
                
                只返回类别名称，不要解释。
                
                学生问题：%s
                """;

        return chatClient.prompt()
                .user(String.format(prompt, userMessage))
                .call()
                .content()
                .trim();
    }

    /**
     * 判断是否需要联网搜索
     */
    public boolean needsWebSearch(String userMessage) {
        String prompt = """
                判断以下问题是否需要实时网络搜索才能回答（回答 YES 或 NO）：
                %s
                """;

        String result = chatClient.prompt()
                .user(String.format(prompt, userMessage))
                .call()
                .content()
                .trim()
                .toUpperCase();

        return "YES".equals(result);
    }
}
