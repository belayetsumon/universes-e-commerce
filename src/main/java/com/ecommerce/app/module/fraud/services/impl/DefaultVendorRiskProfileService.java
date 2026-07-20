package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudPostOrderEventRequest;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.VendorRiskProfile;
import com.ecommerce.app.module.fraud.repository.VendorRiskProfileRepository;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.fraud.services.VendorRiskProfileService;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutMethodRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultVendorRiskProfileService implements VendorRiskProfileService {

    private static final String CFG_SPIKE_WINDOW_HOURS = "fraud.vendor.sales_spike.window_hours";
    private static final String CFG_SPIKE_ORDER_COUNT = "fraud.vendor.sales_spike.order_count";
    private static final String CFG_ABNORMAL_REFUND_RATE = "fraud.vendor.abnormal_refund_rate";
    private static final String CFG_ABNORMAL_CANCEL_RATE = "fraud.vendor.abnormal_cancel_rate";

    private final VendorRiskProfileRepository vendorRiskProfileRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final VendorprofileRepository vendorprofileRepository;
    private final VendorPayoutMethodRepository vendorPayoutMethodRepository;
    private final FraudPostOrderMonitoringService fraudPostOrderMonitoringService;
    private final FraudConfigurationService fraudConfigurationService;

    public DefaultVendorRiskProfileService(VendorRiskProfileRepository vendorRiskProfileRepository,
            SalesOrderRepository salesOrderRepository,
            ShipmentRepository shipmentRepository,
            VendorprofileRepository vendorprofileRepository,
            VendorPayoutMethodRepository vendorPayoutMethodRepository,
            FraudPostOrderMonitoringService fraudPostOrderMonitoringService,
            FraudConfigurationService fraudConfigurationService) {
        this.vendorRiskProfileRepository = vendorRiskProfileRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.shipmentRepository = shipmentRepository;
        this.vendorprofileRepository = vendorprofileRepository;
        this.vendorPayoutMethodRepository = vendorPayoutMethodRepository;
        this.fraudPostOrderMonitoringService = fraudPostOrderMonitoringService;
        this.fraudConfigurationService = fraudConfigurationService;
    }

    @Override
    @Transactional
    public void refreshVendorProfile(Long vendorId) {
        if (vendorId == null) {
            return;
        }
        VendorRiskProfile profile = vendorRiskProfileRepository.findByVendorId(vendorId)
                .orElseGet(() -> newProfile(vendorId));

        long orderCount = salesOrderRepository.countByVendorId(vendorId);
        long refundCount = salesOrderRepository.countByVendorIdAndStatusIn(vendorId,
                Set.of(OrderStatus.RETURN_REQUESTED, OrderStatus.PARTIALLY_RETURNED, OrderStatus.RETURNED));
        long cancelCount = salesOrderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.CANCELLED);
        long deliveredWithoutCarrier = shipmentRepository
                .countByVendorIdAndStatusAndCarrierMissing(vendorId, ShipmentStatus.DELIVERED);
        long recentOrderCount = salesOrderRepository.countByVendorIdAndCreatedAfter(
                vendorId,
                LocalDateTime.now().minusHours(fraudConfigurationService.getInt(CFG_SPIKE_WINDOW_HOURS, 24))
        );
        long spikeThreshold = Math.max(1, fraudConfigurationService.getInt(CFG_SPIKE_ORDER_COUNT, 20));
        long sharedBankAccounts = countSharedBankAccounts(vendorId);

        BigDecimal refundRate = rate(refundCount, orderCount);
        BigDecimal cancelRate = rate(cancelCount, orderCount);

        profile.setOrderCount(orderCount);
        profile.setRefundCount(refundCount);
        profile.setCancelCount(cancelCount);
        profile.setUnverifiedDeliveryCount(Math.max(profile.getUnverifiedDeliveryCount(), deliveredWithoutCarrier));
        profile.setFakeDeliveryCount(Math.max(profile.getFakeDeliveryCount(), deliveredWithoutCarrier));
        profile.setSharedBankAccountCount(sharedBankAccounts);
        profile.setSuddenSalesSpikeCount(recentOrderCount >= spikeThreshold ? recentOrderCount : 0);
        profile.setAbnormalRefundRate(refundRate);
        profile.setAbnormalCancellationRate(cancelRate);
        applyRisk(profile);
        vendorRiskProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void evaluateOrderForVendorRisk(SalesOrder order, FraudContext context) {
        if (order == null || order.getVendorId() == null) {
            return;
        }
        VendorRiskProfile profile = vendorRiskProfileRepository.findByVendorId(order.getVendorId())
                .orElseGet(() -> newProfile(order.getVendorId()));
        Vendorprofile vendor = vendorprofileRepository.findById(order.getVendorId()).orElse(null);

        boolean selfPurchase = isSelfPurchase(vendor, order);
        boolean sharedMobile = sharesMobile(vendor, order);
        boolean sharedAddress = sharesAddress(vendor, order);
        boolean sharedDevice = context != null && Boolean.TRUE.equals(context.getMetadata().get("vendorCustomerSharedDevice"));

        if (selfPurchase) {
            profile.setSelfPurchaseCount(profile.getSelfPurchaseCount() + 1);
        }
        if (sharedMobile) {
            profile.setSharedMobileCount(profile.getSharedMobileCount() + 1);
        }
        if (sharedAddress) {
            profile.setSharedAddressCount(profile.getSharedAddressCount() + 1);
        }
        if (selfPurchase || sharedMobile || sharedAddress || sharedDevice) {
            profile.setCollusionSignalCount(profile.getCollusionSignalCount() + 1);
            recordVendorRiskEvent(order, FraudPostOrderEventType.VENDOR_CUSTOMER_COLLUSION,
                    "Vendor/customer relationship signal detected.",
                    "selfPurchase", selfPurchase,
                    "sharedMobile", sharedMobile,
                    "sharedAddress", sharedAddress,
                    "sharedDevice", sharedDevice);
        }

        vendorRiskProfileRepository.save(profile);
        refreshVendorProfile(order.getVendorId());
    }

    @Override
    @Transactional
    public void recordTrackingReuseAttempt(Shipment requestedShipment, Shipment existingShipment) {
        Long vendorId = requestedShipment == null ? null : requestedShipment.getVendorId();
        if (vendorId == null) {
            return;
        }
        VendorRiskProfile profile = vendorRiskProfileRepository.findByVendorId(vendorId)
                .orElseGet(() -> newProfile(vendorId));
        profile.setTrackingReuseCount(profile.getTrackingReuseCount() + 1);
        profile.setCollusionSignalCount(profile.getCollusionSignalCount() + 1);
        applyRisk(profile);
        vendorRiskProfileRepository.save(profile);

        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(FraudPostOrderEventType.TRACKING_REUSE_ATTEMPT);
        request.setOrderId(requestedShipment.getSalesOrderId());
        request.setVendorId(vendorId);
        request.setAggregateType("SHIPMENT");
        request.setAggregateId(requestedShipment.getId());
        request.setReason("Tracking number reuse attempted.");
        request.getMetadata().put("trackingNumber", maskTracking(requestedShipment.getTrackingNumber()));
        request.getMetadata().put("existingShipmentId", existingShipment == null ? "" : String.valueOf(existingShipment.getId()));
        request.setIdempotencyKey("FRAUD:VENDOR:TRACKING_REUSE:" + vendorId + ":"
                + safe(requestedShipment.getTrackingNumber()) + ":" + safe(requestedShipment.getSalesOrderId()));
        fraudPostOrderMonitoringService.recordEvent(request);
    }

    @Override
    @Transactional
    public void recordDeliveryConfirmation(Shipment shipment, SalesOrder order) {
        if (shipment == null || shipment.getVendorId() == null || shipment.getStatus() != ShipmentStatus.DELIVERED) {
            return;
        }
        boolean carrierVerified = shipment.getCarrier() != null;
        if (carrierVerified) {
            refreshVendorProfile(shipment.getVendorId());
            return;
        }
        VendorRiskProfile profile = vendorRiskProfileRepository.findByVendorId(shipment.getVendorId())
                .orElseGet(() -> newProfile(shipment.getVendorId()));
        profile.setUnverifiedDeliveryCount(profile.getUnverifiedDeliveryCount() + 1);
        profile.setFakeDeliveryCount(profile.getFakeDeliveryCount() + 1);
        applyRisk(profile);
        vendorRiskProfileRepository.save(profile);

        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(FraudPostOrderEventType.FAKE_DELIVERY_SUSPECTED);
        request.setOrderId(order == null ? shipment.getSalesOrderId() : order.getId());
        request.setCustomerId(order == null || order.getCustomer() == null ? null : order.getCustomer().getId());
        request.setVendorId(shipment.getVendorId());
        request.setAggregateType("SHIPMENT");
        request.setAggregateId(shipment.getId());
        request.setReason("Delivery was confirmed without a linked carrier verification.");
        request.getMetadata().put("shipmentStatus", shipment.getStatus().name());
        request.getMetadata().put("carrierVerified", false);
        request.setIdempotencyKey("FRAUD:VENDOR:UNVERIFIED_DELIVERY:" + shipment.getId());
        fraudPostOrderMonitoringService.recordEvent(request);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVendorUnderReview(Long vendorId) {
        return vendorId != null && vendorRiskProfileRepository.existsByVendorIdAndUnderReviewTrue(vendorId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVendorPayoutHeld(Long vendorId) {
        return vendorId != null && vendorRiskProfileRepository.existsByVendorIdAndPayoutHeldTrue(vendorId);
    }

    private VendorRiskProfile newProfile(Long vendorId) {
        VendorRiskProfile profile = new VendorRiskProfile();
        profile.setVendorId(vendorId);
        profile.setRiskLevel(FraudRiskLevel.LOW);
        return profile;
    }

    private void applyRisk(VendorRiskProfile profile) {
        int score = 0;
        StringBuilder reason = new StringBuilder();

        score += add(profile.getSelfPurchaseCount() > 0, 40, reason, "self purchase");
        score += add(profile.getCollusionSignalCount() > 0, 30, reason, "collusion signals");
        score += add(profile.getSharedMobileCount() > 0, 20, reason, "shared mobile");
        score += add(profile.getSharedAddressCount() > 0, 20, reason, "shared address");
        score += add(profile.getSharedBankAccountCount() > 0, 35, reason, "shared payout account");
        score += add(profile.getTrackingReuseCount() > 0, 50, reason, "tracking reuse");
        score += add(profile.getUnverifiedDeliveryCount() > 0, 45, reason, "unverified delivery");
        score += add(profile.getSuddenSalesSpikeCount() > 0, 25, reason, "sales spike");
        score += add(rateExceeds(profile.getAbnormalRefundRate(), CFG_ABNORMAL_REFUND_RATE, "0.20"), 25, reason, "abnormal refund rate");
        score += add(rateExceeds(profile.getAbnormalCancellationRate(), CFG_ABNORMAL_CANCEL_RATE, "0.25"), 20, reason, "abnormal cancellation rate");

        int boundedScore = Math.max(0, Math.min(100, score));
        profile.setRiskScore(boundedScore);
        profile.setRiskLevel(riskLevel(boundedScore));
        profile.setUnderReview(boundedScore >= 40);
        profile.setPayoutHeld(boundedScore >= 60);
        profile.setLastRiskReason(reason.length() == 0 ? null : trim(reason.toString(), 500));
        profile.setLastAssessedAt(LocalDateTime.now());

        if (profile.isPayoutHeld()) {
            recordVendorRiskEscalated(profile);
        }
    }

    private int add(boolean condition, int score, StringBuilder reason, String label) {
        if (!condition) {
            return 0;
        }
        if (reason.length() > 0) {
            reason.append("; ");
        }
        reason.append(label);
        return score;
    }

    private boolean rateExceeds(BigDecimal actual, String key, String fallback) {
        BigDecimal threshold = fraudConfigurationService.getMoney(key, new BigDecimal(fallback));
        return actual != null && actual.compareTo(threshold) >= 0;
    }

    private FraudRiskLevel riskLevel(int score) {
        if (score >= 80) {
            return FraudRiskLevel.CRITICAL;
        }
        if (score >= 60) {
            return FraudRiskLevel.HIGH;
        }
        if (score >= 30) {
            return FraudRiskLevel.MEDIUM;
        }
        return FraudRiskLevel.LOW;
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private long countSharedBankAccounts(Long vendorId) {
        return vendorPayoutMethodRepository.findByVendorId(vendorId).stream()
                .map(VendorPayoutMethod::getAccountNumber)
                .map(this::clean)
                .filter(value -> value != null)
                .mapToLong(accountNumber -> vendorPayoutMethodRepository.countSharedAccountNumber(accountNumber, vendorId))
                .sum();
    }

    private boolean isSelfPurchase(Vendorprofile vendor, SalesOrder order) {
        if (vendor == null || order == null || order.getCustomer() == null) {
            return false;
        }
        Long customerId = order.getCustomer().getId();
        if (vendor.getUserId() != null && Objects.equals(vendor.getUserId().getId(), customerId)) {
            return true;
        }
        return vendor.getUsers() != null && vendor.getUsers().stream()
                .filter(Objects::nonNull)
                .anyMatch(user -> Objects.equals(user.getId(), customerId));
    }

    private boolean sharesMobile(Vendorprofile vendor, SalesOrder order) {
        String vendorMobile = clean(vendor == null ? null : vendor.getPhone());
        if (vendorMobile == null || order == null) {
            return false;
        }
        Users customer = order.getCustomer();
        ShippingAddress shippingAddress = order.getShippingAddress();
        return vendorMobile.equals(clean(customer == null ? null : customer.getMobile()))
                || vendorMobile.equals(clean(order.getMobileNumber()))
                || vendorMobile.equals(clean(shippingAddress == null ? null : shippingAddress.getMobile()));
    }

    private boolean sharesAddress(Vendorprofile vendor, SalesOrder order) {
        String vendorAddress = normalizeAddress(vendor == null ? null : vendor.getAddress());
        if (vendorAddress == null || order == null || order.getShippingAddress() == null) {
            return false;
        }
        ShippingAddress address = order.getShippingAddress();
        String shippingAddress = normalizeAddress(String.join(" ",
                safe(address.getAddressLineOne()),
                safe(address.getAddressLinetwo()),
                safe(address.getCity()),
                safe(address.getDistrict()),
                safe(address.getPostCode()),
                safe(address.getCountry())
        ));
        return vendorAddress.equals(shippingAddress);
    }

    private void recordVendorRiskEvent(SalesOrder order, FraudPostOrderEventType eventType, String reason,
            String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(eventType);
        request.setOrderId(order.getId());
        request.setCustomerId(order.getCustomer() == null ? null : order.getCustomer().getId());
        request.setVendorId(order.getVendorId());
        request.setAggregateType("SALES_ORDER");
        request.setAggregateId(order.getId());
        request.setReason(reason);
        request.getMetadata().put(key1, value1);
        request.getMetadata().put(key2, value2);
        request.getMetadata().put(key3, value3);
        request.getMetadata().put(key4, value4);
        request.setIdempotencyKey("FRAUD:VENDOR:COLLUSION:" + order.getId());
        fraudPostOrderMonitoringService.recordEvent(request);
    }

    private void recordVendorRiskEscalated(VendorRiskProfile profile) {
        FraudPostOrderEventRequest request = new FraudPostOrderEventRequest();
        request.setEventType(FraudPostOrderEventType.VENDOR_RISK_ESCALATED);
        request.setVendorId(profile.getVendorId());
        request.setAggregateType("VENDOR");
        request.setAggregateId(profile.getVendorId());
        request.setReason("Vendor risk score requires payout hold or manual review.");
        request.getMetadata().put("riskScore", profile.getRiskScore());
        request.getMetadata().put("riskLevel", profile.getRiskLevel().name());
        request.getMetadata().put("riskReason", safe(profile.getLastRiskReason()));
        request.setIdempotencyKey("FRAUD:VENDOR:RISK_ESCALATED:" + profile.getVendorId() + ":" + profile.getRiskScore());
        fraudPostOrderMonitoringService.recordEvent(request);
    }

    private String normalizeAddress(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.replaceAll("[^a-z0-9]+", " ").trim();
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String maskTracking(String value) {
        String cleanValue = safe(value);
        if (cleanValue.length() <= 4) {
            return "****";
        }
        return "****" + cleanValue.substring(cleanValue.length() - 4);
    }

    private String trim(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
