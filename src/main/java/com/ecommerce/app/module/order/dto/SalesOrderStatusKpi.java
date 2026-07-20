package com.ecommerce.app.module.order.dto;

import com.ecommerce.app.module.order.model.OrderStatus;

public class SalesOrderStatusKpi {

    private final OrderStatus status;
    private final String label;
    private final long count;
    private final boolean selected;

    public SalesOrderStatusKpi(OrderStatus status, String label, long count, boolean selected) {
        this.status = status;
        this.label = label;
        this.count = count;
        this.selected = selected;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getLabel() {
        return label;
    }

    public long getCount() {
        return count;
    }

    public boolean isSelected() {
        return selected;
    }
}
