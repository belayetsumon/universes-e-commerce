/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Unitofmeasurement;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.services.ProductService;
import jakarta.servlet.http.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

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
    ProductService productService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, HttpSession session) {

        double subtotal2 = 0.00;
        if (session.getAttribute("sessioncart") != null) {

            List<CartItem> cartitem = (List<CartItem>) session.getAttribute("sessioncart");

            model.addAttribute("subtotal", subtotal(cartitem));

        } else {

            model.addAttribute("subtotal", subtotal2);
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
    public String addItem(Model model,
            @RequestParam(value = "product_id", required = false) String pid,
            @RequestParam(value = "quantity", required = false, defaultValue = "0") BigDecimal quantity,
            HttpSession session) {

        Long id = Long.valueOf(pid);
        Product product = productRepository.findById(id).orElse(null);
        if (product == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/cart/index";
        }

        BigDecimal salesPrice = product.getSalesPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal subTotal = salesPrice.multiply(quantity);

        BigDecimal discountRate
                = productService.totalDiscountPercentCalculate(product.getVendordiscount(), product.getMarketPlaceDiscount()
                ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDiscountAmount = subTotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountItemTotal = subTotal.subtract(totalDiscountAmount);

        BigDecimal commissionRate = product.getMarketPlaceCommissionRate() != null
                ? product.getMarketPlaceCommissionRate()
                : BigDecimal.ZERO;
        BigDecimal marketPlaceCommissionAmount = afterDiscountItemTotal.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal vendorAmount = afterDiscountItemTotal.subtract(marketPlaceCommissionAmount);

        BigDecimal vatRate = product.getVatRate() != null
                ? product.getVatRate()
                : BigDecimal.ZERO;
        BigDecimal totalVatAmount = afterDiscountItemTotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal itemTotal = afterDiscountItemTotal.add(totalVatAmount);

        Unitofmeasurement uom = product.getUom();

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("sessioncart", cart);
        }

        int index = this.exists(id.intValue(), cart);
        if (index == -1) {
            cart.add(new CartItem(
                    product,
                    product.getVendorprofile().getId(),
                    product.getId(),
                    quantity,
                    uom,
                    salesPrice,
                    discountRate,
                    totalDiscountAmount,
                    commissionRate,
                    marketPlaceCommissionAmount,
                    vendorAmount,
                    vatRate,
                    totalVatAmount,
                    itemTotal
            ));
        } else {
            CartItem existingItem = cart.get(index);
            BigDecimal newQuantity = existingItem.getQuantity().add(quantity).setScale(2, RoundingMode.HALF_UP);

            BigDecimal newSubTotal = salesPrice.multiply(newQuantity);
            BigDecimal newTotalDiscount = newSubTotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newAfterDiscountTotal = newSubTotal.subtract(newTotalDiscount);
            BigDecimal newTotalCommission = newAfterDiscountTotal.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newVendorAmount = newAfterDiscountTotal.subtract(newTotalCommission);
            BigDecimal newVat = newAfterDiscountTotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal newItemTotal = newAfterDiscountTotal.add(newVat);

            existingItem.setQuantity(newQuantity);
            existingItem.setDiscountAmount(newTotalDiscount);
            existingItem.setMarketPlaceCommissionAmount(newTotalCommission);
            existingItem.setVendorAmount(newVendorAmount);
            existingItem.setVatAmount(newVat);
            existingItem.setItemTotal(newItemTotal);
        }

        session.setAttribute("sessioncart", cart);
        return "redirect:/cart/index";
    }

    @RequestMapping("/quantityUpdate")
    public String quantityUpdate(Model model,
            @RequestParam(value = "product_id", required = false) String pid,
            @RequestParam(value = "quantity", required = false, defaultValue = "0") BigDecimal newQuantity,
            HttpSession session) {

        if (pid == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/cart/index";
        }

        Long id = Long.valueOf(pid);
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/cart/index";
        }

        BigDecimal salesPrice = product.getSalesPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountRate
                = productService.totalDiscountPercentCalculate(product.getVendordiscount(), product.getMarketPlaceDiscount()
                ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subTotal = salesPrice.multiply(newQuantity);
        BigDecimal totalDiscount = subTotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountTotal = subTotal.subtract(totalDiscount);

        BigDecimal commissionRate = product.getMarketPlaceCommissionRate() != null
                ? product.getMarketPlaceCommissionRate()
                : BigDecimal.ZERO;
        BigDecimal totalCommission = afterDiscountTotal.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal vendorAmount = afterDiscountTotal.subtract(totalCommission);

        BigDecimal vatRate = product.getVatRate() != null
                ? product.getVatRate()
                : BigDecimal.ZERO;
        BigDecimal totalVat = afterDiscountTotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal itemTotal = afterDiscountTotal.add(totalVat);

        if (session.getAttribute("sessioncart") != null) {
            List<CartItem> shoppingCart = (List<CartItem>) session.getAttribute("sessioncart");
            int index = this.exists(id.intValue(), shoppingCart);

            if (index != -1) {
                CartItem existingItem = shoppingCart.get(index);

                existingItem.setQuantity(newQuantity);
                existingItem.setSalesPrice(salesPrice);
                existingItem.setDiscountRate(discountRate);
                existingItem.setDiscountAmount(totalDiscount);
                existingItem.setMarketPlaceCommissionRate(commissionRate);
                existingItem.setMarketPlaceCommissionAmount(totalCommission);
                existingItem.setVendorAmount(vendorAmount);
                existingItem.setVatRate(vatRate);
                existingItem.setVatAmount(totalVat);
                existingItem.setItemTotal(itemTotal);
            }

            session.setAttribute("sessioncart", shoppingCart);
        }

        return "redirect:/cart/index";
    }

    @RequestMapping(value = "remove/{id}", method = RequestMethod.GET)
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
