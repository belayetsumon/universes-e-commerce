package com.ecommerce.app.vendor.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorAccessAuthorityChecker;
import com.ecommerce.app.vendor.user.componant.VendorRoleChecker;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.user.services.VendorStaffAdministrationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    VendorStaffIamControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    VendorAccessControllController.class,
    VendorRoleManagementController.class
})
class VendorStaffIamControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {
    }

    @MockBean
    private VendorUserContext vendorUserContext;

    @MockBean
    private VendorStaffAdministrationService vendorStaffAdministrationService;

    @MockBean(name = "vendorAccessAuthorityChecker")
    private VendorAccessAuthorityChecker vendorAccessAuthorityChecker;

    @MockBean(name = "vendorRoleChecker")
    private VendorRoleChecker vendorRoleChecker;

    @MockBean
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    private Vendorprofile vendor;

    @BeforeEach
    void setUp() {
        reset(
                vendorUserContext,
                vendorStaffAdministrationService,
                vendorAccessAuthorityChecker,
                vendorRoleChecker,
                customLoginSuccessHandler,
                userDetailsService);
        vendor = new Vendorprofile();
        vendor.setId(42L);
        vendor.setCompanyName("Test Vendor");
        when(vendorUserContext.getActiveVendor()).thenReturn(vendor);
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void ordinaryAuthenticatedUserCannotReadVendorStaffList() throws Exception {
        mockMvc.perform(get("/vendor-users/userlist")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().isForbidden());

        verify(vendorStaffAdministrationService, never()).listStaff(any());
    }

    @Test
    void vendorStaffManagerCanReadVendorStaffList() throws Exception {
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.staff.manage"))).thenReturn(true);
        when(vendorStaffAdministrationService.listStaff(vendor)).thenReturn(List.of());

        mockMvc.perform(get("/vendor-users/userlist")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.staff.manage")))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/users/vendor_users_list"));

        verify(vendorStaffAdministrationService).listStaff(vendor);
    }

    @Test
    void staffDeleteIsPostOnlyAndCsrfProtected() throws Exception {
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.staff.manage"))).thenReturn(true);

        mockMvc.perform(get("/vendor-users/delete/9")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.staff.manage")))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/vendor-users/delete/9")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.staff.manage")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/vendor-users/delete/9")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.staff.manage"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor-users/userlist"));

        verify(vendorStaffAdministrationService).removeStaffAssignment(vendor, 9L);
    }

    @Test
    void roleDeleteIsPostOnlyAndCsrfProtected() throws Exception {
        when(vendorAccessAuthorityChecker.hasAuthority(any(), eq("vendor.role.manage"))).thenReturn(true);

        mockMvc.perform(get("/vendor-users/roles/delete/7")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.role.manage")))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/vendor-users/roles/delete/7")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.role.manage")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/vendor-users/roles/delete/7")
                .with(user("manager@example.com").authorities(() -> "VENDOR_42:vendor.role.manage"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor-users/roles"));

        verify(vendorStaffAdministrationService).deleteRole(vendor, 7L);
    }
}
