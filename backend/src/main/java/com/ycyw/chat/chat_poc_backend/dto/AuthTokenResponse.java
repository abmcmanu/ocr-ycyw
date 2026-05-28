package com.ycyw.chat.chat_poc_backend.dto;

import java.util.UUID;

public class AuthTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String role;
    private UUID customerId;
    private String agentId;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}
