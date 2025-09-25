package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.Unitofmeasurement;
import com.ecommerce.app.product.ripository.UnitsOfMeasureRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class UnitsOfMeasureService {
    private final UnitsOfMeasureRepository repository;

    public UnitsOfMeasureService(UnitsOfMeasureRepository repository) {
        this.repository = repository;
    }

    public List<Unitofmeasurement> getAllUnits() {
        return repository.findAll();
    }

    public Optional<Unitofmeasurement> getUnitById(Long id) {
        return repository.findById(id);
    }

    public Unitofmeasurement saveUnit(Unitofmeasurement unit) {
        return repository.save(unit);
    }

    public Unitofmeasurement updateUnit(Long id, Unitofmeasurement unitDetails) {
        return repository.findById(id).map(unit -> {
            unit.setName(unitDetails.getName());
            unit.setSymbol(unitDetails.getSymbol());
            unit.setDescription(unitDetails.getDescription());
            return repository.save(unit);
        }).orElseThrow(() -> new RuntimeException("Unit not found with id " + id));
    }

    public void deleteUnit(Long id) {
        repository.deleteById(id);
    }
}
