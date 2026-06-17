package com.ecommerce.app.module.wishlist.controller;

import com.ecommerce.app.module.wishlist.service.WishlistService;
import com.ecommerce.app.product.ripository.ProductRepository;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/", "/index"})
    public String index(Model model, Principal principal) {
        model.addAttribute("wishlistItems", wishlistService.getWishlistItems(principal));
        return "customer/wishlist/index";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam(name = "product_uuid", required = false) String productUuid,
            @RequestParam(name = "product_id", required = false) Long productId,
            @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean added = wishlistService.addProduct(principal, resolveProductUuid(productUuid, productId));
            if (added) {
                redirectAttributes.addFlashAttribute("successMessage", "Product added to wishlist.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Product is already in your wishlist.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + sanitizeRedirectUrl(redirectUrl);
    }

    @PostMapping("/remove")
    public String remove(
            @RequestParam(name = "product_uuid", required = false) String productUuid,
            @RequestParam(name = "product_id", required = false) Long productId,
            @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean removed = wishlistService.removeProduct(principal, resolveProductUuid(productUuid, productId));
            if (removed) {
                redirectAttributes.addFlashAttribute("successMessage", "Product removed from wishlist.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Product was not in your wishlist.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + sanitizeRedirectUrl(redirectUrl);
    }

    private String resolveProductUuid(String productUuid, Long productId) {
        if (productUuid != null && !productUuid.isBlank()) {
            return productUuid.trim();
        }
        if (productId != null) {
            return productRepository.findById(productId)
                    .map(product -> product.getUuid())
                    .orElse(null);
        }
        return null;
    }

    private String sanitizeRedirectUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return "/wishlist/index";
        }
        if (!redirectUrl.startsWith("/") || redirectUrl.startsWith("//")) {
            return "/wishlist/index";
        }
        return redirectUrl;
    }
}
