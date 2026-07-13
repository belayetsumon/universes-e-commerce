package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.SalesOrderMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class OrderSettingsForm {

    private Long version;

    @Size(max = 20, message = "Order prefix must be 20 characters or fewer.")
    private String orderPrefix;

    @Size(max = 20, message = "Invoice prefix must be 20 characters or fewer.")
    private String invoicePrefix;

    private Boolean autoConfirmOrder;
    private Boolean autoCancelUnpaidOrder;

    private SalesOrderMode salesOrderMode;

    @Min(value = 0, message = "Cancel unpaid after minutes cannot be negative.")
    private Integer cancelOrderAfterMinutes;

    @Min(value = 0, message = "Return allowed days cannot be negative.")
    private Integer returnAllowedDays;

    @Min(value = 0, message = "Refund allowed days cannot be negative.")
    private Integer refundAllowedDays;

    public static OrderSettingsForm from(GlobalSettings settings) {
        OrderSettingsForm form = new OrderSettingsForm();
        form.setVersion(settings.getVersion());
        form.setOrderPrefix(settings.getOrderPrefix());
        form.setInvoicePrefix(settings.getInvoicePrefix());
        form.setAutoConfirmOrder(settings.getAutoConfirmOrder());
        form.setAutoCancelUnpaidOrder(settings.getAutoCancelUnpaidOrder());
        form.setSalesOrderMode(settings.getSalesOrderMode());
        form.setCancelOrderAfterMinutes(settings.getCancelOrderAfterMinutes());
        form.setReturnAllowedDays(settings.getReturnAllowedDays());
        form.setRefundAllowedDays(settings.getRefundAllowedDays());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getOrderPrefix() {
        return orderPrefix;
    }

    public void setOrderPrefix(String orderPrefix) {
        this.orderPrefix = orderPrefix;
    }

    public String getInvoicePrefix() {
        return invoicePrefix;
    }

    public void setInvoicePrefix(String invoicePrefix) {
        this.invoicePrefix = invoicePrefix;
    }

    public Boolean getAutoConfirmOrder() {
        return autoConfirmOrder;
    }

    public void setAutoConfirmOrder(Boolean autoConfirmOrder) {
        this.autoConfirmOrder = autoConfirmOrder;
    }

    public Boolean getAutoCancelUnpaidOrder() {
        return autoCancelUnpaidOrder;
    }

    public void setAutoCancelUnpaidOrder(Boolean autoCancelUnpaidOrder) {
        this.autoCancelUnpaidOrder = autoCancelUnpaidOrder;
    }

    public SalesOrderMode getSalesOrderMode() {
        return salesOrderMode;
    }

    public void setSalesOrderMode(SalesOrderMode salesOrderMode) {
        this.salesOrderMode = salesOrderMode;
    }

    public Integer getCancelOrderAfterMinutes() {
        return cancelOrderAfterMinutes;
    }

    public void setCancelOrderAfterMinutes(Integer cancelOrderAfterMinutes) {
        this.cancelOrderAfterMinutes = cancelOrderAfterMinutes;
    }

    public Integer getReturnAllowedDays() {
        return returnAllowedDays;
    }

    public void setReturnAllowedDays(Integer returnAllowedDays) {
        this.returnAllowedDays = returnAllowedDays;
    }

    public Integer getRefundAllowedDays() {
        return refundAllowedDays;
    }

    public void setRefundAllowedDays(Integer refundAllowedDays) {
        this.refundAllowedDays = refundAllowedDays;
    }
}
