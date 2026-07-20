/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.commission.service.ProductCommissionApplierService;
import com.ecommerce.app.globalComponant.SlagGenerator;
import com.ecommerce.app.globalComponant.UnixTimeComponent;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductImage;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.ProductTypeEnum;
import com.ecommerce.app.product.ripository.AvailableDeliveryAreaRepository;
import com.ecommerce.app.product.ripository.DeliveryChargeRepository;
import com.ecommerce.app.product.ripository.DeliveryTimelineRepository;
import com.ecommerce.app.product.ripository.ManufacturerRepository;
import com.ecommerce.app.product.ripository.ProductImageRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.WarrantyRepository;
import com.ecommerce.app.product.services.CatalogProductAttributeService;
import com.ecommerce.app.product.services.ProductDimensionService;
import com.ecommerce.app.product.services.ProductImageStorageService;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductVideoEmbedService;
import com.ecommerce.app.product.services.ProductVariantCatalogService;
import com.ecommerce.app.product.services.UnitsOfMeasureService;
import com.ecommerce.app.services.StorageProperties;
import jakarta.servlet.http.HttpServletRequest;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/productvendor")
//@PreAuthorize("hasAuthority('exam')")
public class VendorProductController {

    @Autowired
    StorageProperties properties;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductService productService;

    @Autowired
    ProductVideoEmbedService productVideoEmbedService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UnitsOfMeasureService unitsOfMeasureService;

    @Autowired
    private SlagGenerator slagGenerator;

    @Autowired
    UnixTimeComponent unixTimeComponent;

    @Autowired
    ManufacturerRepository manufacturerRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    ProductImageStorageService productImageStorageService;

    @Autowired
    AvailableDeliveryAreaRepository availableDeliveryAreaRepository;

    @Autowired
    DeliveryChargeRepository deliveryChargeRepository;

    @Autowired
    DeliveryTimelineRepository deliveryTimelineRepository;

    @Autowired
    WarrantyRepository warrantyRepository;

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    ProductVariantCatalogService productVariantCatalogService;

    @Autowired
    ProductDimensionService productDimensionService;

    @Autowired
    CatalogProductAttributeService catalogProductAttributeService;

    @Autowired
    ProductCommissionApplierService productCommissionApplierService;

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) ProductStatusEnum status,
            @RequestParam(value = "productType", required = false) ProductTypeEnum productType,
            @RequestParam(value = "onlineShow", required = false) Boolean onlineShow,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "featuredProduct", required = false) Boolean featuredProduct,
            @RequestParam(value = "newProduct", required = false) Boolean newProduct,
            @RequestParam(value = "manageStock", required = false) Boolean manageStock,
            @RequestParam(value = "allowPreorder", required = false) Boolean allowPreorder,
            @RequestParam(value = "manageProductVariants", required = false) Boolean manageProductVariants,
            @RequestParam(value = "createdFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(value = "createdTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(value = "uomId", required = false) Long uomId,
            @RequestParam(value = "minPurchasePrice", required = false) BigDecimal minPurchasePrice,
            @RequestParam(value = "maxPurchasePrice", required = false) BigDecimal maxPurchasePrice,
            @RequestParam(value = "minMarketPlaceDiscount", required = false) BigDecimal minMarketPlaceDiscount,
            @RequestParam(value = "maxMarketPlaceDiscount", required = false) BigDecimal maxMarketPlaceDiscount,
            @RequestParam(value = "minVendorDiscount", required = false) BigDecimal minVendorDiscount,
            @RequestParam(value = "maxVendorDiscount", required = false) BigDecimal maxVendorDiscount,
            @RequestParam(value = "discountStartFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate discountStartFrom,
            @RequestParam(value = "discountStartTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate discountStartTo,
            @RequestParam(value = "discountEndFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate discountEndFrom,
            @RequestParam(value = "discountEndTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate discountEndTo,
            @RequestParam(value = "preorderFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preorderFrom,
            @RequestParam(value = "preorderTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preorderTo,
            @RequestParam(value = "hasSpecifications", required = false) Boolean hasSpecifications,
            @RequestParam(value = "hasImage", required = false) Boolean hasImage,
            @RequestParam(value = "hasCatalogVariants", required = false) Boolean hasCatalogVariants,
            @RequestParam(value = "hasDimensions", required = false) Boolean hasDimensions,
            @RequestParam(value = "hasDeliveryAreas", required = false) Boolean hasDeliveryAreas,
            @RequestParam(value = "hasDeliveryCharges", required = false) Boolean hasDeliveryCharges,
            @RequestParam(value = "hasDeliveryTimelines", required = false) Boolean hasDeliveryTimelines,
            @RequestParam(value = "hasWarranty", required = false) Boolean hasWarranty,
            HttpSession session) {

        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();

        model.addAttribute("productlist", productService.all_Product_for_admin(
                keyword,
                categoryId,
                vendorprofile.getId(),
                status,
                productType,
                onlineShow,
                minPrice,
                maxPrice,
                featuredProduct,
                newProduct,
                manageStock,
                allowPreorder,
                manageProductVariants,
                createdFrom,
                createdTo,
                uomId,
                minPurchasePrice,
                maxPurchasePrice,
                minMarketPlaceDiscount,
                maxMarketPlaceDiscount,
                minVendorDiscount,
                maxVendorDiscount,
                discountStartFrom,
                discountStartTo,
                discountEndFrom,
                discountEndTo,
                preorderFrom,
                preorderTo,
                hasSpecifications,
                hasImage,
                hasCatalogVariants,
                hasDimensions,
                hasDeliveryAreas,
                hasDeliveryCharges,
                hasDeliveryTimelines,
                hasWarranty
        ));
        loadVendorProductIndexFilterData(model);
        model.addAttribute("activeVendor", vendorprofile);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("productType", productType);
        model.addAttribute("onlineShow", onlineShow);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("featuredProduct", featuredProduct);
        model.addAttribute("newProduct", newProduct);
        model.addAttribute("manageStock", manageStock);
        model.addAttribute("allowPreorder", allowPreorder);
        model.addAttribute("manageProductVariants", manageProductVariants);
        model.addAttribute("createdFrom", createdFrom);
        model.addAttribute("createdTo", createdTo);
        model.addAttribute("uomId", uomId);
        model.addAttribute("minPurchasePrice", minPurchasePrice);
        model.addAttribute("maxPurchasePrice", maxPurchasePrice);
        model.addAttribute("minMarketPlaceDiscount", minMarketPlaceDiscount);
        model.addAttribute("maxMarketPlaceDiscount", maxMarketPlaceDiscount);
        model.addAttribute("minVendorDiscount", minVendorDiscount);
        model.addAttribute("maxVendorDiscount", maxVendorDiscount);
        model.addAttribute("discountStartFrom", discountStartFrom);
        model.addAttribute("discountStartTo", discountStartTo);
        model.addAttribute("discountEndFrom", discountEndFrom);
        model.addAttribute("discountEndTo", discountEndTo);
        model.addAttribute("preorderFrom", preorderFrom);
        model.addAttribute("preorderTo", preorderTo);
        model.addAttribute("hasSpecifications", hasSpecifications);
        model.addAttribute("hasImage", hasImage);
        model.addAttribute("hasCatalogVariants", hasCatalogVariants);
        model.addAttribute("hasDimensions", hasDimensions);
        model.addAttribute("hasDeliveryAreas", hasDeliveryAreas);
        model.addAttribute("hasDeliveryCharges", hasDeliveryCharges);
        model.addAttribute("hasDeliveryTimelines", hasDeliveryTimelines);
        model.addAttribute("hasWarranty", hasWarranty);
        model.addAttribute("advancedFiltersApplied", hasAdvancedProductFilters(
                featuredProduct,
                newProduct,
                manageStock,
                allowPreorder,
                manageProductVariants,
                createdFrom,
                createdTo,
                uomId,
                minPurchasePrice,
                maxPurchasePrice,
                minMarketPlaceDiscount,
                maxMarketPlaceDiscount,
                minVendorDiscount,
                maxVendorDiscount,
                discountStartFrom,
                discountStartTo,
                discountEndFrom,
                discountEndTo,
                preorderFrom,
                preorderTo,
                hasSpecifications,
                hasImage,
                hasCatalogVariants,
                hasDimensions,
                hasDeliveryAreas,
                hasDeliveryCharges,
                hasDeliveryTimelines,
                hasWarranty
        ));
        return "vendor/product/index";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/create")
    public String create(Model model, Product product, HttpSession session) {
        int suk = (int) unixTimeComponent.unixTimeEpochSecond();
        product.setSku(suk);

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);
        loadProductFormData(model);
        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        product.setVendorprofile(vendorprofile);
        productCommissionApplierService.prefillCommissionForForm(product);
        return "vendor/product/add";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/save")
    public String create(Model model, @Valid Product product, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam(value = "pic", required = false) MultipartFile pic,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            Users userss = new Users();
            userss.setId(loggedUserService.activeUserid());
            product.setUserId(userss);
            product.setVendorprofile(vendorUserContext.getActiveVendor());
            loadProductFormData(model);

            return "vendor/product/add";
        }

        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        if (vendorprofile == null || vendorprofile.getId() == null) {
            redirectAttributes.addFlashAttribute("message", "Vendor context not found.");
            return "redirect:/productvendor/index";
        }
        product.setVendorprofile(vendorprofile);
        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);
        productCommissionApplierService.applyCommissionBeforeSave(product);

        if (product.getId() != null) {
            Product oldProduct = productRepository.findById(product.getId()).orElse(null);
            if (!isOwnedByVendor(oldProduct, vendorprofile)) {
                redirectAttributes.addFlashAttribute("message", "Product not found for the active vendor.");
                return "redirect:/productvendor/index";
            }
            if (oldProduct != null) {
                product.setUuid(oldProduct.getUuid());
                if (product.getSlug() == null || product.getSlug().isBlank()) {
                    product.setSlug(oldProduct.getSlug());
                }
                if (pic == null || pic.isEmpty()) {
                    product.setImageName(oldProduct.getImageName());
                }
            }
        }

        if (pic != null && !pic.isEmpty()) {
            try {
                String filename = productImageStorageService.storeProductImage(pic);

                model.addAttribute("message", "You successfully uploaded");

                product.setImageName(filename);

                if (product.getId() == null) {
                    String slug = slagGenerator.generateSlug(product.getTitle());
                    product.setSlug(slug);
                }

                Product savedProduct = productRepository.save(product);
                redirectAttributes.addFlashAttribute("message", "Basic product information saved. Now add product specifications.");
                return "redirect:/productvendor/details/" + savedProduct.getId() + "?tab=specifications";
            } catch (Exception e) {
                loadProductFormData(model);

                redirectAttributes.addFlashAttribute("message", "Image upload failed: " + e.getMessage());
                return "redirect:/productvendor/index";
            }
        } else if ((pic == null || pic.isEmpty()) && product.getId() != null) {

//            Product products = productRepository.findById(product.getId()).orElse(null);
//
//            products.setImageName(product.getImageName());
            Product savedProduct = productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", "Basic product information saved. Now add product specifications.");

            return "redirect:/productvendor/details/" + savedProduct.getId() + "?tab=specifications";

        } else {

            if (product.getId() == null) {
                String slug = slagGenerator.generateSlug(product.getTitle());
                product.setSlug(slug);
            }
            Product savedProduct = productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", "Basic product information saved. Now add product specifications.");
            return "redirect:/productvendor/details/" + savedProduct.getId() + "?tab=specifications";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/details/{id}")
    public String create(Model model,
            @PathVariable Long id,
            @RequestParam(value = "tab", required = false) String activeTab,
            Product product,
            ProductImage productImage,
            RedirectAttributes redirectAttributes) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (!isOwnedByActiveVendor(existingProduct)) {
            redirectAttributes.addFlashAttribute("message", "Product not found for the active vendor.");
            return "redirect:/productvendor/index";
        }
        populateProductDetailsModel(model, id, null, normalizeDetailsTab(activeTab));
        return "vendor/product/product_details";

    }

    @PostMapping("/details/{id}/specifications")
    public String saveSpecifications(Model model,
            @PathVariable Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (!isOwnedByActiveVendor(product)) {
            redirectAttributes.addFlashAttribute("message", "Product not found.");
            return "redirect:/productvendor/index";
        }

        String categoryUuid = resolveCategoryUuid(product);
        List<String> attributeErrors = catalogProductAttributeService
                .validateSubmittedValues(categoryUuid, request.getParameterMap());

        if (!attributeErrors.isEmpty()) {
            populateProductDetailsModel(model, id, request.getParameterMap(), "specifications");
            model.addAttribute("specificationErrorMessages", attributeErrors);
            return "vendor/product/product_details";
        }

        try {
            catalogProductAttributeService.replaceProductAttributes(product, categoryUuid, request.getParameterMap());
            redirectAttributes.addFlashAttribute("message", "Product specifications saved successfully.");
            return "redirect:/productvendor/details/" + id + "?tab=specifications";
        } catch (RuntimeException ex) {
            populateProductDetailsModel(model, id, request.getParameterMap(), "specifications");
            model.addAttribute("specificationErrorMessages", List.of(ex.getMessage()));
            return "vendor/product/product_details";
        }
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Product product, RedirectAttributes redirectAttributes) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (!isOwnedByActiveVendor(existingProduct)) {
            redirectAttributes.addFlashAttribute("message", "Product not found for the active vendor.");
            return "redirect:/productvendor/index";
        }
        model.addAttribute("product", existingProduct);
        loadProductFormData(model);
        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        if (existingProduct != null) {
            existingProduct.setUserId(userss);
            existingProduct.setVendorprofile(vendorUserContext.getActiveVendor());
        }

        return "vendor/product/add";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.product.write')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Product product, RedirectAttributes redirectAttributes) {

        product = productRepository.findById(id).orElse(null);
        if (!isOwnedByActiveVendor(product)) {
            redirectAttributes.addFlashAttribute("message", "Product not found for the active vendor.");
            return "redirect:/productvendor/index";
        }
        if (product.getImageName() != null && !product.getImageName().isBlank()) {
            File file = new File(properties.getRootPath() + File.separator + product.getImageName());
            file.delete();
        }

        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/productvendor/index";
    }

    private void loadProductFormData(Model model) {
        model.addAttribute("statuslist", ProductStatusEnum.values());
        model.addAttribute("producttype", ProductTypeEnum.values());
        model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
        model.addAttribute("manufacturerlist", manufacturerRepository.findAll());
    }

    private void loadVendorProductIndexFilterData(Model model) {
        model.addAttribute("statusOptions", ProductStatusEnum.values());
        model.addAttribute("productTypeOptions", ProductTypeEnum.values());
        model.addAttribute("filterCategories", productcategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("filterUoms", unitsOfMeasureService.getAllUnits());
    }

    private boolean hasAdvancedProductFilters(Boolean featuredProduct,
            Boolean newProduct,
            Boolean manageStock,
            Boolean allowPreorder,
            Boolean manageProductVariants,
            LocalDate createdFrom,
            LocalDate createdTo,
            Long uomId,
            BigDecimal minPurchasePrice,
            BigDecimal maxPurchasePrice,
            BigDecimal minMarketPlaceDiscount,
            BigDecimal maxMarketPlaceDiscount,
            BigDecimal minVendorDiscount,
            BigDecimal maxVendorDiscount,
            LocalDate discountStartFrom,
            LocalDate discountStartTo,
            LocalDate discountEndFrom,
            LocalDate discountEndTo,
            LocalDate preorderFrom,
            LocalDate preorderTo,
            Boolean hasSpecifications,
            Boolean hasImage,
            Boolean hasCatalogVariants,
            Boolean hasDimensions,
            Boolean hasDeliveryAreas,
            Boolean hasDeliveryCharges,
            Boolean hasDeliveryTimelines,
            Boolean hasWarranty) {
        return featuredProduct != null
                || newProduct != null
                || manageStock != null
                || allowPreorder != null
                || manageProductVariants != null
                || createdFrom != null
                || createdTo != null
                || uomId != null
                || minPurchasePrice != null
                || maxPurchasePrice != null
                || minMarketPlaceDiscount != null
                || maxMarketPlaceDiscount != null
                || minVendorDiscount != null
                || maxVendorDiscount != null
                || discountStartFrom != null
                || discountStartTo != null
                || discountEndFrom != null
                || discountEndTo != null
                || preorderFrom != null
                || preorderTo != null
                || hasSpecifications != null
                || hasImage != null
                || hasCatalogVariants != null
                || hasDimensions != null
                || hasDeliveryAreas != null
                || hasDeliveryCharges != null
                || hasDeliveryTimelines != null
                || hasWarranty != null;
    }

    private void populateProductDetailsModel(Model model,
            Long id,
            Map<String, String[]> specificationParams,
            String activeTab) {
        Map<String, Object> productDetails = productService.all_Product_for_admin_By_Id(id);
        model.addAttribute("product_details", productDetails);
        model.addAttribute("productVideoEmbedUrl", productVideoEmbedService.toYoutubeEmbedUrl(productDetails.get("video")));
        model.addAttribute("productSpecifications",
                catalogProductAttributeService.buildSpecificationViews((String) productDetails.get("uuid")));
        model.addAttribute("img_list", productImageRepository.findByProductIdOrderByIdDesc(id));
        model.addAttribute("d_a_list", availableDeliveryAreaRepository.findByProductIdOrderByIdDesc(id));
        model.addAttribute("d_c_list", deliveryChargeRepository.findByProductIdOrderByIdDesc(id));
        model.addAttribute("d_t_list", deliveryTimelineRepository.findByProductIdOrderByIdDesc(id));
        model.addAttribute("w_list", warrantyRepository.findByProductIdOrderByIdDesc(id));
        model.addAttribute("catalogVariantSummaries",
                productVariantCatalogService.buildVariantSummaries((String) productDetails.get("uuid")));
        model.addAttribute("hasCatalogVariants",
                productVariantCatalogService.hasCatalogVariants(id));
        model.addAttribute("d_dimension", productDimensionService.findAllById(id));
        model.addAttribute("activeTab", activeTab);
        populateSpecificationFields(
                model,
                (String) productDetails.get("categoryUuid"),
                (String) productDetails.get("uuid"),
                specificationParams
        );
    }

    private void populateSpecificationFields(Model model,
            String categoryUuid,
            String productUuid,
            Map<String, String[]> submittedParams) {
        if (submittedParams != null && !submittedParams.isEmpty()) {
            model.addAttribute("dynamicAttributeFields",
                    catalogProductAttributeService.buildFieldsFromSubmission(categoryUuid, submittedParams));
            return;
        }
        model.addAttribute("dynamicAttributeFields",
                catalogProductAttributeService.buildFieldsForProduct(categoryUuid, productUuid));
    }

    private String normalizeDetailsTab(String activeTab) {
        if (activeTab == null || activeTab.isBlank()) {
            return "home";
        }
        return switch (activeTab) {
            case "home", "specifications", "imageupload", "catalogvariants", "productdimension", "deliveryOptions" ->
                activeTab;
            default ->
                "home";
        };
    }

    private String resolveCategoryUuid(Product product) {
        if (product == null || product.getProductcategory() == null) {
            return null;
        }
        if (product.getProductcategory().getUuid() != null && !product.getProductcategory().getUuid().isBlank()) {
            return product.getProductcategory().getUuid();
        }
        if (product.getProductcategory().getId() == null) {
            return null;
        }
        return productcategoryRepository.findById(product.getProductcategory().getId())
                .map(existingCategory -> existingCategory.getUuid())
                .orElse(null);
    }

    private boolean isOwnedByActiveVendor(Product product) {
        return isOwnedByVendor(product, vendorUserContext.getActiveVendor());
    }

    private boolean isOwnedByVendor(Product product, Vendorprofile vendorprofile) {
        return product != null
                && vendorprofile != null
                && vendorprofile.getId() != null
                && product.getVendorprofile() != null
                && Objects.equals(product.getVendorprofile().getId(), vendorprofile.getId());
    }

}
