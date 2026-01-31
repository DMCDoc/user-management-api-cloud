package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request, UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required");
        }

        // 1️⃣ Auth Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        // 2️⃣ User scoped tenant
        User user = userRepository
                .findByEmailAndTenantId(request.email(), tenantId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // 3️⃣ Tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getEmail());
    }

    @Override
    public AuthResponse refresh(RefreshRequest request, UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required");
        }

        RefreshToken rt = refreshTokenService
                .findValid(request.getRefreshToken())
                .filter(token -> token.getUser()
                        .getTenantId()
                        .equals(tenantId))
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        User user = rt.getUser();

        String accessToken = jwtService.generateToken(user);
        RefreshToken newRt = refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                newRt.getToken(),
                user.getEmail());
    }
}
