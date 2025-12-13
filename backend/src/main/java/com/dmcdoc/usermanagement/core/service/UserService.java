package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
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
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /*
     * ============================================================
     * OAuth2
     * ============================================================
     */

    public User findOrCreateByEmailOAuth2(
                    String email,
                    OAuth2Provider provider,
                    UUID tenantId) {

            return userRepository.findByEmailAndTenantId(email, tenantId)
                            .map(existing -> {
                                    if (existing.getProvider() != provider) {
                                            existing.setProvider(provider);
                                    }
                                    return existing;
                            })
                            .orElseGet(() -> {

                                    Role userRole = roleRepository.findByName("ROLE_USER")
                                                    .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

                                    User user = User.builder()
                                                    .email(email)
                                                    .username(email.split("@")[0])
                                                    .fullName(email)
                                                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                                    .roles(Set.of(userRole))
                                                    .provider(provider)
                                                    .build();
                                    user.setTenantId(tenantId);
                                    user.setActive(true);

                                    log.info("OAuth2 user created [{}] for tenant {}", email, tenantId);
                                    return userRepository.save(user);
                            });
    }

    /*
     * ============================================================
     * Admin
     * ============================================================
     */

    public User createAdminForTenant(
                    UUID tenantId,
                    String email,
                    String encodedPassword,
                    String firstName,
                    String lastName) {

            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setUsername(email.toLowerCase().trim());
            user.setPassword(encodedPassword);
            user.setTenantId(tenantId);
            user.setFullName(firstName + " " + lastName);
            user.setActive(true);

            assignRolesAndSave(user, List.of("ROLE_TENANT_ADMIN"));
            return user;
    }

    /*
     * ============================================================
     * Profile
     * ============================================================
     */

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserProfile(
                    String username,
                    UUID tenantId) {

            return userRepository.findByUsernameAndTenantId(username, tenantId)
                            .map(UserMapper::toResponse);
    }

    public void updateProfile(
                    String username,
                    UUID tenantId,
                    RegisterRequest request) {

            User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
    }

    /*
     * ============================================================
     * Deletion
     * ============================================================
     */

    public void deleteAccount(
                    String username,
                    UUID tenantId) {

            User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            refreshTokenService.revokeAll(user);
            userRepository.delete(user);
    }

    public void deleteAccountById(
                    UUID userId,
                    UUID tenantId) {

            User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            refreshTokenService.revokeAll(user);
            userRepository.delete(user);
    }

    /*
     * ============================================================
     * Tokens
     * ============================================================
     */

    public AuthResponse refreshToken(
                    RefreshRequest request,
                    UUID tenantId) {

            RefreshToken rt = refreshTokenService.findValid(
                            request.getRefreshToken())
                            .filter(token -> token.getUser().getTenantId().equals(tenantId))
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                            "Invalid refresh token"));

            String accessToken = jwtService.generateToken(rt.getUser());

            return new AuthResponse(
                            accessToken,
                            rt.getToken(),
                            rt.getUser().getEmail());
    }

    public RefreshToken createRefreshTokenForUser(User user) {
            return refreshTokenService.create(user);
    }

    /*
     * ============================================================
     * Utilities
     * ============================================================
     */

    public Optional<User> findByEmailOptional(String email, UUID tenantId) {
            return userRepository.findByEmailAndTenantId(email, tenantId);
    }

    public User registerWithEmailOnly(String email, UUID tenantId) {

            Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));

        User user = User.builder()
                        .email(email)
                        .username(email.split("@")[0])
                        .fullName(email)
                        .roles(Set.of(role))
                        .provider(OAuth2Provider.LOCAL)
                .build();
        user.setTenantId(tenantId);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User assignRolesAndSave(
                    User user,
                    List<String> roleNames) {

            Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                                            .orElseThrow(() -> new ResponseStatusException(
                                                            HttpStatus.BAD_REQUEST,
                                                            "Role not found: " + name)))
                .collect(Collectors.toSet());

            user.setRoles(roles);
        return userRepository.save(user);
    }
}
