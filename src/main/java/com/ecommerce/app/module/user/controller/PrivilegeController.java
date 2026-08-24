package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Privilege;
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
@RequestMapping("/privilege")
@PreAuthorize(PlatformIamPermissions.CAN_READ)
public class PrivilegeController {

    private final IamAdministrationService iamAdministrationService;

    public PrivilegeController(IamAdministrationService iamAdministrationService) {
        this.iamAdministrationService = iamAdministrationService;
    }

    @GetMapping(value = {"", "/", "/index"})
    public String index(Model model, Privilege privilege) {
        populatePage(model);
        return "user/privilege";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("privilege", iamAdministrationService.findPrivilege(id));
            populatePage(model);
            return "user/privilege";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/privilege/index";
        }
    }

    @PostMapping("/save")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public String save(
            Model model,
            @Valid Privilege privilege,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populatePage(model);
            return "user/privilege";
        }

        try {
            iamAdministrationService.updatePrivilegeDisplayName(privilege);
            redirectAttributes.addFlashAttribute("success", "Permission display name updated.");
            return "redirect:/privilege/index";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("privilege.save", exception.getMessage());
            populatePage(model);
            return "user/privilege";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Permissions are protected catalogue entries and cannot be deleted through the application.");
        return "redirect:/privilege/index";
    }

    private void populatePage(Model model) {
        model.addAttribute("list", iamAdministrationService.findAllPrivileges());
        model.addAttribute("modulelist", iamAdministrationService.findAllModules());
    }
}
