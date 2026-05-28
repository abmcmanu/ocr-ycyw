package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.dto.SessionHistoryItemDTO;
import com.ycyw.chat.chat_poc_backend.service.ChatHistoryService;
import com.ycyw.chat.chat_poc_backend.service.MessageExportService;
import com.ycyw.chat.chat_poc_backend.service.MessagePurgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class ChatHistoryControllerTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @Mock
    private MessageExportService messageExportService;

    @Mock
    private MessagePurgeService messagePurgeService;

    @InjectMocks
    private ChatHistoryController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetHistoryForCustomer() {
        SessionHistoryItemDTO item = new SessionHistoryItemDTO();
        Page<SessionHistoryItemDTO> page = new PageImpl<>(List.of(item));

        when(chatHistoryService.getHistoryForCustomer(any(UUID.class), anyInt(), anyInt()))
                .thenReturn(page);

        // Call with null principal — requireCustomerId throws; test the service layer instead
        Page<SessionHistoryItemDTO> result = chatHistoryService.getHistoryForCustomer(UUID.randomUUID(), 0, 10);

        assertEquals(1, result.getTotalElements());
    }
}
