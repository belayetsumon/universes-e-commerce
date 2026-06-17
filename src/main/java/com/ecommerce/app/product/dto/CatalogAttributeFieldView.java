package com.ecommerce.app.product.dto;

import com.ecommerce.app.product.model.AttributeInputType;
import com.ecommerce.app.product.model.AttributeValueType;
import java.util.ArrayList;
import java.util.List;

/**
 * 2026-05-15: View model for rendering category-driven product attributes in
 * admin and vendor product forms.
 */
public class CatalogAttributeFieldView {

    private String mappingUuid;
    private String attributeUuid;
    private String attributeName;
    private String attributeCode;
    private String attributeGroup;
    private String helperText;
    private AttributeInputType inputType;
    private AttributeValueType valueType;
    private Boolean required;
    private Boolean allowMultipleValues;
    private Boolean variantAttribute;
    private String scalarValue;
    private String multiLineValue;
    private List<String> selectedOptionUuids = new ArrayList<>();
    private List<CatalogAttributeOptionView> options = new ArrayList<>();

    public String getMappingUuid() {
        return mappingUuid;
    }

    public void setMappingUuid(String mappingUuid) {
        this.mappingUuid = mappingUuid;
    }

    public String getAttributeUuid() {
        return attributeUuid;
    }

    public void setAttributeUuid(String attributeUuid) {
        this.attributeUuid = attributeUuid;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(String attributeCode) {
        this.attributeCode = attributeCode;
    }

    public String getAttributeGroup() {
        return attributeGroup;
    }

    public void setAttributeGroup(String attributeGroup) {
        this.attributeGroup = attributeGroup;
    }

    public String getHelperText() {
        return helperText;
    }

    public void setHelperText(String helperText) {
        this.helperText = helperText;
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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(Boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }

    public Boolean getVariantAttribute() {
        return variantAttribute;
    }

    public void setVariantAttribute(Boolean variantAttribute) {
        this.variantAttribute = variantAttribute;
    }

    public String getScalarValue() {
        return scalarValue;
    }

    public void setScalarValue(String scalarValue) {
        this.scalarValue = scalarValue;
    }

    public String getMultiLineValue() {
        return multiLineValue;
    }

    public void setMultiLineValue(String multiLineValue) {
        this.multiLineValue = multiLineValue;
    }

    public List<String> getSelectedOptionUuids() {
        return selectedOptionUuids;
    }

    public void setSelectedOptionUuids(List<String> selectedOptionUuids) {
        this.selectedOptionUuids = selectedOptionUuids;
    }

    public List<CatalogAttributeOptionView> getOptions() {
        return options;
    }

    public void setOptions(List<CatalogAttributeOptionView> options) {
        this.options = options;
    }
}
