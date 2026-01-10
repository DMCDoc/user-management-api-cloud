package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;
import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
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
     * USERS
     * ==========================================================
     */

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String search, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        String q = (search == null) ? "" : search;

        return userRepository
                .findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        tenantId, q, tenantId, q, pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        UUID tenantId = TenantContext.getTenantIdRequired();
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public User updateUser(UUID userId, AdminUserUpdateRequest body) {
        User user = getUserById(userId);

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
            UUID tenantId = TenantContext.getTenantIdRequired();
            Set<Role> roles = body.getRoles().stream()
                    .map(role -> roleRepository.findByNameAndTenantId(role, tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        UUID tenantId = TenantContext.getTenantIdRequired();

        if (!userRepository.existsByIdAndTenantId(userId, tenantId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }

        userRepository.deleteByIdAndTenantId(userId, tenantId);
    }

    public void blockUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void unblockUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    public String resetPassword(UUID userId) {
        User user = getUserById(userId);
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
        User user = getUserById(userId);

        Role role = roleRepository.findByNameAndTenantId(roleName, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = getUserById(userId);

        boolean removed = user.getRoles()
                .removeIf(r -> r.getName().equals(roleName));

        if (!removed) {
            throw new ResourceNotFoundException(
                    "User does not have role: " + roleName);
        }

        userRepository.save(user);
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
                "totalUsers", userRepository.countByTenantId(tenantId),
                "disabled", userRepository.countByTenantIdAndActiveFalse(tenantId));
    }
}
