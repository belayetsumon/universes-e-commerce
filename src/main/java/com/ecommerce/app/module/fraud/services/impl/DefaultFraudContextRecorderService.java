package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.model.DeviceIdentity;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.services.FraudContextRecorderService;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DefaultFraudContextRecorderService implements FraudContextRecorderService {

    private final DeviceIdentityRepository deviceIdentityRepository;
    private final OrderVelocityService orderVelocityService;
    private final TransactionTemplate requiresNew;

    public DefaultFraudContextRecorderService(
            DeviceIdentityRepository deviceIdentityRepository,
            OrderVelocityService orderVelocityService,
            PlatformTransactionManager transactionManager
    ) {
        this.deviceIdentityRepository = deviceIdentityRepository;
        this.orderVelocityService = orderVelocityService;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void recordOrderAttempt(SalesOrder order, FraudContext context) {
        FraudContext safeContext = context == null ? new FraudContext() : context;
        Long customerId = order == null || order.getCustomer() == null ? null : order.getCustomer().getId();
        increment(VelocityCounterScope.CUSTOMER, customerId == null ? null : String.valueOf(customerId));
        increment(VelocityCounterScope.MOBILE_NUMBER, metadataText(safeContext, "mobileNumber"));
        increment(VelocityCounterScope.IP_ADDRESS, safeContext.getIpAddress());
        increment(VelocityCounterScope.DEVICE, resolveDeviceIdentifier(safeContext));
        if (order != null) {
            increment(VelocityCounterScope.VENDOR, order.getVendorId() == null ? null : String.valueOf(order.getVendorId()));
        }
        recordDevice(customerId, safeContext);
    }

    private void increment(VelocityCounterScope scope, String value) {
        if (value != null && !value.isBlank()) {
            orderVelocityService.increment(scope, value);
        }
    }

    private void recordDevice(Long customerId, FraudContext context) {
        String deviceIdentifier = resolveDeviceIdentifier(context);
        if (deviceIdentifier == null) {
            return;
        }
        String actorKey = customerId == null
                ? "SESSION:" + safe(context.getSessionIdentifier())
                : "CUSTOMER:" + customerId;
        String identityKey = FraudHashingSupport.sha256(deviceIdentifier + "|" + actorKey);
        if (identityKey == null) {
            return;
        }
        try {
            requiresNew.executeWithoutResult(status -> saveDevice(identityKey, deviceIdentifier, customerId, context));
        } catch (DataIntegrityViolationException concurrentInsert) {
            requiresNew.executeWithoutResult(status -> saveDevice(identityKey, deviceIdentifier, customerId, context));
        }
    }

    private void saveDevice(
            String identityKey,
            String deviceIdentifier,
            Long customerId,
            FraudContext context
    ) {
        LocalDateTime now = LocalDateTime.now();
        DeviceIdentity identity = deviceIdentityRepository.findByIdentityKeyForUpdate(identityKey)
                .or(() -> deviceIdentityRepository.findFirstByDeviceIdentifierAndCustomerIdOrderByIdAsc(
                deviceIdentifier,
                customerId
        ))
                .orElseGet(() -> {
                    DeviceIdentity created = new DeviceIdentity();
                    created.setDeviceIdentifier(deviceIdentifier);
                    created.setFirstSeenAt(now);
                    return created;
                });
        identity.setIdentityKey(identityKey);
        identity.setCustomerId(customerId);
        identity.setDeviceFingerprintHash(resolveFingerprintHash(context));
        identity.setUserAgent(trim(context.getUserAgent(), 500));
        identity.setSessionIdentifier(FraudHashingSupport.sha256(context.getSessionIdentifier()));
        identity.setIpAddress(FraudHashingSupport.sha256(context.getIpAddress()));
        identity.setIpCountry(trim(context.getIpCountry(), 80));
        identity.setIpLocation(trim(context.getIpLocation(), 160));
        identity.setVpnIndicator(booleanMetadata(context, "vpnIndicator"));
        identity.setProxyIndicator(booleanMetadata(context, "proxyIndicator"));
        identity.setHostingIndicator(booleanMetadata(context, "hostingIndicator"));
        identity.setLastSeenAt(now);
        deviceIdentityRepository.saveAndFlush(identity);
    }

    private String resolveDeviceIdentifier(FraudContext context) {
        String identifier = FraudHashingSupport.canonicalIdentifierHash(
                context == null ? null : context.getDeviceIdentifier()
        );
        if (identifier != null) {
            return identifier;
        }
        return resolveFingerprintHash(context);
    }

    private String resolveFingerprintHash(FraudContext context) {
        return FraudHashingSupport.canonicalIdentifierHash(
                context == null ? null : context.getDeviceFingerprint()
        );
    }

    private boolean booleanMetadata(FraudContext context, String key) {
        Object value = context == null || context.getMetadata() == null ? null : context.getMetadata().get(key);
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private String metadataText(FraudContext context, String key) {
        Object value = context == null || context.getMetadata() == null ? null : context.getMetadata().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }
}
