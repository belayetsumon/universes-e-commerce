package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShippingLabel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingLabelRepository extends JpaRepository<ShippingLabel, Long> {

    List<ShippingLabel> findByShipmentIdOrderByIdDesc(Long shipmentId);

    List<ShippingLabel> findByShipmentVendorIdOrderByIdDesc(Long vendorId);
}
