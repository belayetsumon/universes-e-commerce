-- Customer gift-card purchase migration helper.
-- Hibernate ddl-auto=update may create this in local environments, but
-- production databases should review and run an explicit migration.

CREATE TABLE IF NOT EXISTS promotions_gift_card_purchase (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    version BIGINT,
    created_by VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    modified_by VARCHAR(255),
    modified TIMESTAMP,
    buyer_id BIGINT NOT NULL,
    issued_to_id BIGINT,
    gift_card_id BIGINT,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
    status VARCHAR(30) NOT NULL,
    recipient_name VARCHAR(120),
    recipient_email VARCHAR(180),
    gift_message VARCHAR(500),
    payment_method VARCHAR(30),
    payment_reference VARCHAR(120),
    payment_note VARCHAR(500),
    paid_at TIMESTAMP,
    failure_message VARCHAR(500),
    CONSTRAINT fk_gift_card_purchase_buyer
        FOREIGN KEY (buyer_id) REFERENCES usermodule_users(id),
    CONSTRAINT fk_gift_card_purchase_issued_to
        FOREIGN KEY (issued_to_id) REFERENCES usermodule_users(id),
    CONSTRAINT fk_gift_card_purchase_gift_card
        FOREIGN KEY (gift_card_id) REFERENCES promotions_gift_card(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_gift_card_purchase_payment_reference
    ON promotions_gift_card_purchase (LOWER(payment_reference))
    WHERE payment_reference IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_gift_card_purchase_buyer_status
    ON promotions_gift_card_purchase (buyer_id, status);

CREATE INDEX IF NOT EXISTS ix_gift_card_purchase_created
    ON promotions_gift_card_purchase (created DESC);
