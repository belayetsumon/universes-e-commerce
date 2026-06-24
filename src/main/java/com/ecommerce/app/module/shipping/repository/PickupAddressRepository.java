package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.PickupAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickupAddressRepository extends JpaRepository<PickupAddress, Long> {

    List<PickupAddress> findByVendorIdOrderByDefaultAddressDescIdDesc(Long vendorId);

    Optional<PickupAddress> findFirstByVendorIdAndDefaultAddressTrueAndActiveTrueOrderByIdDesc(Long vendorId);
}
