package com.ecommerce.app.module.checkout.guest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MobileNumberNormalizationServiceTest {

    private final MobileNumberNormalizationService service = new MobileNumberNormalizationService();

    @Test
    void normalizesBangladeshMobileNumbersToSingleStoredFormat() {
        assertEquals("8801712345678", service.normalizeBangladeshMobile("01712345678"));
        assertEquals("8801712345678", service.normalizeBangladeshMobile("+8801712345678"));
        assertEquals("8801712345678", service.normalizeBangladeshMobile("8801712345678"));
        assertEquals("8801712345678", service.normalizeBangladeshMobile("1712345678"));
    }

    @Test
    void rejectsInvalidBangladeshMobileNumbers() {
        assertThrows(IllegalArgumentException.class, () -> service.normalizeBangladeshMobile("012345"));
        assertThrows(IllegalArgumentException.class, () -> service.normalizeBangladeshMobile("8801112345678"));
        assertThrows(IllegalArgumentException.class, () -> service.normalizeBangladeshMobile("email@example.com"));
    }
}
