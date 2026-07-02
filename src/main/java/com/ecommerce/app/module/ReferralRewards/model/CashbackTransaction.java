/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackCreditDestination;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_cashback_transaction")
public class CashbackTransaction extends BaseEntityPromotions {

    @NotNull(message = "Customer is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @NotBlank(message = "Order ID is required.")
    @Size(max = 80, message = "Order ID must not exceed 80 characters.")
    @Column(name = "order_id", nullable = false, length = 80)
    private String orderId;

    @NotNull(message = "Cashback policy is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CashbackPolicy policy;

    @NotNull(message = "Cashback amount is required.")
    @DecimalMin(value = "0.01", message = "Cashback amount must be greater than zero.")
    @Digits(integer = 15, fraction = 4, message = "Cashback amount must have maximum 15 digits and 4 decimal places.")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required.")
    @Size(min = 3, max = 3, message = "Currency must be a valid ISO 4217 code, for example BDT, USD, EUR.")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Cashback status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CashbackStatus status;

    @NotNull(message = "Cashback destination is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "credited_to", nullable = false, length = 30)
    private CashbackCreditDestination creditedTo;

    @NotBlank(message = "Idempotency key is required.")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters.")
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "credited_at")
    private LocalDateTime creditedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Size(max = 255, message = "Remarks must not exceed 255 characters.")
    @Column(length = 255)
    private String remarks;

    public CashbackTransaction(Users user, String orderId, CashbackPolicy policy, BigDecimal amount, String currency, CashbackStatus status, CashbackCreditDestination creditedTo, String idempotencyKey, LocalDateTime availableAt, LocalDateTime creditedAt, LocalDateTime cancelledAt, String remarks) {
        this.user = user;
        this.orderId = orderId;
        this.policy = policy;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.creditedTo = creditedTo;
        this.idempotencyKey = idempotencyKey;
        this.availableAt = availableAt;
        this.creditedAt = creditedAt;
        this.cancelledAt = cancelledAt;
        this.remarks = remarks;
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

    public CashbackPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(CashbackPolicy policy) {
        this.policy = policy;
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

    public CashbackStatus getStatus() {
        return status;
    }

    public void setStatus(CashbackStatus status) {
        this.status = status;
    }

    public CashbackCreditDestination getCreditedTo() {
        return creditedTo;
    }

    public void setCreditedTo(CashbackCreditDestination creditedTo) {
        this.creditedTo = creditedTo;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(LocalDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public LocalDateTime getCreditedAt() {
        return creditedAt;
    }

    public void setCreditedAt(LocalDateTime creditedAt) {
        this.creditedAt = creditedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
