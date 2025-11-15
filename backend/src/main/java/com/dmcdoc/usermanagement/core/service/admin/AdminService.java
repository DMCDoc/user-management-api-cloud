package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // pour reset password

    public Page<User> searchUsers(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
    }

    public Map<String, Long> getStats() {
        long total = userRepository.count();
        long admins = userRepository.countByRoles_Name("ROLE_ADMIN");
        long disabled = userRepository.countByEnabledFalse();
        return Map.of("totalUsers", total, "admins", admins, "disabled", disabled);
    }

    public User updateUser(Long unusedId /* keep old if needed */ , UUID userId, String email, String username,
            Boolean enabled, Set<String> roleNames) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (email != null)
            user.setEmail(email);
        if (username != null)
            user.setUsername(username);
        if (enabled != null)
            user.setEnabled(enabled);

        if (roleNames != null) {
            Set<Role> roles = roleNames.stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public String adminResetPassword(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String temp = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        user.setPassword(passwordEncoder.encode(temp));
        userRepository.save(user);
        return temp; // return temp (send via email in prod)
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User updateUserRole(UUID userId, String roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.getRoles().clear();
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public void addRoleToUser(UUID userId, String roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(role.getName()))) {
            return; // idempotent
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = getUserById(userId);
        boolean removed = user.getRoles().removeIf(r -> r.getName().equals(roleName));
        if (!removed) {
            throw new ResourceNotFoundException("User does not have role: " + roleName);
        }
        userRepository.save(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public void blockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void unblockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public String resetPassword(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String tempPassword = UUID.randomUUID().toString().substring(0, 10);

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return tempPassword;
    }

    public void setUserRoles(UUID userId, List<String> roleNames) {
        User user = getUserById(userId);
        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
    }

    // Convenience overload to accept DTO from controller
    public User updateUser(UUID userId, AdminUserUpdateRequest body) {
        if (body == null)
            return getUserById(userId);
        return updateUser(null, userId, body.getEmail(), body.getUsername(), body.getEnabled(), body.getRoles());
    }

}
