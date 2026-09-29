package com.ecommerce.app.product.dto;

import com.ecommerce.app.module.settings.dto.ImageUploadSettingsSnapshot;

public final class ProductFeaturedImageRequirements {

    private final long minFileSizeBytes;
    private final long maxFileSizeBytes;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final int outputMaxWidth;
    private final int outputMaxHeight;
    private final String helpText;

    public ProductFeaturedImageRequirements(ImageUploadSettingsSnapshot settings) {
        this.minFileSizeBytes = settings.getProductFeaturedImageMinFileSizeBytes();
        this.maxFileSizeBytes = settings.getProductFeaturedImageMaxFileSizeBytes();
        this.minWidth = settings.getProductFeaturedImageMinWidth();
        this.minHeight = settings.getProductFeaturedImageMinHeight();
        this.maxWidth = settings.getProductFeaturedImageMaxWidth();
        this.maxHeight = settings.getProductFeaturedImageMaxHeight();
        this.outputMaxWidth = settings.getProductFeaturedImageOutputMaxWidth();
        this.outputMaxHeight = settings.getProductFeaturedImageOutputMaxHeight();
        this.helpText = settings.getProductFeaturedImageHelpText();
    }

    public long getMinFileSizeBytes() {
        return minFileSizeBytes;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getOutputMaxWidth() {
        return outputMaxWidth;
    }

    public int getOutputMaxHeight() {
        return outputMaxHeight;
    }

    public String getHelpText() {
        return helpText;
    }
}
