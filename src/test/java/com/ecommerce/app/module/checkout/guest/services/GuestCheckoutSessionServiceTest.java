package com.ecommerce.app.module.checkout.guest.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class GuestCheckoutSessionServiceTest {

    private final GuestCheckoutSessionService service = new GuestCheckoutSessionService(
            new MobileNumberNormalizationService()
    );

    @Test
    void verifiedProofAuthorizesOnlyItsNormalizedFulfillmentMobile() {
        MockHttpSession session = new MockHttpSession();
        GuestCheckoutSession guest = new GuestCheckoutSession();
        guest.setUserId(17L);
        guest.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        guest.setMobileVerificationRequired(true);
        guest.setMobileVerificationStatus(MobileVerificationStatus.VERIFIED);
        guest.setVerifiedMobile("8801712345678");
        service.store(session, guest);

        assertTrue(service.isVerifiedContactMobile(session, "+880 1712-345678"));
        assertFalse(service.isVerifiedContactMobile(session, "01812345678"));
        assertFalse(service.isVerifiedContactMobile(session, null));
    }
}
