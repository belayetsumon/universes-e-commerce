package com.ecommerce.app.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetForm {

    @NotBlank(message = "Please provide a new password.")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters.")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
