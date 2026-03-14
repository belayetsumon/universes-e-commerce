/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.DeliveryPerson;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class DeliveryPersonService {

    private final DeliveryPersonRepository repo;

    public DeliveryPersonService(DeliveryPersonRepository repo) {
        this.repo = repo;
    }

    public List<DeliveryPerson> getByVendor(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }
}
