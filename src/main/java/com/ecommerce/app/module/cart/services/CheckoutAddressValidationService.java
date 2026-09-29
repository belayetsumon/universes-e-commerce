package com.ecommerce.app.module.cart.services;

import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Validates the server-owned checkout address snapshot. Entity objects are
 * deliberately not used as web validation forms because their persistence
 * constraints (for example, the billing-address user relation) are different
 * from the fields a customer is allowed to submit.
 */
@Service
public class CheckoutAddressValidationService {

    private static final Pattern OPTIONAL_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String BANGLADESH = "Bangladesh";

    private final MobileNumberNormalizationService mobileNumberService;

    public CheckoutAddressValidationService(MobileNumberNormalizationService mobileNumberService) {
        this.mobileNumberService = mobileNumberService;
    }

    public String validateBillingAddress(BillingAddress address) {
        if (address == null) {
            return "Enter a billing address before placing the order.";
        }
        return validateContactAddress(
                address.getFirstName(),
                address.getLastName(),
                address.getEmail(),
                address.getMobile(),
                address.getCompany(),
                address.getAddressLineOne(),
                address.getAddressLinetwo(),
                address.getCity(),
                address.getDistrict(),
                address.getCountry(),
                address.getPostCode(),
                "billing"
        );
    }

    public String validateShippingAddress(ShippingAddress address, ShippingLocation location) {
        if (address == null) {
            return "Enter a delivery address before placing the order.";
        }
        String contactError = validateContactAddress(
                address.getFirstName(),
                address.getLastName(),
                address.getEmail(),
                address.getMobile(),
                address.getCompany(),
                address.getAddressLineOne(),
                address.getAddressLinetwo(),
                address.getCity(),
                address.getDistrict(),
                address.getCountry(),
                address.getPostCode(),
                "delivery"
        );
        if (contactError != null) {
            return contactError;
        }
        if (location == null || !location.isActive()
                || blank(location.getName()) || blank(location.getDisplayLabel())) {
            return "Select a current delivery location before placing the order.";
        }
        if (!sameText(BANGLADESH, address.getCountry())
                || !sameText(location.getDisplayLabel(), address.getDistrict())
                || !sameText(location.getName(), address.getCity())) {
            return "The delivery address no longer matches the selected location. Please enter it again.";
        }
        return null;
    }

    public String normalizeMobileForStorage(String rawMobile) {
        return mobileNumberService.normalizeBangladeshMobile(rawMobile);
    }

    public boolean sameNormalizedMobile(String first, String second) {
        try {
            return normalizeMobileForStorage(first).equals(normalizeMobileForStorage(second));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String validateContactAddress(
            String firstName,
            String lastName,
            String email,
            String mobile,
            String company,
            String addressLineOne,
            String addressLineTwo,
            String city,
            String district,
            String country,
            String postCode,
            String addressType
    ) {
        if (blank(firstName) || firstName.trim().length() > 120) {
            return "Enter a valid recipient name for the " + addressType + " address.";
        }
        if (blank(addressLineOne) || addressLineOne.trim().length() > 255) {
            return "Enter a valid street address for the " + addressType + " address.";
        }
        try {
            normalizeMobileForStorage(mobile);
        } catch (IllegalArgumentException ex) {
            return "Enter a valid Bangladesh mobile number for the " + addressType + " address.";
        }
        if (tooLong(lastName, 120)
                || tooLong(company, 160)
                || tooLong(addressLineTwo, 255)
                || tooLong(city, 120)
                || tooLong(district, 160)
                || tooLong(country, 80)
                || tooLong(postCode, 40)) {
            return "One or more " + addressType + " address fields are too long.";
        }
        String cleanEmail = clean(email);
        if (cleanEmail != null
                && (cleanEmail.length() > 254 || !OPTIONAL_EMAIL.matcher(cleanEmail).matches())) {
            return "Enter a valid email address or leave it blank.";
        }
        return null;
    }

    private boolean tooLong(String value, int maximumLength) {
        String cleaned = clean(value);
        return cleaned != null && cleaned.length() > maximumLength;
    }

    private boolean sameText(String expected, String actual) {
        String left = canonicalText(expected);
        String right = canonicalText(actual);
        return left != null && left.equals(right);
    }

    private String canonicalText(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private boolean blank(String value) {
        return clean(value) == null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
