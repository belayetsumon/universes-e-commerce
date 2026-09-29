package com.ecommerce.app.module.user.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.ecommerce.app.SecurityConfig;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.customer.services.CustomerRegistrationService;
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.module.user.services.SessionAdministrationService;
import com.ecommerce.app.module.user.services.UsersService;
import com.ecommerce.app.security.authorization.PlatformIdentityAuthorization;
import com.ecommerce.app.security.permission.PlatformIdentityPermissions;
import com.ecommerce.app.security.permission.PlatformSecurityAuditPermissions;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    UsersControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    PlatformIdentityAuthorization.class,
    UsersController.class,
    UserDetailsController.class
})
class UsersControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {
    }

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private SessionAdministrationService sessionAdministrationService;

    @MockBean
    private UserValidator userValidator;

    @MockBean
    private LoginEventService loginEventService;

    @MockBean
    private LoggedUserService loggedUserService;

    @MockBean
    private ReferralRepository referralRepository;

    @MockBean
    private UsersService usersService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private ReferralService referralService;

    @MockBean
    private CustomerRegistrationService customerRegistrationService;

    @MockBean
    private CustomerCodMobileVerificationService customerCodMobileVerificationService;

    @MockBean
    private MobileNumberNormalizationService mobileNumberNormalizationService;

    @MockBean
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(usersRepository, roleRepository, sessionAdministrationService, loggedUserService);
        when(usersRepository.findForAdminListFilters(null, null, null, null)).thenReturn(List.of());
        when(roleRepository.findAll()).thenReturn(List.of());
        when(sessionAdministrationService.findLoginHistoryForAdmin(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(sessionAdministrationService.normalizeDateInput(any())).thenReturn("");
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void ordinaryCustomerCannotAccessIdentityAdministrationReads() throws Exception {
        String[] protectedPaths = {
            "/users",
            "/users/index",
            "/users/userbystatus",
            "/users/login-history",
            "/users/login-history/7",
            "/users/detailsinfo/7",
            "/userdetails/index"
        };

        for (String path : protectedPaths) {
            mockMvc.perform(get(path).with(user("customer@example.com").authorities(() -> "customer")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void customerCanReadOnlyOwnUserProfile() throws Exception {
        Users ownUser = userRecord(7L, "customer@example.com");
        when(usersRepository.existsByIdAndEmail(7L, "customer@example.com")).thenReturn(true);
        when(usersRepository.findById(7L)).thenReturn(Optional.of(ownUser));
        when(loggedUserService.activeUserIdOrNull()).thenReturn(7L);

        mockMvc.perform(get("/users/profile")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/view/7"));

        mockMvc.perform(get("/users/view/7")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/view"));

        mockMvc.perform(get("/users/view/8")
                .with(user("customer@example.com").authorities(() -> "customer")))
                .andExpect(status().isForbidden());

        verify(usersRepository, never()).findById(8L);
    }

    @Test
    void identityReaderAndLegacyAdminCanAccessProtectedReads() throws Exception {
        mockMvc.perform(get("/users/index")
                .with(user("identity-reader@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_READ)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/allusers"));

        mockMvc.perform(get("/userdetails/index")
                .with(user("legacy-admin@example.com").authorities(() -> "admin")))
                .andExpect(status().isOk())
                .andExpect(view().name("pims/userdetails/index"));
    }

    @Test
    void loginHistoryRequiresSecurityAuditPermission() throws Exception {
        mockMvc.perform(get("/users/login-history")
                .with(user("identity-reader@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_READ)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users/login-history")
                .with(user("security-auditor@example.com")
                        .authorities(() -> PlatformSecurityAuditPermissions.READ)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/login_history"));
    }

    @Test
    void userMutationRequiresManagePermissionAndCsrf() throws Exception {
        mockMvc.perform(post("/users/delete/7")
                .with(user("identity-reader@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_READ))
                .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/users/delete/7")
                .with(user("identity-manager@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_MANAGE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/users/delete/7")
                .with(user("identity-manager@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_MANAGE))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/index"));

        verify(usersRepository).deleteById(7L);
    }

    @Test
    void passwordResetRequiresDedicatedPermission() throws Exception {
        Users targetUser = userRecord(9L, "target@example.com");
        when(usersRepository.findById(9L)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(get("/users/change-password/9")
                .with(user("identity-manager@example.com")
                        .authorities(() -> PlatformIdentityPermissions.USER_MANAGE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users/change-password/9")
                .with(user("password-admin@example.com")
                        .authorities(
                                () -> PlatformIdentityPermissions.PASSWORD_RESET,
                                () -> PlatformIdentityPermissions.USER_READ)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/change_password"));
    }

    private Users userRecord(Long id, String email) {
        Users user = new Users();
        user.setId(id);
        user.setFirstName("Test");
        user.setEmail(email);
        return user;
    }
}
