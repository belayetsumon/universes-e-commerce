package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProductFeaturedImageTemplateContractTest {

    @Test
    void adminAndVendorFormsExposeServerConfiguredFeaturedImageRequirements() throws IOException {
        String adminTemplate = read("src/main/resources/templates/product/add.html");
        String vendorTemplate = read("src/main/resources/templates/vendor/product/add.html");

        assertFormContract(adminTemplate);
        assertFormContract(vendorTemplate);
        assertTrue(vendorTemplate.contains("th:if=\"${!#strings.isEmpty(error)}\""));
    }

    @Test
    void sharedClientValidationChecksFileAndPixelBoundaries() throws IOException {
        String validationFragment = read(
                "src/main/resources/templates/product/featured-image-validation.html");

        assertTrue(validationFragment.contains("minFileSizeBytes"));
        assertTrue(validationFragment.contains("maxFileSizeBytes"));
        assertTrue(validationFragment.contains("minWidth"));
        assertTrue(validationFragment.contains("maxWidth"));
        assertTrue(validationFragment.contains("input.checkValidity()"));
    }

    @Test
    void imageBoundariesAreManagedFromTheAdministrationSettingsSection() throws IOException {
        String configuration = read("src/main/resources/application.properties");
        String settingsTemplate = read("src/main/resources/templates/admin/settings/global-settings.html");
        String vendorLogoTemplate = read("src/main/resources/templates/vendor/logo/index.html");

        assertFalse(configuration.contains("app.upload.product-featured-image"));
        assertFalse(configuration.contains("app.upload.vendor-logo"));
        assertTrue(settingsTemplate.contains("data-bs-target=\"#image\""));
        assertTrue(settingsTemplate.contains("th:action=\"@{/admin/settings/image}\""));
        assertTrue(settingsTemplate.contains("*{vendorLogoMaxFileSizeBytes}"));
        assertTrue(settingsTemplate.contains("*{productFeaturedImageMinFileSizeBytes}"));
        assertTrue(settingsTemplate.contains("*{productFeaturedImageOutputMaxHeight}"));
        assertTrue(vendorLogoTemplate.contains("vendorLogoImageRequirements.vendorLogoHelpText"));
    }

    private void assertFormContract(String template) {
        assertTrue(template.contains("data-product-featured-image"));
        assertTrue(template.contains("productFeaturedImageRequirements.helpText"));
        assertTrue(template.contains("data-min-file-size-bytes"));
        assertTrue(template.contains("data-max-file-size-bytes"));
        assertTrue(template.contains("data-min-width"));
        assertTrue(template.contains("data-min-height"));
        assertTrue(template.contains("data-max-width"));
        assertTrue(template.contains("data-max-height"));
        assertTrue(template.contains("product/featured-image-validation :: script"));
    }

    private String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
