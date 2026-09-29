package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FraudConfigurationTemplateSecurityContractTest {

    @Test
    void sensitiveConfigurationUsesAWriteOnlyControlWithExplicitClear() throws IOException {
        String template = readTemplate();

        assertTrue(template.contains("th:if=\"*{sensitive}\""));
        assertTrue(template.contains("type=\"password\" name=\"configValue\""));
        assertTrue(template.contains("*{clearConfigValue}"));
        assertTrue(template.contains("stored value is never displayed"));
        assertFalse(template.contains("th:value=\"*{configValue}\""));
    }

    private String readTemplate() throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/admin/fraud/configuration.html")) {
            assertNotNull(stream, "fraud configuration template must be present");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
