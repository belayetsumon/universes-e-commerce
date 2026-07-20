package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.TrustedDevice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByCustomerIdAndDeviceIdentifierAndActiveTrue(Long customerId, String deviceIdentifier);

    boolean existsByCustomerIdAndDeviceIdentifierAndActiveTrue(Long customerId, String deviceIdentifier);
}
