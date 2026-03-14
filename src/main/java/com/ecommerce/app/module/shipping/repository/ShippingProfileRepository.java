/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.ShippingProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface ShippingProfileRepository extends JpaRepository<ShippingProfile, Long> {

    List<ShippingProfile> findByVendorIdOrVendorIdIsNull(Long vendorId);

    ShippingProfile findByVendorId(Long vendorId);

    long countByAllowedCarriers(Carrier carrier);

    List<ShippingProfile> findByVendorIdAndActiveTrue(Long vendorId);
}
