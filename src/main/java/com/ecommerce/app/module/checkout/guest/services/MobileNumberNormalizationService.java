package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.support.BangladeshMobileNumbers;
import org.springframework.stereotype.Service;

@Service
public class MobileNumberNormalizationService {

    public String normalizeBangladeshMobile(String rawMobile) {
        return BangladeshMobileNumbers.normalize(rawMobile);
    }

    public String toLocalDisplay(String normalizedMobile) {
        if (BangladeshMobileNumbers.isNormalized(normalizedMobile)) {
            return "0" + normalizedMobile.substring(3);
        }
        return normalizedMobile;
    }

    public String mask(String normalizedMobile) {
        if (normalizedMobile == null || normalizedMobile.length() < 7) {
            return "01*********";
        }
        String local = toLocalDisplay(normalizedMobile);
        if (local == null || local.length() < 7) {
            return "01*********";
        }
        return local.substring(0, 3) + "*****" + local.substring(local.length() - 3);
    }
}
