package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.services.ProductVariantCatalogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 2026-05-15: Generic variant admin endpoints for the phase-2 catalog rollout.
 */
@Controller
@RequestMapping("/catalog-variants")
public class CatalogVariantController {

    @Autowired
    private ProductVariantCatalogService productVariantCatalogService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/add/{productUuid}")
    public String add(@PathVariable String productUuid, Model model) {
        populateVariantForm(model, productUuid, null, null);
        return "product/catalogvariants/form";
    }

    @GetMapping("/edit/{uuid}")
    public String edit(@PathVariable String uuid, Model model) {
        ProductVariant variant = productVariantCatalogService.findByUuid(uuid);
        populateVariantForm(model, variant.getProduct().getUuid(), variant, null);
        return "product/catalogvariants/form";
    }

    @PostMapping("/save")
    public Object save(@RequestParam("productUuid") String productUuid,
            @RequestParam(value = "variantUuid", required = false) String variantUuid,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "sellingPrice", required = false) BigDecimal sellingPrice,
            @RequestParam(value = "specialPrice", required = false) BigDecimal specialPrice,
            @RequestParam(value = "stockQuantity", required = false) BigDecimal stockQuantity,
            @RequestParam(value = "status", required = false) ProductStatusEnum status,
            @RequestParam(value = "active", required = false, defaultValue = "false") Boolean active,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        try {
            productVariantCatalogService.saveVariant(
                    productUuid,
                    variantUuid,
                    sku,
                    barcode,
                    sellingPrice,
                    specialPrice,
                    stockQuantity,
                    status,
                    active,
                    request.getParameterMap()
            );
            response.setHeader("HX-Refresh", "true");
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            ProductVariant variant = variantUuid == null || variantUuid.isBlank()
                    ? null
                    : productVariantCatalogService.findByUuid(variantUuid);
            populateVariantForm(model, productUuid, variant, ex.getMessage());
            return "product/catalogvariants/form";
        }
    }

    @GetMapping("/generate/{productUuid}")
    public String generateForm(@PathVariable String productUuid, Model model) {
        Product product = productRepository.findByUuid(productUuid).orElse(null);
        model.addAttribute("productUuid", productUuid);
        model.addAttribute("productTitle", product != null ? product.getTitle() : null);
        model.addAttribute("variantSelections",
                productVariantCatalogService.buildVariantSelectionViews(productUuid, null));
        return "product/catalogvariants/generate";
    }

    @PostMapping("/generate")
    public Object generate(@RequestParam("productUuid") String productUuid,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        try {
            int createdCount = productVariantCatalogService.autoGenerateVariants(productUuid, request.getParameterMap());
            response.setHeader("HX-Refresh", "true");
            return ResponseEntity.ok().header("X-Variant-Generated", String.valueOf(createdCount)).build();
        } catch (Exception ex) {
            model.addAttribute("productUuid", productUuid);
            model.addAttribute("variantSelections",
                    productVariantCatalogService.buildVariantSelectionViews(productUuid, null));
            model.addAttribute("errorMessage", ex.getMessage());
            return "product/catalogvariants/generate";
        }
    }

    @DeleteMapping("/delete/{uuid}")
    @ResponseBody
    public String delete(@PathVariable String uuid, HttpServletResponse response) {
        try {
            productVariantCatalogService.deleteByUuid(uuid);
            response.setHeader("HX-Refresh", "true");
            return "<div class='alert alert-success'>Catalog variant deleted successfully.</div>";
        } catch (Exception ex) {
            return "<div class='alert alert-danger'>Delete failed: " + escapeHtml(ex.getMessage()) + "</div>";
        }
    }

    private void populateVariantForm(Model model, String productUuid, ProductVariant variant, String errorMessage) {
        ProductVariant safeVariant = variant == null ? new ProductVariant() : variant;
        model.addAttribute("catalogVariant", safeVariant);
        model.addAttribute("productUuid", productUuid);
        model.addAttribute("variantSelections",
                productVariantCatalogService.buildVariantSelectionViews(productUuid, safeVariant.getUuid()));
        model.addAttribute("statusList", ProductStatusEnum.values());
        model.addAttribute("errorMessage", errorMessage);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
