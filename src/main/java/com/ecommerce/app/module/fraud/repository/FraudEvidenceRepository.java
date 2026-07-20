package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudEvidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudEvidenceRepository extends JpaRepository<FraudEvidence, Long> {

    List<FraudEvidence> findByFraudCase_IdOrderByIdDesc(Long caseId);

    List<FraudEvidence> findByAssessment_IdOrderByIdDesc(Long assessmentId);
}
