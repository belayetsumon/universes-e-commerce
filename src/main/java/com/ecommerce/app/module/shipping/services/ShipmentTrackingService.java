package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.model.ShipmentTrackingEvent;
import com.ecommerce.app.module.shipping.repository.ShipmentTrackingEventRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ShipmentTrackingService {

    private final ShipmentTrackingEventRepository repository;

    public ShipmentTrackingService(ShipmentTrackingEventRepository repository) {
        this.repository = repository;
    }

    public ShipmentTrackingEvent recordStatusEvent(Shipment shipment, ShipmentStatus status, String message) {
        return recordStatusEvent(shipment, status, message, null, null);
    }

    public ShipmentTrackingEvent recordStatusEvent(Shipment shipment, ShipmentStatus status,
            String message, String location, String metadataJson) {
        if (shipment == null || shipment.getId() == null || status == null) {
            return null;
        }

        ShipmentTrackingEvent event = new ShipmentTrackingEvent();
        event.setShipment(shipment);
        event.setStatus(status);
        event.setMessage(message);
        event.setLocation(location);
        event.setMetadataJson(metadataJson);
        event.setEventTime(LocalDateTime.now());
        return repository.save(event);
    }

    public void recordStatusChange(Shipment shipment, ShipmentStatus previousStatus, ShipmentStatus newStatus, String source) {
        if (shipment == null || shipment.getId() == null || newStatus == null) {
            return;
        }
        if (Objects.equals(previousStatus, newStatus)) {
            return;
        }

        String message = previousStatus == null
                ? "Shipment created with status " + newStatus.name()
                : "Shipment status changed from " + previousStatus.name() + " to " + newStatus.name();
        if (source != null && !source.isBlank()) {
            message = message + " by " + source;
        }
        recordStatusEvent(shipment, newStatus, message);
    }

    public List<ShipmentTrackingEvent> getEvents(Long shipmentId) {
        if (shipmentId == null) {
            return List.of();
        }
        return repository.findByShipmentIdOrderByEventTimeDescIdDesc(shipmentId);
    }

    public Map<Long, List<ShipmentTrackingEvent>> getEventsByShipmentIds(Collection<Long> shipmentIds) {
        if (shipmentIds == null || shipmentIds.isEmpty()) {
            return Map.of();
        }
        return repository.findByShipmentIdInOrderByEventTimeDescIdDesc(shipmentIds).stream()
                .filter(event -> event.getShipment() != null && event.getShipment().getId() != null)
                .collect(Collectors.groupingBy(
                        event -> event.getShipment().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
