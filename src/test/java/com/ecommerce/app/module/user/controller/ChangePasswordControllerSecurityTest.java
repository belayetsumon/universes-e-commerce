package com.ecommerce.app.module.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.app.SecurityConfig;
import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
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
    ChangePasswordControllerSecurityTest.TestConfiguration.class,
    SecurityConfig.class,
    ChangePasswordController.class
})
class ChangePasswordControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {

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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void legacyPasswordPageIsDisabledForAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/changepassword")
                .with(user("legacy-user@example.com").authorities(() -> "changepassword")))
                .andExpect(status().isForbidden());
    }

    @Test
    void legacyPasswordMutationIsDisabledEvenWithCsrf() throws Exception {
        mockMvc.perform(post("/changepassword/update")
                .with(user("legacy-user@example.com").authorities(() -> "changepassword")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/changepassword/update")
                .with(user("legacy-user@example.com").authorities(() -> "changepassword"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
