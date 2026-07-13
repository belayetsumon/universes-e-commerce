package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GuestCheckoutSessionService {

    public static final String SESSION_KEY = "guestCheckoutSession";

    public Optional<GuestCheckoutSession> current(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }
        Object value = session.getAttribute(SESSION_KEY);
        if (!(value instanceof GuestCheckoutSession guestSession)) {
            return Optional.empty();
        }
        if (!guestSession.isActive(LocalDateTime.now())) {
            session.removeAttribute(SESSION_KEY);
            return Optional.empty();
        }
        return Optional.of(guestSession);
    }

    public boolean isVerified(HttpSession session) {
        return current(session).isPresent();
    }

    public void store(HttpSession session, GuestCheckoutSession guestSession) {
        session.setAttribute(SESSION_KEY, guestSession);
    }

    public void clear(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
        }
    }
}
