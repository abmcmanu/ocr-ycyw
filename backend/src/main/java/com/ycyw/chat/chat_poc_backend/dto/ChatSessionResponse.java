package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class ChatSessionResponse {
    private UUID threadId;
    private String status;
    private int queuePosition;
    private int estimatedWaitTimeMinutes;

    public ChatSessionResponse() {}

    public ChatSessionResponse(UUID threadId, String status, int queuePosition, int estimatedWaitTimeMinutes) {
        this.threadId = threadId;
        this.status = status;
        this.queuePosition = queuePosition;
        this.estimatedWaitTimeMinutes = estimatedWaitTimeMinutes;
    }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public int getEstimatedWaitTimeMinutes() { return estimatedWaitTimeMinutes; }
    public void setEstimatedWaitTimeMinutes(int estimatedWaitTimeMinutes) { this.estimatedWaitTimeMinutes = estimatedWaitTimeMinutes; }
}
