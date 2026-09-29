package com.ecommerce.app.module.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.user.services.SessionAdministrationService;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;

class UsersControllerRegistrationBindingTest {

    @Test
    void publicRegistrationSuppressesIdentityAndAuthorityParameters() {
        CustomerRegistrationForm form = new CustomerRegistrationForm();
        WebDataBinder binder = new WebDataBinder(form, "users");
        new UsersController(Mockito.mock(SessionAdministrationService.class)).configureUserBinding(binder);

        binder.bind(new MutablePropertyValues(Map.ofEntries(
                Map.entry("firstName", "Safe"),
                Map.entry("lastName", "Customer"),
                Map.entry("email", "safe@example.com"),
                Map.entry("mobile", "01712345678"),
                Map.entry("password", "strong-pass"),
                Map.entry("id", "99"),
                Map.entry("role", "1"),
                Map.entry("status", "Active"),
                Map.entry("userType", "systemadmin"),
                Map.entry("mobileVerified", "true"),
                Map.entry("emailVerified", "true"),
                Map.entry("guestAccount", "true"),
                Map.entry("passwordConfigured", "false"),
                Map.entry("registrationSource", "ADMIN")
        )));

        assertEquals("Safe", form.getFirstName());
        assertEquals("safe@example.com", form.getEmail());
        var suppressed = Arrays.asList(binder.getBindingResult().getSuppressedFields());
        assertTrue(suppressed.containsAll(java.util.List.of(
                "id",
                "role",
                "status",
                "userType",
                "mobileVerified",
                "emailVerified",
                "guestAccount",
                "passwordConfigured",
                "registrationSource")));
    }
}
