/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.MessageInterpolator.Context;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

/**
 *
 * @author libertyerp_local
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

//    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
//        try {
//            Context context = new Context(); // ✅ correct way
//            context.setVariables(variables);
//
//            String htmlContent = templateEngine.process(templateName, context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
//            helper.setFrom("no-reply@yourdomain.com");
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//        } catch (Exception e) {
//            e.printStackTrace();
//            // Handle error appropriately
//        }
//    }
}
