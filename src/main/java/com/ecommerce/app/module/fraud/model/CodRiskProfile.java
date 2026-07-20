package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_cod_risk_profiles", indexes = {
    @Index(name = "idx_fraud_cod_customer", columnList = "customer_id"),
    @Index(name = "idx_fraud_cod_vendor", columnList = "vendor_id"),
    @Index(name = "idx_fraud_cod_mobile", columnList = "mobile_hash"),
    @Index(name = "idx_fraud_cod_address", columnList = "address_hash"),
    @Index(name = "idx_fraud_cod_device", columnList = "device_identifier"),
    @Index(name = "idx_fraud_cod_district", columnList = "district")
})
public class CodRiskProfile extends BaseFraudEntity {

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "mobile_hash", length = 128)
    private String mobileHash;

    @Column(name = "address_hash", length = 128)
    private String addressHash;

    @Column(name = "device_identifier", length = 160)
    private String deviceIdentifier;

    @Column(name = "district", length = 120)
    private String district;

    @Column(name = "cod_order_count", nullable = false)
    private long codOrderCount;

    @Column(name = "cod_success_count", nullable = false)
    private long codSuccessCount;

    @Column(name = "successful_prepaid_order_count", nullable = false)
    private long successfulPrepaidOrderCount;

    @Column(name = "cod_rto_count", nullable = false)
    private long codRtoCount;

    @Column(name = "delivery_refusal_count", nullable = false)
    private long deliveryRefusalCount;

    @Column(name = "cod_disabled", nullable = false)
    private boolean codDisabled;

    @Column(name = "customer_cod_limit", precision = 19, scale = 4)
    private BigDecimal customerCodLimit;

    @Column(name = "vendor_cod_limit", precision = 19, scale = 4)
    private BigDecimal vendorCodLimit;

    @Column(name = "last_delivery_refusal_reason", length = 500)
    private String lastDeliveryRefusalReason;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getMobileHash() { return mobileHash; }
    public void setMobileHash(String mobileHash) { this.mobileHash = mobileHash; }
    public String getAddressHash() { return addressHash; }
    public void setAddressHash(String addressHash) { this.addressHash = addressHash; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public long getCodOrderCount() { return codOrderCount; }
    public void setCodOrderCount(long codOrderCount) { this.codOrderCount = codOrderCount; }
    public long getCodSuccessCount() { return codSuccessCount; }
    public void setCodSuccessCount(long codSuccessCount) { this.codSuccessCount = codSuccessCount; }
    public long getSuccessfulPrepaidOrderCount() { return successfulPrepaidOrderCount; }
    public void setSuccessfulPrepaidOrderCount(long successfulPrepaidOrderCount) { this.successfulPrepaidOrderCount = successfulPrepaidOrderCount; }
    public long getCodRtoCount() { return codRtoCount; }
    public void setCodRtoCount(long codRtoCount) { this.codRtoCount = codRtoCount; }
    public long getDeliveryRefusalCount() { return deliveryRefusalCount; }
    public void setDeliveryRefusalCount(long deliveryRefusalCount) { this.deliveryRefusalCount = deliveryRefusalCount; }
    public boolean isCodDisabled() { return codDisabled; }
    public void setCodDisabled(boolean codDisabled) { this.codDisabled = codDisabled; }
    public BigDecimal getCustomerCodLimit() { return customerCodLimit; }
    public void setCustomerCodLimit(BigDecimal customerCodLimit) { this.customerCodLimit = customerCodLimit; }
    public BigDecimal getVendorCodLimit() { return vendorCodLimit; }
    public void setVendorCodLimit(BigDecimal vendorCodLimit) { this.vendorCodLimit = vendorCodLimit; }
    public String getLastDeliveryRefusalReason() { return lastDeliveryRefusalReason; }
    public void setLastDeliveryRefusalReason(String lastDeliveryRefusalReason) { this.lastDeliveryRefusalReason = lastDeliveryRefusalReason; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
