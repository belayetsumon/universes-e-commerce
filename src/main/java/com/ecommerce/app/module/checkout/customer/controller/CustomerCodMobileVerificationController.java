package com.ecommerce.app.module.checkout.customer.controller;

import com.ecommerce.app.module.checkout.customer.dto.CustomerCodMobileOtpRequest;
import com.ecommerce.app.module.checkout.customer.dto.CustomerCodMobileOtpResponse;
import com.ecommerce.app.module.checkout.customer.dto.CustomerCodMobileOtpVerifyRequest;
import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService.OtpRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout/customer/mobile")
public class CustomerCodMobileVerificationController {

    private final CustomerCodMobileVerificationService verificationService;

    public CustomerCodMobileVerificationController(CustomerCodMobileVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/status")
    public ResponseEntity<CustomerCodMobileOtpResponse> status() {
        try {
            return ResponseEntity.ok(verificationService.currentStatus());
        } catch (AuthenticationCredentialsNotFoundException ex) {
            return authenticationRequired();
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<CustomerCodMobileOtpResponse> sendOtp(
            @Valid @ModelAttribute CustomerCodMobileOtpRequest form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(CustomerCodMobileOtpResponse.failure(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            ));
        }
        try {
            CustomerCodMobileOtpResponse response = verificationService.sendOtp(
                    form.getDeviceFingerprint(), request, session
            );
            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            return authenticationRequired();
        } catch (OtpRateLimitException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(CustomerCodMobileOtpResponse.failure(ex.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(CustomerCodMobileOtpResponse.failure(ex.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<CustomerCodMobileOtpResponse> resendOtp(
            @Valid @ModelAttribute CustomerCodMobileOtpRequest form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {
        return sendOtp(form, bindingResult, request, session);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CustomerCodMobileOtpResponse> verifyOtp(
            @Valid @ModelAttribute CustomerCodMobileOtpVerifyRequest form,
            BindingResult bindingResult,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(CustomerCodMobileOtpResponse.failure(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            ));
        }
        try {
            CustomerCodMobileOtpResponse response = verificationService.verifyOtp(
                    form.getSessionToken(),
                    form.getOtp(),
                    form.getDeviceFingerprint(),
                    session
            );
            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            return authenticationRequired();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(CustomerCodMobileOtpResponse.failure(ex.getMessage()));
        }
    }

    private ResponseEntity<CustomerCodMobileOtpResponse> authenticationRequired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CustomerCodMobileOtpResponse.failure("Authentication is required."));
    }
}
