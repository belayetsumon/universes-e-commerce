package com.ecommerce.app.module.settings.services;

import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.module.settings.dto.ImageUploadSettingsSnapshot;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageUploadSettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploadSettingsService.class);

    private final GlobalSettingsService globalSettingsService;

    public ImageUploadSettingsService(GlobalSettingsService globalSettingsService) {
        this.globalSettingsService = globalSettingsService;
    }

    public ImageUploadSettingsSnapshot getCurrentSettings() {
        try {
            return ImageUploadSettingsSnapshot.from(globalSettingsService.getActiveSettings());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Persisted image upload settings are invalid; safe defaults will be used until an administrator corrects them.", ex);
            return ImageUploadSettingsSnapshot.defaults();
        }
    }

    public ImageUploadPolicy vendorLogoPolicy() {
        ImageUploadSettingsSnapshot settings = getCurrentSettings();
        return new ImageUploadPolicy(
                Set.of("image/jpeg", "image/png"),
                Set.of("jpg", "jpeg", "png"),
                Set.of("jpeg", "png"),
                settings.getVendorLogoMaxFileSizeBytes(),
                settings.getVendorLogoMaxWidth(),
                settings.getVendorLogoMaxHeight(),
                "JPG or PNG images"
        );
    }

    public ImageUploadPolicy productFeaturedImagePolicy(ImageUploadSettingsSnapshot settings) {
        return new ImageUploadPolicy(
                Set.of("image/jpeg", "image/png", "image/webp"),
                Set.of("jpg", "jpeg", "png", "webp"),
                Set.of("jpeg", "png", "webp"),
                settings.getProductFeaturedImageMinFileSizeBytes(),
                settings.getProductFeaturedImageMaxFileSizeBytes(),
                settings.getProductFeaturedImageMinWidth(),
                settings.getProductFeaturedImageMinHeight(),
                settings.getProductFeaturedImageMaxWidth(),
                settings.getProductFeaturedImageMaxHeight(),
                "JPG, PNG, or WEBP images"
        );
    }
}
