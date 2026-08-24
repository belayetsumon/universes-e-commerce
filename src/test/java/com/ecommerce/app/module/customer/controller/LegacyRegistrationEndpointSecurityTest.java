package com.ecommerce.app.module.customer.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.ReferralRewards.controller.RegisterController;
import com.ecommerce.app.module.cart.controller.CartAddressController;
import com.ecommerce.app.module.customer.ReferralRewards.controller.RegisterCustomerController;
import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.user.model.Users;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

class LegacyRegistrationEndpointSecurityTest {

    private static final Set<Class<?>> LEGACY_CONTROLLERS = Set.of(
            RegisterController.class,
            RegisterCustomerController.class);

    @Test
    void legacyControllersNoLongerPublishFakeOrderOrWalletMutationRoutes() {
        Set<String> postMappings = LEGACY_CONTROLLERS.stream()
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(java.util.Objects::nonNull)
                .flatMap(mapping -> Arrays.stream(mapping.value()))
                .collect(Collectors.toSet());

        assertFalse(postMappings.contains("/order/create"));
        assertFalse(postMappings.contains("/order/complete"));
    }

    @Test
    void legacyRegistrationUsesPublicDtoInsteadOfUsersEntity() {
        for (Class<?> controllerType : LEGACY_CONTROLLERS) {
            Method registrationMethod = Arrays.stream(controllerType.getDeclaredMethods())
                    .filter(method -> method.getName().equals("registerUser"))
                    .findFirst()
                    .orElseThrow();
            assertTrue(Arrays.asList(registrationMethod.getParameterTypes()).contains(CustomerRegistrationForm.class));
            assertFalse(Arrays.asList(registrationMethod.getParameterTypes()).contains(Users.class));
        }
    }

    @Test
    void referralAdministrationRequiresNamedAdminAuthority() throws Exception {
        for (Class<?> controllerType : LEGACY_CONTROLLERS) {
            Method referralStats = controllerType.getDeclaredMethod("referralStats", org.springframework.ui.Model.class);
            PreAuthorize authorization = referralStats.getAnnotation(PreAuthorize.class);
            assertEquals("hasAnyAuthority('admin', 'ROLE_ADMIN')", authorization.value());
        }
    }

    @Test
    void cartAddressMutationsArePostOnly() throws Exception {
        for (String methodName : Set.of(
                "addBillingAddress",
                "addShippingAddress",
                "saveGuestDeliveryAddress")) {
            Method method = Arrays.stream(CartAddressController.class.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals(methodName))
                    .findFirst()
                    .orElseThrow();
            assertTrue(method.isAnnotationPresent(PostMapping.class));
        }
    }
}
