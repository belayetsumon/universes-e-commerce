package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.repository.TrustedDeviceRepository;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultDeviceFingerprintServiceTest {

    private DeviceIdentityRepository deviceIdentityRepository;
    private TrustedDeviceRepository trustedDeviceRepository;
    private DefaultDeviceFingerprintService service;

    @BeforeEach
    void setUp() {
        deviceIdentityRepository = mock(DeviceIdentityRepository.class);
        trustedDeviceRepository = mock(TrustedDeviceRepository.class);
        service = new DefaultDeviceFingerprintService(deviceIdentityRepository, trustedDeviceRepository);
    }

    @Test
    void rawIdentifiersResolveToCanonicalHash() {
        FraudContext context = new FraudContext();
        context.setDeviceIdentifier("raw-browser-token");

        assertEquals(
                FraudHashingSupport.sha256("raw-browser-token"),
                service.resolveDeviceIdentifier(context)
        );
    }

    @Test
    void repositoryLookupsUseTheSameCanonicalHash() {
        String rawIdentifier = "raw-browser-token";
        String expectedHash = FraudHashingSupport.sha256(rawIdentifier);

        service.isTrustedDevice(42L, rawIdentifier);
        service.isBlacklistedDevice(rawIdentifier);

        verify(trustedDeviceRepository)
                .existsEffectiveTrustedDevice(eq(42L), eq(expectedHash), any());
        verify(deviceIdentityRepository)
                .existsByDeviceIdentifierAndBlacklistedTrue(expectedHash);
    }
}
