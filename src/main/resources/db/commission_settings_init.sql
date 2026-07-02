-- Create commission_settings table
-- This table stores marketplace commission rates configured by administrators
-- Supports hierarchy: Product > Vendor > Category > Default

CREATE TABLE IF NOT EXISTS commission_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36),
    commission_type VARCHAR(50) NOT NULL,
    commission_rate DECIMAL(10, 2) NOT NULL,
    description TEXT,
    category_id BIGINT,
    vendor_id BIGINT,
    product_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Unique constraint to prevent duplicate commission settings
    UNIQUE KEY uk_commission_settings_uuid (uuid),
    UNIQUE KEY uk_commission_settings (commission_type, category_id, vendor_id, product_id),

    -- Indexes for common queries
    INDEX idx_commission_type_status (commission_type, status),
    INDEX idx_category_id_status (category_id, status),
    INDEX idx_vendor_id_status (vendor_id, status),
    INDEX idx_product_id_status (product_id, status),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default commission setting if it doesn't exist
-- Adjust the percentage (currently 20%) based on your business needs
INSERT INTO commission_settings (
    uuid,
    commission_type,
    commission_rate,
    description,
    status,
    created_at,
    updated_at
) SELECT
    UUID(),
    'DEFAULT',
    20.00,
    'Default marketplace commission rate for all products',
    'ACTIVE',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM commission_settings
    WHERE commission_type = 'DEFAULT'
      AND status = 'ACTIVE'
);

UPDATE commission_settings
SET commission_rate = 20.00,
    updated_at = NOW()
WHERE commission_type = 'DEFAULT'
  AND status = 'ACTIVE';
