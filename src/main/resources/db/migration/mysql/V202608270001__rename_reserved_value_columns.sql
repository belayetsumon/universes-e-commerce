-- MySQL 8 reserves VALUE in several grammar contexts. Rename the two legacy
-- columns without changing their types, nullability, defaults, or stored data.

SET @coupon_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'promotions_coupon'
);
SET @coupon_legacy_value_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'promotions_coupon'
      AND COLUMN_NAME = 'value'
);
SET @coupon_value_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'promotions_coupon'
      AND COLUMN_NAME = 'coupon_value'
);
SET @coupon_value_rename_sql = CASE
    WHEN @coupon_table_exists = 0 THEN 'SELECT 1'
    WHEN @coupon_legacy_value_exists = 1 AND @coupon_value_exists = 0
        THEN 'ALTER TABLE promotions_coupon RENAME COLUMN `value` TO coupon_value'
    WHEN @coupon_legacy_value_exists = 0 AND @coupon_value_exists = 1 THEN 'SELECT 1'
    ELSE 'SELECT * FROM promotions_coupon_value_column_state_is_ambiguous__repair_before_deployment'
END;
PREPARE coupon_value_rename_statement FROM @coupon_value_rename_sql;
EXECUTE coupon_value_rename_statement;
DEALLOCATE PREPARE coupon_value_rename_statement;

SET @attribute_option_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_catalog_attribute_option'
);
SET @attribute_option_legacy_value_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_catalog_attribute_option'
      AND COLUMN_NAME = 'value'
);
SET @attribute_option_value_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_catalog_attribute_option'
      AND COLUMN_NAME = 'option_value'
);
SET @attribute_option_value_rename_sql = CASE
    WHEN @attribute_option_table_exists = 0 THEN 'SELECT 1'
    WHEN @attribute_option_legacy_value_exists = 1 AND @attribute_option_value_exists = 0
        THEN 'ALTER TABLE product_catalog_attribute_option RENAME COLUMN `value` TO option_value'
    WHEN @attribute_option_legacy_value_exists = 0 AND @attribute_option_value_exists = 1 THEN 'SELECT 1'
    ELSE 'SELECT * FROM product_catalog_attribute_option_value_column_state_is_ambiguous__repair_before_deployment'
END;
PREPARE attribute_option_value_rename_statement FROM @attribute_option_value_rename_sql;
EXECUTE attribute_option_value_rename_statement;
DEALLOCATE PREPARE attribute_option_value_rename_statement;
