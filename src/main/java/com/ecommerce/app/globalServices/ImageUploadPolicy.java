package com.ecommerce.app.globalServices;

import java.util.Locale;
import java.util.Set;

/**
 * Immutable server-side requirements for an uploaded raster image.
 */
public record ImageUploadPolicy(
        Set<String> allowedContentTypes,
        Set<String> allowedExtensions,
        Set<String> allowedImageFormats,
        long maxFileSizeBytes,
        int maxWidth,
        int maxHeight,
        String allowedFileDescription
) {

    public ImageUploadPolicy {
        if (maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException("Maximum file size must be greater than zero.");
        }
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("Maximum image dimensions must be greater than zero.");
        }

        allowedContentTypes = normalize(allowedContentTypes);
        allowedExtensions = normalize(allowedExtensions);
        allowedImageFormats = normalize(allowedImageFormats);
        allowedFileDescription = allowedFileDescription == null || allowedFileDescription.isBlank()
                ? "supported image files"
                : allowedFileDescription;
    }

    private static Set<String> normalize(Set<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed image value is required.");
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
