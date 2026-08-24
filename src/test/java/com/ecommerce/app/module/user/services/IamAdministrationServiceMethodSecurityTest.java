package com.ecommerce.app.module.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.model.Modules;
import com.ecommerce.app.module.user.model.Privilege;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.ripository.ModuleRepository;
import com.ecommerce.app.module.user.ripository.PrivilegeRepository;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.security.authorization.PlatformIamAuthorization;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IamAdministrationServiceMethodSecurityTest.TestConfiguration.class)
class IamAdministrationServiceMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }

        @Bean
        PrivilegeRepository privilegeRepository() {
            return Mockito.mock(PrivilegeRepository.class);
        }

        @Bean
        ModuleRepository moduleRepository() {
            return Mockito.mock(ModuleRepository.class);
        }

        @Bean
        PlatformIamAuthorization platformIamAuthorization() {
            return new PlatformIamAuthorization();
        }

        @Bean
        IamAdministrationService iamAdministrationService(
                RoleRepository roleRepository,
                PrivilegeRepository privilegeRepository,
                ModuleRepository moduleRepository,
                PlatformIamAuthorization platformIamAuthorization) {
            return new IamAdministrationService(
                    roleRepository,
                    privilegeRepository,
                    moduleRepository,
                    platformIamAuthorization);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private IamAdministrationService iamAdministrationService;

    @org.springframework.beans.factory.annotation.Autowired
    private RoleRepository roleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private PrivilegeRepository privilegeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private ModuleRepository moduleRepository;

    @BeforeEach
    void resetMocks() {
        reset(roleRepository, privilegeRepository, moduleRepository);
    }

    @Test
    @WithMockUser(authorities = "platform.iam.read")
    void readPermissionAllowsRoleCatalogueAccess() {
        when(roleRepository.findAll()).thenReturn(List.of(new Role()));

        assertEquals(1, iamAdministrationService.findAllRoles().size());
        verify(roleRepository).findAll();
    }

    @Test
    @WithMockUser(authorities = "platform.catalog.read")
    void missingIamPermissionDeniesRoleCatalogueAccess() {
        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.findAllRoles());
        verify(roleRepository, never()).findAll();
    }

    @Test
    @WithMockUser(authorities = {"platform.iam.manage", "platform.catalog.read"})
    void managerCanGrantOnlyPermissionAlreadyInEffectiveAccess() {
        Privilege catalogRead = privilege(11L, "platform.catalog.read");
        Role submittedRole = role(null, "Catalog reader", "catalog-reader", Set.of(catalogRead));
        when(privilegeRepository.findAllById(Set.of(11L))).thenReturn(List.of(catalogRead));
        when(roleRepository.existsBySlugIgnoreCase("catalog-reader")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role savedRole = iamAdministrationService.saveRole(submittedRole);

        assertEquals("catalog-reader", savedRole.getSlug());
        assertEquals(Set.of(catalogRead), savedRole.getPrivilege());
    }

    @Test
    @WithMockUser(authorities = "platform.iam.manage")
    void managerCannotGrantPermissionBroaderThanEffectiveAccess() {
        Privilege payoutApproval = privilege(12L, "platform.finance.payout.approve");
        Role submittedRole = role(null, "Payout approver", "payout-approver", Set.of(payoutApproval));
        when(privilegeRepository.findAllById(Set.of(12L))).thenReturn(List.of(payoutApproval));

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.saveRole(submittedRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = {"platform.iam.manage", "public.catalog.read"})
    void publicPolicyCapabilityCannotBeAssignedToRole() {
        Privilege publicPolicy = privilege(13L, "public.catalog.read");
        Role submittedRole = role(null, "Invalid public role", "invalid-public", Set.of(publicPolicy));
        when(privilegeRepository.findAllById(Set.of(13L))).thenReturn(List.of(publicPolicy));

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.saveRole(submittedRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void legacyAdminBridgeCannotModifyProtectedRole() {
        Privilege catalogRead = privilege(14L, "platform.catalog.read");
        Role protectedRole = role(1L, "Administrator", "admin", Set.of(catalogRead));
        when(privilegeRepository.findAllById(Set.of(14L))).thenReturn(List.of(catalogRead));
        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(protectedRole));

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.saveRole(protectedRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = "admin")
    void legacyAdminBridgeStillEnforcesGrantCeiling() {
        Privilege payoutApproval = privilege(16L, "platform.finance.payout.approve");
        Role submittedRole = role(null, "Payout approver", "payout-approver", Set.of(payoutApproval));
        when(privilegeRepository.findAllById(Set.of(16L))).thenReturn(List.of(payoutApproval));

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.saveRole(submittedRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = {"platform.iam.manage", "platform.catalog.read"})
    void ordinaryIamManagerCannotCreateProtectedRole() {
        Privilege catalogRead = privilege(17L, "platform.catalog.read");
        Role submittedRole = role(null, "Administrator", "admin", Set.of(catalogRead));
        when(privilegeRepository.findAllById(Set.of(17L))).thenReturn(List.of(catalogRead));
        when(roleRepository.existsBySlugIgnoreCase("admin")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.saveRole(submittedRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = "platform.iam.protected.manage")
    void protectedSystemRoleCannotBeDeleted() {
        Role protectedRole = role(1L, "Administrator", "admin", Set.of());
        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(protectedRole));

        assertThrows(AccessDeniedException.class, () -> iamAdministrationService.deleteRole(1L));
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @WithMockUser(authorities = "platform.iam.manage")
    void ordinaryIamManagerCannotChangeProtectedCatalogueMetadata() {
        Privilege submittedPrivilege = privilege(15L, "platform.catalog.read");

        assertThrows(
                AccessDeniedException.class,
                () -> iamAdministrationService.updatePrivilegeDisplayName(submittedPrivilege));
        verify(privilegeRepository, never()).save(any(Privilege.class));
    }

    private static Privilege privilege(Long id, String slug) {
        return new Privilege(id, new Modules(), slug, slug, Set.of());
    }

    private static Role role(Long id, String name, String slug, Set<Privilege> privileges) {
        return new Role(id, name, slug, Set.of(), privileges);
    }
}
