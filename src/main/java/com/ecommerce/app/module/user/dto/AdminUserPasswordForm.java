package com.ecommerce.app.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUserPasswordForm {

    @NotBlank(message = "Please enter a new password.")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters.")
    private String newPassword;

    @NotBlank(message = "Please confirm the new password.")
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
