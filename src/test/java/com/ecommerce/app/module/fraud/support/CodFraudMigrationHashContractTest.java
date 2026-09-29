package com.ecommerce.app.module.fraud.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class CodFraudMigrationHashContractTest {

    @Test
    void customerIdentityBackfillsUseTheSameLowercaseInputAsJavaHashing() throws IOException {
        String mysql = read(
                "db/migration/mysql/V202608240003__cod_fraud_postflight_guards.sql"
        );

        assertTrue(mysql.contains("LOWER(SHA2(LOWER(CONCAT("));
    }

    @Test
    void mysqlVelocityRollupIsTransactionalAndAllRequiredTablesUseInnoDb() throws IOException {
        String preflight = read(
                "db/migration/mysql/V202608230001__cod_fraud_schema_preflight.sql"
        );
        String migration = read(
                "db/migration/mysql/V202608240002__checkout_idempotency_and_fraud_context.sql"
        );

        assertTrue(preflight.contains("@cod_fraud_innodb_table_count = 9"));
        assertTrue(preflight.contains("'fraud_velocity_counters'"));
        assertTrue(preflight.contains("'fraud_device_identities'"));
        assertTrue(preflight.contains("'fraud_cod_risk_profiles'"));
        int transactionStart = migration.indexOf("START TRANSACTION;");
        int rollupUpdate = migration.indexOf("UPDATE fraud_velocity_counters AS counter");
        int duplicateDelete = migration.indexOf("DELETE counter");
        int commit = migration.indexOf("COMMIT;");
        assertTrue(transactionStart >= 0 && transactionStart < rollupUpdate);
        assertTrue(rollupUpdate < duplicateDelete && duplicateDelete < commit);
    }

    @Test
    void mysqlBlocklistMigrationEnforcesTheRuntimeExpiryAndLookupContract() throws IOException {
        String mysql = read(
                "db/migration/mysql/V202608260005__fraud_blocklist_invariants.sql"
        );

        assertTrue(mysql.contains("ck_fraud_blocklist_expiry_contract"));
        assertTrue(mysql.contains("idx_fraud_block_effective_lookup"));
        assertTrue(mysql.contains("block_type"));
        assertTrue(mysql.contains("hashed_value"));
        assertTrue(mysql.contains("expires_at"));
    }

    private String read(String resourcePath) throws IOException {
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
