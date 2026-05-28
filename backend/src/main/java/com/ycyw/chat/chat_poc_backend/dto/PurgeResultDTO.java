package com.ycyw.chat.chat_poc_backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PurgeResultDTO {
    private boolean dryRun;
    private OffsetDateTime cutoffDate;
    private long messagesDeleted;
    private UUID auditLogId;

    public boolean isDryRun() { return dryRun; }
    public void setDryRun(boolean dryRun) { this.dryRun = dryRun; }

    public OffsetDateTime getCutoffDate() { return cutoffDate; }
    public void setCutoffDate(OffsetDateTime cutoffDate) { this.cutoffDate = cutoffDate; }

    public long getMessagesDeleted() { return messagesDeleted; }
    public void setMessagesDeleted(long messagesDeleted) { this.messagesDeleted = messagesDeleted; }

    public UUID getAuditLogId() { return auditLogId; }
    public void setAuditLogId(UUID auditLogId) { this.auditLogId = auditLogId; }
}
