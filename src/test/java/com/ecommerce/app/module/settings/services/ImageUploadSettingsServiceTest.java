package com.ecommerce.app.module.settings.services;

import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.PRODUCT_FEATURED_IMAGE_MAX_WIDTH;
import static com.ecommerce.app.module.settings.ImageUploadSettingsDefaults.VENDOR_LOGO_MAX_FILE_SIZE_BYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.module.settings.dto.ImageUploadSettingsSnapshot;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageUploadSettingsServiceTest {

    @Mock
    private GlobalSettingsService globalSettingsService;

    @Test
    void buildsRuntimePoliciesFromPersistedSettings() {
        GlobalSettings settings = configuredSettings();
        when(globalSettingsService.getActiveSettings()).thenReturn(settings);
        ImageUploadSettingsService service = new ImageUploadSettingsService(globalSettingsService);

        ImageUploadSettingsSnapshot snapshot = service.getCurrentSettings();
        ImageUploadPolicy vendorPolicy = service.vendorLogoPolicy();
        ImageUploadPolicy productPolicy = service.productFeaturedImagePolicy(snapshot);

        assertEquals(1_500_000L, vendorPolicy.maxFileSizeBytes());
        assertEquals(3_200, vendorPolicy.maxWidth());
        assertEquals(2_400, vendorPolicy.maxHeight());
        assertEquals(2_048L, productPolicy.minFileSizeBytes());
        assertEquals(7_000_000L, productPolicy.maxFileSizeBytes());
        assertEquals(640, productPolicy.minWidth());
        assertEquals(480, productPolicy.minHeight());
        assertEquals(4_096, productPolicy.maxWidth());
        assertEquals(3_072, productPolicy.maxHeight());
        assertTrue(snapshot.getProductFeaturedImageHelpText().contains("1024 x 768"));
    }

    @Test
    void invalidPersistedPolicyFallsBackToSafeDefaults() {
        GlobalSettings settings = configuredSettings();
        settings.setProductFeaturedImageMaxWidth(8_001);
        when(globalSettingsService.getActiveSettings()).thenReturn(settings);
        ImageUploadSettingsService service = new ImageUploadSettingsService(globalSettingsService);

        ImageUploadSettingsSnapshot snapshot = service.getCurrentSettings();

        assertEquals(PRODUCT_FEATURED_IMAGE_MAX_WIDTH, snapshot.getProductFeaturedImageMaxWidth());
        assertEquals(VENDOR_LOGO_MAX_FILE_SIZE_BYTES, snapshot.getVendorLogoMaxFileSizeBytes());
    }

    private GlobalSettings configuredSettings() {
        GlobalSettings settings = new GlobalSettings();
        settings.setVendorLogoMaxFileSizeBytes(1_500_000L);
        settings.setVendorLogoMaxWidth(3_200);
        settings.setVendorLogoMaxHeight(2_400);
        settings.setProductFeaturedImageMinFileSizeBytes(2_048L);
        settings.setProductFeaturedImageMaxFileSizeBytes(7_000_000L);
        settings.setProductFeaturedImageMinWidth(640);
        settings.setProductFeaturedImageMinHeight(480);
        settings.setProductFeaturedImageMaxWidth(4_096);
        settings.setProductFeaturedImageMaxHeight(3_072);
        settings.setProductFeaturedImageOutputMaxWidth(1_024);
        settings.setProductFeaturedImageOutputMaxHeight(768);
        return settings;
    }
}
