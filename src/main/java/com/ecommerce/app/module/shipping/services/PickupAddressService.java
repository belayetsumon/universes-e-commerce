package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.PickupAddress;
import com.ecommerce.app.module.shipping.repository.PickupAddressRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PickupAddressService {

    private final PickupAddressRepository repository;

    public PickupAddressService(PickupAddressRepository repository) {
        this.repository = repository;
    }

    public List<PickupAddress> getAll() {
        return repository.findAll();
    }

    public List<PickupAddress> getByVendor(Long vendorId) {
        return vendorId == null ? List.of() : repository.findByVendorIdOrderByDefaultAddressDescIdDesc(vendorId);
    }

    public PickupAddress getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public PickupAddress getDefaultForVendor(Long vendorId) {
        return vendorId == null ? null : repository.findFirstByVendorIdAndDefaultAddressTrueAndActiveTrueOrderByIdDesc(vendorId).orElse(null);
    }

    @Transactional
    public PickupAddress save(PickupAddress address) {
        if (address != null && address.isDefaultAddress() && address.getVendorId() != null) {
            for (PickupAddress existing : repository.findByVendorIdOrderByDefaultAddressDescIdDesc(address.getVendorId())) {
                if (address.getId() == null || !address.getId().equals(existing.getId())) {
                    existing.setDefaultAddress(false);
                    repository.save(existing);
                }
            }
        }
        return repository.save(address);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
