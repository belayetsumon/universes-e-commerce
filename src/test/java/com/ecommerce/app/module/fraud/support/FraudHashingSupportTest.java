package com.ecommerce.app.module.fraud.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FraudHashingSupportTest {

    @Test
    void canonicalIdentifierHashHashesRawValues() {
        assertEquals(
                FraudHashingSupport.sha256("browser-device-token"),
                FraudHashingSupport.canonicalIdentifierHash(" browser-device-token ")
        );
    }

    @Test
    void canonicalIdentifierHashDoesNotHashSha256Twice() {
        String digest = FraudHashingSupport.sha256("browser-device-token");

        assertEquals(digest, FraudHashingSupport.canonicalIdentifierHash(digest.toUpperCase()));
    }

    @Test
    void canonicalIdentifierHashRejectsBlankValues() {
        assertNull(FraudHashingSupport.canonicalIdentifierHash("  "));
    }
}
