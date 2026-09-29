package com.ecommerce.app.module.settings.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.module.settings.form.ImageSettingsForm;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.repository.GlobalSettingsRepository;
import com.ecommerce.app.services.StorageProperties;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalSettingsServiceImageSettingsTest {

    @Mock
    private GlobalSettingsRepository repository;

    @Mock
    private VendorprofileRepository vendorprofileRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private StorageProperties storageProperties;

    private GlobalSettings existing;
    private GlobalSettingsService service;

    @BeforeEach
    void setUp() {
        existing = new GlobalSettings();
        existing.setVersion(7L);
        service = new GlobalSettingsService(repository, vendorprofileRepository, imageService, storageProperties);
    }

    @Test
    void savesEveryImagePolicyFieldFromTheAdministrationForm() {
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(GlobalSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ImageSettingsForm form = validForm();

        GlobalSettings saved = service.updateImageSettings(form);

        assertSame(existing, saved);
        assertEquals(1_500_000L, saved.getVendorLogoMaxFileSizeBytes());
        assertEquals(3_200, saved.getVendorLogoMaxWidth());
        assertEquals(2_400, saved.getVendorLogoMaxHeight());
        assertEquals(2_048L, saved.getProductFeaturedImageMinFileSizeBytes());
        assertEquals(7_000_000L, saved.getProductFeaturedImageMaxFileSizeBytes());
        assertEquals(640, saved.getProductFeaturedImageMinWidth());
        assertEquals(480, saved.getProductFeaturedImageMinHeight());
        assertEquals(4_096, saved.getProductFeaturedImageMaxWidth());
        assertEquals(3_072, saved.getProductFeaturedImageMaxHeight());
        assertEquals(1_024, saved.getProductFeaturedImageOutputMaxWidth());
        assertEquals(768, saved.getProductFeaturedImageOutputMaxHeight());
        verify(repository).save(existing);
    }

    @Test
    void rejectsAnInvalidRangeBeforeLoadingOrSavingSettings() {
        ImageSettingsForm form = validForm();
        form.setProductFeaturedImageMinFileSizeBytes(8_000L);
        form.setProductFeaturedImageMaxFileSizeBytes(4_000L);

        assertThrows(GlobalSettingsService.SettingsValidationException.class,
                () -> service.updateImageSettings(form));

        verify(repository, never()).save(any(GlobalSettings.class));
    }

    private ImageSettingsForm validForm() {
        ImageSettingsForm form = new ImageSettingsForm();
        form.setVersion(7L);
        form.setVendorLogoMaxFileSizeBytes(1_500_000L);
        form.setVendorLogoMaxWidth(3_200);
        form.setVendorLogoMaxHeight(2_400);
        form.setProductFeaturedImageMinFileSizeBytes(2_048L);
        form.setProductFeaturedImageMaxFileSizeBytes(7_000_000L);
        form.setProductFeaturedImageMinWidth(640);
        form.setProductFeaturedImageMinHeight(480);
        form.setProductFeaturedImageMaxWidth(4_096);
        form.setProductFeaturedImageMaxHeight(3_072);
        form.setProductFeaturedImageOutputMaxWidth(1_024);
        form.setProductFeaturedImageOutputMaxHeight(768);
        return form;
    }
}
