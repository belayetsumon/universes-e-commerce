package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PermissionCatalogueSeedSystemToolContractTest {

    @Test
    void systemPageProvidesOneClickPermissionCatalogueSeedAction() throws IOException {
        String template = Files.readString(Path.of("src/main/resources/templates/admin/system/index.html"));
        String controller = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/system/controller/SystemEndpointController.java"));

        assertTrue(template.contains("th:action=\"@{/admin/system/seed/permission-catalogue}\""));
        assertTrue(template.contains("Install all permissions"));
        assertTrue(template.contains("no role will be granted automatically"));
        assertTrue(controller.contains("@PostMapping(\"/seed/permission-catalogue\")"));
        assertTrue(controller.contains("PlatformIamPermissions.CAN_SEED_PERMISSION_CATALOGUE"));
        assertTrue(controller.contains("@GetMapping(\"/seed/permission-catalogue\")"));
    }

    @Test
    void packagedCatalogueMatchesApprovedPermissionCatalogue() throws IOException {
        String docsCatalogue = Files.readString(Path.of("src/docs/application-security-permission-catalogue.csv"));
        String packagedCatalogue = Files.readString(Path.of("src/main/resources/security/application-security-permission-catalogue.csv"));
        long permissionRows = Files.lines(Path.of("src/main/resources/security/application-security-permission-catalogue.csv"))
                .skip(1)
                .filter(line -> !line.isBlank())
                .count();

        assertEquals(docsCatalogue, packagedCatalogue);
        assertEquals(132, permissionRows);
    }

    @Test
    void seedServiceCreatesMissingModulesAndPrivilegesBySlug() throws IOException {
        String service = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/system/services/PermissionCatalogueSeedService.java"));
        String moduleRepository = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/user/ripository/ModuleRepository.java"));
        String privilegeRepository = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/user/ripository/PrivilegeRepository.java"));

        assertTrue(service.contains("security/application-security-permission-catalogue.csv"));
        assertTrue(service.contains("findFirstBySlugIgnoreCase(seed.moduleSlug())"));
        assertTrue(service.contains("findFirstBySlugIgnoreCase(seed.permissionSlug())"));
        assertTrue(service.contains("privilege.setModule(module)"));
        assertTrue(service.contains("normalizeCsvValue(headers.get(i))"));
        assertTrue(service.contains("replace(\"\\uFEFF\", \"\")"));
        assertTrue(moduleRepository.contains("findFirstBySlugIgnoreCase"));
        assertTrue(privilegeRepository.contains("findFirstBySlugIgnoreCase"));
    }

    @Test
    void permissionCatalogueSeedHasLegacyAdminBootstrapGuard() throws IOException {
        String permissions = Files.readString(Path.of("src/main/java/com/ecommerce/app/security/permission/PlatformIamPermissions.java"));
        String authorization = Files.readString(Path.of("src/main/java/com/ecommerce/app/security/authorization/PlatformIamAuthorization.java"));

        assertTrue(permissions.contains("CAN_SEED_PERMISSION_CATALOGUE"));
        assertTrue(authorization.contains("canSeedPermissionCatalogue"));
        assertTrue(authorization.contains("canManageProtected(authentication)"));
        assertTrue(authorization.contains("isLegacyPlatformAdmin(authentication)"));
    }
}
