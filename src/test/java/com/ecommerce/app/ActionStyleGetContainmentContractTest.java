package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ActionStyleGetContainmentContractTest {

    @Test
    void vendorEmailVerificationConsumesTokenOnlyThroughPost() throws IOException {
        String controller = read("src/main/java/com/ecommerce/app/vendor/controller/VendorVerificationsController.java");
        int getMapping = controller.indexOf("@GetMapping(\"/verify-email\")");
        int postMapping = controller.indexOf("@PostMapping(\"/verify-email\")");
        int tokenConsumption = controller.indexOf("verificationService.verifyEmail(token)");

        assertTrue(getMapping >= 0, "Vendor email verification link must keep a GET landing route.");
        assertTrue(postMapping > getMapping, "Vendor email verification must expose a POST confirmation route.");
        assertTrue(tokenConsumption > postMapping, "GET must not consume the email verification token.");
    }

    @Test
    void vendorEmailConfirmationFormSubmitsWithCsrfAndHiddenToken() throws IOException {
        String template = read("src/main/resources/templates/vendor/verifications/confirm_email.html");

        assertTrue(template.contains("th:action=\"@{/vendorverifications/verify-email}\""));
        assertTrue(template.contains("method=\"post\""));
        assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""));
        assertTrue(template.contains("name=\"token\""));
        assertTrue(template.contains("th:value=\"${verificationToken}\""));
    }

    @Test
    void legacyReferralVerificationRoutesAreReadOnlyRedirects() throws IOException {
        for (String path : new String[]{
            "src/main/java/com/ecommerce/app/module/customer/ReferralRewards/controller/RegisterCustomerController.java",
            "src/main/java/com/ecommerce/app/module/ReferralRewards/controller/RegisterController.java"
        }) {
            String controller = read(path);
            assertTrue(controller.contains("@GetMapping(\"/verify\")"));
            assertTrue(controller.contains("legacyEmailVerificationRedirect(@RequestParam(required = false) String token"));
            assertFalse(controller.contains("public String verifyEmail(@RequestParam String token"),
                    path + " must not expose an action-named required-token GET handler.");
            assertFalse(controller.contains("usersRepository.save(users)"),
                    path + " must not keep old GET-side account mutation code.");
        }
    }

    @Test
    void legacyPasswordChangeEndpointFamilyRemainsDenyAll() throws IOException {
        String controller = read("src/main/java/com/ecommerce/app/module/user/controller/ChangePasswordController.java");

        assertTrue(controller.contains("@RequestMapping(\"/changepassword\")"));
        assertTrue(controller.contains("@PreAuthorize(\"denyAll()\")"));
        assertTrue(controller.contains("@GetMapping(value = {\"\", \"/\", \"/index\", \"/changepassword\"})"));
        assertTrue(controller.contains("@PostMapping(\"/update\")"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
