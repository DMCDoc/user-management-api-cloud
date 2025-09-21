package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.model.User;
import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.model.Role;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Service @RequiredArgsConstructor @Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("➡️ [UserService] Register user=" + request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER manquant en DB"));

        User user = User.builder().username(request.getUsername()).email(request.getEmail()).roles(Set.of(userRole))
                .fullName(request.getFullName()).password(passwordEncoder.encode(request.getPassword())).enabled(true)
                .build();

        userRepository.save(user);
        System.out.println("✅ [UserService] User enregistré id=" + user.getId());

        String accessToken = jwtUtils.generateToken(user);
        System.out.println("🔑 [UserService] Access token généré");

        RefreshToken refresh = refreshTokenService.create(user);
        System.out.println("🔄 [UserService] Refresh token généré");

        return new AuthResponse(accessToken, refresh.getToken());
    }

public AuthResponse login(LoginRequest request) {
    System.out.println("➡️ [UserService] Login user=" + request.getUsername());

    try {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

 } catch (BadCredentialsException ex) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        System.out.println("✅ [UserService] Authentifié id=" + user.getId());

        String accessToken = jwtUtils.generateToken(user);
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refresh.getToken());
}

    @Transactional
    public AuthResponse refreshToken(RefreshRequest request) {
        RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token invalide ou expiré"));

        // Génère un nouveau access token (on garde le même refresh token ici,
        // pas de rotation)
        String newAccess = jwtUtils.generateToken(rt.getUser());
        return new AuthResponse(newAccess, rt.getToken());
    }

    public Optional<UserResponse> getUserProfile(String username) {
        return userRepository.findByUsername(username).map(user -> UserResponse.builder().username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName()).build());
    }

    @Transactional
    public void updateProfile(String username, RegisterRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAccount(String username) {
        System.out.println("👉 Transaction active ? " + TransactionSynchronizationManager.isActualTransactionActive());
        userRepository.findByUsername(username).ifPresent(user -> {
            userRepository.delete(user);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAccountById(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            refreshTokenService.revokeAll(user);
            userRepository.delete(user);
        });
    }
}
