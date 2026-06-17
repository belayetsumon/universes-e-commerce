package com.ecommerce.app.finance.dto;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VendorFinanceDashboardDto {

    private Vendorprofile vendor;
    private BigDecimal pendingAmount = BigDecimal.ZERO;
    private BigDecimal availableAmount = BigDecimal.ZERO;
    private BigDecimal reservedAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal marketplaceDueToVendor = BigDecimal.ZERO;
    private BigDecimal marketplaceShare = BigDecimal.ZERO;
    private BigDecimal requestedPayoutAmount = BigDecimal.ZERO;
    private BigDecimal processedPayoutAmount = BigDecimal.ZERO;
    private BigDecimal paidPayoutAmount = BigDecimal.ZERO;
    private List<VendorPayout> recentPayouts = new ArrayList<>();
    private List<Shipment> recentShipments = new ArrayList<>();

    public Vendorprofile getVendor() {
        return vendor;
    }

    public void setVendor(Vendorprofile vendor) {
        this.vendor = vendor;
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

    public BigDecimal getMarketplaceDueToVendor() {
        return marketplaceDueToVendor;
    }

    public void setMarketplaceDueToVendor(BigDecimal marketplaceDueToVendor) {
        this.marketplaceDueToVendor = marketplaceDueToVendor;
    }

    public BigDecimal getMarketplaceShare() {
        return marketplaceShare;
    }

    public void setMarketplaceShare(BigDecimal marketplaceShare) {
        this.marketplaceShare = marketplaceShare;
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

    public List<VendorPayout> getRecentPayouts() {
        return recentPayouts;
    }

    public void setRecentPayouts(List<VendorPayout> recentPayouts) {
        this.recentPayouts = recentPayouts;
    }

    public List<Shipment> getRecentShipments() {
        return recentShipments;
    }

    public void setRecentShipments(List<Shipment> recentShipments) {
        this.recentShipments = recentShipments;
    }
}
