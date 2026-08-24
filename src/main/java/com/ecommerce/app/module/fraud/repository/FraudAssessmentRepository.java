package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessment, Long>, JpaSpecificationExecutor<FraudAssessment> {

    Optional<FraudAssessment> findByUuid(String uuid);

    Optional<FraudAssessment> findTopByOrderIdOrderByIdDesc(Long orderId);

    Optional<FraudAssessment> findByIdempotencyKey(String idempotencyKey);

    Page<FraudAssessment> findByStatus(FraudAssessmentStatus status, Pageable pageable);

    long countByStatus(FraudAssessmentStatus status);

    long countByRiskLevel(FraudRiskLevel riskLevel);

    long countByDecision(FraudDecision decision);

    boolean existsByOrderIdAndStatusIn(Long orderId, Collection<FraudAssessmentStatus> statuses);

    @Query("""
            select count(assessment)
            from FraudAssessment assessment
            where assessment.orderId in (
                select salesOrder.id
                from SalesOrder salesOrder
                where salesOrder.orderGroup.id = :orderGroupId
            )
              and assessment.status in :statuses
            """)
    long countByOrderGroupIdAndStatusIn(
            @Param("orderGroupId") Long orderGroupId,
            @Param("statuses") Collection<FraudAssessmentStatus> statuses
    );
}
