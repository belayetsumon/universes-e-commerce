package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.model.CustomerRiskProfile;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.CustomerRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.CodEligibilityService;
import com.ecommerce.app.module.fraud.services.CodRiskProfileService;
import com.ecommerce.app.module.fraud.services.CodRiskService;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultCodRiskService implements CodRiskService, CodEligibilityService, CodRiskProfileService {

    private static final BigDecimal DEFAULT_FIRST_ORDER_COD_LIMIT = new BigDecimal("5000.00");
    private static final BigDecimal DEFAULT_HIGH_VALUE_COD_THRESHOLD = new BigDecimal("15000.00");

    private final CodRiskProfileRepository codRiskProfileRepository;
    private final CustomerRiskProfileRepository customerRiskProfileRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final FraudConfigurationService fraudConfigurationService;
    private final FraudEventLogRepository fraudEventLogRepository;
    private final FraudEventPublisher fraudEventPublisher;

    public DefaultCodRiskService(CodRiskProfileRepository codRiskProfileRepository,
            CustomerRiskProfileRepository customerRiskProfileRepository,
            SalesOrderRepository salesOrderRepository,
            FraudConfigurationService fraudConfigurationService,
            FraudEventLogRepository fraudEventLogRepository,
            FraudEventPublisher fraudEventPublisher) {
        this.codRiskProfileRepository = codRiskProfileRepository;
        this.customerRiskProfileRepository = customerRiskProfileRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.fraudConfigurationService = fraudConfigurationService;
        this.fraudEventLogRepository = fraudEventLogRepository;
        this.fraudEventPublisher = fraudEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudGuardResult checkCodCheckoutEligibility(Long customerId, Long vendorId, BigDecimal orderTotal,
            String paymentPlan, boolean mobileVerified, FraudContext context) {
        if (!isCodPaymentPlan(paymentPlan)) {
            return FraudGuardResult.allowed();
        }

        FraudContext safeContext = context == null ? new FraudContext() : context;
        BigDecimal total = money(orderTotal);
        if (isCodDisabled(customerId, vendorId, safeContext)) {
            return FraudGuardResult.blocked("Cash on Delivery is currently unavailable for this checkout. Please choose prepaid payment.");
        }
        if (!mobileVerified) {
            return FraudGuardResult.blocked("Mobile OTP verification is required before placing a Cash on Delivery order.");
        }
        if (isAddressChangedAfterConfirmation(safeContext)) {
            return FraudGuardResult.blocked("Delivery address changed after confirmation. Please re-verify the delivery address before COD fulfilment.");
        }

        boolean firstOrder = isFirstOrder(customerId);
        BigDecimal firstOrderLimit = fraudConfigurationService.getMoney("fraud.cod.first_order_limit", DEFAULT_FIRST_ORDER_COD_LIMIT);
        if (firstOrder && total.compareTo(firstOrderLimit) > 0) {
            return FraudGuardResult.blocked("First Cash on Delivery order exceeds the configured COD limit. Please choose prepaid or partial advance payment.");
        }

        if (PAYMENT_PLAN_FULL_COD.equals(paymentPlan) && requiresPartialPrepayment(customerId, vendorId, safeContext)) {
            return FraudGuardResult.blocked("COD requires a partial advance payment because of previous delivery risk.");
        }

        BigDecimal customerLimit = resolveCustomerCodLimit(customerId);
        if (customerLimit != null && total.compareTo(customerLimit) > 0) {
            return FraudGuardResult.blocked("Order amount exceeds the customer COD limit. Please choose prepaid or partial advance payment.");
        }
        BigDecimal vendorLimit = resolveVendorCodLimit(vendorId);
        if (vendorLimit != null && total.compareTo(vendorLimit) > 0) {
            return FraudGuardResult.blocked("Order amount exceeds the vendor COD limit. Please choose prepaid or partial advance payment.");
        }

        BigDecimal highValueThreshold = fraudConfigurationService.getMoney(
                "fraud.cod.high_value_confirmation_threshold",
                DEFAULT_HIGH_VALUE_COD_THRESHOLD
        );
        if (PAYMENT_PLAN_FULL_COD.equals(paymentPlan) && total.compareTo(highValueThreshold) > 0) {
            return FraudGuardResult.blocked("High-value COD orders require customer-service confirmation or partial advance payment.");
        }

        return FraudGuardResult.allowed();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean requiresFirstOrderOtp(SalesOrder order, FraudContext context) {
        if (!isCodOrder(order, context)) {
            return false;
        }
        Long customerId = customerId(order);
        return isFirstOrder(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean requiresPartialPrepayment(SalesOrder order, int riskScore) {
        if (!isCodOrder(order, null)) {
            return false;
        }
        if (riskScore >= 60) {
            return true;
        }
        return requiresPartialPrepayment(customerId(order), order == null ? null : order.getVendorId(), buildContext(order, null));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodDisabledForCustomer(Long customerId) {
        return customerId != null && (codRiskProfileRepository.existsByCustomerIdAndCodDisabledTrue(customerId)
                || customerRiskProfileRepository.existsByCustomerIdAndCodDisabledTrue(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodDisabledForVendor(Long vendorId) {
        return vendorId != null && codRiskProfileRepository.existsByVendorIdAndCodDisabledTrue(vendorId);
    }

    @Override
    @Transactional
    public void recordCodOrderPlaced(SalesOrder order, FraudContext context) {
        if (!isCodOrder(order, context)) {
            return;
        }
        for (CodRiskProfile profile : resolveProfiles(order, context)) {
            profile.setCodOrderCount(profile.getCodOrderCount() + 1);
            profile.setLastUpdatedAt(LocalDateTime.now());
            codRiskProfileRepository.save(profile);
        }
    }

    @Override
    @Transactional
    public void recordCodShipmentOutcome(SalesOrder order, ShipmentStatus status, String refusalReason, FraudContext context) {
        if (order == null || order.getId() == null || !isCodOrder(order, context) || status == null) {
            return;
        }
        if (status != ShipmentStatus.DELIVERED && status != ShipmentStatus.FAILED && status != ShipmentStatus.RETURNED) {
            return;
        }

        List<CodRiskProfile> profiles = resolveProfiles(order, context);
        boolean disabled = false;
        for (CodRiskProfile profile : profiles) {
            if (status == ShipmentStatus.DELIVERED) {
                profile.setCodSuccessCount(profile.getCodSuccessCount() + 1);
            } else if (status == ShipmentStatus.FAILED) {
                profile.setDeliveryRefusalCount(profile.getDeliveryRefusalCount() + 1);
                profile.setLastDeliveryRefusalReason(trimReason(refusalReason));
            } else if (status == ShipmentStatus.RETURNED) {
                profile.setCodRtoCount(profile.getCodRtoCount() + 1);
                profile.setLastDeliveryRefusalReason(trimReason(refusalReason));
            }
            profile.setLastUpdatedAt(LocalDateTime.now());
            if (shouldDisableCod(profile)) {
                profile.setCodDisabled(true);
                disabled = true;
            }
            codRiskProfileRepository.save(profile);
        }
        updateCustomerProfile(order, status, disabled);
        if (disabled) {
            recordCodDisabledEvent(order, refusalReason);
        }
    }

    @Override
    @Transactional
    public void recordSuccessfulPrepaidOrder(SalesOrder order, FraudContext context) {
        if (order == null || order.getId() == null || isCodOrder(order, context)) {
            return;
        }
        if (order.getPaymentPlan() != OrderPaymentPlan.FULL_PREPAID && order.getPaymentPlan() != OrderPaymentPlan.EMI) {
            return;
        }

        int restoreThreshold = fraudConfigurationService.getInt("fraud.cod.restore_after_prepaid_success_count", 3);
        for (CodRiskProfile profile : resolveProfiles(order, context)) {
            profile.setSuccessfulPrepaidOrderCount(profile.getSuccessfulPrepaidOrderCount() + 1);
            if (profile.getSuccessfulPrepaidOrderCount() >= restoreThreshold) {
                profile.setCodDisabled(false);
            }
            profile.setLastUpdatedAt(LocalDateTime.now());
            codRiskProfileRepository.save(profile);
        }
        restoreCustomerProfileIfEligible(order, restoreThreshold);
    }

    private boolean isCodDisabled(Long customerId, Long vendorId, FraudContext context) {
        if (isCodDisabledForCustomer(customerId) || isCodDisabledForVendor(vendorId)) {
            return true;
        }
        String mobileHash = FraudHashingSupport.sha256(metadataText(context, "mobileNumber"));
        String addressHash = FraudHashingSupport.sha256(metadataText(context, "addressKey"));
        return (mobileHash != null && codRiskProfileRepository.existsByMobileHashAndCodDisabledTrue(mobileHash))
                || (addressHash != null && codRiskProfileRepository.existsByAddressHashAndCodDisabledTrue(addressHash))
                || (context.getDeviceIdentifier() != null && codRiskProfileRepository.existsByDeviceIdentifierAndCodDisabledTrue(context.getDeviceIdentifier()))
                || (context.getShippingDistrict() != null && codRiskProfileRepository.existsByDistrictIgnoreCaseAndCodDisabledTrue(context.getShippingDistrict()));
    }

    private boolean requiresPartialPrepayment(Long customerId, Long vendorId, FraudContext context) {
        int rtoThreshold = fraudConfigurationService.getInt("fraud.cod.high_risk_partial_prepayment_rto_count", 1);
        int refusalThreshold = fraudConfigurationService.getInt("fraud.cod.high_risk_partial_prepayment_refusal_count", 1);
        return profileRequiresPartial(codProfileByCustomer(customerId).orElse(null), rtoThreshold, refusalThreshold)
                || profileRequiresPartial(codProfileByVendor(vendorId).orElse(null), rtoThreshold, refusalThreshold)
                || profileRequiresPartial(findMobileProfile(context).orElse(null), rtoThreshold, refusalThreshold)
                || profileRequiresPartial(findAddressProfile(context).orElse(null), rtoThreshold, refusalThreshold)
                || profileRequiresPartial(findDeviceProfile(context).orElse(null), rtoThreshold, refusalThreshold)
                || profileRequiresPartial(findDistrictProfile(context).orElse(null), rtoThreshold, refusalThreshold);
    }

    private boolean profileRequiresPartial(CodRiskProfile profile, int rtoThreshold, int refusalThreshold) {
        return profile != null && (profile.getCodRtoCount() >= rtoThreshold
                || profile.getDeliveryRefusalCount() >= refusalThreshold);
    }

    private List<CodRiskProfile> resolveProfiles(SalesOrder order, FraudContext context) {
        FraudContext safeContext = buildContext(order, context);
        List<CodRiskProfile> profiles = new ArrayList<>();
        Long customerId = customerId(order);
        Long vendorId = order == null ? null : order.getVendorId();
        if (customerId != null) {
            profiles.add(codProfileByCustomer(customerId).orElseGet(() -> newCustomerProfile(customerId)));
        }
        if (vendorId != null) {
            profiles.add(codProfileByVendor(vendorId).orElseGet(() -> newVendorProfile(vendorId)));
        }
        String mobileHash = FraudHashingSupport.sha256(metadataText(safeContext, "mobileNumber"));
        if (mobileHash != null) {
            profiles.add(codRiskProfileRepository.findByMobileHash(mobileHash).orElseGet(() -> newMobileProfile(mobileHash)));
        }
        String addressHash = FraudHashingSupport.sha256(metadataText(safeContext, "addressKey"));
        if (addressHash != null) {
            profiles.add(codRiskProfileRepository.findByAddressHash(addressHash).orElseGet(() -> newAddressProfile(addressHash)));
        }
        if (safeContext.getDeviceIdentifier() != null) {
            profiles.add(codRiskProfileRepository.findByDeviceIdentifier(safeContext.getDeviceIdentifier())
                    .orElseGet(() -> newDeviceProfile(safeContext.getDeviceIdentifier())));
        }
        if (safeContext.getShippingDistrict() != null && !safeContext.getShippingDistrict().isBlank()) {
            String district = safeContext.getShippingDistrict().trim();
            profiles.add(codRiskProfileRepository.findByDistrictIgnoreCase(district).orElseGet(() -> newDistrictProfile(district)));
        }
        return profiles;
    }

    private void updateCustomerProfile(SalesOrder order, ShipmentStatus status, boolean disabled) {
        Long customerId = customerId(order);
        if (customerId == null) {
            return;
        }
        CustomerRiskProfile profile = customerRiskProfileRepository.findByCustomerId(customerId).orElseGet(() -> {
            CustomerRiskProfile created = new CustomerRiskProfile();
            created.setCustomerId(customerId);
            return created;
        });
        if (status == ShipmentStatus.FAILED) {
            profile.setDeliveryRefusalCount(profile.getDeliveryRefusalCount() + 1);
        } else if (status == ShipmentStatus.RETURNED) {
            profile.setCodRtoCount(profile.getCodRtoCount() + 1);
        } else if (status == ShipmentStatus.DELIVERED) {
            profile.setSuccessfulOrderCount(profile.getSuccessfulOrderCount() + 1);
        }
        if (disabled) {
            profile.setCodDisabled(true);
        }
        profile.setLastAssessedAt(LocalDateTime.now());
        customerRiskProfileRepository.save(profile);
    }

    private void restoreCustomerProfileIfEligible(SalesOrder order, int restoreThreshold) {
        Long customerId = customerId(order);
        if (customerId == null) {
            return;
        }
        CustomerRiskProfile profile = customerRiskProfileRepository.findByCustomerId(customerId).orElseGet(() -> {
            CustomerRiskProfile created = new CustomerRiskProfile();
            created.setCustomerId(customerId);
            return created;
        });
        profile.setSuccessfulOrderCount(profile.getSuccessfulOrderCount() + 1);
        if (profile.getSuccessfulOrderCount() >= restoreThreshold) {
            profile.setCodDisabled(false);
        }
        profile.setLastAssessedAt(LocalDateTime.now());
        customerRiskProfileRepository.save(profile);
    }

    private boolean shouldDisableCod(CodRiskProfile profile) {
        int rtoThreshold = fraudConfigurationService.getInt("fraud.cod.rto_disable_threshold", 2);
        int refusalThreshold = fraudConfigurationService.getInt("fraud.cod.delivery_refusal_disable_threshold", 2);
        return profile.getCodRtoCount() >= rtoThreshold || profile.getDeliveryRefusalCount() >= refusalThreshold;
    }

    private boolean isFirstOrder(Long customerId) {
        return customerId == null || salesOrderRepository.countByCustomer_Id(customerId) == 0;
    }

    private BigDecimal resolveCustomerCodLimit(Long customerId) {
        return codProfileByCustomer(customerId).map(CodRiskProfile::getCustomerCodLimit).orElse(null);
    }

    private BigDecimal resolveVendorCodLimit(Long vendorId) {
        return codProfileByVendor(vendorId).map(CodRiskProfile::getVendorCodLimit).orElse(null);
    }

    private Optional<CodRiskProfile> codProfileByCustomer(Long customerId) {
        return customerId == null ? Optional.empty() : codRiskProfileRepository.findByCustomerId(customerId);
    }

    private Optional<CodRiskProfile> codProfileByVendor(Long vendorId) {
        return vendorId == null ? Optional.empty() : codRiskProfileRepository.findByVendorId(vendorId);
    }

    private Optional<CodRiskProfile> findMobileProfile(FraudContext context) {
        String mobileHash = FraudHashingSupport.sha256(metadataText(context, "mobileNumber"));
        return mobileHash == null ? Optional.empty() : codRiskProfileRepository.findByMobileHash(mobileHash);
    }

    private Optional<CodRiskProfile> findAddressProfile(FraudContext context) {
        String addressHash = FraudHashingSupport.sha256(metadataText(context, "addressKey"));
        return addressHash == null ? Optional.empty() : codRiskProfileRepository.findByAddressHash(addressHash);
    }

    private Optional<CodRiskProfile> findDeviceProfile(FraudContext context) {
        return context == null || context.getDeviceIdentifier() == null
                ? Optional.empty()
                : codRiskProfileRepository.findByDeviceIdentifier(context.getDeviceIdentifier());
    }

    private Optional<CodRiskProfile> findDistrictProfile(FraudContext context) {
        return context == null || context.getShippingDistrict() == null
                ? Optional.empty()
                : codRiskProfileRepository.findByDistrictIgnoreCase(context.getShippingDistrict());
    }

    private CodRiskProfile newCustomerProfile(Long customerId) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setCustomerId(customerId);
        return profile;
    }

    private CodRiskProfile newVendorProfile(Long vendorId) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setVendorId(vendorId);
        return profile;
    }

    private CodRiskProfile newMobileProfile(String mobileHash) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setMobileHash(mobileHash);
        return profile;
    }

    private CodRiskProfile newAddressProfile(String addressHash) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setAddressHash(addressHash);
        return profile;
    }

    private CodRiskProfile newDeviceProfile(String deviceIdentifier) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setDeviceIdentifier(deviceIdentifier);
        return profile;
    }

    private CodRiskProfile newDistrictProfile(String district) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setDistrict(district);
        return profile;
    }

    private FraudContext buildContext(SalesOrder order, FraudContext context) {
        FraudContext safeContext = context == null ? new FraudContext() : context;
        if (order != null && order.getShippingAddress() != null) {
            safeContext.setShippingDistrict(safeContext.getShippingDistrict() == null
                    ? order.getShippingAddress().getDistrict()
                    : safeContext.getShippingDistrict());
            safeContext.getMetadata().putIfAbsent("mobileNumber", order.getShippingAddress().getMobile());
            safeContext.getMetadata().putIfAbsent("addressKey", String.join("|",
                    safe(order.getShippingAddress().getAddressLineOne()),
                    safe(order.getShippingAddress().getAddressLinetwo()),
                    safe(order.getShippingAddress().getCity()),
                    safe(order.getShippingAddress().getDistrict()),
                    safe(order.getShippingAddress().getPostCode()),
                    safe(order.getShippingAddress().getCountry()),
                    safe(order.getShippingAddress().getMobile())));
        }
        return safeContext;
    }

    private boolean isCodOrder(SalesOrder order, FraudContext context) {
        if (context != null && context.getPaymentMethod() != null && "COD".equalsIgnoreCase(context.getPaymentMethod())) {
            return true;
        }
        return order != null && (order.getPaymentPlan() == OrderPaymentPlan.FULL_COD
                || order.getPaymentPlan() == OrderPaymentPlan.PARTIAL_ADVANCE_COD);
    }

    private boolean isCodPaymentPlan(String paymentPlan) {
        return PAYMENT_PLAN_FULL_COD.equals(paymentPlan) || PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan);
    }

    private boolean isAddressChangedAfterConfirmation(FraudContext context) {
        Object value = context == null || context.getMetadata() == null
                ? null
                : context.getMetadata().get("deliveryAddressChangedAfterConfirmation");
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private Long customerId(SalesOrder order) {
        return order == null || order.getCustomer() == null ? null : order.getCustomer().getId();
    }

    private BigDecimal money(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String metadataText(FraudContext context, String key) {
        Object value = context == null || context.getMetadata() == null ? null : context.getMetadata().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String trimReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void recordCodDisabledEvent(SalesOrder order, String reason) {
        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setEventType(FraudEventType.COD_DISABLED);
        eventLog.setAggregateType("SALES_ORDER");
        eventLog.setAggregateId(order.getId());
        eventLog.setOrderId(order.getId());
        eventLog.setCustomerId(customerId(order));
        eventLog.setVendorId(order.getVendorId());
        eventLog.setPayloadJson("{\"reason\":\"" + json(reason) + "\"}");
        eventLog.setEventTime(LocalDateTime.now());
        fraudEventLogRepository.save(eventLog);
        fraudEventPublisher.publish(eventLog.getEventType(), eventLog.getAggregateType(), eventLog.getAggregateId(),
                "{\"reason\":\"" + json(reason) + "\",\"orderId\":" + nullToJson(eventLog.getOrderId())
                        + ",\"customerId\":" + nullToJson(eventLog.getCustomerId())
                        + ",\"vendorId\":" + nullToJson(eventLog.getVendorId()) + "}",
                eventLog.getCorrelationId(), eventLog.getIdempotencyKey());
    }

    private String nullToJson(Long value) {
        return value == null ? "null" : value.toString();
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final String PAYMENT_PLAN_FULL_COD = "FULL_COD";
    private static final String PAYMENT_PLAN_PARTIAL_ADVANCE_COD = "PARTIAL_ADVANCE_COD";
}
