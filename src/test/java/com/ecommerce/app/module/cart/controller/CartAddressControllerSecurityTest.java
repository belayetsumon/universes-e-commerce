package com.ecommerce.app.module.cart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.cart.services.CheckoutAddressValidationService;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailability;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailabilityService;
import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutSessionService;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class CartAddressControllerSecurityTest {

    private CartAddressController controller;
    private MockHttpSession session;
    private LoggedUserService loggedUserService;
    private GuestCheckoutSessionService guestCheckoutSessionService;
    private CheckoutAvailabilityService checkoutAvailabilityService;

    @BeforeEach
    void setUp() {
        loggedUserService = mock(LoggedUserService.class);
        guestCheckoutSessionService = mock(GuestCheckoutSessionService.class);
        checkoutAvailabilityService = mock(CheckoutAvailabilityService.class);

        controller = new CartAddressController();
        ReflectionTestUtils.setField(controller, "loggedUserService", loggedUserService);
        ReflectionTestUtils.setField(controller, "guestCheckoutSessionService", guestCheckoutSessionService);
        ReflectionTestUtils.setField(controller, "checkoutAvailabilityService", checkoutAvailabilityService);
        ReflectionTestUtils.setField(
                controller,
                "checkoutAddressValidationService",
                new CheckoutAddressValidationService(new MobileNumberNormalizationService())
        );

        when(loggedUserService.isAuthenticatedUser()).thenReturn(true);
        when(checkoutAvailabilityService.availability(true)).thenReturn(new CheckoutAvailability(
                true, false, true, true, false, false, false, "/order/create", null
        ));

        ShippingLocation location = new ShippingLocation();
        location.setId(10L);
        location.setUuid("location-10");
        location.setCode("GULSHAN");
        location.setName("Gulshan");
        location.setActive(true);
        session = new MockHttpSession();
        session.setAttribute("shippingLocation", location);
    }

    @Test
    void stripsPersistenceFieldsAndCanonicalizesLocationAndMobile() {
        BillingAddress submitted = validBillingAddress();
        submitted.setId(999L);
        submitted.setUserId(new Users());
        submitted.setCreatedBy("attacker");
        submitted.setCreated(LocalDateTime.now());
        submitted.setCountry("Forged country");
        submitted.setDistrict("Forged district");
        submitted.setCity("Forged city");

        String view = controller.addBillingAddress(
                new ConcurrentModel(),
                session,
                submitted,
                true,
                new RedirectAttributesModelMap()
        );

        assertEquals("redirect:/order/create", view);
        BillingAddress billing = (BillingAddress) session.getAttribute("session_Billing_address");
        ShippingAddress shipping = (ShippingAddress) session.getAttribute("session_Shipping_address");
        assertNull(billing.getId());
        assertNull(billing.getUserId());
        assertNull(billing.getCreatedBy());
        assertNull(billing.getCreated());
        assertEquals("8801712345678", billing.getMobile());
        assertEquals("Bangladesh", shipping.getCountry());
        assertEquals("Gulshan", shipping.getDistrict());
        assertEquals("Gulshan", shipping.getCity());
        assertEquals("8801712345678", shipping.getMobile());
    }

    @Test
    void directPostWithBlankRequiredFieldsIsRejectedAndClearsOldAddresses() {
        session.setAttribute("session_Billing_address", validBillingAddress());
        session.setAttribute("session_Shipping_address", new ShippingAddress());
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.addBillingAddress(
                new ConcurrentModel(),
                session,
                new BillingAddress(),
                true,
                redirectAttributes
        );

        assertEquals("redirect:/order/create", view);
        assertNull(session.getAttribute("session_Billing_address"));
        assertNull(session.getAttribute("session_Shipping_address"));
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
        assertFalse(redirectAttributes.getFlashAttributes().get("errorMessage").toString().isBlank());
    }

    @Test
    void verifiedGuestShippingPostCannotReplaceTheOtpMobile() {
        when(loggedUserService.isAuthenticatedUser()).thenReturn(false);
        when(checkoutAvailabilityService.availability(false)).thenReturn(new CheckoutAvailability(
                false, true, false, true, false, true, false, "/cart/checkout", null
        ));
        GuestCheckoutSession guestSession = new GuestCheckoutSession();
        guestSession.setVerifiedMobile("8801712345678");
        guestSession.setMobileVerificationStatus(MobileVerificationStatus.VERIFIED);
        when(guestCheckoutSessionService.current(session)).thenReturn(Optional.of(guestSession));

        ShippingAddress submitted = new ShippingAddress();
        submitted.setFirstName("Guest customer");
        submitted.setMobile("01812345678");
        submitted.setAddressLineOne("House 1, Road 2");

        String view = controller.addShippingAddress(
                new ConcurrentModel(),
                session,
                submitted,
                new RedirectAttributesModelMap()
        );

        assertEquals("redirect:/order/create", view);
        ShippingAddress stored = (ShippingAddress) session.getAttribute("session_Shipping_address");
        assertEquals("8801712345678", stored.getMobile());
    }

    private BillingAddress validBillingAddress() {
        BillingAddress address = new BillingAddress();
        address.setFirstName("Customer");
        address.setLastName("One");
        address.setEmail("customer@example.com");
        address.setMobile("01712-345678");
        address.setAddressLineOne("House 1, Road 2");
        return address;
    }
}
