-- Global settings schema repair for MySQL 8+.
-- Run this on live only if /admin/settings/index logs missing global_settings table
-- or missing-column errors. Review existing data before running on production.

CREATE TABLE IF NOT EXISTS global_settings (
    id INT NOT NULL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    site_name VARCHAR(150) NOT NULL,
    site_title VARCHAR(200) NULL,
    site_tagline VARCHAR(300) NULL,
    site_logo VARCHAR(500) NULL,
    site_logo_desktop VARCHAR(500) NULL,
    site_logo_mobile VARCHAR(500) NULL,
    site_logo_square VARCHAR(500) NULL,
    favicon VARCHAR(500) NULL,
    site_url VARCHAR(300) NULL,
    admin_email VARCHAR(150) NULL,
    support_email VARCHAR(150) NULL,
    support_phone VARCHAR(50) NULL,
    address TEXT NULL,
    timezone VARCHAR(80) NULL,
    currency VARCHAR(10) NULL,
    language VARCHAR(10) NULL,
    meta_title VARCHAR(200) NULL,
    meta_description VARCHAR(500) NULL,
    meta_keywords VARCHAR(500) NULL,
    og_title VARCHAR(200) NULL,
    og_description VARCHAR(500) NULL,
    og_image VARCHAR(500) NULL,
    google_analytics_id VARCHAR(100) NULL,
    facebook_pixel_id VARCHAR(100) NULL,
    default_currency VARCHAR(10) NULL,
    tax_enabled BIT NOT NULL DEFAULT 0,
    tax_percentage DECIMAL(5,2) NULL DEFAULT 0.00,
    stock_management_enabled BIT NOT NULL DEFAULT 1,
    low_stock_alert_qty INT NULL DEFAULT 5,
    allow_guest_checkout BIT NOT NULL DEFAULT 1,
    minimum_order_amount DECIMAL(19,2) NULL DEFAULT 0.00,
    maximum_order_amount DECIMAL(19,2) NULL,
    cod_enabled BIT NOT NULL DEFAULT 1,
    online_payment_enabled BIT NOT NULL DEFAULT 0,
    partial_payment_enabled BIT NOT NULL DEFAULT 0,
    emi_enabled BIT NOT NULL DEFAULT 0,
    delivery_enabled BIT NOT NULL DEFAULT 1,
    free_delivery_enabled BIT NOT NULL DEFAULT 0,
    free_delivery_min_amount DECIMAL(19,2) NULL DEFAULT 0.00,
    inside_dhaka_delivery_charge DECIMAL(19,2) NULL DEFAULT 0.00,
    outside_dhaka_delivery_charge DECIMAL(19,2) NULL DEFAULT 0.00,
    delivery_time_text VARCHAR(150) NULL,
    cash_on_delivery_charge DECIMAL(19,2) NULL DEFAULT 0.00,
    order_prefix VARCHAR(20) NULL,
    invoice_prefix VARCHAR(20) NULL,
    auto_confirm_order BIT NOT NULL DEFAULT 0,
    auto_cancel_unpaid_order BIT NOT NULL DEFAULT 1,
    cancel_order_after_minutes INT NULL DEFAULT 60,
    return_allowed_days INT NULL DEFAULT 7,
    refund_allowed_days INT NULL DEFAULT 7,
    facebook_url VARCHAR(300) NULL,
    youtube_url VARCHAR(300) NULL,
    instagram_url VARCHAR(300) NULL,
    linkedin_url VARCHAR(300) NULL,
    twitter_url VARCHAR(300) NULL,
    whatsapp_number VARCHAR(50) NULL,
    about_us LONGTEXT NULL,
    contact_us_content LONGTEXT NULL,
    help_page_content LONGTEXT NULL,
    terms_of_use_content LONGTEXT NULL,
    terms_and_conditions LONGTEXT NULL,
    privacy_policy LONGTEXT NULL,
    payment_methods_content LONGTEXT NULL,
    return_policy LONGTEXT NULL,
    refund_policy LONGTEXT NULL,
    shipping_policy LONGTEXT NULL,
    maintenance_mode BIT NOT NULL DEFAULT 0,
    maintenance_message TEXT NULL,
    registration_enabled BIT NOT NULL DEFAULT 1,
    vendor_registration_enabled BIT NOT NULL DEFAULT 1,
    active BIT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_on DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NULL,
    updated_on DATETIME(6) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT uk_global_settings_uuid UNIQUE (uuid),
    CONSTRAINT chk_global_settings_singleton CHECK (id = 1)
);

INSERT INTO global_settings (
    id, uuid, site_name, admin_email, timezone, currency, language, default_currency,
    tax_enabled, tax_percentage, stock_management_enabled, low_stock_alert_qty,
    allow_guest_checkout, minimum_order_amount, cod_enabled, online_payment_enabled,
    partial_payment_enabled, emi_enabled, delivery_enabled, free_delivery_enabled,
    free_delivery_min_amount, inside_dhaka_delivery_charge, outside_dhaka_delivery_charge,
    cash_on_delivery_charge, order_prefix, invoice_prefix, auto_confirm_order,
    auto_cancel_unpaid_order, cancel_order_after_minutes, return_allowed_days,
    refund_allowed_days, maintenance_mode, registration_enabled,
    vendor_registration_enabled, active, version, created_on
)
SELECT
    1, UUID(), 'Universes Ecommerce', 'admin@gmail.com', 'Asia/Dhaka', 'BDT', 'en', 'BDT',
    0, 0.00, 1, 5,
    1, 0.00, 1, 0,
    0, 0, 1, 0,
    0.00, 0.00, 0.00,
    0.00, 'ORD', 'INV', 0,
    1, 60, 7,
    7, 0, 1,
    1, 1, 0, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM global_settings WHERE id = 1);

SET @global_settings_version_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'version'
);

SET @global_settings_version_sql = IF(
    @global_settings_version_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
    'SELECT 1'
);

PREPARE global_settings_version_stmt FROM @global_settings_version_sql;
EXECUTE global_settings_version_stmt;
DEALLOCATE PREPARE global_settings_version_stmt;

UPDATE global_settings
SET version = 0
WHERE version IS NULL;

ALTER TABLE global_settings
    MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
