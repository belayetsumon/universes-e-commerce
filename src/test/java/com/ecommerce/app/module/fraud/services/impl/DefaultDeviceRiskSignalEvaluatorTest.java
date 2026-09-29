package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.repository.TrustedDeviceRepository;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.SalesOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultDeviceRiskSignalEvaluatorTest {

    private DeviceIdentityRepository deviceIdentityRepository;
    private DefaultDeviceRiskSignalEvaluator evaluator;

    @BeforeEach
    void setUp() {
        deviceIdentityRepository = mock(DeviceIdentityRepository.class);
        evaluator = new DefaultDeviceRiskSignalEvaluator(
                deviceIdentityRepository,
                mock(TrustedDeviceRepository.class)
        );
    }

    @Test
    void deviceSignalsQueryCanonicalIdentifier() {
        FraudContext context = new FraudContext();
        context.setDeviceIdentifier("raw-browser-token");
        String expectedHash = FraudHashingSupport.sha256("raw-browser-token");

        evaluator.evaluate(new SalesOrder(), context);

        verify(deviceIdentityRepository).findFirstByDeviceIdentifierOrderByIdAsc(expectedHash);
        verify(deviceIdentityRepository).existsByDeviceIdentifierAndBlacklistedTrue(expectedHash);
        verify(deviceIdentityRepository).countDistinctCustomersByDeviceIdentifier(expectedHash);
    }

    @Test
    void missingContextDoesNotBreakFraudAssessment() {
        assertDoesNotThrow(() -> evaluator.evaluate(new SalesOrder(), null));
    }
}
