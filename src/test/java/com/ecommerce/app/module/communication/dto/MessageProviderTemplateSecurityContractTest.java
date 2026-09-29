package com.ecommerce.app.module.communication.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MessageProviderTemplateSecurityContractTest {

    @Test
    void providerEditNeverRendersStoredCredentialValuesOrPersistenceMetadata() throws IOException {
        String template = readTemplate();

        assertFalse(template.contains("th:field=\"*{apiKey}\""));
        assertFalse(template.contains("th:field=\"*{apiSecret}\""));
        assertFalse(template.contains("th:field=\"*{configJson}\""));
        assertFalse(template.contains("th:field=\"*{uuid}\""));
        assertFalse(template.contains("th:field=\"*{version}\""));
        assertTrue(template.contains("type=\"password\" name=\"apiKey\""));
        assertTrue(template.contains("type=\"password\" name=\"apiSecret\""));
        assertTrue(template.contains("name=\"configJson\""));
        assertTrue(template.contains("*{clearApiKey}"));
        assertTrue(template.contains("*{clearApiSecret}"));
        assertTrue(template.contains("*{clearConfigJson}"));
    }

    private String readTemplate() throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/admin/communication/provider-form.html")) {
            assertNotNull(stream, "provider form template must be present");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
