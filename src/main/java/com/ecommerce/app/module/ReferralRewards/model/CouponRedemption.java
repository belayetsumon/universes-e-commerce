/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_coupon_redemption")
public class CouponRedemption extends BaseEntityPromotions {

    @ManyToOne
    private Coupon coupon;

    @ManyToOne
    private Users user;

    private String orderId;

    private BigDecimal discountAmount;

    public CouponRedemption() {
    }

    public CouponRedemption(Coupon coupon, Users user, String orderId, BigDecimal discountAmount) {
        this.coupon = coupon;
        this.user = user;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Users getUsers() {
        return user;
    }

    public void setUsers(Users user) {
        this.user = user;
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

}
