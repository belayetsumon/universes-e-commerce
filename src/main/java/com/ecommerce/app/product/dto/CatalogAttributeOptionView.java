package com.ecommerce.app.product.dto;

/**
 * 2026-05-15: Lightweight option projection used by dynamic catalog forms.
 */
public class CatalogAttributeOptionView {

    private String uuid;
    private String label;

    public CatalogAttributeOptionView() {
    }

    public CatalogAttributeOptionView(String uuid, String label) {
        this.uuid = uuid;
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
