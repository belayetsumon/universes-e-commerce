-- Mobile-only guest checkout with OTP verification.
-- Review before production use. Syntax follows the existing MySQL-oriented project scripts.

ALTER TABLE usermodule_users ADD COLUMN IF NOT EXISTS mobile_verified BIT NOT NULL DEFAULT 0;
ALTER TABLE usermodule_users ADD COLUMN IF NOT EXISTS email_verified BIT NOT NULL DEFAULT 0;
ALTER TABLE usermodule_users ADD COLUMN IF NOT EXISTS registration_source VARCHAR(30) NULL;
ALTER TABLE usermodule_users ADD COLUMN IF NOT EXISTS guest_account BIT NOT NULL DEFAULT 0;
ALTER TABLE usermodule_users ADD COLUMN IF NOT EXISTS password_configured BIT NOT NULL DEFAULT 1;

ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS secure_checkout_enabled BIT NOT NULL DEFAULT 1;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_mobile_required BIT NOT NULL DEFAULT 1;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_mobile_otp_verification_enabled BIT NOT NULL DEFAULT 1;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_otp_expiry_minutes INT NOT NULL DEFAULT 5;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_otp_maximum_attempts INT NOT NULL DEFAULT 5;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_otp_resend_cooldown_seconds INT NOT NULL DEFAULT 60;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_otp_daily_send_limit INT NOT NULL DEFAULT 5;
ALTER TABLE global_settings ADD COLUMN IF NOT EXISTS guest_auto_create_customer_account BIT NOT NULL DEFAULT 1;

ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS guest_checkout BIT NOT NULL DEFAULT 0;
ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(20) NULL;
ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS mobile_verification_required BIT NOT NULL DEFAULT 0;
ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS mobile_verification_status VARCHAR(30) NULL;
ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS mobile_verified_at DATETIME(6) NULL;
ALTER TABLE customer_order_group ADD COLUMN IF NOT EXISTS checkout_session_id VARCHAR(80) NULL;
ALTER TABLE customer_order_group ADD INDEX IF NOT EXISTS idx_customer_order_group_guest_checkout (guest_checkout, mobile_verification_status);
ALTER TABLE customer_order_group ADD INDEX IF NOT EXISTS idx_customer_order_group_mobile_number (mobile_number);

ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS guest_checkout BIT NOT NULL DEFAULT 0;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(20) NULL;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS mobile_verification_required BIT NOT NULL DEFAULT 0;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS mobile_verification_status VARCHAR(30) NULL;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS mobile_verified_at DATETIME(6) NULL;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS checkout_session_id VARCHAR(80) NULL;
ALTER TABLE sales_order ADD INDEX IF NOT EXISTS idx_sales_order_guest_checkout (guest_checkout, mobile_verification_status);
ALTER TABLE sales_order ADD INDEX IF NOT EXISTS idx_sales_order_mobile_number (mobile_number);

CREATE UNIQUE INDEX IF NOT EXISTS uk_usermodule_users_mobile_normalized ON usermodule_users (mobile);
CREATE INDEX IF NOT EXISTS idx_usermodule_users_guest_account ON usermodule_users (guest_account, registration_source);
CREATE INDEX IF NOT EXISTS idx_usermodule_users_mobile_verified ON usermodule_users (mobile_verified);

CREATE TABLE IF NOT EXISTS guest_checkout_otp_verification (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(100) NOT NULL,
    purpose VARCHAR(40) NOT NULL DEFAULT 'GUEST_CHECKOUT',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    resend_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6) NULL,
    used_at DATETIME(6) NULL,
    session_token VARCHAR(80) NOT NULL,
    http_session_id VARCHAR(120) NULL,
    ip_address_hash VARCHAR(100) NULL,
    device_fingerprint_hash VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_guest_otp_uuid UNIQUE (uuid),
    CONSTRAINT uk_guest_otp_session_token UNIQUE (session_token),
    INDEX idx_guest_otp_mobile_purpose_status (mobile_number, purpose, status),
    INDEX idx_guest_otp_mobile_created (mobile_number, created_at),
    INDEX idx_guest_otp_ip_created (ip_address_hash, created_at),
    INDEX idx_guest_otp_device_created (device_fingerprint_hash, created_at),
    INDEX idx_guest_otp_expires_status (expires_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO communication_message_templates (
    uuid,
    event_type,
    channel,
    subject,
    body,
    language,
    status,
    variables,
    created_by
) SELECT
    UUID(),
    'GUEST_CHECKOUT_OTP',
    'SMS',
    'Guest checkout OTP',
    'Your Universes Commerce guest checkout verification code is {{otp}}. It expires in {{ttlMinutes}} minutes.',
    'en',
    'ACTIVE',
    'otp,ttlMinutes',
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM communication_message_templates
    WHERE event_type = 'GUEST_CHECKOUT_OTP'
      AND channel = 'SMS'
      AND language = 'en'
);
