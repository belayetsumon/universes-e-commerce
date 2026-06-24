package com.ecommerce.app.order.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReturnRefundReportDto {

    private long requestedCount;
    private long returnedCount;
    private long totalReturnRows;
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;
    private BigDecimal pendingRefundAmount = BigDecimal.ZERO;
    private List<ReturnRefundReportRow> rows = new ArrayList<>();

    public long getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(long requestedCount) {
        this.requestedCount = requestedCount;
    }

    public long getReturnedCount() {
        return returnedCount;
    }

    public void setReturnedCount(long returnedCount) {
        this.returnedCount = returnedCount;
    }

    public long getTotalReturnRows() {
        return totalReturnRows;
    }

    public void setTotalReturnRows(long totalReturnRows) {
        this.totalReturnRows = totalReturnRows;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(BigDecimal totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public BigDecimal getPendingRefundAmount() {
        return pendingRefundAmount;
    }

    public void setPendingRefundAmount(BigDecimal pendingRefundAmount) {
        this.pendingRefundAmount = pendingRefundAmount;
    }

    public List<ReturnRefundReportRow> getRows() {
        return rows;
    }

    public void setRows(List<ReturnRefundReportRow> rows) {
        this.rows = rows;
    }
}
