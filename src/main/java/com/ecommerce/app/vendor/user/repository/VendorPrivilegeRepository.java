/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.user.repository;

import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface VendorPrivilegeRepository extends JpaRepository<VendorPrivilege, Long> {

}
