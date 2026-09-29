package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UserPermissionAssignmentPhaseOneContractTest {

    @Test
    void userFormShowsRolePrivilegesAndEffectivePermissionPreview() throws IOException {
        String template = Files.readString(Path.of("src/main/resources/templates/user/registrations.html"));

        assertTrue(template.contains("id=\"rolePicker\""));
        assertTrue(template.contains("th:field=\"*{role}\""));
        assertTrue(template.contains("data-permission-slug"));
        assertTrue(template.contains("id=\"roleSearch\""));
        assertTrue(template.contains("id=\"roleModuleFilter\""));
        assertTrue(template.contains("id=\"effectivePermissionList\""));
        assertTrue(template.contains("id=\"selectAllRoles\""));
    }

    @Test
    void userSaveResolvesSubmittedRolesFromTheDatabase() throws IOException {
        String controller = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/user/controller/UsersController.java"));

        assertTrue(controller.contains("roleRepository.findAllById(submittedRoleIds)"));
        assertTrue(controller.contains("target.setRole(new HashSet<>(resolvedRoles))"));
    }
}
