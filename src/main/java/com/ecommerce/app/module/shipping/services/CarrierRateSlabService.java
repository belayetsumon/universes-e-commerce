package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.CarrierRateSlab;
import com.ecommerce.app.module.shipping.repository.CarrierRateSlabRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarrierRateSlabService {

    private final CarrierRateSlabRepository repository;

    public CarrierRateSlabService(CarrierRateSlabRepository repository) {
        this.repository = repository;
    }

    public List<CarrierRateSlab> getByRate(CarrierRate rate) {
        if (rate == null) {
            return List.of();
        }
        return repository.findByCarrierRateOrderByPriorityAscMinWeightAsc(rate);
    }

    public CarrierRateSlab getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public CarrierRateSlab save(CarrierRateSlab slab) {
        if (slab.getPriority() == null) {
            slab.setPriority(100);
        }
        return repository.save(slab);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
