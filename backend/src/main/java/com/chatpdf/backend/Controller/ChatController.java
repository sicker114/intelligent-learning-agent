package com.chatpdf.backend.Controller;

import com.chatpdf.backend.Service.AgentOrchestratorService;
import com.chatpdf.backend.Service.SessionHistoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final AgentOrchestratorService agentOrchestratorService;
    private final SessionHistoryService sessionHistoryService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);//线程池

    public ChatController(AgentOrchestratorService agentOrchestratorService,
                         SessionHistoryService sessionHistoryService) {
        this.agentOrchestratorService = agentOrchestratorService;
        this.sessionHistoryService = sessionHistoryService;
    }

    /*
    接收数据
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> payload) {
        // 设置超长超时，防止断连
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String question = payload.get("message");
        String sessionId = payload.getOrDefault("sessionId", UUID.randomUUID().toString());

        System.out.println("[1] 收到问题: " + question + ", SessionId: " + sessionId);

        if (question == null || question.trim().isEmpty()) {
            emitter.completeWithError(new IllegalArgumentException("问题不能为空"));
            return emitter;
        }//空值判断

        //保存用户消息到 Redis
        sessionHistoryService.saveUserMessage(sessionId, question);

        executor.execute(() -> {
            try {
                // --- Agent 模式：智能处理 ---
                System.out.println("[Agent] 开始智能处理...");
                String agentResponse = agentOrchestratorService.processRequest(question, sessionId);

                // --- 流式输出（模拟）---
                System.out.println("[Agent] 开始向前端推送响应...");
                int chunkSize = 15;
                int count = 0;
                for (int i = 0; i < agentResponse.length(); i += chunkSize) {
                    String chunk = agentResponse.substring(i, Math.min(i + chunkSize, agentResponse.length()));
                    emitter.send(chunk);
                    count++;

                    // 模拟流式延迟，提升用户体验
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println("[Agent] 响应结束，共推送 " + count + " 块。");

                //保存AI回复到 Redis
                if (!agentResponse.isEmpty()) {
                    sessionHistoryService.saveAssistantMessage(sessionId, agentResponse);
                }

                emitter.complete();

            } catch (Exception e) {
                //关键：捕获所有异常并打印堆栈，同时发给前端
                System.err.println("[Error] 处理失败: " + e.getMessage());
                e.printStackTrace();
                try {
                    emitter.send("服务异常: " + e.getMessage());
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
