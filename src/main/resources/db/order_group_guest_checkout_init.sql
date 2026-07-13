-- Order group and guest checkout schema additions.
-- Review before production use. This script uses MySQL 8 syntax to match the existing repair scripts.

CREATE TABLE IF NOT EXISTS customer_order_group (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    order_group_code VARCHAR(40) NOT NULL,
    customer_id BIGINT NULL,
    guest_name VARCHAR(150) NULL,
    guest_email VARCHAR(150) NULL,
    guest_phone VARCHAR(50) NULL,
    guest_session_id VARCHAR(100) NULL,
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    shipping_total DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    packing_total DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    discount_total DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    payment_state VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    payment_method VARCHAR(30) NULL,
    status_summary VARCHAR(50) NULL,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    created DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    modified_by VARCHAR(255) NULL,
    modified DATETIME(6) NULL,
    CONSTRAINT uk_customer_order_group_uuid UNIQUE (uuid),
    CONSTRAINT uk_customer_order_group_code UNIQUE (order_group_code),
    INDEX idx_customer_order_group_customer (customer_id),
    INDEX idx_customer_order_group_guest_phone (guest_phone)
);

SET @sales_order_group_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_order'
      AND COLUMN_NAME = 'order_group_id'
);

SET @sales_order_group_sql = IF(
    @sales_order_group_column_exists = 0,
    'ALTER TABLE sales_order ADD COLUMN order_group_id BIGINT NULL, ADD INDEX idx_sales_order_group (order_group_id)',
    'SELECT 1'
);

PREPARE sales_order_group_stmt FROM @sales_order_group_sql;
EXECUTE sales_order_group_stmt;
DEALLOCATE PREPARE sales_order_group_stmt;

SET @sales_order_customer_required = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_order'
      AND COLUMN_NAME = 'customer_id'
      AND IS_NULLABLE = 'NO'
);

SET @sales_order_customer_sql = IF(
    @sales_order_customer_required > 0,
    'ALTER TABLE sales_order MODIFY COLUMN customer_id BIGINT NULL',
    'SELECT 1'
);

PREPARE sales_order_customer_stmt FROM @sales_order_customer_sql;
EXECUTE sales_order_customer_stmt;
DEALLOCATE PREPARE sales_order_customer_stmt;
