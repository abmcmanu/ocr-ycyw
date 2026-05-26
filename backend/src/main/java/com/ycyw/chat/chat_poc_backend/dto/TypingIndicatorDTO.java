package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class TypingIndicatorDTO {
    
    private UUID threadId;
    private String userId;
    private String userType; // "customer" ou "agent"
    private boolean isTyping;

    public TypingIndicatorDTO() {}

    public TypingIndicatorDTO(UUID threadId, String userId, String userType, boolean isTyping) {
        this.threadId = threadId;
        this.userId = userId;
        this.userType = userType;
        this.isTyping = isTyping;
    }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }
}
