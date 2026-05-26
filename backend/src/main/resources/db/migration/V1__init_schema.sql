-- ============================================================================
-- YCYW - POC TCHAT TEMPS RÉEL (PostgreSQL)
-- Architecture: Spring Boot WebSocket + STOMP + Redis pub/sub
-- ============================================================================

-- Extensions requises

-- ============================================================================
-- 0. TABLES PRE-REQUISES (Simulées pour le POC)
-- ============================================================================
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    password_hash VARCHAR(255),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    customer_id UUID REFERENCES customers(id),
    status VARCHAR(50)
);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. CONFIGURATION DU POC (Clients de test)
-- ============================================================================

-- Client de test pour le POC
INSERT INTO customers (id, email, first_name, last_name, password_hash) VALUES
    ('550e8400-e29b-41d4-a716-446655440000'::uuid, 'customer.poc@example.com', 'Jean', 'Dupont', 'argon2id$v=19$m=65536,t=3,p=1$...')
ON CONFLICT (email) DO NOTHING;

-- Agent/Conseiller de test
-- (Note: Les agents ne sont pas dans la table customers, mais dans un système d'identité interne)
-- INSERT INTO agents (id, name, email) - à adapter selon votre système

-- ============================================================================
-- 2. TABLES DU TCHAT (Optimisées pour temps réel)
-- ============================================================================

-- Threads de conversation
CREATE TABLE IF NOT EXISTS chat_threads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Contexte
    subject VARCHAR(255),
    related_reservation_id UUID REFERENCES reservations(id) ON DELETE SET NULL,
    
    -- État
    status VARCHAR(30) DEFAULT 'open', -- 'open', 'waiting', 'in_progress', 'closed', 'archived'
    priority VARCHAR(20) DEFAULT 'normal', -- 'low', 'normal', 'high', 'urgent'
    
    -- Affectation
    assigned_agent_id VARCHAR(100),
    assigned_at TIMESTAMPTZ,
    
    -- SLA tracking
    last_customer_message_at TIMESTAMPTZ,
    last_agent_message_at TIMESTAMPTZ,
    first_response_at TIMESTAMPTZ,
    
    -- Audit
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMPTZ,
    
    CONSTRAINT valid_status CHECK (status IN ('open', 'waiting', 'in_progress', 'closed', 'archived')),
    CONSTRAINT valid_priority CHECK (priority IN ('low', 'normal', 'high', 'urgent'))
);

-- Indices critiques pour les requêtes temps réel
CREATE INDEX IF NOT EXISTS idx_chat_threads_customer ON chat_threads(customer_id);
CREATE INDEX IF NOT EXISTS idx_chat_threads_status ON chat_threads(status) WHERE status != 'archived';
CREATE INDEX IF NOT EXISTS idx_chat_threads_assigned_agent ON chat_threads(assigned_agent_id) WHERE assigned_agent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_chat_threads_updated ON chat_threads(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_threads_created ON chat_threads(created_at DESC);

-- Messages du tchat
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id UUID NOT NULL REFERENCES chat_threads(id) ON DELETE CASCADE,
    
    -- Expéditeur
    sender_type VARCHAR(20) NOT NULL, -- 'customer' ou 'agent'
    sender_id VARCHAR(255) NOT NULL, -- UUID du customer ou ID agent
    sender_name VARCHAR(100),
    
    -- Contenu
    message_text TEXT NOT NULL,
    message_html TEXT, -- Version sanitisée pour affichage
    
    -- Métadonnées
    message_type VARCHAR(20) DEFAULT 'text', -- 'text', 'typing', 'agent_joined', 'agent_left', 'system'
    
    -- État de livraison (WebSocket)
    delivery_status VARCHAR(20) DEFAULT 'sent', -- 'sent', 'delivered', 'read', 'failed'
    delivery_attempt INT DEFAULT 1,
    delivery_failed_at TIMESTAMPTZ,
    
    -- État de lecture (très important pour l'UX)
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    
    -- Optimistic locking (prévient les race conditions)
    version INT DEFAULT 1,
    
    -- Audit
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_sender_type CHECK (sender_type IN ('customer', 'agent')),
    CONSTRAINT valid_message_type CHECK (message_type IN ('text', 'typing', 'agent_joined', 'agent_left', 'system')),
    CONSTRAINT valid_delivery_status CHECK (delivery_status IN ('sent', 'delivered', 'read', 'failed'))
);

-- Indices critiques pour recherche et affichage
CREATE INDEX IF NOT EXISTS idx_chat_messages_thread ON chat_messages(thread_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sent_at ON chat_messages(thread_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_messages_unread ON chat_messages(thread_id, is_read) WHERE is_read = FALSE;
CREATE INDEX IF NOT EXISTS idx_chat_messages_delivery_failed ON chat_messages(thread_id, delivery_status) WHERE delivery_status = 'failed';

-- Vue pour messages non lus par thread
CREATE OR REPLACE VIEW v_chat_unread_summary AS
SELECT 
    thread_id,
    COUNT(*) as unread_count,
    MAX(created_at) as last_unread_at
FROM chat_messages
WHERE is_read = FALSE
GROUP BY thread_id;

-- ============================================================================
-- 3. TABLE D'ÉTAT TEMPS RÉEL (Synchronisation WebSocket)
-- ============================================================================

-- Suivi des utilisateurs connectés (utile pour "online", "typing", etc.)
CREATE TABLE IF NOT EXISTS chat_connection_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id UUID NOT NULL REFERENCES chat_threads(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL, -- 'customer' ou 'agent'
    
    is_connected BOOLEAN DEFAULT FALSE,
    is_typing BOOLEAN DEFAULT FALSE,
    
    connection_started_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_active_connection UNIQUE (thread_id, user_id),
    CONSTRAINT valid_user_type CHECK (user_type IN ('customer', 'agent'))
);

CREATE INDEX IF NOT EXISTS idx_chat_connection_thread ON chat_connection_state(thread_id);
CREATE INDEX IF NOT EXISTS idx_chat_connection_user ON chat_connection_state(user_id);

-- ============================================================================
-- 4. HISTORIQUE ET ARCHIVAGE
-- ============================================================================

-- Archivage des messages (purge RGPD à 12 mois)
CREATE TABLE IF NOT EXISTS chat_messages_archive (
    id UUID,
    thread_id UUID,
    sender_type VARCHAR(20),
    sender_id VARCHAR(255),
    sender_name VARCHAR(100),
    message_text TEXT,
    message_html TEXT,
    message_type VARCHAR(20),
    delivery_status VARCHAR(20),
    is_read BOOLEAN,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    archived_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (archived_at, thread_id, created_at),
    CONSTRAINT fk_archived_thread FOREIGN KEY (thread_id) REFERENCES chat_threads(id) ON DELETE CASCADE
) PARTITION BY RANGE (archived_at);

-- Partitions mensuelles pour les archives
-- CREATE TABLE chat_messages_archive_2024_01 PARTITION OF chat_messages_archive
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- ============================================================================
-- 5. NOTIFICATIONS ET ALERTES
-- ============================================================================

-- Notifications pour les agents (SLA, assignments, etc.)
CREATE TABLE IF NOT EXISTS chat_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL,
    thread_id UUID NOT NULL REFERENCES chat_threads(id) ON DELETE CASCADE,
    
    notification_type VARCHAR(50) NOT NULL, -- 'new_conversation', 'new_message', 'customer_waiting', 'sla_warning', etc.
    title VARCHAR(255) NOT NULL,
    message TEXT,
    
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_notification_type CHECK (notification_type IN ('new_conversation', 'new_message', 'customer_waiting', 'sla_warning', 'assigned'))
);

CREATE INDEX IF NOT EXISTS idx_chat_notifications_agent ON chat_notifications(agent_id, is_read);
CREATE INDEX IF NOT EXISTS idx_chat_notifications_thread ON chat_notifications(thread_id);

-- ============================================================================
-- 6. TRIGGERS POUR MISE À JOUR AUTOMATIQUE
-- ============================================================================

-- Trigger pour mettre à jour updated_at
CREATE OR REPLACE FUNCTION trigger_chat_messages_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chat_messages_update BEFORE UPDATE ON chat_messages
    FOR EACH ROW EXECUTE FUNCTION trigger_chat_messages_update();

-- Trigger pour mettre à jour last_customer_message_at et last_agent_message_at du thread
CREATE OR REPLACE FUNCTION trigger_chat_message_thread_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.sender_type = 'customer' THEN
        UPDATE chat_threads SET last_customer_message_at = NEW.created_at WHERE id = NEW.thread_id;
    ELSIF NEW.sender_type = 'agent' THEN
        UPDATE chat_threads SET last_agent_message_at = NEW.created_at WHERE id = NEW.thread_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chat_message_thread_update AFTER INSERT ON chat_messages
    FOR EACH ROW EXECUTE FUNCTION trigger_chat_message_thread_update();

-- Trigger pour archiver les messages après 12 mois
CREATE OR REPLACE FUNCTION archive_old_chat_messages()
RETURNS void AS $$
BEGIN
    -- Archive les messages > 12 mois
    INSERT INTO chat_messages_archive
    SELECT id, thread_id, sender_type, sender_id, sender_name, message_text, message_html, message_type,
           delivery_status, is_read, read_at, created_at, updated_at
    FROM chat_messages
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '12 months';
    
    -- Supprimer du tableau principal
    DELETE FROM chat_messages
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '12 months';
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 7. REQUÊTES OPTIMISÉES POUR LE POC
-- ============================================================================

-- Récupérer les conversations en attente (pour file d'attente agent)
CREATE OR REPLACE FUNCTION get_waiting_conversations(limit_count INT DEFAULT 20)
RETURNS TABLE (
    thread_id UUID,
    customer_name VARCHAR,
    customer_email VARCHAR,
    subject VARCHAR,
    priority VARCHAR,
    created_at TIMESTAMPTZ,
    unread_count BIGINT,
    wait_time_seconds INT
) AS $$
SELECT 
    ct.id,
    CONCAT(c.first_name, ' ', c.last_name) as customer_name,
    c.email,
    ct.subject,
    ct.priority,
    ct.created_at,
    COUNT(cm.id) FILTER (WHERE cm.is_read = FALSE AND cm.sender_type = 'customer') as unread_count,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ct.created_at))::INT as wait_time_seconds
FROM chat_threads ct
JOIN customers c ON ct.customer_id = c.id
LEFT JOIN chat_messages cm ON ct.id = cm.thread_id
WHERE ct.status = 'waiting'
  AND c.deleted_at IS NULL
GROUP BY ct.id, c.first_name, c.last_name, c.email, ct.subject, ct.priority, ct.created_at
ORDER BY ct.priority DESC, ct.created_at ASC
LIMIT limit_count;
$$ LANGUAGE sql STABLE;

-- Récupérer les messages d'un thread avec contexte client
CREATE OR REPLACE FUNCTION get_chat_thread_messages(
    p_thread_id UUID,
    p_limit INT DEFAULT 50,
    p_offset INT DEFAULT 0
)
RETURNS TABLE (
    message_id UUID,
    sender_type VARCHAR,
    sender_name VARCHAR,
    message_text TEXT,
    message_type VARCHAR,
    delivery_status VARCHAR,
    is_read BOOLEAN,
    created_at TIMESTAMPTZ
) AS $$
SELECT 
    cm.id,
    cm.sender_type,
    cm.sender_name,
    cm.message_text,
    cm.message_type,
    cm.delivery_status,
    cm.is_read,
    cm.created_at
FROM chat_messages cm
WHERE cm.thread_id = p_thread_id
ORDER BY cm.created_at DESC
LIMIT p_limit OFFSET p_offset;
$$ LANGUAGE sql STABLE;

-- Marquer tous les messages d'un thread comme lus
CREATE OR REPLACE FUNCTION mark_thread_as_read(p_thread_id UUID)
RETURNS INT AS $$
DECLARE
    v_count INT;
BEGIN
    UPDATE chat_messages
    SET is_read = TRUE,
        read_at = CURRENT_TIMESTAMP,
        delivery_status = 'read'
    WHERE thread_id = p_thread_id
      AND is_read = FALSE
      AND sender_type = 'agent'; -- Seulement les messages de l'agent sont "lus"
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Insérer un message et retourner son ID
CREATE OR REPLACE FUNCTION insert_chat_message(
    p_thread_id UUID,
    p_sender_type VARCHAR,
    p_sender_id VARCHAR,
    p_sender_name VARCHAR,
    p_message_text TEXT,
    p_message_type VARCHAR DEFAULT 'text'
)
RETURNS TABLE (
    message_id UUID,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    v_message_id UUID;
    v_created_at TIMESTAMPTZ;
BEGIN
    INSERT INTO chat_messages (
        thread_id, sender_type, sender_id, sender_name, message_text, message_type, delivery_status
    ) VALUES (
        p_thread_id, p_sender_type, p_sender_id, p_sender_name, p_message_text, p_message_type, 'sent'
    )
    RETURNING chat_messages.id, chat_messages.created_at INTO v_message_id, v_created_at;
    
    RETURN QUERY SELECT v_message_id, v_created_at;
END;
$$ LANGUAGE plpgsql;

-- Assigner une conversation à un agent
CREATE OR REPLACE FUNCTION assign_conversation(
    p_thread_id UUID,
    p_agent_id VARCHAR
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE chat_threads
    SET status = 'in_progress',
        assigned_agent_id = p_agent_id,
        assigned_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_thread_id;
    
    -- Créer une notification pour l'agent
    INSERT INTO chat_notifications (agent_id, thread_id, notification_type, title, message)
    VALUES (p_agent_id, p_thread_id, 'assigned', 'Conversation assignée', 'Vous avez une nouvelle conversation');
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Fermer une conversation
CREATE OR REPLACE FUNCTION close_conversation(p_thread_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE chat_threads
    SET status = 'closed',
        closed_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_thread_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Récupérer les statistiques de conversation
CREATE OR REPLACE FUNCTION get_conversation_stats()
RETURNS TABLE (
    total_open INT,
    total_waiting INT,
    total_in_progress INT,
    avg_wait_time_minutes NUMERIC,
    avg_response_time_minutes NUMERIC,
    unread_messages_count INT
) AS $$
SELECT 
    COUNT(*) FILTER (WHERE status IN ('open', 'waiting', 'in_progress'))::INT,
    COUNT(*) FILTER (WHERE status = 'waiting')::INT,
    COUNT(*) FILTER (WHERE status = 'in_progress')::INT,
    ROUND(AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - created_at)) / 60) 
          FILTER (WHERE status = 'waiting')::NUMERIC, 2),
    ROUND(AVG(EXTRACT(EPOCH FROM (first_response_at - created_at)) / 60) 
          FILTER (WHERE first_response_at IS NOT NULL)::NUMERIC, 2),
    (SELECT COUNT(*)::INT FROM chat_messages WHERE is_read = FALSE AND sender_type = 'agent')
FROM chat_threads;
$$ LANGUAGE sql STABLE;

-- ============================================================================
-- 8. DONNÉES DE TEST POUR LE POC
-- ============================================================================

-- Créer une conversation de test
INSERT INTO chat_threads (customer_id, subject, status, priority)
VALUES ('550e8400-e29b-41d4-a716-446655440000'::uuid, 'Question sur ma réservation', 'waiting', 'normal')
ON CONFLICT DO NOTHING;

-- Insérer des messages de test
INSERT INTO chat_messages (thread_id, sender_type, sender_id, sender_name, message_text, message_type)
SELECT 
    ct.id,
    'customer',
    c.id::text,
    CONCAT(c.first_name, ' ', c.last_name),
    'Bonjour, j''ai besoin d''aide pour ma réservation.',
    'text'
FROM chat_threads ct
JOIN customers c ON ct.customer_id = c.id
WHERE ct.subject = 'Question sur ma réservation'
LIMIT 1
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 9. PROCÉDURE DE MAINTENANCE (Cron jobs PostgreSQL)
-- ============================================================================

-- Archiver les vieux messages (à exécuter hebdomadairement)
-- SELECT archive_old_chat_messages();

-- Nettoyer les connexions expirées
CREATE OR REPLACE PROCEDURE cleanup_expired_connections()
LANGUAGE SQL AS $$
    DELETE FROM chat_connection_state
    WHERE last_heartbeat_at < CURRENT_TIMESTAMP - INTERVAL '5 minutes'
      AND is_connected = TRUE;
$$;

-- ============================================================================
-- 10. VUE D'ENSEMBLE POUR LE MONITORING
-- ============================================================================

CREATE OR REPLACE VIEW v_chat_dashboard AS
SELECT 
    (SELECT COUNT(*) FROM chat_threads WHERE status IN ('open', 'waiting', 'in_progress')) as total_active,
    (SELECT COUNT(*) FROM chat_threads WHERE status = 'waiting') as waiting_conversations,
    (SELECT COUNT(*) FROM chat_messages WHERE is_read = FALSE AND sender_type = 'agent') as unread_agent_messages,
    (SELECT COUNT(DISTINCT assigned_agent_id) FROM chat_threads WHERE assigned_agent_id IS NOT NULL) as active_agents,
    (SELECT ROUND(AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - created_at)) / 60), 2)
     FROM chat_threads WHERE status = 'waiting') as avg_wait_time_minutes,
    NOW() as last_updated;

-- ============================================================================
-- FIN DU POC TCHAT
-- ============================================================================