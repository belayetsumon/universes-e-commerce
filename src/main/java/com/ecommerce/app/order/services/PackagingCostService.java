/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.PackagingCost;
import com.ecommerce.app.order.repository.PackagingCostRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class PackagingCostService {

    @Autowired
    PackagingCostRepository packagingCostRepository;

    public List<PackagingCost> allPackagingCost() {

        return packagingCostRepository.findAll();

    }

}
