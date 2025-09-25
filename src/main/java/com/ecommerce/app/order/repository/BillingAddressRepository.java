/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.order.repository;

import com.ecommerce.app.order.model.BillingAddress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {

    Optional<BillingAddress> findByUserId_Id(Long Id);
}
