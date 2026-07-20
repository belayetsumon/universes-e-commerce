package com.ecommerce.app.module.fraud.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FraudContext {

    private String deviceIdentifier;
    private String deviceFingerprint;
    private String userAgent;
    private String sessionIdentifier;
    private String ipAddress;
    private String ipCountry;
    private String ipLocation;
    private String shippingCountry;
    private String shippingDistrict;
    private String billingCountry;
    private String paymentCountry;
    private String paymentMethod;
    private String salesChannel;
    private Long vendorId;
    private Long productId;
    private Long categoryId;
    private BigDecimal orderValue;
    private String promotionType;
    private String couponCode;
    private String referralCode;
    private BigDecimal couponDiscountAmount;
    private BigDecimal cashbackAmount;
    private BigDecimal walletAmount;
    private BigDecimal giftCardAmount;
    private Map<String, Object> metadata = new HashMap<>();

    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getSessionIdentifier() { return sessionIdentifier; }
    public void setSessionIdentifier(String sessionIdentifier) { this.sessionIdentifier = sessionIdentifier; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getIpCountry() { return ipCountry; }
    public void setIpCountry(String ipCountry) { this.ipCountry = ipCountry; }
    public String getIpLocation() { return ipLocation; }
    public void setIpLocation(String ipLocation) { this.ipLocation = ipLocation; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
    public String getShippingDistrict() { return shippingDistrict; }
    public void setShippingDistrict(String shippingDistrict) { this.shippingDistrict = shippingDistrict; }
    public String getBillingCountry() { return billingCountry; }
    public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }
    public String getPaymentCountry() { return paymentCountry; }
    public void setPaymentCountry(String paymentCountry) { this.paymentCountry = paymentCountry; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getSalesChannel() { return salesChannel; }
    public void setSalesChannel(String salesChannel) { this.salesChannel = salesChannel; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getOrderValue() { return orderValue; }
    public void setOrderValue(BigDecimal orderValue) { this.orderValue = orderValue; }
    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public BigDecimal getCouponDiscountAmount() { return couponDiscountAmount; }
    public void setCouponDiscountAmount(BigDecimal couponDiscountAmount) { this.couponDiscountAmount = couponDiscountAmount; }
    public BigDecimal getCashbackAmount() { return cashbackAmount; }
    public void setCashbackAmount(BigDecimal cashbackAmount) { this.cashbackAmount = cashbackAmount; }
    public BigDecimal getWalletAmount() { return walletAmount; }
    public void setWalletAmount(BigDecimal walletAmount) { this.walletAmount = walletAmount; }
    public BigDecimal getGiftCardAmount() { return giftCardAmount; }
    public void setGiftCardAmount(BigDecimal giftCardAmount) { this.giftCardAmount = giftCardAmount; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata == null ? new HashMap<>() : metadata; }
}
