package com.ecommerce.app.module.user.controller;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.app.SecurityConfig;
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.ripository.ModuleRepository;
import com.ecommerce.app.module.user.ripository.PrivilegeRepository;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.services.IamAdministrationService;
import com.ecommerce.app.security.authorization.PlatformIamAuthorization;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    RoleControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    PlatformIamAuthorization.class,
    RoleController.class
})
class RoleControllerSecurityTest {

    @Configuration
    @EnableWebMvc
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

        @Bean
        CustomLoginSuccessHandler customLoginSuccessHandler() {
            return Mockito.mock(CustomLoginSuccessHandler.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }

    }

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(roleRepository, privilegeRepository, moduleRepository);
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void missingIamPermissionDeniesRolePage() throws Exception {
        mockMvc.perform(get("/role/index")
                .with(user("limited-admin").authorities(() -> "platform.catalog.read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleMutationWithoutCsrfIsDenied() throws Exception {
        mockMvc.perform(post("/role/delete/7")
                .with(user("iam-admin").authorities(() -> "platform.iam.manage")))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleMutationWithPermissionAndCsrfReachesService() throws Exception {
        Role role = new Role(7L, "Operations", "operations", Set.of(), Set.of());
        when(roleRepository.findById(7L)).thenReturn(java.util.Optional.of(role));

        mockMvc.perform(post("/role/delete/7")
                .with(user("iam-admin").authorities(() -> "platform.iam.manage"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(roleRepository).delete(role);
    }
}
