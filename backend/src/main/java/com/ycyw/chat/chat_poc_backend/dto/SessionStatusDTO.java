package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class SessionStatusDTO {
    private UUID threadId;
    private String status;
    private String agentId;
    private String agentName;

    public SessionStatusDTO() {}

    public SessionStatusDTO(UUID threadId, String status, String agentId, String agentName) {
        this.threadId = threadId;
        this.status = status;
        this.agentId = agentId;
        this.agentName = agentName;
    }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
}
