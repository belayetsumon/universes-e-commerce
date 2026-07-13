package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.user.model.RegistrationSource;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestCheckoutUserResolver {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public GuestCheckoutUserResolver(
            UsersRepository usersRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Users resolveUser(OtpVerification verification) {
        if (verification == null || verification.getMobileNumber() == null) {
            throw new IllegalStateException("Verified mobile session is required.");
        }

        return resolveMobile(verification.getMobileNumber(), true);
    }

    @Transactional
    public Users resolveMobile(String mobile, boolean authoritativeMobileVerification) {
        return resolveMobile(mobile, authoritativeMobileVerification, true);
    }

    @Transactional
    public Users resolveMobile(String mobile, boolean authoritativeMobileVerification, boolean autoCreateCustomerAccount) {
        if (mobile == null || mobile.isBlank()) {
            throw new IllegalStateException("Guest checkout mobile number is required.");
        }
        Optional<Users> existing = usersRepository.findOptionalByMobile(mobile);
        if (existing.isPresent()) {
            Users user = existing.get();
            if (authoritativeMobileVerification && !user.isMobileVerified()) {
                user.setMobileVerified(true);
                usersRepository.save(user);
            }
            return user;
        }
        if (!autoCreateCustomerAccount) {
            throw new IllegalStateException("Please login or use another checkout method.");
        }

        try {
            return usersRepository.save(createGuestUser(mobile, authoritativeMobileVerification));
        } catch (DataIntegrityViolationException duplicateMobileRace) {
            return usersRepository.findOptionalByMobile(mobile)
                    .orElseThrow(() -> duplicateMobileRace);
        }
    }

    private Users createGuestUser(String mobile, boolean mobileVerified) {
        Users user = new Users();
        user.setFirstName("Guest");
        user.setLastName("Customer");
        user.setEmail(null);
        user.setMobile(mobile);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID() + ":" + System.nanoTime()));
        user.setStatus(Status.Active);
        user.setUserType(UserType.customer);
        user.setMobileVerified(mobileVerified);
        user.setEmailVerified(false);
        user.setRegistrationSource(RegistrationSource.GUEST_CHECKOUT);
        user.setGuestAccount(true);
        user.setPasswordConfigured(false);
        user.setCreatedBy("guest-checkout");

        Role customerRole = roleRepository.findBySlug("customer");
        if (customerRole != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            user.setRole(roles);
        }
        return user;
    }
}
