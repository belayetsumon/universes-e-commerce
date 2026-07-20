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
    open_graph_enabled BIT NOT NULL DEFAULT 1,
    og_site_name VARCHAR(150) NULL,
    public_base_url VARCHAR(500) NULL,
    facebook_app_id VARCHAR(100) NULL,
    twitter_card_type VARCHAR(50) NULL DEFAULT 'summary_large_image',
    google_analytics_id VARCHAR(100) NULL,
    facebook_pixel_id VARCHAR(100) NULL,
    social_sharing_enabled BIT NOT NULL DEFAULT 1,
    facebook_sharing_enabled BIT NOT NULL DEFAULT 1,
    messenger_sharing_enabled BIT NOT NULL DEFAULT 1,
    whatsapp_sharing_enabled BIT NOT NULL DEFAULT 1,
    linkedin_sharing_enabled BIT NOT NULL DEFAULT 1,
    twitter_sharing_enabled BIT NOT NULL DEFAULT 1,
    email_sharing_enabled BIT NOT NULL DEFAULT 1,
    copy_link_sharing_enabled BIT NOT NULL DEFAULT 1,
    native_share_enabled BIT NOT NULL DEFAULT 1,
    referral_links_enabled BIT NOT NULL DEFAULT 1,
    referral_cookie_expiry_days INT NOT NULL DEFAULT 30,
    facebook_pixel_enabled BIT NOT NULL DEFAULT 0,
    facebook_browser_tracking_enabled BIT NOT NULL DEFAULT 1,
    facebook_conversion_api_enabled BIT NOT NULL DEFAULT 0,
    facebook_conversion_api_access_token VARCHAR(500) NULL,
    facebook_test_event_code VARCHAR(100) NULL,
    facebook_debug_mode BIT NOT NULL DEFAULT 0,
    google_analytics_enabled BIT NOT NULL DEFAULT 0,
    ga4_enhanced_ecommerce_enabled BIT NOT NULL DEFAULT 1,
    ga4_debug_mode BIT NOT NULL DEFAULT 0,
    google_consent_mode_enabled BIT NOT NULL DEFAULT 1,
    google_tag_manager_enabled BIT NOT NULL DEFAULT 0,
    gtm_container_id VARCHAR(50) NULL,
    server_side_gtm_url VARCHAR(500) NULL,
    tracking_implementation_mode VARCHAR(30) NOT NULL DEFAULT 'DIRECT',
    cookie_consent_enabled BIT NOT NULL DEFAULT 1,
    default_currency VARCHAR(10) NULL,
    tax_enabled BIT NOT NULL DEFAULT 0,
    tax_percentage DECIMAL(5,2) NULL DEFAULT 0.00,
    stock_management_enabled BIT NOT NULL DEFAULT 1,
    low_stock_alert_qty INT NULL DEFAULT 5,
    secure_checkout_enabled BIT NOT NULL DEFAULT 1,
    allow_guest_checkout BIT NOT NULL DEFAULT 1,
    store_mode VARCHAR(30) NOT NULL DEFAULT 'MARKETPLACE',
    primary_vendor_id BIGINT NULL,
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
    sales_order_mode VARCHAR(30) NOT NULL DEFAULT 'SPLIT_BY_VENDOR',
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
    secure_checkout_enabled, allow_guest_checkout, store_mode, sales_order_mode, minimum_order_amount, cod_enabled, online_payment_enabled,
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
    1, 1, 'MARKETPLACE', 'SPLIT_BY_VENDOR', 0.00, 1, 0,
    0, 0, 1, 0,
    0.00, 0.00, 0.00,
    0.00, 'ORD', 'INV', 0,
    1, 60, 7,
    7, 0, 1,
    1, 1, 0, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM global_settings WHERE id = 1);

SET @global_settings_secure_checkout_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'secure_checkout_enabled'
);

SET @global_settings_secure_checkout_sql = IF(
    @global_settings_secure_checkout_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN secure_checkout_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_secure_checkout_stmt FROM @global_settings_secure_checkout_sql;
EXECUTE global_settings_secure_checkout_stmt;
DEALLOCATE PREPARE global_settings_secure_checkout_stmt;

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

SET @global_settings_store_mode_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'store_mode'
);

SET @global_settings_store_mode_sql = IF(
    @global_settings_store_mode_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN store_mode VARCHAR(30) NOT NULL DEFAULT ''MARKETPLACE''',
    'SELECT 1'
);

PREPARE global_settings_store_mode_stmt FROM @global_settings_store_mode_sql;
EXECUTE global_settings_store_mode_stmt;
DEALLOCATE PREPARE global_settings_store_mode_stmt;

SET @global_settings_primary_vendor_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'primary_vendor_id'
);

SET @global_settings_primary_vendor_sql = IF(
    @global_settings_primary_vendor_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN primary_vendor_id BIGINT NULL',
    'SELECT 1'
);

PREPARE global_settings_primary_vendor_stmt FROM @global_settings_primary_vendor_sql;
EXECUTE global_settings_primary_vendor_stmt;
DEALLOCATE PREPARE global_settings_primary_vendor_stmt;

SET @global_settings_sales_order_mode_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'sales_order_mode'
);

SET @global_settings_sales_order_mode_sql = IF(
    @global_settings_sales_order_mode_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN sales_order_mode VARCHAR(30) NOT NULL DEFAULT ''SPLIT_BY_VENDOR''',
    'SELECT 1'
);

PREPARE global_settings_sales_order_mode_stmt FROM @global_settings_sales_order_mode_sql;
EXECUTE global_settings_sales_order_mode_stmt;
DEALLOCATE PREPARE global_settings_sales_order_mode_stmt;

UPDATE global_settings
SET store_mode = 'MARKETPLACE'
WHERE store_mode IS NULL OR store_mode = '';

UPDATE global_settings
SET sales_order_mode = 'SPLIT_BY_VENDOR'
WHERE sales_order_mode IS NULL OR sales_order_mode = '';

SET @global_settings_open_graph_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'open_graph_enabled'
);

SET @global_settings_open_graph_enabled_sql = IF(
    @global_settings_open_graph_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN open_graph_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_open_graph_enabled_stmt FROM @global_settings_open_graph_enabled_sql;
EXECUTE global_settings_open_graph_enabled_stmt;
DEALLOCATE PREPARE global_settings_open_graph_enabled_stmt;

SET @global_settings_og_site_name_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'og_site_name'
);

SET @global_settings_og_site_name_sql = IF(
    @global_settings_og_site_name_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN og_site_name VARCHAR(150) NULL',
    'SELECT 1'
);
PREPARE global_settings_og_site_name_stmt FROM @global_settings_og_site_name_sql;
EXECUTE global_settings_og_site_name_stmt;
DEALLOCATE PREPARE global_settings_og_site_name_stmt;

SET @global_settings_public_base_url_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'public_base_url'
);

SET @global_settings_public_base_url_sql = IF(
    @global_settings_public_base_url_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN public_base_url VARCHAR(500) NULL',
    'SELECT 1'
);
PREPARE global_settings_public_base_url_stmt FROM @global_settings_public_base_url_sql;
EXECUTE global_settings_public_base_url_stmt;
DEALLOCATE PREPARE global_settings_public_base_url_stmt;

SET @global_settings_facebook_app_id_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_app_id'
);

SET @global_settings_facebook_app_id_sql = IF(
    @global_settings_facebook_app_id_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_app_id VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE global_settings_facebook_app_id_stmt FROM @global_settings_facebook_app_id_sql;
EXECUTE global_settings_facebook_app_id_stmt;
DEALLOCATE PREPARE global_settings_facebook_app_id_stmt;

SET @global_settings_twitter_card_type_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'twitter_card_type'
);

SET @global_settings_twitter_card_type_sql = IF(
    @global_settings_twitter_card_type_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN twitter_card_type VARCHAR(50) NULL DEFAULT ''summary_large_image''',
    'SELECT 1'
);
PREPARE global_settings_twitter_card_type_stmt FROM @global_settings_twitter_card_type_sql;
EXECUTE global_settings_twitter_card_type_stmt;
DEALLOCATE PREPARE global_settings_twitter_card_type_stmt;

SET @global_settings_social_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'social_sharing_enabled'
);

SET @global_settings_social_sharing_enabled_sql = IF(
    @global_settings_social_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN social_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_social_sharing_enabled_stmt FROM @global_settings_social_sharing_enabled_sql;
EXECUTE global_settings_social_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_social_sharing_enabled_stmt;

SET @global_settings_facebook_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_sharing_enabled'
);

SET @global_settings_facebook_sharing_enabled_sql = IF(
    @global_settings_facebook_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_facebook_sharing_enabled_stmt FROM @global_settings_facebook_sharing_enabled_sql;
EXECUTE global_settings_facebook_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_facebook_sharing_enabled_stmt;

SET @global_settings_messenger_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'messenger_sharing_enabled'
);

SET @global_settings_messenger_sharing_enabled_sql = IF(
    @global_settings_messenger_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN messenger_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_messenger_sharing_enabled_stmt FROM @global_settings_messenger_sharing_enabled_sql;
EXECUTE global_settings_messenger_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_messenger_sharing_enabled_stmt;

SET @global_settings_whatsapp_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'whatsapp_sharing_enabled'
);

SET @global_settings_whatsapp_sharing_enabled_sql = IF(
    @global_settings_whatsapp_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN whatsapp_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_whatsapp_sharing_enabled_stmt FROM @global_settings_whatsapp_sharing_enabled_sql;
EXECUTE global_settings_whatsapp_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_whatsapp_sharing_enabled_stmt;

SET @global_settings_linkedin_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'linkedin_sharing_enabled'
);

SET @global_settings_linkedin_sharing_enabled_sql = IF(
    @global_settings_linkedin_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN linkedin_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_linkedin_sharing_enabled_stmt FROM @global_settings_linkedin_sharing_enabled_sql;
EXECUTE global_settings_linkedin_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_linkedin_sharing_enabled_stmt;

SET @global_settings_twitter_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'twitter_sharing_enabled'
);

SET @global_settings_twitter_sharing_enabled_sql = IF(
    @global_settings_twitter_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN twitter_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_twitter_sharing_enabled_stmt FROM @global_settings_twitter_sharing_enabled_sql;
EXECUTE global_settings_twitter_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_twitter_sharing_enabled_stmt;

SET @global_settings_email_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'email_sharing_enabled'
);

SET @global_settings_email_sharing_enabled_sql = IF(
    @global_settings_email_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN email_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_email_sharing_enabled_stmt FROM @global_settings_email_sharing_enabled_sql;
EXECUTE global_settings_email_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_email_sharing_enabled_stmt;

SET @global_settings_copy_link_sharing_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'copy_link_sharing_enabled'
);

SET @global_settings_copy_link_sharing_enabled_sql = IF(
    @global_settings_copy_link_sharing_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN copy_link_sharing_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_copy_link_sharing_enabled_stmt FROM @global_settings_copy_link_sharing_enabled_sql;
EXECUTE global_settings_copy_link_sharing_enabled_stmt;
DEALLOCATE PREPARE global_settings_copy_link_sharing_enabled_stmt;

SET @global_settings_native_share_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'native_share_enabled'
);

SET @global_settings_native_share_enabled_sql = IF(
    @global_settings_native_share_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN native_share_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_native_share_enabled_stmt FROM @global_settings_native_share_enabled_sql;
EXECUTE global_settings_native_share_enabled_stmt;
DEALLOCATE PREPARE global_settings_native_share_enabled_stmt;

SET @global_settings_referral_links_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'referral_links_enabled'
);

SET @global_settings_referral_links_enabled_sql = IF(
    @global_settings_referral_links_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN referral_links_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_referral_links_enabled_stmt FROM @global_settings_referral_links_enabled_sql;
EXECUTE global_settings_referral_links_enabled_stmt;
DEALLOCATE PREPARE global_settings_referral_links_enabled_stmt;

SET @global_settings_referral_cookie_expiry_days_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'referral_cookie_expiry_days'
);

SET @global_settings_referral_cookie_expiry_days_sql = IF(
    @global_settings_referral_cookie_expiry_days_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN referral_cookie_expiry_days INT NOT NULL DEFAULT 30',
    'SELECT 1'
);
PREPARE global_settings_referral_cookie_expiry_days_stmt FROM @global_settings_referral_cookie_expiry_days_sql;
EXECUTE global_settings_referral_cookie_expiry_days_stmt;
DEALLOCATE PREPARE global_settings_referral_cookie_expiry_days_stmt;

SET @global_settings_facebook_pixel_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_pixel_enabled'
);

SET @global_settings_facebook_pixel_enabled_sql = IF(
    @global_settings_facebook_pixel_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_pixel_enabled BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_facebook_pixel_enabled_stmt FROM @global_settings_facebook_pixel_enabled_sql;
EXECUTE global_settings_facebook_pixel_enabled_stmt;
DEALLOCATE PREPARE global_settings_facebook_pixel_enabled_stmt;

SET @global_settings_fb_browser_tracking_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_browser_tracking_enabled'
);

SET @global_settings_facebook_browser_tracking_enabled_sql = IF(
    @global_settings_fb_browser_tracking_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_browser_tracking_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_facebook_browser_tracking_enabled_stmt FROM @global_settings_facebook_browser_tracking_enabled_sql;
EXECUTE global_settings_facebook_browser_tracking_enabled_stmt;
DEALLOCATE PREPARE global_settings_facebook_browser_tracking_enabled_stmt;

SET @global_settings_fb_capi_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_conversion_api_enabled'
);

SET @global_settings_facebook_conversion_api_enabled_sql = IF(
    @global_settings_fb_capi_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_conversion_api_enabled BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_facebook_conversion_api_enabled_stmt FROM @global_settings_facebook_conversion_api_enabled_sql;
EXECUTE global_settings_facebook_conversion_api_enabled_stmt;
DEALLOCATE PREPARE global_settings_facebook_conversion_api_enabled_stmt;

SET @global_settings_fb_capi_token_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_conversion_api_access_token'
);

SET @global_settings_facebook_conversion_api_access_token_sql = IF(
    @global_settings_fb_capi_token_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_conversion_api_access_token VARCHAR(500) NULL',
    'SELECT 1'
);
PREPARE global_settings_facebook_conversion_api_access_token_stmt FROM @global_settings_facebook_conversion_api_access_token_sql;
EXECUTE global_settings_facebook_conversion_api_access_token_stmt;
DEALLOCATE PREPARE global_settings_facebook_conversion_api_access_token_stmt;

SET @global_settings_facebook_test_event_code_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_test_event_code'
);

SET @global_settings_facebook_test_event_code_sql = IF(
    @global_settings_facebook_test_event_code_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_test_event_code VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE global_settings_facebook_test_event_code_stmt FROM @global_settings_facebook_test_event_code_sql;
EXECUTE global_settings_facebook_test_event_code_stmt;
DEALLOCATE PREPARE global_settings_facebook_test_event_code_stmt;

SET @global_settings_facebook_debug_mode_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'facebook_debug_mode'
);

SET @global_settings_facebook_debug_mode_sql = IF(
    @global_settings_facebook_debug_mode_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN facebook_debug_mode BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_facebook_debug_mode_stmt FROM @global_settings_facebook_debug_mode_sql;
EXECUTE global_settings_facebook_debug_mode_stmt;
DEALLOCATE PREPARE global_settings_facebook_debug_mode_stmt;

SET @global_settings_google_analytics_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'google_analytics_enabled'
);

SET @global_settings_google_analytics_enabled_sql = IF(
    @global_settings_google_analytics_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN google_analytics_enabled BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_google_analytics_enabled_stmt FROM @global_settings_google_analytics_enabled_sql;
EXECUTE global_settings_google_analytics_enabled_stmt;
DEALLOCATE PREPARE global_settings_google_analytics_enabled_stmt;

SET @global_settings_ga4_ecom_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'ga4_enhanced_ecommerce_enabled'
);

SET @global_settings_ga4_enhanced_ecommerce_enabled_sql = IF(
    @global_settings_ga4_ecom_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN ga4_enhanced_ecommerce_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_ga4_enhanced_ecommerce_enabled_stmt FROM @global_settings_ga4_enhanced_ecommerce_enabled_sql;
EXECUTE global_settings_ga4_enhanced_ecommerce_enabled_stmt;
DEALLOCATE PREPARE global_settings_ga4_enhanced_ecommerce_enabled_stmt;

SET @global_settings_ga4_debug_mode_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'ga4_debug_mode'
);

SET @global_settings_ga4_debug_mode_sql = IF(
    @global_settings_ga4_debug_mode_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN ga4_debug_mode BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_ga4_debug_mode_stmt FROM @global_settings_ga4_debug_mode_sql;
EXECUTE global_settings_ga4_debug_mode_stmt;
DEALLOCATE PREPARE global_settings_ga4_debug_mode_stmt;

SET @global_settings_google_consent_mode_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'google_consent_mode_enabled'
);

SET @global_settings_google_consent_mode_enabled_sql = IF(
    @global_settings_google_consent_mode_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN google_consent_mode_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_google_consent_mode_enabled_stmt FROM @global_settings_google_consent_mode_enabled_sql;
EXECUTE global_settings_google_consent_mode_enabled_stmt;
DEALLOCATE PREPARE global_settings_google_consent_mode_enabled_stmt;

SET @global_settings_google_tag_manager_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'google_tag_manager_enabled'
);

SET @global_settings_google_tag_manager_enabled_sql = IF(
    @global_settings_google_tag_manager_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN google_tag_manager_enabled BIT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE global_settings_google_tag_manager_enabled_stmt FROM @global_settings_google_tag_manager_enabled_sql;
EXECUTE global_settings_google_tag_manager_enabled_stmt;
DEALLOCATE PREPARE global_settings_google_tag_manager_enabled_stmt;

SET @global_settings_gtm_container_id_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'gtm_container_id'
);

SET @global_settings_gtm_container_id_sql = IF(
    @global_settings_gtm_container_id_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN gtm_container_id VARCHAR(50) NULL',
    'SELECT 1'
);
PREPARE global_settings_gtm_container_id_stmt FROM @global_settings_gtm_container_id_sql;
EXECUTE global_settings_gtm_container_id_stmt;
DEALLOCATE PREPARE global_settings_gtm_container_id_stmt;

SET @global_settings_server_side_gtm_url_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'server_side_gtm_url'
);

SET @global_settings_server_side_gtm_url_sql = IF(
    @global_settings_server_side_gtm_url_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN server_side_gtm_url VARCHAR(500) NULL',
    'SELECT 1'
);
PREPARE global_settings_server_side_gtm_url_stmt FROM @global_settings_server_side_gtm_url_sql;
EXECUTE global_settings_server_side_gtm_url_stmt;
DEALLOCATE PREPARE global_settings_server_side_gtm_url_stmt;

SET @global_settings_tracking_implementation_mode_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'tracking_implementation_mode'
);

SET @global_settings_tracking_implementation_mode_sql = IF(
    @global_settings_tracking_implementation_mode_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN tracking_implementation_mode VARCHAR(30) NOT NULL DEFAULT ''DIRECT''',
    'SELECT 1'
);
PREPARE global_settings_tracking_implementation_mode_stmt FROM @global_settings_tracking_implementation_mode_sql;
EXECUTE global_settings_tracking_implementation_mode_stmt;
DEALLOCATE PREPARE global_settings_tracking_implementation_mode_stmt;

SET @global_settings_cookie_consent_enabled_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'cookie_consent_enabled'
);

SET @global_settings_cookie_consent_enabled_sql = IF(
    @global_settings_cookie_consent_enabled_column_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN cookie_consent_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE global_settings_cookie_consent_enabled_stmt FROM @global_settings_cookie_consent_enabled_sql;
EXECUTE global_settings_cookie_consent_enabled_stmt;
DEALLOCATE PREPARE global_settings_cookie_consent_enabled_stmt;

UPDATE global_settings
SET twitter_card_type = 'summary_large_image'
WHERE twitter_card_type IS NULL OR twitter_card_type = '';

UPDATE global_settings
SET referral_cookie_expiry_days = 30
WHERE referral_cookie_expiry_days IS NULL OR referral_cookie_expiry_days < 1;

UPDATE global_settings
SET tracking_implementation_mode = 'DIRECT'
WHERE tracking_implementation_mode IS NULL OR tracking_implementation_mode = '';
