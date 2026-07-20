package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.services.evaluator.NetworkRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultNetworkRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements NetworkRiskSignalEvaluator {

    private final DeviceIdentityRepository deviceIdentityRepository;

    public DefaultNetworkRiskSignalEvaluator(DeviceIdentityRepository deviceIdentityRepository) {
        this.deviceIdentityRepository = deviceIdentityRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        String ipAddress = blankToNull(context.getIpAddress());
        Map<String, Object> metadata = context.getMetadata();

        long ordersFromIp = ipAddress == null ? 0 : deviceIdentityRepository.countByIpAddress(ipAddress);
        signals.add(signal("ORDERS_FROM_IP", category(), ordersFromIp > 3, 10, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(ordersFromIp), "network-risk", "{\"threshold\":3}"));

        boolean vpn = Boolean.TRUE.equals(metadata.get("vpnIndicator"));
        signals.add(signal("VPN_INDICATOR", category(), vpn, 15, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(vpn), "network-risk", null));

        boolean proxy = Boolean.TRUE.equals(metadata.get("proxyIndicator"));
        signals.add(signal("PROXY_INDICATOR", category(), proxy, 15, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(proxy), "network-risk", null));

        boolean hosting = Boolean.TRUE.equals(metadata.get("hostingIndicator"));
        signals.add(signal("HOSTING_IP_INDICATOR", category(), hosting, 15, FraudSignalSeverity.MEDIUM,
                null, String.valueOf(hosting), "network-risk", null));

        boolean countryMismatch = context.getIpCountry() != null && context.getShippingCountry() != null
                && !context.getIpCountry().equalsIgnoreCase(context.getShippingCountry());
        signals.add(signal("IP_SHIPPING_COUNTRY_MISMATCH", category(), countryMismatch, 5, FraudSignalSeverity.LOW,
                null, String.valueOf(countryMismatch), "network-risk", "{\"note\":\"Risk signal only; never reject by itself\"}"));

        return signals;
    }
}
