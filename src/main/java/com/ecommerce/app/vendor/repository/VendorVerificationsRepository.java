/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.vendor.model.VendorVerifications;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface VendorVerificationsRepository extends JpaRepository<VendorVerifications, Long> {

    Optional<VendorVerifications> findByToken(String token);

    Optional<VendorVerifications> findByEmail(String email);
}
