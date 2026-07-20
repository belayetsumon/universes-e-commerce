package com.ecommerce.app.module.order.dto;

import java.math.BigDecimal;

public class SalesItemTopProductChartRow {

    private final int rank;
    private final Long productId;
    private final String productTitle;
    private final Integer sku;
    private final BigDecimal quantity;
    private final BigDecimal itemTotal;
    private final int percentage;

    public SalesItemTopProductChartRow(
            int rank,
            Long productId,
            String productTitle,
            Integer sku,
            BigDecimal quantity,
            BigDecimal itemTotal,
            int percentage
    ) {
        this.rank = rank;
        this.productId = productId;
        this.productTitle = productTitle;
        this.sku = sku;
        this.quantity = quantity;
        this.itemTotal = itemTotal;
        this.percentage = percentage;
    }

    public int getRank() {
        return rank;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public Integer getSku() {
        return sku;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public int getPercentage() {
        return percentage;
    }
}
