/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.controller;

import com.ecommerce.app.module.ReferralRewards.dto.CheckoutIncentiveQuote;
import com.ecommerce.app.module.ReferralRewards.model.OrderIncentiveUsage;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.OrderIncentiveUsageRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.services.CashbackService;
import com.ecommerce.app.module.ReferralRewards.services.CheckoutIncentiveService;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.ReferralRewards.services.WalletTransactionService;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.BillingAddress;
import com.ecommerce.app.order.model.EmiPaymentPlan;
import com.ecommerce.app.order.model.OrderHistory;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderPaymentPlan;
import com.ecommerce.app.order.model.OrderPaymentState;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.OrderStatusChangedBy;
import com.ecommerce.app.order.model.PaymentMethod;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.model.ShippingAddress;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.EmiPaymentPlanService;
import com.ecommerce.app.order.services.PaymentService;
import com.ecommerce.app.order.services.SalesOrderCodeGeneratorService;
import com.ecommerce.app.order.services.SalesOrderService;
import com.ecommerce.app.product.services.StockLedgerService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/order")
//@PreAuthorize("hasAuthority('order')")
public class SalesOrderController {

    private static final String PAYMENT_METHOD_COD = "COD";
    private static final String PAYMENT_METHOD_WALLET = "WALLET";
    private static final String PAYMENT_METHOD_EMI = "EMI";
    private static final String PAYMENT_METHOD_SSLCOMMERZ = "SSLCOMMERZ";
    private static final String PAYMENT_METHOD_BKASH = "BKASH";
    private static final String PAYMENT_PLAN_FULL_COD = "FULL_COD";
    private static final String PAYMENT_PLAN_FULL_PREPAID = "FULL_PREPAID";
    private static final String PAYMENT_PLAN_PARTIAL_ADVANCE_COD = "PARTIAL_ADVANCE_COD";
    private static final String PAYMENT_PLAN_EMI = "EMI";
    private static final BigDecimal DEFAULT_ADVANCE_RATIO = new BigDecimal("0.20");

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    CartService cartService;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    BillingAddressRepository billingAddressRepository;

    @Autowired
    ShippingAddressRepository shippingAddressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderHistoryRepository orderHistoryRepository;

    @Autowired
    ReferralService referralService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    RewardAccountRepository rewardAccountRepository;

    @Autowired
    WalletTransactionService walletTransactionService;

    @Autowired
    CashbackService cashbackService;

    @Autowired
    CheckoutIncentiveService checkoutIncentiveService;

    @Autowired
    OrderIncentiveUsageRepository orderIncentiveUsageRepository;

    @Autowired
    SalesOrderCodeGeneratorService salesOrderCodeGeneratorService;

    @Autowired
    SalesOrderService salesOrderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    StockLedgerService stockLedgerService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("orderlist", salesOrderRepository.findAll());
        return "order/order/index";
    }

    @RequestMapping(value = {"create"})
    public String create(Model model, HttpSession session) {

        model.addAttribute("subtotal", cartService.subtotal());
        model.addAttribute("walletBalance", loadCurrentUserWalletBalance());
        model.addAttribute("rewardBalance", loadCurrentUserRewardBalance());
        if (!model.containsAttribute("selectedPaymentPlan")) {
            model.addAttribute("selectedPaymentPlan", PAYMENT_PLAN_FULL_COD);
        }
        if (!model.containsAttribute("selectedPaymentMethod")) {
            model.addAttribute("selectedPaymentMethod", PAYMENT_METHOD_SSLCOMMERZ);
        }
        List<CartItem> cartItems = cartService.getCartFromSession(session);
        BigDecimal orderTotal = calculateCartTotal(cartItems, session, false);
        boolean requiresShipping = cartService.cartRequiresShipping(cartItems);
        model.addAttribute("requiresShipping", requiresShipping);
        model.addAttribute("emiEligible", cartService.cartSupportsEmi(cartItems));
        if (!model.containsAttribute("selectedAdvanceAmount")) {
            model.addAttribute("selectedAdvanceAmount", suggestAdvanceAmount(orderTotal));
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

        // Billing Address Handling
        BillingAddress billingAddress = new BillingAddress();

        if (session.getAttribute("session_Billing_address") == null) {
            Optional<BillingAddress> optionalBillingAddress
                    = billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(loggedUserService.activeUserid());

            if (optionalBillingAddress.isPresent()) {
                BillingAddress existing = optionalBillingAddress.get();
                billingAddress.setId(existing.getId());
                billingAddress.setFirstName(existing.getFirstName());
                billingAddress.setLastName(existing.getLastName());
                billingAddress.setEmail(existing.getEmail());
                billingAddress.setMobile(existing.getMobile());
                billingAddress.setCompany(existing.getCompany());
                billingAddress.setCountry(existing.getCountry());
                billingAddress.setDistrict(existing.getDistrict());
                billingAddress.setAddressLineOne(existing.getAddressLineOne());
                billingAddress.setAddressLinetwo(existing.getAddressLinetwo());
                billingAddress.setCity(existing.getCity());
                billingAddress.setPostCode(existing.getPostCode());

            } else {
                Users user = usersRepository.findById(loggedUserService.activeUserid()).orElseThrow(
                        () -> new RuntimeException("User not found"));

                billingAddress.setFirstName(user.getFirstName());
                billingAddress.setLastName(user.getLastName());
                billingAddress.setEmail(user.getEmail());
                billingAddress.setMobile(user.getMobile());
            }
        } else {
            BillingAddress sessionBillingAddress
                    = (BillingAddress) session.getAttribute("session_Billing_address");

            billingAddress.setFirstName(sessionBillingAddress.getFirstName());
            billingAddress.setLastName(sessionBillingAddress.getLastName());
            billingAddress.setEmail(sessionBillingAddress.getEmail());
            billingAddress.setMobile(sessionBillingAddress.getMobile());
            billingAddress.setCompany(sessionBillingAddress.getCompany());
            billingAddress.setCountry(sessionBillingAddress.getCountry());
            billingAddress.setDistrict(sessionBillingAddress.getDistrict());
            billingAddress.setAddressLineOne(sessionBillingAddress.getAddressLineOne());
            billingAddress.setAddressLinetwo(sessionBillingAddress.getAddressLinetwo());
            billingAddress.setCity(sessionBillingAddress.getCity());
            billingAddress.setPostCode(sessionBillingAddress.getPostCode());
        }

        model.addAttribute("billingAddress", billingAddress);

        // Shipping Address Handling
        ShippingAddress shippingAddress = new ShippingAddress();

        if (requiresShipping && session.getAttribute("session_Shipping_address") != null) {
            ShippingAddress sessionShippingAddress
                    = (ShippingAddress) session.getAttribute("session_Shipping_address");

            shippingAddress.setFirstName(sessionShippingAddress.getFirstName());
            shippingAddress.setLastName(sessionShippingAddress.getLastName());
            shippingAddress.setEmail(sessionShippingAddress.getEmail());
            shippingAddress.setMobile(sessionShippingAddress.getMobile());
            shippingAddress.setCompany(sessionShippingAddress.getCompany());
            shippingAddress.setCountry(sessionShippingAddress.getCountry());
            shippingAddress.setDistrict(sessionShippingAddress.getDistrict());
            shippingAddress.setAddressLineOne(sessionShippingAddress.getAddressLineOne());
            shippingAddress.setAddressLinetwo(sessionShippingAddress.getAddressLinetwo());
            shippingAddress.setCity(sessionShippingAddress.getCity());
            shippingAddress.setPostCode(sessionShippingAddress.getPostCode());
        }

        model.addAttribute("shippingAddress", shippingAddress);

        return "order/order/create";
    }

    @RequestMapping(value = {"/save"})
    @Transactional
    public String save(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        List<CartItem> cartItems = cartService.getCartFromSession(session);
        boolean requiresShipping = cartService.cartRequiresShipping(cartItems);

        if (session.getAttribute("session_Billing_address") == null
                || (requiresShipping && session.getAttribute("session_Shipping_address") == null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Billing or shipping  address is missing or empty!");

            return "redirect:/order/create";
        }

        BigDecimal grandtotal = BigDecimal.ZERO;

        List<CartItem> cartitem = new ArrayList<>();

        SalesOrder order = new SalesOrder();

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        order.setCustomer(userId);

        order.setStatus(OrderStatus.PENDING);

        salesOrderRepository.save(order);

        Set<OrderItem> orderitems = new HashSet<>();

        for (int i = 0; i < cartitem.size(); i++) {

            OrderItem orderitem = new OrderItem();
            orderitem.setSalesOrder(order);
            orderitem.setProduct(cartitem.get(i).getProduct());
            orderitem.setVendorId(cartitem.get(i).getVendorId());
            orderitem.setProductid(cartitem.get(i).getProductId());
            orderitem.setCatalogVariantUuid(cartitem.get(i).getCatalogVariantUuid());
            orderitem.setVariantSummary(cartitem.get(i).getVariantSummary());
            orderitem.setPreorder(cartitem.get(i).getPreorder());
            orderitem.setPreorderAvailableFrom(cartitem.get(i).getPreorderAvailableFrom());
            orderitem.setDigitalAccessUrl(cartitem.get(i).getProduct().getDigitalAccessUrl());
            orderitem.setDigitalLicenseCode(cartitem.get(i).getProduct().getDigitalLicenseCode());
            orderitem.setDigitalDeliveryNote(cartitem.get(i).getProduct().getDigitalDeliveryNote());
            orderitem.setQuantity(cartitem.get(i).getQuantity());
            orderitem.setDiscountRate(cartitem.get(i).getDiscountRate());
            orderitem.setDiscountAmount(cartitem.get(i).getDiscountAmount());
            orderitem.setItemTotal(cartitem.get(i).getItemTotal());
            orderitems.add(orderitem);
        }
        orderItemRepository.saveAll(orderitems);

        BillingAddress billingAddress = (BillingAddress) session.getAttribute("session_Billing_address");

        billingAddress.setUserId(userId);

//        billingAddress.setSalesOrder(order);
        billingAddressRepository.save(billingAddress);

        if (requiresShipping) {
            ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
            shippingAddress.setOrder(order);
            shippingAddressRepository.save(shippingAddress);
        }

        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        return "redirect:/customerorder/index";
    }

    @RequestMapping(value = {"/savebyvendor"})
    @Transactional
    public String savebyvendor(
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "paymentPlan", required = false) String paymentPlan,
            @RequestParam(name = "paymentMethod", defaultValue = PAYMENT_METHOD_SSLCOMMERZ) String paymentMethod,
            @RequestParam(name = "advanceAmount", required = false) BigDecimal advanceAmount,
            @RequestParam(name = "emiTenureMonths", required = false) Integer emiTenureMonths,
            @RequestParam(name = "couponCode", required = false) String couponCode,
            @RequestParam(name = "rewardPointsToUse", required = false) BigDecimal rewardPointsToUse,
            @RequestParam(name = "giftCardCode", required = false) String giftCardCode,
            @RequestParam(name = "giftCardAmount", required = false) BigDecimal giftCardAmount) {

        String normalizedPaymentPlan = normalizePaymentPlan(paymentPlan, paymentMethod);
        String normalizedPaymentMethod = resolveCheckoutPaymentMethod(normalizedPaymentPlan, paymentMethod);
        BigDecimal normalizedAdvanceAmount = normalizeCheckoutAdvanceAmount(advanceAmount);

        redirectAttributes.addFlashAttribute("selectedPaymentPlan", normalizedPaymentPlan);
        redirectAttributes.addFlashAttribute("selectedPaymentMethod", normalizedPaymentMethod);
        redirectAttributes.addFlashAttribute("selectedAdvanceAmount", normalizedAdvanceAmount);
        redirectAttributes.addFlashAttribute("selectedEmiTenureMonths", emiPaymentPlanService.normalizeTenureMonths(emiTenureMonths));
        preserveIncentiveInputs(redirectAttributes, couponCode, rewardPointsToUse, giftCardCode, giftCardAmount);

        List<CartItem> fullCart = cartService.getCartFromSession(session);
        boolean requiresShippingForCart = cartService.cartRequiresShipping(fullCart);

        if (session.getAttribute("session_Billing_address") == null
                || (requiresShippingForCart && session.getAttribute("session_Shipping_address") == null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Billing or Shipping address is missing!");
            return "redirect:/order/create";
        }

        // Load cart from session
        List<CartItem> cartitem = new ArrayList<>();
        if (session.getAttribute("sessioncart") != null) {
            cartitem = (List<CartItem>) session.getAttribute("sessioncart");
        }

        if (cartitem == null || cartitem.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cart is empty!");
            return "redirect:/order/create";
        }

        Users customer = usersRepository.findById(loggedUserService.activeUserid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal grossPayableTotal = calculateCartTotal(cartitem, session, false);
        CheckoutIncentiveQuote incentiveQuote;
        try {
            incentiveQuote = checkoutIncentiveService.prepareQuote(
                    customer,
                    cartitem,
                    grossPayableTotal,
                    couponCode,
                    rewardPointsToUse,
                    giftCardCode,
                    giftCardAmount
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/order/create";
        }

        BigDecimal payableTotal = incentiveQuote.getNetPayable();
        String paymentValidationRedirect = validateCheckoutSelection(
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                cartitem,
                payableTotal,
                normalizedAdvanceAmount,
                redirectAttributes,
                "/order/create"
        );
        if (paymentValidationRedirect != null) {
            return paymentValidationRedirect;
        }

        BigDecimal immediatePaymentTotal = determineImmediatePaymentTotal(
                normalizedPaymentPlan,
                payableTotal,
                normalizedAdvanceAmount
        );
        String walletCheckoutRedirect = validateWalletCheckout(
                normalizedPaymentMethod, customer, immediatePaymentTotal, redirectAttributes, "/order/create");
        if (walletCheckoutRedirect != null) {
            return walletCheckoutRedirect;
        }

//        String stockAvailabilityMessage = validateCartStockAvailability(cartitem);
//        if (stockAvailabilityMessage != null) {
//            redirectAttributes.addFlashAttribute("errorMessage", stockAvailabilityMessage);
//            return "redirect:/cart/index";
//        }
        // Group cart items by vendor (after loading cart)
        Map<Long, List<CartItem>> itemsByVendors = cartitem.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        // Prepare customer
        List<SalesOrder> orders = new ArrayList<>();

        // Loop per vendor
        for (Map.Entry<Long, List<CartItem>> entry : itemsByVendors.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setCustomer(customer);
            salesOrder.setVendorId(vendorId);

            salesOrder.setStatus(OrderStatus.NEW_ORDER);

            // Save shipping address with order
            if (cartService.vendorCartRequiresShipping(items)) {
                ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
                ShippingAddress newShipping = new ShippingAddress();
                newShipping.copyFrom(shippingAddress); // implement a method to deep copy or map fields
                newShipping.setOrder(salesOrder);
                salesOrder.setShippingAddress(newShipping); // make sure @OneToOne(cascade = CascadeType.ALL)
            } else {
                salesOrder.setDeliveryCharge(BigDecimal.ZERO);
                salesOrder.setPackingCharge(BigDecimal.ZERO);
            }

            salesOrder.setOrderCode(salesOrderCodeGeneratorService.generateNextDailyOrderCode());
            salesOrder = salesOrderRepository.save(salesOrder); // cascade saves shipping

            BigDecimal grandTotal = BigDecimal.ZERO;

            BigDecimal totalmarketPlaceCommissionAmount = BigDecimal.ZERO;

            for (CartItem cartItem : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSalesOrder(salesOrder);
                orderItem.setProductid(cartItem.getProductId());
                orderItem.setCatalogVariantUuid(cartItem.getCatalogVariantUuid());
                orderItem.setVariantSummary(cartItem.getVariantSummary());
                orderItem.setVendorId(cartItem.getVendorId());
                orderItem.setPreorder(cartItem.getPreorder());
                orderItem.setPreorderAvailableFrom(cartItem.getPreorderAvailableFrom());
                orderItem.setDigitalAccessUrl(cartItem.getProduct().getDigitalAccessUrl());
                orderItem.setDigitalLicenseCode(cartItem.getProduct().getDigitalLicenseCode());
                orderItem.setDigitalDeliveryNote(cartItem.getProduct().getDigitalDeliveryNote());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setSalesPrice(cartItem.getSalesPrice());
                orderItem.setDiscountRate(cartItem.getDiscountRate());
                orderItem.setDiscountAmount(cartItem.getDiscountAmount());
                orderItem.setMarketPlaceCommissionRate(cartItem.getMarketPlaceCommissionRate());
                orderItem.setMarketPlaceCommissionAmount(cartItem.getMarketPlaceCommissionAmount());
                orderItem.setVatRate(cartItem.getVatRate());
                orderItem.setVatAmount(cartItem.getVatAmount());
                orderItem.setVendorAmount(cartItem.getVendorAmount());
                orderItem.setItemTotal(cartItem.getItemTotal());
                orderItemRepository.save(orderItem);

                // Sum item total to grand total
                if (cartItem.getItemTotal() != null) {
                    grandTotal = grandTotal.add(cartItem.getItemTotal());
                }

                // SumgetMarketPlaceCommissionAmount to totalmarketPlaceCommissionAmount
                if (cartItem.getItemTotal() != null) {
                    totalmarketPlaceCommissionAmount = totalmarketPlaceCommissionAmount.add(cartItem.getMarketPlaceCommissionAmount());
                }

            }
            // Save grand total in sales order
            salesOrder.setGrandTotal(grandTotal);
            salesOrder.setTotalMarketPlaceCommissionAmount(totalmarketPlaceCommissionAmount);
            salesOrderRepository.save(salesOrder); // update with grand total
            salesOrderService.reserveStockForOrder(salesOrder.getId());
            // 10% for refarel
            BigDecimal commission = totalmarketPlaceCommissionAmount.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);
            referralService.distributeCommission(customer, commission, salesOrder.getId());

            orders.add(salesOrder);
        }

        // Save billing address only if it's new (null ID)
        BillingAddress billingAddress = (BillingAddress) session.getAttribute("session_Billing_address");

        if (billingAddress.getId() == null) {
            billingAddress.setUserId(customer);
            billingAddressRepository.save(billingAddress);
        }

        checkoutIncentiveService.applyQuoteToOrders(incentiveQuote, customer, orders);
        orders.forEach(salesOrderRepository::save);

        Map<Long, BigDecimal> paymentDueNowByOrder = applyPaymentPlanToOrders(
                orders,
                normalizedPaymentPlan,
                immediatePaymentTotal
        );
        persistOrderIncentiveUsage(orders, incentiveQuote, paymentDueNowByOrder, normalizedPaymentMethod);

        // Clean session
        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        return finalizePlacedOrders(
                orders,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                paymentDueNowByOrder,
                immediatePaymentTotal,
                customer,
                emiTenureMonths,
                redirectAttributes
        );
    }

    @RequestMapping(value = {"/savebyvendorupdate"})
    @Transactional
    public String savebyvendorupdate(
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "paymentPlan", required = false) String paymentPlan,
            @RequestParam(name = "paymentMethod", defaultValue = PAYMENT_METHOD_SSLCOMMERZ) String paymentMethod,
            @RequestParam(name = "advanceAmount", required = false) BigDecimal advanceAmount,
            @RequestParam(name = "emiTenureMonths", required = false) Integer emiTenureMonths,
            @RequestParam(name = "couponCode", required = false) String couponCode,
            @RequestParam(name = "rewardPointsToUse", required = false) BigDecimal rewardPointsToUse,
            @RequestParam(name = "giftCardCode", required = false) String giftCardCode,
            @RequestParam(name = "giftCardAmount", required = false) BigDecimal giftCardAmount) {

        String normalizedPaymentPlan = normalizePaymentPlan(paymentPlan, paymentMethod);
        String normalizedPaymentMethod = resolveCheckoutPaymentMethod(normalizedPaymentPlan, paymentMethod);
        BigDecimal normalizedAdvanceAmount = normalizeCheckoutAdvanceAmount(advanceAmount);

        redirectAttributes.addFlashAttribute("selectedPaymentPlan", normalizedPaymentPlan);
        redirectAttributes.addFlashAttribute("selectedPaymentMethod", normalizedPaymentMethod);
        redirectAttributes.addFlashAttribute("selectedAdvanceAmount", normalizedAdvanceAmount);
        redirectAttributes.addFlashAttribute("selectedEmiTenureMonths", emiPaymentPlanService.normalizeTenureMonths(emiTenureMonths));
        preserveIncentiveInputs(redirectAttributes, couponCode, rewardPointsToUse, giftCardCode, giftCardAmount);

        List<CartItem> cartitem = (List<CartItem>) session.getAttribute("sessioncart");
        if (cartitem == null || cartitem.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cart is empty!");
            return "redirect:/cart/index";
        }

        boolean requiresShippingForCart = cartService.cartRequiresShipping(cartitem);
        BillingAddress sessionBillingAddress = (BillingAddress) session.getAttribute("session_Billing_address");
        ShippingAddress sessionShippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
        if (sessionBillingAddress == null || (requiresShippingForCart && sessionShippingAddress == null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Billing or Shipping address is missing!");
            return "redirect:/cart/checkout";
        }

        Users customer = usersRepository.findById(loggedUserService.activeUserid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal grossPayableTotal = calculateCartTotal(cartitem, session, true);
        CheckoutIncentiveQuote incentiveQuote;
        try {
            incentiveQuote = checkoutIncentiveService.prepareQuote(
                    customer,
                    cartitem,
                    grossPayableTotal,
                    couponCode,
                    rewardPointsToUse,
                    giftCardCode,
                    giftCardAmount
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart/checkout";
        }

        BigDecimal payableTotal = incentiveQuote.getNetPayable();
        String paymentValidationRedirect = validateCheckoutSelection(
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                cartitem,
                payableTotal,
                normalizedAdvanceAmount,
                redirectAttributes,
                "/cart/checkout"
        );
        if (paymentValidationRedirect != null) {
            return paymentValidationRedirect;
        }

        BigDecimal immediatePaymentTotal = determineImmediatePaymentTotal(
                normalizedPaymentPlan,
                payableTotal,
                normalizedAdvanceAmount
        );
        String walletCheckoutRedirect = validateWalletCheckout(
                normalizedPaymentMethod, customer, immediatePaymentTotal, redirectAttributes, "/cart/checkout");
        if (walletCheckoutRedirect != null) {
            return walletCheckoutRedirect;
        }

//        String stockAvailabilityMessage = validateCartStockAvailability(cartitem);
//        if (stockAvailabilityMessage != null) {
//            redirectAttributes.addFlashAttribute("errorMessage", stockAvailabilityMessage);
//            return "redirect:/cart/index";
//        }
        // 2. Grouping
        Map<Long, List<CartItem>> itemsByVendors = cartitem.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        List<SalesOrder> orders = new ArrayList<>();

        // 3. Loop per vendor
        for (Map.Entry<Long, List<CartItem>> entry : itemsByVendors.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setCustomer(customer);
            salesOrder.setVendorId(vendorId);
            salesOrder.setStatus(OrderStatus.NEW_ORDER);
            salesOrder.setOrderCode(salesOrderCodeGeneratorService.generateNextDailyOrderCode());

            // --- FETCH SESSION VALUES (Matches Checkout) ---
            boolean vendorRequiresShipping = cartService.vendorCartRequiresShipping(items);
            BigDecimal sCost = vendorRequiresShipping
                    ? (BigDecimal) session.getAttribute("shippingCost_" + vendorId)
                    : BigDecimal.ZERO;
            BigDecimal pCost = vendorRequiresShipping
                    ? (BigDecimal) session.getAttribute("packagingCost_" + vendorId)
                    : BigDecimal.ZERO;

            // Ensure they aren't null
            sCost = (sCost != null) ? sCost : BigDecimal.ZERO;
            pCost = (pCost != null) ? pCost : BigDecimal.ZERO;

            salesOrder.setDeliveryCharge(sCost); // Ensure these fields exist in SalesOrder entity
            salesOrder.setPackingCharge(pCost);

            // 4. Handle Shipping Address
            if (vendorRequiresShipping) {
                ShippingAddress orderShipping = copyShippingAddressForOrder(sessionShippingAddress, salesOrder);
                salesOrder.setShippingAddress(orderShipping);
            }
            // Initial Save to get ID
            salesOrder = salesOrderRepository.save(salesOrder);

            BigDecimal subTotal = BigDecimal.ZERO;
            BigDecimal totalCommission = BigDecimal.ZERO;

            // 5. Save Order Items
            for (CartItem cartItem : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSalesOrder(salesOrder);
                orderItem.setProductid(cartItem.getProductId());
                orderItem.setCatalogVariantUuid(cartItem.getCatalogVariantUuid());
                orderItem.setVariantSummary(cartItem.getVariantSummary());
                orderItem.setVendorId(cartItem.getVendorId());
                orderItem.setPreorder(cartItem.getPreorder());
                orderItem.setPreorderAvailableFrom(cartItem.getPreorderAvailableFrom());
                orderItem.setDigitalAccessUrl(cartItem.getProduct().getDigitalAccessUrl());
                orderItem.setDigitalLicenseCode(cartItem.getProduct().getDigitalLicenseCode());
                orderItem.setDigitalDeliveryNote(cartItem.getProduct().getDigitalDeliveryNote());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setSalesPrice(cartItem.getSalesPrice());
                orderItem.setDiscountAmount(cartItem.getDiscountAmount());
                orderItem.setMarketPlaceCommissionAmount(cartItem.getMarketPlaceCommissionAmount());
                orderItem.setVatAmount(cartItem.getVatAmount());
                orderItem.setItemTotal(cartItem.getItemTotal());

                orderItemRepository.save(orderItem);

                if (cartItem.getItemTotal() != null) {
                    subTotal = subTotal.add(cartItem.getItemTotal());
                }
                if (cartItem.getMarketPlaceCommissionAmount() != null) {
                    totalCommission = totalCommission.add(cartItem.getMarketPlaceCommissionAmount());
                }
            }

            // 6. Final Totals (Subtotal + Shipping + Packaging)
            BigDecimal grandTotal = subTotal.add(sCost).add(pCost);

            salesOrder.setGrandTotal(grandTotal);
            salesOrder.setTotalMarketPlaceCommissionAmount(totalCommission);
            salesOrderRepository.save(salesOrder);
            salesOrderService.reserveStockForOrder(salesOrder.getId());

            // 7. Referral Commission
            BigDecimal referralCommission = totalCommission.multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);
            referralService.distributeCommission(customer, referralCommission, salesOrder.getId());

            orders.add(salesOrder);
        }

        checkoutIncentiveService.applyQuoteToOrders(incentiveQuote, customer, orders);
        orders.forEach(salesOrderRepository::save);

        Map<Long, BigDecimal> paymentDueNowByOrder = applyPaymentPlanToOrders(
                orders,
                normalizedPaymentPlan,
                immediatePaymentTotal
        );
        persistOrderIncentiveUsage(orders, incentiveQuote, paymentDueNowByOrder, normalizedPaymentMethod);

        // 8. Finalize Billing and Cleanup
        persistCheckoutBillingAddress(sessionBillingAddress, customer);
        // Clean up all related session attributes
        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        // Clear dynamic session keys for costs
        itemsByVendors.keySet().forEach(vId -> {
            session.removeAttribute("shippingCost_" + vId);
            session.removeAttribute("packagingCost_" + vId);
            session.removeAttribute("shippingOption_" + vId);
            session.removeAttribute("packagingRate_" + vId);
        });

        return finalizePlacedOrders(
                orders,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                paymentDueNowByOrder,
                immediatePaymentTotal,
                customer,
                emiTenureMonths,
                redirectAttributes
        );
    }

    private BigDecimal loadCurrentUserWalletBalance() {
        return usersRepository.findById(loggedUserService.activeUserid())
                .flatMap(walletRepository::findByUsers)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal loadCurrentUserRewardBalance() {
        return usersRepository.findById(loggedUserService.activeUserid())
                .flatMap(rewardAccountRepository::findByUsers)
                .map(account -> account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    private void preserveIncentiveInputs(RedirectAttributes redirectAttributes, String couponCode,
            BigDecimal rewardPointsToUse, String giftCardCode, BigDecimal giftCardAmount) {
        redirectAttributes.addFlashAttribute("selectedCouponCode", trimToEmpty(couponCode));
        redirectAttributes.addFlashAttribute("selectedRewardPointsToUse", safeMoney(rewardPointsToUse));
        redirectAttributes.addFlashAttribute("selectedGiftCardCode", trimToEmpty(giftCardCode));
        redirectAttributes.addFlashAttribute("selectedGiftCardAmount", safeMoney(giftCardAmount));
    }

    private BigDecimal calculateCartTotal(List<CartItem> cartItems, HttpSession session, boolean includeShippingAndPackaging) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<Long, List<CartItem>> itemsByVendors = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, List<CartItem>> entry : itemsByVendors.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            BigDecimal vendorSubtotal = items.stream()
                    .map(item -> item.getItemTotal() != null ? item.getItemTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            total = total.add(vendorSubtotal);

            if (includeShippingAndPackaging && cartService.vendorCartRequiresShipping(items)) {
                total = total
                        .add(getSessionMoney(session, "shippingCost_" + vendorId))
                        .add(getSessionMoney(session, "packagingCost_" + vendorId));
            }
        }

        return total;
    }

    private BigDecimal getSessionMoney(HttpSession session, String attributeName) {
        Object value = session.getAttribute(attributeName);
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
    }

    private ShippingAddress copyShippingAddressForOrder(ShippingAddress sessionShippingAddress, SalesOrder salesOrder) {
        ShippingAddress orderShipping = new ShippingAddress();
        if (sessionShippingAddress != null) {
            orderShipping.copyFrom(sessionShippingAddress);
        }
        orderShipping.setOrder(salesOrder);
        return orderShipping;
    }

    private void persistCheckoutBillingAddress(BillingAddress billingAddress, Users customer) {
        if (billingAddress == null || customer == null) {
            return;
        }
        billingAddress.setUserId(customer);
        billingAddressRepository.save(billingAddress);
    }

    private void persistOrderIncentiveUsage(List<SalesOrder> orders, CheckoutIncentiveQuote quote,
            Map<Long, BigDecimal> paymentDueNowByOrder, String paymentMethod) {
        CheckoutIncentiveQuote effectiveQuote = quote != null ? quote : new CheckoutIncentiveQuote();

        for (SalesOrder order : orders) {
            OrderIncentiveUsage usage = orderIncentiveUsageRepository.findByOrderId(String.valueOf(order.getId()))
                    .orElseGet(OrderIncentiveUsage::new);

            usage.setOrderId(String.valueOf(order.getId()));
            usage.setCouponId(effectiveQuote.getCoupon() != null ? effectiveQuote.getCoupon().getId() : null);
            usage.setCouponCode(effectiveQuote.getCoupon() != null ? trimToNull(effectiveQuote.getCouponCode()) : null);
            usage.setCouponDiscount(safeMoney(effectiveQuote.getCouponDiscountByOrder().get(order.getId())));
            usage.setWalletUsed(PAYMENT_METHOD_WALLET.equals(paymentMethod)
                    ? safeMoney(paymentDueNowByOrder.get(order.getId()))
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            usage.setRewardPointsUsed(safeMoney(effectiveQuote.getRewardPointsByOrder().get(order.getId())));
            usage.setRewardDiscount(safeMoney(effectiveQuote.getRewardDiscountByOrder().get(order.getId())));
            usage.setGiftCardCode(effectiveQuote.getGiftCardUsed().compareTo(BigDecimal.ZERO) > 0
                    ? trimToNull(effectiveQuote.getGiftCardCode())
                    : null);
            usage.setGiftCardUsed(safeMoney(effectiveQuote.getGiftCardUsedByOrder().get(order.getId())));
            usage.setReferralBonusExpected(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            usage.setQuoteReference(buildIncentiveQuoteReference(effectiveQuote, paymentMethod));
            usage.setIncentiveStatus("APPLIED");

            BigDecimal cashbackExpected;
            try {
                cashbackExpected = safeMoney(cashbackService.computeExpectedCashback(order, safeMoney(order.getGrandTotal())));
            } catch (Exception ignored) {
                cashbackExpected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            usage.setCashbackExpected(cashbackExpected);

            orderIncentiveUsageRepository.save(usage);
        }
    }

    private String buildIncentiveQuoteReference(CheckoutIncentiveQuote quote, String paymentMethod) {
        if (quote == null) {
            return "NO_INCENTIVE";
        }

        String couponPart = quote.getCoupon() != null ? String.valueOf(quote.getCoupon().getId()) : "NONE";
        String giftCardPart = quote.getGiftCard() != null ? String.valueOf(quote.getGiftCard().getId()) : "NONE";
        String rewardPart = safeMoney(quote.getRewardPointsUsed()).toPlainString();
        String walletPart = PAYMENT_METHOD_WALLET.equals(paymentMethod) ? "WALLET" : "NONE";
        return "COUPON:" + couponPart
                + "|REWARD:" + rewardPart
                + "|GIFTCARD:" + giftCardPart
                + "|PAYMENT:" + walletPart;
    }

    private String validateWalletCheckout(String paymentMethod, Users customer, BigDecimal payableTotal,
            RedirectAttributes redirectAttributes, String redirectPath) {

        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
        redirectAttributes.addFlashAttribute("selectedPaymentMethod", normalizedPaymentMethod);

        if (!PAYMENT_METHOD_WALLET.equals(normalizedPaymentMethod)
                || payableTotal == null
                || payableTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        Wallet wallet = walletRepository.findByUsers(customer).orElse(null);
        BigDecimal walletBalance = wallet != null && wallet.getBalance() != null
                ? wallet.getBalance()
                : BigDecimal.ZERO;

        if (wallet == null || !walletTransactionService.hasSufficientBalance(wallet, payableTotal)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Insufficient wallet balance. Available: " + formatMoney(walletBalance)
                    + " BDT, required: " + formatMoney(payableTotal) + " BDT."
            );
            return "redirect:" + redirectPath;
        }

        return null;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PAYMENT_METHOD_SSLCOMMERZ;
        }
        return paymentMethod.trim().toUpperCase();
    }

    private String normalizePaymentPlan(String paymentPlan, String paymentMethod) {
        if (paymentPlan == null || paymentPlan.isBlank()) {
            String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
            if (PAYMENT_METHOD_EMI.equals(normalizedPaymentMethod)) {
                return PAYMENT_PLAN_EMI;
            }
            if (PAYMENT_METHOD_COD.equals(normalizedPaymentMethod)) {
                return PAYMENT_PLAN_FULL_COD;
            }
            return PAYMENT_PLAN_FULL_PREPAID;
        }

        String normalizedPaymentPlan = paymentPlan.trim().toUpperCase();
        return switch (normalizedPaymentPlan) {
            case PAYMENT_PLAN_FULL_PREPAID, PAYMENT_PLAN_PARTIAL_ADVANCE_COD, PAYMENT_PLAN_EMI ->
                normalizedPaymentPlan;
            default ->
                PAYMENT_PLAN_FULL_COD;
        };
    }

    private String resolveCheckoutPaymentMethod(String paymentPlan, String paymentMethod) {
        if (PAYMENT_PLAN_FULL_COD.equals(paymentPlan)) {
            return PAYMENT_METHOD_COD;
        }
        if (PAYMENT_PLAN_EMI.equals(paymentPlan)) {
            return PAYMENT_METHOD_EMI;
        }

        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
        if (PAYMENT_METHOD_WALLET.equals(normalizedPaymentMethod) || isOnlineCheckoutMethod(normalizedPaymentMethod)) {
            return normalizedPaymentMethod;
        }

        return PAYMENT_METHOD_SSLCOMMERZ;
    }

    private BigDecimal normalizeCheckoutAdvanceAmount(BigDecimal advanceAmount) {
        return advanceAmount == null
                ? BigDecimal.ZERO
                : advanceAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal determineImmediatePaymentTotal(String paymentPlan, BigDecimal payableTotal, BigDecimal advanceAmount) {
        if (payableTotal == null || payableTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (PAYMENT_PLAN_FULL_PREPAID.equals(paymentPlan)) {
            return payableTotal.setScale(2, RoundingMode.HALF_UP);
        }

        if (PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)) {
            return normalizeCheckoutAdvanceAmount(advanceAmount);
        }

        return BigDecimal.ZERO;
    }

    private Map<Long, BigDecimal> applyPaymentPlanToOrders(List<SalesOrder> orders, String paymentPlan,
            BigDecimal immediatePaymentTotal) {
        Map<Long, BigDecimal> paymentDueNowByOrder = allocateAmountAcrossOrders(orders, immediatePaymentTotal);

        for (SalesOrder order : orders) {
            BigDecimal orderTotal = safeMoney(order.getGrandTotal());
            BigDecimal orderDueNow = safeMoney(paymentDueNowByOrder.get(order.getId()));

            if (PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)) {
                BigDecimal codDue = orderTotal.subtract(orderDueNow);
                if (codDue.compareTo(BigDecimal.ZERO) < 0) {
                    codDue = BigDecimal.ZERO;
                }
                order.setPaymentPlan(OrderPaymentPlan.PARTIAL_ADVANCE_COD);
                order.setAdvancePaid(BigDecimal.ZERO);
                order.setCodDue(codDue);
                order.setPaymentState(OrderPaymentState.ADVANCE_PENDING);
            } else if (PAYMENT_PLAN_FULL_PREPAID.equals(paymentPlan)) {
                order.setPaymentPlan(OrderPaymentPlan.FULL_PREPAID);
                order.setAdvancePaid(BigDecimal.ZERO);
                order.setCodDue(BigDecimal.ZERO);
                order.setPaymentState(OrderPaymentState.UNPAID);
            } else if (PAYMENT_PLAN_EMI.equals(paymentPlan)) {
                order.setPaymentPlan(OrderPaymentPlan.EMI);
                order.setAdvancePaid(BigDecimal.ZERO);
                order.setCodDue(BigDecimal.ZERO);
                order.setPaymentState(OrderPaymentState.EMI_PENDING);
            } else {
                order.setPaymentPlan(OrderPaymentPlan.FULL_COD);
                order.setAdvancePaid(BigDecimal.ZERO);
                order.setCodDue(orderTotal);
                order.setPaymentState(OrderPaymentState.COD_PENDING);
            }

            salesOrderRepository.save(order);
            paymentService.refreshOrderPaymentTracking(order);
        }

        return paymentDueNowByOrder;
    }

    private Map<Long, BigDecimal> allocateAmountAcrossOrders(List<SalesOrder> orders, BigDecimal totalToAllocate) {
        Map<Long, BigDecimal> allocations = new HashMap<>();
        if (orders == null || orders.isEmpty()) {
            return allocations;
        }

        BigDecimal normalizedTotalToAllocate = safeMoney(totalToAllocate);
        BigDecimal totalOrderAmount = orders.stream()
                .map(order -> safeMoney(order.getGrandTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (normalizedTotalToAllocate.compareTo(BigDecimal.ZERO) <= 0 || totalOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            orders.forEach(order -> allocations.put(order.getId(), BigDecimal.ZERO));
            return allocations;
        }

        if (normalizedTotalToAllocate.compareTo(totalOrderAmount) >= 0) {
            orders.forEach(order -> allocations.put(order.getId(), safeMoney(order.getGrandTotal())));
            return allocations;
        }

        BigDecimal allocatedSoFar = BigDecimal.ZERO;
        for (int index = 0; index < orders.size(); index++) {
            SalesOrder order = orders.get(index);
            BigDecimal orderTotal = safeMoney(order.getGrandTotal());
            BigDecimal orderShare;

            if (index == orders.size() - 1) {
                orderShare = normalizedTotalToAllocate.subtract(allocatedSoFar);
            } else {
                orderShare = normalizedTotalToAllocate
                        .multiply(orderTotal)
                        .divide(totalOrderAmount, 2, RoundingMode.HALF_UP);
            }

            BigDecimal remainingPool = normalizedTotalToAllocate.subtract(allocatedSoFar);
            if (orderShare.compareTo(remainingPool) > 0) {
                orderShare = remainingPool;
            }
            if (orderShare.compareTo(orderTotal) > 0) {
                orderShare = orderTotal;
            }
            if (orderShare.compareTo(BigDecimal.ZERO) < 0) {
                orderShare = BigDecimal.ZERO;
            }

            allocations.put(order.getId(), orderShare);
            allocatedSoFar = allocatedSoFar.add(orderShare);
        }

        return allocations;
    }

    private String finalizePlacedOrders(List<SalesOrder> orders, String paymentPlan, String paymentMethod,
            Map<Long, BigDecimal> paymentDueNowByOrder, BigDecimal immediatePaymentTotal, Users customer,
            Integer emiTenureMonths, RedirectAttributes redirectAttributes) {
        String normalizedPaymentMethod = resolveCheckoutPaymentMethod(paymentPlan, paymentMethod);

        boolean incentivesCoveredAllPayable = safeMoney(immediatePaymentTotal).compareTo(BigDecimal.ZERO) <= 0
                && orders.stream()
                        .map(paymentService::getPaymentSummary)
                        .allMatch(PaymentService.PaymentSummary::isFullyPaid);
        if (incentivesCoveredAllPayable) {
            for (SalesOrder order : orders) {
                SalesOrder paidOrder = salesOrderService.finalizePaidOrder(order.getId());
                try {
                    BigDecimal expected = cashbackService.computeExpectedCashback(paidOrder, safeMoney(paidOrder.getGrandTotal()));
                    cashbackService.createPendingCashbackIfMissing(customer, paidOrder, expected);
                } catch (Exception ignored) {
                }
                createPaymentHistory(
                        paidOrder,
                        paidOrder.getStatus() == OrderStatus.COMPLETED
                        ? "No customer payment was required after applying checkout incentives. Virtual order was completed automatically."
                        : "No customer payment was required after applying checkout incentives. Order moved to confirmed status."
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Order placed successfully. Applied incentives covered the payable amount."
            );
            return "redirect:/customerorder/index";
        }

        if (PAYMENT_METHOD_WALLET.equals(normalizedPaymentMethod)) {
            if (!captureWalletPayment(customer, immediatePaymentTotal)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Orders were placed, but the wallet payment could not be completed. Please review the payment status from your order list."
                );
                return "redirect:/customerorder/index";
            }

            for (SalesOrder order : orders) {
                BigDecimal orderPaymentAmount = safeMoney(paymentDueNowByOrder.get(order.getId()));
                if (orderPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                    paymentService.recordPayment(
                            order,
                            PaymentMethod.WALLET,
                            orderPaymentAmount,
                            null,
                            PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)
                            ? "Wallet advance payment captured during checkout."
                            : "Wallet payment captured during checkout."
                    );
                }

                PaymentService.PaymentSummary summary = paymentService.getPaymentSummary(order);
                if (summary.isFullyPaid()) {
                    SalesOrder paidOrder = salesOrderService.finalizePaidOrder(order.getId());
                    try {
                        BigDecimal expected = cashbackService.computeExpectedCashback(paidOrder, safeMoney(paidOrder.getGrandTotal()));
                        cashbackService.createPendingCashbackIfMissing(customer, paidOrder, expected);
                    } catch (Exception ignored) {
                    }
                    createPaymentHistory(
                            paidOrder,
                            paidOrder.getStatus() == OrderStatus.COMPLETED
                            ? "Wallet payment completed. Virtual order was completed automatically."
                            : "Wallet payment completed. Order moved to confirmed status."
                    );
                } else if (summary.getRemainingAdvanceDue().compareTo(BigDecimal.ZERO) <= 0
                        && summary.getRemainingCodDue().compareTo(BigDecimal.ZERO) > 0) {
                    SalesOrder confirmedOrder = salesOrderService.confirmOrderAfterAdvancePayment(order.getId());
                    try {
                        BigDecimal expected = cashbackService.computeExpectedCashback(confirmedOrder, safeMoney(confirmedOrder.getGrandTotal()));
                        cashbackService.createPendingCashbackIfMissing(customer, confirmedOrder, expected);
                    } catch (Exception ignored) {
                    }
                    createPaymentHistory(
                            confirmedOrder,
                            "Wallet advance payment completed. Remaining COD due: "
                            + formatMoney(summary.getRemainingCodDue())
                            + " BDT. Order moved to confirmed status for fulfillment."
                    );
                } else {
                    createPaymentHistory(
                            order,
                            "Wallet advance payment received. Remaining advance due: "
                            + formatMoney(summary.getRemainingAdvanceDue())
                            + " BDT. Remaining COD due: " + formatMoney(summary.getRemainingCodDue()) + " BDT."
                    );
                }
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)
                    ? "Order placed and wallet advance payment completed. The remaining balance will be collected as COD."
                    : "Order placed and wallet payment completed successfully."
            );
            return "redirect:/customerorder/index";
        }

        if (PAYMENT_PLAN_EMI.equals(paymentPlan)) {
            int normalizedTenure = emiPaymentPlanService.normalizeTenureMonths(emiTenureMonths);
            EmiPaymentPlan firstPlan = null;

            for (SalesOrder order : orders) {
                EmiPaymentPlan plan = emiPaymentPlanService.createPlanForOrder(order, normalizedTenure);
                if (firstPlan == null) {
                    firstPlan = plan;
                }

                if (order.getStatus() != OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.PENDING);
                    salesOrderRepository.save(order);
                }

                createPaymentHistory(
                        order,
                        "Meritten EMI request submitted for provider approval with "
                        + normalizedTenure
                        + " month tenure. Merchant settlement is pending provider approval."
                );
                try {
                    BigDecimal expected = cashbackService.computeExpectedCashback(order, safeMoney(order.getGrandTotal()));
                    cashbackService.createPendingCashbackIfMissing(customer, order, expected);
                } catch (Exception ignored) {
                }
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Meritten EMI request submitted successfully. The provider will approve or reject the request and, if approved, settle the full order amount to the merchant."
            );

            if (orders.size() == 1 && firstPlan != null) {
                return "redirect:/customer-meritten-emi/details/" + firstPlan.getId();
            }

            return "redirect:/customer-meritten-emi/index";
        }

        if (isOnlineCheckoutMethod(normalizedPaymentMethod)) {
            for (SalesOrder order : orders) {
                try {
                    BigDecimal expected = cashbackService.computeExpectedCashback(order, safeMoney(order.getGrandTotal()));
                    cashbackService.createPendingCashbackIfMissing(customer, order, expected);
                } catch (Exception ignored) {
                }
            }
            if (orders.size() == 1) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)
                        ? "Order placed successfully. Complete the advance payment now. The remaining balance is marked COD."
                        : "Order placed successfully. Complete the full payment now."
                );
                return "redirect:/customerorder/payment/" + orders.get(0).getId() + "?method=" + normalizedPaymentMethod;
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)
                    ? "Orders placed successfully. Use Pay Now on each order to pay the planned advance amount. The remaining balance is marked COD."
                    : "Orders placed successfully. Use Pay Now on each order to complete payment via SSLCommerz or bKash."
            );
            return "redirect:/customerorder/index";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                PAYMENT_PLAN_FULL_COD.equals(paymentPlan)
                ? "Order placed successfully. The full balance will be collected as Cash on Delivery."
                : "Order placed successfully."
        );
        for (SalesOrder order : orders) {
            try {
                BigDecimal expected = cashbackService.computeExpectedCashback(order, safeMoney(order.getGrandTotal()));
                cashbackService.createPendingCashbackIfMissing(customer, order, expected);
            } catch (Exception ignored) {
            }
        }
        return "redirect:/customerorder/index";
    }

    private boolean captureWalletPayment(Users customer, BigDecimal amount) {
        if (customer == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        Wallet wallet = walletRepository.findByUsers(customer).orElse(null);
        if (wallet == null || !walletTransactionService.hasSufficientBalance(wallet, amount)) {
            return false;
        }

        return walletTransactionService.deductFromWallet(
                wallet,
                amount,
                "Checkout payment",
                com.ecommerce.app.module.ReferralRewards.model.TransactionType.PURCHASE,
                "ORDER_CHECKOUT",
                null
        );
    }

    private boolean isOnlineCheckoutMethod(String paymentMethod) {
        return PAYMENT_METHOD_SSLCOMMERZ.equals(paymentMethod) || PAYMENT_METHOD_BKASH.equals(paymentMethod);
    }

    private String validateCheckoutSelection(String paymentPlan, String paymentMethod, List<CartItem> cartItems,
            BigDecimal payableTotal, BigDecimal advanceAmount, RedirectAttributes redirectAttributes,
            String redirectPath) {
        String emiCheckoutRedirect = validateEmiCheckout(paymentPlan, cartItems, redirectAttributes, redirectPath);
        if (emiCheckoutRedirect != null) {
            return emiCheckoutRedirect;
        }

        if (PAYMENT_PLAN_FULL_COD.equals(paymentPlan)) {
            return null;
        }

        if (!PAYMENT_METHOD_WALLET.equals(paymentMethod) && !isOnlineCheckoutMethod(paymentMethod)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please choose Wallet, SSLCommerz, or bKash for this checkout option."
            );
            return "redirect:" + redirectPath;
        }

        if (payableTotal == null || payableTotal.compareTo(BigDecimal.ZERO) <= 0) {
            if (PAYMENT_PLAN_FULL_PREPAID.equals(paymentPlan)) {
                return null;
            }

            redirectAttributes.addFlashAttribute("errorMessage", "Payable total must be greater than zero.");
            return "redirect:" + redirectPath;
        }

        if (PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)) {
            if (advanceAmount == null || advanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter an advance amount greater than zero.");
                return "redirect:" + redirectPath;
            }

            if (advanceAmount.compareTo(payableTotal) >= 0) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Advance amount must be smaller than the full order total so a COD balance remains."
                );
                return "redirect:" + redirectPath;
            }
        }

        return null;
    }

    private String validateEmiCheckout(String paymentPlan, List<CartItem> cartItems,
            RedirectAttributes redirectAttributes, String redirectPath) {
        if (!PAYMENT_PLAN_EMI.equals(paymentPlan)) {
            return null;
        }

        if (!cartService.cartSupportsEmi(cartItems)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Meritten EMI is available only for orders where every item is marked Meritten EMI eligible and is not a virtual product."
            );
            return "redirect:" + redirectPath;
        }

        return null;
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal suggestAdvanceAmount(BigDecimal orderTotal) {
        BigDecimal normalizedTotal = safeMoney(orderTotal);
        if (normalizedTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal suggested = normalizedTotal.multiply(DEFAULT_ADVANCE_RATIO).setScale(2, RoundingMode.HALF_UP);
        if (suggested.compareTo(BigDecimal.ZERO) <= 0) {
            suggested = new BigDecimal("1.00");
        }
        if (suggested.compareTo(normalizedTotal) >= 0) {
            suggested = normalizedTotal.subtract(new BigDecimal("1.00"));
        }

        return suggested.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : suggested;
    }

    private void createPaymentHistory(SalesOrder order, String remark) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setSalesOrder(order);
        orderHistory.setStatus(order.getStatus());
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.Customer);
        orderHistory.setRemark(remark);
        orderHistoryRepository.save(orderHistory);
    }

    private String formatMoney(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
