/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.repository;

import com.ecommerce.app.customer.model.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {
    
}
