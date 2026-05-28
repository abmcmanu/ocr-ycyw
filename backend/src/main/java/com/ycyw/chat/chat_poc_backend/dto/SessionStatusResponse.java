package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class SessionStatusResponse {
    private UUID threadId;
    private String status;
    private String agentId;
    private String agentName;

    public SessionStatusResponse(UUID threadId, String status, String agentId, String agentName) {
        this.threadId = threadId;
        this.status = status;
        this.agentId = agentId;
        this.agentName = agentName;
    }

    public UUID getThreadId() { return threadId; }
    public String getStatus() { return status; }
    public String getAgentId() { return agentId; }
    public String getAgentName() { return agentName; }
}
