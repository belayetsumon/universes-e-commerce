package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingZone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShippingZoneRepository extends JpaRepository<ShippingZone, Long> {

    Optional<ShippingZone> findByUuid(String uuid);

    Optional<ShippingZone> findByCode(String code);

    List<ShippingZone> findByActiveTrueOrderByPriorityAscNameAsc();

    @Query("select distinct z from ShippingZone z join z.coverageLocations l where z.active = true and l = :location order by z.priority asc, z.name asc")
    List<ShippingZone> findActiveByLocation(@Param("location") ShippingLocation location);
}
