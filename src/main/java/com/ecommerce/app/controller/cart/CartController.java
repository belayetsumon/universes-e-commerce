/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.controller.cart;

import com.ecommerce.app.model.cart.CartItem;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.ProductRepository;
import jakarta.servlet.http.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    private double subtotal(List< CartItem> cartitem) {

//        cartitem = new ArrayList<>();
        double subtotal = 0.00;

        if (!cartitem.isEmpty()) {

            for (int i = 0; i < cartitem.size(); i++) {

                subtotal += cartitem.get(i).getProduct().getPrice() * cartitem.get(i).getQuantity();
            }
            return subtotal;
        }
        return subtotal;
    }

    @RequestMapping("/add")
    public String addItem2(Model model,
            @RequestParam(value = "product_id", required = true) String pid,
            @RequestParam(value = "quantity", required = true) String quant,
            HttpSession session) {

        Product product;
        // convert product id int to long
        Long id = Long.valueOf(pid);
        int quantity = Integer.parseInt(quant);

        // System.out.println("id" + id + "quentity" + quant);
        if (session.getAttribute("sessioncart") == null) {

            List<CartItem> shoppingcart_list = new ArrayList<CartItem>();

            product = productRepository.getOne(id);

            shoppingcart_list.add(new CartItem(product, quantity));

            session.setAttribute("sessioncart", shoppingcart_list);
            return "redirect:/cart/index";
        } else {

            List<CartItem> shoppingcart_list = (List<CartItem>) session.getAttribute("sessioncart");
            int id2 = id.intValue();
            int index = this.exists(id2, shoppingcart_list);

            //System.out.println("indexxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + index);
            if (index == -1) {
                product= productRepository.getOne(id);
                shoppingcart_list.add(new CartItem(product, quantity));
            } else {
                int quantity2 = shoppingcart_list.get(index).getQuantity() + quantity;
                shoppingcart_list.get(index).setQuantity(quantity2);

            }
            session.setAttribute("sessioncart", shoppingcart_list);

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
