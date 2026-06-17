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
@Table(name = "promotions_cashback_transaction")
public class CashbackTransaction extends BaseEntityPromotions {

    @ManyToOne
    private Users user;

    private String orderId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CashbackStatus status;

    private String creditedTo;

    public CashbackTransaction(Users user, String orderId, BigDecimal amount, CashbackStatus status, String creditedTo) {
        this.user = user;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.creditedTo = creditedTo;
    }

    public CashbackTransaction() {
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CashbackStatus getStatus() {
        return status;
    }

    public void setStatus(CashbackStatus status) {
        this.status = status;
    }

    public String getCreditedTo() {
        return creditedTo;
    }

    public void setCreditedTo(String creditedTo) {
        this.creditedTo = creditedTo;
    }

}
