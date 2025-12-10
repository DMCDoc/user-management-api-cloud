package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.*;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /*
     * ============================================================
     * OAuth2 Auth — tenant-aware
     * ============================================================
     */
    @Transactional
    public User findOrCreateOAuth2User(UUID tenantId, String email, OAuth2Provider provider) {

        return userRepository.findByEmailAndTenantId(email, tenantId).map(existing -> {

            if (existing.getProvider() != provider) {
                existing.setProvider(provider);
                userRepository.save(existing);
            }
            return existing;

        }).orElseGet(() -> {

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .tenantId(tenantId)
                    .username(email.toLowerCase())
                    .email(email.toLowerCase())
                    .fullName(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .provider(provider)
                    .enabled(true)
                    .roles(Set.of(userRole))
                    .build();

            return userRepository.save(user);
        });
    }

    /*
     * ============================================================
     * Create Tenant Admin
     * ============================================================
     */
    @Transactional
    public User createAdminForTenant(UUID tenantId, String email, String encodedPassword,
            String firstName, String lastName) {

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setTenantId(tenantId);
        u.setEmail(email.toLowerCase());
        u.setUsername(email.toLowerCase());
        u.setPassword(encodedPassword);
        u.setFullName(firstName + " " + lastName);
        u.setEnabled(true);

        assignRolesAndSave(u, List.of("ROLE_TENANT_ADMIN"));
        return u;
    }

    /*
     * ============================================================
     * Refresh Token
     * ============================================================
     */
    @Transactional
    public AuthResponse refreshToken(RefreshRequest request) {
        RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        String newAccess = jwtService.generateToken(rt.getUser());
        return new AuthResponse(newAccess, rt.getToken(), rt.getUser().getEmail());
    }

    /*
     * ============================================================
     * Profile
     * ============================================================
     */
    public Optional<UserResponse> getUserProfile(String username, UUID tenantId) {

        return userRepository.findByUsernameAndTenantId(username, tenantId)
                .map(u -> UserResponse.builder()
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .build());
    }

    // Convenience - tenant-less variant used by controllers when tenant is implicit
    public Optional<UserResponse> getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .map(u -> UserResponse.builder()
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .build());
    }

    @Transactional
    public void updateProfile(String username, UUID tenantId, RegisterRequest request) {

        User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    // Convenience - tenant-less variant
    @Transactional
    public void updateProfile(String username, RegisterRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    /*
     * ============================================================
     * Account Deletion
     * ============================================================
     */
    @Transactional
    public void deleteAccount(UUID userId, UUID tenantId) {

        userRepository.findByIdAndTenantId(userId, tenantId).ifPresent(u -> {
            refreshTokenService.revokeAll(u);
            userRepository.delete(u);
        });
    }

    // Delete by username (tenant-less / convenience)
    @Transactional
    public void deleteAccount(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            refreshTokenService.revokeAll(u);
            userRepository.delete(u);
        });
    }

    // Admin deletion by id
    @Transactional
    public void deleteAccountById(UUID id) {
        userRepository.findById(id).ifPresent(u -> {
            refreshTokenService.revokeAll(u);
            userRepository.delete(u);
        });
    }

    /**
     * Convenience for OAuth2 flows where tenant is not yet known.
     * If a user with the email exists, return it (and set provider if provided).
     * Otherwise create a new user with a generated id and no tenant.
     */
    @Transactional
    public User findOrCreateByEmailOAuth2(String email, OAuth2Provider provider) {

        return userRepository.findByEmail(email).map(existing -> {
            if (provider != null && existing.getProvider() != provider) {
                existing.setProvider(provider);
                userRepository.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .tenantId(null)
                    .username(email.toLowerCase())
                    .email(email.toLowerCase())
                    .fullName(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .provider(provider)
                    .enabled(true)
                    .roles(Set.of(userRole))
                    .build();

            return userRepository.save(user);
        });
    }

    /*
     * ============================================================
     * Register user in tenant
     * ============================================================
     */
    @Transactional
    public User registerUser(UUID tenantId, String email, String rawPassword) {

        Role baseRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .username(email.toLowerCase())
                .email(email.toLowerCase())
                .fullName(email)
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .provider(OAuth2Provider.LOCAL)
                .roles(Set.of(baseRole))
                .build();

        return userRepository.save(user);
    }

    /*
     * ============================================================
     * Utility
     * ============================================================
     */
    @Transactional
    public User assignRolesAndSave(User user, List<String> roleNames) {

        Set<Role> rolesSet = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(rolesSet);
        return userRepository.save(user);
    }
}
