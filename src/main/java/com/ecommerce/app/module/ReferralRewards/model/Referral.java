/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_referral")
public class Referral extends BaseEntityPromotions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referral code owned by this user
    @Column(unique = true, nullable = false)
    private String referralCode;

    // The user who owns this referral code
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Users users;

    // The user who was referred using this code
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Users referredUser;

    private boolean rewardGranted = false;

    @Enumerated(EnumType.STRING)
    private ReferralStatus status;

//    /// Audit ///
//    @CreatedBy
//    @Column(nullable = false, updatable = false)
//    private String createdBy;
//
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime created;
//
//    @LastModifiedBy
//    @Column(insertable = false)
//    private String modifiedBy;
//
//    @LastModifiedDate
//    @Column(insertable = false)
//    private LocalDateTime modified;
    public Referral() {
    }

    public Referral(Long id, String referralCode, Users users, Users referredUser, ReferralStatus status) {
        this.id = id;
        this.referralCode = referralCode;
        this.users = users;
        this.referredUser = referredUser;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public Users getReferredUser() {
        return referredUser;
    }

    public void setReferredUser(Users referredUser) {
        this.referredUser = referredUser;
    }

    public boolean isRewardGranted() {
        return rewardGranted;
    }

    public void setRewardGranted(boolean rewardGranted) {
        this.rewardGranted = rewardGranted;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

}
