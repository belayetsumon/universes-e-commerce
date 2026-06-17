/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_referral_reward")
public class ReferralReward extends BaseEntityPromotions {

    @OneToOne
    private Referral referral;

    private BigDecimal referrerReward;

    private BigDecimal referredReward;

    public ReferralReward(Referral referral, BigDecimal referrerReward, BigDecimal referredReward) {
        this.referral = referral;
        this.referrerReward = referrerReward;
        this.referredReward = referredReward;
    }

    public ReferralReward() {
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public BigDecimal getReferrerReward() {
        return referrerReward;
    }

    public void setReferrerReward(BigDecimal referrerReward) {
        this.referrerReward = referrerReward;
    }

    public BigDecimal getReferredReward() {
        return referredReward;
    }

    public void setReferredReward(BigDecimal referredReward) {
        this.referredReward = referredReward;
    }

}
