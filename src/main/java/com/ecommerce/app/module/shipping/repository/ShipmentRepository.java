/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.Shipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findBySalesOrderId(Long orderId);

    List<Shipment> findByVendorId(Long vendorId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    long countByCarrier(Carrier carrier);
}
