package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminVendorIamTemplateSecurityContractTest {

    private static final String ROLE_DELETE_PATH = "/adminvendorusers/role_delete";
    private static final String PRIVILEGE_DELETE_PATH = "/adminvendorusers/privileges_delete";
    private static final String ROLE_DELETE_ACTION = "th:action=\"@{/adminvendorusers/role_delete";
    private static final String PRIVILEGE_DELETE_ACTION = "th:action=\"@{/adminvendorusers/privileges_delete";
    private static final String CSRF_FIELD = "th:name=\"${_csrf.parameterName}\"";

    @Test
    void everyAdminVendorIamDeleteControlUsesAPostFormWithCsrf() throws IOException {
        Path templateRoot = Path.of("src", "main", "resources", "templates");
        List<Path> affectedTemplates;
        try (var paths = Files.walk(templateRoot)) {
            affectedTemplates = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .filter(path -> contains(path, ROLE_DELETE_PATH) || contains(path, PRIVILEGE_DELETE_PATH))
                    .toList();
        }

        assertFalse(affectedTemplates.isEmpty(), "Admin vendor IAM delete controls must remain discoverable.");
        for (Path template : affectedTemplates) {
            String source = Files.readString(template);
            assertFalse(source.contains("th:href=\"@{" + ROLE_DELETE_PATH),
                    () -> "GET role deletion remains in " + template);
            assertFalse(source.contains("th:href=\"@{" + PRIVILEGE_DELETE_PATH),
                    () -> "GET privilege deletion remains in " + template);
            int actionCount = occurrences(source, ROLE_DELETE_ACTION)
                    + occurrences(source, PRIVILEGE_DELETE_ACTION);
            int csrfCount = occurrences(source, CSRF_FIELD);
            assertTrue(actionCount > 0, () -> "Deletion must use a POST form in " + template);
            assertTrue(csrfCount >= actionCount, () -> "Every delete form needs a CSRF field in " + template);
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
