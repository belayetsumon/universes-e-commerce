/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardStatus;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_gift_card")
public class GiftCard extends BaseEntityPromotions {

    @Column(nullable = false, length = 64, unique = true)
    private String code;

//    @Column(nullable = false, length = 64, unique = true)
//    private String codeHash; // store hash, not raw code
//
//    @Column(length = 8)
//    private String codeLast4;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal initialValue;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

//        @Column(nullable = false, length = 3)
//    private String currency; // BDT, USD, EUR
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GiftCardStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users issuedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users purchasedBy;

    private boolean redeemed;

    private LocalDateTime issuedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;

    public GiftCard() {
    }

    public GiftCard(String code, BigDecimal initialValue, BigDecimal balance, GiftCardStatus status, Users issuedTo, Users purchasedBy, boolean redeemed, LocalDateTime issuedAt, LocalDateTime activatedAt, LocalDateTime expiresAt, LocalDateTime lastUsedAt) {
        this.code = code;
        this.initialValue = initialValue;
        this.balance = balance;
        this.status = status;
        this.issuedTo = issuedTo;
        this.purchasedBy = purchasedBy;
        this.redeemed = redeemed;
        this.issuedAt = issuedAt;
        this.activatedAt = activatedAt;
        this.expiresAt = expiresAt;
        this.lastUsedAt = lastUsedAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public GiftCardStatus getStatus() {
        return status;
    }

    public void setStatus(GiftCardStatus status) {
        this.status = status;
    }

    public Users getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(Users issuedTo) {
        this.issuedTo = issuedTo;
    }

    public Users getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(Users purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public void setRedeemed(boolean redeemed) {
        this.redeemed = redeemed;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

}
