package com.ecommerce.app.finance.dto;

import java.math.BigDecimal;

public class AdminVendorDueRow {

    private Long vendorId;
    private String vendorCode;
    private String companyName;
    private BigDecimal pendingAmount = BigDecimal.ZERO;
    private BigDecimal availableAmount = BigDecimal.ZERO;
    private BigDecimal reservedAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal shipmentVendorPayable = BigDecimal.ZERO;
    private BigDecimal shipmentMarketplacePayable = BigDecimal.ZERO;
    private BigDecimal requestedPayoutAmount = BigDecimal.ZERO;
    private BigDecimal processedPayoutAmount = BigDecimal.ZERO;
    private BigDecimal paidPayoutAmount = BigDecimal.ZERO;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(BigDecimal reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public BigDecimal getShipmentVendorPayable() {
        return shipmentVendorPayable;
    }

    public void setShipmentVendorPayable(BigDecimal shipmentVendorPayable) {
        this.shipmentVendorPayable = shipmentVendorPayable;
    }

    public BigDecimal getShipmentMarketplacePayable() {
        return shipmentMarketplacePayable;
    }

    public void setShipmentMarketplacePayable(BigDecimal shipmentMarketplacePayable) {
        this.shipmentMarketplacePayable = shipmentMarketplacePayable;
    }

    public BigDecimal getRequestedPayoutAmount() {
        return requestedPayoutAmount;
    }

    public void setRequestedPayoutAmount(BigDecimal requestedPayoutAmount) {
        this.requestedPayoutAmount = requestedPayoutAmount;
    }

    public BigDecimal getProcessedPayoutAmount() {
        return processedPayoutAmount;
    }

    public void setProcessedPayoutAmount(BigDecimal processedPayoutAmount) {
        this.processedPayoutAmount = processedPayoutAmount;
    }

    public BigDecimal getPaidPayoutAmount() {
        return paidPayoutAmount;
    }

    public void setPaidPayoutAmount(BigDecimal paidPayoutAmount) {
        this.paidPayoutAmount = paidPayoutAmount;
    }
}
