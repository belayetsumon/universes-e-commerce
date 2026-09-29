package com.ecommerce.app;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
    SecurityConfigCsrfCoverageTest.TestConfiguration.class,
    SecurityConfig.class,
    SecurityConfigCsrfCoverageTest.ProbeController.class
})
class SecurityConfigCsrfCoverageTest {

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

    @RestController
    static class ProbeController {

        @PostMapping({
            "/role/probe",
            "/admin/settings/probe",
            "/admin/communication/providers/save",
            "/users/save",
            "/users/change-password/7",
            "/users/generate-referral-code/7",
            "/users/delete/7",
            "/users/frontRegistrationSave",
            "/customer_registration/customer_registration_save",
            "/register",
            "/customerregister/register",
            "/customer-profile/update",
            "/checkout/guest/mobile/send-otp",
            "/checkout/customer/mobile/send-otp",
            "/cart/updateQuantity",
            "/carts/updateShippingOption",
            "/district/save-district",
            "/cart_address/add_billing_address",
            "/order/savebyvendor",
            "/admin/fraud/probe",
            "/payment/meritten-emi/provider/callback/7"
        })
        String postProbe() {
            return "ok";
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

    @ParameterizedTest
    @ValueSource(strings = {
        "/role/probe",
        "/admin/settings/probe",
        "/admin/communication/providers/save",
        "/users/save",
        "/users/change-password/7",
        "/users/generate-referral-code/7",
        "/users/delete/7",
        "/users/frontRegistrationSave",
        "/customer_registration/customer_registration_save",
        "/register",
        "/customerregister/register",
        "/customer-profile/update",
        "/checkout/guest/mobile/send-otp",
        "/checkout/customer/mobile/send-otp",
        "/cart/updateQuantity",
        "/carts/updateShippingOption",
        "/district/save-district",
        "/cart_address/add_billing_address",
        "/order/savebyvendor"
    })
    void browserMutationWithoutCsrfIsDenied(String path) throws Exception {
        mockMvc.perform(post(path).with(adminUser()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/admin/settings/probe",
        "/admin/communication/providers/save",
        "/users/frontRegistrationSave",
        "/checkout/guest/mobile/send-otp",
        "/checkout/customer/mobile/send-otp",
        "/cart/updateQuantity",
        "/carts/updateShippingOption",
        "/district/save-district",
        "/cart_address/add_billing_address",
        "/order/savebyvendor"
    })
    void browserMutationWithCsrfReachesMvc(String path) throws Exception {
        mockMvc.perform(post(path).with(adminUser()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void fraudAdminRouteRemainsOnItsCustomCsrfMechanism() throws Exception {
        mockMvc.perform(post("/admin/fraud/probe").with(adminUser()))
                .andExpect(status().isOk());
    }

    @Test
    void externalProviderCallbackRemainsOutsideBrowserCsrfMatcher() throws Exception {
        mockMvc.perform(post("/payment/meritten-emi/provider/callback/7").with(adminUser()))
                .andExpect(status().isOk());
    }

    @Test
    void communicationAdministrationRejectsNonAdminUsers() throws Exception {
        mockMvc.perform(post("/admin/communication/providers/save")
                        .with(user("customer"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser() {
        return user("security-admin").authorities(new SimpleGrantedAuthority("admin"));
    }
}
