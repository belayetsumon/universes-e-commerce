/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.controller;

import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.cart.services.CartService;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.shipping.services.ShippingChargeService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.BillingAddress;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.model.ShippingAddress;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.SalesOrderCodeGeneratorService;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PostMapping;
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
    ReferralService referralService;

    @Autowired
    SalesOrderCodeGeneratorService salesOrderCodeGeneratorService;

    @Autowired
    ShippingChargeService shippingChargeService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("orderlist", salesOrderRepository.findAll());
        return "order/order/index";
    }

    @RequestMapping(value = {"create"})
    public String create(Model model, HttpSession session) {

        model.addAttribute("subtotal", cartService.subtotal());

        // Billing Address Handling
        BillingAddress billingAddress = new BillingAddress();

        if (session.getAttribute("session_Billing_address") == null) {
            Optional<BillingAddress> optionalBillingAddress
                    = billingAddressRepository.findByUserId_Id(loggedUserService.activeUserid());

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

        if (session.getAttribute("session_Shipping_address") != null) {
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

        if (session.getAttribute("session_Billing_address") == null || session.getAttribute("session_Shipping_address") == null) {
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

        ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
        shippingAddress.setOrder(order);
        shippingAddressRepository.save(shippingAddress);

        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        return "redirect:/customerorder/index";
    }

    @RequestMapping(value = {"/savebyvendor"})
    @Transactional
    public String savebyvendor(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("session_Billing_address") == null || session.getAttribute("session_Shipping_address") == null) {
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

        // Group cart items by vendor (after loading cart)
        Map<Long, List<CartItem>> itemsByVendors = cartitem.stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        // Prepare customer
        Users customer = new Users();
        customer.setId(loggedUserService.activeUserid());

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
            ShippingAddress shippingAddress = (ShippingAddress) session.getAttribute("session_Shipping_address");
            ShippingAddress newShipping = new ShippingAddress();
            newShipping.copyFrom(shippingAddress); // implement a method to deep copy or map fields
            newShipping.setOrder(salesOrder);
            salesOrder.setShippingAddress(newShipping); // make sure @OneToOne(cascade = CascadeType.ALL)

            salesOrder.setOrderCode(salesOrderCodeGeneratorService.generateNextDailyOrderCode());
            salesOrder = salesOrderRepository.save(salesOrder); // cascade saves shipping

            BigDecimal grandTotal = BigDecimal.ZERO;

            BigDecimal totalmarketPlaceCommissionAmount = BigDecimal.ZERO;

            for (CartItem cartItem : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSalesOrder(salesOrder);
                orderItem.setProductid(cartItem.getProductId());
                orderItem.setVendorId(cartItem.getVendorId());
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
            // 10% for refarel
            BigDecimal commission = totalmarketPlaceCommissionAmount.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);
            referralService.distributeCommission(customer, commission);

            orders.add(salesOrder);
        }

        // Save billing address only if it's new (null ID)
        BillingAddress billingAddress = (BillingAddress) session.getAttribute("session_Billing_address");

        if (billingAddress.getId() == null) {
            billingAddress.setUserId(customer);
            billingAddressRepository.save(billingAddress);
        }

        // Clean session
        session.removeAttribute("sessioncart");
        session.removeAttribute("session_Shipping_address");
        session.removeAttribute("session_Billing_address");

        return "redirect:/customerorder/index";
    }

    @PostMapping("/calculate-charge")
    public String calculateCharge(@RequestParam("zoneType") String zoneType,
            @RequestParam("weightKg") BigDecimal weight,
            Model model) {
        BigDecimal charge = shippingChargeService.calculateCharge(zoneType, weight);
        model.addAttribute("charge", charge);
        return "shipping/charge-summary :: chargeFragment";
    }

}
