package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.model.VendorRiskProfile;
import com.ecommerce.app.module.fraud.repository.VendorRiskProfileRepository;
import com.ecommerce.app.module.fraud.services.evaluator.VendorRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultVendorRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements VendorRiskSignalEvaluator {

    private final VendorRiskProfileRepository vendorRiskProfileRepository;

    public DefaultVendorRiskSignalEvaluator(VendorRiskProfileRepository vendorRiskProfileRepository) {
        this.vendorRiskProfileRepository = vendorRiskProfileRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        Long vendorId = order == null ? null : order.getVendorId();
        VendorRiskProfile profile = vendorId == null ? null : vendorRiskProfileRepository.findByVendorId(vendorId).orElse(null);
        FraudContext safeContext = context == null ? new FraudContext() : context;
        Map<String, Object> metadata = safeContext.getMetadata();

        boolean payoutHeld = vendorId != null && vendorRiskProfileRepository.existsByVendorIdAndPayoutHeldTrue(vendorId);
        signals.add(signal("VENDOR_PAYOUT_HELD", category(), payoutHeld, 60, FraudSignalSeverity.HIGH,
                FraudReasonCode.PAYOUT_RISK, String.valueOf(payoutHeld), "vendor-risk-profile", null));

        boolean vendorUnderReview = vendorId != null && vendorRiskProfileRepository.existsByVendorIdAndUnderReviewTrue(vendorId);
        signals.add(signal("VENDOR_UNDER_REVIEW", category(), vendorUnderReview, 40, FraudSignalSeverity.HIGH,
                FraudReasonCode.PAYOUT_RISK, String.valueOf(vendorUnderReview), "vendor-risk-profile", null));

        long trackingReuseCount = profile == null ? 0 : profile.getTrackingReuseCount();
        signals.add(signal("TRACKING_NUMBER_REUSE", category(), trackingReuseCount > 0, 50, FraudSignalSeverity.HIGH,
                FraudReasonCode.TRACKING_NUMBER_REUSE, String.valueOf(trackingReuseCount), "vendor-risk-profile", null));

        long selfPurchaseCount = profile == null ? 0 : profile.getSelfPurchaseCount();
        signals.add(signal("VENDOR_SELF_PURCHASE", category(), selfPurchaseCount > 0, 40, FraudSignalSeverity.HIGH,
                FraudReasonCode.VENDOR_CUSTOMER_COLLUSION, String.valueOf(selfPurchaseCount), "vendor-risk-profile", null));

        long sharedMobileCount = profile == null ? 0 : profile.getSharedMobileCount();
        signals.add(signal("VENDOR_CUSTOMER_SHARED_MOBILE", category(), sharedMobileCount > 0, 20, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.VENDOR_CUSTOMER_COLLUSION, String.valueOf(sharedMobileCount), "vendor-risk-profile", null));

        long sharedAddressCount = profile == null ? 0 : profile.getSharedAddressCount();
        signals.add(signal("VENDOR_CUSTOMER_SHARED_ADDRESS", category(), sharedAddressCount > 0, 20, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.VENDOR_CUSTOMER_COLLUSION, String.valueOf(sharedAddressCount), "vendor-risk-profile", null));

        long sharedBankAccountCount = profile == null ? 0 : profile.getSharedBankAccountCount();
        signals.add(signal("VENDOR_SHARED_BANK_ACCOUNT", category(), sharedBankAccountCount > 0, 35, FraudSignalSeverity.HIGH,
                FraudReasonCode.PAYOUT_RISK, String.valueOf(sharedBankAccountCount), "vendor-risk-profile", null));

        long unverifiedDeliveryCount = profile == null ? 0 : profile.getUnverifiedDeliveryCount();
        signals.add(signal("DELIVERY_WITHOUT_CARRIER_VERIFICATION", category(), unverifiedDeliveryCount > 0, 45, FraudSignalSeverity.HIGH,
                FraudReasonCode.FAKE_DELIVERY, String.valueOf(unverifiedDeliveryCount), "vendor-risk-profile", null));

        long suddenSalesSpikeCount = profile == null ? 0 : profile.getSuddenSalesSpikeCount();
        signals.add(signal("SUDDEN_VENDOR_SALES_SPIKE", category(), suddenSalesSpikeCount > 0, 25, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.PAYOUT_RISK, String.valueOf(suddenSalesSpikeCount), "vendor-risk-profile", null));

        boolean abnormalRefundRate = profile != null && profile.getAbnormalRefundRate() != null
                && profile.getAbnormalRefundRate().compareTo(new java.math.BigDecimal("0.20")) >= 0;
        signals.add(signal("ABNORMAL_VENDOR_REFUND_RATE", category(), abnormalRefundRate, 25, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.REFUND_COLLUSION, profile == null ? "0" : String.valueOf(profile.getAbnormalRefundRate()),
                "vendor-risk-profile", null));

        boolean abnormalCancelRate = profile != null && profile.getAbnormalCancellationRate() != null
                && profile.getAbnormalCancellationRate().compareTo(new java.math.BigDecimal("0.25")) >= 0;
        signals.add(signal("ABNORMAL_VENDOR_CANCEL_RATE", category(), abnormalCancelRate, 20, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.PAYOUT_RISK, profile == null ? "0" : String.valueOf(profile.getAbnormalCancellationRate()),
                "vendor-risk-profile", null));

        int vendorRiskScore = profile == null ? 0 : profile.getRiskScore();
        signals.add(signal("VENDOR_RISK_SCORE", category(), vendorRiskScore >= 60, 0, FraudSignalSeverity.HIGH,
                FraudReasonCode.PAYOUT_RISK, String.valueOf(vendorRiskScore), "vendor-risk-profile",
                profile == null ? null : profile.getLastRiskReason()));

        boolean collusion = Boolean.TRUE.equals(metadata.get("vendorCustomerCollusion"));
        signals.add(signal("VENDOR_CUSTOMER_COLLUSION", category(), collusion, 70, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.VENDOR_CUSTOMER_COLLUSION, String.valueOf(collusion), "vendor-risk", null));

        boolean fakeDelivery = Boolean.TRUE.equals(metadata.get("fakeDelivery"));
        signals.add(signal("FAKE_DELIVERY", category(), fakeDelivery, 70, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.FAKE_DELIVERY, String.valueOf(fakeDelivery), "vendor-risk", null));

        return signals;
    }
}
