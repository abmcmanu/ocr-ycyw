package com.ycyw.chat.chat_poc_backend.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;
import com.ycyw.chat.chat_poc_backend.dto.TypingIndicatorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

@Configuration
public class RedisMessageSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    @Bean
    public MessageListenerAdapter chatMessageListenerAdapter(SimpMessagingTemplate messagingTemplate) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            try {
                // Désérialiser le message + Spring cache parfois la structure, l'ObjectMapper résout proprement.
                String body = new String(message.getBody());
                // Enlever les guillemets si c'est une string encodée JSON
                if (body.startsWith("\"") && body.endsWith("\"")) {
                    body = body.substring(1, body.length() - 1).replace("\\\"", "\"");
                }
                
                ChatMessageDTO dto = objectMapper.readValue(body, ChatMessageDTO.class);
                logger.debug("Reçu message Redis pour le thread {}: {}", dto.getThreadId(), dto.getMessageText());
                
                // Relayer vers le topic STOMP concerné
                messagingTemplate.convertAndSend("/topic/session/" + dto.getThreadId(), dto);
            } catch (IOException e) {
                logger.error("Erreur de désérialisation du message Redis", e);
            }
        });
    }

    @Bean
    public MessageListenerAdapter typingIndicatorListenerAdapter(SimpMessagingTemplate messagingTemplate) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            try {
                String body = new String(message.getBody());
                if (body.startsWith("\"") && body.endsWith("\"")) {
                    body = body.substring(1, body.length() - 1).replace("\\\"", "\"");
                }
                
                TypingIndicatorDTO dto = objectMapper.readValue(body, TypingIndicatorDTO.class);
                logger.debug("Reçu typing indicator Redis pour le thread {}", dto.getThreadId());
                
                // Relayer vers le topic STOMP concerné
                messagingTemplate.convertAndSend("/topic/session/" + dto.getThreadId() + "/typing", dto);
            } catch (IOException e) {
                logger.error("Erreur de désérialisation du typing indicator Redis", e);
            }
        });
    }
}
