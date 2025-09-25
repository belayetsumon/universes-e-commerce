/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.globalServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class SmsService {

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.api.key}")
    private String apiKey;

    public void sendSms(String mobile, String message) {
        // Dummy HTTP POST request (use RestTemplate or WebClient in production)
        System.out.printf("[SMS] Sending to %s: %s%n", mobile, message);
    }
}
