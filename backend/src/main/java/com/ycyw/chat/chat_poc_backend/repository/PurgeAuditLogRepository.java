package com.ycyw.chat.chat_poc_backend.repository;

import com.ycyw.chat.chat_poc_backend.domain.PurgeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurgeAuditLogRepository extends JpaRepository<PurgeAuditLog, UUID> {
}
