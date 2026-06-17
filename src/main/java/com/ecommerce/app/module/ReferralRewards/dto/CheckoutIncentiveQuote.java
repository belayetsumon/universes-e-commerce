package com.ecommerce.app.module.ReferralRewards.dto;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CheckoutIncentiveQuote {

    private String couponCode;
    private Coupon coupon;
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    private BigDecimal rewardPointsUsed = BigDecimal.ZERO;
    private BigDecimal rewardDiscount = BigDecimal.ZERO;

    private String giftCardCode;
    private GiftCard giftCard;
    private BigDecimal giftCardUsed = BigDecimal.ZERO;

    private BigDecimal grossPayable = BigDecimal.ZERO;
    private BigDecimal netPayable = BigDecimal.ZERO;

    private final Map<Long, BigDecimal> couponDiscountByOrder = new HashMap<>();
    private final Map<Long, BigDecimal> rewardPointsByOrder = new HashMap<>();
    private final Map<Long, BigDecimal> rewardDiscountByOrder = new HashMap<>();
    private final Map<Long, BigDecimal> giftCardUsedByOrder = new HashMap<>();

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
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

    public String getGiftCardCode() {
        return giftCardCode;
    }

    public void setGiftCardCode(String giftCardCode) {
        this.giftCardCode = giftCardCode;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public BigDecimal getGiftCardUsed() {
        return giftCardUsed;
    }

    public void setGiftCardUsed(BigDecimal giftCardUsed) {
        this.giftCardUsed = giftCardUsed;
    }

    public BigDecimal getGrossPayable() {
        return grossPayable;
    }

    public void setGrossPayable(BigDecimal grossPayable) {
        this.grossPayable = grossPayable;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    public void setNetPayable(BigDecimal netPayable) {
        this.netPayable = netPayable;
    }

    public Map<Long, BigDecimal> getCouponDiscountByOrder() {
        return couponDiscountByOrder;
    }

    public Map<Long, BigDecimal> getRewardPointsByOrder() {
        return rewardPointsByOrder;
    }

    public Map<Long, BigDecimal> getRewardDiscountByOrder() {
        return rewardDiscountByOrder;
    }

    public Map<Long, BigDecimal> getGiftCardUsedByOrder() {
        return giftCardUsedByOrder;
    }
}
