package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import com.ycyw.chat.chat_poc_backend.service.MessageExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MessageExportServiceTest {

    @Autowired
    private MessageExportService messageExportService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void exportProducesTxtContent() {
        Customer customer = customerRepository.save(new Customer("client@example.com", "Jean", "Dupont"));

        ChatThread thread = new ChatThread();
        thread.setCustomerId(customer.getId());
        thread.setSubject("Problème réservation");
        thread.setStatus("in_progress");
        thread.setCreatedAt(OffsetDateTime.now());
        chatThreadRepository.save(thread);

        ChatMessage msg = new ChatMessage();
        msg.setThreadId(thread.getId());
        msg.setSenderType("customer");
        msg.setSenderId("c1");
        msg.setSenderName("Jean");
        msg.setMessageText("Bonjour, j'ai un souci.");
        msg.setCreatedAt(OffsetDateTime.now());
        chatMessageRepository.save(msg);

        MessageExportService.ExportPayload payload =
                messageExportService.exportThread(thread.getId(), customer.getId());

        assertTrue(payload.content().contains("Client : Jean Dupont"));
        assertTrue(payload.content().contains("Email  : client@example.com"));
        assertTrue(payload.content().contains("Sujet  : Problème réservation"));
        assertTrue(payload.content().contains("Bonjour, j'ai un souci."));
        assertTrue(payload.filename().endsWith(".txt"));
    }
}

