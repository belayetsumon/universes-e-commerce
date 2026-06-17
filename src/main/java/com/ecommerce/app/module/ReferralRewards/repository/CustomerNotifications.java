/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface CustomerNotifications extends JpaRepository<com.ecommerce.app.module.ReferralRewards.model.CustomerNotifications, Long> {

    List<com.ecommerce.app.module.ReferralRewards.model.CustomerNotifications> findByUserOrderByCreatedAtDesc(Users user);
}
