package com.ecommerce.app.module.fraud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FraudAdminCsrfInterceptor implements HandlerInterceptor {

    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final FraudCsrfTokenService csrfTokenService;

    public FraudAdminCsrfInterceptor(FraudCsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!UNSAFE_METHODS.contains(request.getMethod())) {
            return true;
        }
        if (csrfTokenService.isValid(request)) {
            return true;
        }
        response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid fraud form token.");
        return false;
    }
}
