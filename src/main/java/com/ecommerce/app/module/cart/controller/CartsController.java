/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailability;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailabilityService;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingQuoteService;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author libertyerp_local
 */
@RestController
@RequestMapping("/carts")
public class CartsController {

    @Autowired
    ShippingQuoteService shippingQuoteService;

    @Autowired
    PackagingRateService packagingRateService;

    @Autowired
    CartService cartService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    CheckoutAvailabilityService checkoutAvailabilityService;

    @Autowired
    LoggedUserService loggedUserService;

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {

        List<CartItem> sessionCart = (List<CartItem>) session.getAttribute("sessioncart");
        int originalCartSize = sessionCart != null ? sessionCart.size() : 0;
        List<CartItem> cart = cartService.getCartFromSession(session);

        Map<String, Object> response = new HashMap<>();
        response.put("groupedCart", new LinkedHashMap<>());
        response.put("vendorSubtotals", new LinkedHashMap<>());
        response.put("vendorShippingCost", new LinkedHashMap<>());
        response.put("vendorPackagingCost", new LinkedHashMap<>());
        response.put("vendorShippingOptions", new LinkedHashMap<>());
        response.put("vendorPackagingOptions", new LinkedHashMap<>());
        response.put("vendorRequiresShipping", new LinkedHashMap<>());
        response.put("selectedShippingOption", new LinkedHashMap<>());
        response.put("selectedPackagingRate", new LinkedHashMap<>());
        response.put("grandTotal", BigDecimal.ZERO);
        if (originalCartSize > cart.size()) {
            response.put("cartWarning", "Some cart items were removed because they are not assigned to a vendor yet.");
        }

        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.ok(response);
        }

        Map<String, List<CartItem>> grouped = cart.stream()
                .filter(c -> c != null && c.getProduct() != null && c.getVendorUuid() != null && !c.getVendorUuid().isBlank())
                .collect(Collectors.groupingBy(CartItem::getVendorUuid, LinkedHashMap::new, Collectors.toList()));

        Map<String, BigDecimal> vendorSubtotals = new LinkedHashMap<>();
        Map<String, BigDecimal> vendorShippingCost = new LinkedHashMap<>();
        Map<String, BigDecimal> vendorPackagingCost = new LinkedHashMap<>();
        Map<String, List<ShippingOption>> vendorShippingOptions = new LinkedHashMap<>();
        Map<String, List<PackagingRate>> vendorPackagingOptions = new LinkedHashMap<>();
        Map<String, Boolean> vendorRequiresShipping = new LinkedHashMap<>();
        Map<String, String> selectedShippingOption = new LinkedHashMap<>();
        Map<String, String> selectedPackagingRate = new LinkedHashMap<>();
        ShippingLocation customerLocation = currentShippingLocation(session);

        for (Map.Entry<String, List<CartItem>> entry : grouped.entrySet()) {

            String vendorUuid = entry.getKey();
            List<CartItem> items = entry.getValue();
            boolean requiresShipping = cartService.vendorCartRequiresShipping(items);
            vendorRequiresShipping.put(vendorUuid, requiresShipping);

            BigDecimal subtotal = items.stream()
                    .map(c -> c.getItemTotal() != null ? c.getItemTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vendorSubtotals.put(vendorUuid, subtotal);

            BigDecimal totalWeight = items.stream()
                    .map(c -> (c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO)
                    .multiply(c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long vendorId = items.stream()
                    .map(CartItem::getVendorId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> items.stream()
                    .map(CartItem::getProduct)
                    .filter(Objects::nonNull)
                    .map(product -> product.getVendorprofile())
                    .filter(Objects::nonNull)
                    .map(vendor -> vendor.getId())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));

            vendorShippingOptions.put(
                    vendorUuid,
                    requiresShipping && vendorId != null
                            ? Optional.ofNullable(
                                    shippingQuoteService.getShippingOptions(vendorId, customerLocation, totalWeight, subtotal)
                            ).orElse(Collections.emptyList())
                            : Collections.emptyList()
            );

            vendorPackagingOptions.put(
                    vendorUuid,
                    requiresShipping
                            ? Optional.ofNullable(packagingRateService.getByVendorUuid(vendorUuid)).orElse(Collections.emptyList())
                            : Collections.emptyList()
            );

            BigDecimal shippingCost = (BigDecimal) session.getAttribute("shippingCost_" + vendorUuid);
            BigDecimal packagingCost = (BigDecimal) session.getAttribute("packagingCost_" + vendorUuid);

            if (!requiresShipping) {
                session.setAttribute("shippingCost_" + vendorUuid, BigDecimal.ZERO);
                session.setAttribute("packagingCost_" + vendorUuid, BigDecimal.ZERO);
                shippingCost = BigDecimal.ZERO;
                packagingCost = BigDecimal.ZERO;
                session.removeAttribute("shippingOption_" + vendorUuid);
                session.removeAttribute("packagingRate_" + vendorUuid);
            }

            vendorShippingCost.put(vendorUuid, shippingCost != null ? shippingCost : BigDecimal.ZERO);
            vendorPackagingCost.put(vendorUuid, packagingCost != null ? packagingCost : BigDecimal.ZERO);

            String shippingOptionCode = (String) session.getAttribute("shippingOption_" + vendorUuid);
            String packagingRateUuid = (String) session.getAttribute("packagingRate_" + vendorUuid);
            if (shippingOptionCode != null) {
                selectedShippingOption.put(vendorUuid, shippingOptionCode);
            }
            if (packagingRateUuid != null) {
                selectedPackagingRate.put(vendorUuid, packagingRateUuid);
            }
        }

        BigDecimal grandTotal = grouped.keySet().stream()
                .map(vendorUuid
                        -> vendorSubtotals.getOrDefault(vendorUuid, BigDecimal.ZERO)
                        .add(vendorShippingCost.getOrDefault(vendorUuid, BigDecimal.ZERO))
                        .add(vendorPackagingCost.getOrDefault(vendorUuid, BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.put("groupedCart", grouped);
        response.put("vendorSubtotals", vendorSubtotals);
        response.put("vendorShippingCost", vendorShippingCost);
        response.put("vendorPackagingCost", vendorPackagingCost);
        response.put("vendorShippingOptions", vendorShippingOptions);
        response.put("vendorPackagingOptions", vendorPackagingOptions);
        response.put("vendorRequiresShipping", vendorRequiresShipping);
        response.put("selectedShippingOption", selectedShippingOption);
        response.put("selectedPackagingRate", selectedPackagingRate);
        response.put("grandTotal", grandTotal);
        return ResponseEntity.ok(response);
    }

    private ShippingLocation currentShippingLocation(HttpSession session) {
        Object value = session.getAttribute("shippingLocation");
        return value instanceof ShippingLocation location ? location : null;
    }

    @PostMapping("/updateQuantity")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam(required = false) String productUuid,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String catalogVariantUuid,
            @RequestParam BigDecimal quantity,
            HttpSession session) {
        String resolvedProductUuid = resolveProductUuid(productUuid, productId);
        if (resolvedProductUuid == null) {
            return cartError("Product not found.", session);
        }
        boolean updated = cartService.updateQuantityInCart(resolvedProductUuid, normalizeUuid(catalogVariantUuid), quantity, session);
        if (!updated) {
            return cartError("Quantity could not be updated. Please check stock and try again.", session);
        }
        return getCart(session);
    }

    @PostMapping("/removeitem")
    public ResponseEntity<Map<String, Object>> removeItem(
            @RequestParam(required = false) String productUuid,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String catalogVariantUuid,
            HttpSession session) {
        String resolvedProductUuid = resolveProductUuid(productUuid, productId);
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart != null && resolvedProductUuid != null) {
            String resolvedCatalogVariantUuid = normalizeUuid(catalogVariantUuid);
            cart.removeIf(c -> Objects.equals(c.getProductUuid(), resolvedProductUuid)
                    && Objects.equals(normalizeUuid(c.getCatalogVariantUuid()), resolvedCatalogVariantUuid));
        }
        session.setAttribute("sessioncart", cart);
        return getCart(session);
    }

    @PostMapping("/updateShipping")
    public ResponseEntity<Map<String, Object>> updateShipping(
            @RequestParam(required = false) String vendorUuid,
            @RequestParam(required = false) Long vendorId,
            @RequestParam BigDecimal shippingCost,
            HttpSession session) {
        ResponseEntity<Map<String, Object>> unavailable = checkoutUnavailableIfDisabled(session);
        if (unavailable != null) {
            return unavailable;
        }
        String resolvedVendorUuid = resolveVendorUuid(vendorUuid, vendorId);
        if (resolvedVendorUuid == null) {
            return getCart(session);
        }
        List<CartItem> cart = cartService.getCartFromSession(session);
        List<CartItem> vendorCart = cartService.getVendorCart(cart, resolvedVendorUuid);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.setAttribute("shippingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }
        session.setAttribute("shippingCost_" + resolvedVendorUuid, shippingCost);
        return getCart(session);
    }

    @PostMapping("/updatePackaging")
    public ResponseEntity<Map<String, Object>> updatePackaging(
            @RequestParam(required = false) String vendorUuid,
            @RequestParam(required = false) Long vendorId,
            @RequestParam BigDecimal packagingCost,
            HttpSession session) {
        ResponseEntity<Map<String, Object>> unavailable = checkoutUnavailableIfDisabled(session);
        if (unavailable != null) {
            return unavailable;
        }
        String resolvedVendorUuid = resolveVendorUuid(vendorUuid, vendorId);
        if (resolvedVendorUuid == null) {
            return getCart(session);
        }
        List<CartItem> cart = cartService.getCartFromSession(session);
        List<CartItem> vendorCart = cartService.getVendorCart(cart, resolvedVendorUuid);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.setAttribute("packagingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }
        session.setAttribute("packagingCost_" + resolvedVendorUuid, packagingCost);
        return getCart(session);
    }

    @PostMapping("/updateShippingOption")
    public ResponseEntity<Map<String, Object>> updateShippingOption(
            @RequestParam(required = false) String vendorUuid,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String shippingOptionCode,
            HttpSession session) {

        ResponseEntity<Map<String, Object>> unavailable = checkoutUnavailableIfDisabled(session);
        if (unavailable != null) {
            return unavailable;
        }
        String resolvedVendorUuid = resolveVendorUuid(vendorUuid, vendorId);
        if (resolvedVendorUuid == null) {
            return getCart(session);
        }

        List<CartItem> cart = cartService.getCartFromSession(session);
        List<CartItem> vendorCart = cartService.getVendorCart(cart, resolvedVendorUuid);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.removeAttribute("shippingOption_" + resolvedVendorUuid);
            session.setAttribute("shippingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }

        if (shippingOptionCode == null || shippingOptionCode.isBlank() || "0".equals(shippingOptionCode)) {
            session.removeAttribute("shippingOption_" + resolvedVendorUuid);
            session.setAttribute("shippingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }

        BigDecimal shippingCost = cartService.calculateShipping(shippingOptionCode, resolvedVendorUuid, session);
        if (shippingCost == null) {
            shippingCost = BigDecimal.ZERO;
        }

        session.setAttribute("shippingOption_" + resolvedVendorUuid, shippingOptionCode);
        session.setAttribute("shippingCost_" + resolvedVendorUuid, shippingCost);
        return getCart(session);
    }

    @PostMapping("/updatePackagingRate")
    public ResponseEntity<Map<String, Object>> updatePackagingRate(
            @RequestParam(required = false) String vendorUuid,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String packagingRateUuid,
            @RequestParam(required = false) String packagingRateId,
            HttpSession session) {

        ResponseEntity<Map<String, Object>> unavailable = checkoutUnavailableIfDisabled(session);
        if (unavailable != null) {
            return unavailable;
        }
        String resolvedVendorUuid = resolveVendorUuid(vendorUuid, vendorId);
        if (resolvedVendorUuid == null) {
            return getCart(session);
        }

        List<CartItem> cartForVendor = cartService.getCartFromSession(session);
        List<CartItem> vendorCart = cartService.getVendorCart(cartForVendor, resolvedVendorUuid);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.removeAttribute("packagingRate_" + resolvedVendorUuid);
            session.setAttribute("packagingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }

        String resolvedPackagingRateUuid = resolvePackagingRateUuid(packagingRateUuid, packagingRateId);
        if (resolvedPackagingRateUuid == null || resolvedPackagingRateUuid.isBlank() || "0".equals(resolvedPackagingRateUuid)) {
            session.removeAttribute("packagingRate_" + resolvedVendorUuid);
            session.setAttribute("packagingCost_" + resolvedVendorUuid, BigDecimal.ZERO);
            return getCart(session);
        }

        BigDecimal totalWeight = vendorCart.stream()
                .map(c -> (c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO)
                .multiply(c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal packagingCost = BigDecimal.ZERO;
        try {
            BigDecimal calculated = packagingRateService.calculateRateOneByUuid(resolvedPackagingRateUuid, 0.5, totalWeight.doubleValue());
            if (calculated != null) {
                packagingCost = calculated;
            }
        } catch (Exception ignored) {
            packagingCost = BigDecimal.ZERO;
        }

        session.setAttribute("packagingRate_" + resolvedVendorUuid, resolvedPackagingRateUuid);
        session.setAttribute("packagingCost_" + resolvedVendorUuid, packagingCost);
        return getCart(session);
    }

    private String resolveProductUuid(String productUuid, Long productId) {
        if (productUuid != null && !productUuid.isBlank()) {
            return productUuid.trim();
        }
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId)
                .map(product -> product.getUuid())
                .orElse(null);
    }

    private ResponseEntity<Map<String, Object>> cartError(String message, HttpSession session) {
        Map<String, Object> body = getCart(session).getBody();
        if (body == null) {
            body = new HashMap<>();
        }
        body.put("success", false);
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> checkoutUnavailableIfDisabled(HttpSession session) {
        boolean authenticated = loggedUserService.isAuthenticatedUser();
        CheckoutAvailability availability = checkoutAvailabilityService.availability(authenticated);
        if (availability.isCheckoutAvailable()
                && (authenticated || !availability.isLoginRequired() || availability.isGuestAllowed())) {
            return null;
        }
        Map<String, Object> body = getCart(session).getBody();
        if (body == null) {
            body = new HashMap<>();
        }
        body.put("success", false);
        body.put("checkoutUnavailable", !availability.isCheckoutAvailable());
        body.put("message", availability.isCheckoutAvailable() ? "Please login before checkout." : CheckoutAvailabilityService.CHECKOUT_UNAVAILABLE_MESSAGE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private String normalizeUuid(String uuid) {
        return uuid == null || uuid.isBlank() ? null : uuid.trim();
    }

    private String resolveVendorUuid(String vendorUuid, Long vendorId) {
        if (vendorUuid != null && !vendorUuid.isBlank()) {
            return vendorUuid.trim();
        }
        if (vendorId == null) {
            return null;
        }
        return vendorprofileRepository.findById(vendorId)
                .map(vendor -> vendor.getUuid())
                .orElse(null);
    }

    private String resolvePackagingRateUuid(String packagingRateUuid, String packagingRateId) {
        if (packagingRateUuid != null && !packagingRateUuid.isBlank()) {
            return packagingRateUuid.trim();
        }
        if (packagingRateId == null || packagingRateId.isBlank()) {
            return null;
        }
        try {
            return Optional.ofNullable(packagingRateService.getById(Long.valueOf(packagingRateId.trim())))
                    .map(rate -> rate.getUuid())
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
