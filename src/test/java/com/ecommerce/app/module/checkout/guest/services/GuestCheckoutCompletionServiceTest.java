package com.ecommerce.app.module.checkout.guest.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class GuestCheckoutCompletionServiceTest {

    @Test
    void otpAuditFailureDoesNotRetainReusableSessionProof() {
        GuestCheckoutSessionService sessionService = mock(GuestCheckoutSessionService.class);
        GuestCheckoutOtpService otpService = mock(GuestCheckoutOtpService.class);
        HttpSession session = mock(HttpSession.class);
        GuestCheckoutSession guestSession = new GuestCheckoutSession();
        guestSession.setOtpVerificationUuid("otp-verification-1");
        when(sessionService.current(session)).thenReturn(Optional.of(guestSession));
        doThrow(new IllegalStateException("database unavailable"))
                .when(otpService).markUsed("otp-verification-1");
        GuestCheckoutCompletionService service = new GuestCheckoutCompletionService(sessionService, otpService);

        assertDoesNotThrow(() -> service.consumeProof(session));

        InOrder ordered = inOrder(sessionService, otpService);
        ordered.verify(sessionService).clear(session);
        ordered.verify(otpService).markUsed("otp-verification-1");
        verify(sessionService).current(session);
    }
}
