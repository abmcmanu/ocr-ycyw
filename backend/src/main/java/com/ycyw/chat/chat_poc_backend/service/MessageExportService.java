package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class MessageExportService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CustomerRepository customerRepository;

    public MessageExportService(
            ChatThreadRepository chatThreadRepository,
            ChatMessageRepository chatMessageRepository,
            CustomerRepository customerRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.customerRepository = customerRepository;
    }

    public ExportPayload exportThread(UUID threadId, UUID customerId) {
        ChatThread thread = chatThreadRepository.findByIdAndCustomerId(threadId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        List<ChatMessage> messages = chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        StringBuilder sb = new StringBuilder();
        sb.append("YCYW Support — Export de conversation\n");
        sb.append("=====================================\n");
        sb.append("Client : ").append(customer.getFirstName()).append(' ').append(customer.getLastName()).append('\n');
        sb.append("Email  : ").append(customer.getEmail()).append('\n');
        sb.append("Sujet  : ").append(thread.getSubject()).append('\n');
        sb.append("Statut : ").append(thread.getStatus()).append('\n');
        sb.append("ID     : ").append(thread.getId()).append("\n\n");

        if (messages.isEmpty()) {
            sb.append("(Aucun message enregistré)\n");
        } else {
            for (ChatMessage msg : messages) {
                String time = msg.getCreatedAt() != null ? FORMAT.format(msg.getCreatedAt()) : "—";
                sb.append('[').append(time).append("] ");
                sb.append(msg.getSenderName() != null ? msg.getSenderName() : msg.getSenderType());
                sb.append(" : ").append(msg.getMessageText()).append('\n');
            }
        }

        String filename = "ycyw-chat-" + threadId.toString().substring(0, 8) + ".txt";
        return new ExportPayload(sb.toString(), filename);
    }

    public record ExportPayload(String content, String filename) {}
}
