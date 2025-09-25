/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.repository.VendorPayoutMethodRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorPayoutMethodService {

    @Autowired
    private VendorPayoutMethodRepository repository;

    public VendorPayoutMethodService(VendorPayoutMethodRepository repository) {
        this.repository = repository;
    }

    public List<VendorPayoutMethod> findByVendorId(Long vendorId) {
        return repository.findByVendorId(vendorId);
    }

    public Map<Long, String> getPayoutMethodList(Long vendorId) {
        List<VendorPayoutMethod> methods = repository.findByVendorId(vendorId);
        return mapById(methods);
    }

    public Map<Long, String> mapById(List<VendorPayoutMethod> methods) {
        Map<Long, String> result = new HashMap<>();
        if (methods != null) {
            for (VendorPayoutMethod method : methods) {
                result.put(method.getId(), method.getPreferredMethod() + "--" + method.getBankName() + "--" + method.getAccountTitle());
            }
        }
        return result;
    }

    public List<VendorPayoutMethod> findAll() {
        return repository.findAll();
    }

    public VendorPayoutMethod findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
    }

    public VendorPayoutMethod save(VendorPayoutMethod entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
