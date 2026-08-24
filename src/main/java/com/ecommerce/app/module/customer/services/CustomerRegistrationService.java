package com.ecommerce.app.module.customer.services;

import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.checkout.customer.services.CustomerCodMobileVerificationService;
import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.user.model.RegistrationSource;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRegistrationService {

    private static final String CUSTOMER_ROLE = "customer";

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomerCodMobileVerificationService mobileVerificationService;
    private final ReferralService referralService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomerRegistrationService(
            UsersRepository usersRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder,
            CustomerCodMobileVerificationService mobileVerificationService,
            ReferralService referralService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mobileVerificationService = mobileVerificationService;
        this.referralService = referralService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public Users register(CustomerRegistrationForm form, String referralCode) {
        if (form == null) {
            throw new CustomerRegistrationException(null, "Registration details are required.");
        }

        String normalizedEmail = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (usersRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new CustomerRegistrationException("email", "This email address is already registered.");
        }

        Users user = new Users();
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        mobileVerificationService.updateMobileAndInvalidateVerificationIfChanged(user, form.getMobile());

        Users mobileOwner = findMobileOwner(user.getMobile());
        if (mobileOwner != null) {
            throw new CustomerRegistrationException("mobile", "This mobile number is already registered.");
        }

        Role customerRole = roleRepository.findBySlug(CUSTOMER_ROLE);
        if (customerRole == null) {
            throw new IllegalStateException("The customer role is not configured.");
        }

        user.setRole(new HashSet<>(java.util.Set.of(customerRole)));
        user.setUserType(UserType.customer);
        user.setStatus(Status.Active);
        user.setRegistrationSource(RegistrationSource.CUSTOMER_REGISTRATION);
        user.setGuestAccount(false);
        user.setPasswordConfigured(true);
        user.setEmailVerified(false);

        try {
            usersRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new CustomerRegistrationException(
                    null,
                    "An account already exists with this email address or mobile number.");
        }

        Users referringUser = referralService.resolveReferrerByCode(trimToNull(referralCode));
        referralService.createReferralProfileAndGrantSignupReward(user, referringUser);
        applicationEventPublisher.publishEvent(
                CommunicationRequestedEvent.customer(
                        MessageEventType.CUSTOMER_REGISTERED,
                        user,
                        MessageChannel.EMAIL,
                        user.getEmail(),
                        Map.of("customerName", user.getFirstName())
                )
        );
        return user;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Users findMobileOwner(String normalizedMobile) {
        if (normalizedMobile == null) {
            return null;
        }
        Users owner = usersRepository.findByMobile(normalizedMobile);
        if (owner == null) {
            owner = usersRepository.findByMobile("+" + normalizedMobile);
        }
        if (owner == null && normalizedMobile.startsWith("880")) {
            owner = usersRepository.findByMobile("0" + normalizedMobile.substring(3));
        }
        return owner;
    }
}
