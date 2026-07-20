package com.ecommerce.app.module.review.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.review.services.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer-product-review")
public class CustomerProductReviewController {

    @Autowired
    private LoggedUserService loggedUserService;

    @Autowired
    private ProductReviewService productReviewService;

    @PostMapping("/save")
    public String saveReview(
            @RequestParam(name = "orderId", required = false) Long orderId,
            @RequestParam(name = "productUuid", required = false) String productUuid,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "reviewText", required = false) String reviewText,
            @RequestParam(name = "source", defaultValue = "order") String source,
            RedirectAttributes redirectAttributes
    ) {

        try {
            if (productUuid != null && !productUuid.isBlank()) {
                productReviewService.saveCustomerReview(
                        loggedUserService.activeUserid(),
                        orderId,
                        productUuid,
                        rating,
                        title,
                        reviewText
                );
            } else {
                productReviewService.saveCustomerReview(
                        loggedUserService.activeUserid(),
                        orderId,
                        productId,
                        rating,
                        title,
                        reviewText
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Your review has been saved successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if ("product".equalsIgnoreCase(source)) {
            if (productUuid != null && !productUuid.isBlank()) {
                return "redirect:/public/single-product/" + productUuid;
            }
            if (productId != null) {
                return "redirect:/public/single-product/" + productId;
            }
        }

        return "redirect:/customerorder/details/" + orderId;
    }
}
