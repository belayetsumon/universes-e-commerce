/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_reward_point")
public class RewardPoint extends BaseEntityPromotions {

    @OneToOne
    private Users user;

    private Long referredUserId;

//    private String status; // PENDING, APPROVED
    private BigDecimal totalPoints = BigDecimal.ZERO;

    private BigDecimal availablePoints = BigDecimal.ZERO;

    private BigDecimal lifetimeEarned = BigDecimal.ZERO;

    public RewardPoint() {

    }

    public RewardPoint(Users user, Long referredUserId) {
        this.user = user;
        this.referredUserId = referredUserId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Long getReferredUserId() {
        return referredUserId;
    }

    public void setReferredUserId(Long referredUserId) {
        this.referredUserId = referredUserId;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }

    public BigDecimal getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(BigDecimal availablePoints) {
        this.availablePoints = availablePoints;
    }

    public BigDecimal getLifetimeEarned() {
        return lifetimeEarned;
    }

    public void setLifetimeEarned(BigDecimal lifetimeEarned) {
        this.lifetimeEarned = lifetimeEarned;
    }

}
