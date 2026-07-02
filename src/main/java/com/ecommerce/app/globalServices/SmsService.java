package com.ecommerce.app.globalServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${sms.api.url:}")
    private String smsApiUrl;

    @Value("${sms.api.key:}")
    private String apiKey;

    public void sendSms(String mobile, String message) {
        if (smsApiUrl == null || smsApiUrl.isBlank()) {
            System.out.printf("[SMS disabled] Missing sms.api.url. To %s: %s%n", mobile, message);
            return;
        }

        System.out.printf("[SMS] Sending to %s: %s%n", mobile, message);
    }
}
