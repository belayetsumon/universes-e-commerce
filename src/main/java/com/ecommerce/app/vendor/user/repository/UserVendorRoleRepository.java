/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.user.repository;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface UserVendorRoleRepository extends JpaRepository<UserVendorRole, Long> {

    List<UserVendorRole> findAllByVendor(Vendorprofile vendor);

    boolean existsByUsers_EmailAndVendor_Id(String email, Long vendorId);

    boolean existsByUsers_EmailAndVendor_IdAndVendorRole_Name(String email, Long vendorId, String roleName);

}
