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
import com.ecommerce.app.product.model.ProductTypeEnum;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.Unitofmeasurement;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductVariantCatalogService;
import com.ecommerce.app.product.services.StockLedgerService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    StockLedgerService stockLedgerService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;
    @Autowired
    ProductVariantRepository productVariantRepository;
    @Autowired
    ProductVariantCatalogService productVariantCatalogService;

    public boolean addToCart(Long productId, String catalogVariantUuid, BigDecimal quantity, HttpSession session) {
        if (productId == null) {
            return false;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getUuid() == null || product.getUuid().isBlank()) {
            return false;
        }
        return addToCart(product.getUuid(), catalogVariantUuid, quantity, session);
    }

    public boolean addToCart(String productUuid, String catalogVariantUuid, BigDecimal quantity, HttpSession session) {

        catalogVariantUuid = normalizeUuid(catalogVariantUuid);

        if (productUuid == null || productUuid.isBlank()
                || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Product product = productRepository.findByUuid(productUuid).orElse(null);
        if (product == null || product.getId() == null) {
            return false;
        }
        if (!hasValidVendor(product)) {
            return false;
        }

        ProductVariant catalogVariant = null;

        if (Boolean.TRUE.equals(product.getManageProductVariants())
                && (catalogVariantUuid == null || catalogVariantUuid.isBlank())) {
            return false;
        }

        if (catalogVariantUuid != null && !catalogVariantUuid.isBlank()) {
            catalogVariant = productVariantRepository.findByUuid(catalogVariantUuid).orElse(null);
            if (catalogVariant == null || catalogVariant.getProduct() == null
                    || !Objects.equals(catalogVariant.getProduct().getUuid(), productUuid)) {
                return false;
            }
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("sessioncart", cart);
        }

        int index = exists(productUuid, catalogVariantUuid, cart);
        BigDecimal existingQty = index >= 0 ? cart.get(index).getQuantity() : BigDecimal.ZERO;
        BigDecimal totalRequestedQty = existingQty.add(quantity);

        BigDecimal availableStock = stockLedgerService.getAvailableQuantity(
                product.getId(),
                catalogVariant != null ? catalogVariant.getUuid() : null
        );

        boolean preorderAllowed = Boolean.TRUE.equals(product.getAllowPreorder());
        boolean preorderItem = Boolean.TRUE.equals(product.getManageStock())
                && totalRequestedQty.compareTo(availableStock) > 0
                && preorderAllowed;

        if (Boolean.TRUE.equals(product.getManageStock())
                && totalRequestedQty.compareTo(availableStock) > 0
                && !preorderItem) {
            return false;
        }

        BigDecimal salesPrice = catalogVariant != null
                ? (catalogVariant.getSpecialPrice() != null ? catalogVariant.getSpecialPrice() : catalogVariant.getSellingPrice())
                : product.getSalesPrice();
        salesPrice = (salesPrice == null ? BigDecimal.ZERO : salesPrice).setScale(2, RoundingMode.HALF_UP);
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
        BigDecimal weight = catalogVariant != null && catalogVariant.getWeight() != null
                ? catalogVariant.getWeight()
                : (dim != null && dim.getWeight() != null ? dim.getWeight() : BigDecimal.ZERO.setScale(2));
        BigDecimal grandweight = weight.multiply(quantity).setScale(2);
        String catalogVariantSummary = catalogVariant != null
                ? productVariantCatalogService.buildVariantSummaryLabel(catalogVariant)
                : null;

        if (index == -1) {
            cart.add(new CartItem(
                    product,
                    product.getVendorprofile() != null ? product.getVendorprofile().getId() : null,
                    product.getVendorprofile() != null ? product.getVendorprofile().getUuid() : null,
                    product.getId(),
                    product.getUuid(),
                    catalogVariant != null ? catalogVariant.getUuid() : null,
                    catalogVariantSummary,
                    preorderItem,
                    preorderItem ? product.getPreorderAvailableFrom() : null,
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
            CartItem existingItem = cart.get(index);
            BigDecimal newQuantity = existingItem.getQuantity().add(quantity).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newWeight = newQuantity.multiply(weight).setScale(2, RoundingMode.HALF_UP);
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

            existingItem.setProduct(product);
            existingItem.setVendorId(product.getVendorprofile() != null ? product.getVendorprofile().getId() : null);
            existingItem.setVendorUuid(product.getVendorprofile() != null ? product.getVendorprofile().getUuid() : null);
            existingItem.setProductId(product.getId());
            existingItem.setProductUuid(product.getUuid());
            existingItem.setPreorder(preorderItem);
            existingItem.setPreorderAvailableFrom(preorderItem ? product.getPreorderAvailableFrom() : null);
            existingItem.setCatalogVariantUuid(catalogVariant != null ? catalogVariant.getUuid() : existingItem.getCatalogVariantUuid());
            existingItem.setVariantSummary(catalogVariantSummary != null ? catalogVariantSummary : existingItem.getVariantSummary());
            existingItem.setQuantity(newQuantity);
            existingItem.setDiscountAmount(newTotalDiscount);
            existingItem.setMarketPlaceCommissionAmount(newTotalCommission);
            existingItem.setVendorAmount(newVendorAmount);
            existingItem.setVatAmount(newVat);
            existingItem.setWeight(newWeight);
            existingItem.setItemTotal(newItemTotal.setScale(2, RoundingMode.HALF_UP));
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
        cartItem.setVendorId(product != null && product.getVendorprofile() != null ? product.getVendorprofile().getId() : null);
        cartItem.setVendorUuid(product != null && product.getVendorprofile() != null ? product.getVendorprofile().getUuid() : null);
        cartItem.setProductId(product != null ? product.getId() : null);
        cartItem.setProductUuid(product != null ? product.getUuid() : null);
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

        int index = exists(product.getUuid(), null, shoppingCart);
        CartItem updatedItem = calculateCartItem(product, quantity);

        if (index != -1) {
            shoppingCart.set(index, updatedItem);
        } else {
            shoppingCart.add(updatedItem);
        }

        session.setAttribute("sessioncart", shoppingCart);
    }

    public boolean updateQuantityInCart(Long productId, String catalogVariantUuid, BigDecimal quantity, HttpSession session) {
        if (productId == null) {
            return false;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getUuid() == null || product.getUuid().isBlank()) {
            return false;
        }
        return updateQuantityInCart(product.getUuid(), catalogVariantUuid, quantity, session);
    }

    public boolean updateQuantityInCart(String productUuid, String catalogVariantUuid, BigDecimal quantity, HttpSession session) {
        catalogVariantUuid = normalizeUuid(catalogVariantUuid);

        if (productUuid == null || productUuid.isBlank()
                || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null || cart.isEmpty()) {
            return false;
        }

        int index = exists(productUuid, catalogVariantUuid, cart);
        if (index < 0) {
            return false;
        }

        CartItem existingItem = cart.get(index);
        Product product = productRepository.findByUuid(productUuid).orElse(existingItem.getProduct());
        if (product == null || product.getId() == null) {
            return false;
        }

        BigDecimal availableStock = stockLedgerService.getAvailableQuantity(
                product.getId(),
                existingItem.getCatalogVariantUuid()
        );
        boolean preorderAllowed = Boolean.TRUE.equals(product.getAllowPreorder());
        boolean preorderItem = Boolean.TRUE.equals(product.getManageStock())
                && quantity.compareTo(availableStock) > 0
                && preorderAllowed;

        if (Boolean.TRUE.equals(product.getManageStock())
                && quantity.compareTo(availableStock) > 0
                && !preorderItem) {
            return false;
        }

        BigDecimal newQuantity = quantity.setScale(2, RoundingMode.HALF_UP);
        BigDecimal salesPrice = (existingItem.getSalesPrice() != null
                ? existingItem.getSalesPrice()
                : product.getSalesPrice()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountRate = existingItem.getDiscountRate() != null
                ? existingItem.getDiscountRate().setScale(2, RoundingMode.HALF_UP)
                : productService.totalDiscountPercentCalculate(
                        product.getVendordiscount(),
                        product.getMarketPlaceDiscount()
                ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal commissionRate = existingItem.getMarketPlaceCommissionRate() != null
                ? existingItem.getMarketPlaceCommissionRate().setScale(2, RoundingMode.HALF_UP)
                : (product.getMarketPlaceCommissionRate() != null
                ? product.getMarketPlaceCommissionRate().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        BigDecimal vatRate = existingItem.getVatRate() != null
                ? existingItem.getVatRate().setScale(2, RoundingMode.HALF_UP)
                : (product.getVatRate() != null
                ? product.getVatRate().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        ProductDimension dim = productDimensionRepository.findByProduct_Id(product.getId());
        BigDecimal unitWeight = (dim != null && dim.getWeight() != null)
                ? dim.getWeight()
                : BigDecimal.ZERO;

        BigDecimal subTotal = salesPrice.multiply(newQuantity);
        BigDecimal totalDiscount = subTotal.multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountTotal = subTotal.subtract(totalDiscount);
        BigDecimal totalCommission = afterDiscountTotal.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal vendorAmount = afterDiscountTotal.subtract(totalCommission);
        BigDecimal totalVat = afterDiscountTotal.multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal itemTotal = afterDiscountTotal.add(totalVat).setScale(2, RoundingMode.HALF_UP);
        BigDecimal weight = unitWeight.multiply(newQuantity).setScale(2, RoundingMode.HALF_UP);

        existingItem.setProduct(product);
        existingItem.setVendorId(product.getVendorprofile() != null ? product.getVendorprofile().getId() : null);
        existingItem.setVendorUuid(product.getVendorprofile() != null ? product.getVendorprofile().getUuid() : null);
        existingItem.setProductId(product.getId());
        existingItem.setProductUuid(product.getUuid());
        existingItem.setPreorder(preorderItem);
        existingItem.setPreorderAvailableFrom(preorderItem ? product.getPreorderAvailableFrom() : null);
        existingItem.setQuantity(newQuantity);
        existingItem.setSalesPrice(salesPrice);
        existingItem.setDiscountRate(discountRate);
        existingItem.setDiscountAmount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        existingItem.setMarketPlaceCommissionRate(commissionRate);
        existingItem.setMarketPlaceCommissionAmount(totalCommission.setScale(2, RoundingMode.HALF_UP));
        existingItem.setVendorAmount(vendorAmount.setScale(2, RoundingMode.HALF_UP));
        existingItem.setVatRate(vatRate);
        existingItem.setVatAmount(totalVat.setScale(2, RoundingMode.HALF_UP));
        existingItem.setWeight(weight);
        existingItem.setItemTotal(itemTotal);

        session.setAttribute("sessioncart", cart);
        return true;
    }

    // Helper to check if product exists in cart
    private int exists(String productUuid, String catalogVariantUuid, List<CartItem> cart) {
        catalogVariantUuid = normalizeUuid(catalogVariantUuid);
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            if (Objects.equals(item.getProductUuid(), productUuid)
                    && Objects.equals(normalizeUuid(item.getCatalogVariantUuid()), catalogVariantUuid)) {
                return i;
            }
        }
        return -1;
    }

    private String normalizeUuid(String uuid) {
        return uuid == null || uuid.isBlank() ? null : uuid.trim();
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
        return sanitizeSessionCart(session);
    }

    public List<CartItem> sanitizeSessionCart(HttpSession session) {
        List<CartItem> sessionCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (sessionCart == null || sessionCart.isEmpty()) {
            session.removeAttribute("sessioncart");
            return new ArrayList<>();
        }

        List<CartItem> sanitized = new ArrayList<>();
        for (CartItem item : sessionCart) {
            if (item == null) {
                continue;
            }

            Product product = resolveCartProduct(item);
            if (product == null || product.getId() == null || product.getUuid() == null || product.getUuid().isBlank()) {
                continue;
            }

            if (!hasValidVendor(product)) {
                continue;
            }

            item.setProduct(product);
            item.setVendorId(product.getVendorprofile().getId());
            item.setVendorUuid(product.getVendorprofile().getUuid());
            item.setProductId(product.getId());
            item.setProductUuid(product.getUuid());
            sanitized.add(item);
        }

        if (sanitized.isEmpty()) {
            session.removeAttribute("sessioncart");
        } else {
            session.setAttribute("sessioncart", sanitized);
        }

        return sanitized;
    }

// 2️⃣ Filter cart items for a specific vendor
    public List<CartItem> getVendorCart(List<CartItem> cart, Long vendorId) {
        return cart.stream()
                .filter(c -> c.getProduct().getVendorprofile().getId().equals(vendorId))
                .collect(Collectors.toList());
    }

    public List<CartItem> getVendorCart(List<CartItem> cart, String vendorUuid) {
        if (cart == null || vendorUuid == null || vendorUuid.isBlank()) {
            return List.of();
        }
        return cart.stream()
                .filter(c -> Objects.equals(c.getVendorUuid(), vendorUuid))
                .collect(Collectors.toList());
    }

    public boolean requiresShipping(Product product) {
        return product != null && product.getProductType() != ProductTypeEnum.Virtual;
    }

    public boolean supportsEmi(Product product) {
        return product != null
                && Boolean.TRUE.equals(product.getEmiavailable())
                && product.getProductType() != ProductTypeEnum.Virtual;
    }

    public boolean vendorCartSupportsEmi(List<CartItem> vendorCart) {
        return vendorCart != null
                && !vendorCart.isEmpty()
                && vendorCart.stream()
                        .map(CartItem::getProduct)
                        .allMatch(this::supportsEmi);
    }

    public boolean cartSupportsEmi(List<CartItem> cart) {
        return cart != null
                && !cart.isEmpty()
                && cart.stream()
                        .map(CartItem::getProduct)
                        .allMatch(this::supportsEmi);
    }

    public boolean vendorCartRequiresShipping(List<CartItem> vendorCart) {
        return vendorCart != null && vendorCart.stream()
                .map(CartItem::getProduct)
                .anyMatch(this::requiresShipping);
    }

    public boolean cartRequiresShipping(List<CartItem> cart) {
        return cart != null && cart.stream()
                .map(CartItem::getProduct)
                .anyMatch(this::requiresShipping);
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

    public BigDecimal calculateShipping(String shippingOption, String vendorUuid, HttpSession session) {
        if (vendorUuid == null || vendorUuid.isBlank()) {
            return BigDecimal.ZERO;
        }

        Map<String, BigDecimal> shippingMap = (Map<String, BigDecimal>) session.getAttribute("shippingCosts");
        if (shippingMap == null) {
            shippingMap = new HashMap<>();
        }

        List<CartItem> fullCart = (List<CartItem>) session.getAttribute("sessioncart");
        if (fullCart == null || fullCart.isEmpty()) {
            shippingMap.put(vendorUuid, BigDecimal.ZERO);
            session.setAttribute("shippingCosts", shippingMap);
            return BigDecimal.ZERO;
        }

        List<CartItem> vendorCart = fullCart.stream()
                .filter(item -> Objects.equals(item.getVendorUuid(), vendorUuid))
                .toList();

        if (vendorCart.isEmpty()) {
            shippingMap.put(vendorUuid, BigDecimal.ZERO);
            session.setAttribute("shippingCosts", shippingMap);
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeight = vendorCart.stream()
                .map(item -> item.getWeight()
                .multiply(BigDecimal.valueOf(item.getQuantity().intValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cost = carrierRateService.calculateShippingRateByUuid(
                shippingOption,
                totalWeight,
                true
        );

        shippingMap.put(vendorUuid, cost);
        session.setAttribute("shippingCosts", shippingMap);

        return cost;
    }

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

    private Product resolveCartProduct(CartItem item) {
        if (item.getProduct() != null) {
            return item.getProduct();
        }

        String productUuid = item.getProductUuid();
        if (productUuid != null && !productUuid.isBlank()) {
            return productRepository.findByUuid(productUuid.trim()).orElse(null);
        }

        Long productId = item.getProductId();
        if (productId != null) {
            return productRepository.findById(productId).orElse(null);
        }

        return null;
    }

    public boolean hasValidVendor(Product product) {
        return product != null
                && product.getVendorprofile() != null
                && product.getVendorprofile().getId() != null
                && product.getVendorprofile().getUuid() != null
                && !product.getVendorprofile().getUuid().isBlank();
    }

}
