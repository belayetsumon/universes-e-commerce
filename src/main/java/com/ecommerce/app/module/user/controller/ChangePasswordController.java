/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("/changepassword")
@PreAuthorize("denyAll()")
public class ChangePasswordController {

    @GetMapping(value = {"", "/", "/index", "/changepassword"})
    public String disabledLegacyPasswordPage() {
        throw disabledLegacyPasswordFlow();
    }

    @PostMapping("/update")
    public String disabledLegacyPasswordUpdate() {
        throw disabledLegacyPasswordFlow();
    }

    private ResponseStatusException disabledLegacyPasswordFlow() {
        return new ResponseStatusException(HttpStatus.GONE, "The legacy password-change flow is disabled.");
    }
}
