package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
import com.dmcdoc.usermanagement.core.model.*;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.dmcdoc.usermanagement.tenant.hibernate.HibernateSystemQueryExecutor;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final HibernateSystemQueryExecutor hibernateSystemQueryExecutor;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

        @PersistenceContext
        private EntityManager entityManager;

        // Simple in-memory cache for system roles (ROLE_* with tenant =
        // SystemTenant.SYSTEM_TENANT)
        // These roles are immutable in normal operation so a simple cache without TTL
        // is fine.
        

        /**
         * Clear the system role cache. Useful for tests or initialization flows.
         */


        /* ================= OAuth2 ================= */

        @Override
        public User findOrCreateByEmailOAuth2(
                        String email,
                        OAuth2Provider provider,
                        UUID tenantId) {

                requireTenant(tenantId);

                return userRepository.findByEmailAndTenantId(email, tenantId)
                                .map(existing -> {
                                        existing.setProvider(provider);
                                        return existing;
                                })
                                .orElseGet(() -> {
                                        Role role = findSystemRole("ROLE_USER");

                                        User user = User.builder()
                                                        .email(email.toLowerCase())
                                                        .username(email.split("@")[0])
                                                        .fullName(email)
                                                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                                        .roles(Set.of(role))
                                                        .provider(provider)
                                                        .build();

                                        user.setTenantId(tenantId);
                                        return userRepository.save(user);
                                });
        }

        /* ================= Admin ================= */

        @Override
        public User createAdminForTenant(
                        UUID tenantId,
                        String email,
                        String encodedPassword,
                        String firstName,
                        String lastName) {

                requireTenant(tenantId);

                User user = new User();
                user.setEmail(email.toLowerCase());
                user.setUsername(email.toLowerCase());
                user.setPassword(encodedPassword);
                user.setTenantId(tenantId);
                user.setFullName(firstName + " " + lastName);
                user.setActive(true);

                return assignRolesAndSave(user, List.of("ROLE_TENANT_ADMIN"));
        }

        /* ================= Profile ================= */

        @Override
        @Transactional(readOnly = true)
        public Optional<UserResponse> getUserProfile(String username, UUID tenantId) {
                requireTenant(tenantId);

                return userRepository
                                .findByUsernameAndTenantId(username, tenantId)
                                .map(UserMapper::toResponse);
        }

        /* ================= Deletion ================= */

        @Override
        public void deleteAccountById(UUID userId, UUID tenantId) {
                requireTenant(tenantId);

                User user = userRepository
                                .findByIdAndTenantId(userId, tenantId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                refreshTokenService.revokeAll(user);
                userRepository.delete(user);
        }

        @Override
        public void deleteAccount(String username, UUID tenantId) {
                requireTenant(tenantId);

                User user = userRepository
                                .findByUsernameAndTenantId(username, tenantId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                refreshTokenService.revokeAll(user);
                userRepository.delete(user);
        }

        /* ================= Tokens ================= */

        @Override
        public AuthResponse refreshToken(RefreshRequest request, UUID tenantId) {
                requireTenant(tenantId);

                RefreshToken rt = refreshTokenService
                                .findValid(request.getRefreshToken())
                                .filter(token -> token.getUser().getTenantId().equals(tenantId))
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

                String accessToken = jwtService.generateToken(rt.getUser());

                return new AuthResponse(
                                accessToken,
                                rt.getToken(),
                                rt.getUser().getEmail());
        }

        /* ================= Utilities ================= */

        @Override
        public Optional<User> findByEmailOptional(String email, UUID tenantId) {
                requireTenant(tenantId);
                return userRepository.findByEmailAndTenantId(email, tenantId);
        }

        @Override
        public void updateProfile(String username, UUID tenantId, RegisterRequest request) {
                requireTenant(tenantId);

                User user = userRepository
                                .findByUsernameAndTenantId(username, tenantId)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                user.setEmail(request.getEmail());
                user.setFullName(request.getFullName());
        }

        /* ================= Internal helpers ================= */

        private User assignRolesAndSave(User user, List<String> roleNames) {

                Set<Role> roles = roleNames.stream()
                                .map(this::findSystemRole)
                                .collect(Collectors.toSet());

                user.setRoles(roles);
                return userRepository.save(user);
        }

        private Role findSystemRole(String roleName) {
                return hibernateSystemQueryExecutor.findSystemRole(roleName);
        }

        private void requireTenant(UUID tenantId) {
                if (tenantId == null) {
                        throw new IllegalStateException("TenantId is required");
                }
        }
}
