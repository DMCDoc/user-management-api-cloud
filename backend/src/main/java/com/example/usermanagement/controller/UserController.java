package com.example.usermanagement.controller;

// import specific DTO classes needed from com.example.shared.dto
import com.example.shared.dto.RegisterRequest;
import com.example.shared.dto.LoginRequest;
import com.example.shared.dto.UserResponse;
import com.example.usermanagement.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("🎯 Reçu une tentative de login avec username=" + request.getUsername());
        String jwt = userService.login(request);
        return ResponseEntity.ok(jwt);
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

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteAccount(username);
        return ResponseEntity.ok("Compte supprimé");
    }
    
}