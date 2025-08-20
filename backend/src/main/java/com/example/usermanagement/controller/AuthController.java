package com.example.usermanagement.controller;

import com.example.usermanagement.dto.AuthResponse;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.dto.RefreshRequest;
import com.example.usermanagement.service.AuthenticationService;
import com.example.usermanagement.service.RefreshTokenService;
import com.example.usermanagement.security.JwtService;
import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.model.User;
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
