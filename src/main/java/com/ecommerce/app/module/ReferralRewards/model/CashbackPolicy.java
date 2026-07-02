/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackPolicyStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(
        name = "promotions_cashback_policy",
        indexes = {
            @Index(name = "idx_cashback_policy_status", columnList = "status"),
            @Index(name = "idx_cashback_policy_date_range", columnList = "start_date,end_date")
        }
)
public class CashbackPolicy extends BaseEntityPromotions {

    @NotBlank(message = "Cashback policy name is required.")
    @Size(min = 3, max = 150, message = "Cashback policy name must be between 3 and 150 characters.")
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull(message = "Cashback percentage is required.")
    @DecimalMin(value = "0.01", message = "Cashback percentage must be greater than 0.")
    @DecimalMax(value = "100.00", message = "Cashback percentage cannot exceed 100%.")
    @Digits(integer = 3, fraction = 2, message = "Cashback percentage must have maximum 3 digits and 2 decimal places.")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @NotNull(message = "Maximum cashback amount is required.")
    @DecimalMin(value = "0.00", message = "Maximum cashback amount cannot be negative.")
    @Digits(integer = 15, fraction = 4, message = "Maximum cashback amount must have maximum 15 digits and 4 decimal places.")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maxCashback;

    @NotNull(message = "Minimum order value is required.")
    @DecimalMin(value = "0.00", message = "Minimum order value cannot be negative.")
    @Digits(integer = 15, fraction = 4, message = "Minimum order value must have maximum 15 digits and 4 decimal places.")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minOrderValue;

    @ElementCollection
    @CollectionTable(
            name = "cashback_policy_categories",
            joinColumns = @JoinColumn(name = "cashback_policy_id")
    )
    @Column(name = "category_id", nullable = false)
    private Set<Long> categoryIds = new HashSet<>();

    @NotNull(message = "Cashback policy start date is required.")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "Cashback policy end date is required.")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @NotNull(message = "Cashback policy status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CashbackPolicyStatus status;

    @NotBlank(message = "Currency is required.")
    @Size(min = 3, max = 3, message = "Currency must be a valid ISO 4217 code, for example BDT, USD, EUR.")
    @Column(nullable = false, length = 3)
    private String currency;

    @AssertTrue(message = "Cashback policy end date must be after start date.")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Maximum cashback amount must be greater than zero when cashback percentage is greater than zero.")
    public boolean isValidMaxCashback() {
        if (percentage == null || maxCashback == null) {
            return true;
        }
        return percentage.compareTo(BigDecimal.ZERO) > 0
                && maxCashback.compareTo(BigDecimal.ZERO) > 0;
    }

    public CashbackPolicy(String name, BigDecimal percentage, BigDecimal maxCashback, BigDecimal minOrderValue, LocalDateTime startDate, LocalDateTime endDate, CashbackPolicyStatus status, String currency) {
        this.name = name;
        this.percentage = percentage;
        this.maxCashback = maxCashback;
        this.minOrderValue = minOrderValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.currency = currency;
    }

    public CashbackPolicy() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getMaxCashback() {
        return maxCashback;
    }

    public void setMaxCashback(BigDecimal maxCashback) {
        this.maxCashback = maxCashback;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public CashbackPolicyStatus getStatus() {
        return status;
    }

    public void setStatus(CashbackPolicyStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
