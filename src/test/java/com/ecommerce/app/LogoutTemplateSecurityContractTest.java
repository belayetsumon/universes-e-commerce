package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogoutTemplateSecurityContractTest {

    private static final String LOGOUT_PATH = "/users/logout";
    private static final String LOGOUT_ACTION = "th:action=\"@{/users/logout}\"";
    private static final String LEGACY_LOGOUT_LINK = "th:href=\"@{/users/logout}\"";
    private static final String CSRF_FIELD = "th:name=\"${_csrf.parameterName}\"";

    @Test
    void everyLogoutControlUsesAPostFormWithCsrf() throws IOException {
        Path templateRoot = Path.of("src", "main", "resources", "templates");
        List<Path> logoutTemplates;
        try (var paths = Files.walk(templateRoot)) {
            logoutTemplates = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .filter(path -> contains(path, LOGOUT_PATH))
                    .toList();
        }

        assertFalse(logoutTemplates.isEmpty(), "At least one logout control must remain available.");
        for (Path template : logoutTemplates) {
            String source = Files.readString(template);
            assertFalse(source.contains(LEGACY_LOGOUT_LINK), () -> "GET logout link remains in " + template);
            int actionCount = occurrences(source, LOGOUT_ACTION);
            int csrfCount = occurrences(source, CSRF_FIELD);
            assertTrue(actionCount > 0, () -> "Logout must use a POST form in " + template);
            assertTrue(csrfCount >= actionCount, () -> "Every logout form needs a CSRF field in " + template);
        }
    }

    private boolean contains(Path path, String value) {
        try {
            return Files.readString(path).contains(value);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect template " + path, exception);
        }
    }

    private int occurrences(String source, String value) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(value, index)) >= 0) {
            count++;
            index += value.length();
        }
        return count;
    }
}
