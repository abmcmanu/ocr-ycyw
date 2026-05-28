package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.dto.AdvisorSessionDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.service.AdvisorSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdvisorControllerTest {

    @Mock
    private ChatThreadRepository chatThreadRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AdvisorSessionMapper sessionMapper;

    @InjectMocks
    private AdvisorController advisorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWaitQueue() {
        ChatThread thread = new ChatThread();
        when(chatThreadRepository.findByStatusOrderByCreatedAtAsc("waiting")).thenReturn(List.of(thread));
        when(sessionMapper.toDto(any())).thenReturn(new AdvisorSessionDTO());

        ResponseEntity<List<AdvisorSessionDTO>> response = advisorController.getWaitQueue();
        
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetActiveSessions() {
        ChatThread thread = new ChatThread();
        when(chatThreadRepository.findByAssignedAgentIdAndStatus("agent1", "in_progress")).thenReturn(List.of(thread));
        when(sessionMapper.toDto(any())).thenReturn(new AdvisorSessionDTO());

        ResponseEntity<List<AdvisorSessionDTO>> response = advisorController.getActiveSessions("agent1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testAssignSession() {
        UUID id = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(id);
        thread.setStatus("waiting");

        when(chatThreadRepository.findById(id)).thenReturn(Optional.of(thread));
        when(chatThreadRepository.save(any(ChatThread.class))).thenReturn(thread);
        when(sessionMapper.toDto(any())).thenReturn(new AdvisorSessionDTO());

        ResponseEntity<?> response = advisorController.assignSession(id, "agent1", "Agent One");

        assertEquals(200, response.getStatusCode().value());
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void testCloseSession() {
        UUID id = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(id);
        thread.setStatus("in_progress");

        when(chatThreadRepository.findById(id)).thenReturn(Optional.of(thread));
        when(chatThreadRepository.save(any(ChatThread.class))).thenReturn(thread);

        ResponseEntity<ChatThread> response = advisorController.closeSession(id, "agent1", "Agent One");

        assertEquals(200, response.getStatusCode().value());
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }
}
