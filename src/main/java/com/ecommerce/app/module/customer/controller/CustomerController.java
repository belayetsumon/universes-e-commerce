/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.model.Profile;
import com.ecommerce.app.model.ProfileImage;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.wishlist.repository.WishlistItemRepository;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.OrderPaymentState;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.BillingAddressRepository;
import com.ecommerce.app.ripository.ProfileRepository;
import com.ecommerce.app.ripository.ProfileImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorCodeGenerator;
import com.ecommerce.app.services.BarcodeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import com.google.zxing.BarcodeFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer")
@PreAuthorize("hasAuthority('customer')")
public class CustomerController {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_LABEL = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yy");
    private static final EnumSet<OrderStatus> RETURN_RELATED_STATUSES = EnumSet.of(
            OrderStatus.RETURN_REQUESTED,
            OrderStatus.PARTIALLY_RETURNED,
            OrderStatus.RETURNED
    );
    private static final EnumSet<OrderStatus> FULFILLED_STATUSES = EnumSet.of(
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED
    );

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    WishlistItemRepository wishlistItemRepository;

    @Autowired
    BillingAddressRepository billingAddressRepository;

    @Autowired
    RewardAccountRepository rewardAccountRepository;

    @Autowired
    CashOutRequestRepository cashOutRequestRepository;

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    VendorCodeGenerator vendorCodeGenerator;

    @Autowired
    BarcodeService barcodeService;

    @RequestMapping(value = {"", "/", "/index", "dashboards"})
    public String index(Model model) {
        Long activeUserId = loggedUserService.activeUserid();
        String username = loggedUserService.activeUserName();

        Users user = usersRepository.findById(activeUserId).orElseGet(() -> {
            Users fallbackUser = new Users();
            fallbackUser.setId(activeUserId);
            fallbackUser.setFirstName(username);
            return fallbackUser;
        });

        List<SalesOrder> orders = salesOrderRepository.findByCustomerOrderByIdDesc(user);
        List<SalesOrder> pendingOrders = salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(user, OrderStatus.PENDING);
        List<SalesOrder> completedOrders = salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(user, OrderStatus.COMPLETED);
        List<Vendorprofile> stores = vendorprofileRepository.findByUserId(user);
        List<CashOutRequest> cashOutRequests = cashOutRequestRepository.findByUserOrderByRequestedAtDesc(user);

        Profile customerProfile = profileRepository.findByUserId(user);
        ProfileImage profileImage = profileImageRepository.findByUserId(user);
        BillingAddress billingAddress = billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(activeUserId).orElse(null);
        BigDecimal walletBalance = rewardAccountRepository.findByUsers(user)
                .map(com.ecommerce.app.module.ReferralRewards.model.RewardAccount::getBalance)
                .orElse(BigDecimal.ZERO);
        String referralCode = referralRepository.findByUsers(user)
                .map(Referral::getReferralCode)
                .orElse("");
        String referralRegistrationUrl = referralCode == null || referralCode.isBlank()
                ? "/public/front-registration"
                : "/public/front-registration?ref=" + URLEncoder.encode(referralCode, StandardCharsets.UTF_8);
        String referralInviteMessage = referralCode == null || referralCode.isBlank()
                ? "Register on our site and start shopping: /customerregister/register"
                : "Register on our site using my referral code " + referralCode
                + " to join and buy products: " + referralRegistrationUrl;
        String referralWhatsAppUrl = "https://wa.me/?text="
                + URLEncoder.encode(referralInviteMessage, StandardCharsets.UTF_8);
        String referralFacebookUrl = "https://www.facebook.com/sharer/sharer.php?u="
                + URLEncoder.encode(referralRegistrationUrl, StandardCharsets.UTF_8);
        String referralQrBase64 = referralCode == null || referralCode.isBlank()
                ? null
                : barcodeDataUri(referralRegistrationUrl, BarcodeFormat.QR_CODE, 160, 160);

        long totalOrders = orders.size();
        long openOrders = orders.stream().filter(order -> isOpenStatus(order.getStatus())).count();
        long fulfilledOrders = orders.stream()
                .filter(order -> order.getStatus() != null && FULFILLED_STATUSES.contains(order.getStatus()))
                .count();
        long returnOrders = orders.stream()
                .filter(order -> order.getStatus() != null && RETURN_RELATED_STATUSES.contains(order.getStatus()))
                .count();
        long cancelledOrders = orders.stream().filter(order -> order.getStatus() == OrderStatus.CANCELLED).count();
        long outstandingPayments = orders.stream().filter(this::hasOutstandingPayment).count();
        long wishlistCount = wishlistItemRepository.countByUser_Id(activeUserId);
        long referredCustomerCount = referralRepository.countByUsers_IdAndReferredUserIsNotNull(activeUserId);
        long activeStoreCount = stores.stream().filter(store -> store.getVendorStatusEnum() == VendorStatusEnum.Active).count();
        long pendingStoreCount = stores.stream().filter(store -> store.getVendorStatusEnum() == VendorStatusEnum.Pending).count();

        long placedOrderCount = Math.max(totalOrders - cancelledOrders, 0);
        BigDecimal grossOrderValue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(order -> defaultAmount(order.getGrandTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageOrderValue = placedOrderCount > 0
                ? grossOrderValue.divide(BigDecimal.valueOf(placedOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int profileCompletionPercent = calculateProfileCompletion(user, customerProfile, billingAddress, profileImage);
        long attentionCount = outstandingPayments + returnOrders + pendingStoreCount;

        List<String> monthlyLabels = new ArrayList<>();
        List<BigDecimal> monthlySpendValues = new ArrayList<>();
        List<Long> monthlyOrderCounts = new ArrayList<>();
        buildMonthlyTrend(orders, monthlyLabels, monthlySpendValues, monthlyOrderCounts);

        List<String> orderStatusLabels = new ArrayList<>();
        List<Long> orderStatusCounts = new ArrayList<>();
        buildOrderStatusBreakdown(orders, orderStatusLabels, orderStatusCounts);

        model.addAttribute("pageTitle", "Customer Dashboard");
        model.addAttribute("username", username);
        model.addAttribute("customerName", resolveCustomerName(user, customerProfile, username));
        model.addAttribute("customerInitials", buildInitials(resolveCustomerName(user, customerProfile, username)));
        model.addAttribute("customerEmail", user.getEmail());
        model.addAttribute("customerMobile", user.getMobile());
        model.addAttribute("customerSinceLabel", formatDate(user.getCreatedOn()));
        model.addAttribute("lastLoginLabel", formatDate(user.getLastLogin()));
        model.addAttribute("profileCompletionPercent", profileCompletionPercent);
        model.addAttribute("profileCompletionLabel", profileCompletionPercent + "% of your account checklist is complete.");
        model.addAttribute("profileImageName", profileImage != null ? profileImage.getImageName() : null);
        model.addAttribute("billingAddressSummary", summarizeBillingAddress(billingAddress));
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("walletBalanceLabel", formatMoney(walletBalance));
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralRegistrationUrl", referralRegistrationUrl);
        model.addAttribute("referralInviteMessage", referralInviteMessage);
        model.addAttribute("referralWhatsAppUrl", referralWhatsAppUrl);
        model.addAttribute("referralFacebookUrl", referralFacebookUrl);
        model.addAttribute("referralQrBase64", referralQrBase64);
        model.addAttribute("grossOrderValueLabel", formatMoney(grossOrderValue));
        model.addAttribute("averageOrderValueLabel", formatMoney(averageOrderValue));
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("openOrders", openOrders);
        model.addAttribute("fulfilledOrders", fulfilledOrders);
        model.addAttribute("returnOrders", returnOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("referredCustomerCount", referredCustomerCount);
        model.addAttribute("storeCount", stores.size());
        model.addAttribute("activeStoreCount", activeStoreCount);
        model.addAttribute("pendingStoreCount", pendingStoreCount);
        model.addAttribute("outstandingPayments", outstandingPayments);
        model.addAttribute("attentionCount", attentionCount);
        model.addAttribute("storeSummaryLabel", buildStoreSummary(stores.size(), activeStoreCount, pendingStoreCount));
        model.addAttribute("accountSupportLabel", buildSupportSummary(customerProfile, billingAddress, profileImage));
        model.addAttribute("latestCashOutSummary", buildCashOutSummary(cashOutRequests));
        model.addAttribute("monthlyLabels", monthlyLabels);
        model.addAttribute("monthlySpendValues", monthlySpendValues);
        model.addAttribute("monthlyOrderCounts", monthlyOrderCounts);
        model.addAttribute("orderStatusLabels", orderStatusLabels);
        model.addAttribute("orderStatusCounts", orderStatusCounts);
        model.addAttribute("recentOrders", buildRecentOrders(orders));
        model.addAttribute("priorityActions", buildActionCards(
                totalOrders,
                wishlistCount,
                walletBalance,
                stores.size(),
                referredCustomerCount,
                cashOutRequests.size(),
                outstandingPayments
        ));

        model.addAttribute("orderlist", orders);
        model.addAttribute("orderlist_panding", pendingOrders);
        model.addAttribute("examlist", completedOrders);

        return "customer/index";
    }

    @RequestMapping(value = {"/create"})
    public String create(Model model, Vendorprofile vendorprofile) {

        Users users = new Users();

        users.setId(loggedUserService.activeUserid());

        vendorprofile.setUserId(users);

        vendorprofile.setVendorCode(vendorCodeGenerator.generateNextVendorCode());

        return "customer/vendor_profile_create";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Users users = new Users();
            users.setId(loggedUserService.activeUserid());
            vendorprofile.setUserId(users);
            return "customer/vendor_profile_create";
        }

        vendorprofile.setVendorStatusEnum(VendorStatusEnum.Pending);
        vendorprofileRepository.save(vendorprofile);
        return "redirect:/customer/storelist";
    }

    @RequestMapping(value = {"/storelist"})
    public String storeList(Model model, HttpSession session) {

        model.addAttribute("username", loggedUserService.activeUserName());

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        session.removeAttribute("vendorprofile");

        List<Vendorprofile> stores = vendorprofileRepository.findByUserId(userId);
        model.addAttribute("storelist", stores);
        model.addAttribute("activeStoreCount", stores.stream()
                .filter(store -> store.getVendorStatusEnum() == VendorStatusEnum.Active)
                .count());
        model.addAttribute("pendingStoreCount", stores.stream()
                .filter(store -> store.getVendorStatusEnum() == VendorStatusEnum.Pending)
                .count());

        return "customer/storelist";
    }

    private List<Map<String, Object>> buildActionCards(long totalOrders,
            long wishlistCount,
            BigDecimal walletBalance,
            int storeCount,
            long referredCustomerCount,
            int cashOutRequestCount,
            long outstandingPayments) {
        List<Map<String, Object>> actions = new ArrayList<>();
        actions.add(actionCard(
                "Orders",
                totalOrders + " tracked",
                outstandingPayments > 0
                        ? outstandingPayments + " order(s) still need payment attention."
                        : "Review recent orders, delivery progress, and returns from one place.",
                "bi-bag-check",
                "tone-navy",
                "/customerorder/index",
                outstandingPayments > 0 ? "Resolve Payments" : "Open Orders"
        ));
        actions.add(actionCard(
                "Wishlist",
                wishlistCount + " saved",
                "Keep your shortlist ready for the next purchase or back-in-stock moment.",
                "bi-heart",
                "tone-rose",
                "/wishlist/index",
                "View Wishlist"
        ));
        actions.add(actionCard(
                "Rewards Wallet",
                formatMoney(walletBalance),
                "Track referral rewards and move available balance into cash-out requests.",
                "bi-wallet2",
                "tone-teal",
                "/customerwallet/wallet",
                "Open Wallet"
        ));
        actions.add(actionCard(
                "Vendor Panel",
                storeCount + " store" + (storeCount == 1 ? "" : "s"),
                "Manage your seller-side footprint without leaving the customer account area.",
                "bi-shop",
                "tone-amber",
                "/customer/storelist",
                storeCount == 0 ? "Create Store" : "Open Vendor Panel"
        ));
        actions.add(actionCard(
                "Referral Rewards",
                referredCustomerCount + " joined",
                "See who joined your network, review reward balance, and open the referral dashboard.",
                "bi-people",
                "tone-emerald",
                "/referralrewards/dashbords",
                "Open Rewards"
        ));
        actions.add(actionCard(
                "Cash Out Requests",
                cashOutRequestCount + " submitted",
                "Monitor previous requests and submit the next payout when your wallet is ready.",
                "bi-cash-coin",
                "tone-slate",
                "/cashoutcustomerrequest/list",
                "View Requests"
        ));
        return actions;
    }

    private Map<String, Object> actionCard(String title,
            String value,
            String copy,
            String icon,
            String tone,
            String href,
            String ctaLabel) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("copy", copy);
        card.put("icon", icon);
        card.put("tone", tone);
        card.put("href", href);
        card.put("ctaLabel", ctaLabel);
        return card;
    }

    private String barcodeDataUri(String value, BarcodeFormat format, int width, int height) {
        try {
            return barcodeService.generateBarcodeBase64(value, format, width, height);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<Map<String, Object>> buildRecentOrders(List<SalesOrder> orders) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int limit = Math.min(orders.size(), 5);
        for (int index = 0; index < limit; index++) {
            SalesOrder order = orders.get(index);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderCode", order.getOrderCode() != null ? order.getOrderCode() : "Order #" + order.getId());
            row.put("createdLabel", formatDateTime(order.getCreated()));
            row.put("statusLabel", formatEnumLabel(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN"));
            row.put("statusTone", orderTone(order.getStatus()));
            row.put("totalLabel", formatMoney(order.getGrandTotal()));
            row.put("paymentStateLabel", formatEnumLabel(order.getPaymentState() != null ? order.getPaymentState().name() : "UNPAID"));
            row.put("paymentTone", paymentTone(order.getPaymentState()));
            row.put("detailsHref", "/customerorder/details/" + order.getId());
            row.put("paymentHref", "/customerorder/payment/" + order.getId());
            row.put("canPay", hasOutstandingPayment(order));
            rows.add(row);
        }
        return rows;
    }

    private void buildMonthlyTrend(List<SalesOrder> orders,
            List<String> monthlyLabels,
            List<BigDecimal> monthlySpendValues,
            List<Long> monthlyOrderCounts) {
        LinkedHashMap<YearMonth, BigDecimal> spendByMonth = new LinkedHashMap<>();
        LinkedHashMap<YearMonth, Long> countByMonth = new LinkedHashMap<>();
        YearMonth currentMonth = YearMonth.now();

        for (int offset = 5; offset >= 0; offset--) {
            YearMonth bucket = currentMonth.minusMonths(offset);
            spendByMonth.put(bucket, BigDecimal.ZERO);
            countByMonth.put(bucket, 0L);
        }

        for (SalesOrder order : orders) {
            if (order.getCreated() == null) {
                continue;
            }
            YearMonth bucket = YearMonth.from(order.getCreated());
            if (!spendByMonth.containsKey(bucket)) {
                continue;
            }
            countByMonth.put(bucket, countByMonth.get(bucket) + 1);
            if (order.getStatus() != OrderStatus.CANCELLED) {
                spendByMonth.put(bucket, spendByMonth.get(bucket).add(defaultAmount(order.getGrandTotal())));
            }
        }

        for (Map.Entry<YearMonth, BigDecimal> entry : spendByMonth.entrySet()) {
            monthlyLabels.add(entry.getKey().format(MONTH_LABEL));
            monthlySpendValues.add(entry.getValue());
            monthlyOrderCounts.add(countByMonth.get(entry.getKey()));
        }
    }

    private void buildOrderStatusBreakdown(List<SalesOrder> orders,
            List<String> orderStatusLabels,
            List<Long> orderStatusCounts) {
        EnumMap<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }

        for (SalesOrder order : orders) {
            if (order.getStatus() != null) {
                counts.put(order.getStatus(), counts.get(order.getStatus()) + 1);
            }
        }

        for (Map.Entry<OrderStatus, Long> entry : counts.entrySet()) {
            if (entry.getValue() > 0) {
                orderStatusLabels.add(formatEnumLabel(entry.getKey().name()));
                orderStatusCounts.add(entry.getValue());
            }
        }
    }

    private int calculateProfileCompletion(Users user, Profile profile, BillingAddress billingAddress, ProfileImage profileImage) {
        int completed = 0;
        int total = 8;

        if (hasText(user.getFirstName()) || hasText(profile != null ? profile.getName() : null)) {
            completed++;
        }
        if (hasText(user.getEmail())) {
            completed++;
        }
        if (hasText(user.getMobile())) {
            completed++;
        }
        if (profile != null && hasText(profile.getPresentAddress())) {
            completed++;
        }
        if (profile != null && hasText(profile.getPresentCity())) {
            completed++;
        }
        if (billingAddress != null && hasText(billingAddress.getAddressLineOne())) {
            completed++;
        }
        if (billingAddress != null && hasText(billingAddress.getDistrict())) {
            completed++;
        }
        if (profileImage != null && hasText(profileImage.getImageName())) {
            completed++;
        }

        return (completed * 100) / total;
    }

    private String resolveCustomerName(Users user, Profile profile, String fallback) {
        if (profile != null && hasText(profile.getName())) {
            return profile.getName();
        }

        String firstName = hasText(user.getFirstName()) ? user.getFirstName().trim() : "";
        String lastName = hasText(user.getLastName()) ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (hasText(fullName)) {
            return fullName;
        }

        return hasText(fallback) ? fallback : "Customer";
    }

    private String buildInitials(String value) {
        if (!hasText(value)) {
            return "CU";
        }

        String[] parts = value.trim().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase(Locale.ENGLISH);
        String second = parts.length > 1
                ? parts[parts.length - 1].substring(0, 1).toUpperCase(Locale.ENGLISH)
                : "";
        return (first + second).trim();
    }

    private String buildStoreSummary(int totalStores, long activeStores, long pendingStores) {
        if (totalStores == 0) {
            return "No vendor store is connected to this customer account yet.";
        }
        return activeStores + " active store(s), " + pendingStores + " pending approval, " + totalStores + " total.";
    }

    private String buildSupportSummary(Profile profile, BillingAddress billingAddress, ProfileImage profileImage) {
        List<String> checklist = new ArrayList<>();
        checklist.add(profile != null ? "profile on file" : "profile setup pending");
        checklist.add(billingAddress != null ? "billing address saved" : "billing address missing");
        checklist.add(profileImage != null && hasText(profileImage.getImageName()) ? "photo uploaded" : "photo upload pending");
        return String.join(" | ", checklist);
    }

    private String buildCashOutSummary(List<CashOutRequest> cashOutRequests) {
        if (cashOutRequests == null || cashOutRequests.isEmpty()) {
            return "No cash-out request has been submitted yet.";
        }

        CashOutRequest latestRequest = cashOutRequests.get(0);
        return "Latest request: " + formatMoney(latestRequest.getAmount())
                + " | " + formatEnumLabel(latestRequest.getStatus().name())
                + " | " + formatDateTime(latestRequest.getRequestedAt());
    }

    private boolean isOpenStatus(OrderStatus status) {
        return status == OrderStatus.NEW_ORDER
                || status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.IN_TRANSIT
                || status == OrderStatus.OUT_FOR_DELIVERY;
    }

    private boolean hasOutstandingPayment(SalesOrder order) {
        if (order == null || order.getPaymentState() == null) {
            return false;
        }
        return order.getPaymentState() == OrderPaymentState.UNPAID
                || order.getPaymentState() == OrderPaymentState.ADVANCE_PENDING
                || order.getPaymentState() == OrderPaymentState.COD_PENDING
                || order.getPaymentState() == OrderPaymentState.PARTIALLY_PAID
                || order.getPaymentState() == OrderPaymentState.EMI_PENDING;
    }

    private String summarizeBillingAddress(BillingAddress billingAddress) {
        if (billingAddress == null) {
            return "No billing address saved yet. Add one during checkout for faster repeat purchases.";
        }

        List<String> parts = new ArrayList<>();
        if (hasText(billingAddress.getAddressLineOne())) {
            parts.add(billingAddress.getAddressLineOne().trim());
        }
        if (hasText(billingAddress.getCity())) {
            parts.add(billingAddress.getCity().trim());
        }
        if (hasText(billingAddress.getDistrict())) {
            parts.add(billingAddress.getDistrict().trim());
        }
        if (hasText(billingAddress.getCountry())) {
            parts.add(billingAddress.getCountry().trim());
        }

        return parts.isEmpty()
                ? "A billing address exists, but the visible summary is incomplete."
                : String.join(", ", parts);
    }

    private String formatMoney(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        return "Tk " + numberFormat.format(defaultAmount(amount));
    }

    private String formatDate(java.util.Date value) {
        if (value == null) {
            return "Not captured yet";
        }
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_LABEL);
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        if (value == null) {
            return "No timestamp";
        }
        return value.format(DATE_TIME_LABEL);
    }

    private String formatDateTime(java.time.LocalDateTime value, String fallback) {
        return value == null ? fallback : value.format(DATE_TIME_LABEL);
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatEnumLabel(String value) {
        if (value == null) {
            return "Unknown";
        }

        String[] parts = value.toLowerCase(Locale.ENGLISH).split("_");
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            if (!hasText(part)) {
                continue;
            }
            words.add(part.substring(0, 1).toUpperCase(Locale.ENGLISH) + part.substring(1));
        }
        return words.isEmpty() ? "Unknown" : String.join(" ", words);
    }

    private String orderTone(OrderStatus status) {
        if (status == null) {
            return "tone-slate";
        }
        if (FULFILLED_STATUSES.contains(status)) {
            return "tone-emerald";
        }
        if (RETURN_RELATED_STATUSES.contains(status) || status == OrderStatus.CANCELLED) {
            return "tone-rose";
        }
        if (status == OrderStatus.PENDING || status == OrderStatus.NEW_ORDER) {
            return "tone-amber";
        }
        return "tone-navy";
    }

    private String paymentTone(OrderPaymentState paymentState) {
        if (paymentState == null) {
            return "tone-slate";
        }
        if (paymentState == OrderPaymentState.PAID) {
            return "tone-emerald";
        }
        if (paymentState == OrderPaymentState.CANCELLED || paymentState == OrderPaymentState.REFUNDED) {
            return "tone-slate";
        }
        if (paymentState == OrderPaymentState.PARTIALLY_PAID
                || paymentState == OrderPaymentState.ADVANCE_PENDING
                || paymentState == OrderPaymentState.COD_PENDING) {
            return "tone-amber";
        }
        return "tone-rose";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
