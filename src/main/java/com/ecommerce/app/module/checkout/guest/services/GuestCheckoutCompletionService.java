package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Completes the session-side guest proof after an order has committed. */
@Service
public class GuestCheckoutCompletionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestCheckoutCompletionService.class);

    private final GuestCheckoutSessionService sessionService;
    private final GuestCheckoutOtpService otpService;

    public GuestCheckoutCompletionService(
            GuestCheckoutSessionService sessionService,
            GuestCheckoutOtpService otpService
    ) {
        this.sessionService = sessionService;
        this.otpService = otpService;
    }

    public void consumeProof(HttpSession session) {
        GuestCheckoutSession guestSession = sessionService.current(session).orElse(null);
        if (guestSession == null) {
            return;
        }

        // Invalidate the reusable session proof first. Database status is an
        // audit marker and must not poison the next cart/request key if its
        // best-effort post-commit update is temporarily unavailable.
        sessionService.clear(session);
        try {
            otpService.markUsed(guestSession.getOtpVerificationUuid());
        } catch (RuntimeException markUsedFailure) {
            LOGGER.error(
                    "Guest checkout committed and its session proof was cleared, but OTP verification {} could not be marked USED.",
                    guestSession.getOtpVerificationUuid(),
                    markUsedFailure
            );
        }
    }
}
