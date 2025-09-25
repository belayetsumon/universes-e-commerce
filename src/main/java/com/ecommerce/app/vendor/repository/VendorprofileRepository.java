/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author User
 */
public interface VendorprofileRepository extends JpaRepository<Vendorprofile, Long> {

    // Vendorprofile findByUserId(Users users);
    List<Vendorprofile> findByUserId(Users user);

    @Query("SELECT v.vendorCode FROM Vendorprofile v WHERE v.vendorCode LIKE :prefix ORDER BY v.vendorCode DESC")
    List<String> findLatestVendorCode(@Param("prefix") String prefix, Pageable pageable);

}
