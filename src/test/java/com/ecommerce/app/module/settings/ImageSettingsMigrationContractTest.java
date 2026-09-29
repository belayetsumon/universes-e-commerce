package com.ecommerce.app.module.settings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ImageSettingsMigrationContractTest {

    private static final List<String> REQUIRED_POLICY_COLUMNS = List.of(
            "vendor_logo_max_file_size_bytes",
            "vendor_logo_max_width",
            "vendor_logo_max_height",
            "product_featured_image_min_file_size_bytes",
            "product_featured_image_max_file_size_bytes",
            "product_featured_image_min_width",
            "product_featured_image_min_height",
            "product_featured_image_max_width",
            "product_featured_image_max_height",
            "product_featured_image_output_max_width",
            "product_featured_image_output_max_height"
    );

    @Test
    void mysqlMigrationCarriesTheGuardedPolicy() throws IOException {
        String database = "mysql";
        String sql = read("db/migration/mysql/V202608260006__image_upload_settings.sql");

        REQUIRED_POLICY_COLUMNS.forEach(column -> assertTrue(sql.contains(column),
                () -> database + " migration is missing " + column));
        assertTrue(sql.contains("ck_global_settings_image_upload_policy"));
        assertTrue(sql.contains("DEFAULT 2097152"));
        assertTrue(sql.contains("DEFAULT 10485760"));
        assertTrue(sql.contains("BETWEEN 1 AND 10485760"));
        assertTrue(sql.contains("product_featured_image_output_max_width BETWEEN 1"));
    }

    private String read(String resourcePath) throws IOException {
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
