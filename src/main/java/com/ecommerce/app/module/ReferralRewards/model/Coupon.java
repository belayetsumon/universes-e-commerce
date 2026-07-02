/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponType;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author libertyerp_local
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_coupon")
public class Coupon extends BaseEntityPromotions {

    private String title;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private CouponType type; // FIXED or PERCENT

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal value;
    @Column(precision = 18, scale = 2)
    private BigDecimal maxDiscount;
    // Conditions
    @Column(precision = 18, scale = 2)
    private BigDecimal minimumOrder;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiryDate;

    private int usageLimit; // e.g., 100 uses

    private int perUserUsageLimit;

    private int timesUsed;

    // Stacking
    private boolean stackable;

    private Integer priority = 0;

    private Boolean autoApply = false;

    // Customer Rules
    private boolean newCustomerOnly;

    private boolean firstOrderOnly;

    private Boolean guestAllowed = false;

    private String vendorScope;

    private String campaignScope;

    // Scope
    @Enumerated(EnumType.STRING)
    private CouponScope scope = CouponScope.GLOBAL;

    // Visibility
    private Boolean publicCoupon = true;

    private Boolean requiresCode = true;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    // Fraud
    private Integer maxUsesPerIP;

    private Integer maxUsesPerDevice;

    // Soft Delete
    private Boolean deleted = false;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getAutoApply() {
        return autoApply;
    }

    public void setAutoApply(Boolean autoApply) {
        this.autoApply = autoApply;
    }

    public Boolean getGuestAllowed() {
        return guestAllowed;
    }

    public void setGuestAllowed(Boolean guestAllowed) {
        this.guestAllowed = guestAllowed;
    }

    public CouponScope getScope() {
        return scope;
    }

    public void setScope(CouponScope scope) {
        this.scope = scope;
    }

    public Boolean getPublicCoupon() {
        return publicCoupon;
    }

    public void setPublicCoupon(Boolean publicCoupon) {
        this.publicCoupon = publicCoupon;
    }

    public Boolean getRequiresCode() {
        return requiresCode;
    }

    public void setRequiresCode(Boolean requiresCode) {
        this.requiresCode = requiresCode;
    }

    public Integer getMaxUsesPerIP() {
        return maxUsesPerIP;
    }

    public void setMaxUsesPerIP(Integer maxUsesPerIP) {
        this.maxUsesPerIP = maxUsesPerIP;
    }

    public Integer getMaxUsesPerDevice() {
        return maxUsesPerDevice;
    }

    public void setMaxUsesPerDevice(Integer maxUsesPerDevice) {
        this.maxUsesPerDevice = maxUsesPerDevice;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}
