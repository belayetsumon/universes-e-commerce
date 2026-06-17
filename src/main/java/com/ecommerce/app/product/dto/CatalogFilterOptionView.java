package com.ecommerce.app.product.dto;

/**
 * 2026-05-15: Dynamic storefront filter option with selection state and count.
 */
public class CatalogFilterOptionView {

    private String optionUuid;
    private String label;
    private Long count;
    private Boolean selected;

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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
