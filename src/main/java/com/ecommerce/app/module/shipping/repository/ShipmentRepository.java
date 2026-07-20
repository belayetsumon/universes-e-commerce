/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findBySalesOrderId(Long orderId);

    List<Shipment> findBySalesOrderIdIn(Collection<Long> orderIds);

    List<Shipment> findByVendorId(Long vendorId);

    boolean existsBySalesOrderId(Long orderId);

    Optional<Shipment> findTopBySalesOrderIdOrderByIdDesc(Long orderId);

    Optional<Shipment> findByUuid(String uuid);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    long countByVendorIdAndStatus(Long vendorId, ShipmentStatus status);

    @Query("""
            select count(s)
            from Shipment s
            where s.vendorId = :vendorId
              and s.status = :status
              and s.carrier is null
            """)
    long countByVendorIdAndStatusAndCarrierMissing(@Param("vendorId") Long vendorId,
            @Param("status") ShipmentStatus status);

    long countByCarrier(Carrier carrier);
}
