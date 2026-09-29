package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class FrontendCsrfFetchContractTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "templates/front-layout-inner-page.html",
        "templates/front-layout-home.html",
        "templates/front-layout-single-product.html"
    })
    void csrfFetchProtectionLoadsBeforeActiveFrontScript(String resourcePath) throws IOException {
        String layout = read(resourcePath);
        int csrfLoader = layout.indexOf("/js/csrf-fetch.js");
        int frontScript = layout.indexOf("/assets/front/js/front-site.js");

        assertTrue(csrfLoader >= 0, "layout must load CSRF fetch protection");
        assertTrue(frontScript > csrfLoader, "CSRF fetch protection must load before front-site.js");
        assertTrue(layout.contains("name=\"_csrf\""));
        assertTrue(layout.contains("name=\"_csrf_header\""));
    }

    @Test
    void fetchWrapperAddsTokenOnlyToUnsafeSameOriginRequests() throws IOException {
        String script = read("static/js/csrf-fetch.js");

        assertTrue(script.contains("targetUrl.origin === window.location.origin"));
        assertTrue(script.contains("['POST', 'PUT', 'PATCH', 'DELETE']"));
        assertTrue(script.contains("headers.set(headerName, token)"));
    }

    @Test
    void districtSelectionPageUsesTheCsrfProtectedFrontLayout() throws IOException {
        String page = read("templates/district/select-district.html");

        assertTrue(page.contains("layout:decorate=\"~{front-layout-inner-page}\""));
        assertTrue(page.contains("layout:fragment=\"main_content\""));
    }

    private String read(String resourcePath) throws IOException {
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
