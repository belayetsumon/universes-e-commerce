package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.DeviceIdentity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceIdentityRepository extends JpaRepository<DeviceIdentity, Long> {

    Optional<DeviceIdentity> findByDeviceIdentifier(String deviceIdentifier);

    List<DeviceIdentity> findByCustomerId(Long customerId);

    long countDistinctByDeviceIdentifierAndCustomerIdIsNotNull(String deviceIdentifier);

    long countByIpAddress(String ipAddress);

    boolean existsByDeviceIdentifierAndBlacklistedTrue(String deviceIdentifier);
}
