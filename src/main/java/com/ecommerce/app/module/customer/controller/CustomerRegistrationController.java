package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.customer.services.CustomerRegistrationException;
import com.ecommerce.app.module.customer.services.CustomerRegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer_registration")
public class CustomerRegistrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerRegistrationController.class);
    private static final String REGISTRATION_VIEW = "frontview/front-registration";
    private static final String REFERRAL_SESSION_ATTRIBUTE = "productShareReferralCode";

    private final CustomerRegistrationService customerRegistrationService;

    public CustomerRegistrationController(CustomerRegistrationService customerRegistrationService) {
        this.customerRegistrationService = customerRegistrationService;
    }

    @GetMapping("/registration")
    public String index(
            Model model,
            @RequestParam(name = "ref", required = false) String referralCode,
            HttpSession session,
            @ModelAttribute("users") CustomerRegistrationForm form) {
        rememberReferralCode(referralCode, session);
        addReferralCode(model, session, referralCode);
        return REGISTRATION_VIEW;
    }

    @PostMapping("/customer_registration_save")
    public String registrationSave(
            Model model,
            @Valid @ModelAttribute("users") CustomerRegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "ref_code", required = false) String referralCode,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            addReferralCode(model, session, referralCode);
            return REGISTRATION_VIEW;
        }

        try {
            customerRegistrationService.register(form, resolveRegistrationReferralCode(referralCode, session));
        } catch (CustomerRegistrationException ex) {
            rejectRegistration(bindingResult, ex);
            addReferralCode(model, session, referralCode);
            return REGISTRATION_VIEW;
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("mobile", "invalid", ex.getMessage());
            addReferralCode(model, session, referralCode);
            return REGISTRATION_VIEW;
        } catch (RuntimeException ex) {
            LOGGER.error("Customer registration failed", ex);
            bindingResult.reject("registration.failed", "Registration could not be completed. Please try again.");
            addReferralCode(model, session, referralCode);
            return REGISTRATION_VIEW;
        }

        clearReferralCode(session);
        redirectAttributes.addFlashAttribute("success", "Congratulations! You have successfully registered.");
        return "redirect:/public/member-login";
    }

    private void rejectRegistration(BindingResult bindingResult, CustomerRegistrationException ex) {
        if (ex.getField() == null || ex.getField().isBlank()) {
            bindingResult.reject("registration.failed", ex.getMessage());
        } else {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
        }
    }

    private void rememberReferralCode(String referralCode, HttpSession session) {
        String normalizedReferralCode = trimToNull(referralCode);
        if (normalizedReferralCode != null && session != null) {
            session.setAttribute(REFERRAL_SESSION_ATTRIBUTE, normalizedReferralCode);
        }
    }

    private void addReferralCode(Model model, HttpSession session, String submittedReferralCode) {
        String referralCode = trimToNull(submittedReferralCode);
        if (referralCode == null && session != null) {
            Object sessionReferralCode = session.getAttribute(REFERRAL_SESSION_ATTRIBUTE);
            referralCode = sessionReferralCode instanceof String ? trimToNull((String) sessionReferralCode) : null;
        }
        model.addAttribute("prefilledReferralCode", referralCode == null ? "" : referralCode);
    }

    private String resolveRegistrationReferralCode(String submittedReferralCode, HttpSession session) {
        String normalizedSubmittedCode = trimToNull(submittedReferralCode);
        if (normalizedSubmittedCode != null) {
            return normalizedSubmittedCode;
        }
        if (session == null) {
            return null;
        }
        Object sharedProductReferralCode = session.getAttribute(REFERRAL_SESSION_ATTRIBUTE);
        return sharedProductReferralCode instanceof String ? trimToNull((String) sharedProductReferralCode) : null;
    }

    private void clearReferralCode(HttpSession session) {
        if (session != null) {
            session.removeAttribute(REFERRAL_SESSION_ATTRIBUTE);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
