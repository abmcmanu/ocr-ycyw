package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;
import com.ycyw.chat.chat_poc_backend.metrics.ChatMetrics;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatThreadRepository chatThreadRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMetrics chatMetrics;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAndBroadcastMessage() {
        UUID threadId = UUID.randomUUID();
        
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setThreadId(threadId);
        dto.setSenderType("customer");
        dto.setSenderId("user1");
        dto.setSenderName("User One");
        dto.setMessageText("Hello world");
        dto.setMessageType("text");

        ChatThread thread = new ChatThread();
        thread.setId(threadId);
        thread.setStatus("in_progress");

        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setId(UUID.randomUUID());
        savedMessage.setMessageText("Hello world");
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        chatService.processAndBroadcastMessage(dto);

        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(redisTemplate, times(1)).convertAndSend(anyString(), any(ChatMessageDTO.class));
    }
}
