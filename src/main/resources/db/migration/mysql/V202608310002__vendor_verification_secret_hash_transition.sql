-- Vendor email tokens and mobile OTP values are hashed by application code
-- after this migration. Clear any pending plaintext secrets so old raw values
-- do not remain usable or stored after deployment.
SET @vendor_verifications_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'vendor_verifications'
);

SET @vendor_verification_secret_sql = CASE
    WHEN @vendor_verifications_table_exists = 0 THEN 'SELECT 1'
    ELSE '
        UPDATE vendor_verifications
        SET token = NULL,
            token_created_at = NULL,
            otp = NULL,
            otp_created_at = NULL
        WHERE (email_verified = 0 AND token IS NOT NULL)
           OR (mobile_verified = 0 AND otp IS NOT NULL)
           OR (email_verified = 1 AND token IS NOT NULL)
           OR (mobile_verified = 1 AND otp IS NOT NULL)
    '
END;

PREPARE vendor_verification_secret_statement FROM @vendor_verification_secret_sql;
EXECUTE vendor_verification_secret_statement;
DEALLOCATE PREPARE vendor_verification_secret_statement;
