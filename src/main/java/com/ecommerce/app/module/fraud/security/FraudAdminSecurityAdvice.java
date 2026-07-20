package com.ecommerce.app.module.fraud.security;

import com.ecommerce.app.module.fraud.controller.FraudAdminController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = FraudAdminController.class)
public class FraudAdminSecurityAdvice {

    private final FraudCsrfTokenService csrfTokenService;

    public FraudAdminSecurityAdvice(FraudCsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @ModelAttribute("fraudCsrfToken")
    public String fraudCsrfToken(HttpServletRequest request) {
        return csrfTokenService.getOrCreateToken(request);
    }

    @ModelAttribute("fraudCsrfParameterName")
    public String fraudCsrfParameterName() {
        return FraudCsrfTokenService.PARAMETER_NAME;
    }
}
