package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeInputType;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.model.AttributeValueType;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.services.AttributeOptionService;
import com.ecommerce.app.product.services.AttributeService;
import com.ecommerce.app.product.services.CatalogProductAttributeService;
import com.ecommerce.app.product.services.CategoryAttributeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 2026-05-15: Admin screens for the dynamic catalog attribute phase.
 */
@Controller
@RequestMapping("/catalog-attributes")
public class CatalogAttributeAdminController {

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private AttributeOptionService attributeOptionService;

    @Autowired
    private CategoryAttributeService categoryAttributeService;

    @Autowired
    private ProductcategoryRepository productcategoryRepository;

    @Autowired
    private CatalogProductAttributeService catalogProductAttributeService;

    @Autowired
    private SmartValidator validator;

    @GetMapping({"", "/", "/list"})
    public String list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "false") boolean filterable,
            @RequestParam(required = false, defaultValue = "false") boolean variantCapable,
            @RequestParam(required = false, defaultValue = "false") boolean searchable,
            @RequestParam(required = false, defaultValue = "false") boolean comparable,
            @RequestParam(required = false, defaultValue = "false") boolean allowMultipleValues,
            Model model) {
        model.addAttribute("attributes", attributeService.findListRows(
                keyword,
                active,
                filterable,
                variantCapable,
                searchable,
                comparable,
                allowMultipleValues
        ));
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);
        model.addAttribute("filterable", filterable);
        model.addAttribute("variantCapable", variantCapable);
        model.addAttribute("searchable", searchable);
        model.addAttribute("comparable", comparable);
        model.addAttribute("allowMultipleValues", allowMultipleValues);
        return "product/attribute/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        populateAttributeFormModel(model, new Attribute());
        return "product/attribute/form";
    }

    @GetMapping("/edit/{uuid}")
    public String editForm(@PathVariable String uuid, Model model) {
        populateAttributeFormModel(model, attributeService.findByUuid(uuid));
        return "product/attribute/form";
    }

    @PostMapping("/save")
    public String save(@Valid Attribute attribute, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", firstValidationMessage(result, "Please correct the attribute form and try again."));
            populateAttributeFormModel(model, attribute);
            return "product/attribute/form";
        }

        try {
            attributeService.save(attribute);
            redirectAttributes.addFlashAttribute("successMessage", "Catalog attribute saved successfully.");
            return "redirect:/catalog-attributes/list";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateAttributeFormModel(model, attribute);
            return "product/attribute/form";
        }
    }

    @GetMapping("/delete/{uuid}")
    public String delete(@PathVariable String uuid, RedirectAttributes redirectAttributes) {
        try {
            attributeService.deleteByUuid(uuid);
            redirectAttributes.addFlashAttribute("successMessage", "Catalog attribute deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/catalog-attributes/list";
    }

    @GetMapping("/options/{attributeUuid}")
    public String optionList(@PathVariable String attributeUuid,
            @RequestParam(required = false) String editUuid,
            Model model) {
        Attribute attribute = attributeService.findByUuid(attributeUuid);
        AttributeOption option = editUuid == null || editUuid.isBlank()
                ? new AttributeOption()
                : attributeOptionService.findByUuid(editUuid);
        option.setAttribute(attribute);

        model.addAttribute("attribute", attribute);
        model.addAttribute("attributeOption", option);
        model.addAttribute("options", attributeOptionService.findByAttributeUuid(attributeUuid));
        return "product/attribute/options";
    }

    @PostMapping("/options/save")
    public String saveOption(AttributeOption attributeOption,
            BindingResult result,
            @RequestParam String attributeUuid,
            Model model,
            RedirectAttributes redirectAttributes) {
        Attribute attribute = attributeService.findByUuid(attributeUuid);
        attributeOption.setAttribute(attribute);
        validator.validate(attributeOption, result);

        if (result.hasErrors()) {
            model.addAttribute("attribute", attribute);
            model.addAttribute("attributeOption", attributeOption);
            model.addAttribute("options", attributeOptionService.findByAttributeUuid(attributeUuid));
            model.addAttribute("errorMessage", firstValidationMessage(result, "Please correct the option form and try again."));
            return "product/attribute/options";
        }

        try {
            attributeOptionService.save(attributeUuid, attributeOption);
            redirectAttributes.addFlashAttribute("successMessage", "Attribute option saved successfully.");
            return "redirect:/catalog-attributes/options/" + attributeUuid;
        } catch (RuntimeException ex) {
            model.addAttribute("attribute", attribute);
            model.addAttribute("attributeOption", attributeOption);
            model.addAttribute("options", attributeOptionService.findByAttributeUuid(attributeUuid));
            model.addAttribute("errorMessage", ex.getMessage());
            return "product/attribute/options";
        }
    }

    @GetMapping("/options/delete/{uuid}")
    public String deleteOption(@PathVariable String uuid, RedirectAttributes redirectAttributes) {
        AttributeOption option = attributeOptionService.findByUuid(uuid);
        String attributeUuid = option.getAttribute().getUuid();
        try {
            attributeOptionService.deleteByUuid(uuid);
            redirectAttributes.addFlashAttribute("successMessage", "Attribute option deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/catalog-attributes/options/" + attributeUuid;
    }

    @GetMapping("/category-mappings")
    public String mappingList(@RequestParam(required = false) String categoryUuid,
            @RequestParam(required = false) String editUuid,
            Model model) {
        List<Productcategory> categories = productcategoryRepository.findByStatus(ProductStatusEnum.Active);
        String activeCategoryUuid = categoryUuid;
        if ((activeCategoryUuid == null || activeCategoryUuid.isBlank()) && !categories.isEmpty()) {
            activeCategoryUuid = categories.get(0).getUuid();
        }
        Productcategory activeCategory = activeCategoryUuid == null || activeCategoryUuid.isBlank()
                ? null
                : productcategoryRepository.findByUuid(activeCategoryUuid).orElse(null);

        CategoryAttribute categoryAttribute = editUuid == null || editUuid.isBlank()
                ? new CategoryAttribute()
                : categoryAttributeService.findByUuid(editUuid);

        if ((categoryAttribute.getCategory() == null || categoryAttribute.getCategory().getUuid() == null)
                && activeCategoryUuid != null && !activeCategoryUuid.isBlank()) {
            categoryAttribute.setCategory(activeCategory);
        }

        model.addAttribute("activeCategoryUuid", activeCategoryUuid);
        model.addAttribute("activeCategoryName", activeCategory != null ? activeCategory.getName() : "");
        model.addAttribute("categories", categories);
        model.addAttribute("attributes", attributeService.findAll());
        model.addAttribute("categoryAttribute", categoryAttribute);
        model.addAttribute("mappings",
                activeCategoryUuid == null || activeCategoryUuid.isBlank()
                        ? List.of()
                        : categoryAttributeService.findByCategoryUuid(activeCategoryUuid));
        return "product/attribute/category_mappings";
    }

    @PostMapping("/category-mappings/save")
    public String saveMapping(CategoryAttribute categoryAttribute,
            BindingResult result,
            @RequestParam(required = false) String categoryUuid,
            @RequestParam(required = false) String attributeUuid,
            Model model,
            RedirectAttributes redirectAttributes) {
        String resolvedCategoryUuid = categoryUuid == null ? "" : categoryUuid.trim();
        if (resolvedCategoryUuid.isBlank()
                && categoryAttribute.getCategory() != null
                && categoryAttribute.getCategory().getUuid() != null) {
            resolvedCategoryUuid = categoryAttribute.getCategory().getUuid().trim();
        }

        if (resolvedCategoryUuid.isBlank()) {
            result.reject("category", "Please select a category.");
        }
        if (attributeUuid == null || attributeUuid.isBlank()) {
            result.reject("attribute", "Please select an attribute.");
        }

        if (!resolvedCategoryUuid.isBlank()) {
            productcategoryRepository.findByUuid(resolvedCategoryUuid)
                    .ifPresent(categoryAttribute::setCategory);
        }
        if (attributeUuid != null && !attributeUuid.isBlank()) {
            try {
                categoryAttribute.setAttribute(attributeService.findByUuid(attributeUuid));
            } catch (RuntimeException ignored) {
                // Binding error is surfaced below through service save.
            }
        }

        if (result.hasErrors()) {
            Productcategory activeCategory = resolvedCategoryUuid.isBlank()
                    ? null
                    : productcategoryRepository.findByUuid(resolvedCategoryUuid).orElse(null);
            model.addAttribute("activeCategoryUuid", resolvedCategoryUuid);
            model.addAttribute("activeCategoryName", activeCategory != null ? activeCategory.getName() : "");
            model.addAttribute("categories", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
            model.addAttribute("attributes", attributeService.findAll());
            model.addAttribute("mappings",
                    resolvedCategoryUuid.isBlank()
                            ? List.of()
                            : categoryAttributeService.findByCategoryUuid(resolvedCategoryUuid));
            model.addAttribute("errorMessage", firstValidationMessage(result, "Please correct the category mapping form and try again."));
            return "product/attribute/category_mappings";
        }

        try {
            categoryAttributeService.save(resolvedCategoryUuid, attributeUuid, categoryAttribute);
            redirectAttributes.addFlashAttribute("successMessage", "Category attribute mapping saved successfully.");
            return "redirect:/catalog-attributes/category-mappings?categoryUuid="
                    + resolvedCategoryUuid;
        } catch (RuntimeException ex) {
            Productcategory activeCategory = resolvedCategoryUuid.isBlank()
                    ? null
                    : productcategoryRepository.findByUuid(resolvedCategoryUuid).orElse(null);
            model.addAttribute("activeCategoryUuid", resolvedCategoryUuid);
            model.addAttribute("activeCategoryName", activeCategory != null ? activeCategory.getName() : "");
            model.addAttribute("categories", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
            model.addAttribute("attributes", attributeService.findAll());
            model.addAttribute("mappings",
                    resolvedCategoryUuid.isBlank()
                            ? List.of()
                            : categoryAttributeService.findByCategoryUuid(resolvedCategoryUuid));
            model.addAttribute("errorMessage", ex.getMessage());
            return "product/attribute/category_mappings";
        }
    }

    @GetMapping("/category-mappings/delete/{uuid}")
    public String deleteMapping(@PathVariable String uuid, RedirectAttributes redirectAttributes) {
        CategoryAttribute mapping = categoryAttributeService.findByUuid(uuid);
        String categoryUuid = mapping.getCategory() != null ? mapping.getCategory().getUuid() : "";
        try {
            categoryAttributeService.deleteByUuid(uuid);
            redirectAttributes.addFlashAttribute("successMessage", "Category attribute mapping deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/catalog-attributes/category-mappings?categoryUuid=" + categoryUuid;
    }

    @GetMapping("/form-fields")
    public String formFields(@RequestParam(required = false) String categoryUuid,
            @RequestParam(required = false) String productUuid,
            Model model) {
        model.addAttribute("dynamicAttributeFields",
                catalogProductAttributeService.buildFieldsForProduct(categoryUuid, productUuid));
        return "product/fragments/dynamic_attribute_fields :: dynamicAttributeFields";
    }

    private void populateAttributeFormModel(Model model, Attribute attribute) {
        model.addAttribute("attribute", attribute);
        model.addAttribute("inputTypes", AttributeInputType.values());
        model.addAttribute("valueTypes", AttributeValueType.values());
    }

    private String firstValidationMessage(BindingResult result, String fallbackMessage) {
        return result.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(message -> message != null && !message.isBlank())
                .findFirst()
                .orElse(fallbackMessage);
    }
}
