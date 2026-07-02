package com.ecommerce.app.module.customer.dto;

import com.ecommerce.app.module.user.model.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerAccountForm {

    @NotBlank(message = "*Please provide your first name")
    @Size(min = 3, max = 50, message = "First name must be 3-50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "First name can only contain letters, numbers, single spaces, underscores, or hyphens, and cannot start or end with a space"
    )
    private String firstName;

    @NotBlank(message = "*Please provide your last name")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "Last name can only contain letters, numbers, single spaces, underscores, or hyphens"
    )
    private String lastName;

    @NotBlank(message = "*Please provide your mobile")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
    private String mobile;

    public static CustomerAccountForm fromUser(Users user) {
        CustomerAccountForm form = new CustomerAccountForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setMobile(user.getMobile());
        return form;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
