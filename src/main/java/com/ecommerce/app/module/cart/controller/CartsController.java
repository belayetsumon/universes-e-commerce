/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingOptionService;
import com.ecommerce.app.product.ripository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
    ProductRepository productRepository;

    @Autowired
    ShippingOptionService shippingOptionService;

    @Autowired
    PackagingRateService packagingRateService;

    @Autowired
    CartService cartService;

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");

        Map<String, Object> response = new HashMap<>();

        // ---------- ALWAYS initialize ----------
        response.put("groupedCart", new HashMap<>());
        response.put("vendorSubtotals", new HashMap<>());
        response.put("vendorShippingCost", new HashMap<>());
        response.put("vendorPackagingCost", new HashMap<>());
        response.put("vendorShippingOptions", new HashMap<>());
        response.put("vendorPackagingOptions", new HashMap<>());
        response.put("grandTotal", BigDecimal.ZERO);

        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.ok(response);
        }

        Map<Long, List<CartItem>> grouped = cart.stream()
                .filter(c -> c.getProduct() != null && c.getProduct().getVendorprofile() != null)
                .collect(Collectors.groupingBy(c -> c.getProduct().getVendorprofile().getId()));

        Map<Long, BigDecimal> vendorSubtotals = new HashMap<>();
        Map<Long, BigDecimal> vendorShippingCost = new HashMap<>();
        Map<Long, BigDecimal> vendorPackagingCost = new HashMap<>();
        Map<Long, List<ShippingOption>> vendorShippingOptions = new HashMap<>();
        Map<Long, List<PackagingRate>> vendorPackagingOptions = new HashMap<>();
        District district = (District) session.getAttribute("shippingdistrict");

        for (Map.Entry<Long, List<CartItem>> entry : grouped.entrySet()) {

            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            BigDecimal subtotal = items.stream()
                    .map(c -> c.getItemTotal() != null ? c.getItemTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vendorSubtotals.put(vendorId, subtotal);

            BigDecimal totalWeight = items.stream()
                    .map(c -> (c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO)
                    .multiply(c.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vendorShippingOptions.put(
                    vendorId,
                    Optional.ofNullable(
                            shippingOptionService.getShippingOptions(vendorId, district, totalWeight)
                    ).orElse(Collections.emptyList())
            );

            vendorPackagingOptions.put(
                    vendorId,
                    Optional.ofNullable(
                            packagingRateService.getByVendor(vendorId)
                    ).orElse(Collections.emptyList())
            );

            BigDecimal shippingCost
                    = (BigDecimal) session.getAttribute("shippingCost_" + vendorId);

            BigDecimal packagingCost
                    = (BigDecimal) session.getAttribute("packagingCost_" + vendorId);

            vendorShippingCost.put(vendorId, shippingCost != null ? shippingCost : BigDecimal.ZERO);
            vendorPackagingCost.put(vendorId, packagingCost != null ? packagingCost : BigDecimal.ZERO);
        }

        BigDecimal grandTotal = grouped.keySet().stream()
                .map(vendorId
                        -> vendorSubtotals.get(vendorId)
                        .add(vendorShippingCost.get(vendorId))
                        .add(vendorPackagingCost.get(vendorId)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.put("groupedCart", grouped);
        response.put("vendorSubtotals", vendorSubtotals);
        response.put("vendorShippingCost", vendorShippingCost);
        response.put("vendorPackagingCost", vendorPackagingCost);
        response.put("vendorShippingOptions", vendorShippingOptions);
        response.put("vendorPackagingOptions", vendorPackagingOptions);
        response.put("grandTotal", grandTotal);
        return ResponseEntity.ok(response);
    }

    // Endpoint to update quantity
    @PostMapping("/updateQuantity")
    public ResponseEntity<Map<String, Object>> updateQuantity(@RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart != null) {
            cart.stream().filter(c -> c.getProduct().getId().equals(productId))
                    .forEach(c -> c.setQuantity(quantity));
        }
        session.setAttribute("sessioncart", cart);
        return getCart(session); // Return updated cart JSON
    }

    // Endpoint to remove item
    @PostMapping("/removeitem")
    public ResponseEntity<Map<String, Object>> removeItem(@RequestParam Long productId,
            HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart != null) {
            cart.removeIf(c -> c.getProduct().getId().equals(productId));
        }
        session.setAttribute("sessioncart", cart);
        return getCart(session);
    }

    // Endpoint to update shipping
    @PostMapping("/updateShipping")
    public ResponseEntity<Map<String, Object>> updateShipping(@RequestParam Long vendorId,
            @RequestParam BigDecimal shippingCost,
            HttpSession session) {
        session.setAttribute("shippingCost_" + vendorId, shippingCost);
        return getCart(session);
    }

    // Endpoint to update packaging
    @PostMapping("/updatePackaging")
    public ResponseEntity<Map<String, Object>> updatePackaging(@RequestParam Long vendorId,
            @RequestParam BigDecimal packagingCost,
            HttpSession session) {
        session.setAttribute("packagingCost_" + vendorId, packagingCost);
        return getCart(session);
    }
}
