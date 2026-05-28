package com.ycyw.chat.chat_poc_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AdvisorAuthRequest {

    @NotBlank
    private String agentId;

    @NotBlank
    private String password;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
