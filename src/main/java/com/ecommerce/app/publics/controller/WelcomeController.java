package com.ecommerce.app.publics.controller;

import com.ecommerce.app.module.ads.model.Placement;
import com.ecommerce.app.module.ads.services.AdsService;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.services.ProductService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
public class WelcomeController {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductService productService;

    @Autowired
    AdsService adsService;
//
//    @RequestMapping({"/index", "/index.html"})
//    public String index() {
//        return "redirect:/";
//    }

    @RequestMapping("/")
    public String welcome(Model model) {
        List<Map<String, Object>> banner = adsService.findAllAdsAsMap(Placement.HOME_BANNER);
        List<Map<String, Object>> featuredProducts = productService.all_Product_front_view(
                null, null, null, null, Boolean.TRUE, null, null, 10
        );
        List<Map<String, Object>> newArrivalProducts = productService.all_Product_front_view(
                null, null, null, Boolean.TRUE, null, null, null, 10
        );
        List<Map<String, Object>> latestProducts = productService.all_Product_front_view(
                null, null, null, null, null, null, null, 10
        );
        List<Map<String, Object>> dealCandidates = productService.all_Product_front_view(
                null, null, null, null, null, null, Boolean.TRUE, 30
        );
        List<Map<String, Object>> dealProducts = dealCandidates.stream()
                .filter(this::hasDiscount)
                .sorted(Comparator.comparing(this::discountPercentOrZero).reversed())
                .limit(10)
                .toList();

        model.addAttribute("banner", banner);
        model.addAttribute("featureCatList", productcategoryRepository.findByStatusAndFeaturedCat(ProductStatusEnum.Active, Boolean.TRUE));
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("newArrivalProducts", newArrivalProducts);
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("dealProducts", dealProducts);
        model.addAttribute("pageTitle", "Musapir | Online Shopping");
        model.addAttribute("pageDescription", "Shop featured products, new arrivals, and top deals from Musapir.");
        return "welcome/welcome";
    }

    private List<Map<String, Object>> limit(List<Map<String, Object>> products, int maxItems) {
        if (products == null) {
            return List.of();
        }
        return products.stream().limit(maxItems).toList();
    }

    private boolean hasDiscount(Map<String, Object> product) {
        return discountPercentOrZero(product).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal discountPercentOrZero(Map<String, Object> product) {
        Object value = product == null ? null : product.get("totalDiscountPercent");
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
    }

    @GetMapping("/maintenance")
    public String maintenancePage() {
        return "maintenance/maintenance";
    }

}
