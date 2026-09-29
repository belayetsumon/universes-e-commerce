package com.ecommerce.app.module.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutSessionService;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class SalesOrderControllerRegisteredCustomerLockTest {

    @Test
    void registeredPlacementResolvesCustomerWithPessimisticRowLock() {
        LoggedUserService loggedUserService = mock(LoggedUserService.class);
        UsersRepository usersRepository = mock(UsersRepository.class);
        GuestCheckoutSessionService guestCheckoutSessionService = mock(GuestCheckoutSessionService.class);
        Users customer = new Users();
        customer.setId(7L);

        when(loggedUserService.isAuthenticatedUser()).thenReturn(true);
        when(loggedUserService.activeUserid()).thenReturn(7L);
        when(usersRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(customer));

        SalesOrderController controller = new SalesOrderController();
        ReflectionTestUtils.setField(controller, "loggedUserService", loggedUserService);
        ReflectionTestUtils.setField(controller, "usersRepository", usersRepository);
        ReflectionTestUtils.setField(controller, "guestCheckoutSessionService", guestCheckoutSessionService);

        Users resolved = ReflectionTestUtils.invokeMethod(
                controller,
                "resolveCheckoutCustomer",
                new MockHttpSession(),
                new RedirectAttributesModelMap(),
                "/cart/checkout"
        );

        assertSame(customer, resolved);
        verify(guestCheckoutSessionService).clear(org.mockito.ArgumentMatchers.any());
        verify(usersRepository).findByIdForUpdate(7L);
    }

    @Test
    void authenticatedOrderIgnoresAStaleGuestProofAndSnapshotsCustomerProof() {
        LoggedUserService loggedUserService = mock(LoggedUserService.class);
        GuestCheckoutSessionService guestCheckoutSessionService = mock(GuestCheckoutSessionService.class);
        CustomerCodMobileVerificationService customerVerificationService
                = mock(CustomerCodMobileVerificationService.class);
        StoreOperationModeService storeOperationModeService = mock(StoreOperationModeService.class);
        Users customer = new Users();
        customer.setId(7L);
        customer.setMobile("8801712345678");
        customer.setMobileVerified(true);
        customer.setMobileVerifiedAt(LocalDateTime.now());
        customer.setMobileVerifiedNumber("8801712345678");
        GuestCheckoutSession staleGuest = new GuestCheckoutSession();
        staleGuest.setVerifiedMobile("8801812345678");
        staleGuest.setMobileVerificationStatus(MobileVerificationStatus.VERIFIED);

        when(loggedUserService.isAuthenticatedUser()).thenReturn(true);
        when(guestCheckoutSessionService.current(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(staleGuest));
        when(customerVerificationService.isCurrentMobileVerified(customer)).thenReturn(true);
        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(true);

        SalesOrderController controller = new SalesOrderController();
        ReflectionTestUtils.setField(controller, "loggedUserService", loggedUserService);
        ReflectionTestUtils.setField(controller, "guestCheckoutSessionService", guestCheckoutSessionService);
        ReflectionTestUtils.setField(controller, "customerCodMobileVerificationService", customerVerificationService);
        ReflectionTestUtils.setField(controller, "storeOperationModeService", storeOperationModeService);
        ReflectionTestUtils.setField(
                controller,
                "mobileNumberNormalizationService",
                new MobileNumberNormalizationService()
        );
        SalesOrder order = new SalesOrder();

        ReflectionTestUtils.invokeMethod(
                controller,
                "applyCheckoutMobileVerificationMetadata",
                order,
                customer,
                new MockHttpSession()
        );

        assertFalse(order.isGuestCheckout());
        assertEquals("8801712345678", order.getMobileNumber());
        assertEquals(MobileVerificationStatus.VERIFIED, order.getMobileVerificationStatus());
    }
}
