package com.ecommerce.app.module.customer.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.customer.dto.CustomerAccountForm;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class CustomerProfileControllerMobileSecurityTest {

    @Mock
    private LoggedUserService loggedUserService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private CustomerCodMobileVerificationService mobileVerificationService;

    private CustomerProfileController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerProfileController();
        ReflectionTestUtils.setField(controller, "loggedUserService", loggedUserService);
        ReflectionTestUtils.setField(controller, "usersRepository", usersRepository);
        ReflectionTestUtils.setField(controller, "mobileNumberNormalizationService", new MobileNumberNormalizationService());
        ReflectionTestUtils.setField(controller, "customerCodMobileVerificationService", mobileVerificationService);
    }

    @Test
    void changedMobileIsNormalizedAndVerificationIsInvalidatedBeforeSave() {
        Users currentUser = new Users();
        currentUser.setId(7L);
        currentUser.setFirstName("Existing");
        currentUser.setLastName("Customer");
        currentUser.setMobile("8801711111111");
        currentUser.setMobileVerified(true);
        currentUser.setMobileVerifiedAt(LocalDateTime.now());
        currentUser.setMobileVerifiedNumber("8801711111111");

        CustomerAccountForm form = new CustomerAccountForm();
        form.setFirstName("Updated");
        form.setLastName("Customer");
        form.setMobile("01812-345678");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "accountForm");

        when(loggedUserService.activeUserid()).thenReturn(7L);
        when(usersRepository.findById(7L)).thenReturn(Optional.of(currentUser));
        when(usersRepository.findByMobile("8801812345678")).thenReturn(null);
        org.mockito.Mockito.doAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            String normalizedMobile = invocation.getArgument(1);
            user.setMobile(normalizedMobile);
            user.setMobileVerified(false);
            user.setMobileVerifiedAt(null);
            user.setMobileVerifiedNumber(null);
            return normalizedMobile;
        }).when(mobileVerificationService)
                .updateMobileAndInvalidateVerificationIfChanged(currentUser, "8801812345678");

        String view = controller.updateAccount(
                form,
                bindingResult,
                new org.springframework.ui.ConcurrentModel(),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/customer-profile/index", view);
        assertEquals("8801812345678", currentUser.getMobile());
        assertFalse(currentUser.isMobileVerified());
        verify(usersRepository).findByMobile("8801812345678");
        verify(mobileVerificationService)
                .updateMobileAndInvalidateVerificationIfChanged(currentUser, "8801812345678");
        verify(usersRepository).save(currentUser);
    }
}
