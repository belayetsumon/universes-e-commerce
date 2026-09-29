-- Store only one-way password-reset token digests. Raw links are delivered
-- directly by the mail sender and are never persisted in communication jobs.
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id) REFERENCES usermodule_users(id) ON DELETE CASCADE,
    KEY idx_password_reset_user_expiry (user_id, expires_at),
    KEY idx_password_reset_expiry (expires_at)
) ENGINE=InnoDB;
