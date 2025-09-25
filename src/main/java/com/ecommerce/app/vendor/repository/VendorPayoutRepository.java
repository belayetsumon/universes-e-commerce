/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface VendorPayoutRepository extends JpaRepository<VendorPayout, Long> {

    List<VendorPayout> findByStatus(VendorPayoutStatusEnum status);

    List<VendorPayout> findByVendor_IdOrderByIdDesc(Long vendorId);
}
