/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingOptionService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.ProductRepository;
import jakarta.servlet.http.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/cart")
@SessionAttributes
public class CartController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ShippingOptionService shippingOptionService;

    @Autowired
    PackagingRateService packagingRateService;

    @Autowired
    CartService cartService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, HttpSession session) {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");

        if (cart != null && !cart.isEmpty()) {

            // Group by vendor ID
            Map<Long, List<CartItem>> grouped = cart.stream()
                    .collect(Collectors.groupingBy(c -> c.getProduct().getVendorprofile().getId()));

            // Data holders
            Map<Long, BigDecimal> vendorSubtotals = new HashMap<>();
            Map<Long, BigDecimal> vendorWeights = new HashMap<>();
            Map<Long, List<ShippingOption>> vendorShippingOptions = new HashMap<>();
            Map<Long, List<PackagingRate>> vendorPackagingOptions = new HashMap<>();
            Map<Long, BigDecimal> vendorShippingCost = new HashMap<>();
            Map<Long, BigDecimal> vendorPackagingCost = new HashMap<>();

            //  Loop vendors
            for (Map.Entry<Long, List<CartItem>> entry : grouped.entrySet()) {
                Long vendorId = entry.getKey();
                List<CartItem> items = entry.getValue();

                // ✅ Subtotal
                BigDecimal subtotal = items.stream()
                        .map(CartItem::getItemTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                vendorSubtotals.put(vendorId, subtotal);

                // ✅ Total weight
//                BigDecimal totalWeight = items.stream()
//                        .map(c -> {
//                            BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
//                            BigDecimal qty = c.getQuantity() != null ? new BigDecimal(c.getQuantity()) : BigDecimal.ZERO;
//                            return weight.multiply(qty);
//                        })
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalWeight = items.stream()
                        .map(c -> {
                            BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
                            BigDecimal qty = c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO;
                            return weight.multiply(qty);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                vendorWeights.put(vendorId, totalWeight);

                // ✅ Load shipping options for this vendor
                District district = (District) session.getAttribute("shippingdistrict");
                List<ShippingOption> shippingOptions = shippingOptionService.getShippingOptions(vendorId, district, totalWeight);
                vendorShippingOptions.put(vendorId, shippingOptions);

                // ✅ Load packaging options for this vendor
                List<PackagingRate> packagingRates = packagingRateService.getByVendor(vendorId);
                vendorPackagingOptions.put(vendorId, packagingRates);

                // ✅ Restore shipping/packaging costs from session (if already selected)
                BigDecimal shippingCost = (BigDecimal) session.getAttribute("shippingCost_" + vendorId);
                BigDecimal packagingCost = (BigDecimal) session.getAttribute("packagingCost_" + vendorId);

                vendorShippingCost.put(vendorId, shippingCost != null ? shippingCost : BigDecimal.ZERO);
                vendorPackagingCost.put(vendorId, packagingCost != null ? packagingCost : BigDecimal.ZERO);
            }

            // ✅ Grand total (subtotal + shipping + packaging)
            BigDecimal grandTotal = grouped.keySet().stream()
                    .map(vendorId
                            -> vendorSubtotals.getOrDefault(vendorId, BigDecimal.ZERO)
                            .add(vendorShippingCost.getOrDefault(vendorId, BigDecimal.ZERO))
                            .add(vendorPackagingCost.getOrDefault(vendorId, BigDecimal.ZERO))
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ✅ Add everything to model
            model.addAttribute("groupedCart", grouped);
            model.addAttribute("vendorSubtotals", vendorSubtotals);
            model.addAttribute("vendorWeights", vendorWeights);
            model.addAttribute("vendorShippingOptions", vendorShippingOptions);
            model.addAttribute("vendorPackagingOptions", vendorPackagingOptions);
            model.addAttribute("vendorShippingCost", vendorShippingCost);
            model.addAttribute("vendorPackagingCost", vendorPackagingCost);
            model.addAttribute("grandTotal", grandTotal);
        }
        return "cart/index";
    }

    private BigDecimal subtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return subtotal;
        }

        for (CartItem item : cartItems) {
            subtotal = subtotal.add(item.getItemTotal());
        }

        return subtotal;
    }

    @RequestMapping("/add")
    public String addItem(
            @RequestParam("product_id") String pid,
            @RequestParam(value = "v_id", required = false) String vid,
            @RequestParam(value = "quantity", defaultValue = "0") BigDecimal quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes, Model model) {

        // Check if district is in session
        boolean showDistrictModal = session.getAttribute("shippingdistrict") == null;
        model.addAttribute("showDistrictModal", showDistrictModal);
        model.addAttribute("showDistrictModal", showDistrictModal);
        if (showDistrictModal == true) {
            // Pass a flag to tell the page to show the popup
            model.addAttribute("showDistrictModal", true);
            model.addAttribute("product_id", pid);
            model.addAttribute("quantity", quantity);
            return "cart/index"; // render the same cart page
        }

        Long productId = Long.valueOf(pid);
        Long variantsId = Long.valueOf(vid);
        boolean success = cartService.addToCart(productId, variantsId, quantity, session);

        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Invalid product or quantity");
        }

        return "redirect:/cart/index";
    }

//    @PostMapping("/quantityUpdate")
    public String quantityUpdate(
            @RequestParam(value = "product_id", required = false) String pid,
            @RequestParam(value = "quantity", required = false, defaultValue = "0") BigDecimal quantity,
            HttpSession session) {

        if (pid == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/cart/index";
        }

        Product product = productRepository.findById(Long.valueOf(pid)).orElse(null);
        if (product == null) {
            return "redirect:/cart/index";
        }

        cartService.updateCartItemInSession(session, product, quantity);

        return "redirect:/cart/index";
    }

//    @RequestMapping(value = "remove/{id}", method = RequestMethod.GET)
    public String remove(@PathVariable("id") int id, HttpSession session) {

        List<CartItem> shoppingcart_list = (List<CartItem>) session.getAttribute("sessioncart");
        int index = this.exists(id, shoppingcart_list);
        shoppingcart_list.remove(index);
        session.setAttribute("sessioncart", shoppingcart_list);
        return "redirect:/cart/index";
    }

    private int exists(int id, List<CartItem> cart) {

        for (int i = 0; i < cart.size(); i++) {

            if (cart.get(i).getProduct().getId() == id) {

                return i;
            }
        }
        return -1;
    }

    @GetMapping("/count")
    @ResponseBody
    public String getCartCount(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");

        int count = (cart != null) ? cart.size() : 0;
        return "<span class=\"badge bg-danger ms-1 text-white cart-count\" id=\"cart-count\">" + count + "</span>";
    }

    @PostMapping("/updateshipping/{vendorId}")
    public String updateShippingVendorTotals(
            @PathVariable Long vendorId,
            @RequestParam(required = false) String shippingOption,
            Model model,
            HttpSession session) {

        // 1️⃣ Get full cart safely
        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null) {
            fullCart = new ArrayList<>();
        }

        // 2️⃣ Vendor-specific cart
        List<CartItem> vendorCart = cartService.getVendorCart(fullCart, vendorId);

        // 3️⃣ Subtotal
        BigDecimal subtotal = cartService.calculateSubtotal(vendorCart);

        // 4️⃣ Calculate vendor-specific total weight
        // 2️⃣ Calculate total weight (product weight × quantity)
        BigDecimal totalWeight = fullCart.stream()
                .map(c -> {
                    BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
                    BigDecimal qty = c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO;
                    return weight.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5️⃣ Compute new shipping cost
        BigDecimal shippingCost = BigDecimal.ZERO;
        if (shippingOption != null && !shippingOption.isEmpty()) {
            shippingCost = cartService.calculateShipping(shippingOption, vendorId, session);
            if (shippingCost == null) {
                shippingCost = BigDecimal.ZERO;
            }
            // ✅ Save option code
            session.setAttribute("shippingOption_" + vendorId, shippingOption);
        } else {
            // ✅ Clear previous selection
            session.removeAttribute("shippingOption_" + vendorId);
        }

        // 6️⃣ Keep existing packaging cost
        BigDecimal packagingCost = (BigDecimal) session.getAttribute("packagingCost_" + vendorId);
        if (packagingCost == null) {
            packagingCost = BigDecimal.ZERO;
        }

        // 7️⃣ Save new shipping cost in session
        session.setAttribute("shippingCost_" + vendorId, shippingCost);

        // 8️⃣ Calculate vendor total for summary
        BigDecimal total = subtotal.add(shippingCost).add(packagingCost);

        // 9️⃣ Prepare model for HTMX fragment update
        model.addAttribute("vendorKey", vendorId);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("packagingCost", packagingCost);
        model.addAttribute("total", total);

        // 10️⃣ Return only the summary fragment
        return "cart/vendorSummary :: vendorSummary";
    }

    @PostMapping("/updatePackaging/{vendorId}")
    public String updatePackagingVendorTotals(
            @PathVariable Long vendorId,
            @RequestParam(required = false) Long packaging,
            Model model,
            HttpSession session) {

        System.out.println("📦 Packaging selected for vendor " + vendorId + " → " + packaging);

        // 1️⃣ Retrieve cart from session
        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null) {
            fullCart = new ArrayList<>();
        }

        // 2️⃣ Calculate total weight (product weight × quantity)
        BigDecimal totalWeight = fullCart.stream()
                .map(c -> {
                    BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
                    BigDecimal qty = c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO;
                    return weight.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3️⃣ Filter items belonging to this vendor
        List<CartItem> vendorCart = cartService.getVendorCart(fullCart, vendorId);

        // 4️⃣ Calculate vendor subtotal
        BigDecimal subtotal = cartService.calculateSubtotal(vendorCart);

        // 5️⃣ Retrieve previously saved shipping cost (if any)
        BigDecimal shippingCost = (BigDecimal) session.getAttribute("shippingCost_" + vendorId);
        if (shippingCost == null) {
            shippingCost = BigDecimal.ZERO;
        }

        // 6️⃣ Calculate new packaging cost (handle null safely)
        BigDecimal packagingCost = BigDecimal.ZERO;
        if (packaging != null) {
            double baseWeight = 0.5; // configurable threshold
            BigDecimal calculated = packagingRateService.calculateRateOne(packaging, baseWeight, totalWeight.doubleValue());
            if (calculated != null) {
                packagingCost = calculated;
            }
        }

        // 7️⃣ Save new packaging cost in session (vendor-specific)
        session.setAttribute("packagingCost_" + vendorId, packagingCost);

        // 8️⃣ Update model for partial Thymeleaf fragment rendering
        model.addAttribute("vendorKey", vendorId);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("packagingCost", packagingCost);

        // 9️⃣ Return vendor summary fragment for HTMX update
        return "cart/vendorSummary :: vendorSummary";
    }

    @RequestMapping("/shipping")
    public String shipping(Model model
    ) {
        return "cart/shipping";
    }

    @RequestMapping("/payment")
    public String payment(Model model
    ) {
        return "cart/payment";

    }

}
