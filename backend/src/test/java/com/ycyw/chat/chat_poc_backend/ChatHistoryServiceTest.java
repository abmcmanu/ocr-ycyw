package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.SessionHistoryItemDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import com.ycyw.chat.chat_poc_backend.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatHistoryServiceTest {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void historyIsSortedByThreadCreatedAtDescAndPaged() {
        Customer customer = customerRepository.save(new Customer("client@example.com", "Jean", "Dupont"));
        UUID customerId = customer.getId();

        OffsetDateTime older = OffsetDateTime.now().minusDays(2);
        OffsetDateTime newer = OffsetDateTime.now().minusDays(1);

        ChatThread olderThread = new ChatThread();
        olderThread.setCustomerId(customerId);
        olderThread.setSubject("Thread older");
        olderThread.setStatus("waiting");
        olderThread.setCreatedAt(older);
        chatThreadRepository.save(olderThread);

        ChatThread newerThread = new ChatThread();
        newerThread.setCustomerId(customerId);
        newerThread.setSubject("Thread newer");
        newerThread.setStatus("waiting");
        newerThread.setCreatedAt(newer);
        chatThreadRepository.save(newerThread);

        ChatMessage m1 = new ChatMessage();
        m1.setThreadId(olderThread.getId());
        m1.setSenderType("customer");
        m1.setSenderId("c1");
        m1.setSenderName("Jean");
        m1.setMessageText("older message");
        m1.setCreatedAt(older.plusHours(1));
        chatMessageRepository.save(m1);

        ChatMessage m2 = new ChatMessage();
        m2.setThreadId(olderThread.getId());
        m2.setSenderType("advisor");
        m2.setSenderId("a1");
        m2.setSenderName("Sophie");
        m2.setMessageText("older message 2");
        m2.setCreatedAt(older.plusHours(2));
        chatMessageRepository.save(m2);

        ChatMessage m3 = new ChatMessage();
        m3.setThreadId(newerThread.getId());
        m3.setSenderType("customer");
        m3.setSenderId("c1");
        m3.setSenderName("Jean");
        m3.setMessageText("newer message");
        m3.setCreatedAt(newer.plusHours(1));
        chatMessageRepository.save(m3);

        var page = chatHistoryService.getHistoryForCustomer(customerId, 0, 10);
        assertEquals(2, page.getTotalElements());

        List<SessionHistoryItemDTO> items = page.getContent();
        assertEquals(2, items.size());
        assertEquals(newerThread.getId(), items.get(0).getThreadId(), "Newest thread must appear first");
        assertEquals(1, items.get(0).getMessageCount());
        assertEquals(2, items.get(1).getMessageCount());
    }
}

