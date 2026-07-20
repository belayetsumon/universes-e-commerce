package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long>, JpaSpecificationExecutor<FraudCase> {

    Optional<FraudCase> findByUuid(String uuid);

    Optional<FraudCase> findByCaseNumber(String caseNumber);

    List<FraudCase> findByCaseStatusInOrderByIdDesc(Collection<FraudCaseStatus> statuses);

    Page<FraudCase> findByCaseStatus(FraudCaseStatus status, Pageable pageable);

    long countByCaseStatus(FraudCaseStatus status);

    boolean existsByOrderIdAndCaseStatusIn(Long orderId, Collection<FraudCaseStatus> statuses);

    boolean existsByVendorIdAndCaseStatusIn(Long vendorId, Collection<FraudCaseStatus> statuses);

    Optional<FraudCase> findFirstByOrderIdAndCaseStatusInOrderByIdDesc(Long orderId, Collection<FraudCaseStatus> statuses);

    Optional<FraudCase> findFirstByVendorIdAndCaseStatusInOrderByIdDesc(Long vendorId, Collection<FraudCaseStatus> statuses);
}
