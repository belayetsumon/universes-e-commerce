/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponRedemptionStatus;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "promotions_coupon_redemption",
        indexes = {
            @Index(name = "idx_coupon_redemption_coupon", columnList = "coupon_id"),
            @Index(name = "idx_coupon_redemption_user", columnList = "user_id"),
            @Index(name = "idx_coupon_redemption_order", columnList = "order_id"),
            @Index(name = "idx_coupon_redemption_status", columnList = "status")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_coupon_redemption_order_coupon",
                    columnNames = {"order_id", "coupon_id"}
            ),
            @UniqueConstraint(
                    name = "uk_coupon_redemption_idempotency",
                    columnNames = {"idempotency_key"}
            )
        }
)
public class CouponRedemption extends BaseEntityPromotions {

    @NotNull(message = "Coupon is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @NotNull(message = "Customer is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @NotBlank(message = "Order ID is required.")
    @Size(max = 80, message = "Order ID must not exceed 80 characters.")
    @Column(name = "order_id", nullable = false, length = 80)
    private String orderId;

    @NotNull(message = "Discount amount is required.")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative.")
    @Digits(integer = 15, fraction = 4, message = "Discount amount must have maximum 15 digits and 4 decimal places.")
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @NotBlank(message = "Currency is required.")
    @Size(min = 3, max = 3, message = "Currency must be a valid ISO 4217 code, for example BDT, USD, EUR.")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Coupon redemption status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponRedemptionStatus status;

    @NotBlank(message = "Idempotency key is required.")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters.")
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Size(max = 255, message = "Remarks must not exceed 255 characters.")
    @Column(length = 255)
    private String remarks;

    public CouponRedemption() {
    }

    public CouponRedemption(Coupon coupon, Users user, String orderId, BigDecimal discountAmount, String currency, CouponRedemptionStatus status, String idempotencyKey, LocalDateTime redeemedAt, LocalDateTime cancelledAt, LocalDateTime refundedAt, String remarks) {
        this.coupon = coupon;
        this.user = user;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
        this.currency = currency;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.redeemedAt = redeemedAt;
        this.cancelledAt = cancelledAt;
        this.refundedAt = refundedAt;
        this.remarks = remarks;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Users getUsers() {
        return user;
    }

    public void setUsers(Users users) {
        this.user = users;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CouponRedemptionStatus getStatus() {
        return status;
    }

    public void setStatus(CouponRedemptionStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
