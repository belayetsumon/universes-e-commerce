/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.module.shipping.services.ShippingQuoteService;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.model.AvailableDeliveryArea;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.AvailableDeliveryAreaRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    AvailableDeliveryAreaRepository availableDeliveryAreaRepository;
    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    ShippingQuoteService shippingQuoteService;

    @Autowired
    PackagingRateService packagingRateService;

    @Autowired
    CartService cartService;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    RewardAccountRepository rewardAccountRepository;

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, HttpSession session) {

        List<CartItem> sessionCart = (List<CartItem>) session.getAttribute("sessioncart");
        int originalCartSize = sessionCart != null ? sessionCart.size() : 0;
        List<CartItem> cart = cartService.getCartFromSession(session);

        if (originalCartSize > cart.size()) {
            model.addAttribute("errorMessage", "Some cart items were removed because they are not assigned to a vendor yet.");
        }

        if (cart != null && !cart.isEmpty()) {

            Map<Long, List<CartItem>> grouped = new HashMap<>();
            for (CartItem item : cart) {
                if (item == null || item.getProduct() == null || item.getProduct().getVendorprofile() == null
                        || item.getProduct().getVendorprofile().getId() == null) {
                    continue;
                }
                grouped.computeIfAbsent(item.getProduct().getVendorprofile().getId(), ignored -> new ArrayList<>()).add(item);
            }

            Map<Long, BigDecimal> vendorSubtotals = new HashMap<>();
            Map<Long, BigDecimal> vendorWeights = new HashMap<>();
            Map<Long, List<ShippingOption>> vendorShippingOptions = new HashMap<>();
            Map<Long, List<PackagingRate>> vendorPackagingOptions = new HashMap<>();
            Map<Long, BigDecimal> vendorShippingCost = new HashMap<>();
            Map<Long, BigDecimal> vendorPackagingCost = new HashMap<>();

            for (Map.Entry<Long, List<CartItem>> entry : grouped.entrySet()) {
                Long vendorId = entry.getKey();
                List<CartItem> items = entry.getValue();
                boolean vendorRequiresShipping = cartService.vendorCartRequiresShipping(items);

                BigDecimal subtotal = items.stream()
                        .map(CartItem::getItemTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                vendorSubtotals.put(vendorId, subtotal);

                BigDecimal totalWeight = items.stream()
                        .map(c -> {
                            BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
                            BigDecimal qty = c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO;
                            return weight.multiply(qty);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                vendorWeights.put(vendorId, totalWeight);

                ShippingLocation customerLocation = currentShippingLocation(session);
                List<ShippingOption> shippingOptions = vendorRequiresShipping
                        ? shippingQuoteService.getShippingOptions(vendorId, customerLocation, totalWeight, subtotal)
                        : new ArrayList<>();
                vendorShippingOptions.put(vendorId, shippingOptions);

                List<PackagingRate> packagingRates = vendorRequiresShipping
                        ? packagingRateService.getByVendor(vendorId)
                        : new ArrayList<>();
                vendorPackagingOptions.put(vendorId, packagingRates);

                String vendorUuid = items.stream()
                        .map(CartItem::getVendorUuid)
                        .filter(uuid -> uuid != null && !uuid.isBlank())
                        .findFirst()
                        .orElse(null);

                BigDecimal shippingCost = vendorUuid == null
                        ? (BigDecimal) session.getAttribute("shippingCost_" + vendorId)
                        : (BigDecimal) session.getAttribute("shippingCost_" + vendorUuid);
                BigDecimal packagingCost = vendorUuid == null
                        ? (BigDecimal) session.getAttribute("packagingCost_" + vendorId)
                        : (BigDecimal) session.getAttribute("packagingCost_" + vendorUuid);

                if (!vendorRequiresShipping) {
                    if (vendorUuid != null) {
                        session.setAttribute("shippingCost_" + vendorUuid, BigDecimal.ZERO);
                        session.setAttribute("packagingCost_" + vendorUuid, BigDecimal.ZERO);
                    } else {
                        session.setAttribute("shippingCost_" + vendorId, BigDecimal.ZERO);
                        session.setAttribute("packagingCost_" + vendorId, BigDecimal.ZERO);
                    }
                    shippingCost = BigDecimal.ZERO;
                    packagingCost = BigDecimal.ZERO;
                }

                vendorShippingCost.put(vendorId, shippingCost != null ? shippingCost : BigDecimal.ZERO);
                vendorPackagingCost.put(vendorId, packagingCost != null ? packagingCost : BigDecimal.ZERO);
            }

            BigDecimal grandTotal = grouped.keySet().stream()
                    .map(vendorId
                            -> vendorSubtotals.getOrDefault(vendorId, BigDecimal.ZERO)
                            .add(vendorShippingCost.getOrDefault(vendorId, BigDecimal.ZERO))
                            .add(vendorPackagingCost.getOrDefault(vendorId, BigDecimal.ZERO))
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

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

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        final BigDecimal defaultAdvanceRatio = new BigDecimal("0.20");
        List<CartItem> cart = cartService.getCartFromSession(session);

        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart/index";
        }

        Map<Long, List<CartItem>> grouped = new HashMap<>();
        for (CartItem item : cart) {
            if (item == null || item.getProduct() == null || item.getProduct().getVendorprofile() == null
                    || item.getProduct().getVendorprofile().getId() == null) {
                continue;
            }
            grouped.computeIfAbsent(item.getProduct().getVendorprofile().getId(), ignored -> new ArrayList<>()).add(item);
        }

        Map<Long, BigDecimal> vendorSubtotals = new HashMap<>();
        Map<Long, BigDecimal> vendorShippingCost = new HashMap<>();
        Map<Long, BigDecimal> vendorPackagingCost = new HashMap<>();

        for (Long vendorId : grouped.keySet()) {
            List<CartItem> items = grouped.get(vendorId);
            boolean vendorRequiresShipping = cartService.vendorCartRequiresShipping(items);

            BigDecimal subtotal = items.stream()
                    .map(c -> c.getItemTotal() != null ? c.getItemTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vendorSubtotals.put(vendorId, subtotal);

            String vendorUuid = items.stream()
                    .map(CartItem::getVendorUuid)
                    .filter(uuid -> uuid != null && !uuid.isBlank())
                    .findFirst()
                    .orElse(null);

            BigDecimal sCost = vendorUuid == null
                    ? (BigDecimal) session.getAttribute("shippingCost_" + vendorId)
                    : (BigDecimal) session.getAttribute("shippingCost_" + vendorUuid);
            BigDecimal pCost = vendorUuid == null
                    ? (BigDecimal) session.getAttribute("packagingCost_" + vendorId)
                    : (BigDecimal) session.getAttribute("packagingCost_" + vendorUuid);

            if (!vendorRequiresShipping) {
                if (vendorUuid != null) {
                    session.setAttribute("shippingCost_" + vendorUuid, BigDecimal.ZERO);
                    session.setAttribute("packagingCost_" + vendorUuid, BigDecimal.ZERO);
                } else {
                    session.setAttribute("shippingCost_" + vendorId, BigDecimal.ZERO);
                    session.setAttribute("packagingCost_" + vendorId, BigDecimal.ZERO);
                }
                sCost = BigDecimal.ZERO;
                pCost = BigDecimal.ZERO;
            }

            vendorShippingCost.put(vendorId, sCost != null ? sCost : BigDecimal.ZERO);
            vendorPackagingCost.put(vendorId, pCost != null ? pCost : BigDecimal.ZERO);
        }

        BigDecimal grandTotal = grouped.keySet().stream()
                .map(vId -> vendorSubtotals.get(vId)
                .add(vendorShippingCost.get(vId))
                .add(vendorPackagingCost.get(vId)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("grouped", grouped);
        model.addAttribute("vendorSubtotals", vendorSubtotals);
        model.addAttribute("vendorShippingCost", vendorShippingCost);
        model.addAttribute("vendorPackagingCost", vendorPackagingCost);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("walletBalance", resolveWalletBalance());
        model.addAttribute("rewardBalance", resolveRewardBalance());
        model.addAttribute("emiEligible", cartService.cartSupportsEmi(cart));
        if (!model.containsAttribute("selectedPaymentPlan")) {
            model.addAttribute("selectedPaymentPlan", "FULL_COD");
        }
        if (!model.containsAttribute("selectedPaymentMethod")) {
            model.addAttribute("selectedPaymentMethod", "SSLCOMMERZ");
        }
        if (!model.containsAttribute("selectedAdvanceAmount")) {
            BigDecimal suggestedAdvance = grandTotal.multiply(defaultAdvanceRatio).setScale(2, RoundingMode.HALF_UP);
            if (suggestedAdvance.compareTo(BigDecimal.ZERO) <= 0 && grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                suggestedAdvance = new BigDecimal("1.00");
            }
            if (suggestedAdvance.compareTo(grandTotal) >= 0) {
                suggestedAdvance = grandTotal.subtract(new BigDecimal("1.00"));
            }
            model.addAttribute("selectedAdvanceAmount", suggestedAdvance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : suggestedAdvance);
        }
        if (!model.containsAttribute("selectedEmiTenureMonths")) {
            model.addAttribute("selectedEmiTenureMonths", 3);
        }
        if (!model.containsAttribute("selectedCouponCode")) {
            model.addAttribute("selectedCouponCode", "");
        }
        if (!model.containsAttribute("selectedRewardPointsToUse")) {
            model.addAttribute("selectedRewardPointsToUse", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (!model.containsAttribute("selectedGiftCardCode")) {
            model.addAttribute("selectedGiftCardCode", "");
        }
        if (!model.containsAttribute("selectedGiftCardAmount")) {
            model.addAttribute("selectedGiftCardAmount", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        return "cart/checkout";
    }

    private BigDecimal resolveWalletBalance() {
        try {
            Long userId = loggedUserService.activeUserid();
            if (userId == null) {
                return BigDecimal.ZERO;
            }

            return usersRepository.findById(userId)
                    .flatMap(walletRepository::findByUsers)
                    .map(Wallet::getBalance)
                    .orElse(BigDecimal.ZERO);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal resolveRewardBalance() {
        try {
            Long userId = loggedUserService.activeUserid();
            if (userId == null) {
                return BigDecimal.ZERO;
            }

            return usersRepository.findById(userId)
                    .flatMap(rewardAccountRepository::findByUsers)
                    .map(RewardAccount::getBalance)
                    .orElse(BigDecimal.ZERO);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
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

    @PostMapping("/add")
    public String addItem(
            @RequestParam(value = "product_uuid", required = false) String productUuid,
            @RequestParam(value = "product_id", required = false) Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            @RequestParam(value = "quantity", defaultValue = "1") BigDecimal quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Product product = resolveProduct(productUuid, productId);
        if (product == null || product.getUuid() == null || product.getUuid().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/public/product";
        }

        String resolvedProductUuid = product.getUuid();
        if (!cartService.hasValidVendor(product)) {
            redirectAttributes.addFlashAttribute("errorMessage", "This product is not assigned to a vendor yet, so it cannot be added to the cart.");
            return "redirect:/public/single-product/" + resolvedProductUuid;
        }
        boolean requiresShipping = cartService.requiresShipping(product);

        if (requiresShipping && currentShippingLocation(session) == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select delivery district before adding to cart.");
            return "redirect:/public/single-product/" + resolvedProductUuid;
        }

        ShippingLocation customerLocation = currentShippingLocation(session);
        List<AvailableDeliveryArea> deliveryAreas = availableDeliveryAreaRepository.findByProduct_UuidOrderByIdDesc(resolvedProductUuid);

        if (requiresShipping && deliveryAreas != null && !deliveryAreas.isEmpty()) {
            boolean locationMatched = deliveryAreas.stream()
                    .anyMatch(area -> area.matchesLocation(customerLocation));

            if (!locationMatched) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "This product is not available for delivery in your selected location: " + customerLocation.getDisplayLabel()
                );
                return "redirect:/public/single-product/" + resolvedProductUuid;
            }
        }

        if (Boolean.TRUE.equals(product.getManageProductVariants())
                && (catalogVariantUuid == null || catalogVariantUuid.isBlank())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a catalog variant before adding to cart.");
            return "redirect:/public/single-product/" + resolvedProductUuid;
        }

        try {
            boolean success = cartService.addToCart(resolvedProductUuid, catalogVariantUuid, quantity, session);

            if (!success) {
                redirectAttributes.addFlashAttribute("errorMessage", "The selected item or quantity is unavailable.");
                return "redirect:/public/single-product/" + resolvedProductUuid;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while adding to cart: " + e.getMessage());
            return "redirect:/public/single-product/" + resolvedProductUuid;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully!");
        return "redirect:/cart/index";
    }

    private ShippingLocation currentShippingLocation(HttpSession session) {
        Object value = session.getAttribute("shippingLocation");
        return value instanceof ShippingLocation location ? location : null;
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

        List<CartItem> shoppingcartList = (List<CartItem>) session.getAttribute("sessioncart");
        int index = this.exists(id, shoppingcartList);
        shoppingcartList.remove(index);
        session.setAttribute("sessioncart", shoppingcartList);
        return "redirect:/cart/index";
    }

    private int exists(int id, List<CartItem> cart) {

        for (int i = 0; i < cart.size(); i++) {

            if (Objects.equals(cart.get(i).getProduct().getId(), Long.valueOf(id))) {

                return i;
            }
        }
        return -1;
    }

    @GetMapping("/count")
    @ResponseBody
    public String getCartCount(HttpSession session) {
        List<CartItem> cart = cartService.getCartFromSession(session);

        int count = (cart != null) ? cart.size() : 0;
        return "<span class=\"badge bg-danger ms-1 text-white cart-count\" id=\"cart-count\">" + count + "</span>";
    }

    @PostMapping("/updateshipping/{vendorId}")
    public String updateShippingVendorTotals(
            @PathVariable String vendorId,
            @RequestParam(required = false) String shippingOption,
            Model model,
            HttpSession session) {

        String vendorUuid = resolveVendorUuid(vendorId);
        if (vendorUuid == null || vendorUuid.isBlank()) {
            return "cart/vendorSummary :: vendorSummary";
        }

        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null) {
            fullCart = new ArrayList<>();
        }

        List<CartItem> vendorCart = cartService.getVendorCart(fullCart, vendorUuid);
        BigDecimal subtotal = cartService.calculateSubtotal(vendorCart);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.removeAttribute("shippingOption_" + vendorUuid);
            session.setAttribute("shippingCost_" + vendorUuid, BigDecimal.ZERO);
            session.setAttribute("packagingCost_" + vendorUuid, BigDecimal.ZERO);
            model.addAttribute("vendorKey", vendorUuid);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingCost", BigDecimal.ZERO);
            model.addAttribute("packagingCost", BigDecimal.ZERO);
            model.addAttribute("total", subtotal);
            return "cart/vendorSummary :: vendorSummary";
        }

        BigDecimal shippingCost = BigDecimal.ZERO;
        if (shippingOption != null && !shippingOption.isEmpty()) {
            shippingCost = cartService.calculateShipping(shippingOption, vendorUuid, session);
            if (shippingCost == null) {
                shippingCost = BigDecimal.ZERO;
            }
            session.setAttribute("shippingOption_" + vendorUuid, shippingOption);
        } else {
            session.removeAttribute("shippingOption_" + vendorUuid);
        }

        BigDecimal packagingCost = (BigDecimal) session.getAttribute("packagingCost_" + vendorUuid);
        if (packagingCost == null) {
            packagingCost = BigDecimal.ZERO;
        }

        session.setAttribute("shippingCost_" + vendorUuid, shippingCost);

        BigDecimal total = subtotal.add(shippingCost).add(packagingCost);

        model.addAttribute("vendorKey", vendorUuid);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("packagingCost", packagingCost);
        model.addAttribute("total", total);

        return "cart/vendorSummary :: vendorSummary";
    }

    @PostMapping("/updatePackaging/{vendorId}")
    public String updatePackagingVendorTotals(
            @PathVariable String vendorId,
            @RequestParam(required = false) String packaging,
            Model model,
            HttpSession session) {

        String vendorUuid = resolveVendorUuid(vendorId);
        if (vendorUuid == null || vendorUuid.isBlank()) {
            return "cart/vendorSummary :: vendorSummary";
        }

        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null) {
            fullCart = new ArrayList<>();
        }

        List<CartItem> vendorCart = cartService.getVendorCart(fullCart, vendorUuid);

        BigDecimal totalWeight = vendorCart.stream()
                .map(c -> {
                    BigDecimal weight = c.getWeight() != null ? c.getWeight() : BigDecimal.ZERO;
                    BigDecimal qty = c.getQuantity() != null ? c.getQuantity() : BigDecimal.ZERO;
                    return weight.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = cartService.calculateSubtotal(vendorCart);
        if (!cartService.vendorCartRequiresShipping(vendorCart)) {
            session.removeAttribute("packagingRate_" + vendorUuid);
            session.setAttribute("packagingCost_" + vendorUuid, BigDecimal.ZERO);
            model.addAttribute("vendorKey", vendorUuid);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingCost", BigDecimal.ZERO);
            model.addAttribute("packagingCost", BigDecimal.ZERO);
            return "cart/vendorSummary :: vendorSummary";
        }

        BigDecimal shippingCost = (BigDecimal) session.getAttribute("shippingCost_" + vendorUuid);
        if (shippingCost == null) {
            shippingCost = BigDecimal.ZERO;
        }

        BigDecimal packagingCost = BigDecimal.ZERO;
        if (packaging != null && !packaging.isBlank()) {
            double baseWeight = 0.5;
            try {
                BigDecimal calculated = packagingRateService.calculateRateOneByUuid(packaging, baseWeight, totalWeight.doubleValue());
                if (calculated != null) {
                    packagingCost = calculated;
                }
            } catch (RuntimeException ex) {
                try {
                    BigDecimal calculated = packagingRateService.calculateRateOne(Long.valueOf(packaging), baseWeight, totalWeight.doubleValue());
                    if (calculated != null) {
                        packagingCost = calculated;
                    }
                } catch (Exception ignored) {
                    log.debug("Ignoring legacy packaging value {}", packaging);
                }
            }
        }

        session.setAttribute("packagingCost_" + vendorUuid, packagingCost);

        model.addAttribute("vendorKey", vendorUuid);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("packagingCost", packagingCost);

        return "cart/vendorSummary :: vendorSummary";
    }

    private Product resolveProduct(String productUuid, Long productId) {
        if (productUuid != null && !productUuid.isBlank()) {
            Product product = productRepository.findByUuid(productUuid.trim()).orElse(null);
            if (product != null) {
                return product;
            }
        }
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId).orElse(null);
    }

    private String resolveVendorUuid(String vendorReference) {
        if (vendorReference == null || vendorReference.isBlank()) {
            return null;
        }
        Vendorprofile vendor = vendorprofileRepository.findByUuid(vendorReference.trim()).orElse(null);
        if (vendor != null) {
            return vendor.getUuid();
        }
        try {
            return vendorprofileRepository.findById(Long.valueOf(vendorReference.trim()))
                    .map(Vendorprofile::getUuid)
                    .orElse(null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @RequestMapping("/shipping")
    public String shipping(Model model) {
        return "cart/shipping";
    }

    @RequestMapping("/payment")
    public String payment(Model model) {
        return "cart/payment";
    }
}
