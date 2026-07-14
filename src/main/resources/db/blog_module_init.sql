-- Blog Management Module initialization helper.
-- Target: PostgreSQL. Apply manually if Hibernate DDL auto is disabled.
-- This script focuses on the core operational tables. Hibernate can create the
-- remaining engagement/bridge tables from the JPA model, or DBAs can extend this
-- file using the entity annotations under com.ecommerce.app.module.blog.model.

CREATE TABLE IF NOT EXISTS blog_categories (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description VARCHAR(500),
    parent_id BIGINT REFERENCES blog_categories(id),
    seo_title VARCHAR(180),
    meta_description VARCHAR(320),
    CONSTRAINT uk_blog_category_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS blog_tags (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    CONSTRAINT uk_blog_tag_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS blog_series (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    CONSTRAINT uk_blog_series_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS blogs (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    title VARCHAR(220) NOT NULL,
    slug VARCHAR(240) NOT NULL,
    excerpt VARCHAR(500),
    content_html TEXT NOT NULL,
    content_plain_text TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    visibility VARCHAR(40) NOT NULL DEFAULT 'PUBLIC',
    category_id BIGINT REFERENCES blog_categories(id),
    series_id BIGINT REFERENCES blog_series(id),
    featured_image_url VARCHAR(500),
    featured_image_alt VARCHAR(220),
    password_hash VARCHAR(120),
    language_code VARCHAR(10) NOT NULL DEFAULT 'en',
    country_codes VARCHAR(500),
    device_rules VARCHAR(250),
    customer_segment VARCHAR(120),
    sticky_post BOOLEAN NOT NULL DEFAULT FALSE,
    featured_post BOOLEAN NOT NULL DEFAULT FALSE,
    allow_comments BOOLEAN NOT NULL DEFAULT TRUE,
    scheduled_at TIMESTAMP,
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    reading_time_minutes INTEGER NOT NULL DEFAULT 1,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    share_count BIGINT NOT NULL DEFAULT 0,
    template_key VARCHAR(120),
    utm_campaign VARCHAR(160),
    CONSTRAINT uk_blog_slug_language UNIQUE (slug, language_code)
);

CREATE TABLE IF NOT EXISTS blog_tag_map (
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES blog_tags(id) ON DELETE CASCADE,
    CONSTRAINT uk_blog_tag_map UNIQUE (blog_id, tag_id)
);

CREATE TABLE IF NOT EXISTS blog_seo (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    seo_title VARCHAR(180),
    meta_description VARCHAR(320),
    meta_keywords VARCHAR(500),
    canonical_url VARCHAR(500),
    robots_meta VARCHAR(80),
    open_graph_title VARCHAR(180),
    open_graph_description VARCHAR(320),
    open_graph_image VARCHAR(500),
    twitter_card VARCHAR(80),
    json_ld TEXT,
    breadcrumb_schema TEXT,
    faq_schema TEXT,
    article_schema TEXT,
    redirect_from VARCHAR(500),
    CONSTRAINT uk_blog_seo_blog UNIQUE (blog_id)
);

CREATE TABLE IF NOT EXISTS blog_revisions (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    revision_number INTEGER NOT NULL,
    title VARCHAR(220) NOT NULL,
    slug VARCHAR(240) NOT NULL,
    content_html TEXT NOT NULL,
    change_summary TEXT,
    CONSTRAINT uk_blog_revision_number UNIQUE (blog_id, revision_number)
);

CREATE TABLE IF NOT EXISTS blog_comments (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES blog_comments(id),
    user_id BIGINT,
    guest_name VARCHAR(120) NOT NULL,
    guest_email VARCHAR(180),
    comment_text TEXT NOT NULL,
    moderation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ip_hash VARCHAR(128),
    user_agent VARCHAR(500),
    report_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_subscribers (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    email VARCHAR(180) NOT NULL,
    name VARCHAR(160),
    subscriber_status VARCHAR(30) NOT NULL DEFAULT 'SUBSCRIBED',
    source VARCHAR(100),
    consent_at TIMESTAMP,
    CONSTRAINT uk_blog_subscriber_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS blog_related_products (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    relation_type VARCHAR(80),
    cta_label VARCHAR(80),
    CONSTRAINT uk_blog_related_product UNIQUE (blog_id, product_id)
);

CREATE TABLE IF NOT EXISTS blog_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES usermodule_users(id),
    favorite_flag BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_blog_bookmark_user UNIQUE (blog_id, user_id)
);

CREATE TABLE IF NOT EXISTS blog_views (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    active_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES usermodule_users(id),
    visitor_key VARCHAR(120),
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reading_seconds INTEGER,
    scroll_depth INTEGER,
    utm_campaign VARCHAR(160)
);

CREATE INDEX IF NOT EXISTS idx_blog_status_publish ON blogs(status, published_at, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_blog_category ON blogs(category_id);
CREATE INDEX IF NOT EXISTS idx_blog_featured_sticky ON blogs(featured_post, sticky_post, sort_order);
CREATE INDEX IF NOT EXISTS idx_blog_tag_map_blog ON blog_tag_map(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_tag_map_tag ON blog_tag_map(tag_id);
CREATE INDEX IF NOT EXISTS idx_blog_comment_blog_status ON blog_comments(blog_id, moderation_status, created_at);
CREATE INDEX IF NOT EXISTS idx_blog_subscriber_status ON blog_subscribers(subscriber_status, active_flag);
CREATE INDEX IF NOT EXISTS idx_blog_related_product_blog ON blog_related_products(blog_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_blog_bookmark_user ON blog_bookmarks(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_blog_view_blog_time ON blog_views(blog_id, viewed_at);
CREATE INDEX IF NOT EXISTS idx_blog_view_user_time ON blog_views(user_id, viewed_at);
