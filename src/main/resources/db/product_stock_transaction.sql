-- Stock ledger migration helper.
-- Run once if Hibernate ddl-auto=update does not create or repair the inventory
-- ledger table/columns in MySQL.

ALTER TABLE product
    ADD COLUMN stock_available_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    ADD COLUMN stock_reserved_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    ADD COLUMN stock_sold_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000;

ALTER TABLE product_catalog_product_variant
    ADD COLUMN reserved_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    ADD COLUMN sold_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000;

CREATE TABLE product_stock_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    catalog_variant_id BIGINT NULL,
    sales_order_id BIGINT NULL,
    order_item_id BIGINT NULL,
    transaction_type VARCHAR(255) NULL,
    from_bucket VARCHAR(255) NULL,
    to_bucket VARCHAR(255) NULL,
    quantity DECIMAL(19, 6) NOT NULL,
    available_after DECIMAL(19, 6) NOT NULL,
    reserved_after DECIMAL(19, 6) NOT NULL,
    sold_after DECIMAL(19, 6) NOT NULL,
    note VARCHAR(255) NULL,
    created_by VARCHAR(255) NULL,
    created DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_stock_transaction_uuid (uuid),
    UNIQUE KEY uk_product_stock_transaction_idempotency_key (idempotency_key),
    KEY idx_product (product_id),
    KEY idx_catalog_variant (catalog_variant_id),
    KEY idx_created (created),
    KEY idx_idempotency (idempotency_key),
    KEY idx_stock_transaction_sales_order (sales_order_id),
    KEY idx_stock_transaction_order_item (order_item_id),
    CONSTRAINT fk_stock_transaction_product
        FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_stock_transaction_catalog_variant
        FOREIGN KEY (catalog_variant_id) REFERENCES product_catalog_product_variant (id),
    CONSTRAINT fk_stock_transaction_sales_order
        FOREIGN KEY (sales_order_id) REFERENCES sales_order (id),
    CONSTRAINT fk_stock_transaction_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_item (id)
);
