package com.ecommerce.app.module.checkout.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerCodMobileOtpRequest {

    @NotBlank(message = "Device verification is required.")
    @Size(max = 120, message = "Device verification is invalid.")
    private String deviceFingerprint;

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
}
