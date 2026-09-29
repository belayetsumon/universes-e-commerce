package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SessionCredentialVersionContractTest {

    @Test
    void credentialEpochIsPersistedAndWiredIntoAuthenticationAndPasswordFlows() throws IOException {
        String user = read("src/main/java/com/ecommerce/app/module/user/model/Users.java");
        String migration = read("src/main/resources/db/migration/mysql/V202608310001__credential_version_session_invalidation.sql");
        String security = read("src/main/java/com/ecommerce/app/SecurityConfig.java");
        String login = read("src/main/java/com/ecommerce/app/module/user/componant/CustomLoginSuccessHandler.java");
        String reset = read("src/main/java/com/ecommerce/app/module/user/services/PasswordResetService.java");
        String admin = read("src/main/java/com/ecommerce/app/module/user/controller/UsersController.java");
        String customer = read("src/main/java/com/ecommerce/app/module/customer/controller/CustomerProfileController.java");

        assertTrue(user.contains("credential_version"));
        assertTrue(migration.contains("ADD COLUMN credential_version BIGINT NOT NULL DEFAULT 1"));
        assertTrue(security.contains("maximumSessions(-1)"));
        assertTrue(security.contains("sessionFixation(sessionFixation -> sessionFixation.migrateSession())"));
        assertTrue(security.contains("frameOptions(frameOptions -> frameOptions.sameOrigin())"));
        assertTrue(security.contains("httpStrictTransportSecurity"));
        assertTrue(security.contains("STRICT_ORIGIN_WHEN_CROSS_ORIGIN"));
        assertTrue(security.contains("addFilterAfter(credentialVersionFilter, SecurityContextHolderFilter.class)"));
        assertTrue(login.contains("captureCurrentVersion"));
        assertTrue(reset.contains("sessionCredentialVersionService.updatePassword"));
        assertTrue(admin.contains("sessionCredentialVersionService.updatePassword"));
        assertTrue(customer.contains("sessionCredentialVersionService.updatePassword"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
