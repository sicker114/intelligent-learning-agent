package com.backend.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession implements Serializable {


    private static final long serialVersionUID = 1L;

    private String sessionId;
    private List<ChatMessage> messages;
    private long createTime;
    private long lastUpdateTime;

    public ChatSession(String sessionId) {
        this();
        this.sessionId = sessionId;
    }

    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.lastUpdateTime = System.currentTimeMillis();
    }


}