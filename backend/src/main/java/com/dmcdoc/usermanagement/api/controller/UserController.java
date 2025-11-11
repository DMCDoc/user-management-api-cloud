package com.dmcdoc.usermanagement.api.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;
import com.dmcdoc.sharedcommon.dto.RegisterRequest;
import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.core.service.UserService;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j

public class UserController {

    private final UserService userService;



        @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserProfile(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            Authentication authentication,
            @RequestBody RegisterRequest request) {
        String username = authentication.getName();
        userService.updateProfile(username, request);
        return ResponseEntity.ok("Profil mis à jour");
    }

    // ✅ Un utilisateur connecté supprime son propre compte
    @Transactional
    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteAccount(username);
        return ResponseEntity.ok("Compte supprimé");
    }

    // ✅ ADMIN peut supprimer par ID
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        userService.deleteAccountById(id);
        return ResponseEntity.ok("Utilisateur supprimé : " + id);
    }
    
    // ✅ ADMIN peut supprimer par username
    @PreAuthorize("hasRole('ADMIN')") @DeleteMapping("/by-username/{username}")
    public ResponseEntity<String> deleteByUsername(@PathVariable String username) {
        userService.deleteAccount(username);
        return ResponseEntity.ok("Utilisateur supprimé : " + username);
    }

}