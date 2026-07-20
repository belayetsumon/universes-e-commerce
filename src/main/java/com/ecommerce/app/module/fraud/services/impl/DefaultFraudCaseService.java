package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudCaseAssignRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseResolveRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseResponse;
import com.ecommerce.app.module.fraud.exception.FraudNotFoundException;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudCasePriority;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudCaseService implements FraudCaseService {

    private static final Set<FraudCaseStatus> OPEN_STATUSES = Set.of(
            FraudCaseStatus.OPEN,
            FraudCaseStatus.ASSIGNED,
            FraudCaseStatus.IN_REVIEW,
            FraudCaseStatus.ESCALATED
    );

    private final FraudCaseRepository fraudCaseRepository;
    private final FraudEventLogRepository fraudEventLogRepository;
    private final FraudEventPublisher fraudEventPublisher;

    public DefaultFraudCaseService(FraudCaseRepository fraudCaseRepository,
            FraudEventLogRepository fraudEventLogRepository,
            FraudEventPublisher fraudEventPublisher) {
        this.fraudCaseRepository = fraudCaseRepository;
        this.fraudEventLogRepository = fraudEventLogRepository;
        this.fraudEventPublisher = fraudEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudCaseResponse findById(Long id) {
        return toResponse(findCase(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> findOpenCases() {
        return fraudCaseRepository.findByCaseStatusInOrderByIdDesc(OPEN_STATUSES).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FraudCaseResponse assign(Long caseId, FraudCaseAssignRequest request) {
        if (request == null || request.getInvestigator() == null || request.getInvestigator().isBlank()) {
            throw new FraudValidationException("Investigator is required.");
        }
        FraudCase fraudCase = findCase(caseId);
        fraudCase.setAssignedInvestigator(request.getInvestigator().trim());
        fraudCase.setCaseStatus(FraudCaseStatus.ASSIGNED);
        if (request.getNote() != null && !request.getNote().isBlank()) {
            fraudCase.setInvestigationNotes(appendNote(fraudCase.getInvestigationNotes(), request.getNote()));
        }
        FraudCase saved = fraudCaseRepository.save(fraudCase);
        recordCaseEvent(saved, FraudEventType.FRAUD_CASE_ASSIGNED);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public FraudCaseResponse resolve(Long caseId, FraudCaseResolveRequest request) {
        if (request == null || request.getResolution() == null || request.getResolution().isBlank()
                || request.getResolutionReason() == null || request.getResolutionReason().isBlank()) {
            throw new FraudValidationException("Resolution and resolution reason are required.");
        }
        FraudCase fraudCase = findCase(caseId);
        fraudCase.setCaseStatus(FraudCaseStatus.RESOLVED);
        fraudCase.setResolution(request.getResolution().trim());
        fraudCase.setResolutionReason(request.getResolutionReason().trim());
        fraudCase.setResolvedAt(LocalDateTime.now());
        FraudCase saved = fraudCaseRepository.save(fraudCase);
        recordCaseEvent(saved, FraudEventType.FRAUD_CASE_RESOLVED);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public FraudCaseResponse openForAssessment(FraudAssessment assessment, String reason) {
        if (assessment == null || assessment.getId() == null) {
            throw new FraudValidationException("Fraud assessment is required to open a case.");
        }
        FraudCase fraudCase = new FraudCase();
        fraudCase.setCaseNumber(generateCaseNumber(assessment));
        fraudCase.setAssessment(assessment);
        fraudCase.setOrderId(assessment.getOrderId());
        fraudCase.setCustomerId(assessment.getCustomerId());
        fraudCase.setVendorId(assessment.getVendorId());
        fraudCase.setCaseStatus(FraudCaseStatus.OPEN);
        fraudCase.setPriority(resolvePriority(assessment));
        fraudCase.setCaseReason(reason);
        fraudCase.setOpenedAt(LocalDateTime.now());
        FraudCase saved = fraudCaseRepository.save(fraudCase);
        recordCaseEvent(saved, FraudEventType.FRAUD_CASE_OPENED);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenCaseForOrder(Long orderId) {
        return orderId != null && fraudCaseRepository.existsByOrderIdAndCaseStatusIn(orderId, OPEN_STATUSES);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenCaseForVendor(Long vendorId) {
        return vendorId != null && fraudCaseRepository.existsByVendorIdAndCaseStatusIn(vendorId, OPEN_STATUSES);
    }

    private FraudCase findCase(Long id) {
        return fraudCaseRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud case not found."));
    }

    private FraudCasePriority resolvePriority(FraudAssessment assessment) {
        return switch (assessment.getRiskLevel()) {
            case LOW -> FraudCasePriority.LOW;
            case MEDIUM -> FraudCasePriority.MEDIUM;
            case HIGH -> FraudCasePriority.HIGH;
            case CRITICAL -> FraudCasePriority.CRITICAL;
        };
    }

    private String generateCaseNumber(FraudAssessment assessment) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        return "FRD-" + timestamp + "-" + assessment.getId();
    }

    private String appendNote(String existing, String note) {
        String safeExisting = existing == null || existing.isBlank() ? "" : existing + System.lineSeparator();
        return safeExisting + LocalDateTime.now() + " - " + note.trim();
    }

    private void recordCaseEvent(FraudCase fraudCase, FraudEventType eventType) {
        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setEventType(eventType);
        eventLog.setAggregateType("FRAUD_CASE");
        eventLog.setAggregateId(fraudCase.getId());
        eventLog.setOrderId(fraudCase.getOrderId());
        eventLog.setCustomerId(fraudCase.getCustomerId());
        eventLog.setVendorId(fraudCase.getVendorId());
        eventLog.setPayloadJson("{\"caseNumber\":\"" + json(fraudCase.getCaseNumber())
                + "\",\"assignedInvestigator\":\"" + json(fraudCase.getAssignedInvestigator()) + "\"}");
        eventLog.setEventTime(LocalDateTime.now());
        fraudEventLogRepository.save(eventLog);
        fraudEventPublisher.publish(eventLog.getEventType(), eventLog.getAggregateType(), eventLog.getAggregateId(),
                withCommonPayload(eventLog), eventLog.getCorrelationId(), eventLog.getIdempotencyKey());
    }

    private String withCommonPayload(FraudEventLog eventLog) {
        String payload = eventLog.getPayloadJson() == null || eventLog.getPayloadJson().isBlank()
                ? "{}"
                : eventLog.getPayloadJson().trim();
        if (!payload.endsWith("}")) {
            return payload;
        }
        StringBuilder builder = new StringBuilder(payload);
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

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private FraudCaseResponse toResponse(FraudCase fraudCase) {
        FraudCaseResponse response = new FraudCaseResponse();
        response.setId(fraudCase.getId());
        response.setCaseNumber(fraudCase.getCaseNumber());
        response.setOrderId(fraudCase.getOrderId());
        response.setCustomerId(fraudCase.getCustomerId());
        response.setVendorId(fraudCase.getVendorId());
        response.setAssessmentId(fraudCase.getAssessment() == null ? null : fraudCase.getAssessment().getId());
        response.setStatus(fraudCase.getCaseStatus());
        response.setPriority(fraudCase.getPriority());
        response.setAssignedInvestigator(fraudCase.getAssignedInvestigator());
        response.setCaseReason(fraudCase.getCaseReason());
        response.setResolution(fraudCase.getResolution());
        response.setOpenedAt(fraudCase.getOpenedAt());
        response.setResolvedAt(fraudCase.getResolvedAt());
        return response;
    }
}
