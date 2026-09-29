-- Move vendor-logo and product featured-image upload limits into the
-- administrator-managed singleton global_settings record.
ALTER TABLE global_settings
    ADD COLUMN vendor_logo_max_file_size_bytes BIGINT NOT NULL DEFAULT 2097152,
    ADD COLUMN vendor_logo_max_width INT NOT NULL DEFAULT 5000,
    ADD COLUMN vendor_logo_max_height INT NOT NULL DEFAULT 5000,
    ADD COLUMN product_featured_image_min_file_size_bytes BIGINT NOT NULL DEFAULT 10240,
    ADD COLUMN product_featured_image_max_file_size_bytes BIGINT NOT NULL DEFAULT 10485760,
    ADD COLUMN product_featured_image_min_width INT NOT NULL DEFAULT 400,
    ADD COLUMN product_featured_image_min_height INT NOT NULL DEFAULT 300,
    ADD COLUMN product_featured_image_max_width INT NOT NULL DEFAULT 8000,
    ADD COLUMN product_featured_image_max_height INT NOT NULL DEFAULT 8000,
    ADD COLUMN product_featured_image_output_max_width INT NOT NULL DEFAULT 800,
    ADD COLUMN product_featured_image_output_max_height INT NOT NULL DEFAULT 600,
    ADD CONSTRAINT ck_global_settings_image_upload_policy
        CHECK (
            vendor_logo_max_file_size_bytes BETWEEN 1 AND 10485760
            AND vendor_logo_max_width BETWEEN 1 AND 8000
            AND vendor_logo_max_height BETWEEN 1 AND 8000
            AND product_featured_image_min_file_size_bytes BETWEEN 1 AND 10485760
            AND product_featured_image_max_file_size_bytes BETWEEN product_featured_image_min_file_size_bytes AND 10485760
            AND product_featured_image_min_width BETWEEN 1 AND product_featured_image_max_width
            AND product_featured_image_min_height BETWEEN 1 AND product_featured_image_max_height
            AND product_featured_image_max_width <= 8000
            AND product_featured_image_max_height <= 8000
            AND product_featured_image_output_max_width BETWEEN 1 AND product_featured_image_max_width
            AND product_featured_image_output_max_height BETWEEN 1 AND product_featured_image_max_height
        );
