package com.ecommerce.app.product.model;

import java.util.Locale;

public enum AvailableDeliveryAreaMode {
    SPECIFIC_AREA("Specific Area"),
    ALL_AREA("All Area"),
    ALL_AREA_EXCEPT("All Area Except");

    private final String label;

    AvailableDeliveryAreaMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AvailableDeliveryAreaMode fromStorageValue(String value) {
        if (value == null || value.isBlank()) {
            // 2026-04-25: Keep legacy blank rows readable instead of crashing product pages.
            return SPECIFIC_AREA;
        }

        String normalized = value.trim();
        for (AvailableDeliveryAreaMode mode : values()) {
            if (mode.name().equalsIgnoreCase(normalized) || mode.label.equalsIgnoreCase(normalized)) {
                return mode;
            }
        }

        String key = normalized
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return switch (key) {
            case "SPECIFIC", "SPECIFIC_AREA", "SELECTED_AREA", "SELECTED_AREAS" -> SPECIFIC_AREA;
            case "ALL", "ALL_AREA", "ALL_AREAS" -> ALL_AREA;
            case "ALL_AREA_EXCEPT", "ALL_AREAS_EXCEPT", "EXCLUDED_AREA", "EXCLUDED_AREAS" -> ALL_AREA_EXCEPT;
            default -> throw new IllegalArgumentException("Unsupported AvailableDeliveryAreaMode value: " + value);
        };
    }
}
