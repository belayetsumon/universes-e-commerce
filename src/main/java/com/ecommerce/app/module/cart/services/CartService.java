/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.cart.services;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.services.CarrierRateService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductVariants;
import com.ecommerce.app.product.model.Unitofmeasurement;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantsRepository;
import com.ecommerce.app.product.services.ProductService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class CartService {

    @Autowired
    private HttpSession session;

    @Autowired
    CarrierRateService carrierRateService;

    @Autowired
    ProductDimensionRepository productDimensionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;
    @Autowired
    ProductVariantsRepository productVariantsRepository;

    public boolean addToCart(Long productId, Long variantId, BigDecimal quantity, HttpSession session) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }

        ProductVariants variants = productVariantsRepository.findById(variantId)
                .orElseThrow(null);
        if (variants == null) {
            return false;
        }

        BigDecimal salesPrice = product.getSalesPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal subTotal = salesPrice.multiply(quantity);

        BigDecimal discountRate = productService.totalDiscountPercentCalculate(
                product.getVendordiscount(),
                product.getMarketPlaceDiscount()
        ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDiscountAmount = subTotal.multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal afterDiscountItemTotal = subTotal.subtract(totalDiscountAmount);

        BigDecimal commissionRate = product.getMarketPlaceCommissionRate() != null
                ? product.getMarketPlaceCommissionRate()
                : BigDecimal.ZERO.setScale(2);
        BigDecimal marketPlaceCommissionAmount = afterDiscountItemTotal.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal vendorAmount = afterDiscountItemTotal.subtract(marketPlaceCommissionAmount);

        BigDecimal vatRate = product.getVatRate() != null ? product.getVatRate() : BigDecimal.ZERO.setScale(2);
        BigDecimal totalVatAmount = afterDiscountItemTotal.multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal itemTotal = afterDiscountItemTotal.add(totalVatAmount);

        Unitofmeasurement uom = product.getUom();

        ProductDimension dim = productDimensionRepository.findByProduct_Id(product.getId());
        BigDecimal weight = (dim != null && dim.getWeight() != null) ? dim.getWeight() : BigDecimal.ZERO.setScale(2);
        BigDecimal grandweight = weight.multiply(quantity).setScale(2);

        // Get or create session cart
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("sessioncart", cart);
        }

        // Check if product exists in cart
        int index = exists(productId.intValue(), cart);
        if (index == -1) {
            // Add new item
            cart.add(new CartItem(
                    product,
                    product.getVendorprofile().getId(),
                    product.getId(),
                    variants.getId(),
                    quantity.setScale(2, RoundingMode.HALF_UP),
                    uom,
                    salesPrice.setScale(2, RoundingMode.HALF_UP),
                    discountRate.setScale(2, RoundingMode.HALF_UP),
                    totalDiscountAmount.setScale(2, RoundingMode.HALF_UP),
                    commissionRate.setScale(2, RoundingMode.HALF_UP),
                    marketPlaceCommissionAmount.setScale(2, RoundingMode.HALF_UP),
                    vendorAmount.setScale(2, RoundingMode.HALF_UP),
                    vatRate.setScale(2, RoundingMode.HALF_UP),
                    grandweight.setScale(2, RoundingMode.HALF_UP),
                    totalVatAmount.setScale(2, RoundingMode.HALF_UP),
                    itemTotal.setScale(2, RoundingMode.HALF_UP)
            ));

        } else {
            // Update existing item
            CartItem existingItem = cart.get(index);
            BigDecimal newQuantity = existingItem.getQuantity().add(quantity).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newWeight = newQuantity.multiply(weight);
            BigDecimal newSubTotal = salesPrice.multiply(newQuantity);
            BigDecimal newTotalDiscount = newSubTotal.multiply(discountRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newAfterDiscountTotal = newSubTotal.subtract(newTotalDiscount);
            BigDecimal newTotalCommission = newAfterDiscountTotal.multiply(commissionRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newVendorAmount = newAfterDiscountTotal.subtract(newTotalCommission);
            BigDecimal newVat = newAfterDiscountTotal.multiply(vatRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newItemTotal = newAfterDiscountTotal.add(newVat);

            existingItem.setQuantity(newQuantity);
            existingItem.setDiscountAmount(newTotalDiscount);
            existingItem.setMarketPlaceCommissionAmount(newTotalCommission);
            existingItem.setVendorAmount(newVendorAmount);
            existingItem.setVatAmount(newVat);
            existingItem.setWeight(newWeight.setScale(2));
            existingItem.setItemTotal(newItemTotal);
        }

        session.setAttribute("sessioncart", cart);
        return true;
    }

    public CartItem calculateCartItem(Product product, BigDecimal quantity) {
        if (product == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal salesPrice = product.getSalesPrice().setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountRate = productService.totalDiscountPercentCalculate(
                product.getVendordiscount(),
                product.getMarketPlaceDiscount()
        ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subTotal = salesPrice.multiply(quantity);
        BigDecimal totalDiscount = subTotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountTotal = subTotal.subtract(totalDiscount);

        BigDecimal commissionRate = product.getMarketPlaceCommissionRate() != null
                ? product.getMarketPlaceCommissionRate()
                : BigDecimal.ZERO;
        BigDecimal totalCommission = afterDiscountTotal.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal vendorAmount = afterDiscountTotal.subtract(totalCommission);

        BigDecimal vatRate = product.getVatRate() != null
                ? product.getVatRate()
                : BigDecimal.ZERO;
        BigDecimal totalVat = afterDiscountTotal.multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal itemTotal = afterDiscountTotal.add(totalVat);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setSalesPrice(salesPrice);
        cartItem.setDiscountRate(discountRate);
        cartItem.setDiscountAmount(totalDiscount);
        cartItem.setMarketPlaceCommissionRate(commissionRate);
        cartItem.setMarketPlaceCommissionAmount(totalCommission);
        cartItem.setVendorAmount(vendorAmount);
        cartItem.setVatRate(vatRate);
        cartItem.setVatAmount(totalVat);
        cartItem.setItemTotal(itemTotal);

        return cartItem;
    }

    public void updateCartItemInSession(HttpSession session, Product product, BigDecimal quantity) {
        List<CartItem> shoppingCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (shoppingCart == null) {
            shoppingCart = new ArrayList<>();
        }

        int index = exists(product.getId().intValue(), shoppingCart);
        CartItem updatedItem = calculateCartItem(product, quantity);

        if (index != -1) {
            shoppingCart.set(index, updatedItem);
        } else {
            shoppingCart.add(updatedItem);
        }

        session.setAttribute("sessioncart", shoppingCart);
    }

    // Helper to check if product exists in cart
    private int exists(int productId, List<CartItem> cart) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProductId() == productId) {
                return i;
            }
        }
        return -1;
    }

    public BigDecimal subtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        // Retrieve cart from session
        Object sessionCart = session.getAttribute("sessioncart");

        if (sessionCart instanceof List<?>) {
            List<CartItem> cartItems = (List<CartItem>) sessionCart;

            for (CartItem item : cartItems) {
                subtotal = subtotal.add(item.getItemTotal());
            }
        }

        return subtotal;
    }

    public BigDecimal subtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return subtotal;
        }

        for (CartItem item : cartItems) {
            subtotal = subtotal.add(item.getItemTotal());
        }

        return subtotal;
    }

    public BigDecimal total(List< CartItem> cartitem) {

//        cartitem = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (!cartitem.isEmpty()) {

            for (int i = 0; i < cartitem.size(); i++) {

                total = cartitem.get(i).getItemTotal().multiply(cartitem.get(i).getQuantity());
            }
            return total;
        }
        return total;
    }

    public BigDecimal discountTotal(List<CartItem> cartItems) {
        BigDecimal discountTotal = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return discountTotal;
        }

        for (CartItem item : cartItems) {
            discountTotal = discountTotal.add(item.getDiscountAmount());
        }

        return discountTotal;
    }

    public BigDecimal totalVat(List<CartItem> cartItems) {
        BigDecimal totalVat = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return totalVat;
        }

        for (CartItem item : cartItems) {
            totalVat = totalVat.add(item.getVatAmount());
        }

        return totalVat;
    }

    // 1️⃣ Get cart from session
    public List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        return cart != null ? cart : new ArrayList<>();
    }

// 2️⃣ Filter cart items for a specific vendor
    public List<CartItem> getVendorCart(List<CartItem> cart, Long vendorId) {
        return cart.stream()
                .filter(c -> c.getProduct().getVendorprofile().getId().equals(vendorId))
                .collect(Collectors.toList());
    }

// 3️⃣ Calculate subtotal
    public BigDecimal calculateSubtotal(List<CartItem> vendorCart) {
        return vendorCart.stream()
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

// 4️⃣ Calculate shipping cost
    public BigDecimal calculateShipping(String shippingOption, HttpSession session) {
        if (shippingOption == null || shippingOption.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<String, BigDecimal> shippingPrices = (Map<String, BigDecimal>) session.getAttribute("shippingPrices");
        if (shippingPrices != null && shippingPrices.containsKey(shippingOption)) {
            return shippingPrices.get(shippingOption);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateShipping(String shippingOption, Long vendorId, HttpSession session) {

        // ✅ 1. Get or create the map to store shipping costs by vendor
        Map<Long, BigDecimal> shippingMap = (Map<Long, BigDecimal>) session.getAttribute("shippingCosts");
        if (shippingMap == null) {
            shippingMap = new HashMap<>();
        }

        // ✅ 2. Retrieve full cart from session
        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null || fullCart.isEmpty()) {
            shippingMap.put(vendorId, BigDecimal.ZERO);
            session.setAttribute("shippingCosts", shippingMap);
            return BigDecimal.ZERO;
        }

        // ✅ 3. Filter cart items that belong to this vendor
        List<CartItem> vendorCart = fullCart.stream()
                .filter(item -> item.getProduct().getVendorprofile().getId().equals(vendorId))
                .toList();

        if (vendorCart.isEmpty()) {
            shippingMap.put(vendorId, BigDecimal.ZERO);
            session.setAttribute("shippingCosts", shippingMap);
            return BigDecimal.ZERO;
        }

        // ✅ 4. Calculate total weight
        // (Assumes Product has weight field, adjust if different)
        BigDecimal totalWeight = vendorCart.stream()
                .map(item -> item.getWeight()
                .multiply(BigDecimal.valueOf(item.getQuantity().intValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ 5. Call service to calculate shipping using the weight
        BigDecimal cost = carrierRateService.calculateShippingRateByUuid(
                shippingOption,
                totalWeight,
                true // or false if COD not needed
        );

        // ✅ 6. Store in session
        shippingMap.put(vendorId, cost);
        session.setAttribute("shippingCosts", shippingMap);

        return cost;
    }
    // 5️⃣ Calculate packaging cost

    public BigDecimal calculatePackagings(Long packagingId, List<CartItem> vendorCart, HttpSession session) {
        if (packagingId == null) {
            return BigDecimal.ZERO;
        }

        Map<Long, PackagingRate> packagingMap = (Map<Long, PackagingRate>) session.getAttribute("packagingOptions");
        if (packagingMap != null && packagingMap.containsKey(packagingId)) {
            PackagingRate pack = packagingMap.get(packagingId);
            BigDecimal totalWeight = vendorCart.stream()
                    .map(CartItem::getWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return pack.getBasePrice().add(pack.getAdditionalPrice().multiply(totalWeight));
        }
        return BigDecimal.ZERO;
    }

}
