/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.*;
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
@Table(name = "promotions_redemption")
//@EntityListeners(AuditingEntityListener.class)
public class Redemptions extends BaseEntityPromotions {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
    @ManyToOne
    private Users users;

    @Enumerated(EnumType.STRING)
    private RedemptionType redemptionType; // COUPON, DISCOUNT, GIFT

    private String details;        // e.g. coupon code or gift description

    private BigDecimal pointsUsed;     // how many reward points used for this redemption

    private BigDecimal amount;

    private String currency;

    private BigDecimal conversionRate;

    private String sourceProgram;

    private String sourceId;

    private String orderId;

    private Long ledgerTransactionId;

    private String reversalReference;

    private String externalReferenceId;

    private java.time.LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private RedemptionStatus status;

    public Redemptions() {
    }

    public Redemptions(Users users, RedemptionType redemptionType, String details, BigDecimal pointsUsed) {
        this.users = users;
        this.redemptionType = redemptionType;
        this.details = details;
        this.pointsUsed = pointsUsed;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public RedemptionType getRedemptionType() {
        return redemptionType;
    }

    public void setRedemptionType(RedemptionType redemptionType) {
        this.redemptionType = redemptionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public BigDecimal getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(BigDecimal pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getSourceProgram() {
        return sourceProgram;
    }

    public void setSourceProgram(String sourceProgram) {
        this.sourceProgram = sourceProgram;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getLedgerTransactionId() {
        return ledgerTransactionId;
    }

    public void setLedgerTransactionId(Long ledgerTransactionId) {
        this.ledgerTransactionId = ledgerTransactionId;
    }

    public String getReversalReference() {
        return reversalReference;
    }

    public void setReversalReference(String reversalReference) {
        this.reversalReference = reversalReference;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public java.time.LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(java.time.LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public RedemptionStatus getStatus() {
        return status;
    }

    public void setStatus(RedemptionStatus status) {
        this.status = status;
    }

}
