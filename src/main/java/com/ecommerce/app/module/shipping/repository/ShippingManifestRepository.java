package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShippingManifest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingManifestRepository extends JpaRepository<ShippingManifest, Long> {

    List<ShippingManifest> findDistinctByShipmentsVendorIdOrderByIdDesc(Long vendorId);
}
