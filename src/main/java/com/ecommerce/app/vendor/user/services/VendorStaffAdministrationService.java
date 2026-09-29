package com.ecommerce.app.vendor.user.services;

import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.SessionCredentialVersionService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorRoleChecker;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorStaffAdministrationService {

    public static final String STAFF_MANAGE_AUTHORIZATION = """
            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.staff.manage')
            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
            """;

    public static final String ROLE_MANAGE_AUTHORIZATION = """
            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.role.manage')
            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.staff.manage')
            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
            """;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Set<String> OWNER_ROLE_SLUGS = Set.of("admin", "owner", "vendor_owner");

    private final VendorRoleChecker vendorRoleChecker;
    private final UsersRepository usersRepository;
    private final UserVendorRoleRepository userVendorRoleRepository;
    private final VendorRoleRepository vendorRoleRepository;
    private final VendorPrivilegeRepository vendorPrivilegeRepository;
    private final SessionCredentialVersionService sessionCredentialVersionService;

    public VendorStaffAdministrationService(
            VendorRoleChecker vendorRoleChecker,
            UsersRepository usersRepository,
            UserVendorRoleRepository userVendorRoleRepository,
            VendorRoleRepository vendorRoleRepository,
            VendorPrivilegeRepository vendorPrivilegeRepository,
            SessionCredentialVersionService sessionCredentialVersionService) {
        this.vendorRoleChecker = vendorRoleChecker;
        this.usersRepository = usersRepository;
        this.userVendorRoleRepository = userVendorRoleRepository;
        this.vendorRoleRepository = vendorRoleRepository;
        this.vendorPrivilegeRepository = vendorPrivilegeRepository;
        this.sessionCredentialVersionService = sessionCredentialVersionService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize(STAFF_MANAGE_AUTHORIZATION)
    public List<UserVendorRole> listStaff(Vendorprofile vendor) {
        requireActiveVendor(vendor);
        return userVendorRoleRepository.findAllByVendor(vendor);
    }

    @Transactional(readOnly = true)
    @PreAuthorize(STAFF_MANAGE_AUTHORIZATION)
    public List<VendorRole> findStaffAssignableRoles(Vendorprofile vendor) {
        requireActiveVendor(vendor);
        return vendorRoleRepository.findAssignableRoles(vendor);
    }

    @Transactional
    @PreAuthorize(STAFF_MANAGE_AUTHORIZATION)
    public UserVendorRole assignExistingUserToVendor(Vendorprofile vendor, String usersEmail, Long vendorRoleId) {
        requireActiveVendor(vendor);
        List<String> errors = validateStaffAssignment(usersEmail, vendorRoleId);
        String normalizedEmail = normalizeEmail(usersEmail);

        Users user = null;
        if (normalizedEmail != null) {
            user = usersRepository.findByEmailAndStatus(normalizedEmail, Status.Active);
            if (user == null) {
                errors.add("No active user found with this email.");
            }
        }

        VendorRole vendorRole = null;
        if (vendorRoleId != null && vendorRoleId > 0) {
            vendorRole = vendorRoleRepository.findAssignableRoleById(vendorRoleId, vendor)
                    .orElse(null);
            if (vendorRole == null) {
                errors.add("Please select a valid vendor role.");
            }
        }

        if (user != null && userVendorRoleRepository.existsByUsers_EmailAndVendor_Id(user.getEmail(), vendor.getId())) {
            errors.add("This user is already assigned to this vendor with the selected role.");
        }

        if (!errors.isEmpty()) {
            throw new VendorStaffAssignmentException(errors);
        }

        enforceGrantCeiling(vendor, vendorRole.getVendorPrivilege());

        UserVendorRole assignment = new UserVendorRole();
        assignment.setUsers(user);
        assignment.setVendor(vendor);
        assignment.setVendorRole(vendorRole);
        UserVendorRole savedAssignment = userVendorRoleRepository.save(assignment);
        invalidateCachedAuthorities(user);
        return savedAssignment;
    }

    @Transactional
    @PreAuthorize(STAFF_MANAGE_AUTHORIZATION)
    public void removeStaffAssignment(Vendorprofile vendor, Long assignmentId) {
        requireActiveVendor(vendor);
        if (assignmentId == null || assignmentId <= 0) {
            throw new IllegalArgumentException("Invalid vendor staff assignment.");
        }

        UserVendorRole assignment = userVendorRoleRepository.findByIdAndVendor(assignmentId, vendor)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vendor staff assignment."));
        preventSelfMembershipDeletion(assignment);
        preventLastOwnerDeletion(vendor, assignment);
        Users affectedUser = assignment.getUsers();
        userVendorRoleRepository.delete(assignment);
        invalidateCachedAuthorities(affectedUser);
    }

    @Transactional(readOnly = true)
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public List<VendorRole> listRoles(Vendorprofile vendor) {
        requireActiveVendor(vendor);
        return vendorRoleRepository.findByVendorOrderByNameAsc(vendor);
    }

    @Transactional(readOnly = true)
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public VendorRole findRole(Vendorprofile vendor, Long roleId) {
        requireActiveVendor(vendor);
        return vendorRoleRepository.findByIdAndVendor(roleId, vendor)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vendor role ID: " + roleId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public List<VendorPrivilege> findRoleAssignablePrivileges(Vendorprofile vendor) {
        requireActiveVendor(vendor);
        return vendorPrivilegeRepository.findAll().stream()
                .filter(this::isVendorPrivilege)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public boolean slugExistsForVendor(Vendorprofile vendor, String slug, Long currentRoleId) {
        requireActiveVendor(vendor);
        String normalizedSlug = normalizeSlug(slug);
        if (normalizedSlug == null) {
            return false;
        }

        if (currentRoleId == null) {
            return vendorRoleRepository.existsByVendorAndSlugIgnoreCase(vendor, normalizedSlug);
        }

        return vendorRoleRepository.existsByVendorAndSlugIgnoreCaseAndIdNot(vendor, normalizedSlug, currentRoleId);
    }

    @Transactional
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public VendorRole saveRole(Vendorprofile vendor, VendorRole submittedRole, Collection<Long> privilegeIds) {
        requireActiveVendor(vendor);
        if (submittedRole == null) {
            throw new IllegalArgumentException("Vendor role is required.");
        }

        String normalizedSlug = normalizeSlug(submittedRole.getSlug());
        if (normalizedSlug == null) {
            throw new IllegalArgumentException("Role slug is required.");
        }

        if (slugExistsForVendor(vendor, normalizedSlug, submittedRole.getId())) {
            throw new IllegalArgumentException("Slug already exists for this vendor.");
        }

        Set<VendorPrivilege> privileges = resolveVendorPrivileges(vendor, privilegeIds);
        VendorRole role = submittedRole.getId() == null
                ? new VendorRole()
                : findRole(vendor, submittedRole.getId());
        role.setName(submittedRole.getName());
        role.setSlug(normalizedSlug);
        role.setVendor(vendor);
        role.setVendorPrivilege(privileges);
        VendorRole savedRole = vendorRoleRepository.save(role);
        if (savedRole.getId() != null && submittedRole.getId() != null) {
            invalidateCachedAuthoritiesForRole(savedRole.getId());
        }
        return savedRole;
    }

    @Transactional
    @PreAuthorize(ROLE_MANAGE_AUTHORIZATION)
    public void deleteRole(Vendorprofile vendor, Long roleId) {
        requireActiveVendor(vendor);
        VendorRole role = findRole(vendor, roleId);
        if (userVendorRoleRepository.existsByVendorRole_Id(role.getId())) {
            throw new IllegalStateException("This vendor role is assigned to staff and cannot be deleted.");
        }
        vendorRoleRepository.delete(role);
    }

    private void requireActiveVendor(Vendorprofile vendor) {
        if (vendor == null || vendor.getId() == null) {
            throw new IllegalArgumentException("Active vendor is required.");
        }
    }

    private void invalidateCachedAuthorities(Users user) {
        if (user != null && user.getId() != null) {
            sessionCredentialVersionService.bumpCredentialVersion(user.getId());
        }
    }

    private void invalidateCachedAuthoritiesForRole(Long vendorRoleId) {
        for (Long userId : userVendorRoleRepository.findAssignedUserIdsByVendorRoleId(vendorRoleId)) {
            sessionCredentialVersionService.bumpCredentialVersion(userId);
        }
    }

    private List<String> validateStaffAssignment(String usersEmail, Long vendorRoleId) {
        List<String> errors = new ArrayList<>();
        String normalizedEmail = normalizeEmail(usersEmail);
        if (normalizedEmail == null) {
            errors.add("Email is required.");
        } else if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            errors.add("Invalid email format.");
        }

        if (vendorRoleId == null || vendorRoleId <= 0) {
            errors.add("Please select a valid vendor role.");
        }
        return errors;
    }

    private Set<VendorPrivilege> resolveVendorPrivileges(Vendorprofile vendor, Collection<Long> privilegeIds) {
        if (privilegeIds == null || privilegeIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> requestedIds = new LinkedHashSet<>();
        for (Long privilegeId : privilegeIds) {
            if (privilegeId == null || privilegeId <= 0) {
                throw new IllegalArgumentException("Invalid vendor privilege selection.");
            }
            requestedIds.add(privilegeId);
        }

        List<VendorPrivilege> privileges = vendorPrivilegeRepository.findAllById(requestedIds);
        if (privileges.size() != requestedIds.size()) {
            throw new IllegalArgumentException("Invalid vendor privilege selection.");
        }
        if (privileges.stream().anyMatch(privilege -> !isVendorPrivilege(privilege))) {
            throw new AccessDeniedException("Platform and public capabilities cannot be assigned to vendor roles.");
        }

        enforceGrantCeiling(vendor, privileges);
        return new HashSet<>(privileges);
    }

    private boolean isVendorPrivilege(VendorPrivilege privilege) {
        return privilege != null
                && privilege.getSlug() != null
                && privilege.getSlug().toLowerCase(Locale.ROOT).startsWith("vendor.");
    }

    private void enforceGrantCeiling(Vendorprofile vendor, Collection<VendorPrivilege> privileges) {
        if (privileges == null || privileges.isEmpty()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (hasLegacyOwnerRole(authentication)) {
            return;
        }

        Set<String> effectiveAuthorities = new HashSet<>();
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                effectiveAuthorities.add(authority.getAuthority());
            }
        }

        for (VendorPrivilege privilege : privileges) {
            String requiredAuthority = "VENDOR_" + vendor.getId() + ":" + privilege.getSlug();
            if (!effectiveAuthorities.contains(requiredAuthority)) {
                throw new AccessDeniedException("Cannot grant vendor permissions outside your effective access.");
            }
        }
    }

    private boolean hasLegacyOwnerRole(Authentication authentication) {
        return authentication != null
                && (vendorRoleChecker.hasVendorRole(authentication, "ADMIN")
                || vendorRoleChecker.hasVendorRole(authentication, "OWNER")
                || vendorRoleChecker.hasVendorRole(authentication, "VENDOR_OWNER"));
    }

    private void preventSelfMembershipDeletion(UserVendorRole assignment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication == null ? null : authentication.getName();
        String targetEmail = assignment.getUsers() == null ? null : assignment.getUsers().getEmail();
        if (currentUsername != null
                && targetEmail != null
                && currentUsername.equalsIgnoreCase(targetEmail)) {
            throw new AccessDeniedException("You cannot remove your own vendor staff access.");
        }
    }

    private void preventLastOwnerDeletion(Vendorprofile vendor, UserVendorRole assignment) {
        VendorRole role = assignment.getVendorRole();
        String roleName = role == null || role.getName() == null
                ? ""
                : role.getName().toLowerCase(Locale.ROOT);
        String roleSlug = role == null || role.getSlug() == null
                ? ""
                : role.getSlug().toLowerCase(Locale.ROOT);
        if ((OWNER_ROLE_SLUGS.contains(roleName) || OWNER_ROLE_SLUGS.contains(roleSlug))
                && userVendorRoleRepository.countVendorOwnerAssignments(vendor) <= 1) {
            throw new AccessDeniedException("The final vendor owner cannot be removed.");
        }
    }

    private String normalizeEmail(String usersEmail) {
        if (usersEmail == null || usersEmail.isBlank()) {
            return null;
        }
        return usersEmail.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    public static class VendorStaffAssignmentException extends RuntimeException {

        private final List<String> errors;

        public VendorStaffAssignmentException(List<String> errors) {
            super(String.join(" ", errors));
            this.errors = List.copyOf(errors);
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
