package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudEvidenceUploadRequest;
import com.ecommerce.app.module.fraud.exception.FraudNotFoundException;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.model.FraudEvidence;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.repository.FraudEvidenceRepository;
import com.ecommerce.app.module.fraud.services.FraudEvidenceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudEvidenceService implements FraudEvidenceService {

    private static final long MAX_EVIDENCE_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "webp", "txt", "csv");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "text/plain",
            "text/csv"
    );

    private final FraudEvidenceRepository fraudEvidenceRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudEventLogRepository fraudEventLogRepository;

    public DefaultFraudEvidenceService(FraudEvidenceRepository fraudEvidenceRepository,
            FraudCaseRepository fraudCaseRepository,
            FraudAssessmentRepository fraudAssessmentRepository,
            FraudEventLogRepository fraudEventLogRepository) {
        this.fraudEvidenceRepository = fraudEvidenceRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudEventLogRepository = fraudEventLogRepository;
    }

    @Override
    @Transactional
    public FraudEvidence addEvidence(FraudEvidenceUploadRequest request, String fileName, String filePath,
            String contentType, Long fileSize, String uploadedBy) {
        if (request == null || request.getCaseId() == null) {
            throw new FraudValidationException("Fraud case is required for evidence.");
        }
        validateEvidenceRequest(request, fileName, contentType, fileSize);
        FraudCase fraudCase = fraudCaseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new FraudNotFoundException("Fraud case not found."));
        FraudAssessment assessment = request.getAssessmentId() == null
                ? fraudCase.getAssessment()
                : fraudAssessmentRepository.findById(request.getAssessmentId())
                        .orElseThrow(() -> new FraudNotFoundException("Fraud assessment not found."));

        FraudEvidence evidence = new FraudEvidence();
        evidence.setFraudCase(fraudCase);
        evidence.setAssessment(assessment);
        evidence.setEvidenceType(request.getEvidenceType());
        evidence.setTitle(request.getTitle());
        evidence.setNote(request.getNote());
        evidence.setFileName(fileName);
        evidence.setFilePath(filePath);
        evidence.setContentType(contentType);
        evidence.setFileSize(fileSize);
        evidence.setUploadedBy(uploadedBy);
        FraudEvidence saved = fraudEvidenceRepository.save(evidence);
        recordEvidenceEvent(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudEvidence> findByCaseId(Long caseId) {
        return fraudEvidenceRepository.findByFraudCase_IdOrderByIdDesc(caseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudEvidence> findByAssessmentId(Long assessmentId) {
        return fraudEvidenceRepository.findByAssessment_IdOrderByIdDesc(assessmentId);
    }

    private void recordEvidenceEvent(FraudEvidence evidence) {
        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setEventType(FraudEventType.FRAUD_EVIDENCE_ADDED);
        eventLog.setAggregateType("FRAUD_EVIDENCE");
        eventLog.setAggregateId(evidence.getId());
        if (evidence.getAssessment() != null) {
            eventLog.setOrderId(evidence.getAssessment().getOrderId());
            eventLog.setCustomerId(evidence.getAssessment().getCustomerId());
            eventLog.setVendorId(evidence.getAssessment().getVendorId());
        }
        eventLog.setPayloadJson("{\"evidenceType\":\"" + safe(evidence.getEvidenceType()) + "\"}");
        eventLog.setEventTime(LocalDateTime.now());
        fraudEventLogRepository.save(eventLog);
    }

    private void validateEvidenceRequest(FraudEvidenceUploadRequest request, String fileName,
            String contentType, Long fileSize) {
        if (request.getEvidenceType() == null || request.getEvidenceType().isBlank()) {
            throw new FraudValidationException("Evidence type is required.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new FraudValidationException("Evidence title is required.");
        }
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        String safeFileName = fileName.trim();
        if (safeFileName.contains("/") || safeFileName.contains("\\") || safeFileName.contains("..")) {
            throw new FraudValidationException("Evidence file name is not safe.");
        }
        int extensionIndex = safeFileName.lastIndexOf('.');
        if (extensionIndex < 1 || extensionIndex == safeFileName.length() - 1) {
            throw new FraudValidationException("Evidence file extension is required.");
        }
        String extension = safeFileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FraudValidationException("Evidence file extension is not allowed.");
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new FraudValidationException("Evidence content type is not allowed.");
        }
        if (fileSize == null || fileSize <= 0 || fileSize > MAX_EVIDENCE_FILE_SIZE) {
            throw new FraudValidationException("Evidence file size must be between 1 byte and 10 MB.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
