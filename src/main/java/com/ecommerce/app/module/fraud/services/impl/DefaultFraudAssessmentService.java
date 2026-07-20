package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentCreateRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentReviewRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentSearchFilter;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudDecisionResult;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.exception.FraudNotFoundException;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.model.FraudReviewHistory;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.FraudSignal;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.repository.FraudReviewHistoryRepository;
import com.ecommerce.app.module.fraud.repository.FraudSignalRepository;
import com.ecommerce.app.module.fraud.services.FraudAssessmentService;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.fraud.services.FraudDecisionService;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import com.ecommerce.app.module.fraud.services.FraudIdempotencyService;
import com.ecommerce.app.module.fraud.services.FraudRiskScoringService;
import com.ecommerce.app.module.fraud.services.FraudRuleEngine;
import com.ecommerce.app.module.fraud.services.FraudRuleExecutionLogService;
import com.ecommerce.app.module.fraud.services.FraudSignalCollector;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudAssessmentService implements FraudAssessmentService {

    private static final String EVALUATION_VERSION = "fraud-v1";

    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudSignalRepository fraudSignalRepository;
    private final FraudReviewHistoryRepository fraudReviewHistoryRepository;
    private final FraudEventLogRepository fraudEventLogRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final FraudSignalCollector fraudSignalCollector;
    private final FraudRuleEngine fraudRuleEngine;
    private final FraudRiskScoringService fraudRiskScoringService;
    private final FraudDecisionService fraudDecisionService;
    private final FraudRuleExecutionLogService fraudRuleExecutionLogService;
    private final FraudCaseService fraudCaseService;
    private final FraudAuditService fraudAuditService;
    private final FraudEventPublisher fraudEventPublisher;
    private final FraudIdempotencyService fraudIdempotencyService;

    public DefaultFraudAssessmentService(FraudAssessmentRepository fraudAssessmentRepository,
            FraudSignalRepository fraudSignalRepository,
            FraudReviewHistoryRepository fraudReviewHistoryRepository,
            FraudEventLogRepository fraudEventLogRepository,
            SalesOrderRepository salesOrderRepository,
            FraudSignalCollector fraudSignalCollector,
            FraudRuleEngine fraudRuleEngine,
            FraudRiskScoringService fraudRiskScoringService,
            FraudDecisionService fraudDecisionService,
            FraudRuleExecutionLogService fraudRuleExecutionLogService,
            FraudCaseService fraudCaseService,
            FraudAuditService fraudAuditService,
            FraudEventPublisher fraudEventPublisher,
            FraudIdempotencyService fraudIdempotencyService) {
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudSignalRepository = fraudSignalRepository;
        this.fraudReviewHistoryRepository = fraudReviewHistoryRepository;
        this.fraudEventLogRepository = fraudEventLogRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.fraudSignalCollector = fraudSignalCollector;
        this.fraudRuleEngine = fraudRuleEngine;
        this.fraudRiskScoringService = fraudRiskScoringService;
        this.fraudDecisionService = fraudDecisionService;
        this.fraudRuleExecutionLogService = fraudRuleExecutionLogService;
        this.fraudCaseService = fraudCaseService;
        this.fraudAuditService = fraudAuditService;
        this.fraudEventPublisher = fraudEventPublisher;
        this.fraudIdempotencyService = fraudIdempotencyService;
    }

    @Override
    @Transactional
    public FraudAssessmentResponse evaluate(FraudAssessmentCreateRequest request) {
        if (request == null || request.getOrderId() == null) {
            throw new FraudValidationException("Order ID is required for fraud assessment.");
        }
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<FraudAssessment> existing = fraudAssessmentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
            fraudIdempotencyService.start("FRAUD_ASSESSMENT_EVALUATE", idempotencyKey,
                    fraudIdempotencyService.hashPayload("orderId=" + request.getOrderId()
                            + "|correlationId=" + safe(request.getCorrelationId())));
        }

        SalesOrder order = salesOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new FraudNotFoundException("Order not found for fraud assessment."));
        FraudContext context = prepareContext(request, order);

        List<FraudSignalResult> collectedSignals = fraudSignalCollector.collect(order, context);
        Optional<FraudDecisionResult> hardDecision = fraudRuleEngine.evaluateHardRules(collectedSignals, context);
        FraudAssessmentResponse response;
        if (hardDecision.isPresent()) {
            response = persistAndApply(order, collectedSignals, hardDecision.get(), context);
            completeAssessmentIdempotency(idempotencyKey, response);
            return response;
        }

        List<FraudSignalResult> adjustedSignals = fraudRuleEngine.applyScoringRules(collectedSignals, context);
        int score = fraudRiskScoringService.calculate(adjustedSignals, context);
        FraudDecisionResult decision = fraudDecisionService.decide(order, score, adjustedSignals, context);
        response = persistAndApply(order, adjustedSignals, decision, context);
        completeAssessmentIdempotency(idempotencyKey, response);
        return response;
    }

    @Override
    @Transactional
    public FraudAssessmentResponse persistAndApply(SalesOrder order, List<FraudSignalResult> signals,
            FraudDecisionResult decision, FraudContext context) {
        if (order == null || order.getId() == null) {
            throw new FraudValidationException("Order is required for fraud assessment persistence.");
        }
        FraudDecisionResult safeDecision = normalizeDecision(decision);
        FraudContext safeContext = context == null ? new FraudContext() : context;

        FraudAssessment assessment = new FraudAssessment();
        assessment.setOrderId(order.getId());
        assessment.setOrderUuid(order.getUuid());
        assessment.setCustomerId(order.getCustomer() == null ? null : order.getCustomer().getId());
        assessment.setVendorId(order.getVendorId());
        assessment.setRiskScore(safeDecision.getRiskScore());
        assessment.setRiskLevel(safeDecision.getRiskLevel());
        assessment.setDecision(safeDecision.getDecision());
        assessment.setStatus(safeDecision.getStatus());
        assessment.setDecisionReason(safeDecision.getDecisionReason());
        assessment.setEvaluationSource(com.ecommerce.app.module.fraud.model.FraudEvaluationSource.ORDER_CREATED);
        assessment.setEvaluationVersion(EVALUATION_VERSION);
        assessment.setAutomaticDecision(safeDecision.isAutomaticDecision());
        assessment.setManualReviewRequired(safeDecision.isManualReviewRequired());
        assessment.setEvaluatedAt(LocalDateTime.now());
        assessment.setCorrelationId(safeContext.getMetadata().get("correlationId") == null ? null : String.valueOf(safeContext.getMetadata().get("correlationId")));
        assessment.setIdempotencyKey(safeContext.getMetadata().get("idempotencyKey") == null ? null : String.valueOf(safeContext.getMetadata().get("idempotencyKey")));
        FraudAssessment saved = fraudAssessmentRepository.save(assessment);

        saveSignals(saved, signals);
        fraudRuleExecutionLogService.recordExecutions(saved, signals, safeContext);
        recordAssessmentEvent(saved);
        openCaseIfRequired(saved);
        fraudAuditService.record("FRAUD_ASSESSMENT", saved.getId(), safeDecision.getAction(), saved.getDecisionReason(), "{}");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAssessmentResponse findById(Long id) {
        return toResponse(findAssessment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAssessmentResponse findLatestByOrderId(Long orderId) {
        return fraudAssessmentRepository.findTopByOrderIdOrderByIdDesc(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new FraudNotFoundException("Fraud assessment not found for order."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudAssessmentResponse> search(FraudAssessmentSearchFilter filter) {
        FraudAssessmentSearchFilter safeFilter = filter == null ? new FraudAssessmentSearchFilter() : filter;
        return fraudAssessmentRepository.findAll().stream()
                .filter(assessment -> safeFilter.getOrderId() == null || Objects.equals(assessment.getOrderId(), safeFilter.getOrderId()))
                .filter(assessment -> safeFilter.getCustomerId() == null || Objects.equals(assessment.getCustomerId(), safeFilter.getCustomerId()))
                .filter(assessment -> safeFilter.getVendorId() == null || Objects.equals(assessment.getVendorId(), safeFilter.getVendorId()))
                .filter(assessment -> safeFilter.getRiskLevel() == null || assessment.getRiskLevel() == safeFilter.getRiskLevel())
                .filter(assessment -> safeFilter.getDecision() == null || assessment.getDecision() == safeFilter.getDecision())
                .filter(assessment -> safeFilter.getStatus() == null || assessment.getStatus() == safeFilter.getStatus())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FraudAssessmentResponse review(Long assessmentId, FraudAssessmentReviewRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new FraudValidationException("Review reason is required.");
        }
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            Optional<FraudAssessmentResponse> completed = fraudIdempotencyService
                    .findCompleted("FRAUD_ASSESSMENT_REVIEW", idempotencyKey)
                    .map(record -> extractLong(record.getResponseJson(), "assessmentId"))
                    .filter(Objects::nonNull)
                    .map(this::findAssessment)
                    .map(this::toResponse);
            if (completed.isPresent()) {
                return completed.get();
            }
            fraudIdempotencyService.start("FRAUD_ASSESSMENT_REVIEW", idempotencyKey,
                    fraudIdempotencyService.hashPayload("assessmentId=" + assessmentId
                            + "|decision=" + request.getDecision()
                            + "|action=" + request.getAction()));
        }
        FraudAssessment assessment = findAssessment(assessmentId);
        FraudDecision requestedDecision = request.getDecision() == null ? FraudDecision.MANUAL_REVIEW : request.getDecision();
        FraudAction requestedAction = request.getAction() == null ? toAction(requestedDecision) : request.getAction();
        FraudDecision previousDecision = assessment.getDecision();
        assessment.setDecision(requestedDecision);
        assessment.setStatus(toStatus(requestedDecision));
        assessment.setReviewedBy("fraud-review");
        assessment.setReviewedAt(LocalDateTime.now());
        assessment.setReviewNotes(request.getNotes());
        assessment.setDecisionReason(request.getReason());
        assessment.setManualReviewRequired(false);
        FraudAssessment saved = fraudAssessmentRepository.save(assessment);

        FraudReviewHistory history = new FraudReviewHistory();
        history.setAssessment(saved);
        history.setPreviousDecision(previousDecision);
        history.setNewDecision(requestedDecision);
        history.setAction(requestedAction);
        history.setReason(request.getReason());
        history.setNotes(request.getNotes());
        history.setReviewedBy("fraud-review");
        history.setReviewedAt(LocalDateTime.now());
        fraudReviewHistoryRepository.save(history);

        recordReviewEvent(saved, requestedDecision, requestedAction);
        if (shouldOpenCase(saved) && !fraudCaseService.hasOpenCaseForOrder(saved.getOrderId())) {
            fraudCaseService.openForAssessment(saved, request.getReason());
        }
        fraudAuditService.record("FRAUD_ASSESSMENT", saved.getId(), requestedAction, request.getReason(), "{}");
        FraudAssessmentResponse response = toResponse(saved);
        if (idempotencyKey != null) {
            fraudIdempotencyService.complete("FRAUD_ASSESSMENT_REVIEW", idempotencyKey, responseJson(response));
        }
        return response;
    }

    private FraudContext prepareContext(FraudAssessmentCreateRequest request, SalesOrder order) {
        FraudContext context = request.getContext() == null ? new FraudContext() : request.getContext();
        context.setVendorId(context.getVendorId() == null ? order.getVendorId() : context.getVendorId());
        context.setOrderValue(context.getOrderValue() == null ? order.getGrandTotal() : context.getOrderValue());
        context.getMetadata().putIfAbsent("correlationId", request.getCorrelationId());
        context.getMetadata().putIfAbsent("idempotencyKey", request.getIdempotencyKey());
        return context;
    }

    private void saveSignals(FraudAssessment assessment, List<FraudSignalResult> signals) {
        if (signals == null || signals.isEmpty()) {
            return;
        }
        for (FraudSignalResult result : signals) {
            FraudSignal signal = new FraudSignal();
            signal.setAssessment(assessment);
            signal.setSignalCode(result.getSignalCode());
            signal.setSignalCategory(result.getCategory());
            signal.setSignalValue(result.getSignalValue());
            signal.setScoreImpact(result.getScoreImpact());
            signal.setTriggered(result.isTriggered());
            signal.setSeverity(result.getSeverity());
            signal.setSource(result.getSource());
            signal.setReasonCode(result.getReasonCode());
            signal.setDetectedAt(result.getDetectedAt());
            signal.setMetadataJson(result.getMetadataJson());
            fraudSignalRepository.save(signal);
        }
    }

    private void openCaseIfRequired(FraudAssessment assessment) {
        if (shouldOpenCase(assessment) && !fraudCaseService.hasOpenCaseForOrder(assessment.getOrderId())) {
            fraudCaseService.openForAssessment(assessment, assessment.getDecisionReason());
        }
    }

    private boolean shouldOpenCase(FraudAssessment assessment) {
        return assessment.getStatus() == FraudAssessmentStatus.MANUAL_REVIEW
                || assessment.getStatus() == FraudAssessmentStatus.FRAUD_HOLD
                || assessment.getStatus() == FraudAssessmentStatus.FRAUD_REJECTED
                || assessment.isManualReviewRequired();
    }

    private FraudAssessmentStatus toStatus(FraudDecision decision) {
        if (decision == null) {
            return FraudAssessmentStatus.MANUAL_REVIEW;
        }
        return switch (decision) {
            case APPROVE -> FraudAssessmentStatus.APPROVED;
            case VERIFY, REQUIRE_OTP, REQUIRE_PREPAID, REQUIRE_PARTIAL_PREPAYMENT -> FraudAssessmentStatus.VERIFICATION_REQUIRED;
            case MANUAL_REVIEW -> FraudAssessmentStatus.MANUAL_REVIEW;
            case HOLD, DISABLE_COD, HOLD_REFUND, HOLD_REWARD, HOLD_VENDOR_PAYOUT -> FraudAssessmentStatus.FRAUD_HOLD;
            case REJECT, BLOCK, CANCEL -> FraudAssessmentStatus.FRAUD_REJECTED;
        };
    }

    private FraudAction toAction(FraudDecision decision) {
        if (decision == null) {
            return FraudAction.MANUAL_REVIEW;
        }
        return switch (decision) {
            case APPROVE -> FraudAction.APPROVE;
            case VERIFY -> FraudAction.VERIFY;
            case REQUIRE_OTP -> FraudAction.REQUIRE_OTP;
            case REQUIRE_PREPAID -> FraudAction.REQUIRE_PREPAID;
            case REQUIRE_PARTIAL_PREPAYMENT -> FraudAction.REQUIRE_PARTIAL_PREPAYMENT;
            case MANUAL_REVIEW -> FraudAction.MANUAL_REVIEW;
            case HOLD -> FraudAction.HOLD;
            case DISABLE_COD -> FraudAction.DISABLE_COD;
            case HOLD_REFUND -> FraudAction.HOLD_REFUND;
            case HOLD_REWARD -> FraudAction.HOLD_REWARD;
            case HOLD_VENDOR_PAYOUT -> FraudAction.HOLD_VENDOR_PAYOUT;
            case REJECT -> FraudAction.REJECT;
            case BLOCK -> FraudAction.BLOCK;
            case CANCEL -> FraudAction.CANCEL;
        };
    }

    private FraudDecisionResult normalizeDecision(FraudDecisionResult decision) {
        FraudDecisionResult normalized = decision == null ? new FraudDecisionResult() : decision;
        if (normalized.getRiskLevel() == null) {
            normalized.setRiskLevel(FraudRiskLevel.LOW);
        }
        if (normalized.getDecision() == null) {
            normalized.setDecision(FraudDecision.MANUAL_REVIEW);
        }
        if (normalized.getStatus() == null) {
            normalized.setStatus(toStatus(normalized.getDecision()));
        }
        if (normalized.getAction() == null) {
            normalized.setAction(toAction(normalized.getDecision()));
        }
        if (normalized.getDecisionReason() == null || normalized.getDecisionReason().isBlank()) {
            normalized.setDecisionReason("Fraud assessment completed.");
        }
        return normalized;
    }

    private FraudAssessment findAssessment(Long id) {
        return fraudAssessmentRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud assessment not found."));
    }

    private void recordAssessmentEvent(FraudAssessment assessment) {
        FraudEventLog eventLog = baseAssessmentEvent(assessment);
        eventLog.setEventType(toEventType(assessment));
        eventLog.setPayloadJson("{\"decision\":\"" + assessment.getDecision() + "\",\"riskScore\":" + assessment.getRiskScore() + "}");
        fraudEventLogRepository.save(eventLog);
        publishEvent(eventLog);
    }

    private void recordReviewEvent(FraudAssessment assessment, FraudDecision decision, FraudAction action) {
        FraudEventLog eventLog = baseAssessmentEvent(assessment);
        eventLog.setEventType(FraudEventType.FRAUD_REVIEW_COMPLETED);
        eventLog.setPayloadJson("{\"decision\":\"" + decision + "\",\"action\":\"" + action + "\"}");
        fraudEventLogRepository.save(eventLog);
        publishEvent(eventLog);
    }

    private FraudEventLog baseAssessmentEvent(FraudAssessment assessment) {
        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setAggregateType("FRAUD_ASSESSMENT");
        eventLog.setAggregateId(assessment.getId());
        eventLog.setOrderId(assessment.getOrderId());
        eventLog.setCustomerId(assessment.getCustomerId());
        eventLog.setVendorId(assessment.getVendorId());
        eventLog.setCorrelationId(assessment.getCorrelationId());
        eventLog.setIdempotencyKey(assessment.getIdempotencyKey());
        eventLog.setEventTime(LocalDateTime.now());
        return eventLog;
    }

    private FraudEventType toEventType(FraudAssessment assessment) {
        return switch (assessment.getStatus()) {
            case APPROVED -> FraudEventType.ORDER_FRAUD_APPROVED;
            case VERIFICATION_REQUIRED -> FraudEventType.ORDER_FRAUD_VERIFICATION_REQUIRED;
            case MANUAL_REVIEW, FRAUD_HOLD -> FraudEventType.ORDER_FRAUD_HELD;
            case FRAUD_REJECTED -> FraudEventType.ORDER_FRAUD_REJECTED;
            default -> FraudEventType.ORDER_FRAUD_ASSESSMENT_COMPLETED;
        };
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

    private void completeAssessmentIdempotency(String idempotencyKey, FraudAssessmentResponse response) {
        if (idempotencyKey != null) {
            fraudIdempotencyService.complete("FRAUD_ASSESSMENT_EVALUATE", idempotencyKey, responseJson(response));
        }
    }

    private String responseJson(FraudAssessmentResponse response) {
        if (response == null) {
            return "{}";
        }
        return "{\"assessmentId\":" + response.getId()
                + ",\"orderId\":" + response.getOrderId()
                + ",\"decision\":\"" + response.getDecision()
                + "\",\"status\":\"" + response.getStatus() + "\"}";
    }

    private Long extractLong(String json, String fieldName) {
        if (json == null || fieldName == null) {
            return null;
        }
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            return null;
        }
        int valueStart = start + needle.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        if (valueEnd == valueStart) {
            return null;
        }
        try {
            return Long.valueOf(json.substring(valueStart, valueEnd));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        String cleaned = idempotencyKey.trim();
        return cleaned.length() <= 160 ? cleaned : cleaned.substring(0, 160);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private FraudAssessmentResponse toResponse(FraudAssessment assessment) {
        FraudAssessmentResponse response = new FraudAssessmentResponse();
        response.setId(assessment.getId());
        response.setUuid(assessment.getUuid());
        response.setOrderId(assessment.getOrderId());
        response.setOrderUuid(assessment.getOrderUuid());
        response.setCustomerId(assessment.getCustomerId());
        response.setVendorId(assessment.getVendorId());
        response.setRiskScore(assessment.getRiskScore());
        response.setRiskLevel(assessment.getRiskLevel());
        response.setDecision(assessment.getDecision());
        response.setStatus(assessment.getStatus());
        response.setDecisionReason(assessment.getDecisionReason());
        response.setAutomaticDecision(assessment.isAutomaticDecision());
        response.setManualReviewRequired(assessment.isManualReviewRequired());
        response.setEvaluatedAt(assessment.getEvaluatedAt());
        response.setSignals(fraudSignalRepository.findByAssessment_IdOrderByIdAsc(assessment.getId()).stream()
                .map(this::toSignalResult)
                .toList());
        return response;
    }

    private FraudSignalResult toSignalResult(FraudSignal signal) {
        FraudSignalResult result = new FraudSignalResult();
        result.setSignalCode(signal.getSignalCode());
        result.setCategory(signal.getSignalCategory());
        result.setSignalValue(signal.getSignalValue());
        result.setScoreImpact(signal.getScoreImpact());
        result.setTriggered(signal.isTriggered());
        result.setSeverity(signal.getSeverity());
        result.setSource(signal.getSource());
        result.setReasonCode(signal.getReasonCode());
        result.setDetectedAt(signal.getDetectedAt());
        result.setMetadataJson(signal.getMetadataJson());
        return result;
    }
}
