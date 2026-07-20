package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudReviewHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudReviewHistoryRepository extends JpaRepository<FraudReviewHistory, Long> {

    List<FraudReviewHistory> findByAssessment_IdOrderByReviewedAtDesc(Long assessmentId);

    List<FraudReviewHistory> findByReviewedByOrderByReviewedAtDesc(String reviewedBy);
}
