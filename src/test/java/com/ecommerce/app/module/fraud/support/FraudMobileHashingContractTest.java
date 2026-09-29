package com.ecommerce.app.module.fraud.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FraudMobileHashingContractTest {

    @Test
    void commonBangladeshRepresentationsShareOneCanonicalHash() {
        String canonicalHash = FraudHashingSupport.canonicalBangladeshMobileHash("01712345678");

        assertEquals(canonicalHash, FraudHashingSupport.canonicalBangladeshMobileHash("8801712345678"));
        assertEquals(canonicalHash, FraudHashingSupport.canonicalBangladeshMobileHash("+880 1712-345678"));
        assertEquals(canonicalHash, FraudHashingSupport.canonicalBangladeshMobileHash("008801712345678"));
    }

    @Test
    void dualReadCandidatesIncludeCanonicalAndLegacyLocalHashes() {
        List<String> candidates = FraudHashingSupport
                .bangladeshMobileHashCandidates("+8801712345678");

        assertEquals(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"),
                candidates.get(0)
        );
        assertTrue(candidates.contains(FraudHashingSupport.sha256("01712345678")));
        assertTrue(candidates.contains(FraudHashingSupport.sha256("+8801712345678")));
    }

    @Test
    void invalidMobileIsRejectedByStrictAdministrativeHashing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FraudHashingSupport.canonicalBangladeshMobileHash("555-not-mobile")
        );
        assertTrue(FraudHashingSupport.bangladeshMobileHashCandidates("555-not-mobile").isEmpty());
    }
}
