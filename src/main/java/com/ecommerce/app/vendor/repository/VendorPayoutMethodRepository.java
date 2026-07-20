/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface VendorPayoutMethodRepository extends JpaRepository<VendorPayoutMethod, Long> {

    List<VendorPayoutMethod> findByVendorId(Long vendorId);

    @Query("""
            select count(m)
            from VendorPayoutMethod m
            where m.accountNumber = :accountNumber
              and m.vendor.id <> :vendorId
            """)
    long countSharedAccountNumber(@Param("accountNumber") String accountNumber, @Param("vendorId") Long vendorId);
}
