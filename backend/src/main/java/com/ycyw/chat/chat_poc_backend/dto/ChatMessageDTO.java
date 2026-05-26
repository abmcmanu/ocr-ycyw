package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class ChatMessageDTO {
    
    private UUID threadId;
    private String senderType; // "customer" ou "agent"
    private String senderId;
    private String senderName;
    private String messageText;
    private String messageType; // "text", "system"

    public ChatMessageDTO() {}

    public ChatMessageDTO(UUID threadId, String senderType, String senderId, String senderName, String messageText, String messageType) {
        this.threadId = threadId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.messageType = messageType;
    }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}
