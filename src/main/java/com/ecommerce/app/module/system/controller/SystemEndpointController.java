/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.system.controller;

import com.ecommerce.app.module.system.services.EndpointScannerService;
import com.ecommerce.app.module.system.services.BangladeshLocationSeedService;
import com.ecommerce.app.module.system.services.PermissionCatalogueSeedService;
import com.ecommerce.app.security.permission.PlatformIamPermissions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/system")
@PreAuthorize(PlatformIamPermissions.CAN_READ)
public class SystemEndpointController {

    private final EndpointScannerService endpointScannerService;
    private final BangladeshLocationSeedService bangladeshLocationSeedService;
    private final PermissionCatalogueSeedService permissionCatalogueSeedService;

    public SystemEndpointController(EndpointScannerService endpointScannerService,
            BangladeshLocationSeedService bangladeshLocationSeedService,
            PermissionCatalogueSeedService permissionCatalogueSeedService) {
        this.endpointScannerService = endpointScannerService;
        this.bangladeshLocationSeedService = bangladeshLocationSeedService;
        this.permissionCatalogueSeedService = permissionCatalogueSeedService;
    }

    @GetMapping({"", "/"})
    public String systemIndex() {
        return "admin/system/index";
    }

    @GetMapping("/endpoints")
    public String endpoints(Model model) {
        model.addAttribute("endpoints", endpointScannerService.getAllEndpoints());
        return "admin/system/endpoints";
    }

    @PostMapping("/seed/bangladesh-locations")
    public String seedBangladeshLocations(
            @RequestParam(name = "returnTo", required = false) String returnTo,
            RedirectAttributes redirectAttributes) {
        BangladeshLocationSeedService.SeedResult result = bangladeshLocationSeedService.seedBangladeshLocations();
        redirectAttributes.addFlashAttribute("successMessage",
                result.created() + " Bangladesh location records added; "
                + result.skipped() + " existing records kept.");
        return "shipping-locations".equals(returnTo)
                ? "redirect:/admin/shipping-locations/list"
                : "redirect:/admin/system";
    }

    @GetMapping("/seed/permission-catalogue")
    public String seedPermissionCatalogueInfo(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "Use the Install all permissions button to update the permission catalogue.");
        return "redirect:/admin/system";
    }

    @PostMapping("/seed/permission-catalogue")
    @PreAuthorize(PlatformIamPermissions.CAN_SEED_PERMISSION_CATALOGUE)
    public String seedPermissionCatalogue(RedirectAttributes redirectAttributes) {
        try {
            PermissionCatalogueSeedService.SeedResult result = permissionCatalogueSeedService.seedPermissionCatalogue();
            redirectAttributes.addFlashAttribute("successMessage",
                    result.permissionsCreated() + " permissions added; "
                    + result.permissionsExisting() + " permissions already existed. "
                    + result.modulesCreated() + " modules added; "
                    + result.modulesExisting() + " modules already existed.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/system";
    }

}
