package com.ecommerce.app.commission.controller;

import com.ecommerce.app.commission.model.CommissionStatus;
import com.ecommerce.app.commission.model.CommissionType;
import com.ecommerce.app.commission.model.MarketplaceCommissionSettings;
import com.ecommerce.app.commission.service.CommissionSettingsService;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductcategoryService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/commission-settings")
public class CommissionSettingsController {

    private final CommissionSettingsService commissionSettingsService;
    private final ProductcategoryService productcategoryService;
    private final VendorprofileService vendorprofileService;
    private final ProductService productService;

    public CommissionSettingsController(
            CommissionSettingsService commissionSettingsService,
            ProductcategoryService productcategoryService,
            VendorprofileService vendorprofileService,
            ProductService productService) {
        this.commissionSettingsService = commissionSettingsService;
        this.productcategoryService = productcategoryService;
        this.vendorprofileService = vendorprofileService;
        this.productService = productService;
    }

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 20) Pageable pageable) {
        Page<MarketplaceCommissionSettings> page = commissionSettingsService.findAllActive(pageable);
        populateListModel(model, page, null);
        return "admin/commission/settings_list";
    }

    @GetMapping
    public String root() {
        return "redirect:/admin/commission-settings/list";
    }

    @GetMapping("/")
    public String rootWithSlash() {
        return "redirect:/admin/commission-settings/list";
    }

    @GetMapping("/list-by-type")
    public String listByType(
            @RequestParam(required = false) CommissionType type,
            Model model,
            @PageableDefault(size = 20) Pageable pageable) {
        if (type == null) {
            return list(model, pageable);
        }
        Page<MarketplaceCommissionSettings> page = commissionSettingsService.findByType(type, pageable);
        populateListModel(model, page, type);
        return "admin/commission/settings_list";
    }

    private void populateListModel(Model model, Page<MarketplaceCommissionSettings> page, CommissionType filterType) {
        model.addAttribute("commissionSettings", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("filterType", filterType);
        model.addAttribute("commissionTypes", CommissionType.values());
        model.addAttribute("visibleRuleCount", commissionSettingsService.countVisibleSettings());
        model.addAttribute("activeRuleCount", commissionSettingsService.countActiveSettings());
        model.addAttribute("defaultRuleCount", commissionSettingsService.countActiveSettingsByType(CommissionType.DEFAULT));
        model.addAttribute("categoryRuleCount", commissionSettingsService.countActiveSettingsByType(CommissionType.CATEGORY));
        model.addAttribute("vendorRuleCount", commissionSettingsService.countActiveSettingsByType(CommissionType.VENDOR));
        model.addAttribute("productRuleCount", commissionSettingsService.countActiveSettingsByType(CommissionType.PRODUCT));
    }

    private void populateEntityDropdowns(Model model) {
        model.addAttribute("categories", productcategoryService.findActiveCategoryDropDown());
        model.addAttribute("vendors", vendorprofileService.all_vendor_list_for_Dropdown());
        model.addAttribute("products", productService.product_List_For_Dropdown());
    }

    private void populateFormModel(Model model) {
        model.addAttribute("commissionTypes", CommissionType.values());
        model.addAttribute("commissionStatuses", CommissionStatus.values());
        populateEntityDropdowns(model);
    }

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(required = false) CommissionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long productId,
            Model model) {
        if (!model.containsAttribute("commissionSetting")) {
            model.addAttribute("commissionSetting", buildPrefilledSetting(type, categoryId, vendorId, productId));
        }
        populateFormModel(model);
        return "admin/commission/settings_form";
    }

    private MarketplaceCommissionSettings buildPrefilledSetting(
            CommissionType type,
            Long categoryId,
            Long vendorId,
            Long productId) {
        MarketplaceCommissionSettings setting = new MarketplaceCommissionSettings();
        if (type == null) {
            return setting;
        }

        setting.setCommissionType(type);
        switch (type) {
            case CATEGORY -> setting.setCategoryId(categoryId);
            case VENDOR -> setting.setVendorId(vendorId);
            case PRODUCT -> setting.setProductId(productId);
            case DEFAULT -> {
                setting.setCategoryId(null);
                setting.setVendorId(null);
                setting.setProductId(null);
            }
        }
        return setting;
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("commissionSetting") MarketplaceCommissionSettings commissionSetting,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model);
            return "admin/commission/settings_form";
        }
        try {
            commissionSettingsService.save(commissionSetting);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commission setting created successfully!");
            return "redirect:/admin/commission-settings/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating commission setting: " + e.getMessage());
            populateFormModel(model);
            return "admin/commission/settings_form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<MarketplaceCommissionSettings> optional = commissionSettingsService.findById(id);
        if (optional.isEmpty()) {
            return "redirect:/admin/commission-settings/list";
        }
        model.addAttribute("commissionSetting", optional.get());
        populateFormModel(model);
        return "admin/commission/settings_form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("commissionSetting") MarketplaceCommissionSettings commissionSetting,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        commissionSetting.setId(id);
        if (bindingResult.hasErrors()) {
            populateFormModel(model);
            return "admin/commission/settings_form";
        }
        try {
            commissionSettingsService.save(commissionSetting);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commission setting updated successfully!");
            return "redirect:/admin/commission-settings/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating commission setting: " + e.getMessage());
            populateFormModel(model);
            return "admin/commission/settings_form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            commissionSettingsService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commission setting deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting commission setting: " + e.getMessage());
        }
        return "redirect:/admin/commission-settings/list";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            commissionSettingsService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commission setting deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deactivating commission setting: " + e.getMessage());
        }
        return "redirect:/admin/commission-settings/list";
    }

    @PostMapping("/activate/{id}")
    public String activate(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            commissionSettingsService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commission setting activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error activating commission setting: " + e.getMessage());
        }
        return "redirect:/admin/commission-settings/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<MarketplaceCommissionSettings> optional = commissionSettingsService.findById(id);
        if (optional.isEmpty()) {
            return "redirect:/admin/commission-settings/list";
        }
        model.addAttribute("commissionSetting", optional.get());
        return "admin/commission/settings_detail";
    }
}
