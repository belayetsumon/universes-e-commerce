package com.ecommerce.app;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import org.springframework.web.bind.annotation.GetMapping;
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

        @GetMapping({
            "/robots.txt",
            "/sitemap.xml",
            "/llms.txt",
            "/maintenance",
            "/register",
            "/customerregister/register"
        })
        String publicGetProbe() {
            return "ok";
        }

        @PostMapping({
            "/role/probe",
            "/adminvendorusers/role_save",
            "/admin/settings/probe",
            "/admin/communication/providers/save",
            "/vendor-users/save",
            "/vendor-users/delete/7",
            "/vendor-users/roles/save",
            "/vendor-users/roles/delete/7",
            "/adminvendor/save",
            "/adminvendor/update",
            "/adminvendor/delete/vendor-uuid",
            "/admin-vendor-payout-methods/save",
            "/admin-vendor-payout-methods/delete/7",
            "/vendor-payout-methods/save",
            "/vendor-payout-methods/delete/7",
            "/admin/ads/save",
            "/admin/ads/delete/7",
            "/catalog-attributes/save",
            "/catalog-attributes/delete/attribute-uuid",
            "/catalog-attributes/options/save",
            "/catalog-attributes/options/delete/option-uuid",
            "/catalog-attributes/category-mappings/save",
            "/catalog-attributes/category-mappings/delete/mapping-uuid",
            "/manufacturer/save",
            "/manufacturer/delete/7",
            "/uom/save",
            "/uom/delete/7",
            "/productimage/upload",
            "/product/delete/7",
            "/productcategory/save",
            "/productcategory/delete/7",
            "/productvendor/save",
            "/vendor_productimage/upload",
            "/productvendor/delete/7",
            "/vendorprofile/save",
            "/vendorlogo/save",
            "/vendor-payout/save",
            "/vendorverifications/emailverification",
            "/vendorverifications/verify-email",
            "/vendorverifications/mobile-verification-otp-send",
            "/vendorverifications/verify-mobile",
            "/customer/save",
            "/customerprofileimage/save",
            "/customerorder/statuschange",
            "/customerorder/request-item-return",
            "/admin-customer/statuschange",
            "/admin-customer/item-return",
            "/vendor-order/statuschange",
            "/vendor-order/item-return",
            "/vendor-order/update-charges",
            "/public/home-contact-save",
            "/forgotpassword/showemail",
            "/forgotpassword/reset",
            "/users/save",
            "/users/change-password/7",
            "/users/generate-referral-code/7",
            "/users/delete/7",
            "/changepassword/update",
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
            "/district/select",
            "/cart_address/add_billing_address",
            "/order/savebyvendor",
            "/coupon/save",
            "/coupon/delete/7",
            "/giftcard/save",
            "/giftcard/delete/7",
            "/cashoutrequest/7/approve",
            "/cashoutrequest/7/reject",
            "/cashoutrequest/7/mark-paid",
            "/customerwallet/wallet/generate-giftcard",
            "/customerwallet/wallet/cashout",
            "/customer-giftcard/buy",
            "/customer-giftcard/payment/card-uuid",
            "/customerredeem/save",
            "/wallet/top-up",
            "/wallet/delete/7",
            "/referral/delete/7",
            "/reward-redemption",
            "/lavelratesettings/save",
            "/lavelratesettings/delete/7",
            "/cashback-policy/save",
            "/cashback-policy/delete/7",
            "/admin/promotions/order-incentives/7/reverse",
            "/admin/promotions/fraud-flags/7/review",
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
        "/adminvendorusers/role_save",
        "/admin/settings/probe",
        "/adminvendorusers/role_save",
        "/admin/communication/providers/save",
        "/vendor-users/save",
        "/vendor-users/delete/7",
        "/vendor-users/roles/save",
        "/vendor-users/roles/delete/7",
        "/adminvendor/save",
        "/adminvendor/update",
        "/adminvendor/delete/vendor-uuid",
        "/admin-vendor-payout-methods/save",
        "/admin-vendor-payout-methods/delete/7",
        "/vendor-payout-methods/save",
        "/vendor-payout-methods/delete/7",
        "/admin/ads/save",
        "/admin/ads/delete/7",
        "/catalog-attributes/save",
        "/catalog-attributes/delete/attribute-uuid",
        "/catalog-attributes/options/save",
        "/catalog-attributes/options/delete/option-uuid",
        "/catalog-attributes/category-mappings/save",
        "/catalog-attributes/category-mappings/delete/mapping-uuid",
        "/manufacturer/save",
        "/manufacturer/delete/7",
        "/uom/save",
        "/uom/delete/7",
        "/productimage/upload",
        "/product/delete/7",
        "/productcategory/save",
        "/productcategory/delete/7",
        "/productvendor/save",
        "/vendor_productimage/upload",
        "/productvendor/delete/7",
        "/vendorprofile/save",
        "/vendorlogo/save",
        "/vendor-payout/save",
        "/vendorverifications/emailverification",
        "/vendorverifications/verify-email",
        "/vendorverifications/mobile-verification-otp-send",
        "/vendorverifications/verify-mobile",
        "/customer/save",
        "/customerprofileimage/save",
        "/customerorder/statuschange",
        "/customerorder/request-item-return",
        "/admin-customer/statuschange",
        "/admin-customer/item-return",
        "/vendor-order/statuschange",
        "/vendor-order/item-return",
        "/vendor-order/update-charges",
        "/public/home-contact-save",
        "/forgotpassword/showemail",
        "/forgotpassword/reset",
        "/users/save",
        "/users/change-password/7",
        "/users/generate-referral-code/7",
        "/users/delete/7",
        "/changepassword/update",
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
        "/district/select",
        "/cart_address/add_billing_address",
        "/order/savebyvendor",
        "/coupon/save",
        "/coupon/delete/7",
        "/giftcard/save",
        "/giftcard/delete/7",
        "/cashoutrequest/7/approve",
        "/cashoutrequest/7/reject",
        "/cashoutrequest/7/mark-paid",
        "/customerwallet/wallet/generate-giftcard",
        "/customerwallet/wallet/cashout",
        "/customer-giftcard/buy",
        "/customer-giftcard/payment/card-uuid",
        "/customerredeem/save",
        "/wallet/top-up",
        "/wallet/delete/7",
        "/referral/delete/7",
        "/reward-redemption",
        "/lavelratesettings/save",
        "/lavelratesettings/delete/7",
        "/cashback-policy/save",
        "/cashback-policy/delete/7",
        "/admin/promotions/order-incentives/7/reverse",
        "/admin/promotions/fraud-flags/7/review"
    })
    void browserMutationWithoutCsrfIsDenied(String path) throws Exception {
        mockMvc.perform(post(path).with(adminUser()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/admin/settings/probe",
        "/admin/communication/providers/save",
        "/vendor-users/save",
        "/vendor-users/delete/7",
        "/vendor-users/roles/save",
        "/vendor-users/roles/delete/7",
        "/adminvendor/save",
        "/adminvendor/update",
        "/adminvendor/delete/vendor-uuid",
        "/admin-vendor-payout-methods/save",
        "/admin-vendor-payout-methods/delete/7",
        "/vendor-payout-methods/save",
        "/vendor-payout-methods/delete/7",
        "/admin/ads/save",
        "/admin/ads/delete/7",
        "/catalog-attributes/save",
        "/catalog-attributes/delete/attribute-uuid",
        "/catalog-attributes/options/save",
        "/catalog-attributes/options/delete/option-uuid",
        "/catalog-attributes/category-mappings/save",
        "/catalog-attributes/category-mappings/delete/mapping-uuid",
        "/manufacturer/save",
        "/manufacturer/delete/7",
        "/uom/save",
        "/uom/delete/7",
        "/productimage/upload",
        "/product/delete/7",
        "/productcategory/save",
        "/productcategory/delete/7",
        "/productvendor/save",
        "/vendor_productimage/upload",
        "/productvendor/delete/7",
        "/vendorprofile/save",
        "/vendorlogo/save",
        "/vendor-payout/save",
        "/vendorverifications/emailverification",
        "/vendorverifications/verify-email",
        "/vendorverifications/mobile-verification-otp-send",
        "/vendorverifications/verify-mobile",
        "/customer/save",
        "/customerprofileimage/save",
        "/customerorder/statuschange",
        "/customerorder/request-item-return",
        "/admin-customer/statuschange",
        "/admin-customer/item-return",
        "/vendor-order/statuschange",
        "/vendor-order/item-return",
        "/vendor-order/update-charges",
        "/public/home-contact-save",
        "/forgotpassword/showemail",
        "/forgotpassword/reset",
        "/changepassword/update",
        "/users/frontRegistrationSave",
        "/checkout/guest/mobile/send-otp",
        "/checkout/customer/mobile/send-otp",
        "/cart/updateQuantity",
        "/carts/updateShippingOption",
        "/district/save-district",
        "/district/select",
        "/cart_address/add_billing_address",
        "/order/savebyvendor",
        "/coupon/save",
        "/coupon/delete/7",
        "/giftcard/save",
        "/giftcard/delete/7",
        "/cashoutrequest/7/approve",
        "/cashoutrequest/7/reject",
        "/cashoutrequest/7/mark-paid",
        "/customerwallet/wallet/generate-giftcard",
        "/customerwallet/wallet/cashout",
        "/customer-giftcard/buy",
        "/customer-giftcard/payment/card-uuid",
        "/customerredeem/save",
        "/wallet/top-up",
        "/wallet/delete/7",
        "/referral/delete/7",
        "/reward-redemption",
        "/lavelratesettings/save",
        "/lavelratesettings/delete/7",
        "/cashback-policy/save",
        "/cashback-policy/delete/7",
        "/admin/promotions/order-incentives/7/reverse",
        "/admin/promotions/fraud-flags/7/review"
    })
    void browserMutationWithCsrfReachesMvc(String path) throws Exception {
        mockMvc.perform(post(path).with(adminUser()).with(csrf()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/robots.txt",
        "/sitemap.xml",
        "/llms.txt",
        "/maintenance",
        "/register",
        "/customerregister/register"
    })
    void publicReadRoutesReachMvcWithoutLogin(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }

    @Test
    void publicDistrictSelectionRequiresCsrfButNotLogin() throws Exception {
        mockMvc.perform(post("/district/select"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/district/select").with(csrf()))
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

    @Test
    void logoutIsPostOnlyAndCsrfProtected() throws Exception {
        mockMvc.perform(get("/users/logout").with(adminUser()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/users/logout").with(adminUser()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/users/logout").with(adminUser()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/member-login"));
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser() {
        return user("security-admin").authorities(new SimpleGrantedAuthority("admin"));
    }
}
