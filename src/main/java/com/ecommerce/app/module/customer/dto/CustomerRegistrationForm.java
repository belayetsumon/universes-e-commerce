package com.ecommerce.app.module.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public registration input. Security-sensitive {@code Users} fields are
 * deliberately absent so request parameters cannot assign account state or
 * authorities.
 */
public class CustomerRegistrationForm {

    @NotBlank(message = "*Please provide your first name")
    @Size(min = 3, max = 50, message = "First name must be 3-50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "First name can only contain letters, numbers, single spaces, underscores, or hyphens"
    )
    private String firstName;

    @NotBlank(message = "*Please provide your last name")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "Last name can only contain letters, numbers, single spaces, underscores, or hyphens"
    )
    private String lastName;

    @NotBlank(message = "*Please provide your email")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email must be at most 254 characters")
    private String email;

    @NotBlank(message = "*Please provide your mobile")
    @Size(max = 32, message = "Mobile number is too long")
    @Pattern(
            regexp = "^\\+?[0-9\\s().-]+$",
            message = "Enter a valid Bangladesh mobile number"
    )
    private String mobile;

    @NotBlank(message = "*Please provide a password")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
