package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;
import com.dmcdoc.usermanagement.core.service.auth.AuthenticationService;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationService authenticationService;

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(
                        @Valid @RequestBody LoginRequest request) {
                UUID tenantId = TenantContext.getTenantId();
                return ResponseEntity.ok(
                                authenticationService.login(request, tenantId));
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refresh(
                        @Valid @RequestBody RefreshRequest request) {
                UUID tenantId = TenantContext.getTenantId();
                return ResponseEntity.ok(
                                authenticationService.refresh(request, tenantId));
        }
}
