-- Centralized message and communication module tables.
-- Existing profiles commonly use Hibernate ddl-auto=update, but this file is provided for production-controlled deployments.

CREATE TABLE IF NOT EXISTS communication_settings (
    id INT NOT NULL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    default_language VARCHAR(10) NOT NULL DEFAULT 'en',
    direct_volume_threshold INT NOT NULL DEFAULT 100,
    queue_volume_threshold INT NOT NULL DEFAULT 1000,
    max_retry_count INT NOT NULL DEFAULT 3,
    retry_delay_minutes INT NOT NULL DEFAULT 5,
    scheduler_enabled BIT NOT NULL DEFAULT 1,
    version BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO communication_settings (
    id,
    uuid,
    default_language,
    direct_volume_threshold,
    queue_volume_threshold,
    max_retry_count,
    retry_delay_minutes,
    scheduler_enabled
) SELECT
    1,
    UUID(),
    'en',
    100,
    1000,
    3,
    5,
    1
WHERE NOT EXISTS (SELECT 1 FROM communication_settings WHERE id = 1);

CREATE TABLE IF NOT EXISTS communication_message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    subject VARCHAR(250),
    body TEXT NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    variables TEXT,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comm_template_event_channel_language (event_type, channel, language),
    INDEX idx_comm_template_event_channel_status (event_type, channel, status),
    INDEX idx_comm_template_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_message_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    provider_name VARCHAR(120) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    provider_type VARCHAR(40) NOT NULL,
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    sender_id VARCHAR(100),
    base_url VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    priority INT NOT NULL DEFAULT 100,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comm_provider_name_channel (provider_name, channel),
    INDEX idx_comm_provider_channel_status_priority (channel, status, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_message_routing_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    delivery_mode VARCHAR(40) NOT NULL DEFAULT 'DIRECT',
    provider_id BIGINT,
    min_volume INT NOT NULL DEFAULT 0,
    max_volume INT,
    active BIT NOT NULL DEFAULT 1,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_route_event_channel_active (event_type, channel, active),
    INDEX idx_comm_route_volume (min_volume, max_volume),
    CONSTRAINT fk_comm_route_provider FOREIGN KEY (provider_id) REFERENCES communication_message_providers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_message_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(250),
    body TEXT NOT NULL,
    payload_json TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    retry_count INT NOT NULL DEFAULT 0,
    scheduled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,
    failed_reason TEXT,
    provider_id BIGINT,
    delivery_mode VARCHAR(40),
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_job_status_scheduled (status, scheduled_at),
    INDEX idx_comm_job_event_channel_status (event_type, channel, status),
    INDEX idx_comm_job_recipient (recipient),
    CONSTRAINT fk_comm_job_provider FOREIGN KEY (provider_id) REFERENCES communication_message_providers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_message_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    provider_id BIGINT,
    status VARCHAR(30) NOT NULL,
    response_code VARCHAR(80),
    response_message TEXT,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_log_event_channel_status (event_type, channel, status),
    INDEX idx_comm_log_sent_at (sent_at),
    INDEX idx_comm_log_recipient (recipient),
    CONSTRAINT fk_comm_log_provider FOREIGN KEY (provider_id) REFERENCES communication_message_providers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
    title VARCHAR(250),
    message TEXT NOT NULL,
    payload_json TEXT,
    seen BIT NOT NULL DEFAULT 0,
    seen_at DATETIME NULL,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_notification_user_seen (user_id, seen),
    INDEX idx_comm_notification_event (event_type),
    INDEX idx_comm_notification_created (created_at),
    CONSTRAINT fk_comm_notification_user FOREIGN KEY (user_id) REFERENCES usermodule_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    subject VARCHAR(250) NOT NULL,
    message_body TEXT NOT NULL,
    channel VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
    message_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOM',
    created_by_user_id BIGINT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SENT',
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_message_channel_created (channel, created_at),
    INDEX idx_comm_message_status_created (status, created_at),
    INDEX idx_comm_message_type_created (message_type, created_at),
    CONSTRAINT fk_comm_message_created_user FOREIGN KEY (created_by_user_id) REFERENCES usermodule_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS communication_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    message_id BIGINT NOT NULL,
    receiver_type VARCHAR(30) NOT NULL DEFAULT 'USER',
    receiver_user_id BIGINT NOT NULL,
    vendor_id BIGINT NULL,
    receiver_name VARCHAR(180),
    receiver_email VARCHAR(180),
    receiver_mobile VARCHAR(50),
    delivered BIT NOT NULL DEFAULT 1,
    read_status BIT NOT NULL DEFAULT 0,
    delivered_at DATETIME NULL,
    read_at DATETIME NULL,
    clicked_at DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SENT',
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comm_recipient_user_read (receiver_user_id, read_status),
    INDEX idx_comm_recipient_message_read (message_id, read_status),
    INDEX idx_comm_recipient_message_type_read (message_id, receiver_type, read_status),
    INDEX idx_comm_recipient_vendor (vendor_id),
    CONSTRAINT fk_comm_recipient_message FOREIGN KEY (message_id) REFERENCES communication_messages(id),
    CONSTRAINT fk_comm_recipient_user FOREIGN KEY (receiver_user_id) REFERENCES usermodule_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Enterprise communication hardening additions: outbox metadata, idempotency, preferences, unsubscribe, rate limits, template tracking.
ALTER TABLE communication_settings ADD COLUMN IF NOT EXISTS recipient_hourly_limit INT NOT NULL DEFAULT 20;
ALTER TABLE communication_settings ADD COLUMN IF NOT EXISTS provider_per_minute_limit INT NOT NULL DEFAULT 60;

ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS message_type VARCHAR(30) NOT NULL DEFAULT 'TRANSACTIONAL';
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS language VARCHAR(10) NULL;
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS variables_json TEXT NULL;
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(160) NULL;
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS template_id BIGINT NULL;
ALTER TABLE communication_message_jobs ADD COLUMN IF NOT EXISTS template_version BIGINT NULL;
ALTER TABLE communication_message_jobs ADD UNIQUE KEY IF NOT EXISTS uk_comm_job_idempotency_key (idempotency_key);
ALTER TABLE communication_message_jobs ADD INDEX IF NOT EXISTS idx_comm_job_type_status (message_type, status);

ALTER TABLE communication_message_logs ADD COLUMN IF NOT EXISTS message_type VARCHAR(30) NOT NULL DEFAULT 'TRANSACTIONAL';
ALTER TABLE communication_message_logs ADD COLUMN IF NOT EXISTS template_id BIGINT NULL;
ALTER TABLE communication_message_logs ADD COLUMN IF NOT EXISTS template_version BIGINT NULL;
ALTER TABLE communication_message_logs ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(160) NULL;
ALTER TABLE communication_message_logs ADD INDEX IF NOT EXISTS idx_comm_log_type_sent (message_type, sent_at);
ALTER TABLE communication_message_logs ADD INDEX IF NOT EXISTS idx_comm_log_idempotency (idempotency_key);

CREATE TABLE IF NOT EXISTS communication_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NULL,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    transactional_enabled BIT NOT NULL DEFAULT 1,
    marketing_enabled BIT NOT NULL DEFAULT 1,
    unsubscribed_at DATETIME NULL,
    unsubscribe_token VARCHAR(80) NOT NULL,
    version BIGINT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comm_pref_recipient_channel (recipient, channel),
    UNIQUE KEY uk_comm_pref_unsubscribe_token (unsubscribe_token),
    INDEX idx_comm_pref_user_channel (user_id, channel),
    INDEX idx_comm_pref_recipient (recipient),
    CONSTRAINT fk_comm_pref_user FOREIGN KEY (user_id) REFERENCES usermodule_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
