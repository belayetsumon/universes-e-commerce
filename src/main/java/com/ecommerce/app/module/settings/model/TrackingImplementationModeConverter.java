package com.ecommerce.app.module.settings.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TrackingImplementationModeConverter implements AttributeConverter<TrackingImplementationMode, String> {

    @Override
    public String convertToDatabaseColumn(TrackingImplementationMode attribute) {
        return attribute == null ? TrackingImplementationMode.DIRECT.name() : attribute.name();
    }

    @Override
    public TrackingImplementationMode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return TrackingImplementationMode.DIRECT;
        }
        try {
            return TrackingImplementationMode.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            return TrackingImplementationMode.DIRECT;
        }
    }
}
