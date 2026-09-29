package com.ecommerce.app.module.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class CheckoutRequestIdentityServiceTest {

    private final CheckoutRequestIdentityService service = new CheckoutRequestIdentityService();

    @Test
    void requestIdIsStableUntilTheMatchingCheckoutCompletes() {
        MockHttpSession session = new MockHttpSession();

        String requestId = service.getOrCreateRequestId(session);

        assertNotNull(UUID.fromString(requestId));
        assertEquals(requestId, service.getOrCreateRequestId(session));

        service.clearIfMatches(session, UUID.randomUUID().toString());
        assertEquals(requestId, service.getOrCreateRequestId(session));

        service.clearIfMatches(session, requestId);
        assertNull(session.getAttribute(CheckoutRequestIdentityService.REQUEST_ID_SESSION_ATTRIBUTE));
        assertNotEquals(requestId, service.getOrCreateRequestId(session));
    }

    @Test
    void cartFingerprintIsOrderIndependentAndChangesWithCheckoutData() {
        CartItem first = item(20L, 200L, "variant-b", "2", "100.00");
        CartItem second = item(10L, 100L, "variant-a", "1", "50.00");

        String forward = service.cartFingerprint(List.of(first, second));
        String reversed = service.cartFingerprint(List.of(second, first));

        assertEquals(forward, reversed);
        assertTrue(forward.matches("[a-f0-9]{64}"));

        first.setQuantity(new BigDecimal("3"));
        assertNotEquals(forward, service.cartFingerprint(List.of(first, second)));
    }

    @Test
    void checkoutFingerprintIncludesUuidScopedDeliveryCosts() {
        MockHttpSession session = new MockHttpSession();
        CartItem item = item(20L, 200L, "variant-b", "2", "100.00");
        item.setVendorUuid("vendor-uuid");
        session.setAttribute("shippingCost_vendor-uuid", new BigDecimal("80.00"));
        session.setAttribute("packagingCost_vendor-uuid", new BigDecimal("20.00"));

        String original = service.checkoutFingerprint(session, List.of(item));
        session.setAttribute("shippingCost_vendor-uuid", new BigDecimal("90.00"));

        assertNotEquals(original, service.checkoutFingerprint(session, List.of(item)));
    }

    @Test
    void checkoutFingerprintIncludesSelectedLocationIdentity() {
        MockHttpSession session = new MockHttpSession();
        CartItem item = item(20L, 200L, "variant-b", "2", "100.00");
        ShippingLocation location = new ShippingLocation();
        location.setId(1L);
        location.setUuid("location-1");
        location.setCode("DHAKA");
        location.setName("Dhaka");
        location.setActive(true);
        session.setAttribute("shippingLocation", location);

        String original = service.checkoutFingerprint(session, List.of(item));
        location.setCode("CHITTAGONG");

        assertNotEquals(original, service.checkoutFingerprint(session, List.of(item)));
    }

    @Test
    void canonicalCheckoutFieldsAreUnambiguousAndCaseSensitive() {
        String first = service.canonicalFields("A|B", "C");
        String second = service.canonicalFields("A", "B|C");

        assertNotEquals(first, second);
        assertNotEquals(
                com.ecommerce.app.module.fraud.support.FraudHashingSupport.sha256Exact(first),
                com.ecommerce.app.module.fraud.support.FraudHashingSupport.sha256Exact(first.toLowerCase())
        );
    }

    private CartItem item(
            Long vendorId,
            Long productId,
            String variant,
            String quantity,
            String total
    ) {
        CartItem item = new CartItem();
        item.setVendorId(vendorId);
        item.setProductId(productId);
        item.setCatalogVariantUuid(variant);
        item.setQuantity(new BigDecimal(quantity));
        item.setItemTotal(new BigDecimal(total));
        return item;
    }
}
