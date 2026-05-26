package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.dto.ChatSessionRequest;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionResponse;
import com.ycyw.chat.chat_poc_backend.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Sessions", description = "Endpoints de gestion des sessions de tchat")
public class ChatSessionController {

    private final QueueService queueService;

    public ChatSessionController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Créer une nouvelle session de tchat et entrer dans la file d'attente")
    public ResponseEntity<ChatSessionResponse> createSession(@RequestBody ChatSessionRequest request) {
        ChatSessionResponse response = queueService.createSession(request);
        return ResponseEntity.ok(response);
    }
}
