package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.dto.ManualMessageRequest;
import com.ecommerce.app.module.communication.model.ManualActorType;
import com.ecommerce.app.module.communication.model.ManualAudience;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualCommunicationPermissionService {

    private static final Set<ManualAudience> ADMIN_AUDIENCES = EnumSet.of(
            ManualAudience.ALL_CUSTOMERS,
            ManualAudience.ALL_VENDORS,
            ManualAudience.SELECTED_CUSTOMERS,
            ManualAudience.SELECTED_VENDORS,
            ManualAudience.SINGLE_CUSTOMER,
            ManualAudience.SINGLE_VENDOR
    );

    private static final Set<ManualAudience> VENDOR_AUDIENCES = EnumSet.of(
            ManualAudience.ADMIN,
            ManualAudience.OWN_CUSTOMERS,
            ManualAudience.SELECTED_OWN_CUSTOMERS
    );

    private static final Set<ManualAudience> CUSTOMER_AUDIENCES = EnumSet.of(ManualAudience.ADMIN);

    private static final Set<MessageChannel> MANUAL_CHANNELS = EnumSet.of(
            MessageChannel.EMAIL,
            MessageChannel.SMS,
            MessageChannel.IN_APP,
            MessageChannel.PUSH
    );

    private final UsersRepository usersRepository;
    private final VendorprofileRepository vendorprofileRepository;

    public ManualCommunicationPermissionService(UsersRepository usersRepository, VendorprofileRepository vendorprofileRepository) {
        this.usersRepository = usersRepository;
        this.vendorprofileRepository = vendorprofileRepository;
    }

    @Transactional(readOnly = true)
    public ManualCommunicationActor requireAdminActor() {
        Users user = currentUser();
        if (user.getUserType() != UserType.administrator && user.getUserType() != UserType.systemadmin) {
            throw new AccessDeniedException("Only admins can use this communication screen.");
        }
        return new ManualCommunicationActor(ManualActorType.ADMIN, user, null);
    }

    @Transactional(readOnly = true)
    public ManualCommunicationActor requireVendorActor() {
        Users user = currentUser();
        List<Vendorprofile> vendors = vendorprofileRepository.findByUserId(user);
        if (vendors.isEmpty()) {
            throw new AccessDeniedException("Only vendors can use this communication screen.");
        }
        return new ManualCommunicationActor(ManualActorType.VENDOR, user, vendors.get(0));
    }

    @Transactional(readOnly = true)
    public ManualCommunicationActor requireCustomerActor() {
        Users user = currentUser();
        if (user.getUserType() != UserType.customer) {
            throw new AccessDeniedException("Only customers can use this communication screen.");
        }
        return new ManualCommunicationActor(ManualActorType.CUSTOMER, user, null);
    }

    public void validate(ManualCommunicationActor actor, ManualMessageRequest request) {
        if (actor == null || actor.getActorType() == null) {
            throw new AccessDeniedException("Manual communication actor is required.");
        }
        if (request == null || request.getAudience() == null) {
            throw new IllegalArgumentException("Audience is required.");
        }
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            throw new IllegalArgumentException("Select at least one delivery channel.");
        }
        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new IllegalArgumentException("Message body is required.");
        }
        if (!MANUAL_CHANNELS.containsAll(request.getChannels())) {
            throw new IllegalArgumentException("Manual messages support email, SMS, in-app, and push channels only.");
        }
        if (!allowedAudiences(actor.getActorType()).contains(request.getAudience())) {
            throw new AccessDeniedException("This sender is not allowed to use audience " + request.getAudience() + ".");
        }
    }

    public Set<ManualAudience> allowedAudiences(ManualActorType actorType) {
        if (actorType == ManualActorType.ADMIN) {
            return ADMIN_AUDIENCES;
        }
        if (actorType == ManualActorType.VENDOR) {
            return VENDOR_AUDIENCES;
        }
        return CUSTOMER_AUDIENCES;
    }

    private Users currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Login is required.");
        }
        Users user = usersRepository.findByEmailAndStatus(authentication.getName(), Status.Active);
        if (user == null) {
            throw new AccessDeniedException("Active user account was not found.");
        }
        return user;
    }
}
