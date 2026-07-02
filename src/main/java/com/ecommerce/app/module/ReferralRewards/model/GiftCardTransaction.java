/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_gift_card_transaction")
public class GiftCardTransaction extends BaseEntityPromotions {

    @ManyToOne(fetch = FetchType.LAZY)
    private GiftCard giftCard;

    @Enumerated(EnumType.STRING)
    private GiftCardTransactionType type;
    // ISSUE, ACTIVATE, REDEEM, REFUND, EXPIRE, ADJUSTMENT

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    private String currency;

    private String orderId;
    private String refundId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    public GiftCardTransaction() {
    }

    public GiftCardTransaction(GiftCard giftCard, GiftCardTransactionType type, BigDecimal amount, BigDecimal balanceAfter, String currency, String orderId, String refundId, String idempotencyKey) {
        this.giftCard = giftCard;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.currency = currency;
        this.orderId = orderId;
        this.refundId = refundId;
        this.idempotencyKey = idempotencyKey;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public GiftCardTransactionType getType() {
        return type;
    }

    public void setType(GiftCardTransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

}
