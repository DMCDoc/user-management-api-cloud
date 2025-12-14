package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.RegisterTenantRequest;
import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.*;
import com.dmcdoc.usermanagement.core.service.tenant.TenantService;
import com.dmcdoc.usermanagement.config.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TenantOnboardingController {

    private final TenantService tenantService;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerTenant(
                    @RequestBody RegisterTenantRequest request) {

            // 1️⃣ Create tenant
            UUID tenantId = UUID.randomUUID();
            tenantService.createTenant(
                            tenantId,
                            request.getTenantName(),
                            request.getTenantKey(),
                request.getMetadata());

            // 2️⃣ Create restaurant
            restaurantService.create(
                request.getRestaurantName(),
                        request.getRestaurantAddress(),
                request.getMetadata());

        // 3️⃣ Create admin
        User admin = userService.createAdminForTenant(
                        tenantId,
                request.getAdminEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getFirstName(),
                        request.getLastName());

        // 4️⃣ JWT
        String accessToken = jwtService.generateToken(admin);

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new AuthResponse(accessToken, null, admin.getEmail()));
    }
}
