package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.repository.TrustedDeviceRepository;
import com.ecommerce.app.module.fraud.services.evaluator.DeviceRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultDeviceRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements DeviceRiskSignalEvaluator {

    private final DeviceIdentityRepository deviceIdentityRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;

    public DefaultDeviceRiskSignalEvaluator(DeviceIdentityRepository deviceIdentityRepository,
            TrustedDeviceRepository trustedDeviceRepository) {
        this.deviceIdentityRepository = deviceIdentityRepository;
        this.trustedDeviceRepository = trustedDeviceRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        String deviceIdentifier = blankToNull(context.getDeviceIdentifier());
        Long customerId = customerId(order);

        boolean unknownDevice = deviceIdentifier != null && deviceIdentityRepository.findByDeviceIdentifier(deviceIdentifier).isEmpty();
        signals.add(signal("UNKNOWN_DEVICE", category(), unknownDevice, 10, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(unknownDevice), "device-identity", null));

        boolean blacklisted = deviceIdentifier != null && deviceIdentityRepository.existsByDeviceIdentifierAndBlacklistedTrue(deviceIdentifier);
        signals.add(signal("DEVICE_BLACKLISTED", category(), blacklisted, 100, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.DEVICE_BLACKLISTED, String.valueOf(blacklisted), "device-identity", null));

        boolean trusted = customerId != null && deviceIdentifier != null
                && trustedDeviceRepository.existsByCustomerIdAndDeviceIdentifierAndActiveTrue(customerId, deviceIdentifier);
        signals.add(signal("TRUSTED_DEVICE", category(), trusted, -10, FraudSignalSeverity.LOW,
                null, String.valueOf(trusted), "trusted-device", null));

        long accountsOnDevice = deviceIdentifier == null ? 0
                : deviceIdentityRepository.countDistinctByDeviceIdentifierAndCustomerIdIsNotNull(deviceIdentifier);
        signals.add(signal("ACCOUNTS_PER_DEVICE", category(), accountsOnDevice > 1, 30, FraudSignalSeverity.HIGH,
                FraudReasonCode.MULTIPLE_ACCOUNTS_SAME_DEVICE, String.valueOf(accountsOnDevice), "device-identity", "{\"threshold\":1}"));

        return signals;
    }
}
