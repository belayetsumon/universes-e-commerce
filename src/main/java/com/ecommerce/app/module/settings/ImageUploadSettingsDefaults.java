package com.ecommerce.app.module.settings;

public final class ImageUploadSettingsDefaults {

    public static final long MAX_CONFIGURABLE_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    public static final int MAX_CONFIGURABLE_SOURCE_DIMENSION = 8_000;

    public static final long VENDOR_LOGO_MAX_FILE_SIZE_BYTES = 2L * 1024L * 1024L;
    public static final int VENDOR_LOGO_MAX_WIDTH = 5_000;
    public static final int VENDOR_LOGO_MAX_HEIGHT = 5_000;
    public static final int VENDOR_LOGO_OUTPUT_WIDTH = 300;
    public static final int VENDOR_LOGO_OUTPUT_HEIGHT = 300;

    public static final long PRODUCT_FEATURED_IMAGE_MIN_FILE_SIZE_BYTES = 10L * 1024L;
    public static final long PRODUCT_FEATURED_IMAGE_MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    public static final int PRODUCT_FEATURED_IMAGE_MIN_WIDTH = 400;
    public static final int PRODUCT_FEATURED_IMAGE_MIN_HEIGHT = 300;
    public static final int PRODUCT_FEATURED_IMAGE_MAX_WIDTH = 8_000;
    public static final int PRODUCT_FEATURED_IMAGE_MAX_HEIGHT = 8_000;
    public static final int PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_WIDTH = 800;
    public static final int PRODUCT_FEATURED_IMAGE_OUTPUT_MAX_HEIGHT = 600;

    private ImageUploadSettingsDefaults() {
    }
}
