package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentCreateRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.services.FraudAssessmentService;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.fraud.services.FraudFulfilmentGuard;
import com.ecommerce.app.module.fraud.services.FraudOrderAssessmentGuard;
import com.ecommerce.app.module.fraud.services.FraudPaymentCaptureGuard;
import com.ecommerce.app.module.fraud.services.FraudPayoutGuard;
import com.ecommerce.app.module.fraud.services.FraudPreOrderGuard;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudIntegrationGuard implements FraudPreOrderGuard, FraudOrderAssessmentGuard,
        FraudPaymentCaptureGuard, FraudFulfilmentGuard, FraudPayoutGuard {

    private static final String SAFE_BLOCK_MESSAGE = "Order is under fraud verification. Payment and fulfilment are blocked until review is complete.";
    private static final String SAFE_REJECT_MESSAGE = "Order cannot be processed. Please contact customer support.";

    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudBlocklistRepository fraudBlocklistRepository;
    private final FraudAssessmentService fraudAssessmentService;
    private final FraudCaseService fraudCaseService;

    public DefaultFraudIntegrationGuard(FraudAssessmentRepository fraudAssessmentRepository,
            FraudBlocklistRepository fraudBlocklistRepository,
            FraudAssessmentService fraudAssessmentService,
            FraudCaseService fraudCaseService) {
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudBlocklistRepository = fraudBlocklistRepository;
        this.fraudAssessmentService = fraudAssessmentService;
        this.fraudCaseService = fraudCaseService;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudGuardResult checkCheckoutEligibility(Long customerId, FraudContext context) {
        FraudContext safeContext = context == null ? new FraudContext() : context;
        if (isBlocked(FraudBlockType.CUSTOMER, customerId == null ? null : String.valueOf(customerId))
                || isBlocked(FraudBlockType.ACCOUNT, customerId == null ? null : String.valueOf(customerId))) {
            return FraudGuardResult.blocked(SAFE_REJECT_MESSAGE);
        }
        if (isBlocked(FraudBlockType.MOBILE_NUMBER, metadataText(safeContext, "mobileNumber"))
                || isBlocked(FraudBlockType.DEVICE, safeContext.getDeviceIdentifier())
                || isBlocked(FraudBlockType.DEVICE, safeContext.getDeviceFingerprint())
                || isBlocked(FraudBlockType.IP_ADDRESS, safeContext.getIpAddress())) {
            return FraudGuardResult.blocked(SAFE_REJECT_MESSAGE);
        }
        return FraudGuardResult.allowed();
    }

    @Override
    @Transactional
    public FraudGuardResult checkOrderAllowed(SalesOrder order) {
        return checkOrderAllowed(order, null);
    }

    @Override
    @Transactional
    public FraudGuardResult checkOrderAllowed(SalesOrder order, FraudContext context) {
        FraudAssessmentResponse assessment = ensureAssessment(order, context);
        if (isRejected(assessment)) {
            return withAssessment(FraudGuardResult.blocked(SAFE_REJECT_MESSAGE), assessment);
        }
        return withAssessment(FraudGuardResult.allowed(), assessment);
    }

    @Override
    @Transactional
    public FraudGuardResult checkPaymentCaptureAllowed(SalesOrder order) {
        return requireApproved(order, "Payment capture is blocked until fraud approval is complete.");
    }

    @Override
    @Transactional(readOnly = true)
    public FraudGuardResult checkRefundAllowed(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return FraudGuardResult.blocked("Order not found for fraud refund check.");
        }
        Optional<FraudAssessment> assessment = fraudAssessmentRepository.findTopByOrderIdOrderByIdDesc(order.getId());
        if (fraudCaseService.hasOpenCaseForOrder(order.getId())) {
            return withAssessment(FraudGuardResult.blocked("Refund is blocked while a fraud case is open."), assessment.orElse(null));
        }
        if (assessment.isPresent() && isBlockingStatus(assessment.get().getStatus())) {
            return withAssessment(FraudGuardResult.blocked("Refund is blocked until fraud verification is complete."), assessment.get());
        }
        return withAssessment(FraudGuardResult.allowed(), assessment.orElse(null));
    }

    @Override
    @Transactional
    public FraudGuardResult checkPackingAllowed(SalesOrder order) {
        return requireApproved(order, "Packing is blocked until fraud approval is complete.");
    }

    @Override
    @Transactional
    public FraudGuardResult checkShipmentCreationAllowed(SalesOrder order) {
        return requireApproved(order, "Shipment creation is blocked until fraud approval is complete.");
    }

    @Override
    @Transactional
    public FraudGuardResult checkFulfilmentAllowed(SalesOrder order) {
        return requireApproved(order, "Fulfilment is blocked until fraud approval is complete.");
    }

    @Override
    @Transactional(readOnly = true)
    public FraudGuardResult checkVendorPayoutAllowed(Long vendorId) {
        if (vendorId == null) {
            return FraudGuardResult.blocked("Vendor not found for fraud payout check.");
        }
        if (fraudCaseService.hasOpenCaseForVendor(vendorId)) {
            return FraudGuardResult.blocked("Vendor payout is blocked while a fraud case is open.");
        }
        return FraudGuardResult.allowed();
    }

    private FraudGuardResult requireApproved(SalesOrder order, String reason) {
        FraudAssessmentResponse assessment = ensureAssessment(order, null);
        if (assessment.getStatus() == FraudAssessmentStatus.APPROVED && assessment.getDecision() == FraudDecision.APPROVE) {
            return withAssessment(FraudGuardResult.allowed(), assessment);
        }
        if (isRejected(assessment)) {
            return withAssessment(FraudGuardResult.blocked(SAFE_REJECT_MESSAGE), assessment);
        }
        return withAssessment(FraudGuardResult.blocked(reason), assessment);
    }

    private FraudAssessmentResponse ensureAssessment(SalesOrder order, FraudContext context) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found for fraud assessment.");
        }
        Optional<FraudAssessment> existing = fraudAssessmentRepository.findTopByOrderIdOrderByIdDesc(order.getId());
        if (existing.isPresent()) {
            return fraudAssessmentService.findById(existing.get().getId());
        }

        FraudAssessmentCreateRequest request = new FraudAssessmentCreateRequest();
        request.setOrderId(order.getId());
        request.setCorrelationId(order.getUuid());
        request.setIdempotencyKey("ORDER-" + order.getId() + "-FRAUD-ASSESSMENT");
        request.setContext(context == null ? new FraudContext() : context);
        return fraudAssessmentService.evaluate(request);
    }

    private boolean isRejected(FraudAssessmentResponse assessment) {
        return assessment != null
                && (assessment.getStatus() == FraudAssessmentStatus.FRAUD_REJECTED
                || assessment.getDecision() == FraudDecision.REJECT
                || assessment.getDecision() == FraudDecision.BLOCK
                || assessment.getDecision() == FraudDecision.CANCEL);
    }

    private boolean isBlockingStatus(FraudAssessmentStatus status) {
        return status == FraudAssessmentStatus.FRAUD_EVALUATION_PENDING
                || status == FraudAssessmentStatus.VERIFICATION_REQUIRED
                || status == FraudAssessmentStatus.MANUAL_REVIEW
                || status == FraudAssessmentStatus.FRAUD_HOLD
                || status == FraudAssessmentStatus.FRAUD_REJECTED;
    }

    private boolean isBlocked(FraudBlockType blockType, String rawValue) {
        String hashedValue = FraudHashingSupport.sha256(rawValue);
        if (hashedValue == null) {
            return false;
        }
        Optional<FraudBlocklist> block = fraudBlocklistRepository.findByBlockTypeAndHashedValueAndActiveTrue(blockType, hashedValue);
        return block.filter(this::isEffectiveBlock).isPresent();
    }

    private boolean isEffectiveBlock(FraudBlocklist block) {
        return block != null && (!block.isTemporary() || block.getExpiresAt() == null || block.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    private String metadataText(FraudContext context, String key) {
        Object value = context == null || context.getMetadata() == null ? null : context.getMetadata().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private FraudGuardResult withAssessment(FraudGuardResult result, FraudAssessmentResponse assessment) {
        if (result != null && assessment != null) {
            result.setAssessmentId(assessment.getId());
        }
        return result;
    }

    private FraudGuardResult withAssessment(FraudGuardResult result, FraudAssessment assessment) {
        if (result != null && assessment != null) {
            result.setAssessmentId(assessment.getId());
        }
        return result;
    }
}
