package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.DeviceIdentity;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceIdentityRepository extends JpaRepository<DeviceIdentity, Long> {

    Optional<DeviceIdentity> findFirstByDeviceIdentifierOrderByIdAsc(String deviceIdentifier);

    Optional<DeviceIdentity> findByIdentityKey(String identityKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select identity from DeviceIdentity identity where identity.identityKey = :identityKey")
    Optional<DeviceIdentity> findByIdentityKeyForUpdate(@Param("identityKey") String identityKey);

    List<DeviceIdentity> findByCustomerId(Long customerId);

    @Query("""
            select count(distinct identity.customerId)
            from DeviceIdentity identity
            where identity.deviceIdentifier = :deviceIdentifier
              and identity.customerId is not null
            """)
    long countDistinctCustomersByDeviceIdentifier(@Param("deviceIdentifier") String deviceIdentifier);

    long countByIpAddress(String ipAddress);

    boolean existsByDeviceIdentifierAndBlacklistedTrue(String deviceIdentifier);
}
