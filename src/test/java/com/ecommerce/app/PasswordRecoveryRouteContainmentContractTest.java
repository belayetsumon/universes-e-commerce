package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PasswordRecoveryRouteContainmentContractTest {

    @Test
    void recoveryControllerSeparatesDisplayAndSubmissionMethods() throws IOException {
        String source = read("src/main/java/com/ecommerce/app/module/user/controller/ForgotPasswordController.java");
        assertTrue(source.contains("@GetMapping(value = {\"\", \"/\", \"/index\", \"/userforgotpassword\"})"));
        assertTrue(source.contains("@PostMapping(\"/showemail\")"));
        assertFalse(source.contains("@RequestMapping(\"/showemail\")"));
    }

    @Test
    void publicRecoveryPageIsReadOnlyGet() throws IOException {
        String source = read("src/main/java/com/ecommerce/app/publics/controller/PublicController.java");
        assertTrue(source.contains("@GetMapping(\"/forgot-password\")"));
    }

    @Test
    void recoveryFormsSubmitWithPostAndCsrf() throws IOException {
        Map<String, String> forms = Map.of(
                "src/main/resources/templates/user/forgotpassword.html", "/forgotpassword/showemail",
                "src/main/resources/templates/frontview/forgot-password.html", "/forgotpassword/showemail",
                "src/main/resources/templates/frontview/reset-password.html", "/forgotpassword/reset"
        );
        for (Map.Entry<String, String> entry : forms.entrySet()) {
            String template = read(entry.getKey());
            assertTrue(template.contains("method=\"post\""), entry.getKey() + " must submit with POST");
            assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""),
                    entry.getKey() + " must include a CSRF hidden field");
            assertTrue(template.contains(entry.getValue()), entry.getKey() + " must include the recovery action");
        }
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
