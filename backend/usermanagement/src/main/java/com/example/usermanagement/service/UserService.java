package com.example.usermanagement.service;


import com.example.shared.dto.LoginRequest;
import com.example.shared.dto.RegisterRequest;
import com.example.shared.dto.UserResponse;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JWTUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Nom d'utilisateur déjà pris");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        System.out.println("🔐 Tentative de login pour : " + request.getUsername());
        // Authentifie avec AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Récupère l'utilisateur
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifie manuellement le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("❌ Mot de passe incorrect pour l'utilisateur : " + request.getUsername());
            throw new BadCredentialsException("Mot de passe incorrect");
        }

        // Authentification réussie (tu peux supprimer ceci si tu ne veux pas l'utiliser avec Spring Security natif)
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            System.out.println("❌ Échec Spring Security authenticate() : " + ex.getMessage());
            throw new BadCredentialsException("Authentification échouée");
        }

        return jwtUtils.generateToken(user);
    }

    public Optional<UserResponse> getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .map(user -> UserResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .build());
    }

    @Transactional
    public void updateProfile(String username, RegisterRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    public void deleteAccount(String username) {
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }
}
