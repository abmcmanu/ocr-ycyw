package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.dto.ChatSessionRequest;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionResponse;
import com.ycyw.chat.chat_poc_backend.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatSessionControllerTest {

    @Mock
    private QueueService queueService;

    @InjectMocks
    private ChatSessionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateSession() {
        ChatSessionRequest req = new ChatSessionRequest();
        ChatSessionResponse res = new ChatSessionResponse(UUID.randomUUID(), "waiting", 1, 1);
        when(queueService.createSession(any(ChatSessionRequest.class))).thenReturn(res);

        ResponseEntity<ChatSessionResponse> response = controller.createSession(req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("waiting", response.getBody().getStatus());
    }
}
