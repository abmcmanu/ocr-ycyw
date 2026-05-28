package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.domain.PurgeAuditLog;
import com.ycyw.chat.chat_poc_backend.dto.PurgeResultDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.PurgeAuditLogRepository;
import com.ycyw.chat.chat_poc_backend.service.MessagePurgeService;
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
class MessagePurgeServiceTest {

    @Autowired
    private MessagePurgeService messagePurgeService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PurgeAuditLogRepository purgeAuditLogRepository;

    @Test
    void dryRunDoesNotDeleteButCounts() {
        UUID threadId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ChatMessage oldMsg = new ChatMessage();
        oldMsg.setThreadId(threadId);
        oldMsg.setSenderType("customer");
        oldMsg.setSenderId("c1");
        oldMsg.setSenderName("Jean");
        oldMsg.setMessageText("old");
        oldMsg.setCreatedAt(now.minusDays(1));
        chatMessageRepository.save(oldMsg);

        ChatMessage newMsg = new ChatMessage();
        newMsg.setThreadId(threadId);
        newMsg.setSenderType("customer");
        newMsg.setSenderId("c1");
        newMsg.setSenderName("Jean");
        newMsg.setMessageText("new");
        newMsg.setCreatedAt(now.plusDays(1));
        chatMessageRepository.save(newMsg);

        assertEquals(2, chatMessageRepository.count());
        PurgeResultDTO dry = messagePurgeService.purge(true, "manual", "tester");
        assertTrue(dry.isDryRun());
        assertEquals(1, dry.getMessagesDeleted());
        assertEquals(2, chatMessageRepository.count(), "dryRun must not delete");
        assertEquals(0, purgeAuditLogRepository.count(), "dryRun must not write audit log");
    }

    @Test
    void purgeDeletesAndWritesAuditLog() {
        UUID threadId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ChatMessage oldMsg = new ChatMessage();
        oldMsg.setThreadId(threadId);
        oldMsg.setSenderType("customer");
        oldMsg.setSenderId("c1");
        oldMsg.setSenderName("Jean");
        oldMsg.setMessageText("old");
        oldMsg.setCreatedAt(now.minusDays(1));
        chatMessageRepository.save(oldMsg);

        ChatMessage newMsg = new ChatMessage();
        newMsg.setThreadId(threadId);
        newMsg.setSenderType("customer");
        newMsg.setSenderId("c1");
        newMsg.setSenderName("Jean");
        newMsg.setMessageText("new");
        newMsg.setCreatedAt(now.plusDays(1));
        chatMessageRepository.save(newMsg);

        PurgeResultDTO purge = messagePurgeService.purge(false, "manual", "tester");
        assertFalse(purge.isDryRun());
        assertEquals(1, purge.getMessagesDeleted());
        assertEquals(1, chatMessageRepository.count(), "Exactly one old message must remain deleted");
        assertNotNull(purge.getAuditLogId());

        PurgeAuditLog audit = purgeAuditLogRepository.findById(purge.getAuditLogId()).orElseThrow();
        assertEquals("manual", audit.getTriggerType());
        assertEquals(1, audit.getMessagesDeleted());
    }
}

