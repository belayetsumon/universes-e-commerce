package com.ecommerce.app.product.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 2026-05-15: Combined storefront discovery result for search plus dynamic
 * filter rendering.
 */
public class CatalogDiscoveryResult {

    private List<Map<String, Object>> filteredProducts = new ArrayList<>();
    private List<CatalogFilterGroupView> filterGroups = new ArrayList<>();

    public List<Map<String, Object>> getFilteredProducts() {
        return filteredProducts;
    }

    public void setFilteredProducts(List<Map<String, Object>> filteredProducts) {
        this.filteredProducts = filteredProducts;
    }

    public List<CatalogFilterGroupView> getFilterGroups() {
        return filterGroups;
    }

    public void setFilterGroups(List<CatalogFilterGroupView> filterGroups) {
        this.filterGroups = filterGroups;
    }
}
