/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("/forgotpassword")

public class ForgotPasswordController {

//    @Autowired
//    private JavaMailSender sender;
    @Autowired
    UsersRepository usersRepository;

    @RequestMapping(value = {"", "/", "/index", "/userforgotpassword"})
    public String userforgotpassword(Model model) {
        model.addAttribute("attribute", "value");
        return "user/forgotpassword";
    }

    @RequestMapping("/showemail")
    public String showemail(@RequestParam(required = false, name = "email") String email,
            @RequestParam(required = false, name = "source") String source,
            Model model) {

        boolean publicRequest = "public".equalsIgnoreCase(source);
        if (email == null || email.isBlank()) {
            model.addAttribute("emailNotFound", "Please provide your registered email address.");
            return publicRequest ? "frontview/forgot-password" : "user/forgotpassword";
        }

        Users user = usersRepository.findByEmail(email.trim()).orElse(null);

        if (user == null) {

            model.addAttribute("emailNotFound", "This email is not exist.");

            return publicRequest ? "frontview/forgot-password" : "user/forgotpassword";
        }

        if (publicRequest) {
            model.addAttribute("success", "If this email is registered, recovery instructions will be shared through the configured account channel.");
            return "frontview/forgot-password";
        }

        model.addAttribute("user", "Hello Mr " + user.getFirstName() + " " + user.getLastName() + "Your password has been sent successfully! Please check your email. <br>");
        return "user/showemail";
    }

//    @RequestMapping("/simpleemail")
//    @ResponseBody
//    String home() {
//        try {
//            sendEmail();
//            return "Email Sent!";
//        } catch (Exception ex) {
//            return "Error in sending email: " + ex;
//        }
//    }
//    private void sendEmail() throws Exception {
//        MimeMessage message = sender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message);
//        helper.setTo("set-your-recipient-email-here@gmail.com");
//        helper.setText("How are you?");
//        helper.setSubject("Hi");
//        sender.send(message);
//
//    }
}
