package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class VendorStaffIamTemplateSecurityContractTest {

    private static final String STAFF_DELETE_PATH = "/vendor-users/delete";
    private static final String ROLE_DELETE_PATH = "/vendor-users/roles/delete";
    private static final String STAFF_DELETE_ACTION = "th:action=\"@{/vendor-users/delete";
    private static final String ROLE_DELETE_ACTION = "th:action=\"@{/vendor-users/roles/delete";
    private static final String CSRF_FIELD = "th:name=\"${_csrf.parameterName}\"";

    @Test
    void vendorStaffIamDeleteControlsUsePostFormsWithCsrf() throws IOException {
        List<Path> templates = List.of(
                Path.of("src", "main", "resources", "templates", "vendor", "users", "vendor_users_list.html"),
                Path.of("src", "main", "resources", "templates", "vendor", "users", "vendor_role_manage_list.html"));

        for (Path template : templates) {
            String source = Files.readString(template);
            assertFalse(source.contains("th:href=\"@{" + STAFF_DELETE_PATH),
                    () -> "GET vendor staff deletion remains in " + template);
            assertFalse(source.contains("th:href=\"@{" + ROLE_DELETE_PATH),
                    () -> "GET vendor role deletion remains in " + template);
            int actionCount = occurrences(source, STAFF_DELETE_ACTION)
                    + occurrences(source, ROLE_DELETE_ACTION);
            int csrfCount = occurrences(source, CSRF_FIELD);
            assertTrue(actionCount > 0, () -> "Deletion must use a POST form in " + template);
            assertTrue(csrfCount >= actionCount, () -> "Every delete form needs a CSRF field in " + template);
        }
    }

    @Test
    void vendorStaffIamMutationFormsCarryCsrfTokens() throws IOException {
        List<Path> templates = List.of(
                Path.of("src", "main", "resources", "templates", "vendor", "users", "vendor_users_form.html"),
                Path.of("src", "main", "resources", "templates", "vendor", "users", "vendor_role_manage_form.html"));

        for (Path template : templates) {
            String source = Files.readString(template);
            assertTrue(source.contains(CSRF_FIELD), () -> "Mutation form needs a CSRF field in " + template);
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
