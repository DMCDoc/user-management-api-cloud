package com.dmcdoc.usermanagement.api.controller;


import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;
import com.dmcdoc.sharedcommon.dto.RegisterRequest;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.AuthenticationService;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.sharedcommon.dto.AuthResponse;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
   

    // 🔹 Inscription
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    // 🔹 Connexion
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authenticationService.loginByEmail(request);
    }

    // 🔹 Rafraîchir l’access token
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));

        User user = rt.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(newAccessToken, rt.getToken(), user.getEmail());
    }

    @PostMapping("/test-password")
    public String testPassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("Raw: " + rawPassword);
        System.out.println("Encoded: " + encodedPassword);
        System.out.println("Matches: " + passwordEncoder.matches(rawPassword, encodedPassword));

        return "Raw: " + rawPassword + " | Encoded: " + encodedPassword;
    }
}
