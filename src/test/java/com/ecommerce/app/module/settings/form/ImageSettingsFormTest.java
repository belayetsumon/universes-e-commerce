package com.ecommerce.app.module.settings.form;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ImageSettingsFormTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void defaultPolicyIsValid() {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(1L);

        assertTrue(validator.validate(ImageSettingsForm.from(settings)).isEmpty());
    }

    @Test
    void rejectsInvertedFileDimensionAndOutputRanges() {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(1L);
        ImageSettingsForm form = ImageSettingsForm.from(settings);
        form.setProductFeaturedImageMinFileSizeBytes(2_000L);
        form.setProductFeaturedImageMaxFileSizeBytes(1_000L);
        form.setProductFeaturedImageMinWidth(900);
        form.setProductFeaturedImageMaxWidth(800);
        form.setProductFeaturedImageOutputMaxHeight(700);
        form.setProductFeaturedImageMaxHeight(600);

        var violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getMessage().contains("minimum file size")));
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getMessage().contains("minimum dimensions")));
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getMessage().contains("output dimensions")));
    }
}
