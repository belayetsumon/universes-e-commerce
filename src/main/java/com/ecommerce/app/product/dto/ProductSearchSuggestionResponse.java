package com.ecommerce.app.product.dto;

import java.util.List;

public record ProductSearchSuggestionResponse(
        String query,
        List<ProductSearchSuggestion> products,
        List<ProductSearchSuggestion> categories
) {
}
