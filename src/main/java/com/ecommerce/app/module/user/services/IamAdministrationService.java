package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.Modules;
import com.ecommerce.app.module.user.model.Privilege;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.ripository.ModuleRepository;
import com.ecommerce.app.module.user.ripository.PrivilegeRepository;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.security.authorization.PlatformIamAuthorization;
import com.ecommerce.app.security.permission.PlatformIamPermissions;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IamAdministrationService {

    private static final Pattern ROLE_SLUG_PATTERN = Pattern.compile("[a-z0-9]+(?:[._-][a-z0-9]+)*");

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final ModuleRepository moduleRepository;
    private final PlatformIamAuthorization platformIamAuthorization;

    public IamAdministrationService(
            RoleRepository roleRepository,
            PrivilegeRepository privilegeRepository,
            ModuleRepository moduleRepository,
            PlatformIamAuthorization platformIamAuthorization) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.moduleRepository = moduleRepository;
        this.platformIamAuthorization = platformIamAuthorization;
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public Role findRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public List<Privilege> findAllPrivileges() {
        return privilegeRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public Privilege findPrivilege(Long privilegeId) {
        return privilegeRepository.findById(privilegeId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public List<Modules> findAllModules() {
        return moduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformIamPermissions.CAN_READ)
    public Modules findModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found."));
    }

    @Transactional
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE)
    public Role saveRole(Role submittedRole) {
        requireRoleInput(submittedRole);
        Authentication authentication = currentAuthentication();
        Set<Privilege> requestedPrivileges = resolvePrivileges(submittedRole.getPrivilege());
        platformIamAuthorization.assertCanGrant(authentication, requestedPrivileges);

        Role role;
        if (submittedRole.getId() == null) {
            String slug = normalizeRoleSlug(submittedRole.getSlug());
            if (roleRepository.existsBySlugIgnoreCase(slug)) {
                throw new IllegalArgumentException("A role with this slug already exists.");
            }
            if (platformIamAuthorization.isProtectedRole(slug)
                    && !platformIamAuthorization.canManageProtected(authentication)) {
                throw new AccessDeniedException("Protected role management permission is required.");
            }
            role = new Role();
            role.setSlug(slug);
        } else {
            role = roleRepository.findById(submittedRole.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found."));
            assertRoleCanBeChanged(role, authentication);
        }

        role.setName(submittedRole.getName().trim());
        role.setPrivilege(requestedPrivileges);
        return roleRepository.save(role);
    }

    @Transactional
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE)
    public void deleteRole(Long roleId) {
        Authentication authentication = currentAuthentication();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        if (platformIamAuthorization.isProtectedRole(role.getSlug())) {
            throw new AccessDeniedException("Protected system roles cannot be deleted.");
        }
        assertRoleCanBeChanged(role, authentication);

        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new IllegalStateException("A role assigned to users cannot be deleted.");
        }
        roleRepository.delete(role);
    }

    @Transactional
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public Privilege updatePrivilegeDisplayName(Privilege submittedPrivilege) {
        if (submittedPrivilege == null || submittedPrivilege.getId() == null) {
            throw new IllegalStateException("Permissions are application-managed and must be created by a versioned migration.");
        }
        if (submittedPrivilege.getName() == null || submittedPrivilege.getName().isBlank()) {
            throw new IllegalArgumentException("Permission name is required.");
        }

        Privilege privilege = privilegeRepository.findById(submittedPrivilege.getId())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
        privilege.setName(submittedPrivilege.getName().trim());
        return privilegeRepository.save(privilege);
    }

    @Transactional
    @PreAuthorize(PlatformIamPermissions.CAN_MANAGE_PROTECTED)
    public Modules updateModuleDisplayName(Modules submittedModule) {
        if (submittedModule == null || submittedModule.getId() == null) {
            throw new IllegalStateException("Permission modules are application-managed and must be created by a versioned migration.");
        }
        if (submittedModule.getName() == null || submittedModule.getName().isBlank()) {
            throw new IllegalArgumentException("Module name is required.");
        }

        Modules module = moduleRepository.findById(submittedModule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found."));
        module.setName(submittedModule.getName().trim());
        return moduleRepository.save(module);
    }

    private Set<Privilege> resolvePrivileges(Set<Privilege> submittedPrivileges) {
        if (submittedPrivileges == null || submittedPrivileges.isEmpty()) {
            throw new IllegalArgumentException("Select at least one permission for the role.");
        }

        Set<Long> privilegeIds = new HashSet<>();
        for (Privilege privilege : submittedPrivileges) {
            if (privilege == null || privilege.getId() == null) {
                throw new IllegalArgumentException("Every selected permission must be an existing catalogue entry.");
            }
            privilegeIds.add(privilege.getId());
        }

        List<Privilege> managedPrivileges = privilegeRepository.findAllById(privilegeIds);
        if (managedPrivileges.size() != privilegeIds.size()) {
            throw new IllegalArgumentException("One or more selected permissions no longer exist.");
        }
        return new HashSet<>(managedPrivileges);
    }

    private void assertRoleCanBeChanged(Role role, Authentication authentication) {
        if (platformIamAuthorization.isProtectedRole(role.getSlug())
                && !platformIamAuthorization.canManageProtected(authentication)) {
            throw new AccessDeniedException("Protected role management permission is required.");
        }
        if (role.getUsers() != null && authentication != null) {
            boolean actorUsesRole = role.getUsers().stream()
                    .anyMatch(user -> user != null
                    && user.getEmail() != null
                    && user.getEmail().equalsIgnoreCase(authentication.getName()));
            if (actorUsesRole) {
                throw new AccessDeniedException("You cannot modify or delete a role assigned to your own account.");
            }
        }
    }

    private void requireRoleInput(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role is required.");
        }
        if (role.getName() == null || role.getName().isBlank()) {
            throw new IllegalArgumentException("Role name is required.");
        }
        if (role.getId() == null && (role.getSlug() == null || role.getSlug().isBlank())) {
            throw new IllegalArgumentException("Role slug is required.");
        }
    }

    private String normalizeRoleSlug(String slug) {
        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);
        if (!ROLE_SLUG_PATTERN.matcher(normalizedSlug).matches()) {
            throw new IllegalArgumentException("Role slug may contain lowercase letters, numbers, dots, underscores, and hyphens only.");
        }
        return normalizedSlug;
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
