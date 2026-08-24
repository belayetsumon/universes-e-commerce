package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Modules;
import com.ecommerce.app.module.user.services.IamAdministrationService;
import com.ecommerce.app.security.permission.PlatformIamPermissions;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/module")
@PreAuthorize(PlatformIamPermissions.CAN_READ)
public class ModuleController {

    private final IamAdministrationService iamAdministrationService;

    public ModuleController(IamAdministrationService iamAdministrationService) {
        this.iamAdministrationService = iamAdministrationService;
    }

    @GetMapping(value = {"", "/", "/index"})
    public String index(Model model, Modules modules) {
        populatePage(model);
        return "user/module";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("modules", iamAdministrationService.findModule(id));
            populatePage(model);
            return "user/module";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/module/index";
        }
    }

    @PostMapping("/save")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public String save(
            Model model,
            @Valid Modules modules,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populatePage(model);
            return "user/module";
        }

        try {
            iamAdministrationService.updateModuleDisplayName(modules);
            redirectAttributes.addFlashAttribute("success", "Module display name updated.");
            return "redirect:/module/index";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("module.save", exception.getMessage());
            populatePage(model);
            return "user/module";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Permission modules are protected catalogue entries and cannot be deleted through the application.");
        return "redirect:/module/index";
    }

    private void populatePage(Model model) {
        model.addAttribute("list", iamAdministrationService.findAllModules());
    }
}
