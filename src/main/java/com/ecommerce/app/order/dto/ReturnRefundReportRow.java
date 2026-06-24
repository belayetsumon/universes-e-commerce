package com.ecommerce.app.order.dto;

import com.ecommerce.app.order.model.OrderItemReturnStatus;
import com.ecommerce.app.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReturnRefundReportRow {

    private Long orderId;
    private String orderCode;
    private OrderStatus orderStatus;
    private String customerName;
    private String customerEmail;
    private Long vendorId;
    private String vendorName;
    private Long orderItemId;
    private String productTitle;
    private String variantSummary;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal itemTotal = BigDecimal.ZERO;
    private OrderItemReturnStatus returnStatus;
    private LocalDateTime returnRequestedAt;
    private LocalDateTime returnedAt;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private String requestRemark;
    private String processedRemark;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(String variantSummary) {
        this.variantSummary = variantSummary;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    public OrderItemReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(OrderItemReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public LocalDateTime getReturnRequestedAt() {
        return returnRequestedAt;
    }

    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) {
        this.returnRequestedAt = returnRequestedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRequestRemark() {
        return requestRemark;
    }

    public void setRequestRemark(String requestRemark) {
        this.requestRemark = requestRemark;
    }

    public String getProcessedRemark() {
        return processedRemark;
    }

    public void setProcessedRemark(String processedRemark) {
        this.processedRemark = processedRemark;
    }
}
