-- Collapse the legacy stock/preorder flags into one explicit availability mode.
-- Existing rows with preorder enabled retain preorder behavior; every other row
-- receives the safe managed-stock default.

SET @product_table_valid = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND ENGINE = 'InnoDB'
);
SET @product_table_guard_sql = IF(
    @product_table_valid = 1,
    'SELECT 1',
    'SELECT * FROM product_availability_table_missing_or_not_innodb__repair_before_deployment'
);
PREPARE product_availability_table_guard FROM @product_table_guard_sql;
EXECUTE product_availability_table_guard;
DEALLOCATE PREPARE product_availability_table_guard;

SET @product_availability_columns = (
    SELECT COUNT(DISTINCT COLUMN_NAME)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND COLUMN_NAME IN ('manage_stock', 'allow_preorder')
);
SET @product_availability_column_guard_sql = IF(
    @product_availability_columns = 2,
    'SELECT 1',
    'SELECT * FROM product_availability_columns_missing__repair_before_deployment'
);
PREPARE product_availability_column_guard FROM @product_availability_column_guard_sql;
EXECUTE product_availability_column_guard;
DEALLOCATE PREPARE product_availability_column_guard;

UPDATE product
SET manage_stock = CASE WHEN allow_preorder = 1 THEN 0 ELSE 1 END,
    allow_preorder = CASE WHEN allow_preorder = 1 THEN 1 ELSE 0 END
WHERE manage_stock IS NULL
   OR allow_preorder IS NULL
   OR (manage_stock = 1 AND allow_preorder = 1)
   OR (manage_stock = 0 AND allow_preorder = 0)
   OR (manage_stock = 0 AND allow_preorder = 1);

ALTER TABLE product
    MODIFY manage_stock BOOLEAN NOT NULL DEFAULT TRUE,
    MODIFY allow_preorder BOOLEAN NOT NULL DEFAULT FALSE;

SET @product_availability_constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND CONSTRAINT_NAME = 'ck_product_availability_mode'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @product_availability_constraint_sql = IF(
    @product_availability_constraint_exists = 0,
    'ALTER TABLE product ADD CONSTRAINT ck_product_availability_mode CHECK ((manage_stock = 1 AND allow_preorder = 0) OR (manage_stock = 0 AND allow_preorder = 1))',
    'SELECT 1'
);
PREPARE product_availability_constraint_statement FROM @product_availability_constraint_sql;
EXECUTE product_availability_constraint_statement;
DEALLOCATE PREPARE product_availability_constraint_statement;

