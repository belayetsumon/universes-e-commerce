package com.ecommerce.app.module.order.repository;

import java.math.BigDecimal;

public interface SalesItemTopProductProjection {

    Long getProductId();

    String getProductTitle();

    Integer getSku();

    BigDecimal getQuantity();

    BigDecimal getItemTotal();
}
