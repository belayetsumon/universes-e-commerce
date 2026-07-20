package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudEvidenceUploadRequest;
import com.ecommerce.app.module.fraud.model.FraudEvidence;
import java.util.List;

public interface FraudEvidenceService {

    FraudEvidence addEvidence(FraudEvidenceUploadRequest request, String fileName, String filePath,
            String contentType, Long fileSize, String uploadedBy);

    List<FraudEvidence> findByCaseId(Long caseId);

    List<FraudEvidence> findByAssessmentId(Long assessmentId);
}
