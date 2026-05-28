package com.ycyw.chat.chat_poc_backend.repository;

import com.ycyw.chat.chat_poc_backend.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByThreadIdOrderByCreatedAtAsc(UUID threadId);

    long countByThreadId(UUID threadId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChatMessage m WHERE m.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") OffsetDateTime cutoff);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.createdAt < :cutoff")
    long countByCreatedAtBefore(@Param("cutoff") OffsetDateTime cutoff);
}
