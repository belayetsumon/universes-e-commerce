/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.order.controller;

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
import com.ecommerce.app.module.checkout.availability.CheckoutAvailability;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailabilityService;
import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutOtpService;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutSessionService;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.services.CodEligibilityService;
import com.ecommerce.app.module.fraud.services.CodRiskProfileService;
import com.ecommerce.app.module.fraud.services.FraudOrderAssessmentGuard;
import com.ecommerce.app.module.fraud.services.FraudPaymentCaptureGuard;
import com.ecommerce.app.module.fraud.services.FraudPreOrderGuard;
import com.ecommerce.app.module.fraud.services.VendorRiskProfileService;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.CustomerOrderGroup;
import com.ecommerce.app.module.order.model.EmiPaymentPlan;
import com.ecommerce.app.module.order.model.OrderHistory;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.OrderPaymentState;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.OrderStatusChangedBy;
import com.ecommerce.app.module.order.model.PaymentMethod;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.order.repository.BillingAddressRepository;
import com.ecommerce.app.module.order.repository.CustomerOrderGroupRepository;
import com.ecommerce.app.module.order.repository.OrderHistoryRepository;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.order.repository.ShippingAddressRepository;
import com.ecommerce.app.module.order.services.EmiPaymentPlanService;
import com.ecommerce.app.module.order.services.CustomerOrderGroupCodeGeneratorService;
import com.ecommerce.app.module.order.services.PaymentService;
import com.ecommerce.app.module.order.services.SalesOrderCodeGeneratorService;
import com.ecommerce.app.module.order.services.SalesOrderService;
import com.ecommerce.app.product.services.StockLedgerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
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
    private static final String PRODUCT_SHARE_REFERRAL_CODE_SESSION_KEY = "productShareReferralCode";
    private static final String PRODUCT_SHARE_REFERRAL_PRODUCT_SESSION_KEY = "productShareReferralProductUuid";

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    CustomerOrderGroupRepository customerOrderGroupRepository;

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
    CustomerOrderGroupCodeGeneratorService customerOrderGroupCodeGeneratorService;

    @Autowired
    SalesOrderService salesOrderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    StockLedgerService stockLedgerService;

    @Autowired
    StoreOperationModeService storeOperationModeService;

    @Autowired
    CheckoutAvailabilityService checkoutAvailabilityService;

    @Autowired
    GuestCheckoutSessionService guestCheckoutSessionService;

    @Autowired
    GuestCheckoutOtpService guestCheckoutOtpService;

    @Autowired
    MobileNumberNormalizationService mobileNumberNormalizationService;

    @Autowired
    FraudPreOrderGuard fraudPreOrderGuard;

    @Autowired
    FraudOrderAssessmentGuard fraudOrderAssessmentGuard;

    @Autowired
    FraudPaymentCaptureGuard fraudPaymentCaptureGuard;

    @Autowired
    CodEligibilityService codEligibilityService;

    @Autowired
    CodRiskProfileService codRiskProfileService;

    @Autowired
    VendorRiskProfileService vendorRiskProfileService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("orderlist", salesOrderRepository.findAll());
        return "order/order/index";
    }

    @RequestMapping(value = {"create"})
    public String create(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        boolean authenticatedCustomer = loggedUserService.isAuthenticatedUser();
        CheckoutAvailability availability = checkoutAvailabilityService.availability(authenticatedCustomer);
        if (!availability.isCheckoutAvailable()) {
            redirectAttributes.addFlashAttribute("errorMessage", CheckoutAvailabilityService.CHECKOUT_UNAVAILABLE_MESSAGE);
            return "redirect:/cart/index";
        }
        if (!authenticatedCustomer && availability.isLoginRequired() && !availability.isGuestAllowed()) {
            return "redirect:/public/member-login";
        }
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (!authenticatedCustomer && guestSession == null) {
            return "redirect:/cart/checkout";
        }

        model.addAttribute("subtotal", cartService.subtotal());
        model.addAttribute("guestCheckout", !authenticatedCustomer);
        model.addAttribute("guestCheckoutSession", guestSession);
        model.addAttribute("guestVerifiedMobileDisplay", guestSession == null ? "" : mobileNumberNormalizationService.toLocalDisplay(guestSession.getVerifiedMobile()));
        String guestMobileVerificationStatus = guestSession == null || guestSession.getMobileVerificationStatus() == null
                ? ""
                : guestSession.getMobileVerificationStatus().name();
        model.addAttribute("guestMobileVerificationStatus", guestMobileVerificationStatus);
        model.addAttribute("guestMobileVerificationRequired", guestSession != null && guestSession.isMobileVerificationRequired());
        model.addAttribute("selectedShippingLocation", currentShippingLocation(session));
        model.addAttribute("walletBalance", authenticatedCustomer ? loadCurrentUserWalletBalance() : BigDecimal.ZERO);
        model.addAttribute("rewardBalance", authenticatedCustomer ? loadCurrentUserRewardBalance() : BigDecimal.ZERO);
        if (!model.containsAttribute("selectedPaymentPlan")) {
            model.addAttribute("selectedPaymentPlan", PAYMENT_PLAN_FULL_COD);
        }
        if (!model.containsAttribute("selectedPaymentMethod")) {
            model.addAttribute("selectedPaymentMethod", authenticatedCustomer ? PAYMENT_METHOD_SSLCOMMERZ : PAYMENT_METHOD_COD);
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
            Optional<BillingAddress> optionalBillingAddress = authenticatedCustomer
                    ? billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(loggedUserService.activeUserid())
                    : Optional.empty();

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

            } else if (authenticatedCustomer) {
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

        String availabilityRedirect = validateCheckoutAvailabilityForOrder(session, redirectAttributes, "/order/create");
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }
        if (!loggedUserService.isAuthenticatedUser()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please continue through the guest checkout flow.");
            return "redirect:/cart/checkout";
        }

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
            @RequestParam(name = "giftCardAmount", required = false) BigDecimal giftCardAmount,
            HttpServletRequest request) {

        String availabilityRedirect = validateCheckoutAvailabilityForOrder(session, redirectAttributes, "/order/create");
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }

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
        String vendorModeRedirect = validateCartVendorsForStoreMode(cartitem, redirectAttributes, "/order/create");
        if (vendorModeRedirect != null) {
            return vendorModeRedirect;
        }

        Users customer = resolveCheckoutCustomer(session, redirectAttributes, "/order/create");
        boolean guestCheckout = !loggedUserService.isAuthenticatedUser();
        if (customer == null) {
            return "redirect:/order/create";
        }
        String guestValidationRedirect = validateGuestCheckoutSelection(
                guestCheckout,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                couponCode,
                rewardPointsToUse,
                redirectAttributes,
                "/order/create"
        );
        if (guestValidationRedirect != null) {
            return guestValidationRedirect;
        }

        String fraudCheckoutRedirect = validateFraudCheckoutEligibility(
                customer,
                session,
                request,
                normalizedPaymentMethod,
                redirectAttributes,
                "/order/create"
        );
        if (fraudCheckoutRedirect != null) {
            return fraudCheckoutRedirect;
        }

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
        String codControlRedirect = validateCodCheckoutControls(
                customer,
                session,
                request,
                normalizedPaymentPlan,
                payableTotal,
                cartitem,
                redirectAttributes,
                "/order/create"
        );
        if (codControlRedirect != null) {
            return codControlRedirect;
        }

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

        applyProductShareReferral(customer, session);

//        String stockAvailabilityMessage = validateCartStockAvailability(cartitem);
//        if (stockAvailabilityMessage != null) {
//            redirectAttributes.addFlashAttribute("errorMessage", stockAvailabilityMessage);
//            return "redirect:/cart/index";
//        }
        // Group cart items by vendor (after loading cart)
        Map<Long, List<CartItem>> itemsByVendors = cartitem.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        // Prepare customer
        CustomerOrderGroup orderGroup = createInitialOrderGroup(customer, session, normalizedPaymentMethod);
        List<SalesOrder> orders = new ArrayList<>();

        // Loop per vendor
        for (Map.Entry<Long, List<CartItem>> entry : itemsByVendors.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setCustomer(customer);
            salesOrder.setOrderGroup(orderGroup);
            applyGuestCheckoutMetadata(salesOrder, session);
            salesOrder.setVendorId(vendorId);

            salesOrder.setStatus(OrderStatus.NEW_ORDER);

            // Save shipping address with order
            if (cartService.vendorCartRequiresShipping(items)) {
                ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
                ShippingAddress newShipping = new ShippingAddress();
                newShipping.copyFrom(shippingAddress);
                newShipping.setOrder(salesOrder);
                salesOrder.setShippingAddress(newShipping);
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
            if (customer != null) {
                referralService.distributeCommission(customer, commission, salesOrder.getId());
            }

            orders.add(salesOrder);
        }

        // Save billing address only if it's new (null ID)
        BillingAddress billingAddress = (BillingAddress) session.getAttribute("session_Billing_address");

        persistCheckoutBillingAddress(billingAddress, customer);

        checkoutIncentiveService.applyQuoteToOrders(incentiveQuote, customer, orders);
        orders.forEach(salesOrderRepository::save);
        finalizeOrderGroup(orderGroup, orders, normalizedPaymentMethod);

        Map<Long, BigDecimal> paymentDueNowByOrder = applyPaymentPlanToOrders(
                orders,
                normalizedPaymentPlan,
                immediatePaymentTotal
        );
        finalizeOrderGroup(orderGroup, orders, normalizedPaymentMethod);
        persistOrderIncentiveUsage(orders, incentiveQuote, paymentDueNowByOrder, normalizedPaymentMethod);

        FraudGuardResult orderFraudGuard = assessPlacedOrders(
                orders,
                session,
                request,
                normalizedPaymentMethod,
                incentiveQuote,
                paymentDueNowByOrder
        );

        // Clean session
        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        consumeGuestCheckoutSession(session);
        clearProductShareReferral(session);

        if (!orderFraudGuard.isAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", orderFraudGuard.getReason());
            return placedOrderRedirect(customer, orderGroup);
        }

        return finalizePlacedOrders(
                orders,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                paymentDueNowByOrder,
                immediatePaymentTotal,
                customer,
                orderGroup,
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
            @RequestParam(name = "giftCardAmount", required = false) BigDecimal giftCardAmount,
            HttpServletRequest request) {

        String availabilityRedirect = validateCheckoutAvailabilityForOrder(session, redirectAttributes, "/cart/checkout");
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }

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
        String vendorModeRedirect = validateCartVendorsForStoreMode(cartitem, redirectAttributes, "/cart/checkout");
        if (vendorModeRedirect != null) {
            return vendorModeRedirect;
        }

        boolean requiresShippingForCart = cartService.cartRequiresShipping(cartitem);
        BillingAddress sessionBillingAddress = (BillingAddress) session.getAttribute("session_Billing_address");
        ShippingAddress sessionShippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
        if (sessionBillingAddress == null || (requiresShippingForCart && sessionShippingAddress == null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Billing or Shipping address is missing!");
            return "redirect:/cart/checkout";
        }

        Users customer = resolveCheckoutCustomer(session, redirectAttributes, "/cart/checkout");
        boolean guestCheckout = !loggedUserService.isAuthenticatedUser();
        if (customer == null) {
            return "redirect:/cart/checkout";
        }
        String guestValidationRedirect = validateGuestCheckoutSelection(
                guestCheckout,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                couponCode,
                rewardPointsToUse,
                redirectAttributes,
                "/cart/checkout"
        );
        if (guestValidationRedirect != null) {
            return guestValidationRedirect;
        }

        String fraudCheckoutRedirect = validateFraudCheckoutEligibility(
                customer,
                session,
                request,
                normalizedPaymentMethod,
                redirectAttributes,
                "/cart/checkout"
        );
        if (fraudCheckoutRedirect != null) {
            return fraudCheckoutRedirect;
        }

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
        String codControlRedirect = validateCodCheckoutControls(
                customer,
                session,
                request,
                normalizedPaymentPlan,
                payableTotal,
                cartitem,
                redirectAttributes,
                "/cart/checkout"
        );
        if (codControlRedirect != null) {
            return codControlRedirect;
        }

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

        applyProductShareReferral(customer, session);

//        String stockAvailabilityMessage = validateCartStockAvailability(cartitem);
//        if (stockAvailabilityMessage != null) {
//            redirectAttributes.addFlashAttribute("errorMessage", stockAvailabilityMessage);
//            return "redirect:/cart/index";
//        }
        // 2. Grouping
        Map<Long, List<CartItem>> itemsByVendors = cartitem.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        CustomerOrderGroup orderGroup = createInitialOrderGroup(customer, session, normalizedPaymentMethod);
        List<SalesOrder> orders = new ArrayList<>();

        // 3. Loop per vendor
        for (Map.Entry<Long, List<CartItem>> entry : itemsByVendors.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartItem> items = entry.getValue();

            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setCustomer(customer);
            salesOrder.setOrderGroup(orderGroup);
            applyGuestCheckoutMetadata(salesOrder, session);
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
            if (customer != null) {
                referralService.distributeCommission(customer, referralCommission, salesOrder.getId());
            }

            orders.add(salesOrder);
        }

        checkoutIncentiveService.applyQuoteToOrders(incentiveQuote, customer, orders);
        orders.forEach(salesOrderRepository::save);
        finalizeOrderGroup(orderGroup, orders, normalizedPaymentMethod);

        Map<Long, BigDecimal> paymentDueNowByOrder = applyPaymentPlanToOrders(
                orders,
                normalizedPaymentPlan,
                immediatePaymentTotal
        );
        finalizeOrderGroup(orderGroup, orders, normalizedPaymentMethod);
        persistOrderIncentiveUsage(orders, incentiveQuote, paymentDueNowByOrder, normalizedPaymentMethod);

        FraudGuardResult orderFraudGuard = assessPlacedOrders(
                orders,
                session,
                request,
                normalizedPaymentMethod,
                incentiveQuote,
                paymentDueNowByOrder
        );

        // 8. Finalize Billing and Cleanup
        persistCheckoutBillingAddress(sessionBillingAddress, customer);
        // Clean up all related session attributes
        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        consumeGuestCheckoutSession(session);
        clearProductShareReferral(session);

        // Clear dynamic session keys for costs
        itemsByVendors.keySet().forEach(vId -> {
            session.removeAttribute("shippingCost_" + vId);
            session.removeAttribute("packagingCost_" + vId);
            session.removeAttribute("shippingOption_" + vId);
            session.removeAttribute("packagingRate_" + vId);
        });

        if (!orderFraudGuard.isAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", orderFraudGuard.getReason());
            return placedOrderRedirect(customer, orderGroup);
        }

        return finalizePlacedOrders(
                orders,
                normalizedPaymentPlan,
                normalizedPaymentMethod,
                paymentDueNowByOrder,
                immediatePaymentTotal,
                customer,
                orderGroup,
                emiTenureMonths,
                redirectAttributes
        );
    }

    @GetMapping("/placed")
    public String placed(@RequestParam(name = "group", required = false) String groupUuid, Model model) {
        CustomerOrderGroup orderGroup = groupUuid == null || groupUuid.isBlank()
                ? null
                : customerOrderGroupRepository.findByUuid(groupUuid.trim()).orElse(null);
        model.addAttribute("orderGroup", orderGroup);
        return "order/order/placed";
    }

    private String validateCheckoutAvailabilityForOrder(
            HttpSession session,
            RedirectAttributes redirectAttributes,
            String redirectPath) {
        boolean authenticatedCustomer = loggedUserService.isAuthenticatedUser();
        CheckoutAvailability availability = checkoutAvailabilityService.availability(authenticatedCustomer);
        if (!availability.isCheckoutAvailable()) {
            redirectAttributes.addFlashAttribute("errorMessage", CheckoutAvailabilityService.CHECKOUT_UNAVAILABLE_MESSAGE);
            return "redirect:/cart/index";
        }
        if (!authenticatedCustomer && availability.isLoginRequired() && !availability.isGuestAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login before checkout.");
            return "redirect:/public/member-login";
        }
        return null;
    }

    private Users resolveCheckoutCustomer(HttpSession session, RedirectAttributes redirectAttributes, String redirectPath) {
        if (loggedUserService.isAuthenticatedUser()) {
            return loggedUserService.activeUserOptional()
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        if (!storeOperationModeService.isGuestCheckoutAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login before checkout.");
            return null;
        }
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please verify your mobile number before checkout.");
            return null;
        }
        if (storeOperationModeService.isGuestMobileOtpVerificationEnabled()
                && guestSession.getMobileVerificationStatus() != MobileVerificationStatus.VERIFIED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please verify your mobile number before checkout.");
            return null;
        }
        return usersRepository.findById(guestSession.getUserId()).orElse(null);
    }

    private String validateGuestCheckoutSelection(
            boolean guestCheckout,
            String paymentPlan,
            String paymentMethod,
            String couponCode,
            BigDecimal rewardPointsToUse,
            RedirectAttributes redirectAttributes,
            String redirectPath
    ) {
        if (!guestCheckout) {
            return null;
        }
        if (!storeOperationModeService.isGuestCheckoutAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Guest checkout is currently disabled.");
            return "redirect:/public/member-login";
        }
        if (PAYMENT_PLAN_EMI.equals(paymentPlan) || PAYMENT_METHOD_EMI.equals(paymentMethod)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to use EMI checkout.");
            return "redirect:" + redirectPath;
        }
        if (PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to pay an advance during checkout.");
            return "redirect:" + redirectPath;
        }
        if (PAYMENT_METHOD_WALLET.equals(paymentMethod)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to use wallet payment.");
            return "redirect:" + redirectPath;
        }
        if (isOnlineCheckoutMethod(paymentMethod)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to use online payment. Guest checkout currently supports COD or fully covered gift-card orders.");
            return "redirect:" + redirectPath;
        }
        if (safeMoney(rewardPointsToUse).compareTo(BigDecimal.ZERO) > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to redeem reward points.");
            return "redirect:" + redirectPath;
        }
        if (couponCode != null && !couponCode.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to use coupon codes.");
            return "redirect:" + redirectPath;
        }
        return null;
    }

    private String validateCartVendorsForStoreMode(
            List<CartItem> cartItems,
            RedirectAttributes redirectAttributes,
            String redirectPath
    ) {
        if (!storeOperationModeService.isSingleVendorMode()) {
            return null;
        }
        Long primaryVendorId = storeOperationModeService.primaryVendorId();
        if (primaryVendorId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Single-vendor mode needs a primary vendor in settings.");
            return "redirect:" + redirectPath;
        }
        boolean hasOutsideVendor = cartItems.stream()
                .filter(Objects::nonNull)
                .map(CartItem::getVendorId)
                .anyMatch(vendorId -> !Objects.equals(primaryVendorId, vendorId));
        if (hasOutsideVendor) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart contains products outside the configured primary vendor. Please refresh the cart before checkout."
            );
            return "redirect:/cart/index";
        }
        return null;
    }

    private CustomerOrderGroup createInitialOrderGroup(Users customer, HttpSession session, String paymentMethod) {
        CustomerOrderGroup group = new CustomerOrderGroup();
        group.setOrderGroupCode(customerOrderGroupCodeGeneratorService.generateNextDailyOrderGroupCode());
        group.setCustomer(customer);
        group.setPaymentMethod(parsePaymentMethod(paymentMethod));
        group.setPaymentState(OrderPaymentState.UNPAID);
        group.setStatusSummary(OrderStatus.NEW_ORDER.name());

        BillingAddress billingAddress = (BillingAddress) session.getAttribute("session_Billing_address");
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession != null) {
            group.setGuestCheckout(true);
            group.setGuestPhone(guestSession.getVerifiedMobile());
            group.setGuestSessionId(session.getId());
            group.setMobileNumber(guestSession.getVerifiedMobile());
            group.setMobileVerificationRequired(guestSession.isMobileVerificationRequired());
            group.setMobileVerificationStatus(guestSession.getMobileVerificationStatus());
            group.setMobileVerifiedAt(guestSession.getVerificationTime());
            group.setCheckoutSessionId(guestSession.getCheckoutSessionUuid());
        }
        if (customer == null && billingAddress != null) {
            group.setGuestName(joinName(billingAddress.getFirstName(), billingAddress.getLastName()));
            group.setGuestEmail(cleanText(billingAddress.getEmail()));
            group.setGuestPhone(cleanText(billingAddress.getMobile()));
            group.setGuestSessionId(session.getId());
        }

        return customerOrderGroupRepository.save(group);
    }

    private void finalizeOrderGroup(CustomerOrderGroup group, List<SalesOrder> orders, String paymentMethod) {
        if (group == null || orders == null) {
            return;
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal shipping = BigDecimal.ZERO;
        BigDecimal packing = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SalesOrder order : orders) {
            subtotal = subtotal.add(safeMoney(order.getItemtotal()));
            shipping = shipping.add(safeMoney(order.getDeliveryCharge()));
            packing = packing.add(safeMoney(order.getPackingCharge()));
            discount = discount.add(safeMoney(order.getTotalDiscountAmount()));
            grandTotal = grandTotal.add(safeMoney(order.getGrandTotal()));
        }

        group.setSubtotal(subtotal);
        group.setShippingTotal(shipping);
        group.setPackingTotal(packing);
        group.setDiscountTotal(discount);
        group.setGrandTotal(grandTotal);
        group.setPaymentMethod(parsePaymentMethod(paymentMethod));
        group.setPaymentState(resolveGroupPaymentState(orders));
        group.setStatusSummary(resolveGroupStatusSummary(orders));
        customerOrderGroupRepository.save(group);
    }

    private OrderPaymentState resolveGroupPaymentState(List<SalesOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return OrderPaymentState.UNPAID;
        }
        boolean allPaid = orders.stream()
                .map(SalesOrder::getPaymentState)
                .allMatch(OrderPaymentState.PAID::equals);
        if (allPaid) {
            return OrderPaymentState.PAID;
        }
        boolean anyAdvancePending = orders.stream()
                .map(SalesOrder::getPaymentState)
                .anyMatch(OrderPaymentState.ADVANCE_PENDING::equals);
        if (anyAdvancePending) {
            return OrderPaymentState.ADVANCE_PENDING;
        }
        boolean anyEmiPending = orders.stream()
                .map(SalesOrder::getPaymentState)
                .anyMatch(OrderPaymentState.EMI_PENDING::equals);
        if (anyEmiPending) {
            return OrderPaymentState.EMI_PENDING;
        }
        boolean anyCodPending = orders.stream()
                .map(SalesOrder::getPaymentState)
                .anyMatch(OrderPaymentState.COD_PENDING::equals);
        return anyCodPending ? OrderPaymentState.COD_PENDING : OrderPaymentState.UNPAID;
    }

    private String resolveGroupStatusSummary(List<SalesOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return OrderStatus.NEW_ORDER.name();
        }
        return orders.stream()
                .map(SalesOrder::getStatus)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private PaymentMethod parsePaymentMethod(String paymentMethod) {
        try {
            return paymentMethod == null ? null : PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String joinName(String firstName, String lastName) {
        String value = (cleanText(firstName) == null ? "" : cleanText(firstName))
                + " "
                + (cleanText(lastName) == null ? "" : cleanText(lastName));
        return cleanText(value);
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    private String placedOrderRedirect(Users customer, CustomerOrderGroup orderGroup) {
        if (customer != null && loggedUserService.isAuthenticatedUser()) {
            return "redirect:/customerorder/index";
        }
        if (orderGroup != null && orderGroup.getUuid() != null) {
            return "redirect:/order/placed?group=" + orderGroup.getUuid();
        }
        return "redirect:/order/placed";
    }

    private void applyProductShareReferral(Users customer, HttpSession session) {
        if (customer == null || session == null) {
            return;
        }

        Object referralCode = session.getAttribute(PRODUCT_SHARE_REFERRAL_CODE_SESSION_KEY);
        if (!(referralCode instanceof String) || ((String) referralCode).isBlank()) {
            return;
        }

        Object productUuid = session.getAttribute(PRODUCT_SHARE_REFERRAL_PRODUCT_SESSION_KEY);
        referralService.applyProductShareReferralIfEligible(
                customer,
                ((String) referralCode).trim(),
                productUuid instanceof String ? ((String) productUuid).trim() : null
        );
    }

    private void clearProductShareReferral(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(PRODUCT_SHARE_REFERRAL_CODE_SESSION_KEY);
        session.removeAttribute(PRODUCT_SHARE_REFERRAL_PRODUCT_SESSION_KEY);
    }

    private void consumeGuestCheckoutSession(HttpSession session) {
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession == null) {
            return;
        }
        guestCheckoutOtpService.markUsed(guestSession.getOtpVerificationUuid());
        guestCheckoutSessionService.clear(session);
    }

    private void applyGuestCheckoutMetadata(SalesOrder salesOrder, HttpSession session) {
        if (salesOrder == null) {
            return;
        }
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession == null) {
            return;
        }
        salesOrder.setGuestCheckout(true);
        salesOrder.setMobileNumber(guestSession.getVerifiedMobile());
        salesOrder.setMobileVerificationRequired(guestSession.isMobileVerificationRequired());
        salesOrder.setMobileVerificationStatus(guestSession.getMobileVerificationStatus());
        salesOrder.setMobileVerifiedAt(guestSession.getVerificationTime());
        salesOrder.setCheckoutSessionId(guestSession.getCheckoutSessionUuid());
    }

    private ShippingLocation currentShippingLocation(HttpSession session) {
        Object value = session.getAttribute("shippingLocation");
        return value instanceof ShippingLocation location ? location : null;
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

    private String validateFraudCheckoutEligibility(Users customer, HttpSession session, HttpServletRequest request,
            String paymentMethod, RedirectAttributes redirectAttributes, String redirectPath) {
        FraudContext context = buildFraudContext(session, request, paymentMethod);
        context.setSalesChannel(loggedUserService.isAuthenticatedUser() ? "CUSTOMER_WEB" : "GUEST_WEB");
        String mobileNumber = resolveFraudMobileNumber(customer, session);
        if (mobileNumber != null) {
            context.getMetadata().put("mobileNumber", mobileNumber);
        }

        FraudGuardResult fraudGuard = fraudPreOrderGuard.checkCheckoutEligibility(
                customer == null ? null : customer.getId(),
                context
        );
        if (fraudGuard.isAllowed()) {
            return null;
        }
        redirectAttributes.addFlashAttribute("errorMessage", fraudGuard.getReason());
        return "redirect:" + redirectPath;
    }

    private String validateCodCheckoutControls(Users customer, HttpSession session, HttpServletRequest request,
            String paymentPlan, BigDecimal payableTotal, List<CartItem> cartItems,
            RedirectAttributes redirectAttributes, String redirectPath) {
        if (!PAYMENT_PLAN_FULL_COD.equals(paymentPlan) && !PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)) {
            return null;
        }

        FraudContext context = buildFraudContext(session, request, PAYMENT_METHOD_COD);
        context.getMetadata().putIfAbsent("mobileNumber", resolveFraudMobileNumber(customer, session));
        context.getMetadata().putIfAbsent("addressKey", buildAddressKey(
                (ShippingAddress) session.getAttribute("session_Shipping_address"),
                (BillingAddress) session.getAttribute("session_Billing_address")
        ));

        Map<Long, BigDecimal> vendorTotals = cartItems == null ? Map.of() : cartItems.stream()
                .filter(item -> item != null && item.getVendorId() != null)
                .collect(Collectors.groupingBy(
                        CartItem::getVendorId,
                        Collectors.reducing(BigDecimal.ZERO, item -> safeMoney(item.getItemTotal()), BigDecimal::add)
                ));
        if (vendorTotals.isEmpty()) {
            FraudGuardResult codGuard = codEligibilityService.checkCodCheckoutEligibility(
                    customer == null ? null : customer.getId(),
                    null,
                    payableTotal,
                    paymentPlan,
                    isMobileVerifiedForCod(customer, session),
                    context
            );
            if (!codGuard.isAllowed()) {
                redirectAttributes.addFlashAttribute("errorMessage", codGuard.getReason());
                return "redirect:" + redirectPath;
            }
        }
        for (Map.Entry<Long, BigDecimal> entry : vendorTotals.entrySet()) {
            FraudGuardResult codGuard = codEligibilityService.checkCodCheckoutEligibility(
                    customer == null ? null : customer.getId(),
                    entry.getKey(),
                    entry.getValue(),
                    paymentPlan,
                    isMobileVerifiedForCod(customer, session),
                    context
            );
            if (!codGuard.isAllowed()) {
                redirectAttributes.addFlashAttribute("errorMessage", codGuard.getReason());
                return "redirect:" + redirectPath;
            }
        }
        return null;
    }

    private FraudGuardResult assessPlacedOrders(List<SalesOrder> orders, HttpSession session, HttpServletRequest request,
            String paymentMethod, CheckoutIncentiveQuote incentiveQuote, Map<Long, BigDecimal> paymentDueNowByOrder) {
        if (orders == null || orders.isEmpty()) {
            return FraudGuardResult.allowed();
        }
        for (SalesOrder order : orders) {
            FraudContext context = buildFraudContext(session, request, paymentMethod);
            context.setVendorId(order.getVendorId());
            context.setOrderValue(order.getGrandTotal());
            if (order.getShippingAddress() != null) {
                context.setShippingCountry(order.getShippingAddress().getCountry());
                context.setShippingDistrict(order.getShippingAddress().getDistrict());
            }
            context.setCouponCode(incentiveQuote == null ? null : incentiveQuote.getCouponCode());
            context.setGiftCardAmount(safeMoney(incentiveQuote == null ? null : incentiveQuote.getGiftCardUsedByOrder().get(order.getId())));
            context.setCouponDiscountAmount(safeMoney(incentiveQuote == null ? null : incentiveQuote.getCouponDiscountByOrder().get(order.getId())));
            context.setWalletAmount(PAYMENT_METHOD_WALLET.equals(paymentMethod)
                    ? safeMoney(paymentDueNowByOrder == null ? null : paymentDueNowByOrder.get(order.getId()))
                    : BigDecimal.ZERO);
            if (incentiveQuote != null && (safeMoney(incentiveQuote.getCouponDiscountByOrder().get(order.getId())).compareTo(BigDecimal.ZERO) > 0
                    || safeMoney(incentiveQuote.getRewardDiscountByOrder().get(order.getId())).compareTo(BigDecimal.ZERO) > 0
                    || safeMoney(incentiveQuote.getGiftCardUsedByOrder().get(order.getId())).compareTo(BigDecimal.ZERO) > 0)) {
                context.setPromotionType("CHECKOUT_INCENTIVE");
            }
            context.getMetadata().put("rewardPointsUsed",
                    safeMoney(incentiveQuote == null ? null : incentiveQuote.getRewardPointsByOrder().get(order.getId())));
            context.getMetadata().put("idempotencyKey", "ORDER-" + order.getId() + "-FRAUD-ASSESSMENT");
            context.getMetadata().put("correlationId", order.getUuid());

            FraudGuardResult fraudGuard = fraudOrderAssessmentGuard.checkOrderAllowed(order, context);
            if (!fraudGuard.isAllowed()) {
                return fraudGuard;
            }
            recordCodRiskProfile(order, context);
            vendorRiskProfileService.evaluateOrderForVendorRisk(order, context);
        }
        return FraudGuardResult.allowed();
    }

    private FraudContext buildFraudContext(HttpSession session, HttpServletRequest request, String paymentMethod) {
        FraudContext context = new FraudContext();
        context.setPaymentMethod(paymentMethod);
        context.setSessionIdentifier(session == null ? null : session.getId());
        if (request != null) {
            context.setIpAddress(resolveClientIp(request));
            context.setUserAgent(request.getHeader("User-Agent"));
        }
        ShippingAddress shippingAddress = session == null ? null : (ShippingAddress) session.getAttribute("session_Shipping_address");
        BillingAddress billingAddress = session == null ? null : (BillingAddress) session.getAttribute("session_Billing_address");
        if (shippingAddress != null) {
            context.setShippingCountry(shippingAddress.getCountry());
            context.setShippingDistrict(shippingAddress.getDistrict());
            context.getMetadata().put("mobileNumber", shippingAddress.getMobile());
        }
        if (billingAddress != null) {
            context.setBillingCountry(billingAddress.getCountry());
            context.getMetadata().putIfAbsent("mobileNumber", billingAddress.getMobile());
        }
        return context;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveFraudMobileNumber(Users customer, HttpSession session) {
        if (customer != null && customer.getMobile() != null && !customer.getMobile().isBlank()) {
            return customer.getMobile();
        }
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession != null && guestSession.getVerifiedMobile() != null && !guestSession.getVerifiedMobile().isBlank()) {
            return guestSession.getVerifiedMobile();
        }
        return null;
    }

    private void recordCodRiskProfile(SalesOrder order, FraudContext context) {
        if (order == null) {
            return;
        }
        if (order.getPaymentPlan() == OrderPaymentPlan.FULL_COD || order.getPaymentPlan() == OrderPaymentPlan.PARTIAL_ADVANCE_COD) {
            codRiskProfileService.recordCodOrderPlaced(order, context);
        }
    }

    private boolean isMobileVerifiedForCod(Users customer, HttpSession session) {
        if (customer != null && customer.isMobileVerified()) {
            return true;
        }
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        return guestSession != null && guestSession.getMobileVerificationStatus() == MobileVerificationStatus.VERIFIED;
    }

    private String buildAddressKey(ShippingAddress shippingAddress, BillingAddress billingAddress) {
        if (shippingAddress != null) {
            return String.join("|",
                    safeText(shippingAddress.getAddressLineOne()),
                    safeText(shippingAddress.getAddressLinetwo()),
                    safeText(shippingAddress.getCity()),
                    safeText(shippingAddress.getDistrict()),
                    safeText(shippingAddress.getPostCode()),
                    safeText(shippingAddress.getCountry()),
                    safeText(shippingAddress.getMobile()));
        }
        if (billingAddress != null) {
            return String.join("|",
                    safeText(billingAddress.getAddressLineOne()),
                    safeText(billingAddress.getAddressLinetwo()),
                    safeText(billingAddress.getCity()),
                    safeText(billingAddress.getDistrict()),
                    safeText(billingAddress.getPostCode()),
                    safeText(billingAddress.getCountry()),
                    safeText(billingAddress.getMobile()));
        }
        return null;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private FraudGuardResult firstPaymentFraudBlock(List<SalesOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return FraudGuardResult.allowed();
        }
        for (SalesOrder order : orders) {
            FraudGuardResult fraudGuard = fraudPaymentCaptureGuard.checkPaymentCaptureAllowed(order);
            if (!fraudGuard.isAllowed()) {
                return fraudGuard;
            }
        }
        return FraudGuardResult.allowed();
    }

    private String finalizePlacedOrders(List<SalesOrder> orders, String paymentPlan, String paymentMethod,
            Map<Long, BigDecimal> paymentDueNowByOrder, BigDecimal immediatePaymentTotal, Users customer,
            CustomerOrderGroup orderGroup, Integer emiTenureMonths, RedirectAttributes redirectAttributes) {
        String normalizedPaymentMethod = resolveCheckoutPaymentMethod(paymentPlan, paymentMethod);

        boolean incentivesCoveredAllPayable = safeMoney(immediatePaymentTotal).compareTo(BigDecimal.ZERO) <= 0
                && orders.stream()
                        .map(paymentService::getPaymentSummary)
                        .allMatch(PaymentService.PaymentSummary::isFullyPaid);
        boolean fraudApprovalRequiredBeforeNextStep = incentivesCoveredAllPayable
                || PAYMENT_METHOD_WALLET.equals(normalizedPaymentMethod)
                || PAYMENT_PLAN_EMI.equals(paymentPlan)
                || isOnlineCheckoutMethod(normalizedPaymentMethod);
        if (fraudApprovalRequiredBeforeNextStep) {
            FraudGuardResult fraudGuard = firstPaymentFraudBlock(orders);
            if (!fraudGuard.isAllowed()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        fraudGuard.getReason() == null
                        ? "Order placed, but payment or fulfilment is waiting for fraud approval."
                        : fraudGuard.getReason()
                );
                return placedOrderRedirect(customer, orderGroup);
            }
        }
        if (incentivesCoveredAllPayable) {
            for (SalesOrder order : orders) {
                SalesOrder paidOrder = salesOrderService.finalizePaidOrder(order.getId());
                if (customer != null) {
                    try {
                        BigDecimal expected = cashbackService.computeExpectedCashback(paidOrder, safeMoney(paidOrder.getGrandTotal()));
                        cashbackService.createPendingCashbackIfMissing(customer, paidOrder, expected);
                    } catch (Exception ignored) {
                    }
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
            return placedOrderRedirect(customer, orderGroup);
        }

        if (PAYMENT_METHOD_WALLET.equals(normalizedPaymentMethod)) {
            if (!captureWalletPayment(customer, immediatePaymentTotal)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Orders were placed, but the wallet payment could not be completed. Please review the payment status from your order list."
                );
                return placedOrderRedirect(customer, orderGroup);
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
            return placedOrderRedirect(customer, orderGroup);
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
                if (customer == null) {
                    return placedOrderRedirect(null, orderGroup);
                }
                return "redirect:/customerorder/payment/" + orders.get(0).getId() + "?method=" + normalizedPaymentMethod;
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    PAYMENT_PLAN_PARTIAL_ADVANCE_COD.equals(paymentPlan)
                    ? "Orders placed successfully. Use Pay Now on each order to pay the planned advance amount. The remaining balance is marked COD."
                    : "Orders placed successfully. Use Pay Now on each order to complete payment via SSLCommerz or bKash."
            );
            return placedOrderRedirect(customer, orderGroup);
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
        return placedOrderRedirect(customer, orderGroup);
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
