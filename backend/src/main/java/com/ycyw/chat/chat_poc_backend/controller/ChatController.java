package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;
import com.ycyw.chat.chat_poc_backend.dto.TypingIndicatorDTO;
import com.ycyw.chat.chat_poc_backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Reçoit un message envoyé par un client via le canal WebSocket.
     * Le endpoint complet est `/app/chat.send`.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        chatService.processAndBroadcastMessage(chatMessage);
    }

    /**
     * Reçoit l'indicateur "en train d'écrire" d'un client.
     * Le endpoint complet est `/app/chat.typing`.
     */
    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(@Payload TypingIndicatorDTO typingIndicator) {
        chatService.broadcastTypingIndicator(typingIndicator);
    }
}
