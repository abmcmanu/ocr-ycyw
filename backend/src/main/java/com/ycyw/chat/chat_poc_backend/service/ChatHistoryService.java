package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.dto.SessionHistoryItemDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatHistoryService {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatHistoryService(
            ChatThreadRepository chatThreadRepository,
            ChatMessageRepository chatMessageRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public Page<SessionHistoryItemDTO> getHistoryForCustomer(UUID customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return chatThreadRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toHistoryItem);
    }

    private SessionHistoryItemDTO toHistoryItem(ChatThread thread) {
        long count = chatMessageRepository.countByThreadId(thread.getId());
        return new SessionHistoryItemDTO(
                thread.getId(),
                thread.getSubject(),
                thread.getStatus(),
                thread.getCreatedAt(),
                count
        );
    }
}
