package com.ycyw.chat.chat_poc_backend.repository;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {
    
    // Trouver toutes les sessions en attente d'un agent
    List<ChatThread> findByStatusOrderByCreatedAtAsc(String status);
    
    // Trouver les sessions actives assignées à un agent
    List<ChatThread> findByAssignedAgentIdAndStatus(String assignedAgentId, String status);

    Page<ChatThread> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Optional<ChatThread> findByIdAndCustomerId(UUID id, UUID customerId);
}
