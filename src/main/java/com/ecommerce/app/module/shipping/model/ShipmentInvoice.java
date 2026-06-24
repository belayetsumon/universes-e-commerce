package com.ecommerce.app.module.shipping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class ShipmentInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    @NotNull(message = "Shipment is required")
    private Shipment shipment;

    @Size(max = 120)
    @Column(unique = true, length = 120)
    private String invoiceNumber;

    @DecimalMin("0.00")
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @DecimalMin("0.00")
    private BigDecimal codFeeAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    private BigDecimal vendorPayableAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    private BigDecimal marketplacePayableAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime invoiceTime = LocalDateTime.now();

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    public void snapshotFromShipment() {
        if (shipment == null) {
            return;
        }
        shippingCost = shipment.getShippingCost() != null ? shipment.getShippingCost() : BigDecimal.ZERO;
        codFeeAmount = shipment.getCodFeeAmount() != null ? shipment.getCodFeeAmount() : BigDecimal.ZERO;
        vendorPayableAmount = shipment.getVendorPayableAmount() != null ? shipment.getVendorPayableAmount() : BigDecimal.ZERO;
        marketplacePayableAmount = shipment.getMarketplacePayableAmount() != null ? shipment.getMarketplacePayableAmount() : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
    public BigDecimal getCodFeeAmount() { return codFeeAmount; }
    public void setCodFeeAmount(BigDecimal codFeeAmount) { this.codFeeAmount = codFeeAmount; }
    public BigDecimal getVendorPayableAmount() { return vendorPayableAmount; }
    public void setVendorPayableAmount(BigDecimal vendorPayableAmount) { this.vendorPayableAmount = vendorPayableAmount; }
    public BigDecimal getMarketplacePayableAmount() { return marketplacePayableAmount; }
    public void setMarketplacePayableAmount(BigDecimal marketplacePayableAmount) { this.marketplacePayableAmount = marketplacePayableAmount; }
    public LocalDateTime getInvoiceTime() { return invoiceTime; }
    public void setInvoiceTime(LocalDateTime invoiceTime) { this.invoiceTime = invoiceTime; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
}
