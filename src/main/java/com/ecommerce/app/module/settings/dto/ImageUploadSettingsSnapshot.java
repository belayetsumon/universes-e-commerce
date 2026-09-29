package com.ecommerce.app.module.settings.dto;

import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.MAX_CONFIGURABLE_FILE_SIZE_BYTES;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.MAX_CONFIGURABLE_SOURCE_DIMENSION;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MAX_FILE_SIZE_BYTES;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MAX_HEIGHT;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MAX_WIDTH;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MIN_FILE_SIZE_BYTES;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MIN_HEIGHT;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MIN_WIDTH;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_HEIGHT;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_WIDTH;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_MAX_FILE_SIZE_BYTES;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_MAX_HEIGHT;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_MAX_WIDTH;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_OUTPUT_HEIGHT;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_OUTPUT_WIDTH;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import java.util.Locale;

public final class ImageUploadSettingsSnapshot {

    private final long vendorLogoMaxFileSizeBytes;
    private final int vendorLogoMaxWidth;
    private final int vendorLogoMaxHeight;
    private final long productFeaturedImageMinFileSizeBytes;
    private final long productFeaturedImageMaxFileSizeBytes;
    private final int productFeaturedImageMinWidth;
    private final int productFeaturedImageMinHeight;
    private final int productFeaturedImageMaxWidth;
    private final int productFeaturedImageMaxHeight;
    private final int productFeaturedImageOutputMaxWidth;
    private final int productFeaturedImageOutputMaxHeight;

    public ImageUploadSettingsSnapshot(
            long vendorLogoMaxFileSizeBytes,
            int vendorLogoMaxWidth,
            int vendorLogoMaxHeight,
            long productFeaturedImageMinFileSizeBytes,
            long productFeaturedImageMaxFileSizeBytes,
            int productFeaturedImageMinWidth,
            int productFeaturedImageMinHeight,
            int productFeaturedImageMaxWidth,
            int productFeaturedImageMaxHeight,
            int productFeaturedImageOutputMaxWidth,
            int productFeaturedImageOutputMaxHeight) {
        this.vendorLogoMaxFileSizeBytes = vendorLogoMaxFileSizeBytes;
        this.vendorLogoMaxWidth = vendorLogoMaxWidth;
        this.vendorLogoMaxHeight = vendorLogoMaxHeight;
        this.productFeaturedImageMinFileSizeBytes = productFeaturedImageMinFileSizeBytes;
        this.productFeaturedImageMaxFileSizeBytes = productFeaturedImageMaxFileSizeBytes;
        this.productFeaturedImageMinWidth = productFeaturedImageMinWidth;
        this.productFeaturedImageMinHeight = productFeaturedImageMinHeight;
        this.productFeaturedImageMaxWidth = productFeaturedImageMaxWidth;
        this.productFeaturedImageMaxHeight = productFeaturedImageMaxHeight;
        this.productFeaturedImageOutputMaxWidth = productFeaturedImageOutputMaxWidth;
        this.productFeaturedImageOutputMaxHeight = productFeaturedImageOutputMaxHeight;
        validate();
    }

    public static ImageUploadSettingsSnapshot from(GlobalSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Global image settings are required.");
        }
        return new ImageUploadSettingsSnapshot(
                required(settings.getVendorLogoMaxFileSizeBytes()),
                required(settings.getVendorLogoMaxWidth()),
                required(settings.getVendorLogoMaxHeight()),
                required(settings.getProductFeaturedImageMinFileSizeBytes()),
                required(settings.getProductFeaturedImageMaxFileSizeBytes()),
                required(settings.getProductFeaturedImageMinWidth()),
                required(settings.getProductFeaturedImageMinHeight()),
                required(settings.getProductFeaturedImageMaxWidth()),
                required(settings.getProductFeaturedImageMaxHeight()),
                required(settings.getProductFeaturedImageOutputMaxWidth()),
                required(settings.getProductFeaturedImageOutputMaxHeight())
        );
    }

    public static ImageUploadSettingsSnapshot defaults() {
        return new ImageUploadSettingsSnapshot(
                VENDOR_LOGO_MAX_FILE_SIZE_BYTES,
                VENDOR_LOGO_MAX_WIDTH,
                VENDOR_LOGO_MAX_HEIGHT,
                PRODUCT_FEATURED_IMAGE_MIN_FILE_SIZE_BYTES,
                PRODUCT_FEATURED_IMAGE_MAX_FILE_SIZE_BYTES,
                PRODUCT_FEATURED_IMAGE_MIN_WIDTH,
                PRODUCT_FEATURED_IMAGE_MIN_HEIGHT,
                PRODUCT_FEATURED_IMAGE_MAX_WIDTH,
                PRODUCT_FEATURED_IMAGE_MAX_HEIGHT,
                PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_WIDTH,
                PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_HEIGHT
        );
    }

    private void validate() {
        if (vendorLogoMaxFileSizeBytes < 1
                || vendorLogoMaxFileSizeBytes > MAX_CONFIGURABLE_FILE_SIZE_BYTES
                || !validDimension(vendorLogoMaxWidth)
                || !validDimension(vendorLogoMaxHeight)) {
            throw new IllegalArgumentException("Vendor logo upload settings are outside the safe range.");
        }
        if (productFeaturedImageMinFileSizeBytes < 1
                || productFeaturedImageMaxFileSizeBytes < productFeaturedImageMinFileSizeBytes
                || productFeaturedImageMaxFileSizeBytes > MAX_CONFIGURABLE_FILE_SIZE_BYTES
                || !validDimension(productFeaturedImageMinWidth)
                || !validDimension(productFeaturedImageMinHeight)
                || !validDimension(productFeaturedImageMaxWidth)
                || !validDimension(productFeaturedImageMaxHeight)
                || productFeaturedImageMinWidth > productFeaturedImageMaxWidth
                || productFeaturedImageMinHeight > productFeaturedImageMaxHeight
                || !validDimension(productFeaturedImageOutputMaxWidth)
                || !validDimension(productFeaturedImageOutputMaxHeight)
                || productFeaturedImageOutputMaxWidth > productFeaturedImageMaxWidth
                || productFeaturedImageOutputMaxHeight > productFeaturedImageMaxHeight) {
            throw new IllegalArgumentException("Product featured image upload settings are outside the safe range.");
        }
    }

    private boolean validDimension(int value) {
        return value >= 1 && value <= MAX_CONFIGURABLE_SOURCE_DIMENSION;
    }

    private static long required(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("An image upload file-size setting is missing.");
        }
        return value;
    }

    private static int required(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("An image upload dimension setting is missing.");
        }
        return value;
    }

    public String getVendorLogoHelpText() {
        return String.format(
                Locale.ROOT,
                "JPG or PNG; up to %d x %d pixels; maximum %s. Stored as %d x %d WEBP.",
                vendorLogoMaxWidth,
                vendorLogoMaxHeight,
                formatBytes(vendorLogoMaxFileSizeBytes),
                VENDOR_LOGO_OUTPUT_WIDTH,
                VENDOR_LOGO_OUTPUT_HEIGHT
        );
    }

    public String getProductFeaturedImageHelpText() {
        return String.format(
                Locale.ROOT,
                "JPG, PNG, or WEBP; %d x %d to %d x %d pixels; %s to %s. Stored as WEBP up to %d x %d pixels.",
                productFeaturedImageMinWidth,
                productFeaturedImageMinHeight,
                productFeaturedImageMaxWidth,
                productFeaturedImageMaxHeight,
                formatBytes(productFeaturedImageMinFileSizeBytes),
                formatBytes(productFeaturedImageMaxFileSizeBytes),
                productFeaturedImageOutputMaxWidth,
                productFeaturedImageOutputMaxHeight
        );
    }

    private String formatBytes(long bytes) {
        if (bytes > 0 && bytes % (1024L * 1024L) == 0) {
            return (bytes / (1024L * 1024L)) + " MB";
        }
        if (bytes > 0 && bytes % 1024L == 0) {
            return (bytes / 1024L) + " KB";
        }
        return bytes + " bytes";
    }

    public long getVendorLogoMaxFileSizeBytes() {
        return vendorLogoMaxFileSizeBytes;
    }

    public int getVendorLogoMaxWidth() {
        return vendorLogoMaxWidth;
    }

    public int getVendorLogoMaxHeight() {
        return vendorLogoMaxHeight;
    }

    public long getProductFeaturedImageMinFileSizeBytes() {
        return productFeaturedImageMinFileSizeBytes;
    }

    public long getProductFeaturedImageMaxFileSizeBytes() {
        return productFeaturedImageMaxFileSizeBytes;
    }

    public int getProductFeaturedImageMinWidth() {
        return productFeaturedImageMinWidth;
    }

    public int getProductFeaturedImageMinHeight() {
        return productFeaturedImageMinHeight;
    }

    public int getProductFeaturedImageMaxWidth() {
        return productFeaturedImageMaxWidth;
    }

    public int getProductFeaturedImageMaxHeight() {
        return productFeaturedImageMaxHeight;
    }

    public int getProductFeaturedImageOutputMaxWidth() {
        return productFeaturedImageOutputMaxWidth;
    }

    public int getProductFeaturedImageOutputMaxHeight() {
        return productFeaturedImageOutputMaxHeight;
    }
}
