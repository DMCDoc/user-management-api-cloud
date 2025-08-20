package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.model.User;
import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        User user = User.builder().username(request.getUsername()).email(request.getEmail())
                .fullName(request.getFullName()).password(passwordEncoder.encode(request.getPassword())).enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtUtils.generateToken(user); // ton utilitaire
                                                           // existant
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refresh.getToken());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String accessToken = jwtUtils.generateToken(user);
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refresh.getToken());
    }

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

    @Transactional
    public void deleteAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenService.revokeAll(user);
            userRepository.delete(user);
        });
    }
}
