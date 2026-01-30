package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.core.service.UserServiceImpl;
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

    private final UserServiceImpl userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(@AuthenticationPrincipal UserDetails principal) {

            // 🛡️ GARDE-FOU : Si on arrive ici sans user (mauvaise config security), on
            // renvoie 401 proprement
            if (principal == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return userService.getUserProfile(
                            principal.getUsername(),
                            TenantContext.getTenantId())
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody RegisterRequest request) {

        userService.updateProfile(
                principal.getUsername(),
                TenantContext.getTenantId(),
                request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails principal) {

        userService.deleteAccount(
                principal.getUsername(),
                TenantContext.getTenantId());

        return ResponseEntity.noContent().build();
    }
}
