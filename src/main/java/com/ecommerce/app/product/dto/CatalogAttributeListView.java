package com.ecommerce.app.product.dto;

import com.ecommerce.app.product.model.AttributeInputType;
import com.ecommerce.app.product.model.AttributeValueType;
import java.util.ArrayList;
import java.util.List;

/**
 * 2026-05-25: Lightweight admin list row for catalog attributes with usage
 * counts and filter-friendly fields.
 */
public class CatalogAttributeListView {

    private Long id;
    private String uuid;
    private String name;
    private String description;
    private AttributeInputType inputType;
    private AttributeValueType valueType;
    private boolean allowMultipleValues;
    private boolean variantCapable;
    private boolean filterable;
    private boolean searchable;
    private boolean comparable;
    private boolean active;
    private long categoryMappingCount;
    private List<String> categoryNames = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttributeInputType getInputType() {
        return inputType;
    }

    public void setInputType(AttributeInputType inputType) {
        this.inputType = inputType;
    }

    public AttributeValueType getValueType() {
        return valueType;
    }

    public void setValueType(AttributeValueType valueType) {
        this.valueType = valueType;
    }

    public boolean isAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }

    public boolean isVariantCapable() {
        return variantCapable;
    }

    public void setVariantCapable(boolean variantCapable) {
        this.variantCapable = variantCapable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isComparable() {
        return comparable;
    }

    public void setComparable(boolean comparable) {
        this.comparable = comparable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCategoryMappingCount() {
        return categoryMappingCount;
    }

    public void setCategoryMappingCount(long categoryMappingCount) {
        this.categoryMappingCount = categoryMappingCount;
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }

    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames == null ? new ArrayList<>() : new ArrayList<>(categoryNames);
    }
}
