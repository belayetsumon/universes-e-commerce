/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.model.PackagingType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface PackagingRateRepository extends JpaRepository<PackagingRate, Long> {

    Optional<PackagingRate> findByPackagingType(PackagingType packagingType);

    boolean existsByPackagingTypeAndIdNot(PackagingType packagingType, Long id);

    Optional<PackagingRate> findByUuid(String uuid);

    boolean existsByPackagingTypeAndVendorId(PackagingType packagingType, Long vendorId);

    List<PackagingRate> findByVendorId(Long vendorId);

    List<PackagingRate> findByVendor_Uuid(String vendorUuid);

    Optional<PackagingRate> findFirstByVendorIdAndActiveTrue(Long vendorId);
}
