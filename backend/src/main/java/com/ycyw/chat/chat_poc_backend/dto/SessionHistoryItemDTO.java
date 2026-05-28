package com.ycyw.chat.chat_poc_backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SessionHistoryItemDTO {
    private UUID threadId;
    private String subject;
    private String status;
    private OffsetDateTime createdAt;
    private long messageCount;

    public SessionHistoryItemDTO() {}

    public SessionHistoryItemDTO(UUID threadId, String subject, String status,
                                 OffsetDateTime createdAt, long messageCount) {
        this.threadId = threadId;
        this.subject = subject;
        this.status = status;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
    }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public long getMessageCount() { return messageCount; }
    public void setMessageCount(long messageCount) { this.messageCount = messageCount; }
}
