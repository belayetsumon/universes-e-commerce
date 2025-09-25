/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface VendorPayoutMethodRepository extends JpaRepository<VendorPayoutMethod, Long> {

    List<VendorPayoutMethod> findByVendorId(Long vendorId);
}
