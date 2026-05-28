package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.config.RedisConfig;
import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;
import com.ycyw.chat.chat_poc_backend.dto.SessionStatusDTO;
import com.ycyw.chat.chat_poc_backend.dto.TypingIndicatorDTO;
import com.ycyw.chat.chat_poc_backend.metrics.ChatMetrics;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.util.MessageSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMetrics chatMetrics;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            ChatThreadRepository chatThreadRepository,
            RedisTemplate<String, Object> redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            ChatMetrics chatMetrics) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatThreadRepository = chatThreadRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.chatMetrics = chatMetrics;
    }

    public void processAndBroadcastMessage(ChatMessageDTO dto) {
        if (isThreadClosed(dto.getThreadId())) {
            logger.warn("Message ignoré : le thread {} est clôturé", dto.getThreadId());
            notifyThreadClosed(dto.getThreadId());
            return;
        }

        // 1. Persister le message en DB
        ChatMessage message = new ChatMessage();
        message.setThreadId(dto.getThreadId());
        message.setSenderType(dto.getSenderType());
        message.setSenderId(dto.getSenderId());
        message.setSenderName(dto.getSenderName());
        message.setMessageText(MessageSanitizer.escapeForStorage(dto.getMessageText()));
        message.setMessageType(dto.getMessageType());
        message.setCreatedAt(OffsetDateTime.now());
        message.setRead(false);
        
        chatMessageRepository.save(message);
        chatMetrics.incrementMessages();
        logger.debug("Message sauvegardé en base de données pour le thread {}", dto.getThreadId());

        // 2. Diffuser sur Redis Pub/Sub pour le multi-instances
        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, dto);
    }

    public void broadcastTypingIndicator(TypingIndicatorDTO dto) {
        if (isThreadClosed(dto.getThreadId())) {
            return;
        }
        redisTemplate.convertAndSend(RedisConfig.TYPING_TOPIC, dto);
    }

    private boolean isThreadClosed(UUID threadId) {
        return chatThreadRepository.findById(threadId)
                .map(thread -> "closed".equals(thread.getStatus()))
                .orElse(true);
    }

    private void notifyThreadClosed(UUID threadId) {
        SessionStatusDTO statusEvent = new SessionStatusDTO(threadId, "closed", null, null);
        messagingTemplate.convertAndSend("/topic/session/" + threadId + "/status", statusEvent);
    }
}
