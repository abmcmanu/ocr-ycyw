package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.dto.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoTests {

    @Test
    void testChatSessionRequest() {
        ChatSessionRequest dto = new ChatSessionRequest();
        dto.setFirstName("Jean");
        dto.setLastName("Dupont");
        dto.setEmail("jean@example.com");
        dto.setSubject("Test");

        assertEquals("Jean", dto.getFirstName());
        assertEquals("Dupont", dto.getLastName());
        assertEquals("jean@example.com", dto.getEmail());
        assertEquals("Test", dto.getSubject());
    }

    @Test
    void testSessionStatusDTO() {
        UUID id = UUID.randomUUID();
        SessionStatusDTO dto = new SessionStatusDTO(id, "in_progress", "agent1", "Agent One");
        
        assertEquals(id, dto.getThreadId());
        assertEquals("in_progress", dto.getStatus());
        assertEquals("agent1", dto.getAgentId());
        assertEquals("Agent One", dto.getAgentName());

        dto.setStatus("closed");
        assertEquals("closed", dto.getStatus());
    }

    @Test
    void testAdvisorSessionDTO() {
        AdvisorSessionDTO dto = new AdvisorSessionDTO();
        dto.setId(UUID.randomUUID());
        dto.setCustomerName("Jean");
        dto.setSubject("Test");
        dto.setStatus("waiting");
        
        assertNotNull(dto.getId());
        assertEquals("Jean", dto.getCustomerName());
        assertEquals("waiting", dto.getStatus());
    }

    @Test
    void testChatMessageDTO() {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setThreadId(UUID.randomUUID());
        dto.setSenderType("customer");
        dto.setMessageText("Hello");
        dto.setMessageType("text");
        
        assertNotNull(dto.getThreadId());
        assertEquals("customer", dto.getSenderType());
        assertEquals("Hello", dto.getMessageText());
    }

    @Test
    void testTypingIndicatorDTO() {
        TypingIndicatorDTO dto = new TypingIndicatorDTO();
        dto.setThreadId(UUID.randomUUID());
        dto.setUserId("user1");
        dto.setUserType("customer");
        dto.setTyping(true);

        assertNotNull(dto.getThreadId());
        assertEquals("user1", dto.getUserId());
        assertEquals("customer", dto.getUserType());
        assertEquals(true, dto.isTyping());
    }

    @Test
    void testAuthResponses() {
        AuthTokenResponse res = new AuthTokenResponse();
        res.setAccessToken("token123");
        res.setTokenType("bearer");
        assertEquals("token123", res.getAccessToken());
        assertEquals("bearer", res.getTokenType());

        ClientAuthRequest cReq = new ClientAuthRequest();
        cReq.setEmail("test@test.com");
        assertEquals("test@test.com", cReq.getEmail());

        AdvisorAuthRequest aReq = new AdvisorAuthRequest();
        aReq.setAgentId("a1");
        aReq.setPassword("p1");
        assertEquals("a1", aReq.getAgentId());
    }
}
