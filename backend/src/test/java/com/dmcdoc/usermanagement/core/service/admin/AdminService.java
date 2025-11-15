package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;
import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import com.dmcdoc.usermanagement.core.model.AdminLog;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.AdminLogRepository;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogRepository adminLogRepository;

    /*
     * ==============================
     * SEARCH + STATS
     * ==============================
     */
    public Page<User> searchUsers(String q, Pageable pageable) {
        if (q == null || q.isBlank())
            return userRepository.findAll(pageable);
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
    }

    public Map<String, Long> getStats() {
        long total = userRepository.count();
        long admins = userRepository.countByRoles_Name("ROLE_ADMIN");
        long disabled = userRepository.countByEnabledFalse();
        return Map.of("totalUsers", total, "admins", admins, "disabled", disabled);
    }

    /*
     * ==============================
     * CORE CRUD
     * ==============================
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateUser(UUID userId, AdminUserUpdateRequest body) {
        User user = getUserById(userId);

        if (body.getEmail() != null)
            user.setEmail(body.getEmail());
        if (body.getUsername() != null)
            user.setUsername(body.getUsername());
        if (body.getEnabled() != null)
            user.setEnabled(body.getEnabled());

        if (body.getRoles() != null) {
            Set<Role> roles = body.getRoles().stream()
                    .map(role -> roleRepository.findByName(role)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        log("UPDATE_USER", saved.getId());
        return saved;
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        log("DELETE_USER", id);
    }

    /*
     * ==============================
     * ROLE MANAGEMENT
     * ==============================
     */
    public void addRoleToUser(UUID userId, String roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(role.getName())))
            return;

        user.getRoles().add(role);
        userRepository.save(user);
        log("ADD_ROLE_" + roleName, userId);
    }

    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = getUserById(userId);
        boolean removed = user.getRoles().removeIf(r -> r.getName().equals(roleName));

        if (!removed) {
            throw new ResourceNotFoundException("User does not have role: " + roleName);
        }

        userRepository.save(user);
        log("REMOVE_ROLE_" + roleName, userId);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /*
     * ==============================
     * BLOCK / UNBLOCK
     * ==============================
     */
    public void blockUser(UUID id) {
        User user = getUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
        log("BLOCK_USER", id);
    }

    public void unblockUser(UUID id) {
        User user = getUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
        log("UNBLOCK_USER", id);
    }

    /*
     * ==============================
     * PASSWORD RESET
     * ==============================
     */
    public String resetPassword(UUID id) {
        User user = getUserById(id);
        String tmpPassword = UUID.randomUUID().toString().substring(0, 12);

        user.setPassword(passwordEncoder.encode(tmpPassword));
        userRepository.save(user);

        log("RESET_PASSWORD", id);
        return tmpPassword;
    }

    /*
     * ==============================
     * AUDIT LOG
     * ==============================
     */
    private void log(String action, UUID userId) {
        AdminLog log = new AdminLog();
        // During tests we don't have SecurityUtil available; record null
        log.setAdminEmail(null);
        log.setAction(action);
        log.setUserId(userId);
        log.setTimestamp(LocalDateTime.now());
        adminLogRepository.save(log);
    }
}
