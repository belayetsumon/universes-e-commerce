package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.services.evaluator.AddressRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.model.ShippingAddress;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultAddressRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements AddressRiskSignalEvaluator {

    private final FraudBlocklistRepository fraudBlocklistRepository;

    public DefaultAddressRiskSignalEvaluator(FraudBlocklistRepository fraudBlocklistRepository) {
        this.fraudBlocklistRepository = fraudBlocklistRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        ShippingAddress shipping = order == null ? null : order.getShippingAddress();
        BillingAddress billing = order == null ? null : order.getBillingAddress();

        String addressHash = sha256(buildShippingAddressKey(shipping));
        boolean blacklistedAddress = addressHash != null
                && fraudBlocklistRepository.existsByBlockTypeAndHashedValueAndActiveTrue(FraudBlockType.ADDRESS, addressHash);
        signals.add(signal("ADDRESS_BLACKLISTED", category(), blacklistedAddress, 100, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.ADDRESS_BLACKLISTED, String.valueOf(blacklistedAddress), "fraud-blocklist", null));

        boolean addressMismatch = shipping != null && billing != null
                && shipping.getDistrict() != null && billing.getDistrict() != null
                && !shipping.getDistrict().equalsIgnoreCase(billing.getDistrict());
        signals.add(signal("ADDRESS_MISMATCH", category(), addressMismatch, 10, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.ADDRESS_MISMATCH, String.valueOf(addressMismatch), "address-risk", null));

        boolean unusualDistrict = context.getShippingDistrict() != null && shipping != null && shipping.getDistrict() != null
                && !context.getShippingDistrict().equalsIgnoreCase(shipping.getDistrict());
        signals.add(signal("UNUSUAL_DELIVERY_LOCATION", category(), unusualDistrict, 10, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(unusualDistrict), "address-risk", null));

        return signals;
    }

    private String buildShippingAddressKey(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return String.join("|",
                nullSafe(address.getAddressLineOne()),
                nullSafe(address.getAddressLinetwo()),
                nullSafe(address.getCity()),
                nullSafe(address.getDistrict()),
                nullSafe(address.getPostCode()),
                nullSafe(address.getCountry()),
                nullSafe(address.getMobile()));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }
}
