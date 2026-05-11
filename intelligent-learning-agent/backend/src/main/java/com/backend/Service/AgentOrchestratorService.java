package com.backend.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentOrchestratorService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final IntentRecognitionService intentService;
    private final SessionHistoryService sessionHistoryService;
    private final ToolService toolService;

    public AgentOrchestratorService(ChatModel chatModel,
                                   VectorStore vectorStore,
                                   IntentRecognitionService intentService,
                                   SessionHistoryService sessionHistoryService,
                                   ToolService toolService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.vectorStore = vectorStore;
        this.intentService = intentService;
        this.sessionHistoryService = sessionHistoryService;
        this.toolService = toolService;
    }


    /**
     * Agent 主入口：根据意图智能处理用户请求
     */
    public String processRequest(String userMessage, String sessionId) {
        try {
            // 步骤1：意图识别
            String intent = intentService.recognizeIntent(userMessage);
            System.out.println("识别意图: " + intent);

            // 步骤2：检查是否需要工具调用
            if (needsToolCall(userMessage)) {
                System.out.println("检测到需要调用工具");
                return handleToolCall(userMessage);
            }

            // 步骤3：根据意图路由到不同处理器
            String result = switch (intent) {
                case "DOCUMENT_QUERY" -> handleDocumentQuery(userMessage, sessionId);
                case "GENERAL_CHAT" -> handleGeneralChat(userMessage, sessionId);
                case "SUMMARY" -> handleDocumentSummary(sessionId);
                case "COMPARISON" -> handleComparison(userMessage, sessionId);
                case "EXTRACT" -> handleInformationExtraction(userMessage, sessionId);
                default -> handleDocumentQuery(userMessage, sessionId);
            };

            return result != null ? result : "抱歉，我暂时无法回答这个问题。";

        } catch (Exception e) {
            System.err.println("Agent 处理失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }


    /**
     * 判断是否需要调用工具
     */
    private boolean needsToolCall(String message) {
        String lowerMsg = message.toLowerCase();
        return lowerMsg.contains("今天") ||
                lowerMsg.contains("日期") ||
                lowerMsg.contains("时间") ||
                lowerMsg.contains("几号") ||
                lowerMsg.contains("http") ||
                lowerMsg.contains("www.") ||
                lowerMsg.contains("搜索");
    }

    /**
     * 从消息中提取 URL
     */
    private String extractUrl(String message) {
        int httpIndex = message.indexOf("http");
        if (httpIndex != -1) {
            return message.substring(httpIndex).split("\\s")[0];
        }
        return null;
    }


    /**
     * 处理工具调用
     */
    private String handleToolCall(String message) {
        String lowerMsg = message.toLowerCase();

        // 工具1：获取当前日期时间
        if (lowerMsg.contains("今天") || lowerMsg.contains("日期") ||
            lowerMsg.contains("时间") || lowerMsg.contains("几号")) {
            return "当前日期：" + toolService.getCurrentDateTime();
        }

        // 工具2：网页内容抓取
        if (message.contains("http") || message.contains("www.")) {
            // 提取 URL
            String url = extractUrl(message);
            if (url != null) {
                String content = toolService.fetchWebContent(url);
                return "网页内容摘要：\n\n" + content;
            }
        }

        // 工具3：网页搜索（模拟）
        if (lowerMsg.contains("搜索") || lowerMsg.contains("查一下")) {
            String query = message.replace("搜索", "").replace("查一下", "").trim();
            return toolService.webSearch(query);
        }

        // 默认返回
        return null; // 返回 null 表示不使用工具，走正常流程
    }



    /**
     * 处理文档查询（学习答疑）
     */
    private String handleDocumentQuery(String question, String sessionId) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build()
        );

        if (docs == null || docs.isEmpty()) {
            return "未在学习资料中找到相关内容。\n\n建议：\n1. 换个问法试试\n2. 上传相关教材或课件\n3. 问我其他学习问题";
        }

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String systemPrompt = """
                你的名字叫李豪，是专业的学习助手。请基于提供的学习资料回答问题：
                - 用通俗易懂的语言解释概念
                - 适当举例帮助理解
                - 如果资料中没有相关信息，诚实地告诉学生
                - 鼓励学生深入思考
                """;
        String finalPrompt = systemPrompt + "\n\n学习资料：\n" + context + "\n\n学生问题：" + question;

        return chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
    }

    /**
     * 处理闲聊
     */
    private String handleGeneralChat(String message, String sessionId) {
        return chatClient.prompt()
                .system("""
                        你的名字叫李豪，是友好专业的学习助手。
                        - 保持对话自然流畅，适当使用emoji增加亲和力
                        - 鼓励学生认真学习
                        - 可以分享学习方法和建议
                        """)
                .user(message)
                .call()
                .content();
    }

    /**
     * 处理学习资料总结
     */
    private String handleDocumentSummary(String sessionId) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query("").topK(10).build()
        );

        if (docs == null || docs.isEmpty()) {
            return "当前没有可总结的学习资料。请先上传教材、课件或笔记。";
        }

        String fullText = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                请对以下学习资料进行全面总结，帮助学生复习：
                
                1.核心内容概述
                2.关键知识点（列出5-8个）
                3.重要公式或定理（如果有）
                4.考试重点预测
                5.复习建议
                
                学习资料内容：
                %s
                """;

        return chatClient.prompt()
                .user(String.format(prompt, fullText))
                .call()
                .content();
    }

    /**
     * 处理对比分析
     */
    private String handleComparison(String question, String sessionId) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(5).build()
        );

        if (docs == null || docs.isEmpty()) {
            return "未找到可用于对比的学习内容。";
        }

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                请基于以下学习资料进行对比分析，使用清晰的表格或列表格式呈现：
                
                学习资料：
                %s
                
                对比要求：%s
                
                帮助学生理解不同概念或方法的异同。
                """;

        return chatClient.prompt()
                .user(String.format(prompt, context, question))
                .call()
                .content();
    }

    /**
     * 处理信息提取（知识点提取）
     */
    private String handleInformationExtraction(String question, String sessionId) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(5).build()
        );

        if (docs == null || docs.isEmpty()) {
            return "📚 未找到可提取的信息。";
        }

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                请从以下学习资料中提取 requested 信息，以结构化格式返回（使用Markdown列表或表格）：
                
                学习资料：
                %s
                
                提取要求：%s
                
                帮助学生快速定位重点内容。
                """;

        return chatClient.prompt()
                .user(String.format(prompt, context, question))
                .call()
                .content();
    }
}
