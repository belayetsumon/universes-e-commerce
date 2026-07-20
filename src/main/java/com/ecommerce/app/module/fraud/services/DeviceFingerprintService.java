package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;

public interface DeviceFingerprintService {

    String resolveDeviceIdentifier(FraudContext context);

    String hashFingerprint(String rawFingerprint);

    boolean isTrustedDevice(Long customerId, String deviceIdentifier);

    boolean isBlacklistedDevice(String deviceIdentifier);
}
