package com.ecommerce.app.module.shipping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class ShippingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    private Long vendorId;

    @NotBlank(message = "Rule name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Rule action is required")
    @Column(nullable = false, length = 40)
    private ShippingRuleAction action = ShippingRuleAction.DISABLE_CARRIER;

    @Size(max = 80)
    @Column(length = 80)
    private String carrierCode;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private ShippingLocation district;

    @DecimalMin(value = "0.00")
    private BigDecimal minWeight;

    @DecimalMin(value = "0.00")
    private BigDecimal maxWeight;

    @DecimalMin(value = "0.00")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal maxOrderAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal extraFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(nullable = false)
    private boolean active = true;

    @Size(max = 255)
    private String message;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modified;

    public boolean matches(Long quoteVendorId, ShippingLocation quoteLocation, BigDecimal weight, BigDecimal orderAmount, String quoteCarrierCode) {
        if (!active) {
            return false;
        }
        if (vendorId != null && (quoteVendorId == null || !vendorId.equals(quoteVendorId))) {
            return false;
        }
        if (district != null && (quoteLocation == null || !district.isSameOrAncestorOf(quoteLocation))) {
            return false;
        }
        if (carrierCode != null && !carrierCode.isBlank()
                && (quoteCarrierCode == null || !carrierCode.equalsIgnoreCase(quoteCarrierCode))) {
            return false;
        }
        BigDecimal safeWeight = weight != null ? weight : BigDecimal.ZERO;
        BigDecimal safeAmount = orderAmount != null ? orderAmount : BigDecimal.ZERO;
        return notBelow(safeWeight, minWeight) && notAbove(safeWeight, maxWeight)
                && notBelow(safeAmount, minOrderAmount) && notAbove(safeAmount, maxOrderAmount);
    }

    private boolean notBelow(BigDecimal value, BigDecimal minimum) {
        return minimum == null || value.compareTo(minimum) >= 0;
    }

    private boolean notAbove(BigDecimal value, BigDecimal maximum) {
        return maximum == null || value.compareTo(maximum) <= 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ShippingRuleAction getAction() { return action; }
    public void setAction(ShippingRuleAction action) { this.action = action; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public ShippingLocation getDistrict() { return district; }
    public void setDistrict(ShippingLocation district) { this.district = district; }
    public BigDecimal getMinWeight() { return minWeight; }
    public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public BigDecimal getMaxOrderAmount() { return maxOrderAmount; }
    public void setMaxOrderAmount(BigDecimal maxOrderAmount) { this.maxOrderAmount = maxOrderAmount; }
    public BigDecimal getExtraFee() { return extraFee; }
    public void setExtraFee(BigDecimal extraFee) { this.extraFee = extraFee; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }
}
