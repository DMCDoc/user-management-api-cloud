package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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
}
