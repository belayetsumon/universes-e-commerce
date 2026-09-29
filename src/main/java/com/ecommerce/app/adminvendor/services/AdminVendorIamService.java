package com.ecommerce.app.adminvendor.services;

import com.ecommerce.app.security.permission.PlatformVendorManagementPermissions;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminVendorIamService {

    private static final Pattern VENDOR_PERMISSION_PATTERN
            = Pattern.compile("vendor\\.[a-z0-9]+(?:[._-][a-z0-9]+)*");
    private static final Pattern ROLE_SLUG_PATTERN
            = Pattern.compile("[a-z0-9]+(?:[._-][a-z0-9]+)*");

    private final VendorRoleRepository roleRepository;
    private final VendorPrivilegeRepository privilegeRepository;
    private final UserVendorRoleRepository userVendorRoleRepository;

    public AdminVendorIamService(
            VendorRoleRepository roleRepository,
            VendorPrivilegeRepository privilegeRepository,
            UserVendorRoleRepository userVendorRoleRepository) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.userVendorRoleRepository = userVendorRoleRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public List<VendorRole> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public VendorRole findRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor role not found."));
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public List<VendorPrivilege> findAllPrivilegesForAdministration() {
        return privilegeRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public List<VendorPrivilege> findAllAssignablePrivileges() {
        return privilegeRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public VendorPrivilege findPrivilege(Long privilegeId) {
        return privilegeRepository.findById(privilegeId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor permission not found."));
    }

    @Transactional
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE)
    public VendorRole saveRole(VendorRole submittedRole, List<Long> privilegeIds) {
        requireRoleInput(submittedRole);

        VendorRole role = submittedRole.getId() == null
                ? new VendorRole()
                : findRoleForMutation(submittedRole.getId());
        String roleSlug = normalizeRoleSlug(submittedRole.getSlug());

        boolean duplicateSlug = role.getId() == null
                ? roleRepository.existsByVendorAndSlugIgnoreCase(role.getVendor(), roleSlug)
                : roleRepository.existsByVendorAndSlugIgnoreCaseAndIdNot(role.getVendor(), roleSlug, role.getId());
        if (duplicateSlug) {
            throw new IllegalArgumentException("A vendor role with this slug already exists in the same scope.");
        }

        role.setName(submittedRole.getName().trim());
        role.setSlug(roleSlug);
        role.setVendorPrivilege(resolvePrivileges(privilegeIds));
        return roleRepository.save(role);
    }

    @Transactional
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_DELETE)
    public void deleteRole(Long roleId) {
        VendorRole role = findRoleForMutation(roleId);
        if (userVendorRoleRepository.existsByVendorRole_Id(roleId)) {
            throw new IllegalStateException("A vendor role assigned to users cannot be deleted.");
        }
        roleRepository.delete(role);
    }

    @Transactional
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public VendorPrivilege savePrivilege(VendorPrivilege submittedPrivilege) {
        requirePrivilegeInput(submittedPrivilege);

        VendorPrivilege privilege = submittedPrivilege.getId() == null
                ? new VendorPrivilege()
                : privilegeRepository.findById(submittedPrivilege.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Vendor permission not found."));
        String permissionSlug = normalizeVendorPermission(submittedPrivilege.getSlug());

        boolean duplicateSlug = privilege.getId() == null
                ? privilegeRepository.existsBySlugIgnoreCase(permissionSlug)
                : privilegeRepository.existsBySlugIgnoreCaseAndIdNot(permissionSlug, privilege.getId());
        if (duplicateSlug) {
            throw new IllegalArgumentException("A vendor permission with this slug already exists.");
        }

        privilege.setName(submittedPrivilege.getName().trim());
        privilege.setSlug(permissionSlug);
        return privilegeRepository.save(privilege);
    }

    @Transactional
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_DELETE)
    public void deletePrivilege(Long privilegeId) {
        VendorPrivilege privilege = privilegeRepository.findById(privilegeId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor permission not found."));
        if (privilege.getVendorRole() != null && !privilege.getVendorRole().isEmpty()) {
            throw new IllegalStateException("A vendor permission assigned to roles cannot be deleted.");
        }
        privilegeRepository.delete(privilege);
    }

    private VendorRole findRoleForMutation(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Vendor role ID is required.");
        }
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor role not found."));
    }

    private Set<VendorPrivilege> resolvePrivileges(List<Long> privilegeIds) {
        if (privilegeIds == null || privilegeIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> uniqueIds = new HashSet<>(privilegeIds);
        List<VendorPrivilege> privileges = privilegeRepository.findAllById(uniqueIds);
        if (privileges.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("One or more selected vendor permissions no longer exist.");
        }
        for (VendorPrivilege privilege : privileges) {
            normalizeVendorPermission(privilege.getSlug());
        }
        return new HashSet<>(privileges);
    }

    private void requireRoleInput(VendorRole role) {
        if (role == null || role.getName() == null || role.getName().isBlank()) {
            throw new IllegalArgumentException("Vendor role name is required.");
        }
        if (role.getSlug() == null || role.getSlug().isBlank()) {
            throw new IllegalArgumentException("Vendor role slug is required.");
        }
    }

    private void requirePrivilegeInput(VendorPrivilege privilege) {
        if (privilege == null || privilege.getName() == null || privilege.getName().isBlank()) {
            throw new IllegalArgumentException("Vendor permission name is required.");
        }
        if (privilege.getSlug() == null || privilege.getSlug().isBlank()) {
            throw new IllegalArgumentException("Vendor permission slug is required.");
        }
    }

    private String normalizeRoleSlug(String slug) {
        String normalized = slug.trim().toLowerCase(Locale.ROOT);
        if (!ROLE_SLUG_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Vendor role slug has an invalid format.");
        }
        return normalized;
    }

    private String normalizeVendorPermission(String slug) {
        if (slug == null) {
            throw new IllegalArgumentException("Vendor permission slug is required.");
        }
        String normalized = slug.trim().toLowerCase(Locale.ROOT);
        if (!VENDOR_PERMISSION_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Vendor permissions must use a valid vendor.* catalogue slug.");
        }
        return normalized;
    }
}
