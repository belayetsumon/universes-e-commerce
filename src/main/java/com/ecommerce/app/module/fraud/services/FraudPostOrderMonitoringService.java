package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.dto.FraudPostOrderEventRequest;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;

public interface FraudPostOrderMonitoringService {

    void recordEvent(FraudPostOrderEventRequest request);

    void recordShipmentCreated(SalesOrder order, Long shipmentId, String trackingNumber);

    void recordShipmentStatus(SalesOrder order, Long shipmentId, ShipmentStatus status, String reason);

    void recordOrderStatusChanged(SalesOrder order, String previousStatus, String nextStatus, String changedBy, String remark);

    void recordRefundReleased(SalesOrder order, BigDecimal amount, String reference);

    void recordGiftCardUsage(SalesOrder order, BigDecimal amount, String giftCardCode, Long transactionId);

    void recordReferralReward(Users beneficiary, Long orderId, BigDecimal amount, String sourceType, String sourceReference);

    void recordCashbackRelease(Long orderId, Long customerId, BigDecimal amount, boolean held, String reason);

    void recordWalletCredit(Users user, BigDecimal amount, String sourceType, String sourceReference, boolean held);

    void recordVendorPayout(Long vendorId, Long payoutId, BigDecimal amount, String payoutStatus, boolean held, String reason);

    void recordChargeback(Long orderId, Long customerId, Long vendorId, BigDecimal amount, String providerReference);

    FraudGuardResult checkValueReleaseAllowed(FraudPostOrderEventType releaseType, Long orderId, Long customerId,
            Long vendorId, String sourceReference);
}
