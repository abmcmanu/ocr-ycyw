package com.ycyw.chat.chat_poc_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_purge_audit_log")
public class PurgeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt = OffsetDateTime.now();

    @Column(name = "cutoff_date", nullable = false)
    private OffsetDateTime cutoffDate;

    @Column(name = "messages_deleted", nullable = false)
    private long messagesDeleted;

    @Column(name = "trigger_type", nullable = false)
    private String triggerType;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }

    public OffsetDateTime getCutoffDate() { return cutoffDate; }
    public void setCutoffDate(OffsetDateTime cutoffDate) { this.cutoffDate = cutoffDate; }

    public long getMessagesDeleted() { return messagesDeleted; }
    public void setMessagesDeleted(long messagesDeleted) { this.messagesDeleted = messagesDeleted; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
