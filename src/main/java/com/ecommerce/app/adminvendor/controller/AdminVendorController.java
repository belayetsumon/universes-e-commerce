package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.adminvendor.dto.AdminVendorFilter;
import com.ecommerce.app.adminvendor.dto.AdminVendorProfileForm;
import com.ecommerce.app.adminvendor.services.AdminVendorManagementService;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/adminvendor")
public class AdminVendorController {

    @Autowired
    private AdminVendorManagementService adminVendorManagementService;
    private static final Logger log = LoggerFactory.getLogger(AdminVendorController.class);

    @GetMapping({"", "/", "/list"})
    public String index(
            @ModelAttribute("filter") AdminVendorFilter filter,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("list", adminVendorManagementService.search(filter));

            model.addAttribute(
                    "emailVerifiedByVendorUuid",
                    adminVendorManagementService.emailVerifiedByVendorUuid()
            );

            model.addAttribute(
                    "mobileVerifiedByVendorUuid",
                    adminVendorManagementService.mobileVerifiedByVendorUuid()
            );

            model.addAttribute("vendorStatusOptions", VendorStatusEnum.values());
            model.addAttribute("pageTitle", "Vendor Management");

            return "admin/vendor/admin_vendor_list";

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to load vendor management list. Please try again."
            );

            return "redirect:/admin";
        }
    }

    @GetMapping("/edit/{uuid}")
    public String edit(
            Model model,
            @PathVariable("uuid") String uuid,
            RedirectAttributes redirectAttributes
    ) {
        try {
            return adminVendorManagementService.getEditForm(uuid)
                    .map(form -> {
                        prepareEditModel(model, form);
                        return "admin/vendor/vendor_profile_create";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute(
                                "errorMessage",
                                "Vendor profile was not found."
                        );
                        return "redirect:/adminvendor/list";
                    });

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to load vendor edit form. Please try again."
            );

            return "redirect:/adminvendor/list";
        }
    }

    @PostMapping("/update")
    public String updateVendor(
            @Valid @ModelAttribute("vendorProfileForm") AdminVendorProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareEditModel(model, form);
            model.addAttribute("errorMessage", "Please fix the validation errors.");
            return "admin/vendor/vendor_profile_create";
        }

        try {
            adminVendorManagementService.updateVendor(form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Vendor profile updated successfully."
            );

            return "redirect:/adminvendor/list";

        } catch (RuntimeException ex) {
            prepareEditModel(model, form);
            model.addAttribute(
                    "errorMessage",
                    "Unable to update vendor profile. Please try again."
            );
            return "admin/vendor/vendor_profile_create";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("vendorForm") AdminVendorProfileForm vendorForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareEditModel(model, vendorForm);
            model.addAttribute("errorMessage", "Vendor profile could not be saved. Please correct the highlighted fields.");
            return "admin/vendor/vendor_profile_create";
        }

        try {
            Vendorprofile updatedVendor = adminVendorManagementService.updateVendor(vendorForm);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor profile updated successfully.");
            return "redirect:/adminvendor/edit/" + updatedVendor.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return redirectAfterSaveFailure(vendorForm);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile could not be saved. Please try again or review related records.");
            return redirectAfterSaveFailure(vendorForm);
        }
    }

    @GetMapping("/view/{uuid}")
    public String view(
            Model model,
            @PathVariable("uuid") String uuid,
            RedirectAttributes redirectAttributes
    ) {
        String normalizedUuid = uuid == null ? "" : uuid.trim();

        log.info("Loading vendor profile. uuid=[{}]", normalizedUuid);

        try {
            return adminVendorManagementService.findByUuid(normalizedUuid)
                    .map(vendor -> {
                        log.info("Vendor profile found. id={}, uuid={}", vendor.getId(), vendor.getUuid());

                        model.addAttribute("vendor", vendor);
                        model.addAttribute("emailVerified", adminVendorManagementService.isEmailVerified(vendor.getUuid()));
                        model.addAttribute("mobileVerified", adminVendorManagementService.isMobileVerified(vendor.getUuid()));
                        model.addAttribute("pageTitle", vendor.getCompanyName() + " Vendor Profile");

                        return "admin/vendor/details";
                    })
                    .orElseGet(() -> {
                        log.warn("Vendor profile not found. uuid=[{}]", normalizedUuid);

                        redirectAttributes.addFlashAttribute(
                                "errorMessage",
                                "Vendor profile was not found."
                        );
                        return "redirect:/adminvendor/list";
                    });

        } catch (Exception ex) {
            log.error("Unable to load vendor profile. uuid=[{}]", normalizedUuid, ex);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to load vendor profile. Please try again."
            );
            return "redirect:/adminvendor/list";
        }
    }

    @GetMapping("/delete/{uuid}")
    public String delete(
            @PathVariable("uuid") String uuid,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (uuid == null || uuid.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile UUID is required.");
                return "redirect:/adminvendor/list";
            }

            if (adminVendorManagementService.findByUuid(uuid).isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile was not found.");
                return "redirect:/adminvendor/list";
            }

            adminVendorManagementService.deleteByUuid(uuid);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Vendor profile deleted successfully."
            );

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Vendor profile could not be deleted because it may be used by other records."
            );
        }

        return "redirect:/adminvendor/list";
    }

    private void prepareEditModel(Model model, AdminVendorProfileForm vendorForm) {
        model.addAttribute("vendorForm", vendorForm);
        model.addAttribute("vendor_status", VendorStatusEnum.values());
        model.addAttribute("pageTitle", "Edit Vendor Profile");
    }

    private String redirectAfterSaveFailure(AdminVendorProfileForm vendorForm) {
        if (vendorForm != null
                && vendorForm.getUuid() != null
                && !vendorForm.getUuid().trim().isEmpty()) {

            return "redirect:/adminvendor/edit/" + vendorForm.getUuid().trim();
        }

        return "redirect:/adminvendor/list";
    }
}
