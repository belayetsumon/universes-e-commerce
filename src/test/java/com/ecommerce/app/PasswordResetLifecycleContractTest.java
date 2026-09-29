package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PasswordResetLifecycleContractTest {

    @Test
    void lifecycleUsesHashedExpiringSingleUseCapabilities() throws IOException {
        String service = read("src/main/java/com/ecommerce/app/module/user/services/PasswordResetService.java");
        String token = read("src/main/java/com/ecommerce/app/module/user/model/PasswordResetToken.java");
        String repository = read("src/main/java/com/ecommerce/app/module/user/ripository/PasswordResetTokenRepository.java");
        assertTrue(service.contains("MessageDigest.getInstance(\"SHA-256\")"));
        assertTrue(service.contains("secureRandom.nextBytes(bytes)"));
        assertTrue(service.contains("tokenRepository.consumeIfValid"));
        assertTrue(token.contains("token_hash"));
        assertTrue(token.contains("expires_at"));
        assertTrue(token.contains("used_at"));
        assertTrue(repository.contains("token.usedAt is null"));
        assertTrue(repository.contains("token.expiresAt > :now"));
        assertFalse(service.contains("setToken(rawToken)"));
    }

    @Test
    void resetRoutesAreExplicitAndCsrfCovered() throws IOException {
        String controller = read("src/main/java/com/ecommerce/app/module/user/controller/ForgotPasswordController.java");
        String security = read("src/main/java/com/ecommerce/app/SecurityConfig.java");
        String template = read("src/main/resources/templates/frontview/reset-password.html");
        String migration = read("src/main/resources/db/migration/mysql/V202608300001__password_reset_tokens.sql");
        assertTrue(controller.contains("@GetMapping(\"/reset\")"));
        assertTrue(controller.contains("@PostMapping(\"/reset\")"));
        assertTrue(controller.contains("app.security.password-reset.base-url"));
        assertTrue(security.contains("new AntPathRequestMatcher(\"/forgotpassword/**\")"));
        assertTrue(security.contains("\"/forgotpassword/reset\""));
        assertFalse(security.contains("\"/forgotpassword/**\",\n"));
        assertTrue(template.contains("method=\"post\""));
        assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""));
        assertTrue(migration.contains("token_hash CHAR(64) NOT NULL"));
        assertTrue(migration.contains("FOREIGN KEY (user_id) REFERENCES usermodule_users(id)"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
