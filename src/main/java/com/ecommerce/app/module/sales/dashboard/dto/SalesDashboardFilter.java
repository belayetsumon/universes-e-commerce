package com.ecommerce.app.module.sales.dashboard.dto;

import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.PaymentMethod;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SalesDashboardFilter {

    private SalesDashboardDateRange range = SalesDashboardDateRange.LAST_30_DAYS;
    private Long vendorId;
    private Long categoryId;
    private Long brandId;
    private Long productId;
    private Long customerId;
    private Long shippingCarrierId;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private SalesDashboardChannel salesChannel;
    private String region;
    private String currency = "BDT";
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    public SalesDashboardDateRange getRange() {
        return range;
    }

    public void setRange(SalesDashboardDateRange range) {
        this.range = range == null ? SalesDashboardDateRange.LAST_30_DAYS : range;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getShippingCarrierId() { return shippingCarrierId; }
    public void setShippingCarrierId(Long shippingCarrierId) { this.shippingCarrierId = shippingCarrierId; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public SalesDashboardChannel getSalesChannel() { return salesChannel; }
    public void setSalesChannel(SalesDashboardChannel salesChannel) { this.salesChannel = salesChannel; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region == null || region.isBlank() ? null : region.trim(); }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency == null || currency.isBlank() ? "BDT" : currency.trim().toUpperCase(); }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
