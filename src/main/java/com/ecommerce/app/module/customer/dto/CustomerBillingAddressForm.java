package com.ecommerce.app.module.customer.dto;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.BillingAddress;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerBillingAddressForm {

    @NotBlank(message = "*Please provide your first name")
    private String firstName;

    @NotBlank(message = "*Please provide your last name")
    private String lastName;

    @NotBlank(message = "*Please provide your email")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "*Please provide your mobile")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
    private String mobile;

    private String company;

    @NotBlank(message = "*Please provide address line one")
    private String addressLineOne;

    private String addressLinetwo;

    @NotBlank(message = "*Please provide your city")
    private String city;

    private String postCode;

    @NotBlank(message = "*Please provide your country")
    private String country;

    @NotBlank(message = "*Please provide your district")
    private String district;

    public static CustomerBillingAddressForm from(Users user, BillingAddress billingAddress) {
        CustomerBillingAddressForm form = new CustomerBillingAddressForm();

        if (billingAddress != null) {
            form.setFirstName(billingAddress.getFirstName());
            form.setLastName(billingAddress.getLastName());
            form.setEmail(billingAddress.getEmail());
            form.setMobile(billingAddress.getMobile());
            form.setCompany(billingAddress.getCompany());
            form.setAddressLineOne(billingAddress.getAddressLineOne());
            form.setAddressLinetwo(billingAddress.getAddressLinetwo());
            form.setCity(billingAddress.getCity());
            form.setPostCode(billingAddress.getPostCode());
            form.setCountry(billingAddress.getCountry());
            form.setDistrict(billingAddress.getDistrict());
            return form;
        }

        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setMobile(user.getMobile());
        form.setCountry("Bangladesh");
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddressLineOne() {
        return addressLineOne;
    }

    public void setAddressLineOne(String addressLineOne) {
        this.addressLineOne = addressLineOne;
    }

    public String getAddressLinetwo() {
        return addressLinetwo;
    }

    public void setAddressLinetwo(String addressLinetwo) {
        this.addressLinetwo = addressLinetwo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
