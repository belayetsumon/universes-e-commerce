/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_reward_point_transaction")
public class RewardPointTransaction extends BaseEntityPromotions {

    @ManyToOne
    private Users user;

    @Enumerated(EnumType.STRING)
    private RewardPointType type;

    private BigDecimal points;

    private String sourceType;

    private String referenceId;

    private LocalDateTime expiryDate;

    public RewardPointTransaction(Users user, RewardPointType type, BigDecimal points, String sourceType, String referenceId, LocalDateTime expiryDate) {
        this.user = user;
        this.type = type;
        this.points = points;
        this.sourceType = sourceType;
        this.referenceId = referenceId;
        this.expiryDate = expiryDate;
    }

    public RewardPointTransaction() {
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public RewardPointType getType() {
        return type;
    }

    public void setType(RewardPointType type) {
        this.type = type;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

}
