package com.ecommerce.app.module.checkout.guest.controller;

import com.ecommerce.app.module.checkout.availability.CheckoutAvailabilityService;
import com.ecommerce.app.module.checkout.guest.dto.GuestOtpResponse;
import com.ecommerce.app.module.checkout.guest.dto.GuestOtpSendRequest;
import com.ecommerce.app.module.checkout.guest.dto.GuestOtpVerifyRequest;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutOtpService;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout/guest/mobile")
public class GuestCheckoutMobileController {

    private final GuestCheckoutOtpService otpService;
    private final GuestCheckoutSessionService sessionService;
    private final CheckoutAvailabilityService checkoutAvailabilityService;

    public GuestCheckoutMobileController(
            GuestCheckoutOtpService otpService,
            GuestCheckoutSessionService sessionService,
            CheckoutAvailabilityService checkoutAvailabilityService) {
        this.otpService = otpService;
        this.sessionService = sessionService;
        this.checkoutAvailabilityService = checkoutAvailabilityService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<GuestOtpResponse> sendOtp(
            @Valid GuestOtpSendRequest form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {
        ResponseEntity<GuestOtpResponse> unavailable = checkoutUnavailableIfDisabled();
        if (unavailable != null) {
            return unavailable;
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(GuestOtpResponse.failure(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        try {
            return ResponseEntity.ok(otpService.sendOtp(form.getMobileNumber(), form.getDeviceFingerprint(), request, session));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(GuestOtpResponse.failure(ex.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<GuestOtpResponse> resendOtp(
            @Valid GuestOtpSendRequest form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {
        return sendOtp(form, bindingResult, request, session);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<GuestOtpResponse> verifyOtp(
            @Valid GuestOtpVerifyRequest form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {
        ResponseEntity<GuestOtpResponse> unavailable = checkoutUnavailableIfDisabled();
        if (unavailable != null) {
            return unavailable;
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(GuestOtpResponse.failure(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        try {
            return ResponseEntity.ok(otpService.verifyOtp(form.getSessionToken(), form.getOtp(), form.getDeviceFingerprint(), request, session));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(GuestOtpResponse.failure(ex.getMessage()));
        }
    }

    @PostMapping("/change-mobile")
    public ResponseEntity<GuestOtpResponse> changeMobile(HttpSession session) {
        ResponseEntity<GuestOtpResponse> unavailable = checkoutUnavailableIfDisabled();
        if (unavailable != null) {
            return unavailable;
        }
        sessionService.clear(session);
        return ResponseEntity.ok(GuestOtpResponse.success("Mobile verification has been reset."));
    }

    private ResponseEntity<GuestOtpResponse> checkoutUnavailableIfDisabled() {
        if (checkoutAvailabilityService.availability(false).isCheckoutAvailable()) {
            return null;
        }
        return ResponseEntity.status(403)
                .body(GuestOtpResponse.failure(CheckoutAvailabilityService.CHECKOUT_UNAVAILABLE_MESSAGE));
    }
}
