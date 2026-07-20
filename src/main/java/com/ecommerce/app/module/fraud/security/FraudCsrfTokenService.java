package com.ecommerce.app.module.fraud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class FraudCsrfTokenService {

    public static final String SESSION_ATTRIBUTE = "FRAUD_CSRF_TOKEN";
    public static final String PARAMETER_NAME = "_fraudCsrfToken";
    public static final String HEADER_NAME = "X-FRAUD-CSRF-TOKEN";

    private final SecureRandom secureRandom = new SecureRandom();

    public String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute(SESSION_ATTRIBUTE);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    public boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTRIBUTE);
        if (!(expected instanceof String expectedToken) || expectedToken.isBlank()) {
            return false;
        }
        String supplied = request.getHeader(HEADER_NAME);
        if (supplied == null || supplied.isBlank()) {
            supplied = request.getParameter(PARAMETER_NAME);
        }
        return expectedToken.equals(supplied);
    }
}
