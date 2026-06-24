package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingZone;
import com.ecommerce.app.module.shipping.repository.ShippingZoneRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShippingZoneService {

    private final ShippingZoneRepository repository;

    public ShippingZoneService(ShippingZoneRepository repository) {
        this.repository = repository;
    }

    public List<ShippingZone> getAll() {
        return repository.findAll();
    }

    public List<ShippingZone> getActiveZones() {
        return repository.findByActiveTrueOrderByPriorityAscNameAsc();
    }

    public List<ShippingZone> getActiveZonesForLocation(ShippingLocation location) {
        if (location == null) {
            return List.of();
        }
        return repository.findActiveByLocation(location);
    }

    public ShippingZone getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ShippingZone save(ShippingZone zone) {
        if (zone.getPriority() == null) {
            zone.setPriority(100);
        }
        return repository.save(zone);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
