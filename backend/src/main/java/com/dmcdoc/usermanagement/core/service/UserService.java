package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.config.security.JwtUtils;
import com.dmcdoc.usermanagement.core.model.*;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    /*
     * ============================================================
     * 🔹 Enregistrement local (classique username / password)
     * ============================================================
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("[UserService] Register user={}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nom d'utilisateur déjà pris");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role USER manquant en DB"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .enabled(true)
                .provider(OAuth2Provider.LOCAL)
                .build();

        userRepository.save(user);
        log.info("[UserService] ✅ User enregistré id={}", user.getId());

        String accessToken = jwtUtils.generateToken(user);
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refresh.getToken(), user.getEmail());
    }

    /*
     * ============================================================
     * 🔹 Authentification classique
     * ============================================================
     */
    public AuthResponse login(LoginRequest request) {
        log.info("[UserService] Login user={}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String accessToken = jwtUtils.generateToken(user);
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refresh.getToken(), user.getEmail());
    }

    /*
     * ============================================================
     * 🔹 OAuth2 (Google / Facebook / GitHub)
     * ============================================================
     */
    @Transactional
    public User findOrCreateByEmailOAuth2(String email, OAuth2Provider provider) {
        return userRepository.findByEmail(email).map(existing -> {
            if (existing.getProvider() != provider) {
                log.info("Mise à jour du provider pour {} : {} -> {}", email, existing.getProvider(), provider);
                existing.setProvider(provider);
                userRepository.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("Role USER manquant en DB"));

            User newUser = User.builder()
                    .username(email.split("@")[0])
                    .email(email)
                    .fullName(email)
                    .roles(Set.of(userRole))
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .enabled(true)
                    .provider(provider)
                    .build();

            User saved = userRepository.save(newUser);
            log.info("Nouvel utilisateur OAuth2 créé : {} ({})", saved.getEmail(), provider);
            return saved;
        });
    }

    /*
     * ============================================================
     * 🔹 Token management
     * ============================================================
     */
    @Transactional
    public AuthResponse refreshToken(RefreshRequest request) {
        RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide ou expiré"));

        String newAccess = jwtUtils.generateToken(rt.getUser());
        return new AuthResponse(newAccess, rt.getToken(), rt.getUser().getEmail());
    }

    /*
     * ============================================================
     * 🔹 Profil utilisateur
     * ============================================================
     */
    public Optional<UserResponse> getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .map(u -> UserResponse.builder()
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .build());
    }

    @Transactional
    public void updateProfile(String username, RegisterRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    /*
     * ============================================================
     * 🔹 Suppression de compte
     * ============================================================
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAccount(String username) {
        userRepository.findByUsername(username)
                .ifPresent(u -> {
                    refreshTokenService.revokeAll(u);
                    userRepository.delete(u);
                });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAccountById(Long id) {
        userRepository.findById(id)
                .ifPresent(u -> {
                    refreshTokenService.revokeAll(u);
                    userRepository.delete(u);
                });
    }

    /*
     * ============================================================
     * 🔹 Méthodes utilitaires
     * ============================================================
     */
    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User registerWithEmailOnly(String email) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role USER manquant en DB"));

        User user = User.builder()
                .username(email.split("@")[0])
                .email(email)
                .fullName(email)
                .roles(Set.of(userRole))
                .enabled(true)
                .provider(OAuth2Provider.LOCAL)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public RefreshToken createRefreshTokenForUser(User user) {
        return refreshTokenService.create(user);
    }
}
