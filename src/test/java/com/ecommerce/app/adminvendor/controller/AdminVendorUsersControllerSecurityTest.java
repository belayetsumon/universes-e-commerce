package com.ecommerce.app.adminvendor.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.ecommerce.app.SecurityConfig;
import com.ecommerce.app.adminvendor.services.AdminVendorIamService;
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.security.permission.PlatformVendorManagementPermissions;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
    AdminVendorUsersControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    AdminVendorIamService.class,
    AdminVendorUsersController.class
})
class AdminVendorUsersControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {
    }

    @MockBean
    private VendorRoleRepository roleRepository;

    @MockBean
    private VendorPrivilegeRepository privilegeRepository;

    @MockBean
    private UserVendorRoleRepository userVendorRoleRepository;

    @MockBean
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private AdminVendorIamService vendorIamService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(roleRepository, privilegeRepository, userVendorRoleRepository);
        when(roleRepository.findAll()).thenReturn(List.of());
        when(privilegeRepository.findAll()).thenReturn(List.of());
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void ordinaryCustomerCannotAccessPlatformVendorIam() throws Exception {
        mockMvc.perform(get("/adminvendorusers/rolelist")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/adminvendorusers/privilegeslist")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().isForbidden());

        verify(roleRepository, never()).findAll();
        verify(privilegeRepository, never()).findAll();
    }

    @Test
    void readAndPrivilegeCapabilitiesAreSeparated() throws Exception {
        mockMvc.perform(get("/adminvendorusers/rolelist")
                .with(user("vendor-reader@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.READ)))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/users/vendor_role_list"));

        mockMvc.perform(get("/adminvendorusers/privilegeslist")
                .with(user("vendor-reader@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.READ)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/adminvendorusers/privilegeslist")
                .with(user("vendor-catalogue-admin@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.PRIVILEGE)))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/users/privilegelist"));
    }

    @Test
    void roleMutationRequiresManagePermissionAndCsrf() throws Exception {
        mockMvc.perform(post("/adminvendorusers/role_save")
                .param("name", "Operations")
                .param("slug", "operations")
                .with(user("vendor-reader@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.READ))
                .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/adminvendorusers/role_save")
                .param("name", "Operations")
                .param("slug", "operations")
                .with(user("vendor-manager@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.MANAGE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/adminvendorusers/role_save")
                .param("name", "Operations")
                .param("slug", "operations")
                .with(user("vendor-manager@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.MANAGE))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/adminvendorusers/rolelist"));

        ArgumentCaptor<VendorRole> roleCaptor = ArgumentCaptor.forClass(VendorRole.class);
        verify(roleRepository).save(roleCaptor.capture());
    }

    @Test
    void roleDeleteIsPostOnlyCsrfProtectedAndAssignmentSafe() throws Exception {
        VendorRole role = new VendorRole();
        role.setId(7L);
        role.setName("Operations");
        role.setSlug("operations");
        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));

        mockMvc.perform(get("/adminvendorusers/role_delete/7")
                .with(user("vendor-deleter@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.DELETE)))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/adminvendorusers/role_delete/7")
                .with(user("vendor-deleter@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.DELETE)))
                .andExpect(status().isForbidden());

        when(userVendorRoleRepository.existsByVendorRole_Id(7L)).thenReturn(false);
        mockMvc.perform(post("/adminvendorusers/role_delete/7")
                .with(user("vendor-deleter@example.com")
                        .authorities(() -> PlatformVendorManagementPermissions.DELETE))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/adminvendorusers/rolelist"));
        verify(roleRepository).delete(role);
    }

    @Test
    @WithMockUser(authorities = PlatformVendorManagementPermissions.READ)
    void directServiceMutationIsDeniedWithoutManageCapability() {
        VendorRole role = new VendorRole();
        role.setName("Operations");
        role.setSlug("operations");

        assertThrows(AccessDeniedException.class, () -> vendorIamService.saveRole(role, List.of()));
        verify(roleRepository, never()).save(Mockito.any());
    }

    @Test
    @WithMockUser(authorities = PlatformVendorManagementPermissions.DELETE)
    void assignedVendorRoleCannotBeDeletedThroughTheService() {
        VendorRole role = new VendorRole();
        role.setId(7L);
        role.setName("Operations");
        role.setSlug("operations");
        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));
        when(userVendorRoleRepository.existsByVendorRole_Id(7L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> vendorIamService.deleteRole(7L));
        verify(roleRepository, never()).delete(role);
    }
}
