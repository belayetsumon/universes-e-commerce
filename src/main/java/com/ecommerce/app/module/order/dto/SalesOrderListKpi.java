package com.ecommerce.app.module.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesOrderListKpi {

    private final int allOrderCount;
    private final int filteredOrderCount;
    private final long shipmentReadyCount;
    private final long shipmentCreatedCount;
    private final long shipmentBlockedCount;
    private final BigDecimal filteredGrandTotal;
    private final BigDecimal filteredVendorTotal;
    private final BigDecimal filteredCommissionTotal;
    private final BigDecimal filteredDiscountTotal;
    private final BigDecimal filteredVatTotal;
    private final BigDecimal averageOrderValue;
    private final long paidOrderCount;
    private final long unpaidOrderCount;
    private final long guestOrderCount;
    private final long cancelledOrderCount;
    private final long returnedOrderCount;
    private final List<SalesOrderStatusKpi> statusKpis;

    public SalesOrderListKpi(
            int allOrderCount,
            int filteredOrderCount,
            long shipmentReadyCount,
            long shipmentCreatedCount,
            long shipmentBlockedCount,
            BigDecimal filteredGrandTotal,
            BigDecimal filteredVendorTotal,
            BigDecimal filteredCommissionTotal,
            BigDecimal filteredDiscountTotal,
            BigDecimal filteredVatTotal,
            BigDecimal averageOrderValue,
            long paidOrderCount,
            long unpaidOrderCount,
            long guestOrderCount,
            long cancelledOrderCount,
            long returnedOrderCount,
            List<SalesOrderStatusKpi> statusKpis
    ) {
        this.allOrderCount = allOrderCount;
        this.filteredOrderCount = filteredOrderCount;
        this.shipmentReadyCount = shipmentReadyCount;
        this.shipmentCreatedCount = shipmentCreatedCount;
        this.shipmentBlockedCount = shipmentBlockedCount;
        this.filteredGrandTotal = filteredGrandTotal;
        this.filteredVendorTotal = filteredVendorTotal;
        this.filteredCommissionTotal = filteredCommissionTotal;
        this.filteredDiscountTotal = filteredDiscountTotal;
        this.filteredVatTotal = filteredVatTotal;
        this.averageOrderValue = averageOrderValue;
        this.paidOrderCount = paidOrderCount;
        this.unpaidOrderCount = unpaidOrderCount;
        this.guestOrderCount = guestOrderCount;
        this.cancelledOrderCount = cancelledOrderCount;
        this.returnedOrderCount = returnedOrderCount;
        this.statusKpis = statusKpis == null ? List.of() : statusKpis;
    }

    public int getAllOrderCount() {
        return allOrderCount;
    }

    public int getFilteredOrderCount() {
        return filteredOrderCount;
    }

    public long getShipmentReadyCount() {
        return shipmentReadyCount;
    }

    public long getShipmentCreatedCount() {
        return shipmentCreatedCount;
    }

    public long getShipmentBlockedCount() {
        return shipmentBlockedCount;
    }

    public BigDecimal getFilteredGrandTotal() {
        return filteredGrandTotal;
    }

    public BigDecimal getFilteredVendorTotal() {
        return filteredVendorTotal;
    }

    public BigDecimal getFilteredCommissionTotal() {
        return filteredCommissionTotal;
    }

    public BigDecimal getFilteredDiscountTotal() {
        return filteredDiscountTotal;
    }

    public BigDecimal getFilteredVatTotal() {
        return filteredVatTotal;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public long getPaidOrderCount() {
        return paidOrderCount;
    }

    public long getUnpaidOrderCount() {
        return unpaidOrderCount;
    }

    public long getGuestOrderCount() {
        return guestOrderCount;
    }

    public long getCancelledOrderCount() {
        return cancelledOrderCount;
    }

    public long getReturnedOrderCount() {
        return returnedOrderCount;
    }

    public List<SalesOrderStatusKpi> getStatusKpis() {
        return statusKpis;
    }
}
