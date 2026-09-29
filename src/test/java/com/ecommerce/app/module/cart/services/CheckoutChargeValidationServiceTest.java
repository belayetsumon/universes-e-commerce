package com.ecommerce.app.module.cart.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.ShippingQuoteService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class CheckoutChargeValidationServiceTest {

    private CartService cartService;
    private ShippingQuoteService shippingQuoteService;
    private PackagingRateService packagingRateService;
    private ShippingLocationService shippingLocationService;
    private CheckoutChargeValidationService service;
    private CartItem item;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        shippingQuoteService = mock(ShippingQuoteService.class);
        packagingRateService = mock(PackagingRateService.class);
        shippingLocationService = mock(ShippingLocationService.class);
        service = new CheckoutChargeValidationService(
                cartService,
                shippingQuoteService,
                packagingRateService,
                shippingLocationService,
                new CheckoutAddressValidationService(new MobileNumberNormalizationService())
        );
        item = new CartItem();
        item.setVendorId(7L);
        item.setVendorUuid("vendor-7");
        item.setItemTotal(new BigDecimal("1000.00"));
        item.setWeight(new BigDecimal("2.00"));
        session = new MockHttpSession();
        ShippingLocation location = new ShippingLocation();
        location.setId(99L);
        location.setUuid("location-99");
        location.setName("Gulshan");
        location.setCode("GULSHAN");
        location.setActive(true);
        session.setAttribute("shippingLocation", location);
        when(shippingLocationService.getById(99L)).thenReturn(location);

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFirstName("Customer");
        billingAddress.setMobile("01712345678");
        billingAddress.setAddressLineOne("House 1, Road 2");
        session.setAttribute("session_Billing_address", billingAddress);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Customer");
        shippingAddress.setMobile("01712345678");
        shippingAddress.setAddressLineOne("House 1, Road 2");
        shippingAddress.setCountry("Bangladesh");
        shippingAddress.setDistrict("Gulshan");
        shippingAddress.setCity("Gulshan");
        session.setAttribute("session_Shipping_address", shippingAddress);

        when(cartService.vendorCartRequiresShipping(anyList())).thenReturn(true);
        when(cartService.calculateSubtotal(anyList())).thenReturn(new BigDecimal("1000.00"));
        when(cartService.calculateTotalWeight(anyList())).thenReturn(new BigDecimal("2.00"));
    }

    @Test
    void overwritesForgedSessionAmountsWithCurrentServerRates() {
        ShippingOption option = shippingOption("rate-1", "120.00", true);
        PackagingRate packaging = packagingRate("pack-1", "20.00", "5.00");
        when(shippingQuoteService.getShippingOptions(eq(7L), any(), any(), any()))
                .thenReturn(List.of(option));
        when(packagingRateService.getByVendorUuid("vendor-7"))
                .thenReturn(List.of(packaging));
        session.setAttribute("shippingOption_vendor-7", "rate-1");
        session.setAttribute("packagingRate_vendor-7", "pack-1");
        session.setAttribute("shippingCost_vendor-7", new BigDecimal("-999.00"));
        session.setAttribute("packagingCost_vendor-7", BigDecimal.ZERO);

        String error = service.refreshAndValidate(session, List.of(item), true, true);

        assertNull(error);
        assertEquals(new BigDecimal("120.00"), session.getAttribute("shippingCost_vendor-7"));
        assertEquals(new BigDecimal("27.50"), session.getAttribute("packagingCost_vendor-7"));
    }

    @Test
    void finalCheckoutRequiresAnExplicitCurrentShippingOption() {
        when(shippingQuoteService.getShippingOptions(eq(7L), any(), any(), any()))
                .thenReturn(List.of(shippingOption("rate-1", "0.00", true)));

        String error = service.refreshAndValidate(session, List.of(item), true, false);

        assertNotNull(error);
        assertNull(session.getAttribute("shippingCost_vendor-7"));
    }

    @Test
    void rejectsUnknownShippingRateInsteadOfUsingCachedCost() {
        when(shippingQuoteService.getShippingOptions(eq(7L), any(), any(), any()))
                .thenReturn(List.of(shippingOption("rate-current", "80.00", true)));
        session.setAttribute("shippingOption_vendor-7", "rate-from-other-vendor");
        session.setAttribute("shippingCost_vendor-7", BigDecimal.ONE);

        String error = service.refreshAndValidate(session, List.of(item), true, false);

        assertNotNull(error);
        assertEquals(new BigDecimal("0.00"), session.getAttribute("shippingCost_vendor-7"));
    }

    @Test
    void rejectsCodWhenCurrentCarrierRateDoesNotSupportIt() {
        when(shippingQuoteService.getShippingOptions(eq(7L), any(), any(), any()))
                .thenReturn(List.of(shippingOption("rate-1", "80.00", false)));
        session.setAttribute("shippingOption_vendor-7", "rate-1");

        String error = service.refreshAndValidate(session, List.of(item), true, true);

        assertNotNull(error);
    }

    @Test
    void finalCheckoutRejectsShippingAddressThatDoesNotMatchSelectedLocation() {
        ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
        shippingAddress.setDistrict("Different district");
        session.setAttribute("shippingOption_vendor-7", "rate-1");
        when(shippingQuoteService.getShippingOptions(eq(7L), any(), any(), any()))
                .thenReturn(List.of(shippingOption("rate-1", "80.00", true)));

        String error = service.refreshAndValidate(session, List.of(item), true, true);

        assertNotNull(error);
    }

    @Test
    void finalCheckoutRejectsBlankAddressObjectFromDirectPost() {
        session.setAttribute("session_Billing_address", new BillingAddress());

        String error = service.refreshAndValidate(session, List.of(item), true, true);

        assertNotNull(error);
    }

    private ShippingOption shippingOption(String code, String price, boolean codAvailable) {
        ShippingOption option = new ShippingOption();
        option.setCode(code);
        option.setRateUuid(code);
        option.setPrice(new BigDecimal(price));
        option.setCodAvailable(codAvailable);
        return option;
    }

    private PackagingRate packagingRate(String uuid, String base, String additional) {
        PackagingRate rate = new PackagingRate();
        rate.setId(11L);
        rate.setUuid(uuid);
        rate.setActive(true);
        rate.setBasePrice(new BigDecimal(base));
        rate.setAdditionalPrice(new BigDecimal(additional));
        return rate;
    }
}
