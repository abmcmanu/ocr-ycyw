package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.dto.AdvisorSessionDTO;
import com.ycyw.chat.chat_poc_backend.dto.SessionStatusDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.service.AdvisorSessionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.ycyw.chat.chat_poc_backend.dto.ChatMessageDTO;

@RestController
@RequestMapping("/api/advisor")
@Tag(name = "Advisor Console", description = "Endpoints pour la console des conseillers")
public class AdvisorController {

    private final ChatThreadRepository chatThreadRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AdvisorSessionMapper sessionMapper;

    public AdvisorController(
            ChatThreadRepository chatThreadRepository,
            SimpMessagingTemplate messagingTemplate,
            AdvisorSessionMapper sessionMapper) {
        this.chatThreadRepository = chatThreadRepository;
        this.messagingTemplate = messagingTemplate;
        this.sessionMapper = sessionMapper;
    }

    @GetMapping("/queue")
    @Operation(summary = "Récupérer la liste d'attente (sessions WAITING)")
    public ResponseEntity<List<AdvisorSessionDTO>> getWaitQueue() {
        List<AdvisorSessionDTO> queue = chatThreadRepository.findByStatusOrderByCreatedAtAsc("waiting")
                .stream()
                .map(sessionMapper::toDto)
                .toList();
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/active/{agentId}")
    @Operation(summary = "Récupérer les sessions actives d'un agent")
    public ResponseEntity<List<AdvisorSessionDTO>> getActiveSessions(@PathVariable String agentId) {
        List<AdvisorSessionDTO> sessions = chatThreadRepository
                .findByAssignedAgentIdAndStatus(agentId, "in_progress")
                .stream()
                .map(sessionMapper::toDto)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions/{threadId}/assign")
    @Operation(summary = "Prendre en charge une session par un agent")
    public ResponseEntity<?> assignSession(
            @PathVariable UUID threadId,
            @RequestParam String agentId,
            @RequestParam(required = false) String agentName) {
        return chatThreadRepository.findById(threadId).map(thread -> {
            if ("closed".equals(thread.getStatus())) {
                return ResponseEntity.<ChatThread>badRequest().build();
            }
            thread.setStatus("in_progress");
            thread.setAssignedAgentId(agentId);
            thread.setUpdatedAt(OffsetDateTime.now());
            ChatThread updated = chatThreadRepository.save(thread);

            SessionStatusDTO statusEvent = new SessionStatusDTO(
                    threadId,
                    "in_progress",
                    agentId,
                    agentName != null ? agentName : agentId
            );
            messagingTemplate.convertAndSend("/topic/session/" + threadId + "/status", statusEvent);
            messagingTemplate.convertAndSend("/topic/queue", "Queue Updated");

            return ResponseEntity.ok(sessionMapper.toDto(updated));
        }).orElse(ResponseEntity.<ChatThread>notFound().build());
    }

    @PostMapping("/sessions/{threadId}/close")
    @Operation(summary = "Clôturer une session de tchat")
    public ResponseEntity<ChatThread> closeSession(
            @PathVariable UUID threadId,
            @RequestParam String agentId,
            @RequestParam(required = false) String agentName) {
        return chatThreadRepository.findById(threadId).map(thread -> {
            if ("closed".equals(thread.getStatus())) {
                return ResponseEntity.ok(thread);
            }
            thread.setStatus("closed");
            thread.setUpdatedAt(OffsetDateTime.now());
            ChatThread updated = chatThreadRepository.save(thread);

            SessionStatusDTO statusEvent = new SessionStatusDTO(
                    threadId,
                    "closed",
                    agentId,
                    agentName != null ? agentName : agentId
            );
            messagingTemplate.convertAndSend("/topic/session/" + threadId + "/status", statusEvent);
            messagingTemplate.convertAndSend("/topic/queue", "Queue Updated");

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.<ChatThread>notFound().build());
    }
}
