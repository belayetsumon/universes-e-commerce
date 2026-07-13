package com.ecommerce.app.module.settings.controller;

import com.ecommerce.app.module.settings.form.BasicSiteSettingsForm;
import com.ecommerce.app.module.settings.form.DeliverySettingsForm;
import com.ecommerce.app.module.settings.form.MaintenanceSettingsForm;
import com.ecommerce.app.module.settings.form.OrderSettingsForm;
import com.ecommerce.app.module.settings.form.PaymentSettingsForm;
import com.ecommerce.app.module.settings.form.PolicySettingsForm;
import com.ecommerce.app.module.settings.form.SeoSettingsForm;
import com.ecommerce.app.module.settings.form.SocialSettingsForm;
import com.ecommerce.app.module.settings.form.StoreSettingsForm;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.module.settings.services.GlobalSettingsService.SettingsImageType;
import com.ecommerce.app.module.settings.services.GlobalSettingsService.SettingsOperationException;
import com.ecommerce.app.module.settings.services.GlobalSettingsService.SettingsValidationException;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class GlobalSettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsController.class);
    private static final String VIEW = "admin/settings/global-settings";
    private static final String REDIRECT = "redirect:/admin/settings/index";

    private static final String SETTINGS_ATTRIBUTE = "settings";
    private static final String BASIC_FORM_ATTRIBUTE = "basicSiteSettingsForm";
    private static final String SEO_FORM_ATTRIBUTE = "seoSettingsForm";
    private static final String STORE_FORM_ATTRIBUTE = "storeSettingsForm";
    private static final String PAYMENT_FORM_ATTRIBUTE = "paymentSettingsForm";
    private static final String DELIVERY_FORM_ATTRIBUTE = "deliverySettingsForm";
    private static final String ORDER_FORM_ATTRIBUTE = "orderSettingsForm";
    private static final String SOCIAL_FORM_ATTRIBUTE = "socialSettingsForm";
    private static final String POLICY_FORM_ATTRIBUTE = "policySettingsForm";
    private static final String MAINTENANCE_FORM_ATTRIBUTE = "maintenanceSettingsForm";
    private static final String SUCCESS_ATTRIBUTE = "successMessage";
    private static final String ERROR_ATTRIBUTE = "errorMessage";
    private static final String ACTIVE_SECTION_ATTRIBUTE = "activeSettingsSection";

    private final GlobalSettingsService globalSettingsService;
    private final VendorprofileRepository vendorprofileRepository;

    public GlobalSettingsController(
            GlobalSettingsService globalSettingsService,
            VendorprofileRepository vendorprofileRepository
    ) {
        this.globalSettingsService = globalSettingsService;
        this.vendorprofileRepository = vendorprofileRepository;
    }

    @GetMapping("/index")
    public String settingsPage(Model model) {
        Object existingSettings = model.asMap().get(SETTINGS_ATTRIBUTE);
        GlobalSettings settings = existingSettings instanceof GlobalSettings globalSettings
                ? globalSettings
                : loadSettingsForView(model);
        model.addAttribute(SETTINGS_ATTRIBUTE, settings);
        addSectionForms(model, settings);
        return VIEW;
    }

    @GetMapping("/update")
    public String updatePageFallback(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, "Use a section save button to update settings.");
        return REDIRECT;
    }

    @GetMapping({"/basic", "/seo", "/store", "/payment", "/delivery", "/order", "/social", "/policy", "/maintenance"})
    public String sectionPageFallback(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String sectionId = resolveSectionId(request);
        redirectAttributes.addFlashAttribute(ACTIVE_SECTION_ATTRIBUTE, sectionId);
        return REDIRECT + "#" + sectionId;
    }

    @PostMapping("/update")
    public String updateSettingsFallback(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, "The full settings save action is no longer available. Please save one commented section at a time.");
        return REDIRECT;
    }

    @PostMapping("/basic")
    public String updateBasic(
            @Valid @ModelAttribute(BASIC_FORM_ATTRIBUTE) BasicSiteSettingsForm form,
            BindingResult bindingResult,
            @RequestParam(name = "siteLogoFile", required = false) MultipartFile siteLogoFile,
            @RequestParam(name = "faviconFile", required = false) MultipartFile faviconFile,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Basic site", "basic", BASIC_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Basic site", "basic", BASIC_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateBasicSiteSettings(form, siteLogoFile, faviconFile));
    }

    @PostMapping("/seo")
    public String updateSeo(
            @Valid @ModelAttribute(SEO_FORM_ATTRIBUTE) SeoSettingsForm form,
            BindingResult bindingResult,
            @RequestParam(name = "ogImageFile", required = false) MultipartFile ogImageFile,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("SEO", "seo", SEO_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("SEO", "seo", SEO_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateSeoSettings(form, ogImageFile));
    }

    @PostMapping("/store")
    public String updateStore(
            @Valid @ModelAttribute(STORE_FORM_ATTRIBUTE) StoreSettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Store", "store", STORE_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Store", "store", STORE_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateStoreSettings(form));
    }

    @PostMapping("/payment")
    public String updatePayment(
            @ModelAttribute(PAYMENT_FORM_ATTRIBUTE) PaymentSettingsForm form,
            RedirectAttributes redirectAttributes
    ) {
        return saveSection("Payment", "payment", PAYMENT_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updatePaymentSettings(form));
    }

    @PostMapping("/delivery")
    public String updateDelivery(
            @Valid @ModelAttribute(DELIVERY_FORM_ATTRIBUTE) DeliverySettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Delivery", "delivery", DELIVERY_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Delivery", "delivery", DELIVERY_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateDeliverySettings(form));
    }

    @PostMapping("/order")
    public String updateOrder(
            @Valid @ModelAttribute(ORDER_FORM_ATTRIBUTE) OrderSettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Order", "order", ORDER_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Order", "order", ORDER_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateOrderSettings(form));
    }

    @PostMapping("/social")
    public String updateSocial(
            @Valid @ModelAttribute(SOCIAL_FORM_ATTRIBUTE) SocialSettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Social", "social", SOCIAL_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Social", "social", SOCIAL_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateSocialSettings(form));
    }

    @PostMapping("/policy")
    public String updatePolicy(
            @ModelAttribute(POLICY_FORM_ATTRIBUTE) PolicySettingsForm form,
            RedirectAttributes redirectAttributes
    ) {
        return saveSection("Policy", "policy", POLICY_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updatePolicySettings(form));
    }

    @PostMapping("/maintenance")
    public String updateMaintenance(
            @Valid @ModelAttribute(MAINTENANCE_FORM_ATTRIBUTE) MaintenanceSettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return validationRedirect("Maintenance", "maintenance", MAINTENANCE_FORM_ATTRIBUTE, form, bindingResult, redirectAttributes);
        }
        return saveSection("Maintenance", "maintenance", MAINTENANCE_FORM_ATTRIBUTE, form, redirectAttributes,
                () -> globalSettingsService.updateMaintenanceSettings(form));
    }

    @PostMapping("/images/delete")
    public String deleteImage(
            @RequestParam("type") SettingsImageType imageType,
            RedirectAttributes redirectAttributes
    ) {
        String sectionId = imageType == SettingsImageType.OG_IMAGE ? "seo" : "basic";
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
        redirectAttributes.addFlashAttribute(ACTIVE_SECTION_ATTRIBUTE, sectionId);
        return REDIRECT + "#" + sectionId;
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedError(Exception ex, Model model) {
        String reference = errorReference();
        LOGGER.error("Unexpected settings page error. reference={}", reference, ex);
        GlobalSettings settings = globalSettingsService.defaultSettingsForForm();
        model.addAttribute(SETTINGS_ATTRIBUTE, settings);
        addSectionForms(model, settings);
        model.addAttribute(ERROR_ATTRIBUTE, "Settings page could not complete the request. Error reference: " + reference + ".");
        return VIEW;
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public String handleUploadError(Exception ex, HttpServletRequest request, Model model) {
        String sectionId = resolveSectionId(request);
        LOGGER.warn("Settings upload rejected. section={}, uri={}", sectionId, request.getRequestURI(), ex);
        GlobalSettings settings = loadSettingsForView(model);
        model.addAttribute(SETTINGS_ATTRIBUTE, settings);
        addSectionForms(model, settings);
        model.addAttribute(ACTIVE_SECTION_ATTRIBUTE, sectionId);
        model.addAttribute(ERROR_ATTRIBUTE,
                settingsUploadErrorMessage(sectionId) + " Please upload a JPG, PNG, or WEBP image within the configured upload size limit.");
        return VIEW;
    }

    private String saveSection(
            String sectionName,
            String sectionId,
            String formAttribute,
            Object form,
            RedirectAttributes redirectAttributes,
            SettingsUpdateAction action
    ) {
        try {
            action.update();
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, sectionName + " settings updated successfully.");
        } catch (SettingsValidationException ex) {
            LOGGER.warn("{} settings validation failed: {}", sectionName, ex.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, sectionName + " settings could not be saved: " + String.join(" ", ex.getErrors()));
            redirectAttributes.addFlashAttribute(formAttribute, form);
        } catch (OptimisticLockingFailureException ex) {
            LOGGER.warn("{} settings update rejected because the submitted version is stale", sectionName, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (SettingsOperationException ex) {
            LOGGER.error("Failed to update {} settings", sectionName, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());
        } catch (RuntimeException ex) {
            String reference = errorReference();
            LOGGER.error("Unexpected {} settings update error. reference={}", sectionName, reference, ex);
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE,
                    sectionName + " settings could not be saved. Error reference: " + reference + ".");
        }
        redirectAttributes.addFlashAttribute(ACTIVE_SECTION_ATTRIBUTE, sectionId);
        return REDIRECT + "#" + sectionId;
    }

    private String validationRedirect(
            String sectionName,
            String sectionId,
            String formAttribute,
            Object form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        LOGGER.warn("{} settings rejected by controller validation: {}", sectionName, bindingResult.getAllErrors());
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, sectionName + " settings could not be saved. Please review the highlighted fields.");
        redirectAttributes.addFlashAttribute(formAttribute, form);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + formAttribute, bindingResult);
        redirectAttributes.addFlashAttribute(ACTIVE_SECTION_ATTRIBUTE, sectionId);
        return REDIRECT + "#" + sectionId;
    }

    private void addSectionForms(Model model, GlobalSettings settings) {
        model.addAttribute("vendorProfiles", vendorprofileRepository.findAll(Sort.by(Sort.Direction.ASC, "companyName")));
        if (!model.containsAttribute(BASIC_FORM_ATTRIBUTE)) {
            model.addAttribute(BASIC_FORM_ATTRIBUTE, BasicSiteSettingsForm.from(settings));
        }
        if (!model.containsAttribute(SEO_FORM_ATTRIBUTE)) {
            model.addAttribute(SEO_FORM_ATTRIBUTE, SeoSettingsForm.from(settings));
        }
        if (!model.containsAttribute(STORE_FORM_ATTRIBUTE)) {
            model.addAttribute(STORE_FORM_ATTRIBUTE, StoreSettingsForm.from(settings));
        }
        if (!model.containsAttribute(PAYMENT_FORM_ATTRIBUTE)) {
            model.addAttribute(PAYMENT_FORM_ATTRIBUTE, PaymentSettingsForm.from(settings));
        }
        if (!model.containsAttribute(DELIVERY_FORM_ATTRIBUTE)) {
            model.addAttribute(DELIVERY_FORM_ATTRIBUTE, DeliverySettingsForm.from(settings));
        }
        if (!model.containsAttribute(ORDER_FORM_ATTRIBUTE)) {
            model.addAttribute(ORDER_FORM_ATTRIBUTE, OrderSettingsForm.from(settings));
        }
        if (!model.containsAttribute(SOCIAL_FORM_ATTRIBUTE)) {
            model.addAttribute(SOCIAL_FORM_ATTRIBUTE, SocialSettingsForm.from(settings));
        }
        if (!model.containsAttribute(POLICY_FORM_ATTRIBUTE)) {
            model.addAttribute(POLICY_FORM_ATTRIBUTE, PolicySettingsForm.from(settings));
        }
        if (!model.containsAttribute(MAINTENANCE_FORM_ATTRIBUTE)) {
            model.addAttribute(MAINTENANCE_FORM_ATTRIBUTE, MaintenanceSettingsForm.from(settings));
        }
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

    private String resolveSectionId(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null) {
            return "basic";
        }
        String uri = request.getRequestURI().toLowerCase();
        if (uri.endsWith("/seo")) {
            return "seo";
        }
        if (uri.endsWith("/store")) {
            return "store";
        }
        if (uri.endsWith("/payment")) {
            return "payment";
        }
        if (uri.endsWith("/delivery")) {
            return "delivery";
        }
        if (uri.endsWith("/order")) {
            return "order";
        }
        if (uri.endsWith("/social")) {
            return "social";
        }
        if (uri.endsWith("/policy")) {
            return "policy";
        }
        if (uri.endsWith("/maintenance")) {
            return "maintenance";
        }
        return "basic";
    }

    private String settingsUploadErrorMessage(String sectionId) {
        if ("seo".equals(sectionId)) {
            return "SEO settings could not be saved because the OG image upload was rejected.";
        }
        if ("basic".equals(sectionId)) {
            return "Basic site settings could not be saved because the image upload was rejected.";
        }
        return "Settings could not be saved because the upload was rejected.";
    }

    @FunctionalInterface
    private interface SettingsUpdateAction {

        void update();
    }
}
