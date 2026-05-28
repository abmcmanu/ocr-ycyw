package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.PurgeAuditLog;
import com.ycyw.chat.chat_poc_backend.dto.PurgeResultDTO;
import com.ycyw.chat.chat_poc_backend.repository.ChatMessageRepository;
import com.ycyw.chat.chat_poc_backend.repository.PurgeAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class MessagePurgeService {

    private static final Logger log = LoggerFactory.getLogger(MessagePurgeService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final PurgeAuditLogRepository purgeAuditLogRepository;

    @Value("${chat.retention-months:12}")
    private int retentionMonths;

    public MessagePurgeService(
            ChatMessageRepository chatMessageRepository,
            PurgeAuditLogRepository purgeAuditLogRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.purgeAuditLogRepository = purgeAuditLogRepository;
    }

    public OffsetDateTime computeCutoff() {
        return OffsetDateTime.now().minusMonths(retentionMonths);
    }

    @Transactional
    public PurgeResultDTO purge(boolean dryRun, String triggerType, String triggeredBy) {
        OffsetDateTime cutoff = computeCutoff();
        long toDelete = chatMessageRepository.countByCreatedAtBefore(cutoff);

        PurgeResultDTO result = new PurgeResultDTO();
        result.setDryRun(dryRun);
        result.setCutoffDate(cutoff);
        result.setMessagesDeleted(toDelete);

        if (dryRun) {
            log.info("Purge simulation : {} messages antérieurs à {}", toDelete, cutoff);
            return result;
        }

        int deleted = chatMessageRepository.deleteByCreatedAtBefore(cutoff);
        result.setMessagesDeleted(deleted);

        PurgeAuditLog audit = new PurgeAuditLog();
        audit.setCutoffDate(cutoff);
        audit.setMessagesDeleted(deleted);
        audit.setTriggerType(triggerType);
        audit.setTriggeredBy(triggeredBy);
        audit.setDetails("Purge RGPD messages > " + retentionMonths + " mois");
        purgeAuditLogRepository.save(audit);
        result.setAuditLogId(audit.getId());

        log.info("Purge RGPD exécutée : {} messages supprimés (cutoff={})", deleted, cutoff);
        return result;
    }
}
