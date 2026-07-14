ALTER TABLE global_settings
    ADD COLUMN IF NOT EXISTS open_graph_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS og_site_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS public_base_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS facebook_app_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS twitter_card_type VARCHAR(50) DEFAULT 'summary_large_image',
    ADD COLUMN IF NOT EXISTS social_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS facebook_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS messenger_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS whatsapp_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS linkedin_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS twitter_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS email_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS copy_link_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS native_share_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS referral_links_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS referral_cookie_expiry_days INTEGER NOT NULL DEFAULT 30,
    ADD COLUMN IF NOT EXISTS facebook_pixel_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS facebook_browser_tracking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS facebook_conversion_api_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS facebook_conversion_api_access_token VARCHAR(500),
    ADD COLUMN IF NOT EXISTS facebook_test_event_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS facebook_debug_mode BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS google_analytics_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ga4_enhanced_ecommerce_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS ga4_debug_mode BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS google_consent_mode_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS google_tag_manager_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS gtm_container_id VARCHAR(50),
    ADD COLUMN IF NOT EXISTS server_side_gtm_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS tracking_implementation_mode VARCHAR(30) NOT NULL DEFAULT 'DIRECT',
    ADD COLUMN IF NOT EXISTS cookie_consent_enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS social_share_event (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(40) NOT NULL,
    page_type VARCHAR(50) NOT NULL,
    public_entity_reference VARCHAR(120),
    platform VARCHAR(40) NOT NULL,
    customer_user_id BIGINT,
    guest_tracking_id VARCHAR(80),
    referral_code_present BOOLEAN NOT NULL DEFAULT FALSE,
    referral_code_hash VARCHAR(128),
    public_url VARCHAR(1000) NOT NULL,
    campaign_source VARCHAR(120),
    device_category VARCHAR(40),
    ip_hash VARCHAR(128),
    user_agent_hash VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_social_share_event_created_at ON social_share_event(created_at);
CREATE INDEX IF NOT EXISTS idx_social_share_event_platform ON social_share_event(platform);
CREATE INDEX IF NOT EXISTS idx_social_share_event_page_type ON social_share_event(page_type);
CREATE INDEX IF NOT EXISTS idx_social_share_event_entity ON social_share_event(public_entity_reference);
CREATE INDEX IF NOT EXISTS idx_social_share_event_customer ON social_share_event(customer_user_id);

CREATE TABLE IF NOT EXISTS tracking_delivery_event (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    provider VARCHAR(40) NOT NULL,
    event_name VARCHAR(80) NOT NULL,
    event_id VARCHAR(120) NOT NULL,
    entity_reference VARCHAR(120),
    payload TEXT,
    delivery_status VARCHAR(40) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    last_attempt_at TIMESTAMP,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tracking_delivery_provider_event ON tracking_delivery_event(provider, event_id);
CREATE INDEX IF NOT EXISTS idx_tracking_delivery_status_next ON tracking_delivery_event(delivery_status, next_attempt_at);
