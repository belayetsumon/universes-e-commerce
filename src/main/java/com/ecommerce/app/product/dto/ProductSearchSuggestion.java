package com.ecommerce.app.product.dto;

import java.math.BigDecimal;

public record ProductSearchSuggestion(
        String type,
        String label,
        String detail,
        String url,
        String imageUrl,
        BigDecimal price,
        String badge
) {
}
