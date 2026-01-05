package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;
import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * ==========================================================
     * ADAPTERS POUR ADMIN CONTROLLER (STABLE API)
     * ==========================================================
     */

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(
            UUID tenantId,
            String search,
            Pageable pageable) {

        String query = (search == null) ? "" : search;

        return userRepository
                .findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        tenantId, query, tenantId, query, pageable)
                .map(UserMapper::toResponse);
    }

    public void disableUser(UUID userId, UUID tenantId) {
        User user = findUser(userId, tenantId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void enableUser(UUID userId, UUID tenantId) {
        User user = findUser(userId, tenantId);
        user.setActive(true);
        userRepository.save(user);
    }

    public void deleteUser(UUID userId, UUID tenantId) {
        if (!userRepository.existsByIdAndTenantId(userId, tenantId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userRepository.deleteByIdAndTenantId(userId, tenantId);
    }

    /*
     * ==========================================================
     * CORE LOGIC EXISTANTE (CONSERVÉE)
     * ==========================================================
     */

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        return findUser(userId, tenantId);
    }

    public User updateUser(UUID userId, AdminUserUpdateRequest body) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        User user = findUser(userId, tenantId);

        if (body.getEmail() != null) {
            user.setEmail(body.getEmail());
        }
        if (body.getUsername() != null) {
            user.setUsername(body.getUsername());
        }
        if (body.getEnabled() != null) {
            user.setActive(body.getEnabled());
        }
        if (body.getRoles() != null) {
            Set<Role> roles = body.getRoles().stream()
                    .map(role -> roleRepository.findByNameAndTenantId(role, tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public String resetPassword(UUID userId) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        User user = findUser(userId, tenantId);

        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return tempPassword;
    }

    /*
     * ==========================================================
     * ROLES
     * ==========================================================
     */

    public void addRoleToUser(UUID userId, String roleName) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        User user = findUser(userId, tenantId);

        Role role = roleRepository.findByNameAndTenantId(roleName, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        if (user.getRoles().add(role)) {
            userRepository.save(user);
        }
    }

    public void removeRoleFromUser(UUID userId, String roleName) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        User user = findUser(userId, tenantId);

        boolean removed = user.getRoles()
                .removeIf(r -> r.getName().equals(roleName));

        if (!removed) {
            throw new ResourceNotFoundException(
                    "User does not have role: " + roleName);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        UUID tenantId = TenantContext.getTenantIdRequired();
        return roleRepository.findAllByTenantId(tenantId);
    }

    /*
     * ==========================================================
     * STATS
     * ==========================================================
     */

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        UUID tenantId = TenantContext.getTenantIdRequired();
        return Map.of(
                "totalUsers", userRepository.countByTenantIdAndRoles_Name(tenantId, "ROLE_USER"),
                "admins", userRepository.countByTenantIdAndRoles_Name(tenantId, "ROLE_ADMIN"),
                "disabled", userRepository.countByTenantIdAndActiveFalse(tenantId));
    }

    /*
     * ==========================================================
     * INTERNAL
     * ==========================================================
     */

    private User findUser(UUID userId, UUID tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
