/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.BillingAddress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {

    /**
     * Date: 2026-04-20
     * Fix NonUniqueResultException:
     * Some users may have multiple BillingAddress rows, so queries that expect a single result
     * can fail. This method safely returns the latest record by ID.
     */
    Optional<BillingAddress> findFirstByUserId_IdOrderByIdDesc(Long id);
}
