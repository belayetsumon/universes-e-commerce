/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    
}
