package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionRequest;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionResponse;
import com.ycyw.chat.chat_poc_backend.dto.SessionStatusResponse;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Sessions", description = "Endpoints de gestion des sessions de tchat")
public class ChatSessionController {

    private final QueueService queueService;
    private final ChatThreadRepository chatThreadRepository;

    public ChatSessionController(QueueService queueService, ChatThreadRepository chatThreadRepository) {
        this.queueService = queueService;
        this.chatThreadRepository = chatThreadRepository;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Créer une nouvelle session de tchat et entrer dans la file d'attente")
    public ResponseEntity<ChatSessionResponse> createSession(@RequestBody ChatSessionRequest request) {
        ChatSessionResponse response = queueService.createSession(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{threadId}/status")
    @Operation(summary = "Statut d'une session (fallback si WebSocket indisponible)")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(@PathVariable UUID threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
        String agentId = thread.getAssignedAgentId();
        return ResponseEntity.ok(new SessionStatusResponse(
                thread.getId(),
                thread.getStatus(),
                agentId,
                agentId
        ));
    }
}
