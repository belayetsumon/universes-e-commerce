package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Creates the browser-visible checkout request identity and a server-derived
 * cart fingerprint. Both checkout pages use this service so a retry after a
 * committed order resolves to the original outcome instead of creating a new
 * order.
 */
@Service
public class CheckoutRequestIdentityService {

    public static final String REQUEST_ID_SESSION_ATTRIBUTE = "checkoutPlacement.requestId";

    public String getOrCreateRequestId(HttpSession session) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            Object existing = session.getAttribute(REQUEST_ID_SESSION_ATTRIBUTE);
            if (existing instanceof String existingId && isValidRequestId(existingId)) {
                return existingId;
            }
            String generated = UUID.randomUUID().toString();
            session.setAttribute(REQUEST_ID_SESSION_ATTRIBUTE, generated);
            return generated;
        }
    }

    public boolean isValidRequestId(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String cartFingerprint(List<CartItem> cartItems) {
        List<String> itemParts = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                if (item == null) {
                    continue;
                }
                itemParts.add(canonicalFields(
                        String.valueOf(item.getVendorId()),
                        String.valueOf(item.getProductId()),
                        safeText(item.getCatalogVariantUuid()),
                        String.valueOf(item.getQuantity()),
                        String.valueOf(safeMoney(item.getItemTotal()))));
            }
        }
        itemParts.sort(String::compareTo);
        return FraudHashingSupport.sha256Exact(canonicalFields(itemParts.toArray(String[]::new)));
    }

    public String checkoutFingerprint(HttpSession session, List<CartItem> cartItems) {
        List<String> contextParts = new ArrayList<>();
        contextParts.add("cart=" + safeText(cartFingerprint(cartItems)));

        List<String> vendorCostParts = new ArrayList<>();
        if (cartItems != null) {
            cartItems.stream()
                    .filter(Objects::nonNull)
                    .map(CartItem::getVendorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(vendorId -> {
                        List<CartItem> vendorItems = cartItems.stream()
                                .filter(Objects::nonNull)
                                .filter(item -> Objects.equals(vendorId, item.getVendorId()))
                                .toList();
                        vendorCostParts.add(canonicalFields(
                                String.valueOf(vendorId),
                                String.valueOf(sessionMoney(session, "shippingCost_", vendorId, vendorItems)),
                                String.valueOf(sessionMoney(session, "packagingCost_", vendorId, vendorItems))
                        ));
                    });
        }
        vendorCostParts.sort(String::compareTo);
        contextParts.add(canonicalFields("costs", canonicalFields(vendorCostParts.toArray(String[]::new))));

        BillingAddress billing = sessionAttribute(session, "session_Billing_address", BillingAddress.class);
        ShippingAddress shipping = sessionAttribute(session, "session_Shipping_address", ShippingAddress.class);
        ShippingLocation location = sessionAttribute(session, "shippingLocation", ShippingLocation.class);
        contextParts.add(canonicalFields(
                "location",
                location == null ? null : String.valueOf(location.getId()),
                location == null ? null : location.getUuid(),
                location == null || location.getType() == null ? null : location.getType().name(),
                location == null ? null : location.getCode(),
                location == null ? null : location.getName(),
                location == null ? null : location.getDisplayLabel(),
                location == null ? null : String.valueOf(location.isActive())
        ));
        contextParts.add(canonicalFields("billing", address(
                billing == null ? null : billing.getFirstName(),
                billing == null ? null : billing.getLastName(),
                billing == null ? null : billing.getEmail(),
                billing == null ? null : billing.getMobile(),
                billing == null ? null : billing.getCompany(),
                billing == null ? null : billing.getCountry(),
                billing == null ? null : billing.getDistrict(),
                billing == null ? null : billing.getAddressLineOne(),
                billing == null ? null : billing.getAddressLinetwo(),
                billing == null ? null : billing.getCity(),
                billing == null ? null : billing.getPostCode()
        )));
        contextParts.add(canonicalFields("shipping", address(
                shipping == null ? null : shipping.getFirstName(),
                shipping == null ? null : shipping.getLastName(),
                shipping == null ? null : shipping.getEmail(),
                shipping == null ? null : shipping.getMobile(),
                shipping == null ? null : shipping.getCompany(),
                shipping == null ? null : shipping.getCountry(),
                shipping == null ? null : shipping.getDistrict(),
                shipping == null ? null : shipping.getAddressLineOne(),
                shipping == null ? null : shipping.getAddressLinetwo(),
                shipping == null ? null : shipping.getCity(),
                shipping == null ? null : shipping.getPostCode()
        )));
        return FraudHashingSupport.sha256Exact(canonicalFields(contextParts.toArray(String[]::new)));
    }

    public void clearIfMatches(HttpSession session, String completedRequestId) {
        if (session == null) {
            return;
        }
        synchronized (session) {
            Object current = session.getAttribute(REQUEST_ID_SESSION_ATTRIBUTE);
            if (Objects.equals(current, completedRequestId)) {
                session.removeAttribute(REQUEST_ID_SESSION_ATTRIBUTE);
            }
        }
    }

    public void clearCurrentRequestId(HttpSession session) {
        if (session == null) {
            return;
        }
        synchronized (session) {
            session.removeAttribute(REQUEST_ID_SESSION_ATTRIBUTE);
        }
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal sessionMoney(
            HttpSession session,
            String prefix,
            Long vendorId,
            List<CartItem> vendorItems
    ) {
        if (session == null) {
            return safeMoney(null);
        }
        String vendorUuid = vendorItems == null
                ? null
                : vendorItems.stream()
                        .filter(Objects::nonNull)
                        .map(CartItem::getVendorUuid)
                        .filter(uuid -> uuid != null && !uuid.isBlank())
                        .findFirst()
                        .orElse(null);
        if (vendorUuid != null) {
            Object uuidValue = session.getAttribute(prefix + vendorUuid);
            if (uuidValue instanceof BigDecimal amount) {
                return safeMoney(amount);
            }
        }
        Object idValue = session.getAttribute(prefix + vendorId);
        return idValue instanceof BigDecimal amount ? safeMoney(amount) : safeMoney(null);
    }

    private <T> T sessionAttribute(HttpSession session, String name, Class<T> type) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(name);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public String canonicalFields(String... fields) {
        StringBuilder canonical = new StringBuilder();
        if (fields == null) {
            return canonical.toString();
        }
        for (String field : fields) {
            String value = field == null ? "" : field;
            canonical.append(value.length()).append(':').append(value);
        }
        return canonical.toString();
    }

    private String address(String... fields) {
        String[] values = new String[fields.length];
        for (int index = 0; index < fields.length; index++) {
            values[index] = safeText(fields[index]);
        }
        return canonicalFields(values);
    }
}
