package com.ecommerce.app.product.dto;

/**
 * 2026-05-15: Read model for specification tables on admin, vendor, and
 * storefront product detail pages.
 */
public class ProductSpecificationView {

    private String attributeGroup;
    private String attributeName;
    private String value;

    public ProductSpecificationView() {
    }

    public ProductSpecificationView(String attributeGroup, String attributeName, String value) {
        this.attributeGroup = attributeGroup;
        this.attributeName = attributeName;
        this.value = value;
    }

    public String getAttributeGroup() {
        return attributeGroup;
    }

    public void setAttributeGroup(String attributeGroup) {
        this.attributeGroup = attributeGroup;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
