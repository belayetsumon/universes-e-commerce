package com.ecommerce.app.publics.controller;

import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @RequestMapping("/")
    public String welcome(Model model) {

        model.addAttribute("featureCatList", productcategoryRepository.findByStatusAndFeaturedCat(ProductStatusEnum.Active, Boolean.TRUE));

        model.addAttribute("productlist", productService.all_Product_front_view(null, null, null, null, null, null));
        return "welcome/welcome";
    }

}
