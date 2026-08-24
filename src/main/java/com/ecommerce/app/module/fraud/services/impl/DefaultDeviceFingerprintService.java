package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.repository.TrustedDeviceRepository;
import com.ecommerce.app.module.fraud.services.DeviceFingerprintService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultDeviceFingerprintService implements DeviceFingerprintService {

    private final DeviceIdentityRepository deviceIdentityRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;

    public DefaultDeviceFingerprintService(
            DeviceIdentityRepository deviceIdentityRepository,
            TrustedDeviceRepository trustedDeviceRepository
    ) {
        this.deviceIdentityRepository = deviceIdentityRepository;
        this.trustedDeviceRepository = trustedDeviceRepository;
    }

    @Override
    public String resolveDeviceIdentifier(FraudContext context) {
        if (context == null) {
            return null;
        }
        if (context.getDeviceIdentifier() != null && !context.getDeviceIdentifier().isBlank()) {
            return context.getDeviceIdentifier().trim();
        }
        return hashFingerprint(context.getDeviceFingerprint());
    }

    @Override
    public String hashFingerprint(String rawFingerprint) {
        if (rawFingerprint == null || rawFingerprint.isBlank()) {
            return null;
        }
        String clean = rawFingerprint.trim();
        return clean.matches("(?i)[a-f0-9]{64}")
                ? clean.toLowerCase()
                : FraudHashingSupport.sha256(clean);
    }

    @Override
    public boolean isTrustedDevice(Long customerId, String deviceIdentifier) {
        return customerId != null && deviceIdentifier != null && !deviceIdentifier.isBlank()
                && trustedDeviceRepository.existsByCustomerIdAndDeviceIdentifierAndActiveTrue(
                        customerId,
                        deviceIdentifier.trim()
                );
    }

    @Override
    public boolean isBlacklistedDevice(String deviceIdentifier) {
        return deviceIdentifier != null && !deviceIdentifier.isBlank()
                && deviceIdentityRepository.existsByDeviceIdentifierAndBlacklistedTrue(deviceIdentifier.trim());
    }
}
