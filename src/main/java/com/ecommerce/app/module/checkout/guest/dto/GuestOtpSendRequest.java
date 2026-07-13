package com.ecommerce.app.module.checkout.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GuestOtpSendRequest {

    @NotBlank(message = "Mobile number is required.")
    @Size(max = 30, message = "Mobile number is too long.")
    private String mobileNumber;

    @Size(max = 120, message = "Device fingerprint is too long.")
    private String deviceFingerprint;

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
}
