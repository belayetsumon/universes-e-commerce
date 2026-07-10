package com.ecommerce.app.adminvendor.dto;

import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminVendorProfileForm {

    @NotBlank(message = "Vendor UUID is required.")
    private String uuid;

    private String vendorCode;

    @NotBlank(message = "Company name is required.")
    private String companyName;

    private String firstName;

    private String lastName;

    @NotBlank(message = "Designation is required.")
    private String designation;

    private String phone;

    @Email(message = "Enter a valid email address.")
    private String email;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Vendor status is required.")
    private VendorStatusEnum vendorStatusEnum;

    public static AdminVendorProfileForm from(Vendorprofile vendorprofile) {
        AdminVendorProfileForm form = new AdminVendorProfileForm();

        form.setUuid(vendorprofile.getUuid());
        form.setVendorCode(vendorprofile.getVendorCode());
        form.setCompanyName(vendorprofile.getCompanyName());
        form.setFirstName(vendorprofile.getFirstName());
        form.setLastName(vendorprofile.getLastName());
        form.setDesignation(vendorprofile.getDesignation());
        form.setPhone(vendorprofile.getPhone());
        form.setEmail(vendorprofile.getEmail());
        form.setAddress(vendorprofile.getAddress());
        form.setDescription(vendorprofile.getDescription());
        form.setVendorStatusEnum(vendorprofile.getVendorStatusEnum());

        return form;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VendorStatusEnum getVendorStatusEnum() {
        return vendorStatusEnum;
    }

    public void setVendorStatusEnum(VendorStatusEnum vendorStatusEnum) {
        this.vendorStatusEnum = vendorStatusEnum;
    }

    public String getPrimaryContactName() {
        String fullName = ((firstName == null ? "" : firstName.trim())
                + " "
                + (lastName == null ? "" : lastName.trim())).trim();

        return fullName.isEmpty() ? "Not provided" : fullName;
    }

    public String getVendorStatusLabel() {
        return vendorStatusEnum == null ? "Not selected" : vendorStatusEnum.name();
    }

    public String getVendorStatusBadgeClass() {
        if (vendorStatusEnum == null) {
            return "bg-secondary";
        }

        return switch (vendorStatusEnum) {
            case Active ->
                "bg-success";
            case Pending ->
                "bg-warning text-dark";
            case Block ->
                "bg-secondary";
        };
    }
}
