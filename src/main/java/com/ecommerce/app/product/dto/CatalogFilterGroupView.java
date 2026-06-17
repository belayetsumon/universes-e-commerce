package com.ecommerce.app.product.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 2026-05-15: Dynamic storefront filter group derived from mapped attributes.
 */
public class CatalogFilterGroupView {

    private String attributeUuid;
    private String attributeCode;
    private String attributeName;
    private List<String> selectedOptionUuids = new ArrayList<>();
    private List<CatalogFilterOptionView> options = new ArrayList<>();

    public String getAttributeUuid() {
        return attributeUuid;
    }

    public void setAttributeUuid(String attributeUuid) {
        this.attributeUuid = attributeUuid;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(String attributeCode) {
        this.attributeCode = attributeCode;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public List<String> getSelectedOptionUuids() {
        return selectedOptionUuids;
    }

    public void setSelectedOptionUuids(List<String> selectedOptionUuids) {
        this.selectedOptionUuids = selectedOptionUuids;
    }

    public List<CatalogFilterOptionView> getOptions() {
        return options;
    }

    public void setOptions(List<CatalogFilterOptionView> options) {
        this.options = options;
    }
}
