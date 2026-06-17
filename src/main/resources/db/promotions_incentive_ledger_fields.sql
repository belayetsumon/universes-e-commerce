-- Incentive ledger migration helper.
-- Hibernate ddl-auto=update can add these automatically in local profiles, but
-- production databases should review and run an explicit migration.

ALTER TABLE promotions_reward_transaction
    ADD COLUMN balance_before DECIMAL(12, 2) NULL,
    ADD COLUMN balance_after DECIMAL(12, 2) NULL,
    ADD COLUMN order_id VARCHAR(255) NULL,
    ADD COLUMN idempotency_key VARCHAR(255) NULL,
    ADD COLUMN reversal_transaction_id BIGINT NULL,
    ADD COLUMN status VARCHAR(50) NULL;

CREATE UNIQUE INDEX ux_promotions_reward_transaction_idempotency_key
    ON promotions_reward_transaction (idempotency_key);

ALTER TABLE promotions_order_incentive_usage
    ADD COLUMN wallet_transaction_id BIGINT NULL,
    ADD COLUMN reward_transaction_id BIGINT NULL,
    ADD COLUMN gift_card_transaction_id BIGINT NULL,
    ADD COLUMN referral_bonus_expected DECIMAL(19, 2) NULL,
    ADD COLUMN quote_reference VARCHAR(255) NULL,
    ADD COLUMN incentive_status VARCHAR(50) NULL;

ALTER TABLE promotions_redemption
    ADD COLUMN amount DECIMAL(19, 2) NULL,
    ADD COLUMN currency VARCHAR(10) NULL,
    ADD COLUMN conversion_rate DECIMAL(19, 6) NULL,
    ADD COLUMN source_program VARCHAR(255) NULL,
    ADD COLUMN source_id VARCHAR(255) NULL,
    ADD COLUMN order_id VARCHAR(255) NULL,
    ADD COLUMN ledger_transaction_id BIGINT NULL,
    ADD COLUMN reversal_reference VARCHAR(255) NULL,
    ADD COLUMN external_reference_id VARCHAR(255) NULL,
    ADD COLUMN completed_at DATETIME NULL;
