package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.AdvisorSessionDTO;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AdvisorSessionMapperTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdvisorSessionMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testToDtoWithFullCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        
        ChatThread thread = new ChatThread();
        thread.setId(threadId);
        thread.setCustomerId(customerId);
        thread.setSubject("Subject");
        thread.setStatus("waiting");
        thread.setCreatedAt(OffsetDateTime.now());

        Customer customer = new Customer("test@test.com", "John", "Doe");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        AdvisorSessionDTO dto = mapper.toDto(thread);

        assertEquals(threadId, dto.getId());
        assertEquals("Subject", dto.getSubject());
        assertEquals("John Doe", dto.getCustomerName());
        assertEquals("test@test.com", dto.getCustomerEmail());
    }

    @Test
    void testToDtoWithNoCustomer() {
        UUID customerId = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(UUID.randomUUID());
        thread.setCustomerId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        AdvisorSessionDTO dto = mapper.toDto(thread);

        assertEquals("Client " + customerId.toString().substring(0, 8), dto.getCustomerName());
    }

    @Test
    void testBuildDisplayName() {
        UUID id = UUID.randomUUID();
        assertEquals("John Doe", AdvisorSessionMapper.buildDisplayName("John", "Doe", null, id));
        assertEquals("John", AdvisorSessionMapper.buildDisplayName("John", null, null, id));
        assertEquals("Doe", AdvisorSessionMapper.buildDisplayName(null, "Doe", null, id));
        assertEquals("test@test.com", AdvisorSessionMapper.buildDisplayName(null, null, "test@test.com", id));
        assertEquals("Client " + id.toString().substring(0, 8), AdvisorSessionMapper.buildDisplayName(null, null, null, id));
    }
}
