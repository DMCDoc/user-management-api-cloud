package com.example.usermanagement.core.service;

import com.example.sharedcommon.dto.AuthResponse;
import com.example.sharedcommon.dto.LoginRequest;
import com.example.sharedcommon.dto.RefreshRequest;
import com.example.sharedcommon.dto.RegisterRequest;
import com.example.usermanagement.config.security.JwtService;
import com.example.usermanagement.core.model.RefreshToken;
import com.example.usermanagement.core.model.Role;
import com.example.usermanagement.core.model.User;
import com.example.usermanagement.core.repository.RoleRepository;
import com.example.usermanagement.core.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service @RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // 🔹 Inscription
    public AuthResponse register(RegisterRequest request) {
        // Récupération des rôles demandés ou affectation par défaut
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            roles.add(defaultRole);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRoles(roles);

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    // 🔹 Connexion
    public AuthResponse login(LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    // AuthenticationService.java
public AuthResponse refresh(RefreshRequest request) {
    RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
            .map(refreshTokenService::verifyExpiration)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    User user = rt.getUser();

    String accessToken = jwtService.generateToken(user);
    RefreshToken newRt = refreshTokenService.create(user);

    return new AuthResponse(accessToken, newRt.getToken());
}

}
