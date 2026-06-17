/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique UUID for external reference
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @NotNull(message = "Sales order ID is required")
    private Long salesOrderId;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrier_id", nullable = false)
    @NotNull(message = "Carrier is required")
    private Carrier carrier;

    @ManyToOne
    @JoinColumn(name = "delivery_person_id")
    private DeliveryPerson deliveryPerson;

    @NotBlank(message = "District is required")
    @Size(min = 2, max = 100, message = "District name must be between 2 and 100 characters")
    private String district;

    @Column(unique = true, length = 100)
    @Size(max = 100, message = "Tracking number must be at most 100 characters")
    private String trackingNumber;

    @DecimalMin(value = "0.00", message = "Shipping cost cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Shipping cost must be a valid decimal with 2 digits after decimal")
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeliverySpeed speed = DeliverySpeed.STANDARD; // STANDARD / EXPRESS

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeliveryType deliveryType = DeliveryType.HOME_DELIVERY; // HOME_DELIVERY / OFFICE_PICKUP

    // 2026-04-22: Snapshot delivery and settlement rules on Shipment so old orders stay financially stable.
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CarrierMode carrierModeSnapshot = CarrierMode.THIRD_PARTY;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SettlementMode settlementMode = SettlementMode.MARKETPLACE_MANAGED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShippingChargeOwner shippingChargeOwner = ShippingChargeOwner.MARKETPLACE;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CodCollectionMode codCollectionMode = CodCollectionMode.CARRIER_COLLECTS_FOR_MARKETPLACE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Shipment status is required")
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Size(max = 255, message = "Label URL cannot exceed 255 characters")
    private String labelUrl;

    @Lob
    private String metadataJson;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items;

    // COD fields
    private boolean cod;

    @DecimalMin(value = "0.00", message = "Total order amount cannot be negative")
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "COD collected cannot be negative")
    private BigDecimal codCollected = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "COD pending cannot be negative")
    private BigDecimal codPending = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Product net amount cannot be negative")
    private BigDecimal productNetAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Marketplace commission amount cannot be negative")
    private BigDecimal marketplaceCommissionAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "COD fee amount cannot be negative")
    private BigDecimal codFeeAmount = BigDecimal.ZERO;

    private boolean shippingPaidToMarketplace;

    @DecimalMin(value = "0.00", message = "Vendor payable amount cannot be negative")
    private BigDecimal vendorPayableAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Marketplace payable amount cannot be negative")
    private BigDecimal marketplacePayableAmount = BigDecimal.ZERO;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime modified;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    public Shipment() {
    }

    public Shipment(Long id, Long salesOrderId, Long vendorId, Carrier carrier, DeliveryPerson deliveryPerson, String district, String trackingNumber, String labelUrl, String metadataJson, List<ShipmentItem> items, boolean cod, LocalDateTime created, LocalDateTime modified, String createdBy, String modifiedBy) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.vendorId = vendorId;
        this.carrier = carrier;
        this.deliveryPerson = deliveryPerson;
        this.district = district;
        this.trackingNumber = trackingNumber;
        this.labelUrl = labelUrl;
        this.metadataJson = metadataJson;
        this.items = items;
        this.cod = cod;
        this.created = created;
        this.modified = modified;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    public DeliveryPerson getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(DeliveryPerson deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public DeliverySpeed getSpeed() {
        return speed;
    }

    public void setSpeed(DeliverySpeed speed) {
        this.speed = speed;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public CarrierMode getCarrierModeSnapshot() {
        return carrierModeSnapshot != null ? carrierModeSnapshot : CarrierMode.THIRD_PARTY;
    }

    public void setCarrierModeSnapshot(CarrierMode carrierModeSnapshot) {
        this.carrierModeSnapshot = carrierModeSnapshot;
    }

    public SettlementMode getSettlementMode() {
        return settlementMode != null ? settlementMode : SettlementMode.MARKETPLACE_MANAGED;
    }

    public void setSettlementMode(SettlementMode settlementMode) {
        this.settlementMode = settlementMode;
    }

    public ShippingChargeOwner getShippingChargeOwner() {
        return shippingChargeOwner != null ? shippingChargeOwner : ShippingChargeOwner.MARKETPLACE;
    }

    public void setShippingChargeOwner(ShippingChargeOwner shippingChargeOwner) {
        this.shippingChargeOwner = shippingChargeOwner;
    }

    public CodCollectionMode getCodCollectionMode() {
        return codCollectionMode != null ? codCollectionMode : CodCollectionMode.CARRIER_COLLECTS_FOR_MARKETPLACE;
    }

    public void setCodCollectionMode(CodCollectionMode codCollectionMode) {
        this.codCollectionMode = codCollectionMode;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getLabelUrl() {
        return labelUrl;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public List<ShipmentItem> getItems() {
        return items;
    }

    public void setItems(List<ShipmentItem> items) {
        this.items = items;
    }

    public boolean isCod() {
        return cod;
    }

    public void setCod(boolean cod) {
        this.cod = cod;
    }

    public BigDecimal getTotalOrderAmount() {
        return totalOrderAmount;
    }

    public void setTotalOrderAmount(BigDecimal totalOrderAmount) {
        this.totalOrderAmount = totalOrderAmount;
    }

    public BigDecimal getCodCollected() {
        return codCollected;
    }

    public void setCodCollected(BigDecimal codCollected) {
        this.codCollected = codCollected;
    }

    public BigDecimal getCodPending() {
        return codPending;
    }

    public void setCodPending(BigDecimal codPending) {
        this.codPending = codPending;
    }

    public BigDecimal getProductNetAmount() {
        return productNetAmount;
    }

    public void setProductNetAmount(BigDecimal productNetAmount) {
        this.productNetAmount = productNetAmount;
    }

    public BigDecimal getMarketplaceCommissionAmount() {
        return marketplaceCommissionAmount;
    }

    public void setMarketplaceCommissionAmount(BigDecimal marketplaceCommissionAmount) {
        this.marketplaceCommissionAmount = marketplaceCommissionAmount;
    }

    public BigDecimal getCodFeeAmount() {
        return codFeeAmount;
    }

    public void setCodFeeAmount(BigDecimal codFeeAmount) {
        this.codFeeAmount = codFeeAmount;
    }

    public boolean isShippingPaidToMarketplace() {
        return shippingPaidToMarketplace;
    }

    public void setShippingPaidToMarketplace(boolean shippingPaidToMarketplace) {
        this.shippingPaidToMarketplace = shippingPaidToMarketplace;
    }

    public BigDecimal getVendorPayableAmount() {
        return vendorPayableAmount;
    }

    public void setVendorPayableAmount(BigDecimal vendorPayableAmount) {
        this.vendorPayableAmount = vendorPayableAmount;
    }

    public BigDecimal getMarketplacePayableAmount() {
        return marketplacePayableAmount;
    }

    public void setMarketplacePayableAmount(BigDecimal marketplacePayableAmount) {
        this.marketplacePayableAmount = marketplacePayableAmount;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // 2026-04-22: Marketplace keeps commission plus charges it owns; vendor payout adds vendor-owned prepaid shipping.
    public void recalculateSettlementAmounts() {
        BigDecimal productNet = defaultAmount(productNetAmount);
        BigDecimal commission = defaultAmount(marketplaceCommissionAmount);
        BigDecimal shipping = defaultAmount(shippingCost);
        BigDecimal codFee = defaultAmount(codFeeAmount);

        BigDecimal vendorPayable = productNet.subtract(commission);
        if (vendorPayable.compareTo(BigDecimal.ZERO) < 0) {
            vendorPayable = BigDecimal.ZERO;
        }

        if (getShippingChargeOwner() == ShippingChargeOwner.VENDOR && shippingPaidToMarketplace) {
            vendorPayable = vendorPayable.add(shipping);
        }

        BigDecimal marketplacePayable = commission;
        if (getShippingChargeOwner() == ShippingChargeOwner.MARKETPLACE) {
            marketplacePayable = marketplacePayable.add(shipping);
        }
        if (getCodCollectionMode() == CodCollectionMode.MARKETPLACE_COLLECTS
                || getCodCollectionMode() == CodCollectionMode.CARRIER_COLLECTS_FOR_MARKETPLACE) {
            marketplacePayable = marketplacePayable.add(codFee);
        }

        this.vendorPayableAmount = vendorPayable;
        this.marketplacePayableAmount = marketplacePayable;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

}
