package com.ecommerce.app.module.settings.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.app.SecurityConfig;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    GlobalSettingsControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    GlobalSettingsController.class
})
class GlobalSettingsControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {

        @Bean
        GlobalSettingsService globalSettingsService() {
            return Mockito.mock(GlobalSettingsService.class);
        }

        @Bean
        VendorprofileRepository vendorprofileRepository() {
            return Mockito.mock(VendorprofileRepository.class);
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
    private GlobalSettingsService globalSettingsService;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(globalSettingsService.getActiveSettings()).thenReturn(new GlobalSettings());
        when(vendorprofileRepository.findAll(any(Sort.class))).thenReturn(List.of());
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void authenticatedNonAdminCannotReadSettings() throws Exception {
        mockMvc.perform(get("/admin/settings/index")
                .with(user("customer").authorities(new SimpleGrantedAuthority("customer"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void namedAdminAuthorityCanReadSettings() throws Exception {
        mockMvc.perform(get("/admin/settings/index").with(adminUser()))
                .andExpect(status().isOk());
    }

    @Test
    void settingsMutationRequiresCsrf() throws Exception {
        mockMvc.perform(post("/admin/settings/update").with(adminUser()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithCsrfCanReachSettingsMutation() throws Exception {
        mockMvc.perform(post("/admin/settings/update").with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/settings/index"));
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser() {
        return user("settings-admin").authorities(new SimpleGrantedAuthority("admin"));
    }
}
