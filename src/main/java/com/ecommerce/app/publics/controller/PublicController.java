/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.publics.controller;

import com.ecommerce.app.model.Contact;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import com.ecommerce.app.module.browsinghistory.model.BrowsingHistory;
import com.ecommerce.app.module.browsinghistory.model.BrowsingHistoryViewType;
import com.ecommerce.app.module.browsinghistory.service.BrowsingHistoryService;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.module.wishlist.service.WishlistService;
import com.ecommerce.app.product.dto.CatalogDiscoveryResult;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.model.SortingType;
import com.ecommerce.app.product.ripository.AvailableDeliveryAreaRepository;
import com.ecommerce.app.product.ripository.DeliveryChargeRepository;
import com.ecommerce.app.product.ripository.DeliveryTimelineRepository;
import com.ecommerce.app.product.ripository.ProductImageRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.WarrantyRepository;
import com.ecommerce.app.product.services.CatalogProductAttributeService;
import com.ecommerce.app.product.services.CatalogProductDiscoveryService;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductVariantCatalogService;
import com.ecommerce.app.publics.seo.PublicSeoService;
import com.ecommerce.app.review.services.ProductReviewService;
import com.ecommerce.app.ripository.ContactRepository;
import com.ecommerce.app.ripository.FaqRepository;
import com.ecommerce.app.ripository.GalleryRepository;
import com.ecommerce.app.ripository.ImageGalleryRepository;
import com.ecommerce.app.ripository.NewsRepository;
import com.ecommerce.app.ripository.OurclientsRepository;
import com.ecommerce.app.ripository.OurservicesRepository;
import com.ecommerce.app.ripository.ProfileRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/public")
public class PublicController {

    private static final String STATIC_PAGE_TEMPLATE = "frontview/static_page";

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    GalleryRepository galleryRepository;

    @Autowired
    ImageGalleryRepository imageGalleryRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OurservicesRepository ourservicesRepository;

    @Autowired
    OurclientsRepository ourclientsRepository;

    @Autowired
    FaqRepository faqRepository;

    @Autowired
    ProductRepository examRepository;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BlogRepository blogRepository;

    @Autowired
    BlogCategoryRepository blogCategoryRepository;

    @Autowired
    LoginEventService loginEventService;

    @Autowired
    ProductService productService;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    AvailableDeliveryAreaRepository availableDeliveryAreaRepository;

    @Autowired
    DeliveryChargeRepository deliveryChargeRepository;

    @Autowired
    DeliveryTimelineRepository deliveryTimelineRepository;

    @Autowired
    WarrantyRepository warrantyRepository;

    @Autowired
    ProductReviewService productReviewService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    CatalogProductAttributeService catalogProductAttributeService;

    @Autowired
    ProductVariantCatalogService productVariantCatalogService;

    @Autowired
    CatalogProductDiscoveryService catalogProductDiscoveryService;

    @Autowired
    GlobalSettingsService globalSettingsService;

    @Autowired
    BrowsingHistoryService browsingHistoryService;

    @Autowired
    PublicSeoService publicSeoService;

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    ReferralService referralService;

    @RequestMapping("/about-us")
    public String aboutUs(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/about-us",
                "About Us",
                "Our story",
                "Learn how we serve customers with dependable products, secure checkout, and responsive support.",
                "Learn more about the store, our service approach, and how we support customers before and after every order.",
                normalizeRichText(settings.getAboutUs()),
                defaultAboutSections(settings)
        );
    }

    @RequestMapping("/member-login")
    public String memberlogin(Model model, HttpServletRequest request) {
        model.addAttribute("attribute", "value");
        publicSeoService.apply(model, publicSeoService.noIndexPage(
                request,
                "/public/member-login",
                "Customer Sign In",
                "Sign in to your customer account to manage orders, wishlist items, and account details."
        ));

        return "frontview/member-login";
    }

    @RequestMapping("/front-registration")
    public String studentRegistration(Model model,
            @RequestParam(name = "ref", required = false) String referralCode,
            HttpSession session,
            Users users,
            HttpServletRequest request) {

        /////Role instructor = roleRepository.findBySlug("instructor");
        ////  model.addAttribute("instructor", instructor);
        //// Role customer = roleRepository.findBySlug("customer");
        ///// model.addAttribute("customer", customer);
        String normalizedReferralCode = trimToNull(referralCode);
        if (normalizedReferralCode != null && session != null) {
            session.setAttribute("productShareReferralCode", normalizedReferralCode);
        }
        Object prefilledReferralCode = session == null ? null : session.getAttribute("productShareReferralCode");
        model.addAttribute("prefilledReferralCode", prefilledReferralCode instanceof String ? prefilledReferralCode : "");
        publicSeoService.apply(model, publicSeoService.staticPage(
                request,
                "/public/front-registration",
                "Create Your Shopping Account",
                "Register for a customer account to shop faster, save wishlist items, and track orders."
        ));
        return "frontview/front-registration";
    }

    @RequestMapping("/forgot-password")
    public String forgotPassword(Model model, HttpServletRequest request) {
        model.addAttribute("attribute", "value");
        publicSeoService.apply(model, publicSeoService.noIndexPage(
                request,
                "/public/forgot-password",
                "Reset Your Password",
                "Recover access to your customer account securely."
        ));
        return "frontview/forgot-password";
    }

    @RequestMapping("/product")
    public String product(Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {

        Vendorprofile selectedVendor = null;
        if (vendor != null && !vendor.isBlank()) {
            selectedVendor = vendorprofileRepository.findByUuid(vendor.trim()).orElse(null);
        }

        List<Map<String, Object>> allProducts = (vendor != null && !vendor.isBlank() && selectedVendor == null)
                ? List.of()
                : productService.all_Product_front_view(
                        selectedVendor == null ? null : selectedVendor.getId(),
                        null,
                        null,
                        null,
                        null,
                        null
                );

        allProducts = catalogProductDiscoveryService.applyKeywordSearch(allProducts, q);
        return renderProductList(model, allProducts, page, size, sort, selectedVendor, q, request);
    }

    private String renderProductList(
            Model model,
            List<Map<String, Object>> allProducts,
            int page,
            int size,
            String sort,
            Vendorprofile selectedVendor,
            String query,
            HttpServletRequest request) {

        List<Map<String, Object>> safeProducts = allProducts == null ? List.of() : allProducts;
        List<Map<String, Object>> sortedProducts = sortProductList(safeProducts, sort);

        int safeSize = Math.max(1, Math.min(size, 60));
        int totalProducts = sortedProducts.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / safeSize));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * safeSize, totalProducts);
        int toIndex = Math.min(fromIndex + safeSize, totalProducts);
        List<Map<String, Object>> pagedProducts = sortedProducts.subList(fromIndex, toIndex);

        model.addAttribute("productlist", pagedProducts);
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatusAndParentIsNull(ProductStatusEnum.Active));
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);
        model.addAttribute("currentVendorUuid", selectedVendor == null ? null : selectedVendor.getUuid());
        model.addAttribute("selectedVendorName", selectedVendor == null ? null : selectedVendor.getCompanyName());
        model.addAttribute("currentQuery", query);
        publicSeoService.apply(model, publicSeoService.productList(request, pagedProducts, selectedVendor, query, sort, currentPage));
        return "frontview/product";
    }

    private List<Map<String, Object>> sortProductList(List<Map<String, Object>> products, String sort) {
        Comparator<Map<String, Object>> comparator = switch (sort == null ? "latest" : sort) {
            case "price_low" ->
                Comparator.comparing(product -> getBigDecimal(product, "afterDiscountRemainingAmount", "salesPrice"));
            case "price_high" ->
                Comparator.comparing((Map<String, Object> product) -> getBigDecimal(product, "afterDiscountRemainingAmount", "salesPrice")).reversed();
            case "discount" ->
                Comparator.comparing((Map<String, Object> product) -> getBigDecimal(product, "totalDiscountPercent")).reversed();
            case "name" ->
                Comparator.comparing(product -> String.valueOf(product.getOrDefault("title", "")), String.CASE_INSENSITIVE_ORDER);
            default ->
                Comparator.comparing((Map<String, Object> product) -> getLong(product, "id")).reversed();
        };

        return products.stream().sorted(comparator).toList();
    }

    private BigDecimal getBigDecimal(Map<String, Object> product, String... keys) {
        if (product == null || keys == null) {
            return BigDecimal.ZERO;
        }
        for (String key : keys) {
            Object value = product.get(key);
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal;
            }
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            if (value instanceof String text && !text.isBlank()) {
                try {
                    return new BigDecimal(text.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private Long getLong(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return 0L;
        }
        Object value = product.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    private String getString(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return "";
        }
        Object value = product.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean getBoolean(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return false;
        }
        Object value = product.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }
        return false;
    }

    @RequestMapping("/product-by-category/{prodcatid}")
    public String productByCategory(
            Model model,
            @PathVariable String prodcatid,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(name = "brand", required = false) List<Long> brandIds,
            @RequestParam(required = false) Boolean discounted,
            @RequestParam(required = false) Boolean newProduct,
            @RequestParam(name = "emi", required = false) Boolean emiAvailable,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String q,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Productcategory activeCategory = resolveCategoryReference(prodcatid);
        if (activeCategory == null || activeCategory.getUuid() == null || activeCategory.getUuid().isBlank()) {
            return "redirect:/public/product";
        }

        browsingHistoryService.recordCategoryView(activeCategory, request, response);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal swappedMinPrice = maxPrice;
            maxPrice = minPrice;
            minPrice = swappedMinPrice;
        }

        List<Long> selectedBrandIds = brandIds == null ? List.of() : brandIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Map<String, Object>> baseCategoryProducts = productService.categoryProductsForFrontView(
                activeCategory.getUuid(),
                null,
                null,
                List.of(),
                null,
                null,
                null
        );

        List<Map<String, Object>> filteredProducts = productService.categoryProductsForFrontView(
                activeCategory.getUuid(),
                minPrice,
                maxPrice,
                selectedBrandIds,
                discounted,
                newProduct,
                emiAvailable
        );

        String categoryUuid = activeCategory.getUuid();
        Map<String, List<String>> selectedAttributeFilters = catalogProductDiscoveryService
                .extractSelectedAttributeFilters(request.getParameterMap());
        CatalogDiscoveryResult discoveryResult = catalogProductDiscoveryService.refineCategoryProducts(
                categoryUuid,
                filteredProducts,
                q,
                selectedAttributeFilters
        );

        List<Map<String, Object>> sortedProducts = sortProductList(discoveryResult.getFilteredProducts(), sort);

        List<Long> categoryProductIds = baseCategoryProducts.stream()
                .map(product -> getLong(product, "id"))
                .filter(id -> id > 0)
                .toList();

        List<com.ecommerce.app.product.model.ProductVariant> categoryCatalogVariants = productVariantCatalogService.findByProductIds(categoryProductIds);

        Map<Long, BigDecimal> catalogVariantAvailableByProductId = categoryCatalogVariants.stream()
                .filter(variant -> variant.getProduct() != null && variant.getProduct().getId() != null)
                .collect(Collectors.groupingBy(
                        variant -> variant.getProduct().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                variant -> variant.getStockQuantity() == null ? BigDecimal.ZERO : variant.getStockQuantity(),
                                BigDecimal::add
                        )
                ));

        List<Map<String, Object>> availableManufacturers = baseCategoryProducts.stream()
                .filter(product -> getLong(product, "manufacturerId") > 0 && !getString(product, "manufacturerName").isBlank())
                .sorted(Comparator.comparing(product -> getString(product, "manufacturerName"), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toMap(
                        product -> getLong(product, "manufacturerId"),
                        product -> {
                            Map<String, Object> option = new LinkedHashMap<>();
                            option.put("id", getLong(product, "manufacturerId"));
                            option.put("name", getString(product, "manufacturerName"));
                            return option;
                        },
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        BigDecimal categoryMinAvailablePrice = baseCategoryProducts.stream()
                .map(product -> getBigDecimal(product, "afterDiscountRemainingAmount", "salesPrice"))
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal categoryMaxAvailablePrice = baseCategoryProducts.stream()
                .map(product -> getBigDecimal(product, "afterDiscountRemainingAmount", "salesPrice"))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<String> selectedBrandNames = availableManufacturers.stream()
                .filter(option -> selectedBrandIds.contains(getLong(option, "id")))
                .map(option -> getString(option, "name"))
                .toList();

        boolean hasDynamicAttributeFilters = catalogProductDiscoveryService.hasDynamicFilters(selectedAttributeFilters);
        boolean hasActiveFilters = minPrice != null
                || maxPrice != null
                || !selectedBrandIds.isEmpty()
                || Boolean.TRUE.equals(discounted)
                || Boolean.TRUE.equals(newProduct)
                || Boolean.TRUE.equals(emiAvailable)
                || hasDynamicAttributeFilters
                || (q != null && !q.isBlank());

        sortedProducts.forEach(product -> {
            Long productId = getLong(product, "id");
            boolean manageStock = getBoolean(product, "manageStock");
            boolean manageProductVariants = getBoolean(product, "manageProductVariants");
            boolean allowPreorder = getBoolean(product, "allowPreorder");
            BigDecimal variantAvailableStock = catalogVariantAvailableByProductId.getOrDefault(productId, BigDecimal.ZERO);
            BigDecimal productAvailableStock = getBigDecimal(product, "stockAvailableQuantity");

            BigDecimal availableStock = manageProductVariants
                    ? variantAvailableStock
                    : (productAvailableStock.compareTo(BigDecimal.ZERO) > 0 ? productAvailableStock : variantAvailableStock);

            boolean soldOut = manageStock && availableStock.compareTo(BigDecimal.ZERO) <= 0;

            product.put("availableStockQuantity", availableStock);
            product.put("showPreorderBadge", soldOut && allowPreorder);
            product.put("showOutOfStockBadge", soldOut && !allowPreorder);
        });

        model.addAttribute("productlist", sortedProducts);
        model.addAttribute("totalProducts", sortedProducts.size());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatusAndParentIsNull(ProductStatusEnum.Active));
        model.addAttribute("availableManufacturers", availableManufacturers);
        model.addAttribute("dynamicFilterGroups", discoveryResult.getFilterGroups());
        model.addAttribute("categoryMinPrice", categoryMinAvailablePrice);
        model.addAttribute("categoryMaxPrice", categoryMaxAvailablePrice);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedBrandIds", selectedBrandIds);
        model.addAttribute("selectedBrandNames", selectedBrandNames);
        model.addAttribute("selectedDiscounted", Boolean.TRUE.equals(discounted));
        model.addAttribute("selectedNewProduct", Boolean.TRUE.equals(newProduct));
        model.addAttribute("selectedEmi", Boolean.TRUE.equals(emiAvailable));
        model.addAttribute("hasActiveFilters", hasActiveFilters);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentCategoryUuid", activeCategory.getUuid());
        model.addAttribute("selectedKeyword", q);
        addCategoryMetadataModel(model, activeCategory, request, sortedProducts.size());

        model.addAttribute("child_category_list", productcategoryRepository.findByStatusAndParent(ProductStatusEnum.Active, activeCategory));
        return "frontview/product-by-category";
    }

    @RequestMapping("/single-product/{prodid}")
    public String single_product(Model model, @PathVariable String prodid, Product product, Principal principal,
            @RequestParam(name = "ref", required = false) String referralCode,
            HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        Product activeProduct = resolveProductReference(prodid);
        if (activeProduct == null || activeProduct.getUuid() == null || activeProduct.getUuid().isBlank()) {
            return "redirect:/public/product";
        }

        Map<String, Object> product_details = productService.product_details_for_front_view_single_product_page_by_Uuid(activeProduct.getUuid());
        if (product_details == null || product_details.isEmpty()) {
            return "redirect:/public/product";
        }

        captureProductShareReferral(referralCode, activeProduct.getUuid(), session);
        browsingHistoryService.recordProductView(activeProduct, request, response);
        long productViewCount = browsingHistoryService.getProductViewCount(activeProduct);
        model.addAttribute("productViewCount", productViewCount);

        model.addAttribute("product_details", product_details);
        addProductShareModel(model, activeProduct, product_details, request);
        addProductMetadataModel(model, activeProduct, product_details, request);
        model.addAttribute("productSpecifications",
                catalogProductAttributeService.buildSpecificationViews(getString(product_details, "uuid")));

        Object vendorIdObj = product_details.get("vendorProfileId");
        Long vendorId = null;
        if (vendorIdObj != null) {
            if (vendorIdObj instanceof Number) {
                vendorId = ((Number) vendorIdObj).longValue();
            } else if (vendorIdObj instanceof String) {
                vendorId = Long.parseLong((String) vendorIdObj);
            }
        }

        Object catIdObj = product_details.get("categoryId");
        Long catId = null;
        if (catIdObj != null) {
            if (catIdObj instanceof Number) {
                catId = ((Number) catIdObj).longValue();
            } else if (catIdObj instanceof String) {
                catId = Long.parseLong((String) catIdObj);
            }
        }

        //System.out.println("vendor id " + vendorId);
        model.addAttribute(
                "vProduct",
                productReviewService.enrichProductCardsWithReviewSummary(
                        productService.vendor_random_product_by_category(vendorId, catId, SortingType.RANDOM, 10)
                )
        );

        model.addAttribute(
                "vReandomProduct",
                productReviewService.enrichProductCardsWithReviewSummary(
                        productService.product_By_Vendor(vendorId, SortingType.RANDOM, 10)
                )
        );

        model.addAttribute(
                "allReandomProduct",
                productReviewService.enrichProductCardsWithReviewSummary(
                        productService.all_random_product(SortingType.RANDOM, 10)
                )
        );

        model.addAttribute("img_list", productImageRepository.findByProduct_UuidOrderByIdDesc(activeProduct.getUuid()));
        model.addAttribute("d_a_list", availableDeliveryAreaRepository.findByProduct_UuidOrderByIdDesc(activeProduct.getUuid()));
        model.addAttribute("d_c_list", deliveryChargeRepository.findByProduct_UuidOrderByIdDesc(activeProduct.getUuid()));
        model.addAttribute("d_t_list", deliveryTimelineRepository.findByProduct_UuidOrderByIdDesc(activeProduct.getUuid()));
        model.addAttribute("w_list", warrantyRepository.findByProduct_UuidOrderByIdDesc(activeProduct.getUuid()));
        List<com.ecommerce.app.product.dto.CatalogVariantSummaryView> catalogVariants = productVariantCatalogService
                .buildVariantSummaries(getString(product_details, "uuid"));
        boolean hasCatalogVariants = catalogVariants != null && !catalogVariants.isEmpty();
        model.addAttribute("catalogVariants", catalogVariants);
        model.addAttribute("catalogVariantSelectors",
                productVariantCatalogService.buildVariantSelectionViews(getString(product_details, "uuid"), null));
        model.addAttribute("hasCatalogVariants", hasCatalogVariants);

        boolean manageStock = getBoolean(product_details, "manageStock");
        boolean manageProductVariants = getBoolean(product_details, "manageProductVariants");
        boolean allowPreorder = getBoolean(product_details, "allowPreorder");
        BigDecimal variantAvailableStock = hasCatalogVariants
                ? catalogVariants.stream()
                        .map(variantRow -> variantRow.getStockQuantity() == null ? BigDecimal.ZERO : variantRow.getStockQuantity())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        BigDecimal productAvailableStock = getBigDecimal(product_details, "stockAvailableQuantity");

        BigDecimal availableStock = manageProductVariants
                ? variantAvailableStock
                : (productAvailableStock.compareTo(BigDecimal.ZERO) > 0 ? productAvailableStock : variantAvailableStock);

        boolean soldOut = manageStock && availableStock.compareTo(BigDecimal.ZERO) <= 0;

        product_details.put("availableStockQuantity", availableStock);
        product_details.put("showPreorderBadge", soldOut && allowPreorder);
        product_details.put("showOutOfStockBadge", soldOut && !allowPreorder);
        product_details.put("availabilityLabel", soldOut ? (allowPreorder ? "Preorder" : "Out of stock") : "In stock");
        model.addAttribute("productReviewSummary", productReviewService.getProductReviewSummary(activeProduct.getUuid()));
        model.addAttribute("productReviews", productReviewService.getPublicReviewsForProduct(activeProduct.getUuid()));
        model.addAttribute(
                "customerReviewContext",
                productReviewService.getCustomerReviewContextForProduct(activeProduct.getUuid(), getAuthenticatedCustomerId())
        );
        model.addAttribute("currentProductInWishlist", wishlistService.isInWishlist(principal, activeProduct.getUuid()));

        return "frontview/single-product";
    }

    @RequestMapping("/browsing-history")
    public String browsingHistory(Model model, HttpServletRequest request, HttpServletResponse response) {
        List<BrowsingHistory> historyEntries = browsingHistoryService.getCurrentBrowsingHistory(request, response);
        boolean authenticatedUser = getAuthenticatedUser() != null;
        long productHistoryCount = historyEntries.stream()
                .filter(entry -> entry.getViewType() == BrowsingHistoryViewType.PRODUCT)
                .count();
        long categoryHistoryCount = historyEntries.stream()
                .filter(entry -> entry.getViewType() == BrowsingHistoryViewType.CATEGORY)
                .count();
        String historyTitle = authenticatedUser ? "Your Browsing History" : "This Browser History";
        String historySubtitle = authenticatedUser
                ? "All viewed products and categories saved under your account. Repeated visits are kept as separate history records."
                : "Products and categories viewed from this browser. Sign in to keep this history with your account. Repeated visits are kept as separate history records.";

        model.addAttribute("historyEntries", historyEntries);
        model.addAttribute("historyTitle", historyTitle);
        model.addAttribute("historySubtitle", historySubtitle);
        model.addAttribute("historyCount", historyEntries.size());
        model.addAttribute("productHistoryCount", productHistoryCount);
        model.addAttribute("categoryHistoryCount", categoryHistoryCount);
        model.addAttribute("authenticatedHistoryUser", authenticatedUser);
        publicSeoService.apply(model, publicSeoService.noIndexPage(
                request,
                "/public/browsing-history",
                historyTitle,
                historySubtitle
        ));

        return "frontview/browsing-history";
    }

    @RequestMapping("/blogdetails/{blogid}")
    public String blogDetails(@PathVariable Long blogid) {
        return blogRepository.findById(blogid)
                .map(blog -> "redirect:/public/blog/" + blog.getSlug())
                .orElse("redirect:/public/blog");
    }

    @RequestMapping("/blog-by-cat/{catid}")
    public String blogByCategory(@PathVariable Long catid) {
        return blogCategoryRepository.findById(catid)
                .map(category -> "redirect:/public/blog/category/" + category.getSlug())
                .orElse("redirect:/public/blog");
    }

    private void captureProductShareReferral(String referralCode, String productUuid, HttpSession session) {
        String normalizedCode = trimToNull(referralCode);
        if (normalizedCode == null || session == null) {
            return;
        }

        Users referrer = referralService.resolveReferrerByCode(normalizedCode);
        if (referrer == null || referrer.getId() == null) {
            return;
        }

        Users currentUser = getAuthenticatedUser();
        if (currentUser != null && referrer.getId().equals(currentUser.getId())) {
            return;
        }

        session.setAttribute("productShareReferralCode", normalizedCode);
        session.setAttribute("productShareReferralProductUuid", productUuid);
    }

    private void addProductShareModel(Model model, Product product, Map<String, Object> productDetails, HttpServletRequest request) {
        Users currentUser = getAuthenticatedUser();
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        String referralCode = currentUser == null ? null
                : referralRepository.findByUsers(currentUser)
                        .map(Referral::getReferralCode)
                        .orElse(null);
        if (!Boolean.TRUE.equals(settings.getReferralLinksEnabled())) {
            referralCode = null;
        }
        String shareUrl = buildProductShareUrl(request, product.getUuid(), referralCode);
        String productTitle = getString(productDetails, "title");
        String productDescription = plainText(getString(productDetails, "shortDescription"));
        String shareMessage = (productTitle == null || productTitle.isBlank() ? "Check this product" : "Check this product: " + productTitle)
                + " " + shareUrl;

        model.addAttribute("customerProductReferralCode", referralCode);
        model.addAttribute("productShareUrl", shareUrl);
        model.addAttribute("productShareMessage", shareMessage);
        model.addAttribute("productShareWhatsAppUrl", "https://wa.me/?text=" + encodeUrl(shareMessage));
        model.addAttribute("productShareFacebookUrl", "https://www.facebook.com/sharer/sharer.php?u=" + encodeUrl(shareUrl));
        model.addAttribute("shareUrl", shareUrl);
        model.addAttribute("shareTitle", productTitle);
        model.addAttribute("shareDescription", productDescription);
        model.addAttribute("sharePageType", "PRODUCT");
        model.addAttribute("shareEntityReference", product.getSku() > 0 ? "SKU-" + product.getSku() : product.getUuid());
    }

    private void addProductMetadataModel(Model model, Product product, Map<String, Object> productDetails, HttpServletRequest request) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        publicSeoService.apply(model, publicSeoService.product(request, product, productDetails));
        model.addAttribute("trackingProduct", Map.of(
                "item_id", product.getSku() > 0 ? "SKU-" + product.getSku() : product.getUuid(),
                "item_name", textOrDefault(getString(productDetails, "metaTitle"), getString(productDetails, "title")),
                "item_brand", getString(productDetails, "manufacturerName"),
                "item_category", getString(productDetails, "category"),
                "price", getBigDecimal(productDetails, "afterDiscountRemainingAmount", "salesPrice"),
                "currency", textOrDefault(settings.getCurrency(), "BDT"),
                "availability", getString(productDetails, "availabilityLabel")
        ));
    }

    private void addCategoryMetadataModel(Model model, Productcategory category, HttpServletRequest request, int productCount) {
        publicSeoService.apply(model, publicSeoService.category(request, category, productCount));
    }

    private String buildProductShareUrl(HttpServletRequest request, String productUuid, String referralCode) {
        String path = request.getContextPath() + "/public/single-product/" + productUuid;
        var builder = publicUrlBuilder(request, path);
        if (referralCode != null && !referralCode.isBlank()) {
            builder.queryParam("ref", referralCode.trim());
        }
        return builder.build().toUriString();
    }

    private String buildPublicUrl(HttpServletRequest request, String publicPath) {
        String path = request.getContextPath() + publicPath;
        return publicUrlBuilder(request, path).build().toUriString();
    }

    private org.springframework.web.util.UriComponentsBuilder publicUrlBuilder(HttpServletRequest request, String path) {
        String publicBaseUrl = safePublicBaseUrl(globalSettingsService.getActiveSettings().getPublicBaseUrl());
        var builder = publicBaseUrl != null
                ? ServletUriComponentsBuilder.fromUriString(publicBaseUrl)
                : ServletUriComponentsBuilder.fromRequest(request);
        builder.replacePath(path).replaceQuery(null);
        return builder;
    }

    private String encodeUrl(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String absolutePublicAssetUrl(GlobalSettings settings, String pathOrUrl) {
        String value = trimToNull(pathOrUrl);
        String baseUrl = safePublicBaseUrl(settings == null ? null : settings.getPublicBaseUrl());
        if (value == null || baseUrl == null) {
            return null;
        }
        if (safePublicBaseUrl(value) != null) {
            return value;
        }
        if (value.startsWith("/")) {
            return baseUrl + value;
        }
        return null;
    }

    private String safePublicBaseUrl(String value) {
        String cleanValue = trimToNull(value);
        if (cleanValue == null || !cleanValue.startsWith("https://")) {
            return null;
        }
        String lowerValue = cleanValue.toLowerCase(Locale.ROOT);
        if (lowerValue.contains("localhost")
                || lowerValue.contains("127.0.0.1")
                || lowerValue.contains("0.0.0.0")
                || lowerValue.matches("https://10\\..*")
                || lowerValue.matches("https://172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
                || lowerValue.matches("https://192\\.168\\..*")) {
            return null;
        }
        while (cleanValue.endsWith("/")) {
            cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
        }
        return cleanValue;
    }

    private String plainText(String value) {
        String cleanValue = trimToNull(value);
        if (cleanValue == null) {
            return null;
        }
        return HtmlUtils.htmlUnescape(cleanValue.replaceAll("<[^>]+>", " "))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Productcategory resolveCategoryReference(String categoryRef) {
        if (categoryRef == null || categoryRef.isBlank()) {
            return null;
        }
        Productcategory category = productcategoryRepository.findByUuid(categoryRef.trim()).orElse(null);
        if (category != null) {
            return category;
        }
        try {
            return productcategoryRepository.findById(Long.valueOf(categoryRef.trim())).orElse(null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Product resolveProductReference(String productRef) {
        if (productRef == null || productRef.isBlank()) {
            return null;
        }
        Product product = productRepository.findByUuid(productRef.trim()).orElse(null);
        if (product != null) {
            return product;
        }
        try {
            return productRepository.findById(Long.valueOf(productRef.trim())).orElse(null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long getAuthenticatedCustomerId() {
        Users user = getAuthenticatedUser();
        if (user == null || user.getUserType() != UserType.customer) {
            return null;
        }

        return user.getId();
    }

    private Users getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return usersRepository.findByEmail(authentication.getName()).orElse(null);
    }

    @RequestMapping({"/contactUs", "/contact-us"})
    public String contactUs(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/contact-us",
                "Contact Us",
                "Customer care",
                "Reach our support team for order questions, product details, billing assistance, and account help.",
                "Find the best way to contact the store for product questions, order support, delivery updates, or general help.",
                normalizeRichText(settings.getContactUsContent()),
                defaultContactSections(settings)
        );
    }

    @RequestMapping("/home-contact-save")
    public String homecontactsave(Model model, @Valid Contact contact, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        model.addAttribute("attribute", "value");

        if (bindingResult.hasErrors()) {
            return "welcome/welcome";
        }
        contactRepository.save(contact);

        redirectAttributes.addFlashAttribute("message", "Your Message Successfully ");

        return "redirect:/";
    }

    @RequestMapping({"/privacy_policy", "/privacy-policy"})
    public String privacyPolicy(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/privacy-policy",
                "Privacy Policy",
                "Store policies",
                "Understand how we collect, use, store, and protect customer information across the storefront.",
                "Review how customer data is collected, processed, protected, and used to support your orders and account activity.",
                normalizeRichText(settings.getPrivacyPolicy()),
                defaultPrivacySections()
        );

    }

    @RequestMapping("/help")
    public String help(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/help",
                "Help",
                "Support center",
                "Find quick answers about orders, deliveries, payments, returns, and customer account support.",
                "Get quick guidance on ordering, payments, delivery tracking, account access, and after-sales support.",
                normalizeRichText(settings.getHelpPageContent()),
                defaultHelpSections()
        );
    }

    @RequestMapping("/terms-of-use")
    public String termsOfUse(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/terms-of-use",
                "Terms of Use",
                "Legal information",
                "These terms explain the rules for browsing, shopping, registering, and interacting with the storefront.",
                "Read the general rules for using the website, creating an account, and interacting with storefront content and services.",
                normalizeRichText(settings.getTermsOfUseContent()),
                defaultTermsOfUseSections()
        );
    }

    @RequestMapping("/payment-methods")
    public String paymentMethods(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/payment-methods",
                "Payment Methods",
                "Checkout information",
                "See which payment options are available for eligible orders and how payment is confirmed during checkout.",
                "Review the available payment options and the guidance customers should follow when paying for orders.",
                normalizeRichText(settings.getPaymentMethodsContent()),
                defaultPaymentMethodSections(settings)
        );
    }

    @RequestMapping("/returns-replacements")
    public String returnsAndReplacements(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/returns-replacements",
                "Returns & Replacements",
                "After-sales support",
                "Check how to request a return or replacement if an item arrives damaged, incorrect, or unsuitable.",
                "Learn when returns or replacements can be requested, how items should be prepared, and how support reviews each case.",
                normalizeRichText(settings.getReturnPolicy()),
                defaultReturnSections(settings)
        );
    }

    @RequestMapping({"/refund-returns-policy", "/refund-and-returns-policy"})
    public String refundAndReturnsPolicy(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/refund-returns-policy",
                "Refund and Returns Policy",
                "Store policies",
                "Review how approved returns are handled and how refunds are processed back to the original payment method.",
                "Understand the review process for refunds, return time windows, and how completed refunds are issued to customers.",
                normalizeRichText(settings.getRefundPolicy()),
                defaultRefundSections(settings)
        );
    }

    @RequestMapping("/shipping-rates-policies")
    public String shippingRatesAndPolicies(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/shipping-rates-policies",
                "Shipping Rates & Policies",
                "Delivery details",
                "Understand delivery timelines, location-based charges, and how shipping costs appear during checkout.",
                "Find shipping timing guidance, delivery charge information, and the main shipping rules customers should know before placing an order.",
                normalizeRichText(settings.getShippingPolicy()),
                defaultShippingSections(settings)
        );
    }

    @RequestMapping({"/term-and-conditions", "/terms-and-conditions"})
    public String termsAndConditions(Model model) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        return renderStaticPage(
                model,
                settings,
                "/public/terms-and-conditions",
                "Term and Conditions",
                "Legal information",
                "Review the terms that apply to orders, payments, delivery commitments, returns, and customer responsibilities.",
                "Read the store terms that govern orders, payments, fulfillment, delivery, returns, and customer responsibilities.",
                normalizeRichText(settings.getTermsAndConditions()),
                defaultTermsAndConditionsSections()
        );
    }

    private String renderStaticPage(
            Model model,
            GlobalSettings settings,
            String activePath,
            String heading,
            String eyebrow,
            String intro,
            String description,
            String pageHtml,
            List<Map<String, Object>> pageSections) {

        model.addAttribute("pageTitle", heading);
        model.addAttribute("pageHeading", heading);
        model.addAttribute("pageEyebrow", eyebrow);
        model.addAttribute("pageIntro", intro);
        model.addAttribute("pageDescription", description);
        model.addAttribute("pageHtml", pageHtml);
        model.addAttribute("pageSections", pageSections == null ? List.of() : pageSections);
        model.addAttribute("staticPageLinks", buildStaticPageLinks(activePath));
        model.addAttribute("storeSettings", settings);
        publicSeoService.apply(model, publicSeoService.staticPage(null, activePath, heading, description));
        return STATIC_PAGE_TEMPLATE;
    }

    private List<Map<String, Object>> buildStaticPageLinks(String activePath) {
        return List.of(
                navLink("Privacy Policy", "/public/privacy-policy", activePath),
                navLink("Contact Us", "/public/contact-us", activePath),
                navLink("Help", "/public/help", activePath),
                navLink("Terms of Use", "/public/terms-of-use", activePath),
                navLink("Payment Methods", "/public/payment-methods", activePath),
                navLink("Returns & Replacements", "/public/returns-replacements", activePath),
                navLink("Refund and Returns Policy", "/public/refund-returns-policy", activePath),
                navLink("Shipping Rates & Policies", "/public/shipping-rates-policies", activePath),
                navLink("Term and Conditions", "/public/terms-and-conditions", activePath),
                navLink("About Us", "/public/about-us", activePath)
        );
    }

    private Map<String, Object> navLink(String label, String href, String activePath) {
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("label", label);
        link.put("href", href);
        link.put("active", href.equals(activePath));
        return link;
    }

    private List<Map<String, Object>> defaultAboutSections(GlobalSettings settings) {
        return List.of(
                section(
                        "What we do",
                        List.of(
                                "We aim to make online shopping simple, reliable, and transparent for every customer.",
                                "Our storefront brings together curated products, clear pricing, and dependable order support from browsing through delivery."
                        ),
                        List.of(
                                "Organized product discovery and category browsing",
                                "Secure checkout with trusted payment options",
                                "Responsive support for delivery, returns, and account questions"
                        )
                ),
                section(
                        "How we serve customers",
                        List.of(
                                "We focus on accurate product information, consistent communication, and practical support when questions come up before or after an order.",
                                settings.getDeliveryTimeText() != null && !settings.getDeliveryTimeText().isBlank()
                                ? "Our current delivery guidance is: " + settings.getDeliveryTimeText().trim() + "."
                                : "Delivery timelines may vary by location, seller availability, and product type, and the latest estimate is shown during checkout."
                        ),
                        List.of(
                                "Order updates from confirmation through fulfillment",
                                "Support for damaged, missing, or incorrect shipments",
                                "Clear policy pages covering payment, shipping, returns, and privacy"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultContactSections(GlobalSettings settings) {
        String phone = textOrDefault(settings.getSupportPhone(), "Phone support details are shared during active support hours.");
        String email = textOrDefault(settings.getSupportEmail(), "Email support is available for order, billing, and product questions.");
        String address = textOrDefault(settings.getAddress(), "Store address information will be shared here when configured.");

        return List.of(
                section(
                        "Customer support",
                        List.of(
                                "For order tracking, billing questions, delivery concerns, or account issues, our support team is ready to help.",
                                "The fastest way to get help is to contact us with your order number, registered email address, and a short description of the issue."
                        ),
                        List.of(
                                "Phone: " + phone,
                                "Email: " + email,
                                "Address: " + address
                        )
                ),
                section(
                        "Before you reach out",
                        List.of(
                                "Including the right details in your message helps us resolve issues faster and reduces back-and-forth.",
                                "Please attach product photos, invoice details, or delivery notes whenever they are relevant to your request."
                        ),
                        List.of(
                                "Order number or transaction reference",
                                "Product name or SKU when asking about an item",
                                "A concise summary of the issue and the preferred resolution"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultHelpSections() {
        return List.of(
                section(
                        "Ordering help",
                        List.of(
                                "Use the product page to review pricing, available variants, delivery details, and policy notes before checkout.",
                                "After placing an order, keep your confirmation message and order number handy for future support."
                        ),
                        List.of(
                                "Double-check your delivery address and phone number before submitting an order",
                                "Review cart totals, delivery charges, and payment method details during checkout",
                                "Sign in to your account to review order and profile information when available"
                        )
                ),
                section(
                        "Payments, delivery, and support",
                        List.of(
                                "Payment options, shipping availability, and post-order support can vary by order type and delivery zone.",
                                "If you need help after checkout, contact support with your order details so the team can verify the status quickly."
                        ),
                        List.of(
                                "Visit Payment Methods for accepted payment options",
                                "Visit Shipping Rates & Policies for delivery guidance",
                                "Visit Returns & Replacements or Refund and Returns Policy for after-sales support details"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultTermsOfUseSections() {
        return List.of(
                section(
                        "Using the website",
                        List.of(
                                "By browsing or using the storefront, you agree to use the website in a lawful and responsible way.",
                                "Website content, pricing, and availability may be updated as products change, promotions end, or inventory moves."
                        ),
                        List.of(
                                "Do not misuse the site, attempt unauthorized access, or interfere with normal storefront operation",
                                "Use accurate personal and billing information when placing orders or creating an account",
                                "Review all product, delivery, and policy details before confirming a purchase"
                        )
                ),
                section(
                        "Accounts and customer responsibilities",
                        List.of(
                                "Customers are responsible for keeping account credentials secure and for reviewing order details before checkout.",
                                "We may limit, suspend, or cancel access when fraudulent, abusive, or clearly invalid activity is detected."
                        ),
                        List.of(
                                "Maintain current email, phone, and delivery information",
                                "Report unauthorized activity as soon as it is noticed",
                                "Use the contact and help pages when clarification is needed before ordering"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultPaymentMethodSections(GlobalSettings settings) {
        List<String> methods = new ArrayList<>();
        if (Boolean.TRUE.equals(settings.getCodEnabled())) {
            methods.add("Cash on delivery may be available for eligible products and delivery zones.");
        }
        if (Boolean.TRUE.equals(settings.getOnlinePaymentEnabled())) {
            methods.add("Online payment is supported for qualifying orders through enabled payment channels.");
        }
        if (Boolean.TRUE.equals(settings.getPartialPaymentEnabled())) {
            methods.add("Partial payment may be offered on eligible orders when that option is shown at checkout.");
        }
        if (Boolean.TRUE.equals(settings.getEmiEnabled())) {
            methods.add("Installment or EMI options may be available where supported by the configured payment provider.");
        }
        if (methods.isEmpty()) {
            methods.add("Available payment options are displayed during checkout based on your order and delivery destination.");
        }

        List<String> checkoutNotes = new ArrayList<>();
        if (settings.getMinimumOrderAmount() != null && settings.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            checkoutNotes.add("Minimum order amount: " + settings.getMinimumOrderAmount().stripTrailingZeros().toPlainString());
        }
        if (settings.getMaximumOrderAmount() != null && settings.getMaximumOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            checkoutNotes.add("Maximum order amount: " + settings.getMaximumOrderAmount().stripTrailingZeros().toPlainString());
        }
        if (checkoutNotes.isEmpty()) {
            checkoutNotes.add("Final payable totals are confirmed during checkout after product, location, and delivery details are validated.");
        }

        return List.of(
                section(
                        "Accepted payment options",
                        List.of(
                                "Payment availability can depend on the product type, delivery location, and the payment settings active at checkout.",
                                "The checkout screen always shows the currently accepted payment choices before you confirm an order."
                        ),
                        methods
                ),
                section(
                        "Payment guidance",
                        List.of(
                                "Use only authorized payment channels displayed inside the storefront checkout flow.",
                                "If an online payment is interrupted, confirm the final order status before attempting another payment."
                        ),
                        checkoutNotes
                )
        );
    }

    private List<Map<String, Object>> defaultReturnSections(GlobalSettings settings) {
        List<String> eligibilityBullets = new ArrayList<>();
        Integer returnAllowedDays = settings.getReturnAllowedDays();
        if (returnAllowedDays != null && returnAllowedDays > 0) {
            eligibilityBullets.add("Return requests should normally be raised within " + returnAllowedDays + " days of the eligible delivery date.");
        }
        eligibilityBullets.add("Items should be unused or handled with care and returned with original packaging whenever possible.");
        eligibilityBullets.add("Products that are damaged in transit, defective, incorrect, or materially different from the listing should be reported promptly.");

        return List.of(
                section(
                        "Return eligibility",
                        List.of(
                                "Return approval depends on the product condition, return reason, and whether the request falls within the applicable review window.",
                                "Some product categories may have special handling requirements or limited return eligibility."
                        ),
                        eligibilityBullets
                ),
                section(
                        "Replacement requests",
                        List.of(
                                "If a replacement is available and appropriate, support may arrange an exchange instead of a refund.",
                                "Replacement processing can require order verification, photos of the item, and confirmation of the issue."
                        ),
                        List.of(
                                "Share clear photos for damaged or incorrect items",
                                "Keep all accessories, manuals, and packaging together until the case is resolved",
                                "Wait for support confirmation before shipping any product back"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultRefundSections(GlobalSettings settings) {
        List<String> refundBullets = new ArrayList<>();
        Integer refundAllowedDays = settings.getRefundAllowedDays();
        if (refundAllowedDays != null && refundAllowedDays > 0) {
            refundBullets.add("Refund requests are typically reviewed within the applicable policy window of " + refundAllowedDays + " days.");
        }
        refundBullets.add("Approved refunds are generally issued to the original payment method or the most appropriate supported refund channel.");
        refundBullets.add("Processing time can vary by payment provider, bank, or manual verification requirements.");

        return List.of(
                section(
                        "How refunds are reviewed",
                        List.of(
                                "Refund approval depends on order verification, the reported issue, product condition, and the outcome of the return review.",
                                "If a return is required before refunding, the support team will explain the next steps and any inspection requirements."
                        ),
                        refundBullets
                ),
                section(
                        "Important reminders",
                        List.of(
                                "Refund status updates may take time if external payment providers, banks, or manual quality checks are involved.",
                                "Keep your payment receipt, order number, and any support conversation references until the refund is completed."
                        ),
                        List.of(
                                "Do not send products back without return instructions",
                                "Use the contact page if you need clarification on an ongoing refund case",
                                "Check Returns & Replacements for product return guidance"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultShippingSections(GlobalSettings settings) {
        List<String> rateBullets = new ArrayList<>();
        if (settings.getInsideDhakaDeliveryCharge() != null) {
            rateBullets.add("Inside Dhaka delivery charge: " + settings.getInsideDhakaDeliveryCharge().stripTrailingZeros().toPlainString());
        }
        if (settings.getOutsideDhakaDeliveryCharge() != null) {
            rateBullets.add("Outside Dhaka delivery charge: " + settings.getOutsideDhakaDeliveryCharge().stripTrailingZeros().toPlainString());
        }
        if (Boolean.TRUE.equals(settings.getFreeDeliveryEnabled()) && settings.getFreeDeliveryMinAmount() != null) {
            rateBullets.add("Free delivery may apply on eligible orders from " + settings.getFreeDeliveryMinAmount().stripTrailingZeros().toPlainString() + " and above.");
        }
        if (settings.getCashOnDeliveryCharge() != null && settings.getCashOnDeliveryCharge().compareTo(BigDecimal.ZERO) > 0) {
            rateBullets.add("Cash on delivery surcharge: " + settings.getCashOnDeliveryCharge().stripTrailingZeros().toPlainString());
        }
        if (rateBullets.isEmpty()) {
            rateBullets.add("Shipping costs are calculated at checkout based on delivery zone, order contents, and seller fulfillment rules.");
        }

        return List.of(
                section(
                        "Shipping rates",
                        List.of(
                                "Delivery charges can vary by location, order size, product type, or seller-specific fulfillment setup.",
                                "The final shipping charge is shown before order confirmation so customers can review the complete total."
                        ),
                        rateBullets
                ),
                section(
                        "Delivery policy guidance",
                        List.of(
                                settings.getDeliveryTimeText() != null && !settings.getDeliveryTimeText().isBlank()
                                ? "Current delivery guidance: " + settings.getDeliveryTimeText().trim() + "."
                                : "Delivery timelines depend on destination, stock availability, and fulfillment workload.",
                                "Unexpected weather, regional restrictions, or courier disruptions can affect delivery timing."
                        ),
                        List.of(
                                "Ensure phone and address details are accurate before checkout",
                                "Delivery availability may differ between inside-city and outside-city zones",
                                "Contact support if a shipment delay exceeds the expected delivery window"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultTermsAndConditionsSections() {
        return List.of(
                section(
                        "Orders and pricing",
                        List.of(
                                "Orders are subject to product availability, price confirmation, payment verification, and delivery feasibility.",
                                "We may cancel or adjust an order if there is an obvious pricing issue, inventory problem, duplicate transaction, or verification concern."
                        ),
                        List.of(
                                "Order totals are reviewed before final confirmation",
                                "Promotions, discounts, and stock availability can change without prior notice",
                                "Customers should review all order details carefully before submitting payment"
                        )
                ),
                section(
                        "Fulfillment, returns, and liability",
                        List.of(
                                "Delivery windows are estimates and may change due to courier, location, or stock-related issues.",
                                "Returns, replacements, and refunds are handled according to the published policy pages and the specific facts of each case."
                        ),
                        List.of(
                                "Use the shipping, returns, and refund policy pages for related guidance",
                                "Keep proof of purchase and delivery details for support requests",
                                "Contact support promptly if an order arrives damaged, delayed, or incorrect"
                        )
                )
        );
    }

    private List<Map<String, Object>> defaultPrivacySections() {
        return List.of(
                section(
                        "Information we collect",
                        List.of(
                                "We collect the information needed to operate the storefront, process orders, support customer accounts, and respond to service requests.",
                                "Depending on how you use the site, this can include account details, order information, contact data, and basic technical usage data."
                        ),
                        List.of(
                                "Name, email address, phone number, and delivery details",
                                "Order, payment, and customer support information",
                                "Device, browser, and usage details used to improve site performance and security"
                        )
                ),
                section(
                        "How we use and protect data",
                        List.of(
                                "Customer data is used to confirm orders, deliver products, communicate service updates, and improve the shopping experience.",
                                "Reasonable administrative and technical safeguards are used to protect customer information, but customers should also keep their own account credentials secure."
                        ),
                        List.of(
                                "To process and fulfill orders",
                                "To respond to support requests and service issues",
                                "To detect fraud, prevent abuse, and maintain site reliability"
                        )
                )
        );
    }

    private Map<String, Object> section(String title, List<String> paragraphs, List<String> bullets) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", title);
        section.put("paragraphs", paragraphs == null ? List.of() : paragraphs);
        section.put("bullets", bullets == null ? List.of() : bullets);
        return section;
    }

    private String normalizeRichText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.matches("(?s).*</?[a-zA-Z][^>]*>.*")) {
            return trimmed;
        }

        String[] blocks = trimmed.replace("\r\n", "\n").replace('\r', '\n').split("\n\\s*\n");
        StringBuilder html = new StringBuilder();
        for (String block : blocks) {
            String paragraph = block.trim();
            if (paragraph.isBlank()) {
                continue;
            }
            if (html.length() > 0) {
                html.append(System.lineSeparator());
            }
            html.append("<p>")
                    .append(HtmlUtils.htmlEscape(paragraph).replace("\n", "<br>"))
                    .append("</p>");
        }
        return html.toString();
    }

    private String textOrDefault(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

}
