/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.Vendorprofile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface VendorprofileRepository extends JpaRepository<Vendorprofile, Long> {

    Vendorprofile findByUserId(Users users);

}
