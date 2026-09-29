package com.ecommerce.app.module.user.services;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends reset links without putting the raw token in an application queue or log. */
@Service
public class PasswordResetEmailSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public PasswordResetEmailSender(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.security.password-reset.from:}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    public void send(String recipient, String firstName, String resetUrl, long ttlMinutes) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new PasswordResetDeliveryException("Password-reset email delivery is not configured.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress.trim());
        }
        message.setTo(recipient);
        message.setSubject("Reset your password");
        String greeting = firstName == null || firstName.isBlank() ? "Hello," : "Hello " + firstName.trim() + ",";
        message.setText(greeting + "\n\n"
                + "We received a request to reset your password. Use the secure link below within "
                + ttlMinutes + " minutes:\n\n"
                + resetUrl + "\n\n"
                + "The link can be used once. If you did not request this, you can safely ignore this email.");
        try {
            mailSender.send(message);
        } catch (RuntimeException ex) {
            // Do not expose provider details (or the reset URL) to the caller.
            throw new PasswordResetDeliveryException("Password-reset email delivery failed.");
        }
    }

    public static class PasswordResetDeliveryException extends RuntimeException {

        public PasswordResetDeliveryException(String message) {
            super(message);
        }
    }
}
