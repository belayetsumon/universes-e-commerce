package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodRiskProfileRepository extends JpaRepository<CodRiskProfile, Long> {

    Optional<CodRiskProfile> findByCustomerId(Long customerId);

    Optional<CodRiskProfile> findByVendorId(Long vendorId);

    Optional<CodRiskProfile> findByMobileHash(String mobileHash);

    Optional<CodRiskProfile> findByAddressHash(String addressHash);

    Optional<CodRiskProfile> findByDeviceIdentifier(String deviceIdentifier);

    Optional<CodRiskProfile> findByDistrictIgnoreCase(String district);

    boolean existsByMobileHashAndCodDisabledTrue(String mobileHash);

    boolean existsByAddressHashAndCodDisabledTrue(String addressHash);

    boolean existsByDeviceIdentifierAndCodDisabledTrue(String deviceIdentifier);

    boolean existsByDistrictIgnoreCaseAndCodDisabledTrue(String district);

    boolean existsByCustomerIdAndCodDisabledTrue(Long customerId);

    boolean existsByVendorIdAndCodDisabledTrue(Long vendorId);
}
