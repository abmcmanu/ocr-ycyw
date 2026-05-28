-- US-25 : index purge RGPD + journal d'audit

CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at_purge
    ON chat_messages (created_at);

CREATE TABLE IF NOT EXISTS chat_purge_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    executed_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cutoff_date     TIMESTAMPTZ NOT NULL,
    messages_deleted BIGINT NOT NULL DEFAULT 0,
    trigger_type    VARCHAR(32) NOT NULL,
    triggered_by    VARCHAR(255),
    details         TEXT
);

CREATE INDEX IF NOT EXISTS idx_chat_purge_audit_executed_at
    ON chat_purge_audit_log (executed_at DESC);

COMMENT ON TABLE chat_purge_audit_log IS 'Traçabilité RGPD des purges automatiques et manuelles';
