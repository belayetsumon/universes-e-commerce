/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.globalComponant.SlagGenerator;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.ProductStatusEnum;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.services.ProductImageStorageService;
import com.ecommerce.app.product.services.ProductcategoryService;
import com.ecommerce.app.services.StorageProperties;
import jakarta.validation.Valid;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/productcategory")
//@PreAuthorize("hasAuthority('productcategory')")
public class ProductcategoryController {

    @Autowired
    StorageProperties properties;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private SlagGenerator slagGenerator;

    @Autowired
    ProductcategoryService productcategoryService;

    @Autowired
    ProductImageStorageService productImageStorageService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(
            Model model,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String hierarchy,
            @RequestParam(required = false) String image,
            @RequestParam(required = false) String featured,
            @RequestParam(defaultValue = "0") int page) {
        int pageNumber = Math.max(page, 0);
        ProductStatusEnum selectedStatus = productcategoryService.resolveStatus(status);
        Page<Productcategory> categoryPage = productcategoryService.findCategories(
                q,
                selectedStatus,
                hierarchy,
                image,
                featured,
                PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "id")));

        Map<Long, Long> productCountsByCategoryId = categoryPage.getContent().isEmpty()
                ? Collections.emptyMap()
                : productRepository.countProductsByCategoryIds(categoryPage.getContent().stream()
                        .map(Productcategory::getId)
                        .collect(Collectors.toList()))
                        .stream()
                        .collect(Collectors.toMap(
                                count -> (Long) count[0],
                                count -> ((Number) count[1]).longValue(),
                                Long::sum));

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("productCountsByCategoryId", productCountsByCategoryId);
        model.addAttribute("page", categoryPage);
        model.addAttribute("statuses", ProductStatusEnum.values());
        model.addAttribute("categoryTotal", productcategoryService.countAllCategories());
        model.addAttribute("activeCategoryCount", productcategoryService.countActiveCategories());
        model.addAttribute("rootCategoryCount", productcategoryService.countRootCategories());
        model.addAttribute("featuredCategoryCount", productcategoryService.countFeaturedCategories());
        model.addAttribute("selectedQ", q);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("selectedHierarchy", hierarchy);
        model.addAttribute("selectedImage", image);
        model.addAttribute("selectedFeatured", featured);

        return "product/productcategory/index";
    }

    // Recursive function to generate all paths
    // private void generatePaths(Productcategory node, String currentPath, List<String> paths) {
    //     // Add current node to the path
    //     String path = currentPath.isEmpty() ? node.getName() : currentPath + " -> " + node.getName();
    //     paths.add(path);
    //     // If the node has children, recursively generate paths for them
    //     if (node.getChildren() != null) {
    //         for (Productcategory child : node.getChildren()) {
    //             generatePaths(child, path, paths);
    //         }
    //     }
    // }
    @RequestMapping("/create")
    public String create(Model model, Productcategory productcategory) {

        model.addAttribute("statuslist", ProductStatusEnum.values());

        model.addAttribute("statuslist", ProductStatusEnum.values());

        model.addAttribute("statuslist", ProductStatusEnum.values());

        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));

        List<Productcategory> rootCategories = productcategoryRepository.findByParentIsNull();
        model.addAttribute("categories", rootCategories);

        return "product/productcategory/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Productcategory productcategory, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("statuslist", ProductStatusEnum.values());

            model.addAttribute("statuslist", ProductStatusEnum.values());

            model.addAttribute("statuslist", ProductStatusEnum.values());

            model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
            return "product/productcategory/add";
        }

        if (!pic.isEmpty()) {
            try {
                String filename = productImageStorageService.storeCategoryImage(pic);
                model.addAttribute("message", "You successfully uploaded");

                productcategory.setImageName(filename);

                if (productcategory.getId() == null) {
                    String slug = slagGenerator.generateSlug(productcategory.getName());
                    productcategory.setSlug(slug);
                }
                productcategoryRepository.save(productcategory);
                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/productcategory/index";
            } catch (Exception e) {

                model.addAttribute("statuslist", Status.values());
                redirectAttributes.addFlashAttribute("message", "Image upload failed: " + e.getMessage());
                return "redirect:/productcategory/index";
            }
        } else if (pic.isEmpty() && productcategory.getId() != null) {
            Productcategory productcategorys = productcategoryRepository.findById(productcategory.getId()).orElse(null);
            productcategory.setImageName(productcategorys.getImageName());
            productcategoryRepository.save(productcategory);
            redirectAttributes.addFlashAttribute("message", "Successfully saved.");
            return "redirect:/productcategory/index";

        } else {
            if (productcategory.getId() == null) {
                String slug = slagGenerator.generateSlug(productcategory.getName());
                productcategory.setSlug(slug);
            }
            productcategoryRepository.save(productcategory);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/productcategory/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Productcategory productcategory) {
        model.addAttribute("productcategory_details", productcategoryRepository.findById(id).orElse(null));
        return "product/productcategory/productcategory_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Productcategory productcategory) {

        model.addAttribute("productcategory", productcategoryRepository.findById(id).orElse(null));
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("statuslist", ProductStatusEnum.values());

        model.addAttribute("productcategory", productcategoryRepository.findById(id).orElse(null));
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("statuslist", ProductStatusEnum.values());

        return "product/productcategory/add";
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Productcategory productcategory, RedirectAttributes redirectAttributes) {
        productcategory = productcategoryRepository.findById(id).orElse(null);
        File file = new File(properties.getRootPath() + File.separator + productcategory.getImageName());
        file.delete();
        productcategoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");
        return "redirect:/productcategory/index";
    }

    public List<Productcategory> getAllParents(Productcategory category) {
        List<Productcategory> parents = new ArrayList<>();
        Productcategory currentCategory = category;

        // Traverse up the parent hierarchy
        while (currentCategory != null && currentCategory.getParent() != null) {
            currentCategory = currentCategory.getParent(); // Move to the parent category
            parents.add(currentCategory); // Add the parent to the list
        }

        return parents;
    }

}
