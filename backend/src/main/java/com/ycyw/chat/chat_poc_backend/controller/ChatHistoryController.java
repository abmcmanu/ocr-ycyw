package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.dto.PurgeResultDTO;
import com.ycyw.chat.chat_poc_backend.dto.SessionHistoryItemDTO;
import com.ycyw.chat.chat_poc_backend.security.UserPrincipal;
import com.ycyw.chat.chat_poc_backend.service.ChatHistoryService;
import com.ycyw.chat.chat_poc_backend.service.MessageExportService;
import com.ycyw.chat.chat_poc_backend.service.MessagePurgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat/sessions")
@Tag(name = "Historique & Export", description = "US-25 historique client, export TXT, purge RGPD")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;
    private final MessageExportService messageExportService;
    private final MessagePurgeService messagePurgeService;

    public ChatHistoryController(
            ChatHistoryService chatHistoryService,
            MessageExportService messageExportService,
            MessagePurgeService messagePurgeService) {
        this.chatHistoryService = chatHistoryService;
        this.messageExportService = messageExportService;
        this.messagePurgeService = messagePurgeService;
    }

    @GetMapping("/history")
    @Operation(summary = "Historique des conversations du client connecté (paginé, tri date desc)")
    public Page<SessionHistoryItemDTO> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID customerId = requireCustomerId(principal);
        return chatHistoryService.getHistoryForCustomer(customerId, page, size);
    }

    @GetMapping("/{threadId}/export")
    @Operation(summary = "Exporter une conversation en fichier texte")
    public ResponseEntity<byte[]> export(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID threadId) {
        UUID customerId = requireCustomerId(principal);
        MessageExportService.ExportPayload payload =
                messageExportService.exportThread(threadId, customerId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + payload.filename() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(payload.content().getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/admin/purge")
    @Operation(summary = "Purge manuelle RGPD (simulation ou exécution)")
    public PurgeResultDTO manualPurge(
            @RequestParam(defaultValue = "false") boolean dryRun,
            @AuthenticationPrincipal UserPrincipal principal) {
        String by = principal != null ? principal.getUsername() : "anonymous";
        return messagePurgeService.purge(dryRun, "manual", by);
    }

    private UUID requireCustomerId(UserPrincipal principal) {
        if (principal == null || principal.getCustomerId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Client non authentifié");
        }
        return principal.getCustomerId();
    }
}
