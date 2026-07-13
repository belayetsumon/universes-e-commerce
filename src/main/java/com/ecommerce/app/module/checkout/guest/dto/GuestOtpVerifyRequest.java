package com.ecommerce.app.module.checkout.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GuestOtpVerifyRequest {

    @NotBlank(message = "Verification session is required.")
    @Size(max = 80, message = "Verification session is invalid.")
    private String sessionToken;

    @NotBlank(message = "OTP is required.")
    @Pattern(regexp = "^[0-9]{6}$", message = "Enter the 6 digit OTP.")
    private String otp;

    @Size(max = 120, message = "Device fingerprint is too long.")
    private String deviceFingerprint;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
}
