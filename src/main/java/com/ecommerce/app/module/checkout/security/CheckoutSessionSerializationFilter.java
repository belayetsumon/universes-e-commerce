package com.ecommerce.app.module.checkout.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Serializes checkout-state mutations for one HTTP session. Servlet containers
 * may process two requests carrying the same session cookie concurrently, so a
 * placement request could otherwise race quantity, delivery-charge, OTP, or
 * address mutations and persist data different from its idempotency payload.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CheckoutSessionSerializationFilter extends OncePerRequestFilter {

    private static final int LOCK_STRIPES = 4096;
    private final ReentrantLock[] locks = new ReentrantLock[LOCK_STRIPES];

    public CheckoutSessionSerializationFilter() {
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new ReentrantLock();
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !isSerializedMutation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ReentrantLock lock = locks[Math.floorMod(session.getId().hashCode(), locks.length)];
        lock.lock();
        try {
            filterChain.doFilter(request, response);
        } finally {
            lock.unlock();
        }
    }

    boolean isSerializedMutation(HttpServletRequest request) {
        if (request == null || !isUnsafeMethod(request.getMethod())) {
            return false;
        }
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        return path != null && (path.equals("/cart")
                || path.startsWith("/cart/")
                || path.equals("/carts")
                || path.startsWith("/carts/")
                || path.equals("/cart_address")
                || path.startsWith("/cart_address/")
                || path.equals("/district")
                || path.startsWith("/district/")
                || path.equals("/checkout")
                || path.startsWith("/checkout/")
                || path.equals("/order")
                || path.startsWith("/order/"));
    }

    private boolean isUnsafeMethod(String method) {
        if (method == null) {
            return false;
        }
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }
}
