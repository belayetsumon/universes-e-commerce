/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.Redemptions;
import com.ecommerce.app.module.ReferralRewards.enumvalue.RedemptionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface RewardRedemptionRepository extends JpaRepository<Redemptions, Long> {

//    List<Redemptions> findAllByUsersOrderByRedeemedAtDesc(Users users);
    Optional<Redemptions> findFirstByRedemptionTypeAndOrderIdAndSourceProgramAndSourceId(
            RedemptionType redemptionType,
            String orderId,
            String sourceProgram,
            String sourceId
    );
}
