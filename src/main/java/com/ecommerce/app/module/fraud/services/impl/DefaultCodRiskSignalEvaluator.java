package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import com.ecommerce.app.module.fraud.services.evaluator.CodRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultCodRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements CodRiskSignalEvaluator {

    private final CodRiskProfileRepository codRiskProfileRepository;

    public DefaultCodRiskSignalEvaluator(CodRiskProfileRepository codRiskProfileRepository) {
        this.codRiskProfileRepository = codRiskProfileRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        Long customerId = customerId(order);
        Long vendorId = order == null ? null : order.getVendorId();
        CodRiskProfile customerProfile = customerId == null ? null : codRiskProfileRepository.findByCustomerId(customerId).orElse(null);

        boolean codOrder = isCodOrder(order, context);
        signals.add(signal("PAYMENT_METHOD_COD", category(), codOrder, 0, FraudSignalSeverity.INFO,
                null, String.valueOf(codOrder), "cod-risk", null));

        long rtoCount = customerProfile == null ? 0 : customerProfile.getCodRtoCount();
        signals.add(signal("COD_RTO_COUNT", category(), rtoCount >= 2, 30, FraudSignalSeverity.HIGH,
                FraudReasonCode.COD_RTO_HISTORY, String.valueOf(rtoCount), "cod-risk-profile", "{\"threshold\":2}"));

        long refusalCount = customerProfile == null ? 0 : customerProfile.getDeliveryRefusalCount();
        signals.add(signal("DELIVERY_REFUSAL_COUNT", category(), refusalCount >= 2, 30, FraudSignalSeverity.HIGH,
                FraudReasonCode.DELIVERY_REFUSAL_HISTORY, String.valueOf(refusalCount), "cod-risk-profile", "{\"threshold\":2}"));

        boolean codDisabled = (customerId != null && codRiskProfileRepository.existsByCustomerIdAndCodDisabledTrue(customerId))
                || (vendorId != null && codRiskProfileRepository.existsByVendorIdAndCodDisabledTrue(vendorId));
        signals.add(signal("COD_DISABLED", category(), codDisabled, 60, FraudSignalSeverity.HIGH,
                FraudReasonCode.COD_RTO_HISTORY, String.valueOf(codDisabled), "cod-risk-profile", null));

        return signals;
    }

    private boolean isCodOrder(SalesOrder order, FraudContext context) {
        if (context.getPaymentMethod() != null && "COD".equalsIgnoreCase(context.getPaymentMethod())) {
            return true;
        }
        return order != null && (order.getPaymentPlan() == OrderPaymentPlan.FULL_COD
                || order.getPaymentPlan() == OrderPaymentPlan.PARTIAL_ADVANCE_COD);
    }
}
