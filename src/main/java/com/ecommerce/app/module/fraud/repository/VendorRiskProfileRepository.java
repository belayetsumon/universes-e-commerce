package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.VendorRiskProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VendorRiskProfileRepository extends JpaRepository<VendorRiskProfile, Long>, JpaSpecificationExecutor<VendorRiskProfile> {

    Optional<VendorRiskProfile> findByVendorId(Long vendorId);

    boolean existsByVendorIdAndUnderReviewTrue(Long vendorId);

    boolean existsByVendorIdAndPayoutHeldTrue(Long vendorId);
}
