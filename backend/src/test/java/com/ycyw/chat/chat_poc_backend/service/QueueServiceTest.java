package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionRequest;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionResponse;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    @Mock
    private ChatThreadRepository chatThreadRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateSessionNewCustomer() {
        ChatSessionRequest request = new ChatSessionRequest();
        request.setEmail("new@example.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setSubject("Help");

        when(customerRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        Customer savedCustomer = new Customer("new@example.com", "New", "User");
        savedCustomer.setId(UUID.randomUUID());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        ChatThread savedThread = new ChatThread();
        savedThread.setId(UUID.randomUUID());
        savedThread.setStatus("waiting");
        when(chatThreadRepository.save(any(ChatThread.class))).thenReturn(savedThread);

        when(chatThreadRepository.findByStatusOrderByCreatedAtAsc("waiting"))
                .thenReturn(List.of(savedThread));

        ChatSessionResponse response = queueService.createSession(request);

        assertNotNull(response);
        assertEquals(savedThread.getId(), response.getThreadId());
        assertEquals("waiting", response.getStatus());
        assertEquals(1, response.getQueuePosition());
        assertEquals(1, response.getEstimatedWaitTimeMinutes());

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(chatThreadRepository, times(1)).save(any(ChatThread.class));
    }

    @Test
    void testCreateSessionExistingCustomer() {
        ChatSessionRequest request = new ChatSessionRequest();
        request.setEmail("old@example.com");
        request.setFirstName("Old");
        request.setSubject("Help again");

        Customer existingCustomer = new Customer("old@example.com", "Old", "Man");
        existingCustomer.setId(UUID.randomUUID());
        when(customerRepository.findByEmail("old@example.com")).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        ChatThread savedThread = new ChatThread();
        savedThread.setId(UUID.randomUUID());
        savedThread.setStatus("waiting");
        when(chatThreadRepository.save(any(ChatThread.class))).thenReturn(savedThread);

        ChatThread oldThread = new ChatThread();
        oldThread.setId(UUID.randomUUID());
        oldThread.setStatus("waiting");

        when(chatThreadRepository.findByStatusOrderByCreatedAtAsc("waiting"))
                .thenReturn(List.of(oldThread, savedThread));

        ChatSessionResponse response = queueService.createSession(request);

        assertNotNull(response);
        assertEquals(2, response.getQueuePosition());
        assertEquals(2, response.getEstimatedWaitTimeMinutes());
    }
}
