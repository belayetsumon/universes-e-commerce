/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.SalesOrder;
import jakarta.persistence.Entity;
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
@Table(name = "promotions_gift_card_transaction")
public class GiftCardTransaction extends BaseEntityPromotions {

    @ManyToOne
    private GiftCard giftCard;

    @ManyToOne
    private SalesOrder order;

    private LocalDateTime usedAt;

    @ManyToOne
    private Users user;

    private BigDecimal amountUsed;

    private BigDecimal remainingBalance;

    public GiftCardTransaction() {
    }

    public GiftCardTransaction(GiftCard giftCard, SalesOrder order, LocalDateTime usedAt, Users user, BigDecimal amountUsed, BigDecimal remainingBalance) {
        this.giftCard = giftCard;
        this.order = order;
        this.usedAt = usedAt;
        this.user = user;
        this.amountUsed = amountUsed;
        this.remainingBalance = remainingBalance;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public SalesOrder getOrder() {
        return order;
    }

    public void setOrder(SalesOrder order) {
        this.order = order;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public BigDecimal getAmountUsed() {
        return amountUsed;
    }

    public void setAmountUsed(BigDecimal amountUsed) {
        this.amountUsed = amountUsed;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

}
