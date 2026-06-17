/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.user.repository;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.model.VendorRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface VendorRoleRepository extends JpaRepository<VendorRole, Long> {

    @Query("""
            SELECT vr
            FROM VendorRole vr
            WHERE vr.vendor IS NULL OR vr.vendor = :vendor
            ORDER BY vr.name
            """)
    List<VendorRole> findAssignableRoles(@Param("vendor") Vendorprofile vendor);

    @Query("""
            SELECT vr
            FROM VendorRole vr
            WHERE vr.id = :id
              AND (vr.vendor IS NULL OR vr.vendor = :vendor)
            """)
    Optional<VendorRole> findAssignableRoleById(
            @Param("id") Long id,
            @Param("vendor") Vendorprofile vendor
    );

    List<VendorRole> findByVendorOrderByNameAsc(Vendorprofile vendor);

    Optional<VendorRole> findByIdAndVendor(Long id, Vendorprofile vendor);

    boolean existsByVendorAndSlugIgnoreCase(Vendorprofile vendor, String slug);

    boolean existsByVendorAndSlugIgnoreCaseAndIdNot(Vendorprofile vendor, String slug, Long id);

}
