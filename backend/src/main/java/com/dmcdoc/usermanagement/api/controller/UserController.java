package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> profile(
            @AuthenticationPrincipal UserDetails principal) {

        return userService.getUserProfile(
                principal.getUsername(),
                TenantContext.getTenantId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody RegisterRequest request) {

        userService.updateProfile(
                principal.getUsername(),
                TenantContext.getTenantId(),
                request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails principal) {

        userService.deleteAccount(
                principal.getUsername(),
                TenantContext.getTenantId());

        return ResponseEntity.noContent().build();
    }
}
