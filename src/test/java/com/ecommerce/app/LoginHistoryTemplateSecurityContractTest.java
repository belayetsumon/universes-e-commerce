package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LoginHistoryTemplateSecurityContractTest {

    @Test
    void loginHistoryAdminViewDoesNotExposeRawSessionIdentifiers() throws IOException {
        String source = Files.readString(Path.of("src", "main", "resources", "templates", "user", "login_history.html"));

        assertFalse(source.contains("entry.sessionId"), "Login history view must not bind the raw servlet session id.");
        assertFalse(source.contains("sessionId"), "Login history view must not expose raw session-id fields.");
        assertFalse(source.contains("IP, session, device"), "Search hint must not advertise session-id lookup.");
        assertTrue(source.contains("Audit Ref"), "The table should keep a non-secret audit reference for review work.");
    }

    @Test
    void loginHistorySearchQueryDoesNotMatchRawSessionIdentifiers() throws IOException {
        String source = Files.readString(Path.of(
                "src",
                "main",
                "java",
                "com",
                "ecommerce",
                "app",
                "module",
                "user",
                "ripository",
                "LoginHistoryRepository.java"));

        assertFalse(source.contains("lh.sessionId"), "Admin search must not query by raw servlet session id.");
    }
}