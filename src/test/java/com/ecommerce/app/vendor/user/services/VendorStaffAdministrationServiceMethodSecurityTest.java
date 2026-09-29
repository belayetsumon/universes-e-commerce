package com.ecommerce.app.vendor.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.SessionCredentialVersionService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorAccessAuthorityChecker;
import com.ecommerce.app.vendor.user.componant.VendorRoleChecker;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VendorStaffAdministrationServiceMethodSecurityTest.TestConfiguration.class)
class VendorStaffAdministrationServiceMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        VendorUserContext vendorUserContext() {
            return Mockito.mock(VendorUserContext.class);
        }

        @Bean(name = "vendorAccessAuthorityChecker")
        VendorAccessAuthorityChecker vendorAccessAuthorityChecker() {
            return Mockito.mock(VendorAccessAuthorityChecker.class);
        }

        @Bean(name = "vendorRoleChecker")
        VendorRoleChecker vendorRoleChecker() {
            return Mockito.mock(VendorRoleChecker.class);
        }

        @Bean
        UsersRepository usersRepository() {
            return Mockito.mock(UsersRepository.class);
        }

        @Bean
        UserVendorRoleRepository userVendorRoleRepository() {
            return Mockito.mock(UserVendorRoleRepository.class);
        }

        @Bean
        VendorRoleRepository vendorRoleRepository() {
            return Mockito.mock(VendorRoleRepository.class);
        }

        @Bean
        VendorPrivilegeRepository vendorPrivilegeRepository() {
            return Mockito.mock(VendorPrivilegeRepository.class);
        }

        @Bean
        SessionCredentialVersionService sessionCredentialVersionService() {
            return Mockito.mock(SessionCredentialVersionService.class);
        }

        @Bean
        VendorStaffAdministrationService vendorStaffAdministrationService(
                VendorRoleChecker vendorRoleChecker,
                UsersRepository usersRepository,
                UserVendorRoleRepository userVendorRoleRepository,
                VendorRoleRepository vendorRoleRepository,
                VendorPrivilegeRepository vendorPrivilegeRepository,
                SessionCredentialVersionService sessionCredentialVersionService) {
            return new VendorStaffAdministrationService(
                    vendorRoleChecker,
                    usersRepository,
                    userVendorRoleRepository,
                    vendorRoleRepository,
                    vendorPrivilegeRepository,
                    sessionCredentialVersionService);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private VendorStaffAdministrationService service;

    @org.springframework.beans.factory.annotation.Autowired
    private VendorAccessAuthorityChecker vendorAccessAuthorityChecker;

    @org.springframework.beans.factory.annotation.Autowired
    private VendorRoleChecker vendorRoleChecker;

    @org.springframework.beans.factory.annotation.Autowired
    private UsersRepository usersRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private UserVendorRoleRepository userVendorRoleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private VendorRoleRepository vendorRoleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private VendorPrivilegeRepository vendorPrivilegeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private SessionCredentialVersionService sessionCredentialVersionService;

    @BeforeEach
    void resetMocks() {
        reset(
                vendorAccessAuthorityChecker,
                vendorRoleChecker,
                usersRepository,
                userVendorRoleRepository,
                vendorRoleRepository,
                vendorPrivilegeRepository,
                sessionCredentialVersionService);
    }

    @Test
    @WithMockUser(authorities = "vendor.catalog.read")
    void missingStaffManagementPermissionCannotReadStaffAssignments() {
        Vendorprofile vendor = vendor();

        assertThrows(AccessDeniedException.class, () -> service.listStaff(vendor));

        verify(userVendorRoleRepository, never()).findAllByVendor(vendor);
    }

    @Test
    @WithMockUser(username = "manager@example.com", authorities = {
        "VENDOR_42:vendor.staff.manage",
        "VENDOR_42:vendor.catalog.read"
    })
    void staffAssignmentUsesScopedAssignableRoleAndGrantCeiling() {
        Vendorprofile vendor = vendor();
        Users user = user(6L, "staff@example.com");
        VendorPrivilege privilege = vendorPrivilege(11L, "vendor.catalog.read");
        VendorRole role = vendorRole(7L, "Catalog", "catalog", Set.of(privilege));
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.staff.manage"))).thenReturn(true);
        when(usersRepository.findByEmailAndStatus("staff@example.com", Status.Active)).thenReturn(user);
        when(userVendorRoleRepository.existsByUsers_EmailAndVendor_Id("staff@example.com", 42L)).thenReturn(false);
        when(vendorRoleRepository.findAssignableRoleById(7L, vendor)).thenReturn(Optional.of(role));
        when(userVendorRoleRepository.save(any(UserVendorRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserVendorRole saved = service.assignExistingUserToVendor(vendor, " Staff@Example.com ", 7L);

        assertEquals(user, saved.getUsers());
        assertEquals(vendor, saved.getVendor());
        assertEquals(role, saved.getVendorRole());
        verify(vendorRoleRepository).findAssignableRoleById(7L, vendor);
        verify(userVendorRoleRepository).save(any(UserVendorRole.class));
        verify(sessionCredentialVersionService).bumpCredentialVersion(6L);
    }

    @Test
    @WithMockUser(username = "manager@example.com", authorities = "VENDOR_42:vendor.staff.manage")
    void staffRemovalUsesVendorScopedLookupAndNeverUnscopedDeleteById() {
        Vendorprofile vendor = vendor();
        UserVendorRole assignment = new UserVendorRole();
        assignment.setId(9L);
        assignment.setVendor(vendor);
        assignment.setUsers(user(7L, "staff@example.com"));
        assignment.setVendorRole(vendorRole(8L, "Support", "support", Set.of()));
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.staff.manage"))).thenReturn(true);
        when(userVendorRoleRepository.findByIdAndVendor(9L, vendor)).thenReturn(Optional.of(assignment));

        service.removeStaffAssignment(vendor, 9L);

        verify(userVendorRoleRepository).findByIdAndVendor(9L, vendor);
        verify(userVendorRoleRepository).delete(assignment);
        verify(userVendorRoleRepository, never()).deleteById(9L);
        verify(sessionCredentialVersionService).bumpCredentialVersion(7L);
    }

    @Test
    @WithMockUser(username = "owner@example.com", authorities = "VENDOR_42:vendor.staff.manage")
    void staffCannotRemoveOwnVendorAssignment() {
        Vendorprofile vendor = vendor();
        UserVendorRole assignment = new UserVendorRole();
        assignment.setUsers(user("owner@example.com"));
        assignment.setVendorRole(vendorRole(8L, "Support", "support", Set.of()));
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.staff.manage"))).thenReturn(true);
        when(userVendorRoleRepository.findByIdAndVendor(9L, vendor)).thenReturn(Optional.of(assignment));

        assertThrows(AccessDeniedException.class, () -> service.removeStaffAssignment(vendor, 9L));

        verify(userVendorRoleRepository, never()).delete(assignment);
    }

    @Test
    @WithMockUser(username = "manager@example.com", authorities = "VENDOR_42:vendor.role.manage")
    void platformPrivilegeCannotBeAssignedToVendorRole() {
        Vendorprofile vendor = vendor();
        VendorRole submitted = vendorRole(null, "Payout Admin", "payout-admin", Set.of());
        VendorPrivilege platformPrivilege = vendorPrivilege(15L, "platform.finance.payout.approve");
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.role.manage"))).thenReturn(true);
        when(vendorPrivilegeRepository.findAllById(Set.of(15L))).thenReturn(List.of(platformPrivilege));

        assertThrows(AccessDeniedException.class, () -> service.saveRole(vendor, submitted, List.of(15L)));

        verify(vendorRoleRepository, never()).save(any(VendorRole.class));
    }

    @Test
    @WithMockUser(username = "manager@example.com", authorities = "VENDOR_42:vendor.role.manage")
    void managerCannotGrantVendorPrivilegeOutsideEffectiveAccess() {
        Vendorprofile vendor = vendor();
        VendorRole submitted = vendorRole(null, "Payout Admin", "payout-admin", Set.of());
        VendorPrivilege payoutPrivilege = vendorPrivilege(16L, "vendor.finance.payout.approve");
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.role.manage"))).thenReturn(true);
        when(vendorPrivilegeRepository.findAllById(Set.of(16L))).thenReturn(List.of(payoutPrivilege));

        assertThrows(AccessDeniedException.class, () -> service.saveRole(vendor, submitted, List.of(16L)));

        verify(vendorRoleRepository, never()).save(any(VendorRole.class));
    }

    @Test
    @WithMockUser(username = "owner@example.com", authorities = "VENDOR_42:vendor.role.manage")
    void roleSaveIsVendorScopedAndNormalizesSlug() {
        Vendorprofile vendor = vendor();
        VendorPrivilege catalogRead = vendorPrivilege(17L, "vendor.catalog.read");
        VendorRole submitted = vendorRole(null, "Catalog Editor", " Catalog-Editor ", Set.of());
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.role.manage"))).thenReturn(true);
        when(vendorRoleChecker.hasVendorRole(any(), eq("OWNER"))).thenReturn(true);
        when(vendorPrivilegeRepository.findAllById(Set.of(17L))).thenReturn(List.of(catalogRead));
        when(vendorRoleRepository.save(any(VendorRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorRole saved = service.saveRole(vendor, submitted, List.of(17L));

        assertEquals(vendor, saved.getVendor());
        assertEquals("catalog-editor", saved.getSlug());
        ArgumentCaptor<VendorRole> roleCaptor = ArgumentCaptor.forClass(VendorRole.class);
        verify(vendorRoleRepository).save(roleCaptor.capture());
        assertEquals(Set.of(catalogRead), roleCaptor.getValue().getVendorPrivilege());
    }

    @Test
    @WithMockUser(username = "owner@example.com", authorities = "VENDOR_42:vendor.role.manage")
    void rolePermissionChangeInvalidatesAssignedUsersAuthoritySnapshots() {
        Vendorprofile vendor = vendor();
        VendorPrivilege catalogRead = vendorPrivilege(17L, "vendor.catalog.read");
        VendorRole submitted = vendorRole(18L, "Catalog Editor", "catalog-editor", Set.of());
        VendorRole existing = vendorRole(18L, "Catalog Reader", "catalog-reader", Set.of());
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.role.manage"))).thenReturn(true);
        when(vendorRoleChecker.hasVendorRole(any(), eq("OWNER"))).thenReturn(true);
        when(vendorRoleRepository.findByIdAndVendor(18L, vendor)).thenReturn(Optional.of(existing));
        when(vendorPrivilegeRepository.findAllById(Set.of(17L))).thenReturn(List.of(catalogRead));
        when(vendorRoleRepository.save(any(VendorRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userVendorRoleRepository.findAssignedUserIdsByVendorRoleId(18L)).thenReturn(List.of(6L, 7L));

        VendorRole saved = service.saveRole(vendor, submitted, List.of(17L));

        assertEquals(18L, saved.getId());
        verify(sessionCredentialVersionService).bumpCredentialVersion(6L);
        verify(sessionCredentialVersionService).bumpCredentialVersion(7L);
    }

    private static Vendorprofile vendor() {
        Vendorprofile vendor = new Vendorprofile();
        vendor.setId(42L);
        vendor.setCompanyName("Test Vendor");
        return vendor;
    }

    private static Users user(String email) {
        return user(null, email);
    }

    private static Users user(Long id, String email) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private static VendorPrivilege vendorPrivilege(Long id, String slug) {
        VendorPrivilege privilege = new VendorPrivilege();
        privilege.setId(id);
        privilege.setName(slug);
        privilege.setSlug(slug);
        return privilege;
    }

    private static VendorRole vendorRole(Long id, String name, String slug, Set<VendorPrivilege> privileges) {
        VendorRole role = new VendorRole();
        role.setId(id);
        role.setName(name);
        role.setSlug(slug);
        role.setVendorPrivilege(privileges);
        return role;
    }
}
