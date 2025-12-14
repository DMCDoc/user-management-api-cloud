package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.RegisterTenantRequest;
import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.tenant.provisioning.TenantProvisioningService;
import com.dmcdoc.usermanagement.config.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TenantOnboardingController {

        private final TenantProvisioningService provisioningService;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> registerTenant(
                        @RequestBody RegisterTenantRequest request) {

                User admin = provisioningService.provisionTenant(
                                request,
                                passwordEncoder.encode(request.getPassword()));

                String accessToken = jwtService.generateToken(admin);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new AuthResponse(accessToken, null, admin.getEmail()));
        }
}
