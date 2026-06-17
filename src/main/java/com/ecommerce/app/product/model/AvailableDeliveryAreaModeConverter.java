package com.ecommerce.app.product.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AvailableDeliveryAreaModeConverter implements AttributeConverter<AvailableDeliveryAreaMode, String> {

    @Override
    public String convertToDatabaseColumn(AvailableDeliveryAreaMode attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AvailableDeliveryAreaMode convertToEntityAttribute(String dbData) {
        return AvailableDeliveryAreaMode.fromStorageValue(dbData);
    }
}
