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

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_gift_card")
public class GiftCard extends BaseEntityPromotions {

    private String code;

    private BigDecimal initialValue;
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private GiftCardStatus status;

    @ManyToOne
    private Users issuedTo;

    private boolean redeemed;

    public GiftCard() {
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

    public boolean isRedeemed() {
        return redeemed;
    }

    public void setRedeemed(boolean redeemed) {
        this.redeemed = redeemed;
    }

}
