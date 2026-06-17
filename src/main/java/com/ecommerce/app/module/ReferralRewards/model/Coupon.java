/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_coupon")
public class Coupon extends BaseEntityPromotions {

    private String title;

    @Column(unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    private CouponType type; // FIXED or PERCENT

    private BigDecimal value;
    private BigDecimal maxDiscount;
    private BigDecimal minimumOrder;
    private LocalDateTime expiryDate;
    private LocalDateTime startDate;

    private int usageLimit; // e.g., 100 uses
    private int perUserUsageLimit;

    private int timesUsed;
    private boolean stackable;
    private boolean newCustomerOnly;
    private boolean firstOrderOnly;
    private String vendorScope;
    private String campaignScope;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    public Coupon() {
    }

    public Coupon(String title, String code, String description, CouponType type, BigDecimal value, LocalDateTime expiryDate, int usageLimit, int timesUsed, CouponStatus status) {
        this.title = title;
        this.code = code;
        this.description = description;
        this.type = type;
        this.value = value;
        this.expiryDate = expiryDate;
        this.usageLimit = usageLimit;
        this.timesUsed = timesUsed;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CouponType getType() {
        return type;
    }

    public void setType(CouponType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public BigDecimal getMinimumOrder() {
        return minimumOrder;
    }

    public void setMinimumOrder(BigDecimal minimumOrder) {
        this.minimumOrder = minimumOrder;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getPerUserUsageLimit() {
        return perUserUsageLimit;
    }

    public void setPerUserUsageLimit(int perUserUsageLimit) {
        this.perUserUsageLimit = perUserUsageLimit;
    }

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public boolean isNewCustomerOnly() {
        return newCustomerOnly;
    }

    public void setNewCustomerOnly(boolean newCustomerOnly) {
        this.newCustomerOnly = newCustomerOnly;
    }

    public boolean isFirstOrderOnly() {
        return firstOrderOnly;
    }

    public void setFirstOrderOnly(boolean firstOrderOnly) {
        this.firstOrderOnly = firstOrderOnly;
    }

    public String getVendorScope() {
        return vendorScope;
    }

    public void setVendorScope(String vendorScope) {
        this.vendorScope = vendorScope;
    }

    public String getCampaignScope() {
        return campaignScope;
    }

    public void setCampaignScope(String campaignScope) {
        this.campaignScope = campaignScope;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }

}
