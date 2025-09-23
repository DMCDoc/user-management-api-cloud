package com.example.usermanagement.api.controller;

import com.example.sharedcommon.dto.AuthResponse;
import com.example.sharedcommon.dto.LoginRequest;
import com.example.sharedcommon.dto.RefreshRequest;
import com.example.sharedcommon.dto.RegisterRequest;
import com.example.usermanagement.config.security.JwtService;
import com.example.usermanagement.core.model.RefreshToken;
import com.example.usermanagement.core.model.User;
import com.example.usermanagement.core.service.AuthenticationService;
import com.example.usermanagement.core.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    // 🔹 Inscription
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    // 🔹 Connexion
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    // 🔹 Rafraîchir l’access token
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        RefreshToken rt = refreshTokenService.findValid(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));

        User user = rt.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(newAccessToken, rt.getToken());
    }
}
