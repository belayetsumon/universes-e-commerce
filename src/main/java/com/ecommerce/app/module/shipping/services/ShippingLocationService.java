package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.repository.ShippingLocationRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShippingLocationService {

    private final ShippingLocationRepository repository;

    public ShippingLocationService(ShippingLocationRepository repository) {
        this.repository = repository;
    }

    public List<ShippingLocation> getAll() {
        return repository.findAll();
    }

    public List<ShippingLocation> getActiveLocations() {
        return repository.findByActiveTrueOrderByTypeAscPriorityAscNameAsc();
    }

    public List<ShippingLocation> findAllById(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of() : repository.findByIdIn(ids);
    }

    public ShippingLocation getById(Long id) {
        return id == null ? null : repository.findById(id).orElse(null);
    }

    public ShippingLocation save(ShippingLocation location) {
        normalize(location);
        return repository.save(location);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean rateAppliesToLocation(CarrierRate rate, ShippingLocation customerLocation) {
        if (rate == null || customerLocation == null) {
            return false;
        }

        if (rate.getZone() != null
                && rate.getZone().isActive()
                && appliesToAnyLocation(rate.getZone().getCoverageLocations(), customerLocation)) {
            return true;
        }

        return appliesToAnyLocation(rate.getDistrict(), customerLocation);
    }

    public boolean appliesToAnyLocation(List<ShippingLocation> coverageLocations, ShippingLocation customerLocation) {
        if (coverageLocations == null || coverageLocations.isEmpty() || customerLocation == null) {
            return false;
        }
        return coverageLocations.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(location -> location.isSameOrAncestorOf(customerLocation));
    }

    private void normalize(ShippingLocation location) {
        if (location == null) {
            return;
        }
        if (location.getCode() != null) {
            location.setCode(location.getCode().trim().toUpperCase().replace(' ', '_'));
        }
        if (location.getIso2() != null) {
            location.setIso2(location.getIso2().trim().toUpperCase());
        }
        if (location.getIso3() != null) {
            location.setIso3(location.getIso3().trim().toUpperCase());
        }
    }
}
