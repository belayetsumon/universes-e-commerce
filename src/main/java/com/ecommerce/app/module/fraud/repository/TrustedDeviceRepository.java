package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.TrustedDevice;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByCustomerIdAndDeviceIdentifierAndActiveTrue(Long customerId, String deviceIdentifier);

    @Query("""
            select case when count(device) > 0 then true else false end
            from TrustedDevice device
            where device.customerId = :customerId
              and device.deviceIdentifier = :deviceIdentifier
              and device.active = true
              and (device.expiresAt is null or device.expiresAt > :now)
            """)
    boolean existsEffectiveTrustedDevice(
            @Param("customerId") Long customerId,
            @Param("deviceIdentifier") String deviceIdentifier,
            @Param("now") LocalDateTime now
    );
}
