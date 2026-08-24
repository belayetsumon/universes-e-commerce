package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Role;
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
@RequestMapping("/role")
@PreAuthorize(PlatformIamPermissions.CAN_READ)
public class RoleController {

    private final IamAdministrationService iamAdministrationService;

    public RoleController(IamAdministrationService iamAdministrationService) {
        this.iamAdministrationService = iamAdministrationService;
    }

    @GetMapping(value = {"", "/", "/index"})
    public String index(Model model, Role role) {
        populatePage(model);
        return "user/role";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("role", iamAdministrationService.findRole(id));
            populatePage(model);
            return "user/role";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/role/index";
        }
    }

    @PostMapping("/save")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE)
    public String save(
            Model model,
            @Valid Role role,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populatePage(model);
            return "user/role";
        }

        try {
            iamAdministrationService.saveRole(role);
            redirectAttributes.addFlashAttribute("success", "Role saved successfully.");
            return "redirect:/role/index";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("role.save", exception.getMessage());
            populatePage(model);
            return "user/role";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            iamAdministrationService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/role/index";
    }

    private void populatePage(Model model) {
        model.addAttribute("list", iamAdministrationService.findAllRoles());
        model.addAttribute("privilegelist", iamAdministrationService.findAllPrivileges());
        model.addAttribute("modulelist", iamAdministrationService.findAllModules());
    }
}
