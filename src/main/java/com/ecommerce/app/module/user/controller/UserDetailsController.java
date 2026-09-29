/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.security.permission.PlatformIdentityPermissions;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("userdetails")
//@PreAuthorize("hasAuthority('userdetails')")
public class UserDetailsController {

   @GetMapping(value = {"","/", "/index"})
   @PreAuthorize(PlatformIdentityPermissions.CAN_READ_USERS)
    public String page(Model model) {
        model.addAttribute("attribute", "value");
        return "pims/userdetails/index";
    }

}
