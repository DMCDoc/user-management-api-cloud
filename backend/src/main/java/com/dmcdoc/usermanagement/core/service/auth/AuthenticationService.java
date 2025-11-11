package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;
import com.dmcdoc.sharedcommon.dto.RegisterRequest;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;

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

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getEmail());
    }

    // 🔹 Connexion par EMAIL (nouvelle méthode)
    public AuthResponse loginByEmail(LoginRequest request) {
        // Vérifiez que l'email est fourni
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        // Trouver l'utilisateur par email d'abord
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifiez que le password est fourni
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        // Authentifier avec le username (car Spring Security utilise username)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getEmail());
    }
    // AuthenticationService.java
public AuthResponse refresh(RefreshRequest request) {

    RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
            .map(refreshTokenService::verifyExpiration)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    User user = rt.getUser();

    String accessToken = jwtService.generateToken(user);
    RefreshToken newRt = refreshTokenService.create(user);

    return new AuthResponse(accessToken, newRt.getToken(), user.getEmail());
}

}
