/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_order_incentive_usage")
public class OrderIncentiveUsage extends BaseEntityPromotions {

    private String orderId;

    private Long couponId;

    private String couponCode;

    private BigDecimal couponDiscount;

    private BigDecimal walletUsed;

    private Long walletTransactionId;

    private BigDecimal rewardPointsUsed;

    private BigDecimal rewardDiscount;

    private Long rewardTransactionId;

    private String giftCardCode;

    private BigDecimal giftCardUsed;

    private Long giftCardTransactionId;

    private BigDecimal cashbackExpected;

    private BigDecimal referralBonusExpected;

    private String quoteReference;

    private String incentiveStatus;

    public OrderIncentiveUsage() {
    }

    public OrderIncentiveUsage(String orderId, Long couponId, BigDecimal walletUsed, BigDecimal rewardPointsUsed, BigDecimal giftCardUsed, BigDecimal cashbackExpected) {
        this.orderId = orderId;
        this.couponId = couponId;
        this.walletUsed = walletUsed;
        this.rewardPointsUsed = rewardPointsUsed;
        this.giftCardUsed = giftCardUsed;
        this.cashbackExpected = cashbackExpected;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public BigDecimal getWalletUsed() {
        return walletUsed;
    }

    public void setWalletUsed(BigDecimal walletUsed) {
        this.walletUsed = walletUsed;
    }

    public Long getWalletTransactionId() {
        return walletTransactionId;
    }

    public void setWalletTransactionId(Long walletTransactionId) {
        this.walletTransactionId = walletTransactionId;
    }

    public BigDecimal getRewardPointsUsed() {
        return rewardPointsUsed;
    }

    public void setRewardPointsUsed(BigDecimal rewardPointsUsed) {
        this.rewardPointsUsed = rewardPointsUsed;
    }

    public BigDecimal getRewardDiscount() {
        return rewardDiscount;
    }

    public void setRewardDiscount(BigDecimal rewardDiscount) {
        this.rewardDiscount = rewardDiscount;
    }

    public Long getRewardTransactionId() {
        return rewardTransactionId;
    }

    public void setRewardTransactionId(Long rewardTransactionId) {
        this.rewardTransactionId = rewardTransactionId;
    }

    public String getGiftCardCode() {
        return giftCardCode;
    }

    public void setGiftCardCode(String giftCardCode) {
        this.giftCardCode = giftCardCode;
    }

    public BigDecimal getGiftCardUsed() {
        return giftCardUsed;
    }

    public void setGiftCardUsed(BigDecimal giftCardUsed) {
        this.giftCardUsed = giftCardUsed;
    }

    public Long getGiftCardTransactionId() {
        return giftCardTransactionId;
    }

    public void setGiftCardTransactionId(Long giftCardTransactionId) {
        this.giftCardTransactionId = giftCardTransactionId;
    }

    public BigDecimal getCashbackExpected() {
        return cashbackExpected;
    }

    public void setCashbackExpected(BigDecimal cashbackExpected) {
        this.cashbackExpected = cashbackExpected;
    }

    public BigDecimal getReferralBonusExpected() {
        return referralBonusExpected;
    }

    public void setReferralBonusExpected(BigDecimal referralBonusExpected) {
        this.referralBonusExpected = referralBonusExpected;
    }

    public String getQuoteReference() {
        return quoteReference;
    }

    public void setQuoteReference(String quoteReference) {
        this.quoteReference = quoteReference;
    }

    public String getIncentiveStatus() {
        return incentiveStatus;
    }

    public void setIncentiveStatus(String incentiveStatus) {
        this.incentiveStatus = incentiveStatus;
    }

}
