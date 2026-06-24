package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShipmentTrackingEvent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, Long> {

    List<ShipmentTrackingEvent> findByShipmentIdOrderByEventTimeDescIdDesc(Long shipmentId);

    List<ShipmentTrackingEvent> findByShipmentIdInOrderByEventTimeDescIdDesc(Collection<Long> shipmentIds);
}
