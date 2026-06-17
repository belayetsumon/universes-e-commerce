package com.ecommerce.app.product.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 2026-05-15: Attribute selector view for variant create/edit/generate flows.
 */
public class CatalogVariantSelectionView {

    private String attributeUuid;
    private String attributeCode;
    private String attributeName;
    private Boolean required;
    private String selectedOptionUuid;
    private List<CatalogAttributeOptionView> options = new ArrayList<>();

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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getSelectedOptionUuid() {
        return selectedOptionUuid;
    }

    public void setSelectedOptionUuid(String selectedOptionUuid) {
        this.selectedOptionUuid = selectedOptionUuid;
    }

    public List<CatalogAttributeOptionView> getOptions() {
        return options;
    }

    public void setOptions(List<CatalogAttributeOptionView> options) {
        this.options = options;
    }
}
