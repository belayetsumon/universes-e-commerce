package com.ecommerce.app.finance.dto;

import com.ecommerce.app.module.shipping.model.Shipment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminFinanceDashboardDto {

    private BigDecimal totalVendorPending = BigDecimal.ZERO;
    private BigDecimal totalVendorAvailable = BigDecimal.ZERO;
    private BigDecimal totalVendorReserved = BigDecimal.ZERO;
    private BigDecimal totalVendorPaid = BigDecimal.ZERO;
    private BigDecimal totalMarketplaceDueToVendors = BigDecimal.ZERO;
    private BigDecimal totalMarketplaceShare = BigDecimal.ZERO;
    private BigDecimal totalRequestedPayout = BigDecimal.ZERO;
    private BigDecimal totalProcessedPayout = BigDecimal.ZERO;
    private BigDecimal totalPaidPayout = BigDecimal.ZERO;
    private List<AdminVendorDueRow> vendorDueRows = new ArrayList<>();
    private List<Shipment> recentSettlements = new ArrayList<>();

    public BigDecimal getTotalVendorPending() {
        return totalVendorPending;
    }

    public void setTotalVendorPending(BigDecimal totalVendorPending) {
        this.totalVendorPending = totalVendorPending;
    }

    public BigDecimal getTotalVendorAvailable() {
        return totalVendorAvailable;
    }

    public void setTotalVendorAvailable(BigDecimal totalVendorAvailable) {
        this.totalVendorAvailable = totalVendorAvailable;
    }

    public BigDecimal getTotalVendorPaid() {
        return totalVendorPaid;
    }

    public void setTotalVendorPaid(BigDecimal totalVendorPaid) {
        this.totalVendorPaid = totalVendorPaid;
    }

    public BigDecimal getTotalVendorReserved() {
        return totalVendorReserved;
    }

    public void setTotalVendorReserved(BigDecimal totalVendorReserved) {
        this.totalVendorReserved = totalVendorReserved;
    }

    public BigDecimal getTotalMarketplaceDueToVendors() {
        return totalMarketplaceDueToVendors;
    }

    public void setTotalMarketplaceDueToVendors(BigDecimal totalMarketplaceDueToVendors) {
        this.totalMarketplaceDueToVendors = totalMarketplaceDueToVendors;
    }

    public BigDecimal getTotalMarketplaceShare() {
        return totalMarketplaceShare;
    }

    public void setTotalMarketplaceShare(BigDecimal totalMarketplaceShare) {
        this.totalMarketplaceShare = totalMarketplaceShare;
    }

    public BigDecimal getTotalRequestedPayout() {
        return totalRequestedPayout;
    }

    public void setTotalRequestedPayout(BigDecimal totalRequestedPayout) {
        this.totalRequestedPayout = totalRequestedPayout;
    }

    public BigDecimal getTotalProcessedPayout() {
        return totalProcessedPayout;
    }

    public void setTotalProcessedPayout(BigDecimal totalProcessedPayout) {
        this.totalProcessedPayout = totalProcessedPayout;
    }

    public BigDecimal getTotalPaidPayout() {
        return totalPaidPayout;
    }

    public void setTotalPaidPayout(BigDecimal totalPaidPayout) {
        this.totalPaidPayout = totalPaidPayout;
    }

    public List<AdminVendorDueRow> getVendorDueRows() {
        return vendorDueRows;
    }

    public void setVendorDueRows(List<AdminVendorDueRow> vendorDueRows) {
        this.vendorDueRows = vendorDueRows;
    }

    public List<Shipment> getRecentSettlements() {
        return recentSettlements;
    }

    public void setRecentSettlements(List<Shipment> recentSettlements) {
        this.recentSettlements = recentSettlements;
    }
}
