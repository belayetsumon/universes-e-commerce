package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.model.ShippingProfile.ProfileType;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.ShippingProfileService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor/shipping-profile")
@PreAuthorize("""
        @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.shipping.manage')
        or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
        or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
        or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
        """)
public class VendorShippingProfileController {

    private final VendorUserContext vendorUserContext;
    private final ShippingProfileService shippingProfileService;
    private final CarrierService carrierService;
    private final ShippingLocationService locationService;

    public VendorShippingProfileController(
            VendorUserContext vendorUserContext,
            ShippingProfileService shippingProfileService,
            CarrierService carrierService,
            ShippingLocationService locationService) {
        this.vendorUserContext = vendorUserContext;
        this.shippingProfileService = shippingProfileService;
        this.carrierService = carrierService;
        this.locationService = locationService;
    }

    @GetMapping
    public String edit(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        if (vendor == null || vendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        ShippingProfile profile = shippingProfileService.getByVendor(vendor.getId());
        if (profile == null) {
            profile = new ShippingProfile();
            profile.setVendorId(vendor.getId());
            profile.setName(vendor.getCompanyName() + " Shipping Profile");
            profile.setType(ProfileType.Shipping_From);
            profile.setActive(true);
        }

        populateForm(model, profile, vendor);
        return "vendor/shipping/profile";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("profile") ShippingProfile profile,
            BindingResult result,
            @RequestParam(value = "allowedCarrierIds", required = false) List<Long> allowedCarrierIds,
            @RequestParam(value = "allowedLocationIds", required = false) List<Long> allowedLocationIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        if (vendor == null || vendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        if (result.hasErrors()) {
            populateForm(model, profile, vendor);
            return "vendor/shipping/profile";
        }

        try {
            shippingProfileService.saveVendorProfile(vendor.getId(), profile, allowedCarrierIds, allowedLocationIds);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping profile saved successfully.");
            return "redirect:/vendor/shipping-profile";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateForm(model, profile, vendor);
            return "vendor/shipping/profile";
        }
    }

    private void populateForm(Model model, ShippingProfile profile, Vendorprofile vendor) {
        profile.setVendorId(vendor.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("activeVendor", vendor);
        model.addAttribute("types", ProfileType.values());
        model.addAttribute("carriers", carrierService.getAll());
        model.addAttribute("allLocations", locationService.getActiveLocations());
        model.addAttribute("selectedCarrierIds", profile.getAllowedCarriers() == null
                ? List.of()
                : profile.getAllowedCarriers().stream().map(carrier -> carrier.getId()).toList());
        model.addAttribute("selectedLocationIds", profile.getAllowedDistricts() == null
                ? List.of()
                : profile.getAllowedDistricts().stream().map(location -> location.getId()).toList());
    }
}
