package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShipmentInvoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentInvoiceRepository extends JpaRepository<ShipmentInvoice, Long> {

    List<ShipmentInvoice> findByShipmentIdOrderByIdDesc(Long shipmentId);

    List<ShipmentInvoice> findByShipmentVendorIdOrderByIdDesc(Long vendorId);
}
