package com.ecommerce.app.module.cart.services;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.ShippingQuoteService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Re-resolves checkout delivery charges from current server-side catalog data.
 * Browser-submitted amounts and previously cached session totals are never
 * treated as authoritative.
 */
@Service
public class CheckoutChargeValidationService {

    private static final BigDecimal PACKAGING_BASE_WEIGHT = new BigDecimal("0.50");

    private final CartService cartService;
    private final ShippingQuoteService shippingQuoteService;
    private final PackagingRateService packagingRateService;
    private final ShippingLocationService shippingLocationService;
    private final CheckoutAddressValidationService checkoutAddressValidationService;

    public CheckoutChargeValidationService(
            CartService cartService,
            ShippingQuoteService shippingQuoteService,
            PackagingRateService packagingRateService,
            ShippingLocationService shippingLocationService,
            CheckoutAddressValidationService checkoutAddressValidationService
    ) {
        this.cartService = cartService;
        this.shippingQuoteService = shippingQuoteService;
        this.packagingRateService = packagingRateService;
        this.shippingLocationService = shippingLocationService;
        this.checkoutAddressValidationService = checkoutAddressValidationService;
    }

    /**
     * @return null when all selected charges are valid, otherwise a safe
     * customer-facing validation message.
     */
    public String refreshAndValidate(
            HttpSession session,
            List<CartItem> cartItems,
            boolean requireShippingSelection,
            boolean codRequested
    ) {
        if (session == null) {
            return "Your checkout session expired. Please refresh the cart and try again.";
        }
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }

        Map<VendorKey, List<CartItem>> grouped = new LinkedHashMap<>();
        for (CartItem item : cartItems) {
            if (item == null || item.getVendorId() == null) {
                return "A cart item is missing its vendor. Please remove it and add it again.";
            }
            String vendorUuid = clean(item.getVendorUuid());
            VendorKey key = new VendorKey(item.getVendorId(), vendorUuid);
            grouped.computeIfAbsent(key, ignored -> new java.util.ArrayList<>()).add(item);
        }

        boolean shippingRequired = grouped.values().stream()
                .anyMatch(cartService::vendorCartRequiresShipping);
        ShippingLocation location = resolveCurrentLocation(session);

        if (requireShippingSelection) {
            BillingAddress billingAddress = sessionAttribute(
                    session,
                    "session_Billing_address",
                    BillingAddress.class
            );
            String billingError = checkoutAddressValidationService.validateBillingAddress(billingAddress);
            if (billingError != null) {
                return billingError;
            }
            if (shippingRequired) {
                ShippingAddress shippingAddress = sessionAttribute(
                        session,
                        "session_Shipping_address",
                        ShippingAddress.class
                );
                String shippingError = checkoutAddressValidationService.validateShippingAddress(
                        shippingAddress,
                        location
                );
                if (shippingError != null) {
                    return shippingError;
                }
            }
        }

        for (Map.Entry<VendorKey, List<CartItem>> entry : grouped.entrySet()) {
            VendorKey vendor = entry.getKey();
            List<CartItem> vendorItems = entry.getValue();
            String keySuffix = vendor.sessionKey();
            if (!cartService.vendorCartRequiresShipping(vendorItems)) {
                clearDeliverySelection(session, keySuffix);
                continue;
            }
            if (location == null) {
                return "Select a delivery location before placing this order.";
            }

            BigDecimal subtotal = safeMoney(cartService.calculateSubtotal(vendorItems));
            BigDecimal totalWeight = cartService.calculateTotalWeight(vendorItems);
            List<ShippingOption> currentOptions = shippingQuoteService.getShippingOptions(
                    vendor.id(),
                    location,
                    totalWeight,
                    subtotal
            );
            String selectedShippingCode = selectedValue(
                    session,
                    "shippingOption_",
                    vendor
            );
            if (selectedShippingCode == null) {
                session.removeAttribute("shippingCost_" + keySuffix);
                if (requireShippingSelection) {
                    return "Select a current shipping option for every vendor before placing the order.";
                }
            } else {
                ShippingOption selectedOption = currentOptions == null
                        ? null
                        : currentOptions.stream()
                                .filter(Objects::nonNull)
                                .filter(option -> selectedShippingCode.equals(clean(option.getCode()))
                                || selectedShippingCode.equals(clean(option.getRateUuid())))
                                .findFirst()
                                .orElse(null);
                if (selectedOption == null || selectedOption.getPrice() == null
                        || selectedOption.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    clearShippingSelection(session, keySuffix);
                    return "The selected shipping option is no longer available. Please choose it again.";
                }
                if (codRequested && !selectedOption.isCodAvailable()) {
                    return "Cash on Delivery is not available for the selected shipping option.";
                }
                String canonicalCode = clean(selectedOption.getCode()) != null
                        ? clean(selectedOption.getCode())
                        : clean(selectedOption.getRateUuid());
                session.setAttribute("shippingOption_" + keySuffix, canonicalCode);
                session.setAttribute("shippingCost_" + keySuffix, safeMoney(selectedOption.getPrice()));
            }

            String selectedPackagingKey = selectedValue(
                    session,
                    "packagingRate_",
                    vendor
            );
            if (selectedPackagingKey == null) {
                session.removeAttribute("packagingRate_" + keySuffix);
                session.setAttribute("packagingCost_" + keySuffix, BigDecimal.ZERO.setScale(2));
                continue;
            }

            List<PackagingRate> vendorRates = vendor.uuid() == null
                    ? packagingRateService.getByVendor(vendor.id())
                    : packagingRateService.getByVendorUuid(vendor.uuid());
            PackagingRate selectedRate = vendorRates == null
                    ? null
                    : vendorRates.stream()
                            .filter(Objects::nonNull)
                            .filter(PackagingRate::isActive)
                            .filter(rate -> selectedPackagingKey.equals(clean(rate.getUuid()))
                            || selectedPackagingKey.equals(String.valueOf(rate.getId())))
                            .findFirst()
                            .orElse(null);
            if (selectedRate == null || negative(selectedRate.getBasePrice())
                    || negative(selectedRate.getAdditionalPrice())) {
                clearPackagingSelection(session, keySuffix);
                return "The selected packaging option is no longer available. Please choose it again.";
            }

            BigDecimal packagingCost = safeMoney(selectedRate.getBasePrice());
            if (totalWeight.compareTo(PACKAGING_BASE_WEIGHT) > 0) {
                packagingCost = packagingCost.add(
                        safeMoney(selectedRate.getAdditionalPrice())
                                .multiply(totalWeight.subtract(PACKAGING_BASE_WEIGHT))
                );
            }
            session.setAttribute("packagingRate_" + keySuffix, clean(selectedRate.getUuid()));
            session.setAttribute("packagingCost_" + keySuffix, safeMoney(packagingCost));
        }
        return null;
    }

    private ShippingLocation resolveCurrentLocation(HttpSession session) {
        Object locationValue = session.getAttribute("shippingLocation");
        ShippingLocation selected = locationValue instanceof ShippingLocation value ? value : null;
        if (selected == null || selected.getId() == null) {
            return null;
        }
        ShippingLocation current = shippingLocationService.getById(selected.getId());
        if (current == null || !current.isActive()
                || (clean(selected.getUuid()) != null
                && !Objects.equals(clean(selected.getUuid()), clean(current.getUuid())))) {
            session.removeAttribute("shippingLocation");
            return null;
        }
        session.setAttribute("shippingLocation", current);
        return current;
    }

    private <T> T sessionAttribute(HttpSession session, String name, Class<T> type) {
        Object value = session.getAttribute(name);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    private String selectedValue(HttpSession session, String prefix, VendorKey vendor) {
        Object primary = session.getAttribute(prefix + vendor.sessionKey());
        if (primary instanceof String value && clean(value) != null) {
            return clean(value);
        }
        if (vendor.uuid() != null) {
            Object legacy = session.getAttribute(prefix + vendor.id());
            if (legacy instanceof String value && clean(value) != null) {
                return clean(value);
            }
        }
        return null;
    }

    private void clearDeliverySelection(HttpSession session, String suffix) {
        clearShippingSelection(session, suffix);
        clearPackagingSelection(session, suffix);
    }

    private void clearShippingSelection(HttpSession session, String suffix) {
        session.removeAttribute("shippingOption_" + suffix);
        session.setAttribute("shippingCost_" + suffix, BigDecimal.ZERO.setScale(2));
    }

    private void clearPackagingSelection(HttpSession session, String suffix) {
        session.removeAttribute("packagingRate_" + suffix);
        session.setAttribute("packagingCost_" + suffix, BigDecimal.ZERO.setScale(2));
    }

    private boolean negative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record VendorKey(Long id, String uuid) {

        private String sessionKey() {
            return uuid == null ? String.valueOf(id) : uuid;
        }
    }
}
