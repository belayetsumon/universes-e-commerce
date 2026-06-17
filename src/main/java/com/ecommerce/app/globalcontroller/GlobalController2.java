/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.globalcontroller;

import com.ecommerce.app.globalComponant.EntityNameResolver;
import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.wishlist.service.WishlistService;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 *
 * @author User
 */
@ControllerAdvice
public class GlobalController2 {

    @Autowired
    private EntityNameResolver entityNameResolver;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    WishlistService wishlistService;

    @ModelAttribute
    public void addAttributes(Model model) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if(!auth==null){
//        Users users = usersRepository.findByEmail(auth.getName());
//
//        model.addAttribute("username", users.getName());
    }

    @ModelAttribute
    public void addWishlistAttributes(Model model, Principal principal) {
        Set<String> wishlistProductUuids = wishlistService.getWishlistProductUuids(principal);
        Set<String> wishlistProductKeys = wishlistProductUuids.stream()
                .filter(uuid -> uuid != null && !uuid.isBlank())
                .collect(Collectors.toSet());
        model.addAttribute("wishlistProductIds", wishlistProductUuids);
        model.addAttribute("wishlistProductUuids", wishlistProductUuids);
        model.addAttribute("wishlistProductKeys", wishlistProductKeys);
        model.addAttribute("wishlistCount", wishlistProductUuids.size());
    }

//    @ModelAttribute
//    public void shopingcart(Model model, HttpSession session) {
//
//        if (session.getAttribute("sessioncart") != null) {
//
//            List<CartItem> cartitem = (List<CartItem>) session.getAttribute("sessioncart");
//
//            model.addAttribute("totaltest", cartitem.size());
//
//        } else {
//
//            model.addAttribute("totaltest", "0");
//        }
//
//    }
//
    @ModelAttribute("maincategory")
    public List<Productcategory> populateCategories() {
        return productcategoryRepository.findByStatusAndParentIsNull(ProductStatusEnum.Active);
    }

    // Expose enum values for Thymeleaf dropdown
    @ModelAttribute("shippinglocations")
    public District[] shippingLocations() {
        return District.values();
    }

    @ModelAttribute("currentShippingDistrict")
    public District currentShippingDistrict(HttpSession session) {
        Object obj = session.getAttribute("shippingdistrict");
        if (obj == null) {
            return null;
        }
        if (obj instanceof District) {
            return (District) obj;
        }
        if (obj instanceof String) {
            try {
                return District.valueOf((String) obj);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
