package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.module.settings.services.ImageUploadSettingsService;
import com.ecommerce.app.product.dto.ProductFeaturedImageRequirements;
import com.ecommerce.app.product.services.ProductImageStorageService;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductImageStorageServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private GlobalSettingsService globalSettingsService;

    private GlobalSettings settings;
    private ProductImageStorageService service;
    private MockMultipartFile image;

    @BeforeEach
    void setUp() {
        settings = new GlobalSettings();
        settings.setProductFeaturedImageMinFileSizeBytes(2_048L);
        settings.setProductFeaturedImageMaxFileSizeBytes(5_242_880L);
        settings.setProductFeaturedImageMinWidth(640);
        settings.setProductFeaturedImageMinHeight(480);
        settings.setProductFeaturedImageMaxWidth(4_096);
        settings.setProductFeaturedImageMaxHeight(3_072);
        settings.setProductFeaturedImageOutputMaxWidth(1_024);
        settings.setProductFeaturedImageOutputMaxHeight(768);
        org.mockito.Mockito.lenient().when(globalSettingsService.getActiveSettings()).thenReturn(settings);
        service = new ProductImageStorageService(
                imageService,
                new ImageUploadSettingsService(globalSettingsService));
        image = new MockMultipartFile("pic", "featured.png", "image/png", new byte[2_048]);
    }

    @Test
    void featuredImageUsesEveryConfiguredBoundaryAndOutputDimension() throws IOException {
        when(imageService.resizeAndUploadHighQualityWebp(
                eq(image), any(ImageUploadPolicy.class), eq(1_024), eq(768), eq("")))
                .thenReturn("featured.webp");

        assertEquals("featured.webp", service.storeFeaturedProductImage(image));

        ArgumentCaptor<ImageUploadPolicy> policyCaptor = ArgumentCaptor.forClass(ImageUploadPolicy.class);
        verify(imageService).resizeAndUploadHighQualityWebp(
                eq(image), policyCaptor.capture(), eq(1_024), eq(768), eq(""));
        ImageUploadPolicy policy = policyCaptor.getValue();
        assertEquals(2_048, policy.minFileSizeBytes());
        assertEquals(5_242_880, policy.maxFileSizeBytes());
        assertEquals(640, policy.minWidth());
        assertEquals(480, policy.minHeight());
        assertEquals(4_096, policy.maxWidth());
        assertEquals(3_072, policy.maxHeight());
    }

    @Test
    void requirementsPresentedToFormsMatchConfiguredValues() {
        ProductFeaturedImageRequirements requirements = service.getFeaturedImageRequirements();

        assertEquals(2_048, requirements.getMinFileSizeBytes());
        assertEquals(5_242_880, requirements.getMaxFileSizeBytes());
        assertEquals(640, requirements.getMinWidth());
        assertEquals(480, requirements.getMinHeight());
        assertEquals(4_096, requirements.getMaxWidth());
        assertEquals(3_072, requirements.getMaxHeight());
        assertEquals(1_024, requirements.getOutputMaxWidth());
        assertEquals(768, requirements.getOutputMaxHeight());
    }

    @Test
    void galleryImageRetainsItsExistingIndependentPolicy() throws IOException {
        when(imageService.resizeAndUploadHighQualityWebp(
                eq(image), any(ImageUploadPolicy.class), eq(800), eq(600), eq("")))
                .thenReturn("gallery.webp");

        assertEquals("gallery.webp", service.storeProductImage(image));

        ArgumentCaptor<ImageUploadPolicy> policyCaptor = ArgumentCaptor.forClass(ImageUploadPolicy.class);
        verify(imageService).resizeAndUploadHighQualityWebp(
                eq(image), policyCaptor.capture(), eq(800), eq(600), eq(""));
        ImageUploadPolicy policy = policyCaptor.getValue();
        assertEquals(1, policy.minFileSizeBytes());
        assertEquals(10L * 1024L * 1024L, policy.maxFileSizeBytes());
        assertEquals(1, policy.minWidth());
        assertEquals(1, policy.minHeight());
        assertEquals(8_000, policy.maxWidth());
        assertEquals(8_000, policy.maxHeight());
    }
}
