package com.example.usermanagement.controller;


import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.RefreshRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.usermanagement.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j

public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentative de connexion pour l'utilisateur : {}", request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }

    

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

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