package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingLocationType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingLocationRepository extends JpaRepository<ShippingLocation, Long> {

    List<ShippingLocation> findByActiveTrueOrderByTypeAscPriorityAscNameAsc();

    List<ShippingLocation> findByParentIdOrderByPriorityAscNameAsc(Long parentId);

    List<ShippingLocation> findByIdIn(Collection<Long> ids);

    Optional<ShippingLocation> findFirstByTypeAndCodeIgnoreCase(ShippingLocationType type, String code);
}
