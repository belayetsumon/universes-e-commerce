package com.ecommerce.app.module.settings.form;

import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.MAX_CONFIGURABLE_FILE_SIZE_BYTES;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.MAX_CONFIGURABLE_SOURCE_DIMENSION;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ImageSettingsForm {

    @NotNull(message = "Settings version is required. Reload the page and try again.")
    private Long version;

    @NotNull(message = "Vendor logo maximum file size is required.")
    @Min(value = 1, message = "Vendor logo maximum file size must be at least 1 byte.")
    @Max(value = MAX_CONFIGURABLE_FILE_SIZE_BYTES,
            message = "Vendor logo maximum file size cannot exceed 10 MB.")
    private Long vendorLogoMaxFileSizeBytes;

    @NotNull(message = "Vendor logo maximum width is required.")
    @Min(value = 1, message = "Vendor logo maximum width must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Vendor logo maximum width cannot exceed 8000 pixels.")
    private Integer vendorLogoMaxWidth;

    @NotNull(message = "Vendor logo maximum height is required.")
    @Min(value = 1, message = "Vendor logo maximum height must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Vendor logo maximum height cannot exceed 8000 pixels.")
    private Integer vendorLogoMaxHeight;

    @NotNull(message = "Product featured image minimum file size is required.")
    @Min(value = 1, message = "Product featured image minimum file size must be at least 1 byte.")
    @Max(value = MAX_CONFIGURABLE_FILE_SIZE_BYTES,
            message = "Product featured image minimum file size cannot exceed 10 MB.")
    private Long productFeaturedImageMinFileSizeBytes;

    @NotNull(message = "Product featured image maximum file size is required.")
    @Min(value = 1, message = "Product featured image maximum file size must be at least 1 byte.")
    @Max(value = MAX_CONFIGURABLE_FILE_SIZE_BYTES,
            message = "Product featured image maximum file size cannot exceed 10 MB.")
    private Long productFeaturedImageMaxFileSizeBytes;

    @NotNull(message = "Product featured image minimum width is required.")
    @Min(value = 1, message = "Product featured image minimum width must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image minimum width cannot exceed 8000 pixels.")
    private Integer productFeaturedImageMinWidth;

    @NotNull(message = "Product featured image minimum height is required.")
    @Min(value = 1, message = "Product featured image minimum height must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image minimum height cannot exceed 8000 pixels.")
    private Integer productFeaturedImageMinHeight;

    @NotNull(message = "Product featured image maximum width is required.")
    @Min(value = 1, message = "Product featured image maximum width must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image maximum width cannot exceed 8000 pixels.")
    private Integer productFeaturedImageMaxWidth;

    @NotNull(message = "Product featured image maximum height is required.")
    @Min(value = 1, message = "Product featured image maximum height must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image maximum height cannot exceed 8000 pixels.")
    private Integer productFeaturedImageMaxHeight;

    @NotNull(message = "Product featured image output width is required.")
    @Min(value = 1, message = "Product featured image output width must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image output width cannot exceed 8000 pixels.")
    private Integer productFeaturedImageOutputMaxWidth;

    @NotNull(message = "Product featured image output height is required.")
    @Min(value = 1, message = "Product featured image output height must be at least 1 pixel.")
    @Max(value = MAX_CONFIGURABLE_SOURCE_DIMENSION,
            message = "Product featured image output height cannot exceed 8000 pixels.")
    private Integer productFeaturedImageOutputMaxHeight;

    public static ImageSettingsForm from(GlobalSettings settings) {
        ImageSettingsForm form = new ImageSettingsForm();
        form.setVersion(settings.getVersion());
        form.setVendorLogoMaxFileSizeBytes(settings.getVendorLogoMaxFileSizeBytes());
        form.setVendorLogoMaxWidth(settings.getVendorLogoMaxWidth());
        form.setVendorLogoMaxHeight(settings.getVendorLogoMaxHeight());
        form.setProductFeaturedImageMinFileSizeBytes(settings.getProductFeaturedImageMinFileSizeBytes());
        form.setProductFeaturedImageMaxFileSizeBytes(settings.getProductFeaturedImageMaxFileSizeBytes());
        form.setProductFeaturedImageMinWidth(settings.getProductFeaturedImageMinWidth());
        form.setProductFeaturedImageMinHeight(settings.getProductFeaturedImageMinHeight());
        form.setProductFeaturedImageMaxWidth(settings.getProductFeaturedImageMaxWidth());
        form.setProductFeaturedImageMaxHeight(settings.getProductFeaturedImageMaxHeight());
        form.setProductFeaturedImageOutputMaxWidth(settings.getProductFeaturedImageOutputMaxWidth());
        form.setProductFeaturedImageOutputMaxHeight(settings.getProductFeaturedImageOutputMaxHeight());
        return form;
    }

    @AssertTrue(message = "Product featured image minimum file size cannot exceed its maximum file size.")
    public boolean isProductFeaturedImageFileSizeRangeValid() {
        return productFeaturedImageMinFileSizeBytes == null
                || productFeaturedImageMaxFileSizeBytes == null
                || productFeaturedImageMinFileSizeBytes <= productFeaturedImageMaxFileSizeBytes;
    }

    @AssertTrue(message = "Product featured image minimum dimensions cannot exceed its maximum dimensions.")
    public boolean isProductFeaturedImageDimensionRangeValid() {
        return productFeaturedImageMinWidth == null
                || productFeaturedImageMinHeight == null
                || productFeaturedImageMaxWidth == null
                || productFeaturedImageMaxHeight == null
                || (productFeaturedImageMinWidth <= productFeaturedImageMaxWidth
                && productFeaturedImageMinHeight <= productFeaturedImageMaxHeight);
    }

    @AssertTrue(message = "Product featured image output dimensions cannot exceed accepted maximum dimensions.")
    public boolean isProductFeaturedImageOutputRangeValid() {
        return productFeaturedImageOutputMaxWidth == null
                || productFeaturedImageOutputMaxHeight == null
                || productFeaturedImageMaxWidth == null
                || productFeaturedImageMaxHeight == null
                || (productFeaturedImageOutputMaxWidth <= productFeaturedImageMaxWidth
                && productFeaturedImageOutputMaxHeight <= productFeaturedImageMaxHeight);
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVendorLogoMaxFileSizeBytes() {
        return vendorLogoMaxFileSizeBytes;
    }

    public void setVendorLogoMaxFileSizeBytes(Long vendorLogoMaxFileSizeBytes) {
        this.vendorLogoMaxFileSizeBytes = vendorLogoMaxFileSizeBytes;
    }

    public Integer getVendorLogoMaxWidth() {
        return vendorLogoMaxWidth;
    }

    public void setVendorLogoMaxWidth(Integer vendorLogoMaxWidth) {
        this.vendorLogoMaxWidth = vendorLogoMaxWidth;
    }

    public Integer getVendorLogoMaxHeight() {
        return vendorLogoMaxHeight;
    }

    public void setVendorLogoMaxHeight(Integer vendorLogoMaxHeight) {
        this.vendorLogoMaxHeight = vendorLogoMaxHeight;
    }

    public Long getProductFeaturedImageMinFileSizeBytes() {
        return productFeaturedImageMinFileSizeBytes;
    }

    public void setProductFeaturedImageMinFileSizeBytes(Long value) {
        this.productFeaturedImageMinFileSizeBytes = value;
    }

    public Long getProductFeaturedImageMaxFileSizeBytes() {
        return productFeaturedImageMaxFileSizeBytes;
    }

    public void setProductFeaturedImageMaxFileSizeBytes(Long value) {
        this.productFeaturedImageMaxFileSizeBytes = value;
    }

    public Integer getProductFeaturedImageMinWidth() {
        return productFeaturedImageMinWidth;
    }

    public void setProductFeaturedImageMinWidth(Integer value) {
        this.productFeaturedImageMinWidth = value;
    }

    public Integer getProductFeaturedImageMinHeight() {
        return productFeaturedImageMinHeight;
    }

    public void setProductFeaturedImageMinHeight(Integer value) {
        this.productFeaturedImageMinHeight = value;
    }

    public Integer getProductFeaturedImageMaxWidth() {
        return productFeaturedImageMaxWidth;
    }

    public void setProductFeaturedImageMaxWidth(Integer value) {
        this.productFeaturedImageMaxWidth = value;
    }

    public Integer getProductFeaturedImageMaxHeight() {
        return productFeaturedImageMaxHeight;
    }

    public void setProductFeaturedImageMaxHeight(Integer value) {
        this.productFeaturedImageMaxHeight = value;
    }

    public Integer getProductFeaturedImageOutputMaxWidth() {
        return productFeaturedImageOutputMaxWidth;
    }

    public void setProductFeaturedImageOutputMaxWidth(Integer value) {
        this.productFeaturedImageOutputMaxWidth = value;
    }

    public Integer getProductFeaturedImageOutputMaxHeight() {
        return productFeaturedImageOutputMaxHeight;
    }

    public void setProductFeaturedImageOutputMaxHeight(Integer value) {
        this.productFeaturedImageOutputMaxHeight = value;
    }
}
