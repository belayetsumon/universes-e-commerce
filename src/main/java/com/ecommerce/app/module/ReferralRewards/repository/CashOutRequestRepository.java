/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface CashOutRequestRepository extends JpaRepository<CashOutRequest, Long> {

    List<CashOutRequest> findByUser(Users user);

    List<CashOutRequest> findByStatus(String status);
}
