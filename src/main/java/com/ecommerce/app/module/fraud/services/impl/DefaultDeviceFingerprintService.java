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
        String identifierHash = FraudHashingSupport.canonicalIdentifierHash(context.getDeviceIdentifier());
        if (identifierHash != null) {
            return identifierHash;
        }
        return hashFingerprint(context.getDeviceFingerprint());
    }

    @Override
    public String hashFingerprint(String rawFingerprint) {
        return FraudHashingSupport.canonicalIdentifierHash(rawFingerprint);
    }

    @Override
    public boolean isTrustedDevice(Long customerId, String deviceIdentifier) {
        String identifierHash = FraudHashingSupport.canonicalIdentifierHash(deviceIdentifier);
        return customerId != null && identifierHash != null
                && trustedDeviceRepository.existsEffectiveTrustedDevice(
                        customerId,
                        identifierHash,
                        java.time.LocalDateTime.now()
                );
    }

    @Override
    public boolean isBlacklistedDevice(String deviceIdentifier) {
        String identifierHash = FraudHashingSupport.canonicalIdentifierHash(deviceIdentifier);
        return identifierHash != null
                && deviceIdentityRepository.existsByDeviceIdentifierAndBlacklistedTrue(identifierHash);
    }
}
