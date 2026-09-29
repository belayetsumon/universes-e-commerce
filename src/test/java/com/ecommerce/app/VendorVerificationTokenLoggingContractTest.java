package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VendorVerificationTokenLoggingContractTest {

    @Test
    void vendorVerificationLogsDoNotContainRawTokensOtpOrRecipients() throws IOException {
        String service = Files.readString(Path.of("src/main/java/com/ecommerce/app/vendor/services/VendorVerificationsService.java"));

        for (String line : service.split("\\R")) {
            if (!line.contains("LOGGER.")) {
                continue;
            }
            assertFalse(line.contains("verificationLink"), "Raw email verification links must not be logged.");
            assertFalse(line.contains("verification.getOtp()"), "Raw mobile OTP values must not be logged.");
            assertFalse(line.contains("verification.getToken()"), "Raw email tokens must not be logged.");
            assertFalse(line.contains("verification.getEmail()"), "Vendor email addresses must not be logged.");
            assertFalse(line.contains("verification.getMobile()"), "Vendor mobile numbers must not be logged.");
            assertFalse(line.contains("event.getRecipient()"), "Communication recipients must not be logged.");
        }
    }
}
