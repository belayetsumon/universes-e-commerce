package com.ecommerce.app.product.dto;

/**
 * 2026-05-15: Normalized variant option projection shared by admin,
 * storefront, and inventory views.
 */
public class CatalogVariantOptionView {

    private String attributeUuid;
    private String attributeCode;
    private String attributeName;
    private String optionUuid;
    private String label;
    private String value;
    private Integer sortOrder;

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

    public String getOptionUuid() {
        return optionUuid;
    }

    public void setOptionUuid(String optionUuid) {
        this.optionUuid = optionUuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
