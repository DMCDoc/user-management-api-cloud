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

        return new AuthResponse(newAccessToken, rt.getToken(), user.getEmail());
    }
}
