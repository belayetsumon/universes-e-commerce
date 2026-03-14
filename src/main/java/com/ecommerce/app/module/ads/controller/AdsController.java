/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ads.controller;

import com.ecommerce.app.globalComponant.ImageUtils;
import com.ecommerce.app.module.ads.model.Ads;
import com.ecommerce.app.module.ads.model.Placement;
import com.ecommerce.app.module.ads.model.TargetType;
import com.ecommerce.app.module.ads.services.AdsService;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductcategoryService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/ads")
public class AdsController {

    @Autowired
    private AdsService adsService;

    @Autowired
    ProductService productService;

    @Autowired
    VendorprofileService vendorprofileService;
    @Autowired
    ProductcategoryService productcategoryService;

    @Autowired
    ImageUtils imageUtils;

    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("categoryList", productcategoryService.findActiveCategoryDropDown());
        model.addAttribute("productList", productService.product_List_For_Dropdown());
        model.addAttribute("vendorList", vendorprofileService.all_vendor_list_for_Dropdown());
        model.addAttribute("targetTypes", TargetType.values());
        model.addAttribute("placements", Placement.values());
    }

    @GetMapping("/list")
    public String listAds(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String placement,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Ads> adsPage = adsService.getAds();

        model.addAttribute("adsPage", adsPage);

        return "ads/ads_list";
    }

    @GetMapping("/create")
    public String createForm(Model model, Ads ads) {

        return "ads/ads_form";
    }

    @PostMapping("/save")
    public String saveOrUpdate(
            @Valid Ads ads,
            BindingResult bindingResult,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate Target Type
        if (ads.getTargetType() == null) {
            bindingResult.rejectValue("targetType", "error.ads", "Target type is required");
        }
//        else {
//            switch (ads.getTargetType()) {
//                case CATEGORY -> {
//                    if (ads.getCategoryId() == null || ads.getCategoryId().isEmpty()) {
//                        bindingResult.rejectValue("categoryId", "error.ads", "Category must be selected");
//                    }
//                }
//                case PRODUCT -> {
//                    if (ads.getProductId() == null || ads.getProductId().isEmpty()) {
//                        bindingResult.rejectValue("productId", "error.ads", "Product must be selected");
//                    }
//                }
//                case VENDOR -> {
//                    if (ads.getVendorId() == null || ads.getVendorId().isEmpty()) {
//                        bindingResult.rejectValue("vendorId", "error.ads", "Vendor must be selected");
//                    }
//                }
//                case EXTERNAL -> {
//                    if (ads.getExternalUrl() == null || ads.getExternalUrl().isEmpty()) {
//                        bindingResult.rejectValue("externalUrl", "error.ads", "External URL is required");
//                    }
//                }
//            }
//        }

        // Handle Banner Image Upload
        Ads existingAds = null;
        if (ads.getId() != null) {
            existingAds = adsService.getById(ads.getId());
            if (existingAds == null) {
                bindingResult.rejectValue("id", "error.ads", "Ad not found");
            }
        }

        // Handle Banner Image Upload
        if ((existingAds == null) && (file == null || file.isEmpty())) {
            bindingResult.rejectValue("imageUrl", "error.ads", "Banner image is required for new ads");
        } else if (file != null && !file.isEmpty()) {
            try {
                String bannerFileName = imageUtils.saveBannerImage(
                        file,
                        "banner",
                        ads.getWidth(),
                        ads.getHeight()
                );
                ads.setImageUrl(bannerFileName);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "error.ads", "Image upload failed: " + e.getMessage());
            }
        } else if (existingAds != null) {
            // preserve existing image
            ads.setImageUrl(existingAds.getImageUrl());
        }

        // Return form with errors
        if (bindingResult.hasErrors()) {
            return "ads/ads_form";
        }
        // Return form with errors
        if (bindingResult.hasErrors()) {
            return "ads/ads_form";
        }

        adsService.save(ads);
        redirectAttributes.addFlashAttribute("successMessage", "Ad saved successfully!");

        return "redirect:/admin/ads/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Ads ads = adsService.getById(id);
        model.addAttribute("ads", ads);  // Use "ad" to match form th:object

        return "ads/ads_form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        String BASE_FOLDER = Paths.get(System.getProperty("user.home"), "universesecommerce").toString();

        try {
            Ads ads = adsService.getById(id);

            if (ads == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ad not found or already deleted.");
                return "redirect:/admin/ads/list";
            }

            // Delete image file from disk
            if (ads.getImageUrl() != null && !ads.getImageUrl().isEmpty()) {
                try {
                    Path imagePath = Paths.get(BASE_FOLDER, "banner").resolve(ads.getImageUrl());
                    Files.deleteIfExists(imagePath);  // deletes if file exists
                } catch (IOException e) {
                    System.err.println("Failed to delete banner image: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete banner image: " + e.getMessage());
                }
            }

            // Delete ad record from database
            adsService.delete(id);

            redirectAttributes.addFlashAttribute("successMessage", "Ad deleted successfully!");
        } catch (EmptyResultDataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ad not found or already deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete ad. Please try again.");
            ex.printStackTrace();  // For debugging
        }

        return "redirect:/admin/ads/list";
    }

}
