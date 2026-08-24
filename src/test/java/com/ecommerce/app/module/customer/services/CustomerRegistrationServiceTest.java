package com.ecommerce.app.module.customer.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.user.model.RegistrationSource;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CustomerCodMobileVerificationService mobileVerificationService;

    @Mock
    private ReferralService referralService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CustomerRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CustomerRegistrationService(
                usersRepository,
                roleRepository,
                passwordEncoder,
                mobileVerificationService,
                referralService,
                applicationEventPublisher);
    }

    @Test
    void registrationBuildsServerControlledCustomerAccount() {
        CustomerRegistrationForm form = validForm();
        Role customerRole = new Role(9L, "Customer", "customer", null, null);

        when(usersRepository.findByEmail("buyer@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strong-pass")).thenReturn("encoded-password");
        when(roleRepository.findBySlug("customer")).thenReturn(customerRole);
        when(usersRepository.saveAndFlush(any(Users.class))).thenAnswer(invocation -> {
            Users saved = invocation.getArgument(0);
            saved.setId(41L);
            return saved;
        });
        when(referralService.resolveReferrerByCode("ABC123")).thenReturn(null);
        org.mockito.Mockito.doAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setMobile("8801712345678");
            user.setMobileVerified(false);
            user.setMobileVerifiedAt(null);
            user.setMobileVerifiedNumber(null);
            return "8801712345678";
        }).when(mobileVerificationService)
                .updateMobileAndInvalidateVerificationIfChanged(any(Users.class), any(String.class));

        Users registered = service.register(form, " ABC123 ");

        assertEquals("Buyer", registered.getFirstName());
        assertEquals("Example", registered.getLastName());
        assertEquals("buyer@example.com", registered.getEmail());
        assertEquals("8801712345678", registered.getMobile());
        assertEquals("encoded-password", registered.getPassword());
        assertEquals(Status.Active, registered.getStatus());
        assertEquals(UserType.customer, registered.getUserType());
        assertEquals(RegistrationSource.CUSTOMER_REGISTRATION, registered.getRegistrationSource());
        assertEquals(java.util.Set.of(customerRole), registered.getRole());
        assertFalse(registered.isMobileVerified());
        assertFalse(registered.isEmailVerified());
        assertFalse(registered.isGuestAccount());
        assertTrue(registered.isPasswordConfigured());
        assertNull(registered.getParent());
        verify(referralService).createReferralProfileAndGrantSignupReward(registered, null);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void duplicateEmailIsRejectedBeforeAnyAccountWrite() {
        CustomerRegistrationForm form = validForm();
        when(usersRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(new Users()));

        CustomerRegistrationException exception = assertThrows(
                CustomerRegistrationException.class,
                () -> service.register(form, null));

        assertEquals("email", exception.getField());
        verify(usersRepository, never()).saveAndFlush(any(Users.class));
        verify(mobileVerificationService, never())
                .updateMobileAndInvalidateVerificationIfChanged(any(Users.class), any(String.class));
    }

    private CustomerRegistrationForm validForm() {
        CustomerRegistrationForm form = new CustomerRegistrationForm();
        form.setFirstName(" Buyer ");
        form.setLastName(" Example ");
        form.setEmail(" Buyer@Example.COM ");
        form.setMobile("01712-345678");
        form.setPassword("strong-pass");
        return form;
    }
}
