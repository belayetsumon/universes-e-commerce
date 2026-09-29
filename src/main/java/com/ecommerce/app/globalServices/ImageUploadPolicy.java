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
        long minFileSizeBytes,
        long maxFileSizeBytes,
        int minWidth,
        int minHeight,
        int maxWidth,
        int maxHeight,
        String allowedFileDescription
) {

    public ImageUploadPolicy {
        if (minFileSizeBytes <= 0 || maxFileSizeBytes < minFileSizeBytes) {
            throw new IllegalArgumentException("Image file size range must be valid.");
        }
        if (minWidth <= 0 || minHeight <= 0 || maxWidth < minWidth || maxHeight < minHeight) {
            throw new IllegalArgumentException("Image dimension range must be valid.");
        }

        allowedContentTypes = normalize(allowedContentTypes);
        allowedExtensions = normalize(allowedExtensions);
        allowedImageFormats = normalize(allowedImageFormats);
        allowedFileDescription = allowedFileDescription == null || allowedFileDescription.isBlank()
                ? "supported image files"
                : allowedFileDescription;
    }

    public ImageUploadPolicy(
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            Set<String> allowedImageFormats,
            long maxFileSizeBytes,
            int maxWidth,
            int maxHeight,
            String allowedFileDescription) {
        this(
                allowedContentTypes,
                allowedExtensions,
                allowedImageFormats,
                1L,
                maxFileSizeBytes,
                1,
                1,
                maxWidth,
                maxHeight,
                allowedFileDescription);
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
