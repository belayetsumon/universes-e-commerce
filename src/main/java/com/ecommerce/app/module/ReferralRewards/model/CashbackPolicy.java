/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

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
@Table(name = "promotions_cashback_policy")
public class CashbackPolicy extends BaseEntityPromotions {

    private String name;

    private BigDecimal percentage;

    private BigDecimal maxCashback;

    private BigDecimal minOrderValue;

    private Long categoryId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private CashbackPolicyStatus status;

    public CashbackPolicy(String name, BigDecimal percentage, BigDecimal maxCashback, BigDecimal minOrderValue, Long categoryId, LocalDateTime startDate, LocalDateTime endDate, CashbackPolicyStatus status) {
        this.name = name;
        this.percentage = percentage;
        this.maxCashback = maxCashback;
        this.minOrderValue = minOrderValue;
        this.categoryId = categoryId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

}
