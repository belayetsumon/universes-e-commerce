package com.ecommerce.app.module.communication.dto;

import com.ecommerce.app.module.communication.model.ManualActorType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.Vendorprofile;

public class ManualCommunicationActor {

    private final ManualActorType actorType;
    private final Users user;
    private final Vendorprofile vendor;

    public ManualCommunicationActor(ManualActorType actorType, Users user, Vendorprofile vendor) {
        this.actorType = actorType;
        this.user = user;
        this.vendor = vendor;
    }

    public static ManualCommunicationActor system() {
        return new ManualCommunicationActor(ManualActorType.SYSTEM, null, null);
    }

    public static ManualCommunicationActor customer(Users user) {
        return new ManualCommunicationActor(ManualActorType.CUSTOMER, user, null);
    }

    public static ManualCommunicationActor vendor(Users user, Vendorprofile vendor) {
        return new ManualCommunicationActor(ManualActorType.VENDOR, user, vendor);
    }

    public static ManualCommunicationActor admin(Users user) {
        return new ManualCommunicationActor(ManualActorType.ADMIN, user, null);
    }

    public ManualActorType getActorType() {
        return actorType;
    }

    public Users getUser() {
        return user;
    }

    public Vendorprofile getVendor() {
        return vendor;
    }

    public Long getActorUserId() {
        return user == null ? null : user.getId();
    }

    public Long getVendorId() {
        return vendor == null ? null : vendor.getId();
    }

    public String getDisplayName() {
        if (vendor != null && hasText(vendor.getCompanyName())) {
            return vendor.getCompanyName();
        }

        if (user == null) {
            return "System";
        }

        String firstName = safe(user.getFirstName());
        String lastName = safe(user.getLastName());
        String fullName = (firstName + " " + lastName).trim();

        if (hasText(fullName)) {
            return fullName;
        }

        if (hasText(user.getEmail())) {
            return user.getEmail();
        }

        if (hasText(user.getMobile())) {
            return user.getMobile();
        }

        return user.getId() == null ? "User" : "User #" + user.getId();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
