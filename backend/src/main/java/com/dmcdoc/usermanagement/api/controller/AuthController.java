package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
                    @RequestBody LoginRequest request) {

            authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                            request.getUsername(),
                                            request.getPassword()));

            UUID tenantId = TenantContext.getTenantId();
            User user = userService.findByEmailOptional(
                            request.getUsername(),
                            tenantId).orElseThrow();

        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.create(user);

        return ResponseEntity.ok(
                        new AuthResponse(
                                        accessToken,
                                        refreshToken.getToken(),
                                        user.getEmail()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
                    @RequestBody RefreshRequest request) {

            UUID tenantId = TenantContext.getTenantId();
            return ResponseEntity.ok(
                            userService.refreshToken(
                                            request,
                                            tenantId));
    }
}
