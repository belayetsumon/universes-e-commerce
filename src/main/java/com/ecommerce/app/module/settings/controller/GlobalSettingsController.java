package com.ecommerce.app.module.settings.controller;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.module.settings.services.GlobalSettingsService.SettingsOperationException;
import com.ecommerce.app.module.settings.services.GlobalSettingsService.SettingsValidationException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class GlobalSettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsController.class);
    private static final String VIEW = "admin/settings/global-settings";

    private static final String REDIRECT = "redirect:/admin/settings/index";
    private static final String SETTINGS_ATTRIBUTE = "settings";
    private static final String SUCCESS_ATTRIBUTE = "successMessage";
    private static final String ERROR_ATTRIBUTE = "errorMessage";

    private final GlobalSettingsService globalSettingsService;

    @Autowired
    public GlobalSettingsController(GlobalSettingsService globalSettingsService) {
        this.globalSettingsService = globalSettingsService;
    }

    @GetMapping("/index")
    public String settingsPage(Model model) {
        if (!model.containsAttribute(SETTINGS_ATTRIBUTE)) {
            model.addAttribute(SETTINGS_ATTRIBUTE, loadSettingsForView(model));
        }
        return VIEW;
    }

    @GetMapping("/update")
    public String updatePageFallback(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, "Use the settings form to save changes.");
        return REDIRECT;
    }

    @PostMapping("/update")
    public String updateSettings(
            @Valid @ModelAttribute(SETTINGS_ATTRIBUTE) GlobalSettings settings,
            BindingResult bindingResult,
            @RequestParam(name = "siteLogoFile", required = false) MultipartFile siteLogoFile,
            @RequestParam(name = "faviconFile", required = false) MultipartFile faviconFile,
            @RequestParam(name = "ogImageFile", required = false) MultipartFile ogImageFile,
            RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("Global settings update request received");
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, "Please review the highlighted settings and try again.");
            redirectAttributes.addFlashAttribute(SETTINGS_ATTRIBUTE, settings);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult." + SETTINGS_ATTRIBUTE,
                    bindingResult
            );
            return REDIRECT;
        }

        try {
            globalSettingsService.updateSettings(settings, siteLogoFile, faviconFile, ogImageFile);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, "Global settings updated successfully.");
        } catch (SettingsValidationException ex) {
            LOGGER.warn("Settings update rejected by service validation: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, String.join(" ", ex.getErrors()));
        } catch (OptimisticLockingFailureException ex) {
            LOGGER.warn("Settings update rejected because the submitted version is stale", ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (SettingsOperationException ex) {
            LOGGER.error("Failed to update global settings", ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (RuntimeException ex) {
            String reference = errorReference();
            LOGGER.error("Unexpected global settings update error. reference={}", reference, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE,
                    "Settings could not be saved. Error reference: " + reference + ".");
        }

        return REDIRECT;
    }

    @PostMapping("/images/delete")
    public String deleteImage(
            @RequestParam("type") GlobalSettingsService.SettingsImageType imageType,
            RedirectAttributes redirectAttributes
    ) {
        try {
            globalSettingsService.deleteImage(imageType);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, imageType.getLabel() + " deleted successfully.");
        } catch (SettingsValidationException | SettingsOperationException ex) {
            LOGGER.error("Failed to delete settings image", ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (RuntimeException ex) {
            String reference = errorReference();
            LOGGER.error("Unexpected settings image delete error. reference={}", reference, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE,
                    "Image could not be deleted. Error reference: " + reference + ".");
        }

        return REDIRECT + "#images";
    }

    @PostMapping("/basic")
    public String updateBasic(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.BASIC, "Basic", redirectAttributes);
    }

    @PostMapping("/seo")
    public String updateSeo(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.SEO, "SEO", redirectAttributes);
    }

    @PostMapping("/store")
    public String updateStore(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.STORE, "Store", redirectAttributes);
    }

    @PostMapping("/payment")
    public String updatePayment(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.PAYMENT, "Payment", redirectAttributes);
    }

    @PostMapping("/delivery")
    public String updateDelivery(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.DELIVERY, "Delivery", redirectAttributes);
    }

    @PostMapping("/order")
    public String updateOrder(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.ORDER, "Order", redirectAttributes);
    }

    @PostMapping("/social")
    public String updateSocial(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.SOCIAL, "Social", redirectAttributes);
    }

    @PostMapping("/policy")
    public String updatePolicy(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.POLICY, "Policy", redirectAttributes);
    }

    @PostMapping("/maintenance")
    public String updateMaintenance(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.MAINTENANCE, "Maintenance", redirectAttributes);
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedError(Exception ex, Model model) {
        String reference = errorReference();
        LOGGER.error("Unexpected settings page error. reference={}", reference, ex);
        model.addAttribute(SETTINGS_ATTRIBUTE, globalSettingsService.defaultSettingsForForm());
        model.addAttribute(ERROR_ATTRIBUTE, "Settings page could not complete the request. Error reference: " + reference + ".");
        return VIEW;
    }

    private String updateSettingsSection(
            GlobalSettings settings,
            GlobalSettingsService.SettingsSection section,
            String sectionName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            globalSettingsService.updateSettingsField(settings, section);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, sectionName + " settings updated successfully.");
        } catch (SettingsValidationException | SettingsOperationException | OptimisticLockingFailureException ex) {
            LOGGER.error("Failed to update {} settings", sectionName, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (RuntimeException ex) {
            String reference = errorReference();
            LOGGER.error("Unexpected {} settings update error. reference={}", sectionName, reference, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE,
                    sectionName + " settings could not be saved. Error reference: " + reference + ".");
        }
        return REDIRECT + "#" + section.getFragment();
    }

    private GlobalSettings loadSettingsForView(Model model) {
        try {
            return globalSettingsService.getActiveSettings();
        } catch (Exception ex) {
            LOGGER.error("Failed to load global settings", ex);
            model.addAttribute(ERROR_ATTRIBUTE, uiMessage("Settings could not be loaded", ex));
            return globalSettingsService.defaultSettingsForForm();
        }
    }

    private String uiMessage(String prefix, Exception ex) {
        String detail = ex.getMessage();
        if (detail == null || detail.isBlank()) {
            return prefix + ".";
        }
        return prefix + ": " + detail;
    }

    private String errorReference() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
