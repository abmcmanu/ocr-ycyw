package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.config.RedisConfig;
import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;
import com.ycyw.chat.chat_poc_backend.dto.TypingIndicatorDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ChatService(ChatMessageRepository chatMessageRepository, RedisTemplate<String, Object> redisTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.redisTemplate = redisTemplate;
    }

    public void processAndBroadcastMessage(ChatMessageDTO dto) {
        // 1. Persister le message en DB
        ChatMessage message = new ChatMessage();
        message.setThreadId(dto.getThreadId());
        message.setSenderType(dto.getSenderType());
        message.setSenderId(dto.getSenderId());
        message.setSenderName(dto.getSenderName());
        message.setMessageText(dto.getMessageText());
        message.setMessageType(dto.getMessageType());
        message.setCreatedAt(OffsetDateTime.now());
        message.setRead(false);
        
        chatMessageRepository.save(message);
        logger.debug("Message sauvegardé en base de données pour le thread {}", dto.getThreadId());

        // 2. Diffuser sur Redis Pub/Sub pour le multi-instances
        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, dto);
    }

    public void broadcastTypingIndicator(TypingIndicatorDTO dto) {
        // Pas de persistance pour les indicateurs de frappe, on diffuse directement
        redisTemplate.convertAndSend(RedisConfig.TYPING_TOPIC, dto);
    }
}
