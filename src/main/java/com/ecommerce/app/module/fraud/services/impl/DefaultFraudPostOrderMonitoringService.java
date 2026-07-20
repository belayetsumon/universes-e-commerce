package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.dto.FraudPostOrderEventRequest;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudCasePriority;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import com.ecommerce.app.module.fraud.services.FraudIdempotencyService;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudPostOrderMonitoringService implements FraudPostOrderMonitoringService {

    private static final Set<FraudCaseStatus> OPEN_CASE_STATUSES = Set.of(
            FraudCaseStatus.OPEN,
            FraudCaseStatus.ASSIGNED,
            FraudCaseStatus.IN_REVIEW,
            FraudCaseStatus.ESCALATED
    );

    private static final Set<FraudPostOrderEventType> CASE_TRIGGER_EVENTS = Set.of(
            FraudPostOrderEventType.DELIVERY_REFUSAL,
            FraudPostOrderEventType.RETURN_TO_ORIGIN,
            FraudPostOrderEventType.ORDER_CANCELLED,
            FraudPostOrderEventType.RETURN_COMPLETED,
            FraudPostOrderEventType.CHARGEBACK_RECORDED,
            FraudPostOrderEventType.CASHBACK_HELD,
            FraudPostOrderEventType.WALLET_CREDIT_HELD,
            FraudPostOrderEventType.REFERRAL_REWARD_HELD,
            FraudPostOrderEventType.VENDOR_PAYOUT_HELD,
            FraudPostOrderEventType.VENDOR_RISK_ESCALATED,
            FraudPostOrderEventType.VENDOR_CUSTOMER_COLLUSION,
            FraudPostOrderEventType.TRACKING_REUSE_ATTEMPT,
            FraudPostOrderEventType.FAKE_DELIVERY_SUSPECTED
    );

    private final FraudEventLogRepository fraudEventLogRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudEventPublisher fraudEventPublisher;
    private final FraudIdempotencyService fraudIdempotencyService;

    public DefaultFraudPostOrderMonitoringService(FraudEventLogRepository fraudEventLogRepository,
            FraudCaseRepository fraudCaseRepository,
            FraudAssessmentRepository fraudAssessmentRepository,
            FraudEventPublisher fraudEventPublisher,
            FraudIdempotencyService fraudIdempotencyService) {
        this.fraudEventLogRepository = fraudEventLogRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudEventPublisher = fraudEventPublisher;
        this.fraudIdempotencyService = fraudIdempotencyService;
    }

    @Override
    @Transactional
    public void recordEvent(FraudPostOrderEventRequest request) {
        if (request == null || request.getEventType() == null) {
            return;
        }

        String idempotencyKey = normalizeIdempotencyKey(request);
        if (idempotencyKey != null && fraudEventLogRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return;
        }
        if (idempotencyKey != null) {
            if (fraudIdempotencyService.findCompleted("FRAUD_POST_ORDER_EVENT", idempotencyKey).isPresent()) {
                return;
            }
            fraudIdempotencyService.start("FRAUD_POST_ORDER_EVENT", idempotencyKey,
                    fraudIdempotencyService.hashPayload(request.getEventType() + "|"
                            + request.getAggregateType() + "|" + request.getAggregateId() + "|"
                            + request.getOrderId() + "|" + request.getVendorId()));
        }

        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setEventType(resolveFraudEventType(request.getEventType()));
        eventLog.setAggregateType(defaultText(request.getAggregateType(), request.getEventType().name()));
        eventLog.setAggregateId(request.getAggregateId());
        eventLog.setOrderId(request.getOrderId());
        eventLog.setCustomerId(request.getCustomerId());
        eventLog.setVendorId(request.getVendorId());
        eventLog.setCorrelationId(request.getCorrelationId());
        eventLog.setIdempotencyKey(idempotencyKey);
        eventLog.setPayloadJson(buildPayload(request));
        eventLog.setEventTime(LocalDateTime.now());
        fraudEventLogRepository.save(eventLog);
        publishEvent(eventLog);
        if (idempotencyKey != null) {
            fraudIdempotencyService.complete("FRAUD_POST_ORDER_EVENT", idempotencyKey,
                    "{\"eventLogId\":" + eventLog.getId() + ",\"eventType\":\"" + eventLog.getEventType() + "\"}");
        }

        openOrUpdateCaseIfRequired(request);
    }

    @Override
    public void recordShipmentCreated(SalesOrder order, Long shipmentId, String trackingNumber) {
        FraudPostOrderEventRequest request = baseOrderEvent(order, FraudPostOrderEventType.SHIPMENT_CREATED);
        request.setAggregateType("SHIPMENT");
        request.setAggregateId(shipmentId);
        request.setReason("Shipment created.");
        request.getMetadata().put("trackingNumber", safe(trackingNumber));
        request.setIdempotencyKey("FRAUD:POST:SHIPMENT_CREATED:" + shipmentId);
        recordEvent(request);
    }

    @Override
    public void recordShipmentStatus(SalesOrder order, Long shipmentId, ShipmentStatus status, String reason) {
        if (status == null) {
            return;
        }
        FraudPostOrderEventType eventType = switch (status) {
            case DELIVERED -> FraudPostOrderEventType.DELIVERY_CONFIRMED;
            case FAILED -> FraudPostOrderEventType.DELIVERY_REFUSAL;
            case RETURNED -> FraudPostOrderEventType.RETURN_TO_ORIGIN;
            default -> null;
        };
        if (eventType == null) {
            return;
        }
        FraudPostOrderEventRequest request = baseOrderEvent(order, eventType);
        request.setAggregateType("SHIPMENT");
        request.setAggregateId(shipmentId);
        request.setReason(defaultText(reason, "Shipment status changed to " + status + "."));
        request.getMetadata().put("shipmentStatus", status.name());
        request.setIdempotencyKey("FRAUD:POST:SHIPMENT_STATUS:" + shipmentId + ":" + status.name());
        recordEvent(request);
    }

    @Override
    public void recordOrderStatusChanged(SalesOrder order, String previousStatus, String nextStatus, String changedBy, String remark) {
        FraudPostOrderEventType eventType = resolveOrderStatusEvent(nextStatus);
        if (eventType == null) {
            return;
        }
        FraudPostOrderEventRequest request = baseOrderEvent(order, eventType);
        request.setAggregateType("SALES_ORDER");
        request.setAggregateId(order == null ? null : order.getId());
        request.setReason(defaultText(remark, "Order status changed."));
        request.getMetadata().put("previousStatus", safe(previousStatus));
        request.getMetadata().put("nextStatus", safe(nextStatus));
        request.getMetadata().put("changedBy", safe(changedBy));
        request.setIdempotencyKey("FRAUD:POST:ORDER_STATUS:" + request.getOrderId() + ":" + safe(previousStatus) + ":" + safe(nextStatus));
        recordEvent(request);
    }

    @Override
    public void recordRefundReleased(SalesOrder order, BigDecimal amount, String reference) {
        FraudPostOrderEventRequest request = baseOrderEvent(order, FraudPostOrderEventType.REFUND_RELEASED);
        request.setAggregateType("PAYMENT_REFUND");
        request.setAggregateId(order == null ? null : order.getId());
        request.setAmount(amount);
        request.setReason("Refund released.");
        request.getMetadata().put("reference", safe(reference));
        request.setIdempotencyKey("FRAUD:POST:REFUND:" + request.getOrderId() + ":" + money(amount) + ":" + safe(reference));
        recordEvent(request);
    }

    @Override
    public void recordGiftCardUsage(SalesOrder order, BigDecimal amount, String giftCardCode, Long transactionId) {
        FraudPostOrderEventRequest request = baseOrderEvent(order, FraudPostOrderEventType.GIFT_CARD_USAGE);
        request.setAggregateType("GIFT_CARD_TRANSACTION");
        request.setAggregateId(transactionId);
        request.setAmount(amount);
        request.setReason("Gift card used for order.");
        request.getMetadata().put("giftCardCode", mask(giftCardCode));
        request.setIdempotencyKey("FRAUD:POST:GIFT_CARD:" + transactionId);
        recordEvent(request);
    }

    @Override
    public void recordReferralReward(Users beneficiary, Long orderId, BigDecimal amount, String sourceType, String sourceReference) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(FraudPostOrderEventType.REFERRAL_REWARD_RELEASED);
        request.setOrderId(orderId);
        request.setCustomerId(beneficiary == null ? null : beneficiary.getId());
        request.setAggregateType("REWARD_TRANSACTION");
        request.setAggregateId(orderId);
        request.setAmount(amount);
        request.setReason("Referral or level reward released.");
        request.setSource(sourceType);
        request.getMetadata().put("sourceReference", safe(sourceReference));
        request.setIdempotencyKey("FRAUD:POST:REWARD:" + safe(sourceType) + ":" + safe(sourceReference) + ":" + money(amount));
        recordEvent(request);
    }

    @Override
    public void recordCashbackRelease(Long orderId, Long customerId, BigDecimal amount, boolean held, String reason) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(held ? FraudPostOrderEventType.CASHBACK_HELD : FraudPostOrderEventType.CASHBACK_RELEASED);
        request.setOrderId(orderId);
        request.setCustomerId(customerId);
        request.setAggregateType("CASHBACK");
        request.setAggregateId(orderId);
        request.setAmount(amount);
        request.setReason(defaultText(reason, held ? "Cashback held by fraud control." : "Cashback released."));
        request.setIdempotencyKey("FRAUD:POST:CASHBACK:" + orderId + ":" + (held ? "HELD" : "RELEASED"));
        recordEvent(request);
    }

    @Override
    public void recordWalletCredit(Users user, BigDecimal amount, String sourceType, String sourceReference, boolean held) {
        Long orderId = extractOrderId(sourceReference);
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(held ? FraudPostOrderEventType.WALLET_CREDIT_HELD : FraudPostOrderEventType.WALLET_CREDIT_RELEASED);
        request.setOrderId(orderId);
        request.setCustomerId(user == null ? null : user.getId());
        request.setAggregateType("WALLET_TRANSACTION");
        request.setAggregateId(user == null ? null : user.getId());
        request.setAmount(amount);
        request.setReason(held ? "Wallet credit held by fraud control." : "Wallet credit released.");
        request.setSource(sourceType);
        request.getMetadata().put("sourceReference", safe(sourceReference));
        request.setIdempotencyKey("FRAUD:POST:WALLET:" + safe(sourceType) + ":" + safe(sourceReference) + ":" + money(amount) + ":" + held);
        recordEvent(request);
    }

    @Override
    public void recordVendorPayout(Long vendorId, Long payoutId, BigDecimal amount, String payoutStatus, boolean held, String reason) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(held ? FraudPostOrderEventType.VENDOR_PAYOUT_HELD : resolvePayoutEvent(payoutStatus));
        request.setVendorId(vendorId);
        request.setAggregateType("VENDOR_PAYOUT");
        request.setAggregateId(payoutId);
        request.setAmount(amount);
        request.setReason(defaultText(reason, "Vendor payout " + safe(payoutStatus) + "."));
        request.getMetadata().put("payoutStatus", safe(payoutStatus));
        request.setIdempotencyKey("FRAUD:POST:VENDOR_PAYOUT:" + payoutId + ":" + safe(payoutStatus) + ":" + held);
        recordEvent(request);
    }

    @Override
    public void recordChargeback(Long orderId, Long customerId, Long vendorId, BigDecimal amount, String providerReference) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(FraudPostOrderEventType.CHARGEBACK_RECORDED);
        request.setOrderId(orderId);
        request.setCustomerId(customerId);
        request.setVendorId(vendorId);
        request.setAggregateType("CHARGEBACK");
        request.setAggregateId(orderId);
        request.setAmount(amount);
        request.setReason("Chargeback recorded from payment provider.");
        request.getMetadata().put("providerReference", safe(providerReference));
        request.setIdempotencyKey("FRAUD:POST:CHARGEBACK:" + orderId + ":" + safe(providerReference));
        recordEvent(request);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudGuardResult checkValueReleaseAllowed(FraudPostOrderEventType releaseType, Long orderId, Long customerId,
            Long vendorId, String sourceReference) {
        Long resolvedOrderId = orderId == null ? extractOrderId(sourceReference) : orderId;
        if (resolvedOrderId != null && fraudCaseRepository.existsByOrderIdAndCaseStatusIn(resolvedOrderId, OPEN_CASE_STATUSES)) {
            return FraudGuardResult.blocked("Value release is held while a fraud case is open for this order.");
        }
        if (vendorId != null && fraudCaseRepository.existsByVendorIdAndCaseStatusIn(vendorId, OPEN_CASE_STATUSES)) {
            return FraudGuardResult.blocked("Value release is held while a fraud case is open for this vendor.");
        }
        return FraudGuardResult.allowed();
    }

    private FraudPostOrderEventRequest baseOrderEvent(SalesOrder order, FraudPostOrderEventType eventType) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(eventType);
        request.setOrderId(order == null ? null : order.getId());
        request.setCustomerId(order == null || order.getCustomer() == null ? null : order.getCustomer().getId());
        request.setVendorId(order == null ? null : order.getVendorId());
        request.setCorrelationId(order == null ? null : order.getUuid());
        return request;
    }

    private void openOrUpdateCaseIfRequired(FraudPostOrderEventRequest request) {
        if (!shouldOpenOrUpdateCase(request)) {
            return;
        }
        FraudCase existing = findOpenCase(request);
        if (existing != null) {
            existing.setInvestigationNotes(appendNote(existing.getInvestigationNotes(), caseNote(request)));
            fraudCaseRepository.save(existing);
            return;
        }

        FraudCase fraudCase = new FraudCase();
        fraudCase.setCaseNumber(generateCaseNumber(request));
        fraudCase.setOrderId(request.getOrderId());
        fraudCase.setCustomerId(request.getCustomerId());
        fraudCase.setVendorId(request.getVendorId());
        latestAssessment(request.getOrderId()).ifPresent(fraudCase::setAssessment);
        fraudCase.setCaseStatus(FraudCaseStatus.OPEN);
        fraudCase.setPriority(resolveCasePriority(request));
        fraudCase.setCaseReason(caseNote(request));
        fraudCase.setOpenedAt(LocalDateTime.now());
        fraudCaseRepository.save(fraudCase);
    }

    private boolean shouldOpenOrUpdateCase(FraudPostOrderEventRequest request) {
        if (request == null || !CASE_TRIGGER_EVENTS.contains(request.getEventType())) {
            return false;
        }
        if (request.getEventType() == FraudPostOrderEventType.ORDER_CANCELLED
                || request.getEventType() == FraudPostOrderEventType.RETURN_COMPLETED) {
            return latestAssessment(request.getOrderId())
                    .map(assessment -> assessment.getRiskLevel() == FraudRiskLevel.HIGH || assessment.getRiskLevel() == FraudRiskLevel.CRITICAL)
                    .orElse(false);
        }
        return true;
    }

    private FraudCase findOpenCase(FraudPostOrderEventRequest request) {
        if (request.getOrderId() != null) {
            FraudCase byOrder = fraudCaseRepository
                    .findFirstByOrderIdAndCaseStatusInOrderByIdDesc(request.getOrderId(), OPEN_CASE_STATUSES)
                    .orElse(null);
            if (byOrder != null) {
                return byOrder;
            }
        }
        if (request.getVendorId() != null) {
            return fraudCaseRepository
                    .findFirstByVendorIdAndCaseStatusInOrderByIdDesc(request.getVendorId(), OPEN_CASE_STATUSES)
                    .orElse(null);
        }
        return null;
    }

    private FraudCasePriority resolveCasePriority(FraudPostOrderEventRequest request) {
        if (request.getEventType() == FraudPostOrderEventType.CHARGEBACK_RECORDED
                || request.getEventType() == FraudPostOrderEventType.VENDOR_PAYOUT_HELD
                || request.getEventType() == FraudPostOrderEventType.VENDOR_RISK_ESCALATED
                || request.getEventType() == FraudPostOrderEventType.TRACKING_REUSE_ATTEMPT
                || request.getEventType() == FraudPostOrderEventType.FAKE_DELIVERY_SUSPECTED) {
            return FraudCasePriority.CRITICAL;
        }
        return latestAssessment(request.getOrderId())
                .map(assessment -> switch (assessment.getRiskLevel()) {
                    case LOW -> FraudCasePriority.LOW;
                    case MEDIUM -> FraudCasePriority.MEDIUM;
                    case HIGH -> FraudCasePriority.HIGH;
                    case CRITICAL -> FraudCasePriority.CRITICAL;
                })
                .orElse(FraudCasePriority.HIGH);
    }

    private java.util.Optional<FraudAssessment> latestAssessment(Long orderId) {
        return orderId == null ? java.util.Optional.empty() : fraudAssessmentRepository.findTopByOrderIdOrderByIdDesc(orderId);
    }

    private FraudPostOrderEventType resolveOrderStatusEvent(String nextStatus) {
        if ("CANCELLED".equals(nextStatus)) {
            return FraudPostOrderEventType.ORDER_CANCELLED;
        }
        if ("RETURN_REQUESTED".equals(nextStatus)) {
            return FraudPostOrderEventType.RETURN_REQUESTED;
        }
        if ("RETURNED".equals(nextStatus) || "PARTIALLY_RETURNED".equals(nextStatus)) {
            return FraudPostOrderEventType.RETURN_COMPLETED;
        }
        return null;
    }

    private FraudPostOrderEventType resolvePayoutEvent(String payoutStatus) {
        if ("PAID".equalsIgnoreCase(payoutStatus)) {
            return FraudPostOrderEventType.VENDOR_PAYOUT_PAID;
        }
        if ("PROCESSING".equalsIgnoreCase(payoutStatus)) {
            return FraudPostOrderEventType.VENDOR_PAYOUT_PROCESSING;
        }
        return FraudPostOrderEventType.VENDOR_PAYOUT_REQUESTED;
    }

    private String normalizeIdempotencyKey(FraudPostOrderEventRequest request) {
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            return trimToLength(request.getIdempotencyKey().trim(), 160);
        }
        return trimToLength("FRAUD:POST:" + request.getEventType() + ":" + safe(request.getAggregateType())
                + ":" + request.getAggregateId() + ":" + request.getOrderId() + ":" + request.getVendorId(), 160);
    }

    private FraudEventType resolveFraudEventType(FraudPostOrderEventType eventType) {
        if (eventType == FraudPostOrderEventType.VENDOR_PAYOUT_HELD) {
            return FraudEventType.VENDOR_PAYOUT_HELD;
        }
        if (eventType == FraudPostOrderEventType.CASHBACK_HELD
                || eventType == FraudPostOrderEventType.WALLET_CREDIT_HELD
                || eventType == FraudPostOrderEventType.REFERRAL_REWARD_HELD
                || eventType == FraudPostOrderEventType.VENDOR_RISK_ESCALATED) {
            return FraudEventType.VALUE_RELEASE_HELD;
        }
        return FraudEventType.POST_ORDER_EVENT_RECORDED;
    }

    private void publishEvent(FraudEventLog eventLog) {
        fraudEventPublisher.publish(eventLog.getEventType(), eventLog.getAggregateType(), eventLog.getAggregateId(),
                withCommonPayload(eventLog), eventLog.getCorrelationId(), eventLog.getIdempotencyKey());
    }

    private String withCommonPayload(FraudEventLog eventLog) {
        String payload = eventLog.getPayloadJson();
        String base = payload == null || payload.isBlank() ? "{}" : payload.trim();
        if (!base.endsWith("}")) {
            return base;
        }
        StringBuilder builder = new StringBuilder(base);
        if (builder.length() > 1 && builder.charAt(builder.length() - 2) != '{') {
            builder.insert(builder.length() - 1, ',');
        }
        builder.insert(builder.length() - 1, "\"orderId\":" + nullToJson(eventLog.getOrderId())
                + ",\"customerId\":" + nullToJson(eventLog.getCustomerId())
                + ",\"vendorId\":" + nullToJson(eventLog.getVendorId()));
        return builder.toString();
    }

    private String nullToJson(Long value) {
        return value == null ? "null" : value.toString();
    }

    private Long extractOrderId(String sourceReference) {
        if (sourceReference == null || sourceReference.isBlank()) {
            return null;
        }
        String normalized = sourceReference.trim().toUpperCase();
        if (!normalized.startsWith("ORDER:")) {
            return null;
        }
        try {
            return Long.valueOf(normalized.substring("ORDER:".length()).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildPayload(FraudPostOrderEventRequest request) {
        StringBuilder builder = new StringBuilder("{");
        appendJson(builder, "postOrderEventType", request.getEventType().name());
        appendJson(builder, "source", request.getSource());
        appendJson(builder, "reason", request.getReason());
        appendJson(builder, "amount", request.getAmount() == null ? null : money(request.getAmount()));
        for (Map.Entry<String, Object> entry : request.getMetadata().entrySet()) {
            appendJson(builder, entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        if (builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append('}');
        return builder.toString();
    }

    private void appendJson(StringBuilder builder, String key, String value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        builder.append('"').append(json(key)).append("\":\"").append(json(value)).append("\",");
    }

    private String caseNote(FraudPostOrderEventRequest request) {
        return request.getEventType().name() + ": " + defaultText(request.getReason(), "Post-order fraud event recorded.");
    }

    private String appendNote(String existing, String note) {
        String prefix = existing == null || existing.isBlank() ? "" : existing + System.lineSeparator();
        return prefix + LocalDateTime.now() + " - " + note;
    }

    private String generateCaseNumber(FraudPostOrderEventRequest request) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String suffix = request.getOrderId() != null ? String.valueOf(request.getOrderId()) : String.valueOf(request.getVendorId());
        return "FRD-POST-" + timestamp + "-" + suffix;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0" : amount.toPlainString();
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String clean = value.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        return "****" + clean.substring(clean.length() - 4);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
